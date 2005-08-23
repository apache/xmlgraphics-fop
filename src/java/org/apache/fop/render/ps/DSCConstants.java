/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
 
package org.apache.fop.render.ps;

/**
 * This class defines constants with Strings for the DSC specification.
 * 
 * @author <a href="mailto:fop-dev@xmlgraphics.apache.org">Apache FOP Development Team</a>
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
    /**
     * This comment indicates all types of paper media (paper sizes, weight, color)
     * this document requires. 
     */
    public static final String DOCUMENT_MEDIA              = "DocumentMedia";
    /** This comment provides a list of resources the document needs */
    public static final String DOCUMENT_NEEDED_RESOURCES   = "DocumentNeededResources";
    /** This comment provides a list of resources the document includes */
    public static final String DOCUMENT_SUPPLIED_RESOURCES = "DocumentSuppliedResources";
    //Skipping %%DocumentPrinterRequired
    //Skipping %%DocumentNeededFiles -> deprecated
    //Skipping %%DocumentSuppliedFiles -> deprecated
    //Skipping %%DocumentFonts -> deprecated
    //Skipping %%DocumentNeededFonts -> deprecated
    //Skipping %%DocumentSuppliedFonts -> deprecated
    //Skipping %%DocumentNeededProcSets -> deprecated
    //Skipping %%DocumentSuppliedProcSets -> deprecated
    //Skipping %%OperatorIntervention
    //Skipping %%OperatorMessage
    //Skipping %%ProofMode
    /**
     * This comment describes document requirements, such as duplex printing,
     * hole punching, collating, or other physical document processing needs.
     */
    public static final String REQUIREMENTS = "Requirements";
    //Skipping %%VMlocation
    //Skipping %%VMusage
    
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
    
    // ----==== Requirement Page Comments ====----
    
    //Skipping %%PageFonts -> deprecated
    //Skipping %%PageFiles -> deprecated
    /** Indicates that the paper attributes denoted by medianame are invoked on this page. */
    public static final String PAGE_MEDIA        = "PageMedia";
    /**
     * This is the page-level invocation of a combination of the options listed in
     * the %%Requirements: comment.
     */
    public static final String PAGE_REQUIREMENTS = "PageRequirements";
    /**
     * This comment indicates the names and values of all resources that are needed
     * or supplied on the present page.
     */
    public static final String PAGE_RESOURCES    = "PageResources";

}
