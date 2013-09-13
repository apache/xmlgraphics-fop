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

import org.apache.fop.util.XMLUtil;

// CSOFF: LineLengthCheck

public class PDFDictionaryEntryExtension {

    public static final String PROPERTY_KEY = "key";

    private PDFDictionaryEntryType type;
    private String key = "";
    private Object value;

    PDFDictionaryEntryExtension() {
    }

    PDFDictionaryEntryExtension(PDFDictionaryEntryType type) {
        this.type = type;
    }

    public PDFDictionaryEntryType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    /**
     * Obtain entry value as Boolean.
     * @return entry value
     */
    public Boolean getValueAsBoolean() {
        if (value instanceof String) {
            return Boolean.valueOf((String)value);
        } else {
            return false;
        }
    }

    /**
     * Obtain entry value as Number.
     * @return entry value
     */
    public Number getValueAsNumber() {
        if (value instanceof String) {
            double d = Double.parseDouble((String) value);
            if (Math.floor(d) == d) {
                return Long.valueOf((long) d);
            } else {
                return Double.valueOf(d);
            }
        } else {
            return Integer.valueOf(0);
        }
    }

    public String getValueAsString() {
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }

    public String getValueAsXMLEscapedString() {
        return XMLUtil.escape(getValueAsString());
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getElementName() {
        return type.elementName();
    }

}
