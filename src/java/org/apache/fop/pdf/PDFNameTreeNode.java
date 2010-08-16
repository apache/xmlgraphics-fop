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
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class representing a PDF name tree node.
 */
public class PDFNameTreeNode extends PDFDictionary {

    private static final String KIDS = "Kids";
    private static final String NAMES = "Names";
    private static final String LIMITS = "Limits";

    /**
     * create a named destination
     */
    public PDFNameTreeNode() {
        /* generic creation of PDF object */
        super();
    }

    /**
     * Sets the Kids array.
     * @param kids the Kids array
     */
    public void setKids(PDFArray kids) {
        put(KIDS, kids);
    }

    /**
     * Returns the Kids array.
     * @return the Kids array
     */
    public PDFArray getKids() {
        return (PDFArray)get(KIDS);
    }

    /**
     * Sets the Names array.
     * @param names the Names array
     */
    public void setNames(PDFArray names) {
        put(NAMES, names);
    }

    /**
     * Returns the Names array.
     * @return the Names array
     */
    public PDFArray getNames() {
        return (PDFArray)get(NAMES);
    }

    /**
     * Sets the lower limit value of the Limits array.
     * @param key the lower limit value
     */
    public void setLowerLimit(String key) {
        PDFArray limits = prepareLimitsArray();
        limits.set(0, key);
    }

    /**
     * Returns the lower limit value of the Limits array.
     * @return the lower limit value
     */
    public String getLowerLimit() {
        PDFArray limits = prepareLimitsArray();
        return (String)limits.get(0);
    }

    /**
     * Sets the upper limit value of the Limits array.
     * @param key the upper limit value
     */
    public void setUpperLimit(String key) {
        PDFArray limits = prepareLimitsArray();
        limits.set(1, key);
    }

    /**
     * Returns the upper limit value of the Limits array.
     * @return the upper limit value
     */
    public String getUpperLimit() {
        PDFArray limits = prepareLimitsArray();
        return (String)limits.get(1);
    }

    private PDFArray prepareLimitsArray() {
        PDFArray limits = (PDFArray)get(LIMITS);
        if (limits == null) {
            limits = new PDFArray(this, new Object[2]);
            put(LIMITS, limits);
        }
        if (limits.length() != 2) {
            throw new IllegalStateException("Limits array must have 2 entries");
        }
        return limits;
    }

    /** {@inheritDoc} */
    protected void writeDictionary(OutputStream out, Writer writer) throws IOException {
        sortNames(); //Sort the names before writing them out
        super.writeDictionary(out, writer);
    }

    private void sortNames() {
        PDFArray names = getNames();
        SortedMap map = new TreeMap();
        int i = 0;
        int c = names.length();
        while (i < c) {
            String key = (String)names.get(i++); //Key must be a String
            Object value = names.get(i++);
            map.put(key, value);
        }
        names.clear();
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            names.add(entry.getKey());
            names.add(entry.getValue());
        }
    }
}

