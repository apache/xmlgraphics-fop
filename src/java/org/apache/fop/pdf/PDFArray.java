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

import java.util.Collection;
import java.util.List;

/**
 * Class representing an array object.
 */
public class PDFArray extends PDFObject {
    /**
     * List holding the values of this array
     */
    protected List values = new java.util.ArrayList();

    /**
     * Create a new, empty array object
     */
    public PDFArray() {
        /* generic creation of PDF object */
        super();
    }

    /**
     * Create the array object
     *
     * @param values the actual array wrapped by this object
     */
    public PDFArray(int[] values) {
        /* generic creation of PDF object */
        super();

        for (int i = 0, c = values.length; i < c; i++) {
            this.values.add(new Integer(values[i]));
        }
    }

    /**
     * Create the array object
     *
     * @param values the actual values wrapped by this object
     */
    public PDFArray(Collection values) {
        /* generic creation of PDF object */
        super();
        
        this.values.addAll(values);
    }
    
    /**
     * Create the array object
     *
     * @param values the actual array wrapped by this object
     */
    public PDFArray(Object[] values) {
        /* generic creation of PDF object */
        super();
        
        for (int i = 0, c = values.length; i < c; i++) {
            this.values.add(values[i]);
        }
    }
    
    /**
     * Returns the length of the array
     * @return the length of the array
     */
    public int length() {
        return this.values.size();
    }
    
    /**
     * Sets an entry at a given location.
     * @param index the index of the value to set
     * @param obj the new value
     */
    public void set(int index, Object obj) {
        this.values.set(index, obj);
    }
    
    /**
     * Gets an entry at a given location.
     * @param index the index of the value to set
     * @return the requested value
     */
    public Object get(int index) {
        return this.values.get(index);
    }
    
    /**
     * Adds a new value to the array.
     * @param obj the value
     */
    public void add(Object obj) {
        this.values.add(obj);
    }
    
    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(64);
        if (hasObjectNumber()) {
            p.append(getObjectID());
        }
        p.append("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                p.append(" ");
            }
            Object obj = this.values.get(i);
            formatObject(obj, p);
        }
        p.append("]");
        if (hasObjectNumber()) {
            p.append("\nendobj\n");
        }
        return p.toString();
    }

}
