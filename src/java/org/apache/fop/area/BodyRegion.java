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

/* $Id: BodyRegion.java,v 1.4 2004/02/27 17:41:26 jeremias Exp $ */

package org.apache.fop.area;

import org.apache.fop.fo.Constants;

/**
 * The body region area.
 * This area contains a main reference area and optionally a
 * before float and footnote area.
 */
public class BodyRegion extends RegionReference {
    private BeforeFloat beforeFloat;
    private MainReference mainReference;
    private Footnote footnote;
    private int columnGap;
    private int columnCount;

    /**
     * Create a new body region area.
     * This sets the region reference area class to BODY.
     */
    public BodyRegion() {
        super(Constants.FO_REGION_BODY);
        addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
    }

    /**
     * Set the number of columns for blocks when not spanning
     *
     * @param colCount the number of columns
     */
    public void setColumnCount(int colCount) {
        this.columnCount = colCount;
    }

    /**
     * Get the number of columns when not spanning
     *
     * @return the number of columns
     */
    public int getColumnCount() {
        return this.columnCount;
    }

    /**
     * Set the column gap between columns
     * The length is in millipoints.
     *
     * @param colGap the column gap in millipoints
     */
    public void setColumnGap(int colGap) {
        this.columnGap = colGap;
    }

    /**
     * Set the before float area.
     *
     * @param bf the before float area
     */
    public void setBeforeFloat(BeforeFloat bf) {
        beforeFloat = bf;
    }

    /**
     * Set the main reference area.
     *
     * @param mr the main reference area
     */
    public void setMainReference(MainReference mr) {
        mainReference = mr;
    }

    /**
     * Set the footnote area.
     *
     * @param foot the footnote area
     */
    public void setFootnote(Footnote foot) {
        footnote = foot;
    }

    /**
     * Get the before float area.
     *
     * @return the before float area
     */
    public BeforeFloat getBeforeFloat() {
        return beforeFloat;
    }

    /**
     * Get the main reference area.
     *
     * @return the main reference area
     */
    public MainReference getMainReference() {
        return mainReference;
    }

    /**
     * indicates whether the main reference area has any child areas added to it
     *
     * @return whether the main reference area has any child areas added to it
     */
    public boolean isEmpty() {
        return mainReference == null || mainReference.isEmpty();
    }


    /**
     * Get the footnote area.
     *
     * @return the footnote area
     */
    public Footnote getFootnote() {
        return footnote;
    }

    /**
     * Clone this object.
     *
     * @return a shallow copy of this object
     */
    public Object clone() {
        BodyRegion br = new BodyRegion();
        br.setCTM(getCTM());
        br.setIPD(getIPD());
        br.columnGap = columnGap;
        br.columnCount = columnCount;
        br.beforeFloat = beforeFloat;
        br.mainReference = mainReference;
        br.footnote = footnote;
        return br;
    }
}
