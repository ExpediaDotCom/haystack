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
package com.expedia.www.haystack.service.graph.snapshot.store

object Constants {
  private val DotJson = ".json"

  val DotCsv = ".csv"
  val SlashNodes = "/nodes"
  val SlashEdges = "/edges"
  val _Nodes = "_nodes"
  val _Edges = "_edges"
  val SourceKey: String = "source"
  val EdgesKey: String = "edges"
  val DestinationKey: String = "destination"
  val StatsKey: String = "stats"
  val TagsKey: String = "tags"
  val CountKey: String = "count"
  val LastSeenKey: String = "lastSeen"
  val ErrorCountKey: String = "errorCount"
  val EffectiveFromKey: String = "effectiveFrom"
  val EffectiveToKey: String = "effectiveTo"
  val IdKey: String = "id"
  val NameKey: String = "name"
  val InfrastructureProviderKey: String = "X-HAYSTACK-INFRASTRUCTURE-PROVIDER"
  val TierKey: String = "tier"
  val ServiceGraph: String = "serviceGraph"
  val JsonFileNameWithExtension: String = ServiceGraph + DotJson
  val NodesCsvFileNameWithExtension: String = ServiceGraph + _Nodes + DotCsv
  val EdgesCsvFileNameWithExtension: String = ServiceGraph + _Edges + DotCsv
}
