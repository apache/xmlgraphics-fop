/*
 * $Id$
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

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.rtf.rtflib.rtfdoc;

/** Constants for RTF border attribute names, and a static method for converting
 *  fo attribute strings. */

public class BorderAttributesConverter {

    /** Constant for a single-thick border */
    public static final String BORDER_SINGLE_THICKNESS = "brdrs";
    /** Constant for a double-thick border */
    public static final String BORDER_DOUBLE_THICKNESS = "brdrth";
    /** Constant for a shadowed border */
    public static final String BORDER_SHADOWED = "brdrsh";
    /** Constant for a double border */
    public static final String BORDER_DOUBLE = "brdrdb";
    /** Constant for a dotted border */
    public static final String BORDER_DOTTED = "brdrdot";
    /** Constant for a dashed border */
    public static final String BORDER_DASH = "brdrdash";
    /** Constant for a hairline border */
    public static final String BORDER_HAIRLINE = "brdrhair";
    /** Constant for a small-dashed border */
    public static final String BORDER_DASH_SMALL = "brdrdashsm";
    /** Constant for a dot-dashed border */
    public static final String BORDER_DOT_DASH = "brdrdashd";
    /** Constant for a dot-dot-dashed border */
    public static final String BORDER_DOT_DOT_DASH = "brdrdashdd";
    /** Constant for a triple border */
    public static final String BORDER_TRIPLE = "brdrtriple";
    /** Constant for a think-thin-small border */
    public static final String BORDER_THINK_THIN_SMALL = "brdrtnthsg";
    /** Constant for a thin-thick-small border */
    public static final String BORDER_THIN_THICK_SMALL = "brdrthtnsg";
    /** Constant for a thin-thick-thin-small border */
    public static final String BORDER_THIN_THICK_THIN_SMALL = "brdrthtnthsg";
    /** Constant for a think-thin-medium border */
    public static final String BORDER_THINK_THIN_MEDIUM = "brdrtnthmg";
    /** Constant for a thin-thick-medium border */
    public static final String BORDER_THIN_THICK_MEDIUM = "brdrthtnmg";
    /** Constant for a thin-thick-thin-medium border */
    public static final String BORDER_THIN_THICK_THIN_MEDIUM = "brdrthtnthmg";
    /** Constant for a think-thin-large border */
    public static final String BORDER_THINK_THIN_LARGE = "brdrtnthlg";
    /** Constant for a thin-thick-large border */
    public static final String BORDER_THIN_THICK_LARGE = "brdrthtnlg";
    /** Constant for a thin-thick-thin-large border */
    public static final String BORDER_THIN_THICK_THIN_LARGE = "brdrthtnthlg";
    /** Constant for a wavy border */
    public static final String BORDER_WAVY = "brdrwavy";
    /** Constant for a double wavy border */
    public static final String BORDER_WAVY_DOUBLE = "brdrwavydb";
    /** Constant for a striped border */
    public static final String BORDER_STRIPED = "brdrdashdotstr";
    /** Constant for an embossed border */
    public static final String BORDER_EMBOSS = "brdremboss";
    /** Constant for an engraved border */
    public static final String BORDER_ENGRAVE = "brdrengrave";
    /** Constant for border color */
    public static final String BORDER_COLOR = "brdrcf";
    /** Constant for border space */
    public static final String BORDER_SPACE = "brsp";
    /** Constant for border width */
    public static final String BORDER_WIDTH = "brdrw";

    /** String array of border attributes */
    public static final String [] BORDERS = new String[] {
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

    /**
    * BorderAttributesConverter: Static Method for converting FO strings
    * to RTF control words
    * @param value FO string
    * @return RTF control word
    */
    public static String convertAttributetoRtf(String value) {
        // Added by Normand Masse
        // "solid" is interpreted like "thin"
        if (value.equals("thin") || value.equals("solid")) {
            return BORDER_SINGLE_THICKNESS;
        } else if (value.equals("thick")) {
            return BORDER_DOUBLE_THICKNESS;
        } else if (value.equals("shadowed")) {
            return BORDER_SHADOWED;
        } else if (value.equals("double")) {
            return BORDER_DOUBLE;
        } else if (value.equals("dotted")) {
            return BORDER_DOTTED;
        } else if (value.equals("dash")) {
            return BORDER_DASH;
        } else if (value.equals("hairline")) {
            return BORDER_HAIRLINE;
        } else if (value.equals("dot-dash")) {
            return BORDER_DOT_DASH;
        } else if (value.equals("dot-dot-dash")) {
            return BORDER_DOT_DOT_DASH;
        } else if (value.equals("triple")) {
            return BORDER_TRIPLE;
        } else if (value.equals("wavy")) {
            return BORDER_WAVY;
        } else if (value.equals("wavy-double")) {
            return BORDER_WAVY_DOUBLE;
        } else if (value.equals("striped")) {
            return BORDER_STRIPED;
        } else if (value.equals("emboss")) {
            return BORDER_EMBOSS;
        } else if (value.equals("engrave")) {
            return BORDER_ENGRAVE;
        } else {
            return null;
        }
    }


}