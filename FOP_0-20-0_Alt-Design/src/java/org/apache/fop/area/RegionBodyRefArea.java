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
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * The body region area.
 * This area contains a main reference area and optionally a
 * before float and footnote area.
 * 
 * The region-body-reference-area is the primary context for the layout of
 * the contents of fo:flow blocks on the page. 
 */
public class RegionBodyRefArea
extends RegionRefArea
implements ReferenceArea {
    private MainReferenceArea mainReference;
    private BeforeFloatRefArea beforeFloatRefArea;
    private FootnoteRefArea footnoteRefArea;
    private int columnGap = 0;
    private int columnCount = 1;

    /**
     * Creates a new body region area with no defined rectangular area and the
     * default column count and gap
     * This sets the region reference area class to BODY.
     * @param pageSeq the generating <code>page-sequence</code>
     * @param parent the page-reference-area
     * @param sync
     */
    public RegionBodyRefArea(
            FoPageSequence pageSeq,
            Node parent,
            Object sync) {
        // the page-sequence is the generated-by node
        super(pageSeq, pageSeq, parent, sync);
    }

    /**
     * Creates and returns a <code>RegionBodyRefArea</code> with no rectangular
     * area. The area created references a null <code>MainReferenceArea</code>.
     * <b>N.B.</b> this is a <code>static</code> method.
     * @param pageSeq the <code>page-sequence</code> to which this area belongs
     * @param parent the <code>region-body-viewport-area</code>
     * @param sync
     * @return the created reference area
     */
    public static RegionBodyRefArea nullRegionBodyRef(
            FoPageSequence pageSeq, Node parent, Object sync) {
        RegionBodyRefArea bodyRef =
            new RegionBodyRefArea(pageSeq, parent, sync);
        bodyRef.setMainReference(MainReferenceArea.nullMainRefArea(
                pageSeq, pageSeq, bodyRef, sync));
        bodyRef.setBeforeFloatRefArea(
                new BeforeFloatRefArea(pageSeq, pageSeq, bodyRef, sync));
        bodyRef.setFootnoteRefArea(
                new FootnoteRefArea(pageSeq, pageSeq, bodyRef, sync));
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
     * @param bf the before float reference area
     */
    public void setBeforeFloatRefArea(BeforeFloatRefArea bf) {
        beforeFloatRefArea = bf;
    }

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
     * @param foot the footnote reference area
     */
    public void setFootnoteRefArea(FootnoteRefArea foot) {
        footnoteRefArea = foot;
    }

    /**
     * Get the before float area.
     *
     * @return the before float area
     */
    public BeforeFloatRefArea getBeforeFloatRefArea() {
        return beforeFloatRefArea;
    }

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
    public FootnoteRefArea getFootnoteRefArea() {
        return footnoteRefArea;
    }

}
