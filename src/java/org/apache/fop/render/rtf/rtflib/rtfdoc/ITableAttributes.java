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

/* $Id$ */


/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

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
    
    /**
     *  Position of the leftmost edge of the table with respect to the
     * left edge of its column
     */
    String ATTR_ROW_LEFT_INDENT = "trleft";
    
    /** table row header */
    String ATTR_HEADER = "trhdr";

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

//  for vertical alignment in cells
    /** cell alignment, top */
    String ATTR_CELL_VERT_ALIGN_TOP = "clvertalt";
    /** cell alignment, center */
    String ATTR_CELL_VERT_ALIGN_CENTER = "clvertalc";
    /** cell alignment, bottom */
    String ATTR_CELL_VERT_ALIGN_BOTTOM = "clvertalb";

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
    
    /** Height of a table row in twips */
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

    /**
     * List of ALL vertical alignment attributes, used to select them when writing
     * attributes
     */
    String[] CELL_VERT_ALIGN = {
        ATTR_CELL_VERT_ALIGN_TOP, ATTR_CELL_VERT_ALIGN_CENTER, ATTR_CELL_VERT_ALIGN_BOTTOM};
    
}
