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

package org.apache.fop.render;

import org.w3c.dom.Document;

/**
 * This interface is implemented by classes that can handle a certain type
 * of foreign objects.
 */
public interface XMLHandler {

    /** Used to indicate that all MIME types or XML namespaces are handled. */
    String HANDLE_ALL = "*";

    /**
     * <p>Handle an external xml document inside a Foreign Object Area.
     * </p>
     * <p>This may throw an exception if for some reason it cannot be handled. The
     * caller is expected to deal with this exception.
     * </p>
     * <p>The implementation may convert the XML document internally to another
     * XML dialect (SVG, for example) and call renderXML() on the AbstractRenderer
     * again (which can be retrieved through the RendererContext).</p>
     *
     * @param context        The RendererContext (contains the user agent)
     * @param doc            A DOM containing the foreign object to be
     *      processed
     * @param ns             The Namespace of the foreign object
     * @exception Exception  If an error occurs during processing.
     */
    void handleXML(RendererContext context,
            Document doc, String ns) throws Exception;

    /**
     * Checks if this XMLHandler supports handling an XML namespace for a particular renderer.
     * @param renderer the renderer for which to check.
     * @return true if this XML handler supports a particular renderer
     */
    boolean supportsRenderer(Renderer renderer);

    /**
     * @return the XML namespace for the XML dialect this XMLHandler supports,
     * null if all XML content is handled by this instance.
     */
    String getNamespace();
}

