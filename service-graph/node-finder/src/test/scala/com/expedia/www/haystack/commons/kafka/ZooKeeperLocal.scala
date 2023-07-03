package com.expedia.www.haystack.commons.kafka

import java.io.IOException
import java.util.Properties

import org.apache.zookeeper.server.quorum.QuorumPeerConfig
import org.apache.zookeeper.server.{ServerConfig, ZooKeeperServerMain}
import org.slf4j.LoggerFactory


object ZooKeeperLocal {
  private val LOGGER = LoggerFactory.getLogger(classOf[ZooKeeperLocal])
}

class ZooKeeperLocal(val zkProperties: Properties) extends Runnable {
  private val quorumConfiguration = new QuorumPeerConfig
  quorumConfiguration.parseProperties(zkProperties)
  private val configuration = new ServerConfig
  configuration.readFrom(quorumConfiguration)
  private val zooKeeperServer = new ZooKeeperServerMain

  override def run(): Unit = {
    try {
      zooKeeperServer.runFromConfig(configuration)
    }
    catch {
      case e: IOException =>
        ZooKeeperLocal.LOGGER.error("Zookeeper startup failed.", e)
    }
  }
}
