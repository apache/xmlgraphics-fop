/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.FONode;

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
     */
    protected void addCharacters(char data[], int start, int end) {
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

}
