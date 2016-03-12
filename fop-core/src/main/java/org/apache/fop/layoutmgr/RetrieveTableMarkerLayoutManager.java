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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager;
import org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.table.TableLayoutManager;

public class RetrieveTableMarkerLayoutManager extends LeafNodeLayoutManager {

    private static Log log = LogFactory.getLog(RetrieveTableMarkerLayoutManager.class);

    public RetrieveTableMarkerLayoutManager(RetrieveTableMarker node) {
        super(node);
    }

    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        setFinished(true);
        FONode foNode = (FONode) getFObj();
        foNode = getTableLayoutManager().resolveRetrieveTableMarker((RetrieveTableMarker) foNode);
        if (foNode != null) {
            // resolve the RTM and replace current LM by the resolved target LM
            InlineLevelLayoutManager illm = (InlineLevelLayoutManager) getPSLM().getLayoutManagerMaker()
                    .makeLayoutManager(foNode);
            if (illm instanceof RetrieveTableMarkerLayoutManager) {
                // happens if the retrieve-marker was empty
                return null;
            }
            illm.setParent(getParent());
            illm.initialize();
            return illm.getNextKnuthElements(context, alignment);
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
    }

    private TableLayoutManager getTableLayoutManager() {
        LayoutManager parentLM = getParent();
        while (!(parentLM instanceof TableLayoutManager)) {
            parentLM = parentLM.getParent();
        }
        TableLayoutManager tlm = (TableLayoutManager) parentLM;
        return tlm;
    }

}
