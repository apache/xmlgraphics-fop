/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

// based on work by Takayuki Takeuchi

/**
 * class representing system information for "character identifier" fonts.
 *
 * this small object is used in the CID fonts and in the CMaps.
 */
public class PDFCIDSystemInfo extends PDFObject {
    private String registry;
    private String ordering;
    private int supplement;

    /**
     * Create a CID system info.
     *
     * @param registry the registry value
     * @param ordering the ordering value
     * @param supplement the supplement value
     */
    public PDFCIDSystemInfo(String registry, String ordering,
                            int supplement) {
        this.registry = registry;
        this.ordering = ordering;
        this.supplement = supplement;
    }

    /**
     * Create a string for the CIDSystemInfo dictionary.
     * The entries are placed as an inline dictionary.
     *
     * @return the string for the CIDSystemInfo entry with the inline dictionary
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(64);
        p.setLength(0);
        p.append("/CIDSystemInfo << /Registry (");
        p.append(registry);
        p.append(") /Ordering (");
        p.append(ordering);
        p.append(") /Supplement ");
        p.append(supplement);
        p.append(" >>");
        return p.toString();
    }

}

