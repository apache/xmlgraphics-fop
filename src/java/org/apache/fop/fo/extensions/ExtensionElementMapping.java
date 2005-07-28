/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.fop.fo.extensions;

import org.apache.fop.fo.ElementMapping;

import java.util.HashMap;

/**
 * Element mapping for the pdf bookmark extension.
 * This sets up the mapping for the classes that handle the
 * pdf bookmark extension.
 */
public class ExtensionElementMapping extends ElementMapping {
    public static String URI = "http://xml.apache.org/fop/extensions";

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
        }
    }
}
