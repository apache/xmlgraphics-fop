/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.area.inline.InlineArea;

import org.xml.sax.Attributes;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class InlineContainer extends FObj {

    public InlineContainer(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List lms) {
        ArrayList childList = new ArrayList();
        super.addLayoutManager(childList);
        lms.add(new ICLayoutManager(this, childList));
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Margin Properties-Inline
        MarginInlineProps mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps =
          propMgr.getRelativePositionProps();

        // this.properties.get("alignment-adjust");
        // this.properties.get("alignment-baseline");
        // this.properties.get("baseline-shift");
        // this.properties.get("block-progression-dimension");
        // this.properties.get("clip");
        // this.properties.get("display-align");
        // this.properties.get("dominant-baseline");
        // this.properties.get("height");
        // this.properties.get("id");
        // this.properties.get("inline-progression-dimension");
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("overflow");
        // this.properties.get("reference-orientation");
        // this.properties.get("width");
        // this.properties.get("writing-mode");
    }

    /**
     * This creates a single inline container area after
     * laying out the child block areas. All footnotes, floats
     * and id areas are maintained for later retrieval.
     */
    class ICLayoutManager extends LeafNodeLayoutManager {
        List childrenLM;

        ICLayoutManager(FObj obj, List childLM) {
            super(obj);
            childrenLM = childLM;
        }

        public InlineArea get(int index) {
            return null;
        }
    }
}
