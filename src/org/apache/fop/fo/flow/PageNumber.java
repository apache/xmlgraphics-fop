/*
 * $Id$
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
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Status;
import org.apache.fop.fo.FOText;
import org.apache.fop.layout.TextState;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginInlineProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.properties.VerticalAlign;
import org.apache.fop.apps.FOPException;

public class PageNumber extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new PageNumber(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new PageNumber.Maker();
    }

    float red;
    float green;
    float blue;
    int wrapOption;
    int whiteSpaceCollapse;
    TextState ts;

    public PageNumber(FObj parent, PropertyList propertyList,
                      String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);
    }

    public String getName() {
        return "fo:page-number";
    }

    public int layout(Area area) throws FOPException {
        if (!(area instanceof BlockArea)) {
            log.warn("page-number outside block area");
            return Status.OK;
        }
        if (this.marker == START) {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Font Properties
            //this.fontState = propMgr.getFontState(area.getFontInfo());

            // Common Margin Properties-Inline
            MarginInlineProps mProps = propMgr.getMarginInlineProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

            // this.properties.get("alignment-adjust");
            // this.properties.get("alignment-baseline");
            // this.properties.get("baseline-shift");
            // this.properties.get("dominant-baseline");
            // this.properties.get("id");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("letter-spacing");
            // this.properties.get("line-height");
            // this.properties.get("line-height-shift-adjustment");
            // this.properties.get("score-spaces");
            // this.properties.get("text-decoration");
            // this.properties.get("text-shadow");
            // this.properties.get("text-transform");
            // this.properties.get("word-spacing");

            ColorType c = this.properties.get("color").getColorType();
            this.red = c.red();
            this.green = c.green();
            this.blue = c.blue();

            this.wrapOption = this.properties.get("wrap-option").getEnum();
            this.whiteSpaceCollapse =
                this.properties.get("white-space-collapse").getEnum();
            ts = new TextState();
            this.marker = 0;

            // initialize id
            String id = this.properties.get("id").getString();
            try {
                area.getIDReferences().initializeID(id, area);
            }
            catch(FOPException e) {
                if (!e.isLocationSet()) {
                    e.setLocation(systemId, line, column);
                }
                throw e;
            }
        }

        String p = area.getPage().getFormattedNumber();
        this.marker = FOText.addText((BlockArea)area,
                                     propMgr.getFontState(area.getFontInfo()),
                                     red, green, blue, wrapOption, null,
                                     whiteSpaceCollapse, p.toCharArray(), 0,
                                     p.length(), ts, VerticalAlign.BASELINE);
        return Status.OK;
    }

}
