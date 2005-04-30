/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.fop.layoutmgr;

import java.util.List;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.area.AreaTreeHandler;


/**
 * The interface for all LayoutManager makers
 */
public interface LayoutManagerMaker {
    
    /**
     * Make LayoutManagers for the node and add them to the list lms.
     * @param node the FO node for which the LayoutManagers are made
     * @param lms the list to which the LayoutManagers are added
     */
    public void makeLayoutManagers(FONode node, List lms);

    /**
     * Make a specific LayoutManager for the node.
     * If not exactly one LayoutManagers is available,
     * an IllegalStateException is thrown.
     * @param node the FO node for which the LayoutManagers are made
     * @return The created LayoutManager
     * @throws IllegalStateException if not exactly one
     *    LayoutManager is available for the requested node
     */
    public LayoutManager makeLayoutManager(FONode node);

    /**
     * Make a PageSequenceLayoutManager object.
     * @param ath the AreaTreeHandler object the PSLM interacts with
     * @param ps the fo:page-sequence object this PSLM will process
     * @return The created PageSequenceLayoutManager object
     */
    public PageSequenceLayoutManager makePageSequenceLayoutManager(
        AreaTreeHandler ath, PageSequence ps);

}

