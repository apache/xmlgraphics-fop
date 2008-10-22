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

package org.apache.fop.render.afp.extensions;

import org.xml.sax.helpers.AttributesImpl;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
//import org.xml.sax.helpers.AttributesImpl;

/**
 * An AFP resource group configuration definition for a document
 */
public class AFPResourceInfo extends AFPExtensionAttachment {
    private static final long serialVersionUID = -7333967815112216396L;

    /** AFP resource groups are stored in an external resource file */
    private static final int LEVEL_INLINE = 0;

    /** AFP resource groups are stored at page level */
    private static final int LEVEL_PAGE = 1;

    /** AFP resource groups are stored at page group level */
    private static final int LEVEL_PAGE_GROUP = 2;

    /** AFP resource groups are stored within the document at document level */
    private static final int LEVEL_DOCUMENT = 3;

    /** AFP resource groups are stored outside the document at print file level */
    private static final int LEVEL_PRINT_FILE = 4;

    /** AFP resource groups are stored in an external resource file */
    private static final int LEVEL_EXTERNAL = 5;


    private static final String LEVEL_NAME_INLINE = "inline";

    private static final String LEVEL_NAME_PAGE = "page";

    private static final String LEVEL_NAME_PAGE_GROUP = "page-group";

    private static final String LEVEL_NAME_DOCUMENT = "document";

    private static final String LEVEL_NAME_PRINT_FILE = "print-file";

    private static final String LEVEL_NAME_EXTERNAL = "external";


    /**
     * level token/name mapping
     */
    private static final String[] LEVEL_NAME_MAP = {
        LEVEL_NAME_INLINE, LEVEL_NAME_PAGE, LEVEL_NAME_PAGE_GROUP,
        LEVEL_NAME_DOCUMENT, LEVEL_NAME_PRINT_FILE, LEVEL_NAME_EXTERNAL
    };

    /**
     * the <afp:resource-info/> element name
     */
    public static final String ELEMENT = "resource-info";

    /**
     * the level at which resource groups are placed
     */
    private int level = -1;

    /**
     * the destination filename for resource groups with level = "external"
     */
    private String dest;

    /**
     * Default constructor.
     */
    public AFPResourceInfo() {
        super(ELEMENT);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "AFPResourceInfo("
            + "name=" + name + ", "
            + (level > -1 ? "level=" + LEVEL_NAME_MAP[level] : "")
            + (dest != null ? ", dest=" + getDestination() : "" ) + ")";
    }

    /**
     * Sets the destination filename of where resources
     * are to be stored for this document
     * @param destination the location of the external resource group file
     */
    public void setExternalDestination(String destination) {
        this.dest = destination;
    }

    /**
     * Returns the destination filename of where external resources
     * are to be stored for this document.
     * @return the destination AFP external resource filename
     */
    public String getDestination() {
        return this.dest;
    }

    /**
     * Sets the level at which resource groups are stored
     * @param level the resource group level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Sets the level at which resource groups are stored
     * @param name the level name
     */
    public void setLevel(String name) {
        if (name != null) {
            for (int i = 0; i < LEVEL_NAME_MAP.length; i++) {
                if (name.toLowerCase().equals(LEVEL_NAME_MAP[i])) {
                    this.level = i;
                }
            }
        }
    }

    /**
     * Returns the level at which resource groups are stored
     * @return the level at which resource groups are stored
     */
    public int getLevel() {
        return this.level;
    }

    private static final String ATT_LEVEL = "level";

    private static final String ATT_DEST = "dest";

    /**
     * {@inheritDoc}
     */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        // name
        if (hasName()) {
            atts.addAttribute(null, ATT_NAME, ATT_NAME, "CDATA", super.getName());
        }

        // level
        if (level > 0) {
            atts.addAttribute(null, ATT_LEVEL, ATT_LEVEL, "CDATA", LEVEL_NAME_MAP[level]);

            // dest
            if (level == LEVEL_EXTERNAL && dest != null && dest.length() > 0) {
                atts.addAttribute(null, ATT_DEST, ATT_DEST, "CDATA", dest);
            }
        }
        handler.startElement(CATEGORY, elementName, elementName, atts);
        handler.endElement(CATEGORY, elementName, elementName);
    }

    /**
     * @return true if this resource group is to be stored externally
     */
    public boolean isExternalLevel() {
        return level == LEVEL_EXTERNAL;
    }
}