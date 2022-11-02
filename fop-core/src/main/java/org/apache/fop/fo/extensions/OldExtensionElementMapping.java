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

import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.UnknownXMLObj;

/**
 * Element mapping for the old FOP extension namespace.
 */
public class OldExtensionElementMapping extends ElementMapping {

    /** The old FOP extension namespace URI (FOP 0.20.5 and earlier) */
    public static final String URI = "http://xml.apache.org/fop/extensions";

    /**
     * Constructor.
     */
    public OldExtensionElementMapping() {
        namespaceURI = URI;
    }

    /**
     * Initialize the data structures.
     */
    protected void initialize() {
        if (foObjs == null) {
            foObjs = new HashMap<String, Maker>();
            foObjs.put("outline", new UnknownXMLObj.Maker(URI));
            foObjs.put("label", new UnknownXMLObj.Maker(URI));
        }
    }
}
