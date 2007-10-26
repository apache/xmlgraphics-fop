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
 * The PDF resource context.
 *
 * There is one of these for every page in a PDF document. The object
 * specifies the dimensions of the page and references a /Resources
 * object, a contents stream and the page's parent in the page
 * hierarchy.
 *
 * Modified by Mark Lillywhite, mark-fop@inomial.com. The Parent
 * object was being referred to by reference, but all that we
 * ever used from the Parent was its PDF object ID, and according
 * to the memory profile this was causing OOM issues. So, we store
 * only the object ID of the parent, rather than the parent itself.
 */
public class PDFResourceContext extends PDFObject {

    /**
     * the page's /Resource object
     */
    protected PDFResources resources;

    /**
     * the list of annotation objects for this page
     */
    protected PDFAnnotList annotList;

    /**
     * Creates a new ResourceContext.
     * @param resources the /Resources object
     */
    public PDFResourceContext(PDFResources resources) {
        /* generic creation of object */
        super();

        /* set fields using parameters */
        //this.document = doc;
        this.resources = resources;
        this.annotList = null;
    }

    /**
     * Get the resources for this resource context.
     *
     * @return the resources in this resource context
     */
    public PDFResources getPDFResources() {
        return this.resources;
    }

    /**
     * set this page's annotation list
     *
     * @param annot a PDFAnnotList list of annotations
     */
    public void addAnnotation(PDFObject annot) {
        if (this.annotList == null) {
            this.annotList = getDocument().getFactory().makeAnnotList();
        }
        this.annotList.addAnnot(annot);
    }

    /**
     * Get the current annotations.
     *
     * @return the current annotation list
     */
    public PDFAnnotList getAnnotations() {
        return this.annotList;
    }

    /**
     * A a GState to this resource context.
     *
     * @param gstate the GState to add
     */
    public void addGState(PDFGState gstate) {
        getPDFResources().addGState(gstate);
    }

    /**
     * Add the shading to the current resource context.
     *
     * @param shading the shading to add
     */
    public void addShading(PDFShading shading) {
        getPDFResources().addShading(shading);
    }

}
