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

        header.newLine();
        header.writeGroupMark (true);
        header.writeControlWord ("fonttbl;");

        int len = fontTable.size ();

        for (int i = 0; i < len; i++) {
            header.writeGroupMark (true);
            header.newLine();
            header.write ("\\f" + i);
            header.write (" " + (String) fontTable.elementAt (i));
            header.writeGroupMark (false);
        }

        header.newLine();
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
