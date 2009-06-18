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

package org.apache.fop.render.afp.extensions;

import java.util.HashMap;

import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FONode;


/**
 * AFPElementMapping object provides the ability to extract information
 * from the formatted object that reside in the afp namespace. This is used
 * for custom AFP extensions not supported by the FO schema. Examples include
 * adding overlays or indexing a document using the tag logical element
 * structured field.
 * <p/>
 */
public class AFPElementMapping extends ElementMapping {

    public static final String PAGE = "page";

    public static final String PAGE_GROUP = "page-group";

    public static final String TAG_LOGICAL_ELEMENT = "tag-logical-element";

    public static final String INCLUDE_PAGE_OVERLAY = "include-page-overlay";

    public static final String INCLUDE_PAGE_SEGMENT = "include-page-segment";

    /**
     * The namespace used for AFP extensions
     */
    public static final String NAMESPACE = "http://xmlgraphics.apache.org/fop/extensions/afp";

    /**
     * The usual namespace prefix used for AFP extensions
     */
    public static final String NAMESPACE_PREFIX = "afp";

    /** Main constructor */
    public AFPElementMapping() {
        this.namespaceURI = NAMESPACE;
    }

    /**
     * Private static synchronized method to set up the element and atribute
     * HashMaps, this defines what elements and attributes are extracted.
     */
    protected void initialize() {

        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put(PAGE, new AFPPageSetupMaker());
            // foObjs.put(PAGE_GROUP, new AFPMaker());
            foObjs.put(
                TAG_LOGICAL_ELEMENT,
                new AFPTagLogicalElementMaker());
            foObjs.put(
                INCLUDE_PAGE_SEGMENT,
                new AFPIncludePageSegmentMaker());
            foObjs.put(
                INCLUDE_PAGE_OVERLAY,
                new AFPIncludePageOverlayMaker());
        }

    }

    static class AFPPageSetupMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new AFPPageSetupElement(parent);
        }
    }

    static class AFPIncludePageOverlayMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new AFPElement(parent, INCLUDE_PAGE_OVERLAY);
        }
    }

    static class AFPIncludePageSegmentMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new AFPElement(parent, INCLUDE_PAGE_SEGMENT);
        }
    }

    static class AFPTagLogicalElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new AFPElement(parent, TAG_LOGICAL_ELEMENT);
        }
    }

}
