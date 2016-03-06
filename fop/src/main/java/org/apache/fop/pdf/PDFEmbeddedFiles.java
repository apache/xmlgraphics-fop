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
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

/**
 * Class representing an /EmbeddedFiles dictionary object (name tree).
 */
public class PDFEmbeddedFiles extends PDFNameTreeNode {

    /**
     * Create a /EmbeddedFiles dictionary.
     */
    public PDFEmbeddedFiles() {
        super();
    }

    /** {@inheritDoc} */
    protected void writeDictionary(OutputStream out, StringBuilder textBuffer) throws IOException {
        sortNames(); //Sort the names before writing them out
        super.writeDictionary(out, textBuffer);
    }

    private void sortNames() {
        PDFArray names = getNames();
        SortedMap map = new java.util.TreeMap();
        int i = 0;
        int c = names.length();
        while (i < c) {
            Comparable key = (Comparable)names.get(i++); //Key must be a Comparable for sorting
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

