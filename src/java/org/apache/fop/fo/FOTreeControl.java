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

// Java
import java.util.Map;
import java.util.Set;

// FOP
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.extensions.Bookmarks;
import org.apache.fop.fonts.FontMetrics;

// Avalon
import org.apache.avalon.framework.logger.Logger;

/**
 * An interface for classes that are conceptually the parent class of the
 * fo.pagination.Root object. The purpose of the interface is to maintain
 * encapsulation of the FO Tree classes, but to acknowledge that a higher-level
 * object is needed to control the building of the FO Tree, to provide it
 * with information about the environment, and to keep track of meta-type
 * information.
 */
public interface FOTreeControl {

    /**
     * @param family the font family
     * @param style the font style
     * @param weight the font weight
     * @return the String font name matching the parameters
     */
    String fontLookup(String family, String style,
                             int weight);

    /**
     * @param fontName the String containing the font name for which a
     * FontMetrics object is desired
     * @return the FontMetrics object matching the fontName parameter
     */
    FontMetrics getMetricsFor(String fontName);

    /**
     * @return true if the default font has been properly setup
     */
    boolean isSetupValid();

    /**
     * @return a Map containing the Fonts used in this FO Tree
     */
    Map getFonts();

    /**
     * Sets the Bookmark object which encapsulates the bookmarks for the FO
     * Tree.
     * @param bookmarks the Bookmark object encapsulating the bookmarks for this
     * FO Tree.
     */
    void setBookmarks(Bookmarks bookmarks);

    /**
     * @return the Bookmark object encapsulating the bookmarks for the FO Tree.
     */
    Bookmarks getBookmarks();

    /**
     * Returns the set of ID references found in the FO Tree.
     * @return the ID references
     */
    Set getIDReferences();

    /**
     * @return the FOInputHandler for parsing this FO Tree
     */
    FOInputHandler getFOInputHandler();

    /**
     * @return the Logger being used with this FO Tree
     */
    Logger getLogger();

    /**
     * @return the FOUserAgent used for processing this FO Tree
     */
    FOUserAgent getUserAgent();

}
