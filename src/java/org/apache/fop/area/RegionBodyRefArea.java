/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.apache.fop.area;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * The body region area.
 * This area contains a main reference area and optionally a
 * before float and footnote area.
 */
public class RegionBodyRefArea
extends RegionRefArea
implements ReferenceArea {
    //private BeforeFloat beforeFloat;
    private MainReferenceArea mainReference;
    //private Footnote footnote;
    private int columnGap = 0;
    private int columnCount = 1;

    /**
     * Create a new body region area.
     * This sets the region reference area class to BODY.
     * @param pageSeq the generating <code>page-sequence</code>
     * @param generatedBy the generating <code>FONode</code>
     * @param parent
     * @param sync
     */
    public RegionBodyRefArea(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
    }

    /**
     * Create a new body region area.
     * This sets the region reference area class to BODY.
     */
    public RegionBodyRefArea(
            int columnCount,
            int columnGap,
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
        this.columnCount = columnCount;
        this.columnGap = columnGap;
    }

    public static RegionBodyRefArea nullRegionBodyRef(
            FoPageSequence pageSeq, FONode generatedBy,
            Node parent, Object sync) {
        RegionBodyRefArea bodyRef =
            new RegionBodyRefArea(pageSeq, generatedBy, parent, sync);
        bodyRef.setMainReference(MainReferenceArea.nullMainRefArea(
                pageSeq, generatedBy, bodyRef, sync));
        return bodyRef;
    }
    /**
     * Set the number of columns for blocks when not spanning
     *
     * @param colCount the number of columns
     */
    public void setColumnCount(int colCount) {
        synchronized (sync) {
            this.columnCount = colCount;
        }
    }

    /**
     * Get the number of columns when not spanning
     *
     * @return the number of columns
     */
    public int getColumnCount() {
        synchronized (sync) {
            return this.columnCount;
        }
    }

    /**
     * Set the column gap between columns
     * The length is in millipoints.
     *
     * @param colGap the column gap in millipoints
     */
    public void setColumnGap(int colGap) {
        synchronized (sync) {
            this.columnGap = colGap;
        }
    }

    /**
     * @return the columnGap
     */
    public int getColumnGap() {
        synchronized (sync) {
            return columnGap;
        }
    }
    /**
     * Set the before float area.
     *
     * @param bf the before float area
     */
//    public void setBeforeFloat(BeforeFloat bf) {
//        beforeFloat = bf;
//    }

    /**
     * Set the main reference area.
     *
     * @param mr the main reference area
     */
    public void setMainReference(MainReferenceArea mr) {
        synchronized (sync) {
            mainReference = mr;
        }
    }

    /**
     * Set the footnote area.
     *
     * @param foot the footnote area
     */
//    public void setFootnote(Footnote foot) {
//        footnote = foot;
//    }

    /**
     * Get the before float area.
     *
     * @return the before float area
     */
//    public BeforeFloat getBeforeFloat() {
//        return beforeFloat;
//    }

    /**
     * Get the main reference area.
     *
     * @return the main reference area
     */
    public MainReferenceArea getMainReference() {
        synchronized (sync) {
            return mainReference;
        }
    }

    /**
     * Get the footnote area.
     *
     * @return the footnote area
     */
//    public Footnote getFootnote() {
//        return footnote;
//    }

    /**
     * Clone this object.
     *
     * @return a shallow copy of this object
     */
    public Object clone() {
        synchronized (sync) {
            RegionBodyRefArea br = (RegionBodyRefArea)(super.clone());
            br.columnGap = columnGap;
            br.columnCount = columnCount;
            //br.beforeFloat = beforeFloat;
            br.mainReference = mainReference;
            //br.footnote = footnote;
            return br;
        }
    }
}
