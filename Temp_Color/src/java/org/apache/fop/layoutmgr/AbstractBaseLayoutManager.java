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
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FObj;

/**
 * The base class for nearly all LayoutManagers.
 * Provides the functionality for merging the {@link LayoutManager}
 * and the {@link org.apache.fop.datatypes.PercentBaseContext} interfaces
 * into a common base class for all higher LayoutManagers.
 */
public abstract class AbstractBaseLayoutManager
    implements LayoutManager, PercentBaseContext {

    /** Indicator if this LM generates reference areas. */
    protected boolean generatesReferenceArea = false;
    /** Indicator if this LM generates block areas. */
    protected boolean generatesBlockArea = false;
    /** The formatting object for this LM. */
    protected final FObj fobj;

    /**
     * logging instance
     */
    private static final Log LOG = LogFactory.getLog(AbstractBaseLayoutManager.class);

    /**
     * Abstract base layout manager.
     */
    public AbstractBaseLayoutManager() {
        fobj = null;
    }

    /**
     * Abstract base layout manager.
     *
     * @param fo the formatting object for this layout manager
     */
    public AbstractBaseLayoutManager(FObj fo) {
        this.fobj = fo;
        setGeneratesReferenceArea(fo.generatesReferenceAreas());
        if (getGeneratesReferenceArea()) {
            setGeneratesBlockArea(true);
        }
    }

    // --------- Property Resolution related functions --------- //

    /** {@inheritDoc} */
    public int getBaseLength(int lengthBase, FObj fobjx) {
        if (fobjx == this.fobj) {
            switch (lengthBase) {
            case LengthBase.CONTAINING_BLOCK_WIDTH:
                return getAncestorBlockAreaIPD();
            case LengthBase.CONTAINING_BLOCK_HEIGHT:
                return getAncestorBlockAreaBPD();
            case LengthBase.PARENT_AREA_WIDTH:
                return getParentAreaIPD();
            case LengthBase.CONTAINING_REFAREA_WIDTH:
                return getReferenceAreaIPD();
            default:
                LOG.error("Unknown base type for LengthBase:" + lengthBase);
                return 0;
            }
        } else {
            LayoutManager lm = getParent();
            while (lm != null && fobjx != lm.getFObj()) {
                lm = lm.getParent();
            }
            if (lm != null) {
                return lm.getBaseLength(lengthBase, fobjx);
            }
        }
        LOG.error("Cannot find LM to handle given FO for LengthBase. ("
                + fobjx.getContextInfo() + ")");
        return 0;
    }

    /**
     * Find the first ancestor area that is a block area
     * and returns its IPD.
     * @return the ipd of the ancestor block area
     */
    protected int getAncestorBlockAreaIPD() {
        LayoutManager lm = getParent();
        while (lm != null) {
            if (lm.getGeneratesBlockArea() && !lm.getGeneratesLineArea()) {
                return lm.getContentAreaIPD();
            }
            lm = lm.getParent();
        }
        LOG.error("No parent LM found");
        return 0;
    }

    /**
     * Find the first ancestor area that is a block area
     * and returns its BPD.
     * @return the bpd of the ancestor block area
     */
    protected int getAncestorBlockAreaBPD() {
        LayoutManager lm = getParent();
        while (lm != null) {
            if (lm.getGeneratesBlockArea() && !lm.getGeneratesLineArea()) {
                return lm.getContentAreaBPD();
            }
            lm = lm.getParent();
        }
        LOG.error("No parent LM found");
        return 0;
    }

    /**
     * Find the parent area and returns its IPD.
     * @return the ipd of the parent area
     */
    protected int getParentAreaIPD() {
        LayoutManager lm = getParent();
        if (lm != null) {
            return lm.getContentAreaIPD();
        }
        LOG.error("No parent LM found");
        return 0;
    }

    /**
     * Find the parent area and returns its BPD.
     * @return the bpd of the parent area
     */
    protected int getParentAreaBPD() {
        LayoutManager lm = getParent();
        if (lm != null) {
            return lm.getContentAreaBPD();
        }
        LOG.error("No parent LM found");
        return 0;
    }

    /**
     * Find the first ancestor area that is a reference area
     * and returns its IPD.
     * @return the ipd of the ancestor reference area
     */
    public int getReferenceAreaIPD() {
        LayoutManager lm = getParent();
        while (lm != null) {
            if (lm.getGeneratesReferenceArea()) {
                return lm.getContentAreaIPD();
            }
            lm = lm.getParent();
        }
        LOG.error("No parent LM found");
        return 0;
    }

    /**
     * Find the first ancestor area that is a reference area
     * and returns its BPD.
     * @return the bpd of the ancestor reference area
     */
    protected int getReferenceAreaBPD() {
        LayoutManager lm = getParent();
        while (lm != null) {
            if (lm.getGeneratesReferenceArea()) {
                return lm.getContentAreaBPD();
            }
            lm = lm.getParent();
        }
        LOG.error("No parent LM found");
        return 0;
    }

    /**
     * {@inheritDoc}
     * <i>NOTE: Should be overridden by subclasses.
     * Default implementation throws an <code>UnsupportedOperationException</code>.</i>
     */
    public int getContentAreaIPD() {
        throw new UnsupportedOperationException(
                "getContentAreaIPD() called when it should have been overridden");
    }

    /**
     * {@inheritDoc}
     * <i>NOTE: Should be overridden by subclasses.
     * Default implementation throws an <code>UnsupportedOperationException</code>.</i>
     */
    public int getContentAreaBPD() {
        throw new UnsupportedOperationException(
                "getContentAreaBPD() called when it should have been overridden");
    }

    /** {@inheritDoc} */
    public boolean getGeneratesReferenceArea() {
        return generatesReferenceArea;
    }

    /**
     * Lets implementing LM set the flag indicating if they
     * generate reference areas.
     * @param generatesReferenceArea if true the areas generates by this LM are
     * reference areas.
     */
    protected void setGeneratesReferenceArea(boolean generatesReferenceArea) {
        this.generatesReferenceArea = generatesReferenceArea;
    }

    /** {@inheritDoc} */
    public boolean getGeneratesBlockArea() {
        return generatesBlockArea;
    }

    /**
     * Lets implementing LM set the flag indicating if they
     * generate block areas.
     * @param generatesBlockArea if true the areas generates by this LM are block areas.
     */
    protected void setGeneratesBlockArea(boolean generatesBlockArea) {
        this.generatesBlockArea = generatesBlockArea;
    }

    /** {@inheritDoc} */
    public boolean getGeneratesLineArea() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public FObj getFObj() {
        return fobj;
    }

    /** {@inheritDoc} */
    public void reset() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    public boolean isRestartable() {
        return false;
    }

    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context, int alignment, Stack lmStack,
            Position positionAtIPDChange, LayoutManager restartAtLM) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
