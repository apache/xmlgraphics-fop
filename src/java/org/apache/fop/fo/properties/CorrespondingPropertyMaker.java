/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2004 The Apache Software Foundation. All rights reserved.
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
package org.apache.fop.fo.properties;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.PropertyList;

/**
 */
public class CorrespondingPropertyMaker {
    protected PropertyMaker baseMaker;
    protected int lr_tb;
    protected int rl_tb;
    protected int tb_rl;
    private boolean useParent;
    private boolean relative;
    
    public CorrespondingPropertyMaker(PropertyMaker baseMaker) {
        this.baseMaker = baseMaker;
        baseMaker.setCorresponding(this);
    }
    
    
    public void setCorresponding(int lr_tb, int rl_tb, int tb_rl) {
        this.lr_tb = lr_tb;
        this.rl_tb = rl_tb;
        this.tb_rl = tb_rl;
    }
    
    public void setUseParent(boolean useParent) {
        this.useParent = useParent;
    }

    public void setRelative(boolean relative) {
        this.relative = relative;
    }
    
    /**
     * For properties that operate on a relative direction (before, after,
     * start, end) instead of an absolute direction (top, bottom, left,
     * right), this method determines whether a corresponding property
     * is specified on the corresponding absolute direction. For example,
     * the border-start-color property in a lr-tb writing-mode specifies
     * the same thing that the border-left-color property specifies. In this
     * example, if the Maker for the border-start-color property is testing,
     * and if the border-left-color is specified in the properties,
     * this method should return true.
     * @param propertyList collection of properties to be tested
     * @return true iff 1) the property operates on a relative direction,
     * AND 2) the property has a corresponding property on an absolute
     * direction, AND 3) the corresponding property on that absolute
     * direction has been specified in the input properties
     */
    public boolean isCorrespondingForced(PropertyList propertyList) {
        if (!relative) {
            return false;
        }
        PropertyList pList;
        if (useParent) {
            pList = propertyList.getParentFObj().propertyList;
        } else {
            pList = propertyList;
        }
        int correspondingId = pList.wmMap(lr_tb, rl_tb, tb_rl);
        if (propertyList.getExplicit(correspondingId) != null)
            return true;
        return false;            
    }
    
    /**
     * Return a Property object representing the value of this property,
     * based on other property values for this FO.
     * A special case is properties which inherit the specified value,
     * rather than the computed value.
     * @param propertyList The PropertyList for the FO.
     * @return Property A computed Property value or null if no rules
     * are specified (in foproperties.xml) to compute the value.
     * @throws FOPException for invalid or inconsistent FO input
     */
    public Property compute(PropertyList propertyList) throws FOPException {
        PropertyList pList;
        if (useParent) {
            pList = propertyList.getParentPropertyList();
            if (pList == null) {
                return null;
            }
        } else {
            pList = propertyList;
        }
        int correspondingId = pList.wmMap(lr_tb, rl_tb, tb_rl);
            
        Property p = propertyList.getExplicitOrShorthand(correspondingId);
        if (p != null) {
            FObj parentFO = propertyList.getParentFObj();
            p = baseMaker.convertProperty(p, propertyList, parentFO);
        }
        return p;
    }
}

