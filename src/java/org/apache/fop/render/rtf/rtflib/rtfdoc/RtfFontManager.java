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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 *   RTF font table
 *  @author Andreas Putz a.putz@skynamics.com
 */
public class RtfFontManager {
    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////

    /** Singelton instance */
    private static RtfFontManager instance = null;

    /** Index table for the fonts */
    private Hashtable fontIndex = null;
    /** Used fonts to this vector */
    private Vector fontTable = null;


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     */
    private RtfFontManager () {
        fontTable = new Vector ();
        fontIndex = new Hashtable ();

        init ();
    }

    /**
     * Singelton.
     *
     * @return The instance of RtfFontManager
     */
    public static RtfFontManager getInstance () {
        if (instance == null) {
            instance = new RtfFontManager ();
        }

        return instance;
    }


    //////////////////////////////////////////////////
    // @@ Initializing
    //////////////////////////////////////////////////

    /**
     * Initialize the font table.
     */
    private void init () {

//        getFontNumber ("Helvetica");
        //Chanded by R.Marra default font Arial
        getFontNumber ("Arial");
        getFontNumber ("Symbol"); // used by RtfListItem.java
        getFontNumber ("Times New Roman");

/*
        {\\f0\\fswiss Helv;}

        // f1 is used by RtfList and RtfListItem for bullets

        {\\f1\\froman\\fcharset2 Symbol;}
        {\\f2\\froman\\fprq2 Times New Roman;}
        {\\f3\\froman Times New Roman;}
*/
    }


    //////////////////////////////////////////////////
    // @@ Public methods
    //////////////////////////////////////////////////


    /**
     * Gets the number of font in the font table
     *
     * @param family Font family name ('Helvetica')
     *
     * @return The number of the font in the table
     */
    public int getFontNumber (String family) {

        family = family.toLowerCase ();
        Object o = fontIndex.get (family);
        int retVal;

        if (o == null) {
            addFont (family);

            retVal = fontTable.size () - 1;
        } else {
            retVal = ((Integer) o).intValue ();
        }

        return retVal;
    }

    /**
     * Writes the font table in the header.
     *
     * @param header The header container to write in
     *
     * @throws IOException On error
     */
    public void writeFonts (RtfHeader header) throws IOException {
        if (fontTable == null || fontTable.size () == 0) {
            return;
        }

        header.writeGroupMark (true);
        header.writeControlWord ("fonttbl;");

        int len = fontTable.size ();

        for (int i = 0; i < len; i++) {
            header.writeGroupMark (true);
            header.write ("\\f" + i);
            header.write (" " + (String) fontTable.elementAt (i));
            header.writeGroupMark (false);
        }

        header.writeGroupMark (false);
    }


    //////////////////////////////////////////////////
    // @@ Private methods
    //////////////////////////////////////////////////

    /**
     * Adds a font to the table.
     *
     * @param i Identifier of font
     */
    private void addFont (String family) {
        fontIndex.put (family, new Integer (fontTable.size ()));
        fontTable.addElement (family);
    }
}
