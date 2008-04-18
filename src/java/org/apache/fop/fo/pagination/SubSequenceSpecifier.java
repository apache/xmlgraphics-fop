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
 
package org.apache.fop.fo.pagination;


/**
 * Classes that implement this interface can be added to a PageSequenceMaster,
 * and are capable of looking up an appropriate PageMaster.
 */
public interface SubSequenceSpecifier {
    
    /**
     * Returns the name of the next page master.
     * @param isOddPage True if the next page number is odd
     * @param isFirstPage True if the next page is the first
     * @param isLastPage True if the next page is the last
     * @param isOnlyPage True if the next page is the only page
     * @param isBlankPage True if the next page is blank
     * @return the page master name
     * @throws PageProductionException if there's a problem determining the next page master
     */
    String getNextPageMasterName(boolean isOddPage,
                                 boolean isFirstPage,
                                 boolean isLastPage,
                                 boolean isOnlyPage,
                                 boolean isBlankPage)
                                    throws PageProductionException;

    /**
     * Called before a new page sequence is rendered so subsequences can reset
     * any state they keep during the formatting process.
     */
    void reset();

    /**
     * Used to set the "cursor position" to the previous item.
     * @return true if there is a previous item, false if the current one was the first one.
     */
    boolean goToPrevious();

    /** @return true if the subsequence has a page master for page-position "last" */
    boolean hasPagePositionLast();
    
    /** @return true if the subsequence has a page master for page-position "only" */
    boolean hasPagePositionOnly();
    
}

