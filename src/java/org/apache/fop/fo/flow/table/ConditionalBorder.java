/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.fo.flow.table;

import org.apache.fop.layoutmgr.table.CollapsingBorderModel;

/**
 * A class that holds the three possible values for a border-before/after on a table-cell,
 * in the collapsing model. These three values are (for border-before, similar for
 * border-after):
 * <ul>
 * <li>non-leading: common case, when a cell follows the cell before on a same page;</li>
 * <li>leading: when the table is broken and the cell appears at the top of a page, in
 * which case its border must be resolved with the header (or the top of the table)
 * instead of with the previous cell;</li>
 * <li>rest: when a cell is broken over several pages; same as leading but with
 * conditionality taken into account.</li>
 * </ul>
 */
public class ConditionalBorder {

    public static final int NORMAL = 0;

    public static final int LEADING_TRAILING = 1;

    public static final int REST = 2;

    /** Special case: the cell is at the top or the bottom of the page. */
    BorderSpecification leadingTrailing;

    /** Normal case, no break. */
    BorderSpecification nonLeadingTrailing;

    /** Special case: break inside the cell. */
    BorderSpecification rest;

    /** The model used to resolve borders. */
    private CollapsingBorderModel collapsingBorderModel;

    private ConditionalBorder(BorderSpecification leadingTrailing,
            BorderSpecification nonLeadingTrailing, BorderSpecification rest,
            CollapsingBorderModel collapsingBorderModel) {
        this.leadingTrailing = leadingTrailing;
        this.nonLeadingTrailing = nonLeadingTrailing;
        this.rest = rest;
        this.collapsingBorderModel = collapsingBorderModel;
    }

    /**
     * Creates a new conditional border.
     * 
     * @param borderSpecification the border specification to take as a basis
     * @param collapsingBorderModel the model that will be used to resolved borders
     */
    ConditionalBorder(BorderSpecification borderSpecification,
            CollapsingBorderModel collapsingBorderModel) {
        leadingTrailing = borderSpecification;
        nonLeadingTrailing = leadingTrailing;
        if (borderSpecification.getBorderInfo().getWidth().isDiscard()) {
            rest = BorderSpecification.getDefaultBorder();
        } else {
            rest = leadingTrailing;
        }
        this.collapsingBorderModel = collapsingBorderModel;
    }

    /**
     * Resolves and updates the relevant parts of this border as well as the given one.
     * 
     * @param competitor
     * @param withLeadingTrailing
     * @param withNonLeadingTrailing
     * @param withRest
     */
    void resolve(ConditionalBorder competitor, boolean withLeadingTrailing,
            boolean withNonLeadingTrailing, boolean withRest) {
        if (withLeadingTrailing) {
            BorderSpecification resolvedBorder = collapsingBorderModel.determineWinner(
                    leadingTrailing, competitor.leadingTrailing);
            if (resolvedBorder != null) {
                leadingTrailing = resolvedBorder;
                competitor.leadingTrailing = resolvedBorder;
            }
        }
        if (withNonLeadingTrailing) {
            BorderSpecification resolvedBorder = collapsingBorderModel.determineWinner(
                    nonLeadingTrailing, competitor.nonLeadingTrailing);
            if (resolvedBorder != null) {
                nonLeadingTrailing = resolvedBorder;
                competitor.nonLeadingTrailing = resolvedBorder;
            }
        }
        if (withRest) {
            BorderSpecification resolvedBorder = collapsingBorderModel.determineWinner(rest,
                    competitor.rest);
            if (resolvedBorder != null) {
                rest = resolvedBorder;
                competitor.rest = resolvedBorder;
            }
        }
    }

    /**
     * Integrates the given segment in this border. Unlike for
     * {@link #integrateSegment(ConditionalBorder, boolean, boolean, boolean)}, this
     * method nicely handles the case where the CollapsingBorderModel returns null, by
     * keeping the components to their old values.
     * 
     * @param competitor
     * @param withLeadingTrailing
     * @param withNonLeadingTrailing
     * @param withRest
     */
    void integrateCompetingSegment(ConditionalBorder competitor, boolean withLeadingTrailing,
            boolean withNonLeadingTrailing, boolean withRest) {
        if (withLeadingTrailing) {
            BorderSpecification resolvedBorder = collapsingBorderModel.determineWinner(
                    leadingTrailing, competitor.leadingTrailing);
            if (resolvedBorder != null) {
                leadingTrailing = resolvedBorder;
            }
        }
        if (withNonLeadingTrailing) {
            BorderSpecification resolvedBorder = collapsingBorderModel.determineWinner(
                    nonLeadingTrailing, competitor.nonLeadingTrailing);
            if (resolvedBorder != null) {
                nonLeadingTrailing = resolvedBorder;
            }
        }
        if (withRest) {
            BorderSpecification resolvedBorder = collapsingBorderModel.determineWinner(rest,
                    competitor.rest);
            if (resolvedBorder != null) {
                rest = resolvedBorder;
            }
        }
    }

    /**
     * Updates this border after taking into account the given segment. The
     * CollapsingBorderModel is not expected to return null.
     * 
     * @param segment
     * @param withLeadingTrailing
     * @param withNonLeadingTrailing
     * @param withRest
     */
    void integrateSegment(ConditionalBorder segment, boolean withLeadingTrailing,
            boolean withNonLeadingTrailing, boolean withRest) {
        if (withLeadingTrailing) {
            leadingTrailing = collapsingBorderModel.determineWinner(leadingTrailing,
                    segment.leadingTrailing);
            assert leadingTrailing != null;
        }
        if (withNonLeadingTrailing) {
            nonLeadingTrailing = collapsingBorderModel.determineWinner(nonLeadingTrailing,
                    segment.nonLeadingTrailing);
            assert nonLeadingTrailing != null;
        }
        if (withRest) {
            rest = collapsingBorderModel.determineWinner(rest, segment.rest);
            assert rest != null;
        }
    }

    /**
     * Returns a shallow copy of this border.
     * 
     * @return a copy of this border
     */
    ConditionalBorder copy() {
        return new ConditionalBorder(leadingTrailing, nonLeadingTrailing, rest,
                collapsingBorderModel);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "{non-leading: " + nonLeadingTrailing + ", leading: " + leadingTrailing + ", rest: "
                + rest + "}";
    }

    /**
     * Returns a default border specification.
     * 
     * @param collapsingBorderModel the model that will be used to resolve borders
     * @return a border with style 'none' for all of the three components
     */
    static ConditionalBorder getDefaultBorder(CollapsingBorderModel collapsingBorderModel) {
        BorderSpecification defaultBorderSpec = BorderSpecification.getDefaultBorder();
        return new ConditionalBorder(defaultBorderSpec, defaultBorderSpec, defaultBorderSpec,
                collapsingBorderModel);
    }
}
