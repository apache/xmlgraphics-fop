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
import java.io.IOException;
import java.util.Iterator;

/**
 * Singelton of the RTF style sheet table.
 * This class belongs to the <jfor:stylesheet> tag processing.
 * @author <a href="mailto:a.putz@skynamics.com">Andreas Putz</a>
 */
public class RtfStyleSheetTable
{
    //////////////////////////////////////////////////
    // @@ Symbolic constants
    //////////////////////////////////////////////////

    /** Start index number for the stylesheet reference table */
    private static int startIndex = 15;

    /** OK status value for attribute handling */
    public static int STATUS_OK = 0;
    /** Status value for attribute handling, if the stylesheet not found and
     *  the stylesheet set to the default stylesheet */
    public static int STATUS_DEFAULT = 1;

    /** Standard style name */
    private static String STANDARD_STYLE = "Standard";


    //////////////////////////////////////////////////
    // @@ Singleton
    //////////////////////////////////////////////////

    /** Singelton instance */
    private static RtfStyleSheetTable instance = null;


    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////


    /** Table of styles */
    private Hashtable styles = null;

    /** Used, style attributes to this vector */
    private Hashtable attrTable = null;

    /** Used, style names to this vector */
    private Vector nameTable = null;

    /** Default style */
    private String defaultStyleName = STANDARD_STYLE;


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     */
    private RtfStyleSheetTable ()
    {
        styles = new Hashtable ();
        attrTable = new Hashtable ();
        nameTable = new Vector ();
    }

    /**
     * Singelton.
     *
     * @return The instance of RtfStyleSheetTable
     */
    public static RtfStyleSheetTable getInstance ()
    {
        if (instance == null)
        {
            instance = new RtfStyleSheetTable ();
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
    public void setDefaultStyle (String styleName)
    {
        this.defaultStyleName = styleName;
    }

    /**
     * Gets the name of the default style.
     * @return Default style name.
     */
    public String getDefaultStyleName ()
    {
        if (attrTable.get (defaultStyleName) != null)
            return defaultStyleName;

        if (attrTable.get (STANDARD_STYLE) != null)
        {
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
    public void addStyle (String name, RtfAttributes attrs)
    {
        nameTable.addElement (name);
        if (attrs != null)
            attrTable.put (name, attrs);
        styles.put (name, new Integer (nameTable.size () - 1 + startIndex));
    }

    /**
     * Adds the style attributes to the given attributes.
     * @param name Name of style, of which the attributes will copied to attr
     * @param attrs Default rtf attributes
     * @return Status value
     */
    public int addStyleToAttributes (String name, RtfAttributes attr)
    {
        // Sets status to ok
        int status = STATUS_OK;

        // Gets the style number from table
        Integer style  = (Integer) styles.get (name);

        if (style == null && !name.equals (defaultStyleName))
        {
            // If style not found, and style was not the default style, try the default style
            name = defaultStyleName;
            style = (Integer) styles.get (name);
            // set status for default style setting
            status = STATUS_DEFAULT;
        }

        // Returns the status for invalid styles
        if (style == null)
            return status;

        // Adds the attributes to default attributes, if not available in default attributes
        attr.set ("cs", style.intValue ());

        Object o = attrTable.get (name);
        if (o != null)
        {
            RtfAttributes rtfAttr = (RtfAttributes) o;

            for (Iterator names = rtfAttr.nameIterator (); names.hasNext ();)
            {
                String attrName = (String) names.next ();
                if (! attr.isSet (attrName))
                {
                    Integer i = (Integer) rtfAttr.getValue (attrName);
                    if (i == null)
                        attr.set (attrName);
                    else
                        attr.set (attrName, i.intValue ());
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
    public void writeStyleSheet (RtfHeader header) throws IOException
    {
        if (styles == null || styles.size () == 0)
        {
            return;
        }
        header.writeGroupMark (true);
        header.writeControlWord ("stylesheet");

        int number = nameTable.size ();
        for (int i = 0; i < number; i++)
        {
            String name = (String) nameTable.elementAt (i);
            header.writeGroupMark (true);
            header.writeControlWord ("*\\" + this.getRtfStyleReference (name));

            Object o = attrTable.get (name);
            if (o != null)
            {
                header.writeAttributes ((RtfAttributes) o, RtfText.ATTR_NAMES);
                header.writeAttributes ((RtfAttributes) o, RtfText.ALIGNMENT);
            }

            header.write (name + ";");
            header.writeGroupMark (false);
        }
        header.writeGroupMark (false);
    }

    /**
     * Gets the rtf style reference from the table.
     * @param name Name of Style
     * @return Rtf attribute of the style reference
     */
    private String getRtfStyleReference (String name)
    {
        return "cs" + styles.get (name).toString ();
    }
}