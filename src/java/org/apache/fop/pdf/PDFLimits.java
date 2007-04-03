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

/* $Id: PDFLink.java 426576 2006-07-28 15:44:37Z jeremias $ */

package org.apache.fop.pdf;

import java.util.ArrayList;

import org.apache.fop.pdf.PDFDestination;

/**
 * class representing a Limits object (part of the names dictionary for named destinations)
 */
public class PDFLimits extends PDFObject {

    private ArrayList destinationList;

    /**
     * create a named destination
     */
    public PDFLimits(ArrayList destinationList) {
        /* generic creation of PDF object */
        super();
        this.destinationList = destinationList;
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        String[] idRefs = new String[destinationList.size()];
        String kidsString = "";
        for (int i = 0; i < destinationList.size(); i++) {
            PDFDestination dest = (PDFDestination)destinationList.get(i);
            idRefs[i] = dest.getIDRef();
            kidsString += dest.referencePDF();
            if (!(i == destinationList.size() - 1)) {
                kidsString += " ";
            }
        }
        String s = getObjectID()
                   + "<<\n"
                   + "/Limits [(" + idRefs[0] + ") (" + idRefs[destinationList.size() - 1] + ")]\n"
                   + "/Kids [" + kidsString + "]"
                   + "\n>>\nendobj\n";
        return s;
    }

    /*
     * example:
     *
     * 260 0 obj
     * <<
     * /Limits [(Annotate) (thumbnails)]
     * /Kids [248 0 R 253 0 R 254 0 R 259 0 R]
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

        if (obj == null || !(obj instanceof PDFLimits)) {
            return false;
        }

        return true;
    }
}

