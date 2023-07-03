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

package com.expedia.www.haystack.kinesis.span.collector.config.entities

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel

import scala.concurrent.duration.FiniteDuration

case class KinesisConsumerConfiguration(awsRegion: String,
                                        stsRoleArn: Option[String],
                                        appGroupName: String,
                                        streamName: String,
                                        streamPosition: InitialPositionInStream,
                                        checkpointInterval: FiniteDuration,
                                        checkpointRetries: Int,
                                        checkpointRetryInterval: FiniteDuration,
                                        kinesisEndpoint: Option[String],
                                        dynamoEndpoint: Option[String],
                                        dynamoTableName: Option[String],
                                        maxRecordsToRead: Int,
                                        idleTimeBetweenReads: FiniteDuration,
                                        shardSyncInterval: FiniteDuration,
                                        metricsLevel: MetricsLevel,
                                        metricsBufferTime: FiniteDuration,
                                        taskBackoffTime: FiniteDuration)
