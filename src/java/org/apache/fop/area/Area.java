/*
 * $Id: Area.java,v 1.16 2003/03/05 15:19:31 jeremias Exp $
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
package org.apache.fop.area;

import java.io.Serializable;

import java.util.Map;
import java.util.HashMap;

// If the area appears more than once in the output
// or if the area has external data it is cached
// to keep track of it and to minimize rendered output
// renderers can render the output once and display it
// for every occurence
// this should also extend to all outputs (including PDFGraphics2D)
// and all types of renderers

/**
 * Base object for all areas.
 */
public class Area implements Serializable {
    // stacking directions
    /**
     * Stacking left to right
     */
    public static final int LR = 0;

    /**
     * Stacking right to left
     */
    public static final int RL = 1;

    /**
     * Stacking top to bottom
     */
    public static final int TB = 2;

    /**
     * Stacking bottom to top
     */
    public static final int BT = 3;

    // orientations for reference areas
    /**
     * Normal orientation
     */
    public static final int ORIENT_0 = 0;

    /**
     * Rotated 90 degrees clockwise
     */
    public static final int ORIENT_90 = 1;

    /**
     * Rotate 180 degrees
     */
    public static final int ORIENT_180 = 2;

    /**
     * Rotated 270 degrees clockwise
     */
    public static final int ORIENT_270 = 3;

    // area class values

    /**
     * Normal class
     */
    public static final int CLASS_NORMAL = 0;

    /**
     * Fixed position class
     */
    public static final int CLASS_FIXED = 1;

    /**
     * Absolute position class
     */
    public static final int CLASS_ABSOLUTE = 2;

    /**
     * Before float class
     */
    public static final int CLASS_BEFORE_FLOAT = 3;

    /**
     * Footnote class
     */
    public static final int CLASS_FOOTNOTE = 4;

    /**
     * Side float class
     */
    public static final int CLASS_SIDE_FLOAT = 5;

    // IMPORTANT: make sure this is the maximum + 1
    /**
     * Maximum class count
     */
    public static final int CLASS_MAX = CLASS_SIDE_FLOAT + 1;

    private int areaClass = CLASS_NORMAL;
    private int ipd;

    /**
     * Traits for this area stored in a HashMap
     */
    protected HashMap props = null;

    /**
     * Get the area class of this area.
     *
     * @return the area class
     */
    public int getAreaClass() {
        return areaClass;
    }

    /**
     * Set the area class of this area.
     *
     * @param areaClass the area class
     */
    public void setAreaClass(int areaClass) {
        this.areaClass = areaClass;
    }

    /**
     * Set the inline progression dimension of this area.
     *
     * @param i the new inline progression dimension
     */
    public void setIPD(int i) {
        ipd = i;
    }

    /**
     * Get the inline progression dimension of this area.
     *
     * @return the inline progression dimension
     */
    public int getIPD() {
        return ipd;
    }

    /**
     * Add a child to this area.
     * The default is to do nothing. Subclasses must override
     * to do something if they can have child areas.
     *
     * @param child the child area to add
     */
    public void addChild(Area child) {
    }

    /**
     * Add a trait property to this area.
     *
     * @param prop the Trait to add
     */
    public void addTrait(Trait prop) {
        if (props == null) {
            props = new java.util.HashMap(20);
        }
        props.put(prop.getPropType(), prop.getData());
    }

    /**
     * Add a trait to this area.
     *
     * @param traitCode the trait key
     * @param prop the value of the trait
     */
    public void addTrait(Object traitCode, Object prop) {
        if (props == null) {
            props = new java.util.HashMap(20);
        }
        props.put(traitCode, prop);
    }

    /**
     * Get the map of all traits on this area.
     *
     * @return the map of traits
     */
    public Map getTraits() {
        return this.props;
    }

    /**
     * Get a trait from this area.
     *
     * @param oTraitCode the trait key
     * @return the trait value
     */
    public Object getTrait(Object oTraitCode) {
        return (props != null ? props.get(oTraitCode) : null);
    }
    
    /**
     * Get a trait from this area as an integer.
     *
     * @param oTraitCode the trait key
     * @return the trait value
     */
    public int getTraitAsInteger(Object oTraitCode) {
        final Object obj = getTrait(oTraitCode);
        if (obj instanceof Integer) {
            return ((Integer)obj).intValue();
        } else {
            throw new IllegalArgumentException("Trait " 
                    + oTraitCode.getClass().getName() 
                    + " could not be converted to an integer");
        }
    }
}

