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

package org.apache.fop.render.rtf.rtflib.tools;

import java.util.Hashtable;

/** Here will be defined all supported image formats.
 *  This class belongs to the <fo:external-graphic> tag processing.
 *  @author a.putz@skynamics.com (Andreas Putz)
 */

public class ImageConstants {
    //////////////////////////////////////////////////
    // @@ Symbolic constants
    //////////////////////////////////////////////////

    /** Defines the case, if image is not supported */
    public static final int I_NOT_SUPPORTED = -1;
    /** Integer equivalent for EMF */
    public static final int I_EMF = 0;
    /** Integer equivalent for PNG */
    public static final int I_PNG = 1;
    /** Integer equivalent for JPG */
    public static final int I_JPG = 2;

    /** Defines the RTF properties */
    public static final String [] RTF_TAGS = new String []
        {
            "emfblip", "pngblip", "jpegblip"
        };

    /** constant for image conversion basis (??) */
    public static final int I_TO_CONVERT_BASIS = 50;
    /** Integer equivalent for GIF */
    public static final int I_GIF = 50;
    /** Integer equivalent for JPEG C (??) */
    public static final int I_JPG_C = 51;

    /** Defines the types for converting rtf supported image types */
    public static final int [] CONVERT_TO = new int []
        {
            I_JPG, I_JPG
        };

    /** EMF file extension */
    public static final String EMF_EXT = "emf";
    /** PNG file extension */
    public static final String PNG_EXT = "png";
    /** JPG file extension */
    public static final String JPG_EXT = "jpg";
    /** JPEG file extension */
    public static final String JPEG_EXT = "jpeg";
    /** GIF file extension */
    public static final String GIF_EXT = "gif";

    /** Defines the file extensions and the RTF property belongs to */
    public static final Hashtable SUPPORTED_IMAGE_TYPES = new Hashtable ();
    static {
        SUPPORTED_IMAGE_TYPES.put (EMF_EXT, new Integer (I_EMF));
        SUPPORTED_IMAGE_TYPES.put (PNG_EXT, new Integer (I_PNG));
        SUPPORTED_IMAGE_TYPES.put (JPG_EXT, new Integer (I_JPG_C));
        SUPPORTED_IMAGE_TYPES.put (JPEG_EXT, new Integer (I_JPG_C));
        SUPPORTED_IMAGE_TYPES.put (GIF_EXT, new Integer (I_GIF));
    }

    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Private constructor.
     */
    private ImageConstants() {
    }
}
