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

/* $Id$ */

package org.apache.fop.fo;

// FOP
import org.apache.fop.fonts.Font;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.traits.SpaceVal;

/**
 * Collection of properties used in
 */
public class TextInfo {
    /** object containing the font information */
    public Font fs;
    /** fo:color property */
    public ColorType color;
    /** fo:wrap-option property */
    public int wrapOption;
    /** fo:wrap-option property: true if wrapOption = WRAP */
    public boolean bWrap ;
    /** fo:white-space-collapse property*/
    public int whiteSpaceCollapse;
    /** fo:vertical-align property */
    public int verticalAlign;
    /** fo:line-height property */
    public int lineHeight;
    /** fo:text-transform property */
    public int textTransform = Constants.TextTransform.NONE;

    // Props used for calculating inline-progression-dimension
    /** fo:word-spacing property */
    public SpaceVal wordSpacing;
    /** fo:letter-spacing property */
    public SpaceVal letterSpacing;

    /* the hyphenation character to be used */
    public char hyphChar = '-';

    /** fo:text-decoration property: is text underlined? */
    public boolean underlined = false;
    /** fo:text-decoration property: is text overlined? */
    public boolean overlined = false;
    /** fo:text-decoration property: is text overstriked? */
    public boolean lineThrough = false;

}
