/*
 *  Copyright 2019, Expedia Group.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.www.haystack.trace.indexer.config

import java.util.Properties

import com.expedia.www.haystack.commons.config.ConfigurationLoader
import com.expedia.www.haystack.commons.retries.RetryOperation
import com.expedia.www.haystack.trace.commons.config.entities._
import com.expedia.www.haystack.trace.commons.config.reload.{ConfigurationReloadElasticSearchProvider, Reloadable}
import com.expedia.www.haystack.trace.commons.packer.PackerType
import com.expedia.www.haystack.trace.indexer.config.entities._
import com.expedia.www.haystack.trace.indexer.serde.SpanDeserializer
import com.typesafe.config.Config
import org.apache.commons.lang3.StringUtils
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringDeserializer, StringSerializer}

import scala.collection.JavaConverters._
import scala.util.Try

class ProjectConfiguration extends AutoCloseable {
  private val config = ConfigurationLoader.loadConfigFileWithEnvOverrides()

  val healthStatusFilePath: String = config.getString("health.status.path")

  /**
    * span accumulation related configuration like max buffered records, buffer window, poll interval
    *
    * @return a span config object
    */
  val spanAccumulateConfig: SpanAccumulatorConfiguration = {
    val cfg = config.getConfig("span.accumulate")
    SpanAccumulatorConfiguration(
      cfg.getInt("store.min.traces.per.cache"),
      cfg.getInt("store.all.max.entries"),
      cfg.getLong("poll.ms"),
      cfg.getLong("window.ms"),
      PackerType.withName(cfg.getString("packer").toUpperCase))
  }

  /**
    *
    * @return streams configuration object
    */
  val kafkaConfig: KafkaConfiguration = {
    // verify if the applicationId and bootstrap server config are non empty
    def verifyAndUpdateConsumerProps(props: Properties): Unit = {
      require(props.getProperty(ConsumerConfig.GROUP_ID_CONFIG).nonEmpty)
      require(props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG).nonEmpty)

      // make sure auto commit is false
      require(props.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG) == "false")

      // set the deserializers explicitly
      props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
      props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, new SpanDeserializer().getClass.getCanonicalName)
    }

    def verifyAndUpdateProducerProps(props: Properties): Unit = {
      require(props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG).nonEmpty)
      props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
      props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getCanonicalName)
    }

    def addProps(config: Config, props: Properties): Unit = {
      if (config != null) {
        config.entrySet().asScala foreach {
          kv => {
            props.setProperty(kv.getKey, kv.getValue.unwrapped().toString)
          }
        }
      }
    }

    val kafka = config.getConfig("kafka")
    val producerConfig = if (kafka.hasPath("producer")) kafka.getConfig("producer") else null
    val consumerConfig = kafka.getConfig("consumer")

    val consumerProps = new Properties
    val producerProps = new Properties

    // producer specific properties
    addProps(producerConfig, producerProps)

    // consumer specific properties
    addProps(consumerConfig, consumerProps)

    // validate consumer props
    verifyAndUpdateConsumerProps(consumerProps)
    verifyAndUpdateProducerProps(producerProps)

    KafkaConfiguration(
      numStreamThreads = kafka.getInt("num.stream.threads"),
      pollTimeoutMs = kafka.getLong("poll.timeout.ms"),
      consumerProps = consumerProps,
      producerProps = producerProps,
      produceTopic = if (kafka.hasPath("topic.produce")) kafka.getString("topic.produce") else "",
      consumeTopic = kafka.getString("topic.consume"),
      consumerCloseTimeoutInMillis = kafka.getInt("close.stream.timeout.ms"),
      commitOffsetRetries = kafka.getInt("commit.offset.retries"),
      commitBackoffInMillis = kafka.getLong("commit.offset.backoff.ms"),
      maxWakeups = kafka.getInt("max.wakeups"),
      wakeupTimeoutInMillis = kafka.getInt("wakeup.timeout.ms"))
  }


  /**
    *
    * trace backend configuration object
    */
  val backendConfig: TraceBackendConfiguration = {
    val traceBackendConfig = config.getConfig("backend")

    val grpcClients = traceBackendConfig.entrySet().asScala
      .map(k => StringUtils.split(k.getKey, '.')(0)).toSeq
      .map(cl => traceBackendConfig.getConfig(cl))
      .filter(cl => cl.hasPath("host") && cl.hasPath("port"))
      .map(cl => GrpcClientConfig(cl.getString("host"), cl.getInt("port")))

    // we dont support multiple backends for write operations
    require(grpcClients.size == 1)

    TraceBackendConfiguration(
      TraceStoreBackends(grpcClients),
      maxInFlightRequests = traceBackendConfig.getInt("max.inflight.requests"))

  }

  /**
    * service metadata write configuration
    */
  val serviceMetadataWriteConfig: ServiceMetadataWriteConfiguration = {
    val serviceMetadata = config.getConfig("service.metadata")
    val es = serviceMetadata.getConfig("es")
    val templateJsonConfigField = "index.template.json"
    val indexTemplateJson = if (es.hasPath(templateJsonConfigField)
      && StringUtils.isNotEmpty(es.getString(templateJsonConfigField))) {
      Some(es.getString(templateJsonConfigField))
    } else {
      None
    }
    val username = if (es.hasPath("username")) Option(es.getString("username")) else None
    val password = if (es.hasPath("password")) Option(es.getString("password")) else None
    ServiceMetadataWriteConfiguration(
      enabled = serviceMetadata.getBoolean("enabled"),
      flushIntervalInSec = serviceMetadata.getInt("flush.interval.sec"),
      flushOnMaxOperationCount = serviceMetadata.getInt("flush.operation.count"),
      esEndpoint = es.getString("endpoint"),
      username = username,
      password = password,
      consistencyLevel = es.getString("consistency.level"),
      indexName = es.getString("index.name"),
      indexType = es.getString("index.type"),
      indexTemplateJson = indexTemplateJson,
      connectionTimeoutMillis = es.getInt("conn.timeout.ms"),
      readTimeoutMillis = es.getInt("read.timeout.ms"),
      maxInFlightBulkRequests = es.getInt("bulk.max.inflight"),
      maxDocsInBulk = es.getInt("bulk.max.docs.count"),
      maxBulkDocSizeInBytes = es.getInt("bulk.max.docs.size.kb") * 1000,
      retryConfig = RetryOperation.Config(
        es.getInt("retries.max"),
        es.getLong("retries.backoff.initial.ms"),
        es.getDouble("retries.backoff.factor"))
    )
  }

  /**
    *
    * elastic search configuration object
    */
  val elasticSearchConfig: ElasticSearchConfiguration = {
    val es = config.getConfig("elasticsearch")
    val indexConfig = es.getConfig("index")

    val templateJsonConfigField = "template.json"
    val indexTemplateJson = if (indexConfig.hasPath(templateJsonConfigField)
      && StringUtils.isNotEmpty(indexConfig.getString(templateJsonConfigField))) {
      Some(indexConfig.getString(templateJsonConfigField))
    } else {
      None
    }
    val ausername = if (es.hasPath("username")) Option(es.getString("username")) else None
    val apassword = if (es.hasPath("password")) Option(es.getString("password")) else None

    ElasticSearchConfiguration(
      endpoint = es.getString("endpoint"),
      username = ausername,
      password = apassword,
      indexTemplateJson,
      consistencyLevel = es.getString("consistency.level"),
      indexNamePrefix = indexConfig.getString("name.prefix"),
      indexHourBucket = indexConfig.getInt("hour.bucket"),
      indexType = indexConfig.getString("type"),
      connectionTimeoutMillis = es.getInt("conn.timeout.ms"),
      readTimeoutMillis = es.getInt("read.timeout.ms"),
      maxConnectionsPerRoute = es.getInt("max.connections.per.route"),
      maxInFlightBulkRequests = es.getInt("bulk.max.inflight"),
      maxDocsInBulk = es.getInt("bulk.max.docs.count"),
      maxBulkDocSizeInBytes = es.getInt("bulk.max.docs.size.kb") * 1000,
      retryConfig = RetryOperation.Config(
        es.getInt("retries.max"),
        es.getLong("retries.backoff.initial.ms"),
        es.getDouble("retries.backoff.factor")),
      awsRequestSigningConfig(config.getConfig("elasticsearch.signing.request.aws")))
  }

  private def awsRequestSigningConfig(awsESConfig: Config): AWSRequestSigningConfiguration = {
    val accessKey: Option[String] = if (awsESConfig.hasPath("access.key") && awsESConfig.getString("access.key").nonEmpty) {
      Some(awsESConfig.getString("access.key"))
    } else
      None

    val secretKey: Option[String] = if (awsESConfig.hasPath("secret.key") && awsESConfig.getString("secret.key").nonEmpty) {
      Some(awsESConfig.getString("secret.key"))
    } else
      None

    AWSRequestSigningConfiguration(
      awsESConfig.getBoolean("enabled"),
      awsESConfig.getString("region"),
      awsESConfig.getString("service.name"),
      accessKey,
      secretKey)
  }

  /**
    * configuration that contains list of tags that should be indexed for a span
    */
  val indexConfig: WhitelistIndexFieldConfiguration = {
    val indexConfig = WhitelistIndexFieldConfiguration()
    indexConfig.reloadConfigTableName = Option(config.getConfig("reload.tables").getString("index.fields.config"))
    indexConfig
  }

  // configuration reloader
  private val reloader = registerReloadableConfigurations(List(indexConfig))

  /**
    * registers a reloadable config object to reloader instance.
    * The reloader registers them as observers and invokes them periodically when it re-reads the
    * configuration from an external store
    *
    * @param observers list of reloadable configuration objects
    * @return the reloader instance that uses ElasticSearch as an external database for storing the configs
    */
  private def registerReloadableConfigurations(observers: Seq[Reloadable]): ConfigurationReloadElasticSearchProvider = {
    val reload = config.getConfig("reload")
    val reloadConfig = ReloadConfiguration(
      reload.getString("config.endpoint"),
      reload.getString("config.database.name"),
      reload.getInt("interval.ms"),
      if (reload.hasPath("config.username")) Option(reload.getString("config.username")) else None,
      if (reload.hasPath("config.password")) Option(reload.getString("config.password")) else None,
      observers,
      loadOnStartup = reload.getBoolean("startup.load"))

    val loader = new ConfigurationReloadElasticSearchProvider(reloadConfig, awsRequestSigningConfig(config.getConfig("reload.signing.request.aws")))
    if (reloadConfig.loadOnStartup) loader.load()
    loader
  }

  override def close(): Unit = {
    Try(reloader.close())
  }
}
