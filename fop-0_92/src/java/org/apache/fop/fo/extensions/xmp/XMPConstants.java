/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.fop.fo.extensions.xmp;

/**
 * Constants used in XMP metadata.
 */
public interface XMPConstants {

    /** Namespace URI for XMP */
    String XMP_NAMESPACE = "adobe:ns:meta/";
    
    /** Namespace URI for RDF */
    String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** Namespace URI for Dublin Core */ 
    String DUBLIN_CORE_NAMESPACE = "http://purl.org/dc/elements/1.1/";
    
    /** Namespace URI for the XMP Basic Schema */
    String XMP_BASIC_NAMESPACE = "http://ns.adobe.com/xap/1.0/";
    
    /** Namespace URI for the Adobe PDF Schema */
    String ADOBE_PDF_NAMESPACE = "http://ns.adobe.com/pdf/1.3/";

    /** Namespace URI for the PDF/A Identification Schema */
    String PDF_A_IDENTIFICATION = "http://www.aiim.org/pdfa/ns/id";
    
    /**
     * Namespace URI for the PDF/A Identification Schema
     * (from an older draft of ISO 19005-1, used by Adobe Acrobat)
     */
    String PDF_A_IDENTIFICATION_OLD = "http://www.aiim.org/pdfa/ns/id.html";
    
}
