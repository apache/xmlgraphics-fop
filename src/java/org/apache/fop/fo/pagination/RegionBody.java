/*
 * $Id: RegionBody.java,v 1.23 2003/03/06 13:42:41 jeremias Exp $
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
package org.apache.fop.fo.pagination;

// Java
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.fo.properties.WritingMode;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.fo.properties.CommonMarginBlock;

/**
 * The fo:region-body element.
 */
public class RegionBody extends Region {

    private ColorType backgroundColor;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RegionBody(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getViewportRectangle(FODimension)
     */
    protected Rectangle getViewportRectangle (FODimension reldims) {
        /*
        * Use space-before and space-after which will use corresponding
        * absolute margin properties if specified. For indents:
        * try to get corresponding absolute margin property using the
        * writing-mode on the page (not on the region-body!). If that's not
        * set but indent is explicitly set, it will return that.
        */
        CommonMarginBlock mProps = propMgr.getMarginProps();
        int start = getRelMargin(PropertyList.START, "start-indent");
        Rectangle vpRect;
        if (this.wm == WritingMode.LR_TB || this.wm == WritingMode.RL_TB) {
            vpRect = new Rectangle(start, mProps.spaceBefore,
                    reldims.ipd - start
                        - getRelMargin(PropertyList.END, "end-indent"),
                    reldims.bpd - mProps.spaceBefore - mProps.spaceAfter);
        } else {
            vpRect = new Rectangle(start, mProps.spaceBefore,
                    reldims.bpd - mProps.spaceBefore - mProps.spaceAfter,
                    reldims.ipd - start
                        - getRelMargin(PropertyList.END, "end-indent"));
        }
        return vpRect;
    }

    /**
     * Get the relative margin using parent's writing mode, not own
     * writing mode.
     */
    private int getRelMargin(int reldir, String sRelPropName) {
        FObj parent = (FObj) getParent();
        String sPropName = "margin-"
                + parent.properties.wmRelToAbs(reldir);
        Property prop = properties.getExplicitBaseProp(sPropName);
        if (prop == null) {
            prop = properties.getExplicitBaseProp(sRelPropName);
        }
        return ((prop != null) ? prop.getLength().getValue() : 0);
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getDefaultRegionName()
     */
    protected String getDefaultRegionName() {
        return "xsl-region-body";
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getRegionClass()
     */
    public String getRegionClass() {
        return Region.BODY;
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getRegionAreaClass()
     */
    public int getRegionAreaClass() {
        return RegionReference.BODY;
    }

    /**
     * Override the inherited method.
     * @see org.apache.fop.fo.pagination.Region#makeRegionReferenceArea(Rectangle2D)
     */
    public RegionReference makeRegionReferenceArea(Rectangle2D absRegVPRect) {
        // Should set some column stuff here I think, or put it elsewhere
        BodyRegion body = new BodyRegion();
        setRegionPosition(body, absRegVPRect);
        int columnCount =
                this.properties.get("column-count").getNumber().intValue();
        if ((columnCount > 1) && (overflow == Overflow.SCROLL)) {
            // recover by setting 'column-count' to 1. This is allowed but
            // not required by the spec.
            getLogger().error("Setting 'column-count' to 1 because "
                    + "'overflow' is set to 'scroll'");
            columnCount = 1;
        }
        body.setColumnCount(columnCount);

        int columnGap =
                this.properties.get("column-gap").getLength().getValue();
        body.setColumnGap(columnGap);
        return body;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveVisitor(this);
    }

}
