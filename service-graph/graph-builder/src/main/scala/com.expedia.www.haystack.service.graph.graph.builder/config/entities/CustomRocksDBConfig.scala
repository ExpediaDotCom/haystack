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

package com.expedia.www.haystack.service.graph.graph.builder.config.entities

import java.util

import com.expedia.www.haystack.service.graph.graph.builder.config.entities.CustomRocksDBConfig._
import com.google.common.annotations.VisibleForTesting
import com.typesafe.config.{Config, ConfigRenderOptions}
import org.apache.kafka.streams.state.RocksDBConfigSetter
import org.rocksdb.{BlockBasedTableConfig, Options}
import org.slf4j.{Logger, LoggerFactory}

object CustomRocksDBConfig {
  protected val LOGGER: Logger = LoggerFactory.getLogger(classOf[CustomRocksDBConfig])

  @VisibleForTesting var rocksDBConfig: Config = _
  def setRocksDbConfig(cfg: Config): Unit = rocksDBConfig = cfg
}

class CustomRocksDBConfig extends RocksDBConfigSetter {

  override def setConfig(storeName: String, options: Options, configs: util.Map[String, AnyRef]): Unit = {
    require(rocksDBConfig != null, "rocksdb config should not be empty or null")

    LOGGER.info("setting rocksdb configuration '{}'",
      rocksDBConfig.root().render(ConfigRenderOptions.defaults().setOriginComments(false)))

    val tableConfig = new BlockBasedTableConfig
    tableConfig.setBlockCacheSize(rocksDBConfig.getLong("block.cache.size"))
    tableConfig.setBlockSize(rocksDBConfig.getLong("block.size"))
    tableConfig.setCacheIndexAndFilterBlocks(rocksDBConfig.getBoolean("cache.index.and.filter.blocks"))
    options.setTableFormatConfig(tableConfig)
    options.setMaxWriteBufferNumber(rocksDBConfig.getInt("max.write.buffer.number"))
  }
}
