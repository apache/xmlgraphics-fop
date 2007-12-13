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

package org.apache.fop.layoutmgr.table;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.table.BorderSpecification;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;

/**
 * Implements the normal "collapse" border model defined in 6.7.10 in XSL 1.0.
 * 
 * TODO Column groups are not yet checked in this algorithm!
 */
public class CollapsingBorderModelEyeCatching extends CollapsingBorderModel {

    /** {@inheritDoc} */
    public BorderSpecification determineWinner(BorderSpecification border1,
            BorderSpecification border2, boolean discard) {
        BorderInfo bi1 = border1.getBorderInfo();
        BorderInfo bi2 = border2.getBorderInfo();
        if (discard) {
            if (bi1.getWidth().isDiscard()) {
                if (bi2.getWidth().isDiscard()) {
                    return new BorderSpecification(
                            CommonBorderPaddingBackground.getDefaultBorderInfo(), 0/*TODO*/);
                } else {
                    return border2;
                }
            } else if (bi2.getWidth().isDiscard()) {
                return border1;
            }
        }
        // Otherwise, fall back to the default resolution algorithm
        return determineWinner(border1, border2);
    }

    /** {@inheritDoc} */
    public BorderSpecification determineWinner(BorderSpecification border1,
            BorderSpecification border2) {
        BorderInfo bi1 = border1.getBorderInfo();
        BorderInfo bi2 = border2.getBorderInfo();
        // Rule 1
        if (bi1.getStyle() == Constants.EN_HIDDEN) {
            return border1;
        } else if (bi2.getStyle() == Constants.EN_HIDDEN) {
            return border2;
        }
        // Rule 2
        if (bi2.getStyle() == Constants.EN_NONE) {
            return border1;
        } else if (bi1.getStyle() == Constants.EN_NONE) {
            return border2;
        }
        // Rule 3
        int width1 = bi1.getRetainedWidth();
        int width2 = bi2.getRetainedWidth();
        if (width1 > width2) {
            return border1;
        } else if (width1 == width2) {
            int cmp = compareStyles(bi1.getStyle(), bi2.getStyle());
            if (cmp > 0) {
                return border1;
            } else if (cmp < 0) {
                return border2;
            }
        } else {
            return border2;
        }
        // Rule 4
        int cmp = compareFOs(border1.getHolder(), border2.getHolder());
        if (cmp > 0) {
            return border1;
        } else if (cmp < 0) {
            return border2;
        }
        return null;
    }
}
