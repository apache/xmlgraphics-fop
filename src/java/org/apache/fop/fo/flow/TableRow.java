/*
 * $Id: TableRow.java,v 1.61 2003/03/05 20:38:20 jeremias Exp $
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
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.KeepValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.Constants;

import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonRelativePosition;

/**
 * Class modelling the fo:table-row object. See Sec. 6.7.9 of the XSL-FO
 * Standard.
 */
public class TableRow extends FObj {

    private boolean setup = false;

    private int breakAfter;
    private ColorType backgroundColor;

    private KeepValue keepWithNext;
    private KeepValue keepWithPrevious;
    private KeepValue keepTogether;

    private int minHeight = 0;    // force row height

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableRow(FONode parent) {
        super(parent);
    }

    /**
     * @return keepWithPrevious
     */
    public KeepValue getKeepWithPrevious() {
        return keepWithPrevious;
    }

    private void doSetup() {

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // this.propertyList.get("block-progression-dimension");

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        // only background apply, border apply if border-collapse
        // is collapse.
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps = propMgr.getRelativePositionProps();

        // this.propertyList.get("break-before");
        // this.propertyList.get("break-after");
        setupID();
        // this.propertyList.get("height");
        // this.propertyList.get("keep-together");
        // this.propertyList.get("keep-with-next");
        // this.propertyList.get("keep-with-previous");


        this.breakAfter = this.propertyList.get("break-after").getEnum();
        this.backgroundColor =
            this.propertyList.get("background-color").getColorType();

        this.keepTogether = getKeepValue("keep-together.within-column");
        this.keepWithNext = getKeepValue("keep-with-next.within-column");
        this.keepWithPrevious =
            getKeepValue("keep-with-previous.within-column");

        this.minHeight = this.propertyList.get("height").getLength().getValue();
        setup = true;
    }

    private KeepValue getKeepValue(String sPropName) {
        Property p = this.propertyList.get(sPropName);
        Number n = p.getNumber();
        if (n != null) {
            return new KeepValue(KeepValue.KEEP_WITH_VALUE, n.intValue());
        }
        switch (p.getEnum()) {
        case Constants.ALWAYS:
            return new KeepValue(KeepValue.KEEP_WITH_ALWAYS, 0);
        case Constants.AUTO:
        default:
            return new KeepValue(KeepValue.KEEP_WITH_AUTO, 0);
        }
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveTableRow(this);
    }

    /**
     * @see org.apache.fop.fo.FObj#handleAttrs
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        setupID();

        getFOTreeControl().getFOInputHandler().startRow(this);
    }

    protected void end() {
        getFOTreeControl().getFOInputHandler().endRow(this);
    }

}
