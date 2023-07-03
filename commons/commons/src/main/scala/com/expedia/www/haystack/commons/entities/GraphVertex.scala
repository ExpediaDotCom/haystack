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
package com.expedia.www.haystack.commons.entities


/**
  * Vertex of a graph that includes the name of the vertex and tags associated with the vertex
  * @param name: Name of the service vertex
  * @param tags: List of tag names associated with the service vertex
  */
case class GraphVertex(name: String, tags: Map[String, String] = Map.empty[String, String])

