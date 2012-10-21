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

    /** PDF/A disabled */
    DISABLED("PDF/A disabled"),
    /** PDF/A-1a enabled */
    PDFA_1A("PDF/A-1a"),
    /** PDF/A-1b enabled */
    PDFA_1B("PDF/A-1b");

    private String name;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     */
    private PDFAMode(String name) {
        this.name = name;
    }

    /** @return the name of the enum */
    public String getName() {
        return this.name;
    }

    /**
     * Indicates whether this mode obeys the restrictions established by PDF/A-1a.
     * @return true if this mode obeys the restrictions established by PDF/A-1a.
     */
    public boolean isPDFA1LevelA() {
        return (this == PDFA_1A);
    }

    /**
     * Indicates whether this mode obeys the restrictions established by PDF/A-1b.
     * @return true if this mode obeys the restrictions established by PDF/A-1b.
     */
    public boolean isPDFA1LevelB() {
        return (this != DISABLED);
        //PDF/A-1a is a superset of PDF/A-1b!
    }

    /**
     * Returns the mode enum object given a String.
     * @param s the string
     * @return the PDFAMode enum object (DISABLED will be returned if no match is found)
     */
    public static PDFAMode getValueOf(String s) {
        if (PDFA_1A.getName().equalsIgnoreCase(s)) {
            return PDFA_1A;
        } else if (PDFA_1B.getName().equalsIgnoreCase(s)) {
            return PDFA_1B;
        } else {
            return DISABLED;
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return name;
    }

}
