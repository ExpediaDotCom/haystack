/*
 *  Copyright 2018 Expedia, Inc.
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

package com.expedia.www.haystack.collector.commons;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is used by ProtoSpanExtractor to keep track of the number of operation names for a particular service.
 * It is written in Java because Java's Atomic classes are the preferred way of handling concurrent maps and sets
 * in Scala, and the accesses to the objects that count operation names come from multiple threads.
 */
public class TtlAndOperationNames {
    public final Set<String> operationNames = ConcurrentHashMap.newKeySet();
    private final AtomicLong ttlMillis;

    TtlAndOperationNames(long ttlMillis) {
        this.ttlMillis = new AtomicLong(ttlMillis);
    }

    public long getTtlMillis() {
        return ttlMillis.get();
    }

    public void setTtlMillis(long ttlMillis) {
        this.ttlMillis.set(ttlMillis);
    }
}
