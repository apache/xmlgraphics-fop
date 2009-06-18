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

/**
 * class representing an array object
 */
public class PDFArray extends PDFObject {
    /**
     * Array of calues for this pdf object.
     */
    protected int[] values;

    /**
     * create the array object
     *
     * @param values the actual array wrapped by this object
     */
    public PDFArray(int[] values) {

        /* generic creation of PDF object */
        super();

        /* set fields using paramaters */
        this.values = values;
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(64);
        p.append(getObjectID() + "[");
        for (int i = 0; i < values.length; i++) {
            p.append(" ");
            p.append(values[i]);
        }
        p.append("]\nendobj\n");
        return p.toString();
    }

}
