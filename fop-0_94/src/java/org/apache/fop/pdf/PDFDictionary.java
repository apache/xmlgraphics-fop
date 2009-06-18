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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class representing a PDF dictionary object
 */
public class PDFDictionary extends PDFObject {
    
    /**
     * the entry map
     */
    protected Map entries = new java.util.HashMap();

    /**
     * maintains the order of the entries added to the entry map. Whenever you modify
     * "entries", always make sure you adjust this list accordingly.
     */
    protected List order = new java.util.ArrayList();
    
    /**
     * Create the dictionary object
     */
    public PDFDictionary() {
        /* generic creation of PDF object */
        super();
    }

    /**
     * Puts a new name/value pair.
     * @param name the name
     * @param value the value
     */
    public void put(String name, Object value) {
        if (!entries.containsKey(name)) {
            this.order.add(name);
        }
        this.entries.put(name, value);
    }
    
    /**
     * Returns the value given a name.
     * @param name the name of the value
     * @return the value or null, if there's no value with the given name.
     */
    public Object get(String name) {
        return this.entries.get(name);
    }
    
    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(64);
        if (hasObjectNumber()) {
            p.append(getObjectID());
        }
        p.append("<<");
        Iterator iter = this.order.iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            p.append("\n  /");
            p.append(key);
            p.append(" ");
            Object obj = this.entries.get(key);
            formatObject(obj, p);
        }
        p.append("\n>>\n");
        if (hasObjectNumber()) {
            p.append("endobj\n");
        }
        return p.toString();
    }

}
