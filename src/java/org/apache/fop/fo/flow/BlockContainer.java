/*
 * $Id: BlockContainer.java,v 1.22 2003/03/06 11:36:31 jeremias Exp $
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
package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginBlock;

import org.xml.sax.Attributes;

/**
 * Class modelling the fo:block-container object. See Sec. 6.5.3 of the XSL-FO
 * Standard.
 */
public class BlockContainer extends FObj {

    private ColorType backgroundColor;
    private int position;

    private int top;
    private int bottom;
    private int left;
    private int right;
    private int width;
    private int height;

    private int span;

    /**
     * @param parent FONode that is the parent of this object
     */
    public BlockContainer(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#handleAttrs
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        this.span = this.propertyList.get(PR_SPAN).getEnum();
        setupID();
    }

    private void setup() {

            // Common Accessibility Properties
            CommonAbsolutePosition mAbsProps = propMgr.getAbsolutePositionProps();

            // Common Border, Padding, and Background Properties
            CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
            CommonBackground bProps = propMgr.getBackgroundProps();

            // Common Margin-Block Properties
            CommonMarginBlock mProps = propMgr.getMarginProps();

            // this.propertyList.get("block-progression-dimension");
            // this.propertyList.get("break-after");
            // this.propertyList.get("break-before");
            // this.propertyList.get("clip");
            // this.propertyList.get("display-align");
            // this.propertyList.get("height");
            setupID();
            // this.propertyList.get("keep-together");
            // this.propertyList.get("keep-with-next");
            // this.propertyList.get("keep-with-previous");
            // this.propertyList.get("overflow");
            // this.propertyList.get("reference-orientation");
            // this.propertyList.get("span");
            // this.propertyList.get("width");
            // this.propertyList.get("writing-mode");

            this.backgroundColor =
                this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();

            this.width = this.propertyList.get(PR_WIDTH).getLength().getValue();
            this.height = this.propertyList.get(PR_HEIGHT).getLength().getValue();
            span = this.propertyList.get(PR_SPAN).getEnum();

    }

    /**
     * @return true (BlockContainer can generate Reference Areas)
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * @return false (BlockContainer cannot generate inline areas)
     */
    public boolean generatesInlineAreas() {
        return false;
    }

    /**
     * @return true (BlockContainer can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * @return the span for this object
     */
    public int getSpan() {
        return this.span;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveBlockContainer(this);
    }

}

