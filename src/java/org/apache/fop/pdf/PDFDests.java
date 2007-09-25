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

import org.apache.fop.area.DestinationData;

/**
 * class representing an /Dests object (part of a name dictionary)
 */
public class PDFDests extends PDFObject {

    private String limitsRef;

    /**
     * create a named destination
     */
    public PDFDests(String limitsRef) {
        /* generic creation of PDF object */
        super();
        this.limitsRef = limitsRef;
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        String s = getObjectID()
                   + "<<\n"
                   + "/Dests " + limitsRef
                   + "\n>>\nendobj\n";
        return s;
    }

    /*
     * example:
     *
     * 262 0 obj
     * <<
     * /Dests 260 0 R
     * >>
     * endobj
     */

    /**
     * Check if this equals another object.
     *
     * @param obj the object to compare
     * @return true if this equals other object
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof PDFDests)) {
            return false;
        }

        return true;
    }
}

