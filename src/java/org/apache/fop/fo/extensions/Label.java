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

import org.xml.sax.Locator;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;


/**
 * Labal for PDF bookmark extension.
 * This element contains the label for the bookmark object.
 */
public class Label extends ExtensionObj {
    private String label = "";

    /**
     * Create a new label object.
     *
     * @param parent the fo node parent
     */
    public Label(FONode parent) {
        super(parent);
    }

    /**
     * Add the characters to this label.
     * The text data inside the label xml element is used for the label string.
     *
     * @param data the character data
     * @param start the start position in the data array
     * @param end the end position in the character array
     * @param locator location in fo source file.
     */
    protected void addCharacters(char data[], int start, int end,
                                 Locator locator) {
        label += new String(data, start, end - start);
    }

    /**
     * Get the string for this label.
     *
     * @return the label string
     */
    public String toString() {
        return label;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveLabel(this);
    }

}
