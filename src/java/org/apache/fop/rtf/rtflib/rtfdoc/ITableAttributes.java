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

/** Constants for RTF table attribute names */
public interface ITableAttributes {
    /**
     * Added by Boris POUDEROUS on 2002/06/27 in order to
     * process column/row spanning :
     */
    public static final String COLUMN_SPAN = "number-columns-spanned";
    public static final String ROW_SPAN = "number-rows-spanned";

    // RTF 1.5 attributes (word 97)
    // half the space between the cells of a table row in twips
    String ATTR_RTF_15_TRGAPH = "trgaph";

    // RTF 1.6 Row and table attributes
    String ATTR_ROW_PADDING_TOP = "trpaddt";
    String ATTR_ROW_PADDING_BOTTOM = "trpaddb";
    String ATTR_ROW_PADDING_LEFT = "trpaddl";
    String ATTR_ROW_PADDING_RIGHT = "trpaddr";

    String ATTR_ROW_U_PADDING_TOP = "trpaddft";
    String ATTR_ROW_U_PADDING_BOTTOM = "trpaddfb";
    String ATTR_ROW_U_PADDING_LEFT = "trpaddfl";
    String ATTR_ROW_U_PADDING_RIGHT = "trpaddfr";

    // list of ALL ROW PADDING attributes, used to select them
    // when writing attributes
    String[] ATTRIB_ROW_PADDING = {
        ATTR_ROW_PADDING_TOP, ATTR_ROW_U_PADDING_TOP,
        ATTR_ROW_PADDING_BOTTOM, ATTR_ROW_U_PADDING_BOTTOM,
        ATTR_ROW_PADDING_LEFT, ATTR_ROW_U_PADDING_LEFT,
        ATTR_ROW_PADDING_RIGHT, ATTR_ROW_U_PADDING_RIGHT,
        ATTR_RTF_15_TRGAPH
    };

    // Cell attributes
    String ATTR_CELL_PADDING_TOP = "clpadt";
    String ATTR_CELL_PADDING_BOTTOM = "clpadb";
    String ATTR_CELL_PADDING_LEFT = "clpadl";
    String ATTR_CELL_PADDING_RIGHT = "clpadr";

    String ATTR_CELL_U_PADDING_TOP = "clpadft";
    String ATTR_CELL_U_PADDING_BOTTOM = "clpadfb";
    String ATTR_CELL_U_PADDING_LEFT = "clpadfl";
    String ATTR_CELL_U_PADDING_RIGHT = "clpadfr";

    // These lines added by Chris Scott, Westinghouse
    // need to make a border style file

    String CELL_BORDER_TOP = "clbrdrt";
    String CELL_BORDER_BOTTOM = "clbrdrb";
    String CELL_BORDER_LEFT = "clbrdrl";
    String CELL_BORDER_RIGHT = "clbrdrr";
    //Table row border attributes
    String ROW_BORDER_TOP = "trbrdrt";
    String ROW_BORDER_BOTTOM = "trbrdrb";
    String ROW_BORDER_LEFT = "trbrdrl";
    String ROW_BORDER_RIGHT = "trbrdrr";
    String ROW_BORDER_HORIZONTAL = "trbrdrh";
    String ROW_BORDER_VERTICAL = "trbrdrv";

    //Table row attributes
    String ROW_KEEP_TOGETHER = "trkeep";

    //This control word is nonexistent in RTF, used
	//to simulate the FO:keep-with-next attribute.
     String ROW_KEEP_WITH_NEXT = "knext";

	//This control word is nonexistent in RTF, used
	//to simulate the FO:keep-with-previous attribute.
 	 String ROW_KEEP_WITH_PREVIOUS = "kprevious";

    //shading and color, all are unit based attributes
    String CELL_SHADE = "clshdng";
    String CELL_COLOR_BACKGROUND = "clcbpat";
    String CELL_COLOR_FOREGROUND = "clcfpat";

    // list of ALL CELL PADDING attributes, used to select them
    // when writing attributes
    String[] ATTRIB_CELL_PADDING = {
        ATTR_CELL_PADDING_TOP, ATTR_CELL_U_PADDING_TOP,
        ATTR_CELL_PADDING_BOTTOM, ATTR_CELL_U_PADDING_BOTTOM,
        ATTR_CELL_PADDING_LEFT, ATTR_CELL_U_PADDING_LEFT,
        ATTR_CELL_PADDING_RIGHT, ATTR_CELL_U_PADDING_RIGHT,
    };

    String[] CELL_BORDER = {
        CELL_BORDER_TOP,		CELL_BORDER_BOTTOM,
        CELL_BORDER_LEFT,		CELL_BORDER_RIGHT
    };

    String[] ROW_BORDER = {
        ROW_BORDER_TOP, 		ROW_BORDER_BOTTOM, 		ROW_BORDER_LEFT,
        ROW_BORDER_RIGHT,	ROW_BORDER_HORIZONTAL,	ROW_BORDER_VERTICAL
    };

    String[] CELL_COLOR = {
        CELL_SHADE,	CELL_COLOR_BACKGROUND,	CELL_COLOR_FOREGROUND
    };
}