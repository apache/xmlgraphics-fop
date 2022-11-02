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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    /**
     * {@inheritDoc}
     */
    public byte[] toPDF() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(128);
        try {
            bout.write(encode("<< /Registry "));
            bout.write(encodeText(registry));
            bout.write(encode(" /Ordering "));
            bout.write(encodeText(ordering));
            bout.write(encode(" /Supplement "));
            bout.write(encode(Integer.toString(supplement)));
            bout.write(encode(" >>"));
        } catch (IOException ioe) {
            log.error("Ignored I/O exception", ioe);
        }
        return bout.toByteArray();
    }

}

