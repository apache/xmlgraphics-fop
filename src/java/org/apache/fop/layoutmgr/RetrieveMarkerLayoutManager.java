/*
 * $Id: RetrieveMarkerLayoutManager.java,v 1.4 2003/03/07 07:58:51 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.area.Area;
import org.apache.fop.fo.flow.Marker;

/**
 * LayoutManager for a block FO.
 */
public class RetrieveMarkerLayoutManager extends AbstractLayoutManager {
    private LayoutProcessor replaceLM = null;
    private boolean loaded = false;
    private String name;
    private int position;
    private int boundary;
    private AddLMVisitor addLMVisitor = new AddLMVisitor();

    /**
     * Create a new block container layout manager.
     */
    public RetrieveMarkerLayoutManager(String n, int pos, int bound) {
        name = n;
        position = pos;
        boundary = bound;
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

    public void addAreas(PositionIterator parentIter,
                         LayoutContext layoutContext) {

        loadLM();
        addID();
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

    protected void loadLM() {
        if (loaded) {
            return;
        }
        loaded = true;
        if (replaceLM == null) {
            List list = new ArrayList();
            Marker marker = retrieveMarker(name, position, boundary);
            if (marker != null) {
                addLMVisitor.addLayoutManager(marker, list);
                if (list.size() > 0) {
                    replaceLM =  (LayoutProcessor)list.get(0);
                    replaceLM.setParent(this);
                    replaceLM.init();
                    getLogger().debug("retrieved: " + replaceLM + ":" + list.size());
                } else {
                    getLogger().debug("found no marker with name: " + name);
                }
            }
        }
    }

    /**
     * Get the parent area for children of this block container.
     * This returns the current block container area
     * and creates it if required.
     *
     * @see org.apache.fop.layoutmgr.LayoutProcessor#getParentArea(Area)
     */
    public Area getParentArea(Area childArea) {
        return parentLM.getParentArea(childArea);
    }

    /**
     * Add the child to the block container.
     *
     * @see org.apache.fop.layoutmgr.LayoutProcessor#addChild(Area)
     */
    public void addChild(Area childArea) {
        parentLM.addChild(childArea);
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutProcessor#resetPosition(Position)
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

