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

import java.time.Instant
import java.time.temporal.ChronoUnit

import org.scalatest.{FunSpec, Matchers}

import scala.io.{BufferedSource, Codec, Source}

class SnapshotStoreSpecBase extends FunSpec with Matchers {
  protected val now: Instant = Instant.EPOCH

  protected val twoMillisecondsBeforeNow: Instant = now.minus(2, ChronoUnit.MILLIS)

  protected val oneMillisecondBeforeNow: Instant = now.minus(1, ChronoUnit.MILLIS)

  protected val oneMillisecondAfterNow: Instant = now.plus(1, ChronoUnit.MILLIS)

  protected val twoMillisecondsAfterNow: Instant = now.plus(2, ChronoUnit.MILLIS)

  def readFile(fileName: String): String = {
    implicit val codec: Codec = Codec.UTF8
    lazy val bufferedSource: BufferedSource = Source.fromResource(fileName)
    val fileContents = bufferedSource.getLines.mkString("\n")
    bufferedSource.close()
    fileContents + "\n"
  }

}
