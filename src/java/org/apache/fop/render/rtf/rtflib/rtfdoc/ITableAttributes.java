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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/**
 * Constants for RTF table attribute names
 * @author unascribed
 * @author Boris POUDEROUS
 * @author Chris Scott, Westinghouse
 */
public interface ITableAttributes {
    /** to process column spanning */
    String COLUMN_SPAN = "number-columns-spanned";
    /** to process row spanning */
    String ROW_SPAN = "number-rows-spanned";

// RTF 1.5 attributes (word 97)

    /** half the space between the cells of a table row in twips */
    String ATTR_RTF_15_TRGAPH = "trgaph";
    String ATTR_ROW_LEFT_INDENT = "trleft";
    
    /** table row header */
    public final String ATTR_HEADER = "trhdr";

// RTF 1.6 Row and table attributes
    /** table row padding, top */
    String ATTR_ROW_PADDING_TOP = "trpaddt";
    /** table row padding, bottom */
    String ATTR_ROW_PADDING_BOTTOM = "trpaddb";
    /** table row padding, left */
    String ATTR_ROW_PADDING_LEFT = "trpaddl";
    /** table row padding, right */
    String ATTR_ROW_PADDING_RIGHT = "trpaddr";

    /** table row padding, top */
    String ATTR_ROW_U_PADDING_TOP = "trpaddft";
    /** table row padding, bottom */
    String ATTR_ROW_U_PADDING_BOTTOM = "trpaddfb";
    /** table row padding, left */
    String ATTR_ROW_U_PADDING_LEFT = "trpaddfl";
    /** table row padding, right */
    String ATTR_ROW_U_PADDING_RIGHT = "trpaddfr";

    /**
     * List of ALL ROW PADDING attributes, used to select them when writing
     * attributes
     */
    String[] ATTRIB_ROW_PADDING = {
        ATTR_ROW_PADDING_TOP, ATTR_ROW_U_PADDING_TOP,
        ATTR_ROW_PADDING_BOTTOM, ATTR_ROW_U_PADDING_BOTTOM,
        ATTR_ROW_PADDING_LEFT, ATTR_ROW_U_PADDING_LEFT,
        ATTR_ROW_PADDING_RIGHT, ATTR_ROW_U_PADDING_RIGHT,
        ATTR_RTF_15_TRGAPH, ATTR_ROW_LEFT_INDENT
    };

// Cell attributes
    /** cell padding, top */
    String ATTR_CELL_PADDING_TOP = "clpadt";
    /** cell padding, bottom */
    String ATTR_CELL_PADDING_BOTTOM = "clpadb";
    /** cell padding, left */
    String ATTR_CELL_PADDING_LEFT = "clpadl";
    /** cell padding, right */
    String ATTR_CELL_PADDING_RIGHT = "clpadr";

    /** cell padding, top */
    String ATTR_CELL_U_PADDING_TOP = "clpadft";
    /** cell padding, bottom */
    String ATTR_CELL_U_PADDING_BOTTOM = "clpadfb";
    /** cell padding, left */
    String ATTR_CELL_U_PADDING_LEFT = "clpadfl";
    /** cell padding, right */
    String ATTR_CELL_U_PADDING_RIGHT = "clpadfr";

// for border style file
    /** cell border, top */
    String CELL_BORDER_TOP = "clbrdrt";
    /** cell border, bottom */
    String CELL_BORDER_BOTTOM = "clbrdrb";
    /** cell border, left */
    String CELL_BORDER_LEFT = "clbrdrl";
    /** cell border, right */
    String CELL_BORDER_RIGHT = "clbrdrr";

//Table row border attributes
    /** row border, top */
    String ROW_BORDER_TOP = "trbrdrt";
    /** row border, bottom */
    String ROW_BORDER_BOTTOM = "trbrdrb";
    /** row border, left */
    String ROW_BORDER_LEFT = "trbrdrl";
    /** row border, right */
    String ROW_BORDER_RIGHT = "trbrdrr";
    /** row border, horizontal */
    String ROW_BORDER_HORIZONTAL = "trbrdrh";
    /** row border, vertical */
    String ROW_BORDER_VERTICAL = "trbrdrv";

//Table row attributes
    /** row attribute, keep-together */
    String ROW_KEEP_TOGETHER = "trkeep";
    String ROW_HEIGHT = "trrh";

    /**
     * This control word is nonexistent in RTF, used to simulate the
     * FO:keep-with-next attribute.
     */
    String ROW_KEEP_WITH_NEXT = "knext";

    /**
     * This control word is nonexistent in RTF, used to simulate the
     * FO:keep-with-previous attribute.
     */
    String ROW_KEEP_WITH_PREVIOUS = "kprevious";

    /** cell shading, a unit-based attribute */
    String CELL_SHADE = "clshdng";
    /** cell background color, a unit-based attribute */
    String CELL_COLOR_BACKGROUND = "clcbpat";
    /** cell foreground color, a unit-based attribute */
    String CELL_COLOR_FOREGROUND = "clcfpat";

    /**
     * List of ALL CELL PADDING attributes, used to select them when writing
     * attributes
     */
    String[] ATTRIB_CELL_PADDING = {
        ATTR_CELL_PADDING_TOP, ATTR_CELL_U_PADDING_TOP,
        ATTR_CELL_PADDING_BOTTOM, ATTR_CELL_U_PADDING_BOTTOM,
        ATTR_CELL_PADDING_LEFT, ATTR_CELL_U_PADDING_LEFT,
        ATTR_CELL_PADDING_RIGHT, ATTR_CELL_U_PADDING_RIGHT,
    };

    /**
     * List of ALL CELL BORDER attributes, used to select them when writing
     * attributes
     */
    String[] CELL_BORDER = {
        CELL_BORDER_TOP,    CELL_BORDER_BOTTOM,
        CELL_BORDER_LEFT,   CELL_BORDER_RIGHT
    };

    /**
     * List of ALL ROW BORDER attributes, used to select them when writing
     * attributes
     */
    String[] ROW_BORDER = {
        ROW_BORDER_TOP,     ROW_BORDER_BOTTOM,        ROW_BORDER_LEFT,
        ROW_BORDER_RIGHT,   ROW_BORDER_HORIZONTAL,    ROW_BORDER_VERTICAL
    };

    /**
     * List of ALL CELL SHADING AND COLOR attributes, used to select them when
     * writing attributes
     */
    String[] CELL_COLOR = {
        CELL_SHADE,    CELL_COLOR_BACKGROUND,    CELL_COLOR_FOREGROUND
    };
}
