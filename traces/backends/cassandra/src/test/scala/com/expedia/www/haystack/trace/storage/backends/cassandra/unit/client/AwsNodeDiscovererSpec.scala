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

package com.expedia.www.haystack.trace.storage.backends.cassandra.unit.client

import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model._
import com.expedia.www.haystack.trace.storage.backends.cassandra.client.AwsNodeDiscoverer
import org.easymock.EasyMock
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConverters._

class AwsNodeDiscovererSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("AWS node discovery") {
    it("should return only the running nodes for given ec2 tags") {
      val client = mock[AmazonEC2Client]
      val ec2Tags = Map("name" -> "cassandra")

      val instance_1 = new Instance().withPrivateIpAddress("10.0.0.1").withState(new InstanceState().withName(InstanceStateName.Running))
      val instance_2 = new Instance().withPrivateIpAddress("10.0.0.2").withState(new InstanceState().withName(InstanceStateName.Running))
      val instance_3 = new Instance().withPrivateIpAddress("10.0.0.3").withState(new InstanceState().withName(InstanceStateName.Terminated))
      val reservation = new Reservation().withInstances(instance_1, instance_2, instance_3)

      val capturedRequest = EasyMock.newCapture[DescribeInstancesRequest]()
      expecting {
        client.describeInstances(EasyMock.capture(capturedRequest)).andReturn(new DescribeInstancesResult().withReservations(reservation))
      }

      whenExecuting(client) {
        val ips = AwsNodeDiscoverer.discover(client, ec2Tags)
        ips should contain allOf ("10.0.0.1", "10.0.0.2")
        capturedRequest.getValue.getFilters.asScala.foreach(filter => {
          filter.getName shouldEqual "tag:name"
          filter.getValues.asScala.head shouldEqual "cassandra"
        })
      }
    }
  }
}