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
 * Enumeration type for leaf PDF object extension types used as singletons,
 * dictionary entries, or array entries.
 */
public enum PDFObjectType {
    Array("array"),                     // array valued entry
    Boolean("boolean"),                 // boolean valued entry
    Dictionary("dictionary"),           // dictionary valued entry
    Name("name"),                       // name valued entry
    Number("number"),                   // number valued entry
    Reference("reference"),             // indirect object reference entry
    String("string");                   // string valued entry

    private String elementName;
    PDFObjectType(String elementName) {
        this.elementName = elementName;
    }
    public String elementName() {
        return elementName;
    }
    static PDFObjectType valueOfElementName(String elementName) {
        for (PDFObjectType type : values()) {
            if (type.elementName.equals(elementName)) {
                return type;
            }
        }
        throw new IllegalArgumentException();
    }
    static boolean hasValueOfElementName(String elementName) {
        try {
            valueOfElementName(elementName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
