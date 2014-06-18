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

package org.apache.fop.render.pdf.extensions;

import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FONode;

// CSOFF: LineLengthCheck

/**
 * This class provides the element mapping for the PDF-specific extensions.
 */
public class PDFElementMapping extends ElementMapping {

    /** Namespace for the extension */
    public static final String NAMESPACE = "http://xmlgraphics.apache.org/fop/extensions/pdf";

    /** Main constructor */
    public PDFElementMapping() {
        this.namespaceURI = NAMESPACE;
    }

    /** {@inheritDoc} */
    protected void initialize() {
        if (foObjs == null) {
            foObjs = new java.util.HashMap<String, Maker>();
            // pdf:action
            foObjs.put(PDFDictionaryType.Action.elementName(), new PDFActionElementMaker());
            // pdf:array
            foObjs.put(PDFObjectType.Array.elementName(), new PDFArrayElementMaker());
            // pdf:boolean
            foObjs.put(PDFObjectType.Boolean.elementName(), new PDFCollectionEntryElementMaker(PDFObjectType.Boolean));
            // pdf:catalog
            foObjs.put(PDFDictionaryType.Catalog.elementName(), new PDFCatalogElementMaker());
            // pdf:dictionary
            foObjs.put(PDFDictionaryType.Dictionary.elementName(), new PDFDictionaryElementMaker());
            // pdf:embedded-file
            foObjs.put(PDFEmbeddedFileElement.ELEMENT, new PDFEmbeddedFileElementMaker());
            // pdf:name
            foObjs.put(PDFObjectType.Name.elementName(), new PDFCollectionEntryElementMaker(PDFObjectType.Name));
            // pdf:number
            foObjs.put(PDFObjectType.Number.elementName(), new PDFCollectionEntryElementMaker(PDFObjectType.Number));
            // pdf:navigator
            foObjs.put(PDFDictionaryType.Navigator.elementName(), new PDFNavigatorElementMaker());
            // pdf:layer
            foObjs.put(PDFDictionaryType.Layer.elementName(), new PDFLayerElementMaker());
            // pdf:page
            foObjs.put(PDFDictionaryType.Page.elementName(), new PDFPageElementMaker());
            // pdf:reference
            foObjs.put(PDFObjectType.Reference.elementName(), new PDFReferenceElementMaker());
            // pdf:string
            foObjs.put(PDFObjectType.String.elementName(), new PDFCollectionEntryElementMaker(PDFObjectType.String));
            // pdf:info
            foObjs.put(PDFDictionaryType.Info.elementName(), new PDFDocumentInformationElementMaker());
        }
    }

    static class PDFActionElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFActionElement(parent);
        }
    }

    static class PDFArrayElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFArrayElement(parent);
        }
    }

    static class PDFCatalogElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFCatalogElement(parent);
        }
    }

    static class PDFDocumentInformationElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFDocumentInformationElement(parent);
        }
    }

    static class PDFDictionaryElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFDictionaryElement(parent, PDFDictionaryType.Dictionary);
        }
    }

    static class PDFEmbeddedFileElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFEmbeddedFileElement(parent);
        }
    }

    static class PDFLayerElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFLayerElement(parent);
        }
    }

    static class PDFNavigatorElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFNavigatorElement(parent);
        }
    }

    static class PDFPageElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFPageElement(parent);
        }
    }

    static class PDFCollectionEntryElementMaker extends ElementMapping.Maker {
        private PDFObjectType entryType;
        PDFCollectionEntryElementMaker(PDFObjectType entryType) {
            this.entryType = entryType;
        }
        public FONode make(FONode parent) {
            return new PDFCollectionEntryElement(parent, entryType);
        }
    }

    static class PDFReferenceElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PDFReferenceElement(parent);
        }
    }

}
