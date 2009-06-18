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

package org.apache.fop.fo.extensions.xmp;

import java.util.HashMap;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;
import org.apache.xmlgraphics.xmp.XMPConstants;

import org.w3c.dom.DOMImplementation;

/**
 * Setup the element mapping for XMP metadata.
 */
public class RDFElementMapping extends ElementMapping {

    /** Main constructor. */
    public RDFElementMapping() {
        namespaceURI = XMPConstants.RDF_NAMESPACE;
    }

    /** {@inheritDoc} */
    public DOMImplementation getDOMImplementation() {
        return getDefaultDOMImplementation();
    }

    /** {@inheritDoc} */
    protected void initialize() {
        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("RDF", new RDFElementMaker());
        }
    }

    static class RDFElementMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new RDFElement(parent);
        }
    }

}
