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

package org.apache.fop.layoutmgr.inline;

import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.Font;


/**
 * A factory class for making alignment contexts.
 * Currently supports alignment contexts for basic fonts
 * and graphic inlines.
 */
public class ScaledBaselineTableFactory implements Constants {

    /**
     * Creates a new instance of BasicScaledBaselineTable for the given
     * font, baseline and writingmode.
     * @param font the font for which a baseline table is requested
     * @param dominantBaselineIdentifier the dominant baseline given as an integer constant
     * @param writingMode the writing mode given as an integer constant
     * @return a scaled baseline table for the given font
     */
    public static ScaledBaselineTable makeFontScaledBaselineTable(Font font
                                                                  , int dominantBaselineIdentifier
                                                                  , int writingMode) {
        return new BasicScaledBaselineTable(font.getAscender(), font.getDescender()
                                    , font.getXHeight(), dominantBaselineIdentifier, writingMode);
    }

    /**
     * Creates a new instance of BasicScaledBaselineTable for the given
     * font and writingmode. It assumes an alphabetic baseline.
     * @param font the font for which a baseline table is requested
     * @param writingMode the writing mode given as an integer constant
     * @return a scaled baseline table for the given font
     */
    public static ScaledBaselineTable makeFontScaledBaselineTable(Font font, int writingMode) {
        return makeFontScaledBaselineTable(font, EN_ALPHABETIC, writingMode);
    }

    /**
     * Creates a new instance of BasicScaledBaselineTable for the given
     * height, baseline and writingmode. This is used for non font based areas like
     * external graphic or inline foreign object.
     * @param height the height for which a baseline table is requested
     * @param dominantBaselineIdentifier the dominant baseline given as an integer constant
     * @param writingMode the writing mode given as an integer constant
     * @return a scaled baseline table for the given dimensions
     */
    public static ScaledBaselineTable makeGraphicsScaledBaselineTable(int height
                                                                , int dominantBaselineIdentifier
                                                                , int writingMode) {
        return new BasicScaledBaselineTable(height, 0, height
                                            , dominantBaselineIdentifier, writingMode);
    }

}
