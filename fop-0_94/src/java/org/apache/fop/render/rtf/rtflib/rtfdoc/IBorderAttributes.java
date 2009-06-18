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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */



/** Constants for RTF border attribute names. */

public interface IBorderAttributes {

    /** Constant for border left */
    String BORDER_LEFT = "brdrl";
    /** Constant for border right */
    String BORDER_RIGHT = "brdrr";
    /** Constant for border top */
    String BORDER_TOP = "brdrt";
    /** Constant for border bottom */
    String BORDER_BOTTOM = "brdrb";
    /** Constant for character border (border always appears on all sides) */
    String BORDER_CHARACTER = "chbrdr";
    /** Constant for a single-thick border */
    String BORDER_SINGLE_THICKNESS = "brdrs";
    /** Constant for a double-thick border */
    String BORDER_DOUBLE_THICKNESS = "brdrth";
    /** Constant for a shadowed border */
    String BORDER_SHADOWED = "brdrsh";
    /** Constant for a double border */
    String BORDER_DOUBLE = "brdrdb";
    /** Constant for a dotted border */
    String BORDER_DOTTED = "brdrdot";
    /** Constant for a dashed border */
    String BORDER_DASH = "brdrdash";
    /** Constant for a hairline border */
    String BORDER_HAIRLINE = "brdrhair";
    /** Constant for a small-dashed border */
    String BORDER_DASH_SMALL = "brdrdashsm";
    /** Constant for a dot-dashed border */
    String BORDER_DOT_DASH = "brdrdashd";
    /** Constant for a dot-dot-dashed border */
    String BORDER_DOT_DOT_DASH = "brdrdashdd";
    /** Constant for a triple border */
    String BORDER_TRIPLE = "brdrtriple";
    /** Constant for a think-thin-small border */
    String BORDER_THINK_THIN_SMALL = "brdrtnthsg";
    /** Constant for a thin-thick-small border */
    String BORDER_THIN_THICK_SMALL = "brdrthtnsg";
    /** Constant for a thin-thick-thin-small border */
    String BORDER_THIN_THICK_THIN_SMALL = "brdrthtnthsg";
    /** Constant for a think-thin-medium border */
    String BORDER_THINK_THIN_MEDIUM = "brdrtnthmg";
    /** Constant for a thin-thick-medium border */
    String BORDER_THIN_THICK_MEDIUM = "brdrthtnmg";
    /** Constant for a thin-thick-thin-medium border */
    String BORDER_THIN_THICK_THIN_MEDIUM = "brdrthtnthmg";
    /** Constant for a think-thin-large border */
    String BORDER_THINK_THIN_LARGE = "brdrtnthlg";
    /** Constant for a thin-thick-large border */
    String BORDER_THIN_THICK_LARGE = "brdrthtnlg";
    /** Constant for a thin-thick-thin-large border */
    String BORDER_THIN_THICK_THIN_LARGE = "brdrthtnthlg";
    /** Constant for a wavy border */
    String BORDER_WAVY = "brdrwavy";
    /** Constant for a double wavy border */
    String BORDER_WAVY_DOUBLE = "brdrwavydb";
    /** Constant for a striped border */
    String BORDER_STRIPED = "brdrdashdotstr";
    /** Constant for an embossed border */
    String BORDER_EMBOSS = "brdremboss";
    /** Constant for an engraved border */
    String BORDER_ENGRAVE = "brdrengrave";
    /** Constant for an nil border */
    String BORDER_NIL = "brdrnil";
    /** Constant for border color */
    String BORDER_COLOR = "brdrcf";
    /** Constant for border space */
    String BORDER_SPACE = "brsp";
    /** Constant for border width */
    String BORDER_WIDTH = "brdrw";

    /** String array of border attributes */
    String [] BORDERS = new String[] {
        BORDER_SINGLE_THICKNESS,    BORDER_DOUBLE_THICKNESS,            BORDER_SHADOWED,
        BORDER_DOUBLE,              BORDER_DOTTED,                      BORDER_DASH,
        BORDER_HAIRLINE,            BORDER_DASH_SMALL,                  BORDER_DOT_DASH,
        BORDER_DOT_DOT_DASH,        BORDER_TRIPLE,                      BORDER_THINK_THIN_SMALL,
        BORDER_THIN_THICK_SMALL,    BORDER_THIN_THICK_THIN_SMALL,       BORDER_THINK_THIN_MEDIUM,
        BORDER_THIN_THICK_MEDIUM,   BORDER_THIN_THICK_THIN_MEDIUM,      BORDER_THINK_THIN_LARGE,
        BORDER_THIN_THICK_LARGE,    BORDER_THIN_THICK_THIN_LARGE,       BORDER_WAVY,
        BORDER_WAVY_DOUBLE,         BORDER_STRIPED,                     BORDER_EMBOSS,
        BORDER_ENGRAVE,             BORDER_COLOR,                       BORDER_SPACE,
        BORDER_WIDTH
    };
}
