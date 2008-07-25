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

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;

import org.w3c.dom.Document;

// cacheable object
/**
 * Foreign object inline area.
 * This inline area represents an instream-foreign object.
 * This holds an xml document and the associated namespace.
 */
public class ForeignObject extends Area {

    private static final long serialVersionUID = -214947698798577885L;

    private Document doc;
    private String namespace;

    /**
     * Create a new foreign object with the given dom and namespace.
     *
     * @param d the xml document
     * @param ns the namespace of the document
     */
    public ForeignObject(Document d, String ns) {
        doc = d;
        namespace = ns;
    }

    /**
     * Create a new empty foreign object for which the DOM Document will be set later.
     *
     * @param ns the namespace of the document
     */
    public ForeignObject(String ns) {
        namespace = ns;
    }

    /**
     * Sets the DOM document for this foreign object.
     * @param document the DOM document
     */
    public void setDocument(Document document) {
        this.doc = document;
    }

    /**
     * Get the document for this foreign object.
     *
     * @return the xml document
     */
    public Document getDocument() {
        return doc;
    }

    /**
     * Get the namespace of this foreign object.
     *
     * @return the namespace of this document
     */
    public String getNameSpace() {
        return namespace;
    }
}

