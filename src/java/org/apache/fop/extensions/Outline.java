/*
 * $Id: Outline.java,v 1.10 2003/03/05 20:40:18 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.extensions;

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
     * The attribues on the outline object are the internal and external
     * destination. One of these is required.
     *
     * @param attlist the attribute list
     * @throws FOPException a fop exception if there is an error
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
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
     * Get the bookmark data for this outline.
     * This creates a bookmark data with the destination
     * and adds all the data from child outlines.
     *
     * @return the new bookmark data
     */
    public BookmarkData getData() {
        BookmarkData data = new BookmarkData(internalDestination);
        data.setLabel(getLabel());
        for (int count = 0; count < outlines.size(); count++) {
            Outline out = (Outline)outlines.get(count);
            data.addSubData(out.getData());
        }
        return data;
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
        fotv.serveVisitor(this);
    }

}

