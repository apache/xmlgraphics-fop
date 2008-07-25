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

package org.apache.fop.fo.extensions;

import java.util.HashMap;
import java.util.Set;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.UnknownXMLObj;
import org.apache.fop.fo.extensions.destination.Destination;

/**
 * Element mapping for FOP's proprietary extension to XSL-FO.
 */
public class ExtensionElementMapping extends ElementMapping {

    /** The FOP extension namespace URI */
    public static final String URI = "http://xmlgraphics.apache.org/fop/extensions";

    private static final Set propertyAttributes = new java.util.HashSet();

    static {
        //These are FOP's standard extension properties (fox:*)
        propertyAttributes.add("block-progression-unit");
        propertyAttributes.add("widow-content-limit");
        propertyAttributes.add("orphan-content-limit");
        propertyAttributes.add("internal-destination");
    }

    /**
     * Constructor.
     */
    public ExtensionElementMapping() {
        namespaceURI = URI;
    }

    /**
     * Initialize the data structures.
     */
    protected void initialize() {
        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("outline", new UnknownXMLObj.Maker(URI));
            foObjs.put("label", new UnknownXMLObj.Maker(URI));
            foObjs.put("destination", new DestinationMaker());
            foObjs.put("external-document", new ExternalDocumentMaker());
        }
    }

    static class DestinationMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Destination(parent);
        }
    }

    static class ExternalDocumentMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new ExternalDocument(parent);
        }
    }

    /** {@inheritDoc} */
    public String getStandardPrefix() {
        return "fox";
    }

    /** {@inheritDoc} */
    public boolean isAttributeProperty(QName attributeName) {
        if (!URI.equals(attributeName.getNamespaceURI())) {
            throw new IllegalArgumentException("The namespace URIs don't match");
        }
        return propertyAttributes.contains(attributeName.getLocalName());
    }

}
