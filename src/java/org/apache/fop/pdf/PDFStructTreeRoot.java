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

package org.apache.fop.pdf;

/**
 * Class representing a PDF /StructTreeRoot dictionary.
 */
public class PDFStructTreeRoot extends PDFDictionary {

    /**
     * Create the /StructTreeRoot dictionary.
     */
    public PDFStructTreeRoot() {
        super();
        put("Type", new PDFName("StructTreeRoot"));
        put("K", new PDFArray());
    }

    /**
     * Add parentTree entry.
     * @param parentTree to be added
     */
    public void addParentTree(PDFParentTree parentTree) {
        put("ParentTree", parentTree);
    }

    /**
     * Get the kids.
     * @return the kids
     */
    public PDFArray getKids() {
        return (PDFArray)get("K");
    }

    /**
     * Returns the first child of the kids array (normally the structure tree root element)
     * @return the first child
     */
    public PDFObject getFirstChild() {
        return (PDFObject)getKids().get(0);
    }

    /**
     * Adds a kid.
     * @param kid to be added
     */
    public void addKid(PDFObject kid) {
        getKids().add(kid);
    }
}