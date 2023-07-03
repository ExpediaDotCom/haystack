/*
 *
 *     Copyright 2018 Expedia, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */

package com.expedia.www.haystack.service.graph.node.finder.app.metadata

import java.util.Properties
import java.util.concurrent.{ExecutionException, TimeUnit}

import com.expedia.www.haystack.service.graph.node.finder.config.KafkaConfiguration
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.errors.TopicExistsException
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.Try

object TopicCreator {
  private val LOGGER = LoggerFactory.getLogger(TopicCreator.getClass)

  def makeMetadataTopicReady(config: KafkaConfiguration): Unit = {
    if(!config.metadataConfig.autoCreate)
      return

    val properties = new Properties()
    properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, config.streamsConfig.getList(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
    val adminClient = AdminClient.create(properties)
    try {
      val overridesConfig = new java.util.HashMap[String, String]()
      overridesConfig.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT)
      val topics = new NewTopic(
        config.metadataConfig.topic,
        config.metadataConfig.partitionCount,
        config.metadataConfig.replicationFactor.toShort).configs(overridesConfig)
      adminClient.createTopics(List(topics).asJava).values().entrySet().asScala.foreach(entry => {
        try {
          entry.getValue.get()
        } catch {
          case ex: ExecutionException =>
            if (ex.getCause.isInstanceOf[TopicExistsException]) {
              LOGGER.info(s"metadata topic '${config.metadataConfig}' already exists!")
            } else {
              throw new RuntimeException(s"Fail to create the metadata topic ${config.metadataConfig}", ex)
            }
          case ex: Exception => throw new RuntimeException(s"Fail to create the metadata topic ${config.metadataConfig}", ex)
        }
      })
    }
    finally {
      Try(adminClient.close(5, TimeUnit.SECONDS))
    }
  }
}
