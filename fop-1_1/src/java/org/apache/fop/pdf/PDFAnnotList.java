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

// Java
import java.util.List;

/**
 * class representing an object which is a list of annotations.
 *
 * This PDF object is a list of references to /Annot objects. So far we
 * are dealing only with links.
 */
public class PDFAnnotList extends PDFObject {

    /**
     * the /Annot objects
     */
    private List links = new java.util.Vector();

    /**
     * add an /Annot object of /Subtype /Link.
     *
     * @param link the PDFLink to add.
     */
    public void addAnnot(PDFObject link) {
        this.links.add(link);
    }

    /**
     * get the count of /Annot objects
     *
     * @return the number of links
     */
    public int getCount() {
        return this.links.size();
    }

    /**
     * {@inheritDoc}
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(128);
        p.append("[\n");
        for (int i = 0; i < getCount(); i++) {
            p.append(((PDFObject)links.get(i)).referencePDF());
            p.append("\n");
        }
        p.append("]");
        return p.toString();
    }

    /*
     * example
     * [
     * 19 0 R
     * ]
     */
}
