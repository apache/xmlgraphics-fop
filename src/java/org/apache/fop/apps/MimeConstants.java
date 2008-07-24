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

package org.apache.fop.apps;

/**
 * Frequently used MIME types for various file formats used when working with Apache FOP.
 */
public interface MimeConstants {

    /** Portable Document Format */
    String MIME_PDF             = "application/pdf";

    /** PostScript */
    String MIME_POSTSCRIPT      = "application/postscript";
    /** Encapsulated PostScript (same MIME type as PostScript) */
    String MIME_EPS             = MIME_POSTSCRIPT;

    /** HP's PCL */
    String MIME_PCL             = "application/x-pcl";
    /** HP's PCL (alternative MIME type) */
    String MIME_PCL_ALT         = "application/vnd.hp-PCL";

    /** IBM's AFP */
    String MIME_AFP             = "application/x-afp";
    /** IBM's AFP (alternative MIME type) */
    String MIME_AFP_ALT         = "application/vnd.ibm.modcap";

    /** Plain text */
    String MIME_PLAIN_TEXT      = "text/plain";

    /** Rich text format */
    String MIME_RTF             = "application/rtf";
    /** Rich text format (alternative 1) */
    String MIME_RTF_ALT1        = "text/richtext";
    /** Rich text format (alternative 2) */
    String MIME_RTF_ALT2        = "text/rtf";

    /** FrameMaker's MIF */
    String MIME_MIF             = "application/mif";

    /** Scalable Vector Graphics */
    String MIME_SVG             = "image/svg+xml";

    /** GIF images */
    String MIME_GIF             = "image/gif";
    /** PNG images */
    String MIME_PNG             = "image/png";
    /** JPEG images */
    String MIME_JPEG            = "image/jpeg";
    /** TIFF images */
    String MIME_TIFF            = "image/tiff";

    /** Apache FOP's AWT preview (non-standard MIME type) */
    String MIME_FOP_AWT_PREVIEW = "application/X-fop-awt-preview";
    /** Apache FOP's Direct Printing (non-standard MIME type) */
    String MIME_FOP_PRINT       = "application/X-fop-print";
    /** Apache FOP's area tree XML */
    String MIME_FOP_AREA_TREE   = "application/X-fop-areatree";

    /** Proposed but non-registered MIME type for XSL-FO */
    String MIME_XSL_FO          = "text/xsl";

}
