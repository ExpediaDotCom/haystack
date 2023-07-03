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
package com.expedia.www.haystack.commons.kstreams.serde.graph

import java.util

import com.expedia.www.haystack.commons.entities.GraphEdge
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

class GraphEdgeValueSerde extends Serde[GraphEdge] {
  implicit val formats = DefaultFormats

  override def deserializer(): Deserializer[GraphEdge] = new GraphEdgeDeserializer

  override def serializer(): Serializer[GraphEdge] = new GraphEdgeSerializer

  override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

  override def close(): Unit = ()

  class GraphEdgeSerializer extends Serializer[GraphEdge] {
    override def serialize(topic: String, graphEdge: GraphEdge): Array[Byte] = {
      Serialization.write(graphEdge).getBytes("utf-8")
    }

    override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

    override def close(): Unit = ()
  }

  class GraphEdgeDeserializer extends Deserializer[GraphEdge] {
    override def deserialize(topic: String, data: Array[Byte]): GraphEdge = {
      Serialization.read[GraphEdge](new String(data))
    }

    override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

    override def close(): Unit = ()
  }
}

