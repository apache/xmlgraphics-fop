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

import java.util.Vector;
import java.util.Hashtable;
import java.awt.Color;
import java.io.IOException;

/**
 * Singelton of the RTF color table.
 * This class was created for <fo:basic-link> tag processing.
 * @author <a href="mailto:a.putz@skynamics.com">Andreas Putz</a>
 */

public class RtfColorTable
{
    //////////////////////////////////////////////////
    // @@ Symbolic constants
    //////////////////////////////////////////////////

    // Defines the bit moving for the colors
    private static int RED = 16;
    private static int GREEN = 8;
    private static int BLUE = 0;


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
    private RtfColorTable ()
    {
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
    public static RtfColorTable getInstance ()
    {
        if (instance == null)
        {
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
    private void init ()
    {
        addNamedColor("black",getColorNumber (0, 0, 0));
        addNamedColor("white",getColorNumber (255, 255, 255));
        addNamedColor("red",getColorNumber (255, 0, 0));
        addNamedColor("green",getColorNumber (0, 255, 0));
        addNamedColor("blue",getColorNumber (0, 0, 255));
        addNamedColor("cyan",getColorNumber (0, 255, 255));
        addNamedColor("magenta",getColorNumber (255, 0, 255));
        addNamedColor("yellow",getColorNumber (255, 255, 0));

        getColorNumber (0, 0, 128);
        getColorNumber (0, 128, 128);
        getColorNumber (0, 128, 0);
        getColorNumber (128, 0, 128);
        getColorNumber (128, 0, 0);
        getColorNumber (128, 128, 0);
        getColorNumber (128, 128, 128);

         // Added by Normand Masse
          // Gray color added
        addNamedColor( "gray", getColorNumber( 128, 128, 128 ) );

        getColorNumber (192, 192, 192);
    }

        /** define a named color for getColorNumber(String) */
        private void addNamedColor(String name,int colorNumber)
        {
            namedColors.put(name.toLowerCase(),new Integer(colorNumber));
        }

    //////////////////////////////////////////////////
    // @@ Public methods
    //////////////////////////////////////////////////

        /** get the RTF number of a named color
         *  @return null if name not found
         */
    public Integer getColorNumber (String name)
    {
            return (Integer)namedColors.get(name.toLowerCase());
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
    public int getColorNumber (int red, int green, int blue)
    {
        Integer identifier = new Integer (determineIdentifier (red, green, blue));
        Object o = colorIndex.get (identifier);
        int retVal;

        if (o == null)
        {
            addColor (identifier);

            retVal = colorTable.size ();
        }
        else
        {
            retVal = ((Integer) o).intValue ();
        }

        return retVal + 1;
    }

    /**
     * Writes the color table in the header.
     *
     * @param header The header container to write in
     *
     * @throws IOException On error
     */
    public void writeColors (RtfHeader header) throws IOException
    {
        if (colorTable == null || colorTable.size () == 0)
        {
            return;
        }

        header.writeGroupMark (true);
        header.writeControlWord ("colortbl;");

        int len = colorTable.size ();

        for (int i = 0; i < len; i++)
        {
            int identifier = ((Integer) colorTable.get (i)).intValue ();

            header.write ("\\red" + determineColorLevel (identifier, RED));
            header.write ("\\green" + determineColorLevel (identifier, GREEN));
            header.write ("\\blue" + determineColorLevel (identifier, BLUE) + ";");
        }

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
    private void addColor (Integer i)
    {
        colorIndex.put (i, new Integer (colorTable.size () + 1));
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
    private int determineIdentifier (int red, int green, int blue)
    {
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
    private int determineColorLevel (int identifier, int color)
    {
        int retVal = (byte) (identifier >> color);

        return retVal < 0 ? retVal + 256 : retVal;
    }
}