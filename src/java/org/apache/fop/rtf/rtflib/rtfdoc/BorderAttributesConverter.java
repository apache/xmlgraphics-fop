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
 * The RTF library of the FOP project consists of voluntary contributions made by
 * many individuals on behalf of the Apache Software Foundation and was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and contributors of
 * the jfor project (www.jfor.org), who agreed to donate jfor to the FOP project.
 * For more information on the Apache Software Foundation, please
 * see <http://www.apache.org/>.
 */

package org.apache.fop.rtf.rtflib.rtfdoc;

/** Constants for RTF border attribute names, and a static method for converting fo attribute strings. */

public class BorderAttributesConverter {
    
    public static final String BORDER_SINGLE_THICKNESS = "brdrs";
    public static final String BORDER_DOUBLE_THICKNESS = "brdrth";
    public static final String BORDER_SHADOWED ="brdrsh";
    public static final String BORDER_DOUBLE ="brdrdb";
    public static final String BORDER_DOTTED ="brdrdot";
    public static final String BORDER_DASH ="brdrdash";
    public static final String BORDER_HAIRLINE ="brdrhair";
    public static final String BORDER_DASH_SMALL ="brdrdashsm";
    public static final String BORDER_DOT_DASH = "brdrdashd";
    public static final String BORDER_DOT_DOT_DASH = "brdrdashdd";
    public static final String BORDER_TRIPLE = "brdrtriple";
    public static final String BORDER_THINK_THIN_SMALL = "brdrtnthsg";
    public static final String BORDER_THIN_THICK_SMALL = "brdrthtnsg";
    public static final String BORDER_THIN_THICK_THIN_SMALL = "brdrthtnthsg";
    public static final String BORDER_THINK_THIN_MEDIUM = "brdrtnthmg";
    public static final String BORDER_THIN_THICK_MEDIUM = "brdrthtnmg";
    public static final String BORDER_THIN_THICK_THIN_MEDIUM = "brdrthtnthmg";
    public static final String BORDER_THINK_THIN_LARGE = "brdrtnthlg";
    public static final String BORDER_THIN_THICK_LARGE = "brdrthtnlg";
    public static final String BORDER_THIN_THICK_THIN_LARGE = "brdrthtnthlg";
    public static final String BORDER_WAVY = "brdrwavy";
    public static final String BORDER_WAVY_DOUBLE = "brdrwavydb";
    public static final String BORDER_STRIPED = "brdrdashdotstr";
    public static final String BORDER_EMBOSS = "brdremboss";
    public static final String BORDER_ENGRAVE = "brdrengrave";
    public static final String BORDER_COLOR = "brdrcf";
    public static final String BORDER_SPACE = "brsp";
    public static final String BORDER_WIDTH = "brdrw";
    
    public static final String [] BORDERS = new String[] {
        BORDER_SINGLE_THICKNESS,		BORDER_DOUBLE_THICKNESS,		BORDER_SHADOWED,
        BORDER_DOUBLE,				BORDER_DOTTED, 					BORDER_DASH,
        BORDER_HAIRLINE, 				BORDER_DASH_SMALL,				BORDER_DOT_DASH,
        BORDER_DOT_DOT_DASH,		BORDER_TRIPLE,						BORDER_THINK_THIN_SMALL,
        BORDER_THIN_THICK_SMALL,		BORDER_THIN_THICK_THIN_SMALL, 	BORDER_THINK_THIN_MEDIUM,
        BORDER_THIN_THICK_MEDIUM,	BORDER_THIN_THICK_THIN_MEDIUM,  	BORDER_THINK_THIN_LARGE,
        BORDER_THIN_THICK_LARGE,		BORDER_THIN_THICK_THIN_LARGE, 	BORDER_WAVY,
        BORDER_WAVY_DOUBLE, 		BORDER_STRIPED, 					BORDER_EMBOSS,
        BORDER_ENGRAVE,				BORDER_COLOR,						BORDER_SPACE,
        BORDER_WIDTH
    };
    
    /**BorderAttributesConverter: Static Method for converting FO strings
     * to RTF control words*/
    public static String convertAttributetoRtf(String value) {
        // Added by Normand Masse
        // "solid" is interpreted like "thin"        
		if(value.equals("thin") || value.equals("solid")){
            return BORDER_SINGLE_THICKNESS;
        }else if (value.equals("thick")) {
            return BORDER_DOUBLE_THICKNESS;
        }else if(value.equals("shadowed")){
            return BORDER_SHADOWED;
        }else if(value.equals("double")){
            return BORDER_DOUBLE;
        }else if(value.equals("dotted")){
            return BORDER_DOTTED;
        }else if(value.equals("dash")){
            return BORDER_DASH;
        }else if(value.equals("hairline")){
            return BORDER_HAIRLINE;
        }else if(value.equals("dot-dash")){
            return BORDER_DOT_DASH;
        }else if(value.equals("dot-dot-dash")){
            return BORDER_DOT_DOT_DASH;
        }else if(value.equals("triple")){
            return BORDER_TRIPLE;
        }else if(value.equals("wavy")){
            return BORDER_WAVY;
        }else if(value.equals("wavy-double")){
            return BORDER_WAVY_DOUBLE;
        }else if(value.equals("striped")){
            return BORDER_STRIPED;
        }else if(value.equals("emboss")){
            return BORDER_EMBOSS;
        }else if(value.equals("engrave")){
            return BORDER_ENGRAVE;
        }else{
            return null;
        }
    }
    
    
}