/*
 * $Id: BidiOverride.java,v 1.14 2003/03/06 11:36:30 jeremias Exp $
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
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonRelativePosition;

import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.LayoutProcessor;
import org.apache.fop.area.inline.InlineArea;

import java.util.List;
import java.util.ArrayList;

/**
 * fo:bidi-override element.
 */
public class BidiOverride extends FObjMixed {

    /**
     * @param parent FONode that is the parent of this object
     */
    public BidiOverride(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager
     */
    public void addLayoutManager(List list) {
        if (false) {
            super.addLayoutManager(list);
        } else {
            ArrayList childList = new ArrayList();
            super.addLayoutManager(childList);
            for (int count = childList.size() - 1; count >= 0; count--) {
                LayoutProcessor lm = (LayoutProcessor) childList.get(count);
                if (lm.generatesInlineAreas()) {
                    LayoutProcessor blm = new BidiLayoutManager((LeafNodeLayoutManager) lm);
                    blm.setFObj(this);
                    list.add(blm);
                } else {
                    list.add(lm);
                }
            }
        }
    }

    private void setup() {

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Font Properties
        //this.fontState = propMgr.getFontState(area.getFontInfo());

        // Common Margin Properties-Inline
        CommonRelativePosition mProps = propMgr.getRelativePositionProps();

        // this.properties.get("color");
        // this.properties.get("direction");
        setupID();
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
     * @return true (BidiOverride can contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * If this bidi has a different writing mode direction
     * ltr or rtl than its parent writing mode then this
     * reverses the inline areas (at the character level).
     */
    class BidiLayoutManager extends LeafNodeLayoutManager {

        private List children;

        BidiLayoutManager(LeafNodeLayoutManager cLM) {
            children = new ArrayList();
/*            for (int count = cLM.size() - 1; count >= 0; count--) {
                InlineArea ia = cLM.get(count);
                if (ia instanceof Word) {
                    // reverse word
                    Word word = (Word) ia;
                    StringBuffer sb = new StringBuffer(word.getWord());
                    word.setWord(sb.reverse().toString());
                }
                children.add(ia);
            }
*/        }

        public int size() {
            return children.size();
        }

        public InlineArea get(int index) {
            return (InlineArea) children.get(index);
        }
    }
}
