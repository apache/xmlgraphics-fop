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

package org.apache.fop.fo;

// Java
import java.util.Map;
import java.util.Set;

// FOP
import org.apache.fop.fo.extensions.Bookmarks;
import org.apache.fop.fonts.FontMetrics;

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
}
