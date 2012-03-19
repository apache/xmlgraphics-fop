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

/**
 * A version of PDF. Values are ordered such that compareTo() gives sensible
 * results (e.g., {@code V1_4.compareTo(V1_5) < 0}).
 */
public enum Version {
    /** PDF v1 */
    V1_0("1.0"),
    /** PDF v1.1 */
    V1_1("1.1"),
    /** PDF v1.2 */
    V1_2("1.2"),
    /** PDF v1.3 */
    V1_3("1.3"),
    /** PDF v1.4 */
    V1_4("1.4"),
    /** PDF v1.5 */
    V1_5("1.5"),
    /** PDF v1.6 */
    V1_6("1.6"),
    /** PDF v1.7 */
    V1_7("1.7");

    private String version;

    private Version(String version) {
        this.version = version;
    }

    /**
     * Given the PDF version as a String, returns the corresponding enumerated type. The String
     * should be in the format "1.x" for PDF v1.x.
     *
     * @param version a version number
     * @return the corresponding Version instance
     * @throws IllegalArgumentException if the argument does not correspond to any existing PDF version
     */
    public static Version getValueOf(String version) {
        for (Version pdfVersion : Version.values()) {
            if (pdfVersion.toString().equals(version)) {
                return pdfVersion;
            }
        }
        throw new IllegalArgumentException("Invalid PDF version given: " + version);
    }

    @Override
    public String toString() {
        return version;
    }
}
