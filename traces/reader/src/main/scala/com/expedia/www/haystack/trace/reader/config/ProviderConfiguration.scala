/*
 *  Copyright 2019, Expedia Group.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */

package com.expedia.www.haystack.trace.reader.config

import java.util

import com.expedia.www.haystack.commons.config.ConfigurationLoader
import com.expedia.www.haystack.trace.commons.config.entities._
import com.expedia.www.haystack.trace.commons.config.reload.{ConfigurationReloadElasticSearchProvider, Reloadable}
import com.expedia.www.haystack.trace.reader.config.entities._
import com.expedia.www.haystack.trace.reader.readers.transformers.{PartialSpanTransformer, SpanTreeTransformer, TraceTransformer}
import com.expedia.www.haystack.trace.reader.readers.validators.TraceValidator
import com.typesafe.config.Config
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

class ProviderConfiguration {
  private val config: Config = ConfigurationLoader.loadConfigFileWithEnvOverrides()

  val serviceConfig: ServiceConfiguration = {
    val serviceConfig = config.getConfig("service")

    val ssl = serviceConfig.getConfig("ssl")
    val sslConfig = SslConfiguration(ssl.getBoolean("enabled"), ssl.getString("cert.path"), ssl.getString("private.key.path"))

    ServiceConfiguration(serviceConfig.getInt("port"), sslConfig, serviceConfig.getInt("max.message.size"))
  }

  /**
    * trace backend configuration object
    */
  val traceBackendConfiguration: TraceStoreBackends = {
    val traceBackendConfig = config.getConfig("backend")

    val grpcClients = traceBackendConfig.entrySet().asScala
      .map(k => StringUtils.split(k.getKey, '.')(0)).toSeq
      .map(cl => traceBackendConfig.getConfig(cl))
      .filter(cl => cl.hasPath("host") && cl.hasPath("port"))
      .map(cl => GrpcClientConfig(cl.getString("host"), cl.getInt("port")))

    require(grpcClients.nonEmpty)

    TraceStoreBackends(grpcClients)
  }


  /**
    * ElasticSearch configuration
    */


  private val elasticSearchClientConfig: ElasticSearchClientConfiguration = {
    val es = config.getConfig("elasticsearch.client")

    val username = if (es.hasPath("username")) {
      Option(es.getString("username"))
    } else None
    val password = if (es.hasPath("password")) {
      Option(es.getString("password"))
    } else None

    ElasticSearchClientConfiguration(
      endpoint = es.getString("endpoint"),
      username = username,
      password = password,
      connectionTimeoutMillis = es.getInt("conn.timeout.ms"),
      readTimeoutMillis = es.getInt("read.timeout.ms")
    )
  }
  private val spansIndexConfiguration: SpansIndexConfiguration = {
    val indexConfig = config.getConfig("elasticsearch.index.spans")
    SpansIndexConfiguration(
      indexNamePrefix = indexConfig.getString("name.prefix"),
      indexType = indexConfig.getString("type"),
      indexHourBucket = indexConfig.getInt("hour.bucket"),
      indexHourTtl = indexConfig.getInt("hour.ttl"),
      useRootDocumentStartTime = indexConfig.getBoolean("use.root.doc.starttime")
    )
  }
  private val serviceMetadataIndexConfig: ServiceMetadataIndexConfiguration = {
    val metadataCfg = config.getConfig("elasticsearch.index.service.metadata")

    ServiceMetadataIndexConfiguration(
      metadataCfg.getBoolean("enabled"),
      metadataCfg.getString("name"),
      metadataCfg.getString("type"))
  }

  private def awsRequestSigningConfig(awsESConfig: Config): AWSRequestSigningConfiguration = {
    val accessKey: Option[String] = if (awsESConfig.hasPath("access.key") && awsESConfig.getString("access.key").nonEmpty)
      Some(awsESConfig.getString("access.key"))
    else
      None

    val secretKey: Option[String] = if (awsESConfig.hasPath("secret.key") && awsESConfig.getString("secret.key").nonEmpty) {
      Some(awsESConfig.getString("secret.key"))
    }
    else
      None

    AWSRequestSigningConfiguration(
      awsESConfig.getBoolean("enabled"),
      awsESConfig.getString("region"),
      awsESConfig.getString("service.name"),
      accessKey,
      secretKey)
  }


  val elasticSearchConfiguration: ElasticSearchConfiguration = {
    ElasticSearchConfiguration(
      clientConfiguration = elasticSearchClientConfig,
      spansIndexConfiguration = spansIndexConfiguration,
      serviceMetadataIndexConfiguration = serviceMetadataIndexConfig,
      awsRequestSigningConfiguration = awsRequestSigningConfig(config.getConfig("elasticsearch.signing.request.aws"))
    )
  }

  private def toInstances[T](classes: util.List[String])(implicit ct: ClassTag[T]): scala.Seq[T] = {
    classes.asScala.map(className => {
      val c = Class.forName(className)

      if (c == null) {
        throw new RuntimeException(s"No class found with name $className")
      } else {
        val o = c.newInstance()
        val baseClass = ct.runtimeClass

        if (!baseClass.isInstance(o)) {
          throw new RuntimeException(s"${c.getName} is not an instance of ${baseClass.getName}")
        }
        o.asInstanceOf[T]
      }
    })
  }

  /**
    * Configurations to specify what all transforms to apply on traces
    */
  val traceTransformerConfig: TraceTransformersConfiguration = {
    val preTransformers = config.getStringList("trace.transformers.pre.sequence")
    val postTransformers = config.getStringList("trace.transformers.post.sequence")

    val preTransformerInstances = toInstances[TraceTransformer](preTransformers)
    var postTransformerInstances = toInstances[SpanTreeTransformer](postTransformers).filterNot(_.isInstanceOf[PartialSpanTransformer])
    postTransformerInstances = new PartialSpanTransformer +: postTransformerInstances

    TraceTransformersConfiguration(preTransformerInstances, postTransformerInstances)
  }

  /**
    * Configurations to specify what all validations to apply on traces
    */
  val traceValidatorConfig: TraceValidatorsConfiguration = {
    val validatorConfig: Config = config.getConfig("trace.validators")
    TraceValidatorsConfiguration(toInstances[TraceValidator](validatorConfig.getStringList("sequence")))
  }

  /**
    * configuration that contains list of tags that should be indexed for a span
    */
  val whitelistedFieldsConfig: WhitelistIndexFieldConfiguration = {
    val whitelistedFieldsConfig = WhitelistIndexFieldConfiguration()
    whitelistedFieldsConfig.reloadConfigTableName = Option(config.getConfig("reload.tables").getString("index.fields.config"))
    whitelistedFieldsConfig
  }

  // configuration reloader
  registerReloadableConfigurations(List(whitelistedFieldsConfig))

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
      if (reload.hasPath("config.username")) {
        Option(reload.getString("config.username"))
      } else {
        None
      },
      if (reload.hasPath("config.password")) {
        Option(reload.getString("config.password"))
      } else {
        None
      },
      observers,
      loadOnStartup = reload.getBoolean("startup.load"))

    val awsConfig: AWSRequestSigningConfiguration = awsRequestSigningConfig(config.getConfig("reload.signing.request.aws"))
    val loader = new ConfigurationReloadElasticSearchProvider(reloadConfig, awsConfig)
    if (reloadConfig.loadOnStartup) loader.load()
    loader
  }
}
