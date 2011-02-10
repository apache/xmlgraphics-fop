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

package org.apache.fop.render.rtf.rtflib.tools;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

/** Here will be defined all supported image formats.
 *  This class belongs to the <fo:external-graphic> tag processing.
 *  @author a.putz@skynamics.com (Andreas Putz)
 */

public final class ImageConstants {
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
    /** Integer equivalent for BMP */
    public static final int I_BMP = 3;

    /** Integer equivalent for GIF */
    public static final int I_GIF = 50;
    /** Integer equivalent for JPEG C (??) */
    public static final int I_JPG_C = 51;

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

    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Private constructor.
     */
    private ImageConstants() {
    }
}
