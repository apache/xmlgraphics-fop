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

package org.apache.fop.rtf.rtflib.tools;

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
    public static int I_NOT_SUPPORTED = -1;

    public static int I_EMF = 0;
    public static int I_PNG = 1;
    public static int I_JPG = 2;

    /** Defines the RTF properties */
    public static String [] RTF_TAGS = new String []
        {
            "emfblip", "pngblip", "jpegblip"
        };

    public static int I_TO_CONVERT_BASIS = 50;
    public static int I_GIF = 50;
    public static int I_JPG_C = 51;

    /** Defines the types for converting rtf supported image types */
    public static int [] CONVERT_TO = new int []
        {
            I_JPG, I_JPG
        };

    /** EMF file extension */
    public static String EMF_EXT = "emf";
    /** PNG file extension */
    public static String PNG_EXT = "png";
    /** JPG file extension */
    public static String JPG_EXT = "jpg";
    /** JPEG file extension */
    public static String JPEG_EXT = "jpeg";
    /** GIF file extension */
    public static String GIF_EXT = "gif";

    /** Defines the file extensions and the RTF property belongs to */
    public static Hashtable SUPPORTED_IMAGE_TYPES = new Hashtable ();
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
