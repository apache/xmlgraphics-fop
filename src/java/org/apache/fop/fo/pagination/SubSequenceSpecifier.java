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

import org.apache.fop.fo.ValidationException;

/**
 * Classes that implement this interface can be added to a {@link PageSequenceMaster},
 * and are capable of looking up an appropriate {@link SimplePageMaster}.
 */
public interface SubSequenceSpecifier {

    /**
     * Returns the name of the next page master.
     *
     * @param isOddPage True if the next page number is odd
     * @param isFirstPage True if the next page is the first
     * @param isLastPage True if the next page is the last
     * @param isBlankPage True if the next page is blank
     * @return the page master name
     * @throws PageProductionException if there's a problem determining the next page master
     */
    SimplePageMaster getNextPageMaster(boolean isOddPage,
                                 boolean isFirstPage,
                                 boolean isLastPage,
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

    /**
     * called by the parent LayoutMasterSet to resolve object references
     * from simple page master reference names
     * @param layoutMasterSet the layout-master-set
     * @throws ValidationException when a named reference cannot be resolved
     * */
    void resolveReferences(LayoutMasterSet layoutMasterSet) throws ValidationException;

    /**
     *
     * @param flowName name of the main flow
     * @return true iff page sequence is a finite sequence or can process the entire main flow
     */
    boolean canProcess(String flowName);

    /**
     * Test that this is a finite sequence
     * @return true iff this is a finite sequence
     */
    boolean isInfinite();

    /**
     * Test if this can be reused when it is the last sub-sequence specifer,
     * and has been exhausted
     * @return true if and only if it can be reused
     */
    boolean isReusable();

}

