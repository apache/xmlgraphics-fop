/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import org.xml.sax.SAXParseException;

import org.apache.fop.area.Area;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.RetrieveMarker;

/**
 * LayoutManager for a block FO.
 */
public class RetrieveMarkerLayoutManager extends AbstractLayoutManager {
    private RetrieveMarker fobj;
    
    private LayoutManager replaceLM = null;
    private boolean loaded = false;
    private String name;
    private int position;
    private int boundary;
    private List markerLMList = new ArrayList(10);

    /**
     * Create a new block container layout manager.
     */
    public RetrieveMarkerLayoutManager(RetrieveMarker node) {
        super(node);
        fobj = node;
        name = node.getRetrieveClassName();
        position = node.getRetrievePosition();
        boundary = node.getRetrieveBoundary();
    }

    public boolean generatesInlineAreas() {
        loadLM();
        if (replaceLM == null) {
            return true;
        }
        return replaceLM.generatesInlineAreas();
    }

    public BreakPoss getNextBreakPoss(LayoutContext context) {
        loadLM();
        if (replaceLM == null) {
            return null;
        }
        return replaceLM.getNextBreakPoss(context);
    }

    public LinkedList getNextKnuthElements(LayoutContext context,
            int alignment) {
        loadLM();
        if (replaceLM == null) {
            return null;
        }
        return replaceLM.getNextKnuthElements(context, alignment);
    }

    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {

        loadLM();
        replaceLM.addAreas(parentIter, layoutContext);

    }

    public boolean isFinished() {
        loadLM();
        if (replaceLM == null) {
            return true;
        }
        return replaceLM.isFinished();
    }

    public void setFinished(boolean fin) {
        if (replaceLM != null) {
            replaceLM.setFinished(fin);
        }
    }

    /** @todo unsure how markerLMList will get tied to main currentLMList
        of AreaTreeHandler */
    protected void loadLM() {
        if (loaded) {
            return;
        }
        loaded = true;
        if (replaceLM == null) {
            List list = new ArrayList();
            Marker marker = retrieveMarker(name, position, boundary);
            if (marker != null) {
                try {
                    marker.rebind(fobj.getPropertyList());
                } catch (SAXParseException exc) {
                    log.error("fo:retrieve-marker unable to rebind property values", exc);
                }
                marker.addLayoutManager(list);
                if (list.size() > 0) {
                    replaceLM =  (LayoutManager)list.get(0);
                    replaceLM.setParent(this);
                    replaceLM.initialize();
                    log.debug("retrieved: " + replaceLM + ":" + list.size());
                }
            } else {
                log.debug("found no marker with name: " + name);
            }
        }
    }

    /**
     * Get the parent area for children of this block container.
     * This returns the current block container area
     * and creates it if required.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager#getParentArea(Area)
     */
    public Area getParentArea(Area childArea) {
        return parentLM.getParentArea(childArea);
    }

    /**
     * Add the child to the block container.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager#addChild(Area)
     */
    public void addChild(Area childArea) {
        parentLM.addChild(childArea);
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#resetPosition(Position)
     */
    public void resetPosition(Position resetPos) {
        loadLM();
        if (resetPos == null) {
            reset(null);
        }
        if (replaceLM != null) {
            replaceLM.resetPosition(null);
        }
        loaded = false;
        replaceLM = null;
    }

}

