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
package org.apache.fop.layoutmgr;

import java.util.List;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.SideRegion;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.layoutmgr.inline.ContentLayoutManager;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.Block;

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

    /**
     * Make a ExternalDocumentLayoutManager object for the fox:external-document extension.
     * @param ath the AreaTreeHandler object the external-document interacts with
     * @param ed the fox:external-document object to be processed
     * @return The created ExternalDocumentLayoutManager object
     */
    public ExternalDocumentLayoutManager makeExternalDocumentLayoutManager(
        AreaTreeHandler ath, ExternalDocument ed);

    /**
     * Make a FlowLayoutManager object.
     * @param pslm the parent PageSequenceLayoutManager object
     * @param flow the fo:flow object this FLM will process
     * @return The created FlowLayoutManager object
     */
    public FlowLayoutManager makeFlowLayoutManager(
        PageSequenceLayoutManager pslm, Flow flow);

    /**
     * Make a ContentLayoutManager object.
     * @param pslm the parent PageSequenceLayoutManager object
     * @param title the fo:title object this CLM will process
     * @return The created ContentLayoutManager object
     */
    public ContentLayoutManager makeContentLayoutManager(
        PageSequenceLayoutManager pslm, Title title);

    /**
     * Make a StaticContentLayoutManager object.
     * @param pslm the parent PageSequenceLayoutManager object
     * @param sc the fo:static-content object this SCLM will process
     * @param reg the side region indicating where the static content
     *     needs to be processed.
     * @return The created StaticContentLayoutManager object
     */
    public StaticContentLayoutManager makeStaticContentLayoutManager(
        PageSequenceLayoutManager pslm, StaticContent sc, SideRegion reg);

    /**
     * Make a StaticContentLayoutManager object for a footnote-separator.
     * @param pslm the parent PageSequenceLayoutManager object
     * @param sc the fo:static-content object this SCLM will process
     * @param block the Block area this SCLM must add its areas to
     * @return The created StaticContentLayoutManager object
     */
    public StaticContentLayoutManager makeStaticContentLayoutManager(
        PageSequenceLayoutManager pslm, StaticContent sc, Block block);

}

