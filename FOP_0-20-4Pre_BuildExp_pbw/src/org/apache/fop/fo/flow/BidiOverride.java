/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;

import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Word;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class BidiOverride extends FObjMixed {

    public BidiOverride(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List list) {
        if (false) {
            super.addLayoutManager(list);
        } else {
            ArrayList childList = new ArrayList();
            super.addLayoutManager(childList);
            for (int count = childList.size() - 1; count >= 0; count--) {
                LayoutManager lm = (LayoutManager) childList.get(count);
                if (lm.generatesInlineAreas()) {
                    list.add( new BidiLayoutManager(this,
                                                    (LeafNodeLayoutManager) lm));
                } else {
                    list.add(lm);
                }
            }
        }
    }

    public void setup() {

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Font Properties
        //this.fontState = propMgr.getFontState(area.getFontInfo());

        // Common Margin Properties-Inline
        RelativePositionProps mProps = propMgr.getRelativePositionProps();

        // this.properties.get("color");
        // this.properties.get("direction");
        // this.properties.get("id");
        // this.properties.get("letter-spacing");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("score-spaces");
        // this.properties.get("text-shadow");
        // this.properties.get("text-transform");
        // this.properties.get("unicode-bidi");
        // this.properties.get("word-spacing");

    }

    /**
     * If this bidi has a different writing mode direction
     * ltr or rtl than its parent writing mode then this
     * reverses the inline areas (at the character level).
     */
    class BidiLayoutManager extends LeafNodeLayoutManager {
        List childs;

        BidiLayoutManager(FObj obj, LeafNodeLayoutManager cLM) {
            super(obj);
            childs = new ArrayList();
            for (int count = cLM.size() - 1; count >= 0; count--) {
                InlineArea ia = cLM.get(count);
                if (ia instanceof Word) {
                    // reverse word
                    Word word = (Word) ia;
                    StringBuffer sb = new StringBuffer(word.getWord());
                    word.setWord(sb.reverse().toString());
                }
                childs.add(ia);
            }
        }

        public int size() {
            return childs.size();
        }

        public InlineArea get(int index) {
            return (InlineArea) childs.get(index);
        }
    }
}
