/*
 * -- $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;
import java.util.Iterator;

public class Table extends FObj {

    private static final int MINCOLWIDTH = 10000; // 10pt
    int breakBefore;
    int breakAfter;
    int spaceBefore;
    int spaceAfter;
    ColorType backgroundColor;
    LengthRange ipd;
    int height;
    TableHeader tableHeader = null;
    TableFooter tableFooter = null;
    boolean omitHeaderAtBreak = false;
    boolean omitFooterAtBreak = false;

    ArrayList columns = new ArrayList();
    int bodyCount = 0;
    private boolean bAutoLayout=false;
    private int contentWidth = 0; // Sum of column widths
    /** Optimum inline-progression-dimension */
    private int optIPD;
    /** Minimum inline-progression-dimension */
    private int minIPD;
    /** Maximum inline-progression-dimension */
    private int maxIPD;

    public Table(FONode parent) {
        super(parent);
    }

    public void setup() {
            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Margin Properties-Block
            MarginProps mProps = propMgr.getMarginProps();

            // Common Relative Position Properties 
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();
        
            // this.properties.get("block-progression-dimension");
            // this.properties.get("border-after-precendence");              
            // this.properties.get("border-before-precedence");
            // this.properties.get("border-collapse");
            // this.properties.get("border-end-precendence");
            // this.properties.get("border-separation");
            // this.properties.get("border-start-precendence");
            // this.properties.get("break-after");
            // this.properties.get("break-before");
            setupID();
            // this.properties.get("inline-progression-dimension");
            // this.properties.get("height");
            // this.properties.get("keep-together");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("table-layout");              
            // this.properties.get("table-omit-footer-at-break");              
            // this.properties.get("table-omit-header-at-break");
            // this.properties.get("width");              
            // this.properties.get("writing-mode");              

            this.breakBefore = this.properties.get("break-before").getEnum();
            this.breakAfter = this.properties.get("break-after").getEnum();
            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();
            this.backgroundColor =
                this.properties.get("background-color").getColorType();
            this.ipd =
		this.properties.get("inline-progression-dimension").
		getLengthRange();
            this.height = this.properties.get("height").getLength().mvalue();
            this.bAutoLayout = (this.properties.get("table-layout").getEnum() == 
		TableLayout.AUTO);

            this.omitHeaderAtBreak =
                this.properties.get("table-omit-header-at-break").getEnum()
                == TableOmitHeaderAtBreak.TRUE;
            this.omitFooterAtBreak =
                this.properties.get("table-omit-footer-at-break").getEnum()
                == TableOmitFooterAtBreak.TRUE;

    }

    public boolean generatesInlineAreas() {
        return false;
    }

}
