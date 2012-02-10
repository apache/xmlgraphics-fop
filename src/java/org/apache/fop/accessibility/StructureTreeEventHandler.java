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

package org.apache.fop.accessibility;

import java.util.Locale;

import org.xml.sax.Attributes;

/**
 * Receive notifications relating to the structure tree of an FO document.
 * A structure tree is a reduced version of the document's FO tree, containing only the logical
 * structure that is used by accessible output formats.
 */
public interface StructureTreeEventHandler {

    /**
     * Starts a page sequence structure tree node.
     *
     * @param locale The locale of the page sequence
     */
    void startPageSequence(Locale locale);

    /**
     * Starts a structure tree node.
     *
     * @param name the name of the structure tree node
     * @param attributes the node properties
     * @return the corresponding structure tree element
     */
    StructureTreeElement startNode(String name, Attributes attributes);

    /**
     * Ends a structure tree node.
     *
     * @param name the name of the structure tree node
     */
    void endNode(String name);

    /**
     * Starts an image node.
     *
     * @param name the name of the structure tree node
     * @param attributes the node properties
     * @return the corresponding structure tree element
     */
    StructureTreeElement startImageNode(String name, Attributes attributes);

    /**
     * Starts a node that can be referenced by other nodes. This is usually a
     * node that can have Marked Content References as children.
     *
     * @param name the name of the structure tree node
     * @param attributes the node properties
     * @return the corresponding structure tree element
     */
    StructureTreeElement startReferencedNode(String name, Attributes attributes);

    /**
     * Ends a page sequence structure tree node.
     */
    void endPageSequence();
}
