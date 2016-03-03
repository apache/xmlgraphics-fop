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

public enum PDFVTMode {
    DISABLED("PDF/VT disabled"),
    PDFVT_1("PDF/VT-1");

    private String name;

    PDFVTMode(String s) {
        name = s;
    }

    /**
     * Returns the mode enum object given a String.
     *
     * @param s the string
     * @return the PDFVTMode enum object (DISABLED will be returned if no match is found)
     */
    public static PDFVTMode getValueOf(String s) {
        for (PDFVTMode mode : values()) {
            if (mode.name.equalsIgnoreCase(s)) {
                return mode;
            }
        }
        return DISABLED;
    }
}
