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

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.FOTreeVisitor;
 import org.xml.sax.Attributes;

/**
 * The retrieve-marker formatting object.
 * This will create a layout manager that will retrieve
 * a marker based on the information.
 */
public class RetrieveMarker extends FObjMixed {

    private String retrieveClassName;
    private int retrievePosition;
    private int retrieveBoundary;

    /**
     * Create a retrieve marker object.
     *
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RetrieveMarker(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        this.retrieveClassName =
            this.propertyList.get(PR_RETRIEVE_CLASS_NAME).getString();
        this.retrievePosition =
            this.propertyList.get(PR_RETRIEVE_POSITION).getEnum();
        this.retrieveBoundary =
            this.propertyList.get(PR_RETRIEVE_BOUNDARY).getEnum();
    }

    public String getRetrieveClassName() {
        return retrieveClassName;
    }

    public int getRetrievePosition() {
        return retrievePosition;
    }

    public int getRetrieveBoundary() {
        return retrieveBoundary;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveRetrieveMarker(this);
    }

    public String getName() {
        return "fo:retrieve-marker";
    }
}
