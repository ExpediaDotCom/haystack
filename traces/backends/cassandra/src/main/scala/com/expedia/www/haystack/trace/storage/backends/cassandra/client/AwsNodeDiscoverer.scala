/*
 *  Copyright 2017 Expedia, Inc.
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

package com.expedia.www.haystack.trace.storage.backends.cassandra.client

import java.util.Collections

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeInstancesRequest, Filter, Instance, InstanceStateName}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object AwsNodeDiscoverer {
  private val LOGGER = LoggerFactory.getLogger(AwsNodeDiscoverer.getClass)

  /**
    * discovers the EC2 ip addresses on AWS for a given region and set of tags
    * @param region aws region
    * @param tags a set of ec2 node tags
    * @return
    */
  def discover(region: String,
               tags: Map[String, String]): Seq[String] = {
    LOGGER.info(s"discovering EC2 nodes for region=$region, and tags=${tags.mkString(",")}")

    val awsRegion = Region.getRegion(Regions.fromName(region))
    val client:AmazonEC2Client = new AmazonEC2Client().withRegion(awsRegion)
    try {
      discover(client, tags)
    } catch {
      case ex: Exception =>
        LOGGER.error(s"Fail to discover EC2 nodes for region=$region and tags=$tags with reason", ex)
        throw new RuntimeException(ex)
    } finally {
      client.shutdown()
    }
  }

  /**
    * discovers the EC2 ip addresses on AWS for a given region and set of tags
    * @param client ec2 client
    * @param tags a set of ec2 node tags
    * @return
    */
  private[haystack] def discover(client: AmazonEC2Client, tags: Map[String, String]): Seq[String] = {
    val filters = tags.map { case (key, value) => new Filter("tag:" + key, Collections.singletonList(value)) }
    val request = new DescribeInstancesRequest().withFilters(filters.asJavaCollection)

    val result = client.describeInstances(request)

    val nodes = result.getReservations
      .asScala
      .flatMap(_.getInstances.asScala)
      .filter(isValidInstance)
      .map(_.getPrivateIpAddress)

    LOGGER.info("EC2 nodes discovered [{}]", nodes.mkString(","))
    nodes
  }

  // check if an ec2 instance is in running state
  private def isValidInstance(instance: Instance): Boolean = {
    // instance should be in running state
    InstanceStateName.Running.toString.equals(instance.getState.getName)
  }
}
