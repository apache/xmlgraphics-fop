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

package org.apache.fop.fo.flow;

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.KeepValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.Constants;

import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.Property;


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
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        setupID();
        getFOTreeControl().getFOInputHandler().startRow(this);
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


        this.breakAfter = this.propertyList.get(PR_BREAK_AFTER).getEnum();
        this.backgroundColor =
            this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();

        this.keepTogether = getKeepValue(PR_KEEP_TOGETHER | CP_WITHIN_COLUMN);
        this.keepWithNext = getKeepValue(PR_KEEP_WITH_NEXT | CP_WITHIN_COLUMN);
        this.keepWithPrevious =
            getKeepValue(PR_KEEP_WITH_PREVIOUS | CP_WITHIN_COLUMN);

        this.minHeight = this.propertyList.get(PR_HEIGHT).getLength().getValue();
        setup = true;
    }

    private KeepValue getKeepValue(int propId) {
        Property p = this.propertyList.get(propId);
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

    protected void end() {
        getFOTreeControl().getFOInputHandler().endRow(this);
    }

}
