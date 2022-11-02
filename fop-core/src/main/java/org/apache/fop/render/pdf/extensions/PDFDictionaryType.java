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

/**
 * Enumeration type for PDF dictionary extension elements.
 */
public enum PDFDictionaryType {
    Action("action", true),             // action dictionary element
    Catalog("catalog"),                 // catalog dictionary element
    Dictionary("dictionary"),           // generic (nested) dictionary element
    Layer("layer", true),               // optional content group dictionary element
    Navigator("navigator", true),       // navigation node dictionary element
    Page("page"),                       // page dictionary element
    /** Document Information Dictionary */
    Info("info"),
    VT("vt"),
    PagePiece("pagepiece");

    private String elementName;
    private boolean usesIDAttribute;
    PDFDictionaryType(String elementName, boolean usesIDAttribute) {
        this.elementName = elementName;
        this.usesIDAttribute = usesIDAttribute;
    }
    PDFDictionaryType(String elementName) {
        this(elementName, false);
    }
    public String elementName() {
        return elementName;
    }
    public boolean usesIDAttribute() {
        return usesIDAttribute;
    }
    static PDFDictionaryType valueOfElementName(String elementName) {
        for (PDFDictionaryType type : values()) {
            if (type.elementName.equals(elementName)) {
                return type;
            }
        }
        throw new IllegalArgumentException();
    }
    static boolean hasValueOfElementName(String elementName) {
        try {
            return valueOfElementName(elementName) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
