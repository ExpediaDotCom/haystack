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

package com.expedia.www.haystack.trace.indexer.store

import com.expedia.www.haystack.trace.indexer.store.traits.CacheSizeObserver

import scala.collection.mutable

class DynamicCacheSizer(val minTracesPerCache: Int, maxEntriesAcrossCaches: Int) {

  private val cacheObservers = mutable.HashSet[CacheSizeObserver]()

  /**
    * adds cache observer
    *
    * @param observer state store acts as an observer
    */
  def addCacheObserver(observer: CacheSizeObserver): Unit = {
    this.synchronized {
      cacheObservers.add(observer)
      evaluateNewCacheSizeAndNotify(cacheObservers)
    }
  }

  /**
    * removes cache observer
    * @param observer state store acts as an observer
    */
  def removeCacheObserver(observer: CacheSizeObserver): Unit = {
    this.synchronized {
      cacheObservers.remove(observer)
      evaluateNewCacheSizeAndNotify(cacheObservers)
    }
  }

  /**
    * Cache sizing strategy is simple, distribute the maxEntriesAcrossCaches across all observers
    * @param observers list of changed observers
    */
  private def evaluateNewCacheSizeAndNotify(observers: mutable.HashSet[CacheSizeObserver]): Unit = {
    //notify the observers with a change in their cache size
    def notifyObservers(newMaxEntriesPerCache: Int): Unit = {
      observers.foreach(obs => obs.onCacheSizeChange(newMaxEntriesPerCache))
    }

    if(observers.nonEmpty) {
      val newMaxEntriesPerCache = Math.floor(maxEntriesAcrossCaches / observers.size).toInt
      notifyObservers(newMaxEntriesPerCache)
    }
  }
}
