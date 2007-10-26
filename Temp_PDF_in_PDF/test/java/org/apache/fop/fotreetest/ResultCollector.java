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
 * This class collects the results from assertions injected into the FO stream. 
 */
public class ResultCollector {

    private static ResultCollector instance = null;
    
    private List results = new java.util.ArrayList();
    
    /** @return the ResultColletor singleton */
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
     * This notifies the ResultCollector about an Exception.
     * @param e the exception
     */
    public void notifyException(Exception e) {
        System.out.println(e.getMessage());
        results.add(e);
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
