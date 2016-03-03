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

// CSOFF: LineLengthCheck

public class PDFPageExtension extends PDFDictionaryExtension {

    public static final String PROPERTY_PAGE_NUMBERS = "page-numbers";

    PDFPageExtension() {
        super(PDFDictionaryType.Page);
    }

    /**
     * Determine if page dictionary and page number matches.
     * @param pageNumber page number, where first page number is 1
     * @return true if this dictionary is a page dictionary and specified page number matches specified page-number property
     */
    public boolean matchesPageNumber(int pageNumber) {
        String pageNumbers = getProperty(PROPERTY_PAGE_NUMBERS);
        if ((pageNumbers == null) || (pageNumbers.length() == 0)) {
            return false;
        } else if (pageNumbers.equals("*")) {
            return true;
        } else {
            for (String interval : pageNumbers.split("\\s*,\\s*")) {
                String[] components = interval.split("\\s*-\\s*");
                if (components.length < 1) {
                    continue;
                } else {
                    try {
                        int start = Integer.parseInt(components[0]);
                        int end = 0;
                        if (components.length > 1) {
                            if (!components[1].equals("LAST")) {
                                end = Integer.parseInt(components[1]);
                            }
                        }
                        if ((end == 0) && (pageNumber == start)) {
                            return true;
                        } else if ((end > start) && (pageNumber >= start) && (pageNumber < end)) {
                            return true;
                        } else {
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }
        return false;
    }

}
