/*
 * $Id: DSCConstants.java,v 1.2 2003/03/07 09:46:30 jeremias Exp $
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
package org.apache.fop.render.ps;

/**
 * This class defines constants with Strings for the DSC specification.
 * 
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: DSCConstants.java,v 1.2 2003/03/07 09:46:30 jeremias Exp $
 */
public class DSCConstants {

    // ----==== General Header Comments ====----
    
    /** Lead-in for a DSC-conformant PostScript file */
    public static final String PS_ADOBE_30       = "%!PS-Adobe-3.0";
    
    /** Lead-in for an EPS file */
    public static final String EPSF_30           = "EPSF-3.0";
    
    /** Bounding box for the document */
    public static final String BBOX              = "BoundingBox";
    /** High-resolution bounding box for the document */
    public static final String HIRES_BBOX        = "HiResBoundingBox";
    /** Copyright information associated with the document or resource */
    public static final String COPYRIGHT         = "Copyright";
    /** Creator of the document */
    public static final String CREATOR           = "Creator";
    /** Date and time when the document was created */
    public static final String CREATION_DATE     = "CreationDate";
    /** Type of data */
    public static final String DOCUMENT_DATA     = "BoundingBox";
    /** Use for inidicating an emulator being invoked in the document */
    public static final String EMULATION         = "Emulation";
    /** Explicit end of comments */
    public static final String END_COMMENTS      = "EndComments";
    /** Required PostScript Level 1 extension for this document */
    public static final String EXTENSIONS        = "Extensions";
    /** Indicates who is this document printed for */
    public static final String FOR               = "For";
    /** Indicates the PostScript language level used in the document */
    public static final String LANGUAGE_LEVEL    = "LanguageLevel";
    /** Indicates the orientation of the document */
    public static final String ORIENTATION       = "Orientation";
    /** Number of pages in the document */
    public static final String PAGES             = "Pages";
    /** Indicates the order of the pages */
    public static final String PAGE_ORDER        = "PageOrder";
    /** Indicates how the document should be routed back to its owner */
    public static final String ROUTING           = "Routing";
    /** Title of the document */
    public static final String TITLE             = "Title";
    /** Version of the document */
    public static final String VERSION           = "Version";
 
    // ----==== General Body Comments ====----
    
    /** Indicates a continued line */
    public static final String NEXT_LINE         = "+ ";
    
    //Skipping BeginBinary/EndBinary. They are deprecated.
    
    /** Indicates the start of a data section*/
    public static final String BEGIN_DATA        = "BeginData";
    /** Indicates the end of a data section*/
    public static final String END_DATA          = "EndData";
    
    /** Indicates the start of the defaults section */
    public static final String BEGIN_DEFAULTS    = "BeginDefaults";
    /** Indicates the end of the defaults section */
    public static final String END_DEFAULTS      = "EndDefaults";
    
    /** Indicates the start of a non-PostScript section */
    public static final String BEGIN_EMULATION   = "BeginEmulation";
    /** Indicates the end of a non-PostScript section */
    public static final String END_EMULATION     = "EndEmulation";
    
    /** Indicates the start of a preview section (EPS only)*/
    public static final String BEGIN_PREVIEW     = "BeginPreview";
    /** Indicates the end of a preview section (EPS only)*/
    public static final String END_PREVIEW       = "EndPreview";
    
    /** Indicates the start of the prolog */
    public static final String BEGIN_PROLOG      = "BeginProlog";
    /** Indicates the end of the prolog */
    public static final String END_PROLOG        = "EndProlog";
    
    /** Indicates the start of the document setup */
    public static final String BEGIN_SETUP       = "BeginSetup";
    /** Indicates the end of the document setup */
    public static final String END_SETUP         = "EndSetup";


    // ----==== General Page Comments ====----
    
    /** Indicates the start of a graphic object */
    public static final String BEGIN_OBJECT      = "BeginObject";
    /** Indicates the end of a graphic object */
    public static final String END_OBJECT        = "EndObject";

    /** Indicates the start of the page setup section */
    public static final String BEGIN_PAGE_SETUP  = "BeginPageSetup";
    /** Indicates the end of the page setup section */
    public static final String END_PAGE_SETUP    = "EndPageSetup";

    /** Indicates a page number */
    public static final String PAGE              = "Page";
    /** Bounding box for a page */
    public static final String PAGE_BBOX         = "PageBoundingBox";
    /** High-resolution bounding box for a page */
    public static final String PAGE_HIRES_BBOX   = "PageHiResBoundingBox";
    /** Bounding box for a page */
    public static final String PAGE_ORIENTATION  = "PageOrientation";

    
    // ----==== General Trailer Comments ====----

    /** Indicates the start of the page trailer */    
    public static final String PAGE_TRAILER     = "PageTrailer";
    /** Indicates the start of the document trailer */    
    public static final String TRAILER          = "Trailer";
    /** Indicates the end of a page (NON-STANDARD!) */    
    public static final String END_PAGE         = "EndPage";
    /** Indicates the end of the document */    
    public static final String EOF              = "EOF";


    // ----==== Requirements Conventions ====----

    /**@todo Add the missing comments */
    
    // ----==== Requirement Body Comments ====----
    
    /** Indicates the start of an embedded document */
    public static final String BEGIN_DOCUMENT   = "BeginDocument";
    /** Indicates the end of an embedded document */
    public static final String END_DOCUMENT     = "EndDocument";
    /** Indicates a referenced embedded document */
    public static final String INCLUDE_DOCUMENT = "IncludeDocument";
    
    /** Indicates the start of a PPD feature */
    public static final String BEGIN_FEATURE    = "BeginFeature";
    /** Indicates the end of a PPD feature */
    public static final String END_FEATURE      = "EndFeature";
    /** Indicates a referenced a PPD feature */
    public static final String INCLUDE_FEATURE  = "IncludeFeature";
    
    //Skipping BeginFile/EndFile/IncludeFile. They are deprecated.
    //Skipping BeginFont/EndFont/IncludeFont. They are deprecated.
    //Skipping BeginProcSet/EndProcSet/IncludeProcSet. They are deprecated.
    
    /** Indicates the start of a resource (font, file, procset) */
    public static final String BEGIN_RESOURCE       = "BeginResource";
    /** Indicates the end of a resource (font, file, procset) */
    public static final String END_RESOURCE         = "EndResource";
    /** Indicates a referenced a resource (font, file, procset) */
    public static final String INCLUDE_RESOURCE     = "IncludeResource";
    
    
}
