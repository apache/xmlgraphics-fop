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
import java.util.Vector;

/**
 * <p>Singelton of the RTF color table.
 * This class was created for <fo:basic-link> tag processing.</p>
 *
 * <p>This work was authored by Andreas Putz (a.putz@skynamics.com).</p>
 */

public final class RtfColorTable {
    //////////////////////////////////////////////////
    // @@ Symbolic constants
    //////////////////////////////////////////////////

    // Defines the bit moving for the colors
    private static final int RED = 16;
    private static final int GREEN = 8;
    private static final int BLUE = 0;


    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////

    /** Singelton instance */
    private static RtfColorTable instance = null;

    /** Index table for the colors */
    private Hashtable colorIndex = null;
    /** Used colors to this vector */
    private Vector colorTable = null;
        /** Map of color names to color numbers */
        private Hashtable namedColors = null;


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     */
    private RtfColorTable () {
        colorTable = new Vector ();
        colorIndex = new Hashtable ();
                namedColors = new Hashtable ();

        init ();
    }

    /**
     * Singelton.
     *
     * @return The instance of RTFColorTable
     */
    public static RtfColorTable getInstance () {
        if (instance == null) {
            instance = new RtfColorTable ();
        }

        return instance;
    }


    //////////////////////////////////////////////////
    // @@ Initializing
    //////////////////////////////////////////////////

    /**
     * Initialize the color table.
     */
    private void init () {
        addNamedColor("black", getColorNumber (0, 0, 0).intValue());
        addNamedColor("white", getColorNumber (255, 255, 255).intValue());
        addNamedColor("red", getColorNumber (255, 0, 0).intValue());
        addNamedColor("green", getColorNumber (0, 255, 0).intValue());
        addNamedColor("blue", getColorNumber (0, 0, 255).intValue());
        addNamedColor("cyan", getColorNumber (0, 255, 255).intValue());
        addNamedColor("magenta", getColorNumber (255, 0, 255).intValue());
        addNamedColor("yellow", getColorNumber (255, 255, 0).intValue());

        getColorNumber (0, 0, 128);
        getColorNumber (0, 128, 128);
        getColorNumber (0, 128, 0);
        getColorNumber (128, 0, 128);
        getColorNumber (128, 0, 0);
        getColorNumber (128, 128, 0);
        getColorNumber (128, 128, 128);

         // Added by Normand Masse
          // Gray color added
        addNamedColor("gray", getColorNumber(128, 128, 128).intValue());

        getColorNumber (192, 192, 192);
    }

        /** define a named color for getColorNumber(String) */
        private void addNamedColor(String name, int colorNumber) {
            namedColors.put(name.toLowerCase(), new Integer(colorNumber));
        }

    //////////////////////////////////////////////////
    // @@ Public methods
    //////////////////////////////////////////////////

        /**
         * @param name a named color
         * @return the RTF number of a named color, or null if name not found
         */
    public Integer getColorNumber (String name) {
        return ((Integer)namedColors.get(name.toLowerCase()));
    }

    /**
     * Gets the number of color in the color table
     *
     * @param red Color level red
     * @param green Color level green
     * @param blue Color level blue
     *
     * @return The number of the color in the table
     */
    public Integer getColorNumber (int red, int green, int blue) {
        Integer identifier = new Integer (determineIdentifier (red, green, blue));
        Object o = colorIndex.get (identifier);
        int retVal;

        if (o == null) {
            //The color currently does not exist, so add it to the table.
            //First add it, then read the size as index (to return it).
            //So the first added color gets index 1. That is OK, because
            //index 0 is reserved for auto-colored.
            addColor (identifier);

            retVal = colorTable.size ();
        } else {
            //The color was found. Before returning the index, increment
            //it by one. Because index 0 is reserved for auto-colored, but
            //is not contained in colorTable.
            retVal = ((Integer) o).intValue () + 1;
        }

        return new Integer(retVal);
    }

    /**
     * Writes the color table in the header.
     *
     * @param header The header container to write in
     *
     * @throws IOException On error
     */
    public void writeColors (RtfHeader header) throws IOException {
        if (colorTable == null || colorTable.size () == 0) {
            return;
        }

        header.newLine();
        header.writeGroupMark (true);
        //Don't use writeControlWord, because it appends a blank,
        //which may confuse Wordpad.
        //This also implicitly writes the first color (=index 0), which
        //is reserved for auto-colored.
        header.write ("\\colortbl;");

        int len = colorTable.size ();

        for (int i = 0; i < len; i++) {
            int identifier = ((Integer) colorTable.get (i)).intValue ();

            header.newLine();
            header.write ("\\red" + determineColorLevel (identifier, RED));
            header.write ("\\green" + determineColorLevel (identifier, GREEN));
            header.write ("\\blue" + determineColorLevel (identifier, BLUE) + ";");
        }

        header.newLine();
        header.writeGroupMark (false);
    }


    //////////////////////////////////////////////////
    // @@ Private methods
    //////////////////////////////////////////////////

    /**
     * Adds a color to the table.
     *
     * @param i Identifier of color
     */
    private void addColor (Integer i) {
        colorIndex.put (i, new Integer (colorTable.size ()));
        colorTable.addElement (i);
    }

    /**
     * Determines a identifier for the color.
     *
     * @param red Color level red
     * @param green Color level green
     * @param blue Color level blue
     *
     * @return Unique identifier of color
     */
    private int determineIdentifier (int red, int green, int blue) {
        int c = red << RED;

        c += green << GREEN;
        c += blue << BLUE;

        return c;
    }

    /**
     * Determines the color level from the identifier.
     *
     * @param identifier Unique color identifier
     * @param color One of the bit moving constants
     *
     * @return Color level in byte size
     */
    private int determineColorLevel (int identifier, int color) {
        int retVal = (byte) (identifier >> color);

        return retVal < 0 ? retVal + 256 : retVal;
    }
}
