/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class CompareUtilTestCase {

    @Test
    public void testEqual() {
        int numberOfParallelThreads = Runtime.getRuntime().availableProcessors();
        long numberOfEqualOperations = 100;
        double probabilityOf12 = 0.5;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfParallelThreads);

        final Vector object1 = new Vector();
        object1.add(new Object());
        object1.add(new Object());
        object1.add(new Object());
        object1.add(new Object());
        object1.add(new Object());
        final Vector object2 = new Vector();
        object2.add(new Object());
        object2.add(new Object());
        object2.add(new Object());
        object2.add(new Object());
        object2.add(new Object());
        object2.add(new Object());
        object2.add(new Object());
        object2.add(new Object());
        object2.add(new Object());
        object2.add(new Object());
        final boolean areEqual = object1.equals(object2);
        final AtomicBoolean wrongResult = new AtomicBoolean(false);

        Runnable equal12 = new Runnable() {
            public void run() {
                if (areEqual != CompareUtil.equal(object1, object2)) {
                    wrongResult.set(true);
                }
            }
        };
        Runnable equal21 = new Runnable() {
            public void run() {
                if (areEqual != CompareUtil.equal(object2, object1)) {
                    wrongResult.set(true);
                }
            }
        };

        List<Future<?>> futures = new ArrayList<Future<?>>();
        for (int i = 1; i <= numberOfEqualOperations; i++) {
            Runnable randomTask = Math.random() < probabilityOf12 ? equal12 : equal21;
            futures.add(executor.submit(randomTask));
        }

        Exception exception = null;
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            exception = e;
        }

        assertNull(exception);
        assertFalse(wrongResult.get());
    }
}
