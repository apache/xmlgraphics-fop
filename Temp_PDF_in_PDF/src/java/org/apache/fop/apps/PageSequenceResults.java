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

package org.apache.fop.apps;

/**
 * Class for reporting back formatting results to the calling application. This
 * particular class is used to report the results of a single page-sequence.
 */
public class PageSequenceResults {

    private String id;
    private int pageCount;

    /**
     * Constructor for the PageSequenceResults object
     *
     * @param id         ID of the page-sequence, if available
     * @param pageCount  The number of resulting pages
     */
    public PageSequenceResults(String id, int pageCount) {
        this.id = id;
        this.pageCount = pageCount;
    }

    /**
     * Gets the ID of the page-sequence if one was specified.
     *
     * @return   The ID
     */
    public String getID() {
        return this.id;
    }

    /**
     * Gets the number of pages that resulted by processing the page-sequence.
     *
     * @return   The number of pages generated
     */
    public int getPageCount() {
        return this.pageCount;
    }
}
