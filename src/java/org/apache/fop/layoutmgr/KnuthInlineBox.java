/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

package org.apache.fop.layoutmgr;

public class KnuthInlineBox extends KnuthBox {
    
    private int lead;
    private int total;
    private int middle;
    private FootnoteBodyLayoutManager footnoteBodyLM = null;

    /**
     * Create a new KnuthBox.
     *
     * @param w    the width of this box
     * @param l    the height of this box above the main baseline
     * @param t    the total height of this box
     * @param m    the height of this box above and below the middle baseline
     * @param pos  the Position stored in this box
     * @param bAux is this box auxiliary?
     */
    public KnuthInlineBox(int w, int l, int t, int m, Position pos, boolean bAux) {
        super(w, pos, bAux);
        lead = l;
        total = t;
        middle = m;
    }

    /**
     * @return the height of this box above the main baseline.
     */
    public int getLead() {
        return lead;
    }

    /**
     * @return the total height of this box.
     */
    public int getTotal() {
        return total;
    }

    /**
     * @return the height of this box above and below the middle baseline.
     */
    public int getMiddle() {
        return middle;
    }

    /**
     * @param fblm the FootnoteBodyLM this box must hold a reference to
     */
    public void setFootnoteBodyLM(FootnoteBodyLayoutManager fblm) {
        footnoteBodyLM = fblm;
    }

    /**
     * @return the FootnoteBodyLM this box holds a reference to
     */
    public FootnoteBodyLayoutManager getFootnoteBodyLM() {
        return footnoteBodyLM;
    }

    /**
     * @return true if this box holds a reference to a FootnoteBodyLM
     */
    public boolean isAnchor() {
        return (footnoteBodyLM != null);
    }
    
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" lead=").append(lead);
        sb.append(" total=").append(total);
        sb.append(" middle=").append(middle);
        return sb.toString();
    }
}