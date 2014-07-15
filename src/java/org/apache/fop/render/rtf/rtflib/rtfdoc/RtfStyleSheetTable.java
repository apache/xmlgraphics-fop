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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>Singelton of the RTF style sheet table.
 * This class belongs to the <jfor:stylesheet> tag processing.</p>
 *
 * <p>This work was authored by Andreas Putz (a.putz@skynamics.com).</p>
 */
public final class RtfStyleSheetTable {
    //////////////////////////////////////////////////
    // @@ Symbolic constants
    //////////////////////////////////////////////////

    /** Start index number for the stylesheet reference table */
    private static int startIndex = 15;

    /** OK status value for attribute handling */
    public static final int STATUS_OK = 0;
    /** Status value for attribute handling, if the stylesheet not found and
     *  the stylesheet set to the default stylesheet */
    public static final int STATUS_DEFAULT = 1;

    /** Standard style name */
    private static final String STANDARD_STYLE = "Standard";


    //////////////////////////////////////////////////
    // @@ Singleton
    //////////////////////////////////////////////////

    /** Singelton instance */
    private static RtfStyleSheetTable instance;


    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////


    /** Table of styles */
    private Hashtable styles;

    /** Used, style attributes to this vector */
    private Hashtable attrTable;

    /** Used, style names to this vector */
    private Vector nameTable;

    /** Default style */
    private String defaultStyleName = STANDARD_STYLE;


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     */
    private RtfStyleSheetTable() {
        styles = new Hashtable();
        attrTable = new Hashtable();
        nameTable = new Vector();
    }

    /**
     * Singelton.
     *
     * @return The instance of RtfStyleSheetTable
     */
    public static RtfStyleSheetTable getInstance() {
        if (instance == null) {
            instance = new RtfStyleSheetTable();
        }

        return instance;
    }


    //////////////////////////////////////////////////
    // @@ Member access
    //////////////////////////////////////////////////

    /**
     * Sets the default style.
     * @param styleName Name of the default style, defined in the stylesheet
     */
    public void setDefaultStyle(String styleName) {
        this.defaultStyleName = styleName;
    }

    /**
     * Gets the name of the default style.
     * @return Default style name.
     */
    public String getDefaultStyleName() {
        if (attrTable.get(defaultStyleName) != null) {
            return defaultStyleName;
        }

        if (attrTable.get(STANDARD_STYLE) != null) {
            defaultStyleName = STANDARD_STYLE;
            return defaultStyleName;
        }

        return null;
    }


    //////////////////////////////////////////////////
    // @@ Public methods
    //////////////////////////////////////////////////

    /**
     * Adds a style to the table.
     * @param name Name of style to add
     * @param attrs Rtf attributes which defines the style
     */
    public void addStyle(String name, RtfAttributes attrs) {
        nameTable.addElement(name);
        if (attrs != null) {
            attrTable.put(name, attrs);
        }
        styles.put(name, new Integer(nameTable.size() - 1 + startIndex));
    }

    /**
     * Adds the style attributes to the given attributes.
     * @param name Name of style, of which the attributes will copied to attr
     * @param attr Default rtf attributes
     * @return Status value
     */
    public int addStyleToAttributes(String name, RtfAttributes attr) {
        // Sets status to ok
        int status = STATUS_OK;

        // Gets the style number from table
        Integer style  = (Integer) styles.get(name);

        if (style == null && !name.equals(defaultStyleName)) {
            // If style not found, and style was not the default style, try the default style
            name = defaultStyleName;
            style = (Integer) styles.get(name);
            // set status for default style setting
            status = STATUS_DEFAULT;
        }

        // Returns the status for invalid styles
        if (style == null) {
            return status;
        }

        // Adds the attributes to default attributes, if not available in default attributes
        attr.set("cs", style.intValue());

        Object o = attrTable.get(name);
        if (o != null) {
            RtfAttributes rtfAttr = (RtfAttributes) o;

            for (Iterator names = rtfAttr.nameIterator(); names.hasNext();) {
                String attrName = (String) names.next();
                if (!attr.isSet(attrName)) {
                    Integer i = (Integer) rtfAttr.getValue(attrName);
                    if (i == null) {
                        attr.set(attrName);
                    } else {
                        attr.set(attrName, i.intValue());
                    }
                }
            }
        }
        return status;
    }

    /**
     * Writes the rtf style sheet table.
     * @param header Rtf header is the parent
     * @throws IOException On write error
     */
    public void writeStyleSheet(RtfHeader header) throws IOException {
        if (styles == null || styles.size() == 0) {
            return;
        }
        header.writeGroupMark(true);
        header.writeControlWord("stylesheet");

        int number = nameTable.size();
        for (int i = 0; i < number; i++) {
            String name = (String) nameTable.elementAt(i);
            header.writeGroupMark(true);
            header.writeControlWord("*\\" + this.getRtfStyleReference(name));

            Object o = attrTable.get(name);
            if (o != null) {
                header.writeAttributes((RtfAttributes) o, RtfText.ATTR_NAMES);
                header.writeAttributes((RtfAttributes) o, RtfText.ALIGNMENT);
            }

            header.write(name + ";");
            header.writeGroupMark(false);
        }
        header.writeGroupMark(false);
    }

    /**
     * Gets the rtf style reference from the table.
     * @param name Name of Style
     * @return Rtf attribute of the style reference
     */
    private String getRtfStyleReference(String name) {
        return "cs" + styles.get(name).toString();
    }
}
