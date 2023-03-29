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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.io.output.CountingOutputStream;

/**
 * Class representing an "Nums" array object (for Number Trees).
 */
public class PDFNumsArray extends PDFObject {

    /** Sorted Map holding the values of this array. */
    protected SortedMap<Integer, Object> map = new java.util.TreeMap<Integer, Object>();

    /**
     * Create a new, empty array object.
     * @param parent the object's parent if any
     */
    public PDFNumsArray(PDFObject parent) {
        super(parent);
    }

    /**
     * Returns the length of the array
     * @return the length of the array
     */
    public int length() {
        return this.map.size();
    }

    /**
     * Determines whether a value object should be converted to an indirect reference for inclusion in a Number Tree
     * array according to the PDF spec.
     * PDF1.0 - 1.2 - Spec is silent on this subject (as Number Trees don't exist).
     * PDF1.3 & 1.4 - Values must be indirect object refs.
     * PDF1.5       - Recommended: stream, dictionary, array, and string values be indirect object refs.
     * PDF1.6 - 2.0 - Stream values must be indirect object refs.
     *                Recommended: dictionary, array, and string values be indirect object refs.
     * Method signals for values that must be, and those recommended to be indirect object refs.
     * @param obj The object to be considered.
     * @return True iff the object should be converted.
     */
    private boolean shouldConvertToRef(PDFObject obj) {
        boolean retval = false;
        if (getDocument() != null && getDocument().getPDFVersion() != null) {
            switch (getDocument().getPDFVersion()) {
                case V1_0: // fall-through
                case V1_1: // fall-through
                case V1_2:
                    log.error("Number Tree used in PDF version " + getDocument().getPDFVersion());
                    break;
                case V1_3: // fall-through
                case V1_4:
                    retval = true;
                    break;
                case V1_5: // fall-through
                case V1_6: // fall-through
                case V1_7: // fall-through
                case V2_0:
                    if (obj instanceof PDFStream
                            || obj instanceof PDFDictionary
                            || obj instanceof PDFArray
                            || obj instanceof PDFText) {
                        retval = true;
                    }
                    break;
                default:
                    log.error("Unrecognised PDF version " + getDocument().getPDFVersion());
                    break;
            }
        }
        return retval;
    }

    /**
     * This method provides conformance with the different PDF specs which require or recommend different types be used
     * for Number Tree array values. Method indirects objects where indicated.
     * @param obj The object to be considered for indirection.
     * @return Either the object or a reference to the object.
     */
    private Object indirectIfReq(Object obj) {
        PDFDocument doc = getDocument();
        Object retval = obj;
        if (obj instanceof PDFObject) {
            PDFObject pdfObj = (PDFObject) obj;
            PDFObject parent = pdfObj.getParent();
            if (shouldConvertToRef(pdfObj)) {
                if (!pdfObj.hasObjectNumber()) { // Needs registering with the doc.
                    pdfObj.setParent(null); // Can't register if it has a parent.
                    pdfObj = doc.registerObject(pdfObj);
                    if (parent != null) {
                        pdfObj.setParent(parent); // Reinstate original parent.
                    }
                }
                retval = pdfObj.makeReference();
            }
        }
        return retval;
    }

    /**
     * Sets an entry.
     * @param key the key of the value to set
     * @param obj the new value
     */
    public void put(Integer key, Object obj) {
        this.map.put(key, indirectIfReq(obj));
    }

    /**
     * Sets an entry.
     * @param key the key of the value to set
     * @param obj the new value
     */
    public void put(int key, Object obj) {
        put(Integer.valueOf(key), obj);
    }

    /**
     * Gets an entry.
     * @param key the key of requested value
     * @return the requested value
     */
    public Object get(Integer key) {
        return this.map.get(key);
    }

    /**
     * Gets an entry.
     * @param key the key of requested value
     * @return the requested value
     */
    public Object get(int key) {
        return get(Integer.valueOf(key));
    }

    /** {@inheritDoc} */
    @Override
    public int output(OutputStream stream) throws IOException {
        CountingOutputStream cout = new CountingOutputStream(stream);
        StringBuilder textBuffer = new StringBuilder(64);
        textBuffer.append('[');
        boolean first = true;
        for (Map.Entry<Integer, Object> entry : this.map.entrySet()) {
            if (!first) {
                textBuffer.append(" ");
            }
            first = false;
            formatObject(entry.getKey(), cout, textBuffer);
            textBuffer.append(" ");
            formatObject(entry.getValue(), cout, textBuffer);
        }
        textBuffer.append(']');
        PDFDocument.flushTextBuffer(textBuffer, cout);
        return cout.getCount();
    }

}
