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

/* $Id: PDFArray.java 588547 2007-10-26 07:48:14Z jeremias $ */
 
package org.apache.fop.pdf;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

/**
 * Class representing an "Nums" array object (for Number Trees).
 */
public class PDFNumsArray extends PDFObject {
    
    /** Sorted Map holding the values of this array. */
    protected SortedMap map = new java.util.TreeMap();

    /**
     * Create a new, empty array object
     */
    public PDFNumsArray() {
        /* generic creation of PDF object */
        super();
    }

    /**
     * Returns the length of the array
     * @return the length of the array
     */
    public int length() {
        return this.map.size();
    }
    
    /**
     * Sets an entry.
     * @param key the key of the value to set
     * @param obj the new value
     */
    public void put(int key, Object obj) {
        this.map.put(new Integer(key), obj);
    }
    
    /**
     * Gets an entry.
     * @param key the key of requested value
     * @return the requested value
     */
    public Object get(int key) {
        return this.map.get(new Integer(key));
    }
    
    /** {@inheritDoc} */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(64);
        if (hasObjectNumber()) {
            p.append(getObjectID());
        }
        p.append("[");
        boolean first = true;
        Iterator iter = this.map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (!first) {
                p.append(" ");
            }
            first = false;
            formatObject(entry.getKey(), p);
            p.append(" ");
            formatObject(entry.getValue(), p);
        }
        p.append("]");
        if (hasObjectNumber()) {
            p.append("\nendobj\n");
        }
        return p.toString();
    }

}
