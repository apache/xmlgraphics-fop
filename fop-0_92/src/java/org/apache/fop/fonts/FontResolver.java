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

package org.apache.fop.fonts;

import javax.xml.transform.Source;

/**
 * This interface is used to resolve absolute and relative font URIs.
 */
public interface FontResolver {

    /**
     * Called to resolve an URI to a Source instance. The base URI needed by the URIResolver's
     * resolve() method is defined to be implicitely available in this case. If the URI cannot
     * be resolved, null is returned and it is assumed that the FontResolver implementation
     * already warned the user about the problem.
     * @param href An href attribute, which may be relative or absolute.
     * @return A Source object, or null if the href could not resolved.
     */
    Source resolve(String href);
    
}
