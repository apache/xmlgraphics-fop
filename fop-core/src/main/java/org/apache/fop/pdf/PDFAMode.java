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

/** Enum class for PDF/A modes. */
public enum PDFAMode {

    /** PDF/A disabled. */
    DISABLED("PDF/A disabled"),
    /** PDF/A-1a enabled. */
    PDFA_1A(1, 'A'),
    /** PDF/A-1b enabled. */
    PDFA_1B(1, 'B'),
    /** PDF/A-2a enabled. */
    PDFA_2A(2, 'A'),
    /** PDF/A-2b enabled. */
    PDFA_2B(2, 'B'),
    /** PDF/A-2u enabled. */
    PDFA_2U(2, 'U'),

    PDFA_3A(3, 'A'),
    PDFA_3B(3, 'B'),
    PDFA_3U(3, 'U');

    private final String name;

    private final int part;

    private final char level;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     */
    private PDFAMode(String name) {
        this.name = name;
        this.part = 0;
        this.level = 0;
    }

    private PDFAMode(int part, char level) {
        this.name = "PDF/A-" + part + Character.toLowerCase(level);
        this.part = part;
        this.level = level;
    }

    /** @return the name of the enum */
    public String getName() {
        return this.name;
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
     * Returns the part of the specification this enum corresponds to.
     *
     * @return 1 for PDF/A-1 (ISO 19005-1), 2 for PDF/A-2 (ISO 19005-2)
     */
    public int getPart() {
        return part;
    }

    /**
     * Returns {@code true} if this enum corresponds to PDF/A-1 (ISO 19005-1).
     */
    public boolean isPart1() {
        return part == 1;
    }

    /**
     * Returns {@code true} if this enum corresponds to PDF/A-2 (ISO 19005-2).
     */
    public boolean isPart2() {
        return part == 1 || part == 2;
    }

    /**
     * Returns the conformance level for this enum.
     *
     * @return 'A', 'B' or 'U'
     */
    public char getConformanceLevel() {
        return level;
    }

    /**
     * Returns {@code true} if this enum corresponds to conformance level A.
     */
    public boolean isLevelA() {
        return level == 'A';
    }

    /**
     * Returns the mode enum object given a String.
     * @param s the string
     * @return the PDFAMode enum object (DISABLED will be returned if no match is found)
     */
    public static PDFAMode getValueOf(String s) {
        for (PDFAMode mode : values()) {
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
