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

package org.apache.fop.fo.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.apps.FOPException;

import java.util.ArrayList;

import org.xml.sax.Attributes;


/**
 * The outline object for the pdf bookmark extension.
 * The outline element contains a label and optionally more outlines.
 */
public class Outline extends ExtensionObj {
    private Label label;
    private ArrayList outlines = new ArrayList();

    private String internalDestination;
    private String externalDestination;

    /**
     * Create a new outline object.
     *
     * @param parent the parent fo node
     */
    public Outline(FONode parent) {
        super(parent);
    }

    /**
     * The attributes on the outline object are the internal and external
     * destination. One of these is required.
     *
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        internalDestination =
            attlist.getValue("internal-destination");
        externalDestination =
            attlist.getValue("external-destination");
        if (externalDestination != null && !externalDestination.equals("")) {
            getLogger().warn("fox:outline external-destination not supported currently.");
        }

        if (internalDestination == null || internalDestination.equals("")) {
            getLogger().warn("fox:outline requires an internal-destination.");
        }

    }

    /**
     * Add the child to this outline.
     * This checks for the type, label or outline and handles appropriately.
     *
     * @param obj the child object
     */
    protected void addChild(FONode obj) {
        if (obj instanceof Label) {
            label = (Label)obj;
        } else if (obj instanceof Outline) {
            outlines.add(obj);
        }
    }

    /**
     * Get the label string.
     * This gets the label string from the child label element.
     *
     * @return the label string or empty if not found
     */
    public String getLabel() {
        return label == null ? "" : label.toString();
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveOutline(this);
    }

    public String getInternalDestination() {
        return internalDestination;
    }

    public String getExternalDestination() {
        return externalDestination;
    }

    public ArrayList getOutlines() {
        return outlines;
    }

}
