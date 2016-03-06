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

package org.apache.fop.render.pdf.extensions;

import java.util.List;
import java.util.Map;

// CSOFF: LineLengthCheck

public class PDFArrayExtension extends PDFCollectionExtension {

    private static final long serialVersionUID = -1L;

    private Map<String, String> properties;
    private List<PDFCollectionEntryExtension> entries;

    PDFArrayExtension() {
        super(PDFObjectType.Array);
        this.properties = new java.util.HashMap<String, String>();
        this.entries = new java.util.ArrayList<PDFCollectionEntryExtension>();
    }

    @Override
    public void setValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue() {
        return getEntries();
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public void addEntry(PDFCollectionEntryExtension entry) {
        if (entry.getKey() != null) {
            throw new IllegalArgumentException();
        } else {
            entries.add(entry);
        }
    }

    public List<PDFCollectionEntryExtension> getEntries() {
        return entries;
    }

    public PDFCollectionEntryExtension getLastEntry() {
        if (entries.size() > 0) {
            return entries.get(entries.size() - 1);
        } else {
            return null;
        }
    }

    @Override
    public String getElementName() {
        return PDFObjectType.Array.elementName();
    }

}
