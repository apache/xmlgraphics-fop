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
     *
     * @return the string with the dictionary values
     */
    public String getDictionary() {
        StringBuffer sb = new StringBuffer();
        sb.append("/Type /Trans\n");
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

