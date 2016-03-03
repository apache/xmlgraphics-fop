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

/** Enum class for PDF/UA modes. */
public enum PDFUAMode {

    /** PDF/UA disabled. */
    DISABLED("PDF/UA disabled"),
    /** PDF/UA-1 enabled. */
    PDFUA_1(1);

    private final String name;

    private final int part;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     */
    private PDFUAMode(String name) {
        this.name = name;
        this.part = 0;
    }

    private PDFUAMode(int part) {
        this.name = "PDF/UA-" + part;
        this.part = part;
    }

    /** @return the name of the enum */
    public String getName() {
        return this.name;
    }

    public int getPart() {
        return part;
    }

    /**
     * Returns {@code true} if this enum corresponds to one of the available PDF/A modes.
     *
     * @return {@code true} if this is not DISABLED
     */
    public boolean isEnabled() {
        return this != DISABLED;
    }

    /**
     * Returns the mode enum object given a String.
     * @param s the string
     * @return the PDFAMode enum object (DISABLED will be returned if no match is found)
     */
    public static PDFUAMode getValueOf(String s) {
        for (PDFUAMode mode : values()) {
            if (mode.name.equalsIgnoreCase(s)) {
                return mode;
            }
        }
        return DISABLED;
    }

    /** {@inheritDoc} */
    public String toString() {
        return name;
    }

}
