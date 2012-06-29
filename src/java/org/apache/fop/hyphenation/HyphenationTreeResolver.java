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

package org.apache.fop.hyphenation;

import javax.xml.transform.Source;

/**
 * <p>This interface is used to resolve relative URIs pointing to hyphenation tree files.</p>
 */
public interface HyphenationTreeResolver {

    /**
     * Called to resolve an URI to a Source instance. The base URI needed by the URIResolver's
     * resolve() method is defined to be implicitely available in this case. If the URI cannot
     * be resolved, null is returned.
     * @param href An href attribute, which may be relative or absolute.
     * @return A Source object, or null if the href could not resolved.
     */
    Source resolve(String href);

}
