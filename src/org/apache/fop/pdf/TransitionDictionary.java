/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.util.Map;
import java.util.Iterator;

/**
 * Transition Dictionary
 * This class is used to build a transition dictionary to
 * specify the transition between pages.
 */
public class TransitionDictionary extends PDFObject {

    private Map dictionaryValues;

    /**
     * Create a Transition Dictionary
     *
     * @param values the dictionary values to output
     */
    public TransitionDictionary(Map values) {
        dictionaryValues = values;
    }

    /**
     * Get the dictionary.
     * This returns the string containing the dictionary values.
     */
    public String getDictionary() {
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = dictionaryValues.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            sb.append(key + " " + dictionaryValues.get(key) + "\n");
        }
        return sb.toString();
    }

    /**
     * there is nothing to return for the toPDF method, as it should not be called
     *
     * @return an empty string
     */
    public byte[] toPDF() {
        return new byte[0];
    }
}

