/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

/**
 * Element mapping for the old FOP extension namespace. It is simply mapped to the new namespace.
 */
public class OldExtensionElementMapping extends ExtensionElementMapping {
    
    /** The old FOP extension namespace URI (FOP 0.20.5 and earlier) */
    public static final String URI = "http://xml.apache.org/fop/extensions";

    /**
     * Constructor.
     */
    public OldExtensionElementMapping() {
        namespaceURI = URI;
    }

}
