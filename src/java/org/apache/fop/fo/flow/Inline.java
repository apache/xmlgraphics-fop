/*
 * $Id: Inline.java,v 1.16 2003/03/06 11:36:30 jeremias Exp $
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

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.InlineCharIterator;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.apps.FOPException;

/**
 * Class modelling the fo:inline object. See Sec. 6.6.7 of the XSL-FO Standard.
 */
public class Inline extends FObjMixed {

    // Textdecoration
    /** is this text underlined? */
    protected boolean underlined = false;
    /** is this text overlined? */
    protected boolean overlined = false;
    /** is this text lined through? */
    protected boolean lineThrough = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Inline(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#handleAttrs
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        if (parent.getName().equals("fo:flow")) {
            throw new FOPException("inline formatting objects cannot"
                                   + " be directly under flow");
        }

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Font Properties
        //this.fontState = propMgr.getFontState(area.getFontInfo());

        // Common Margin Properties-Inline
        CommonMarginInline mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps = propMgr.getRelativePositionProps();

        // this.propertyList.get("alignment-adjust");
        // this.propertyList.get("alignment-baseline");
        // this.propertyList.get("baseline-shift");
        // this.propertyList.get("color");
        // this.propertyList.get("dominant-baseline");
        setupID();
        // this.propertyList.get("keep-together");
        // this.propertyList.get("keep-with-next");
        // this.propertyList.get("keep-with-previous");
        // this.propertyList.get("line-height");
        // this.propertyList.get("line-height-shift-adjustment");
        // this.propertyList.get("text-devoration");
        // this.propertyList.get("visibility");
        // this.propertyList.get("z-index");

        int textDecoration = this.propertyList.get(PR_TEXT_DECORATION).getEnum();

        if (textDecoration == TextDecoration.UNDERLINE) {
            this.underlined = true;
        }

        if (textDecoration == TextDecoration.OVERLINE) {
            this.overlined = true;
        }

        if (textDecoration == TextDecoration.LINE_THROUGH) {
            this.lineThrough = true;
        }
        
        getFOTreeControl().getFOInputHandler().startInline(this);
    }

    /**
     * @return true (Inline can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * @see org.apache.fop.fo.FObjMixed#charIterator
     */
    public CharIterator charIterator() {
        return new InlineCharIterator(this, propMgr.getBorderAndPadding());
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveInline(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#end
     */
    public void end() {
        getFOTreeControl().getFOInputHandler().endInline(this);
    }

}
