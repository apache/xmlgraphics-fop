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

/** Enum class for PDF/X modes. */
public enum PDFXMode {

    /** PDF/X disabled */
    DISABLED("PDF/X disabled"),
    /** PDF/X-3:2003 enabled */
    PDFX_3_2003("PDF/X-3:2003");

    private String name;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     */
    private PDFXMode(String name) {
        this.name = name;
    }

    /** @return the name of the enum */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the mode enum object given a String.
     * @param s the string
     * @return the PDFAMode enum object (DISABLED will be returned if no match is found)
     */
    public static PDFXMode getValueOf(String s) {
        if (PDFX_3_2003.getName().equalsIgnoreCase(s)) {
            return PDFX_3_2003;
        } else {
            return DISABLED;
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return name;
    }

}
