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

package org.apache.fop.fotreetest;

import java.util.Collections;
import java.util.List;

/**
 * This class collects failures for assertions injected into the FO stream.
 */
public class ResultCollector {

    private static ResultCollector instance = null;

    private List results = new java.util.ArrayList();

    /** @return the ResultCollector singleton */
    public static ResultCollector getInstance() {
        if (instance == null) {
            instance = new ResultCollector();
        }
        return instance;
    }

    /** Main constructor. */
    public ResultCollector() {
        //nop
    }

    /**
     * This notifies the ResultCollector about an assertion failure.
     *
     * @param message   the message containing the details
     */
    public void notifyAssertionFailure(String message) {
        System.out.println(message);
        results.add(message);
    }

    /**
     * This notifies the ResultCollector about a testcase that ended
     * with a fatal error
     *
     * @param message   the message containing the details
     */
    public void notifyError(String message) {
        results.add(message);
    }

    /** Resets the result list. */
    public void reset() {
        results.clear();
    }

    /** @return the list of results */
    public List getResults() {
        return Collections.unmodifiableList(results);
    }
}
