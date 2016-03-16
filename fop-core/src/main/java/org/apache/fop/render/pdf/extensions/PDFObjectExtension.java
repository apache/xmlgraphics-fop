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

public class PDFObjectExtension {

    private PDFObjectType type;
    private Object value;

    PDFObjectExtension(PDFObjectType type) {
        this.type = type;
    }

    public PDFObjectType getType() {
        return type;
    }

    public void setValue(Object value) {
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
        Object value = getValue();
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
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
        Object value = getValue();
        if (value instanceof Number) {
            return (Number) value;
        } else if (value instanceof String) {
            double d = Double.parseDouble((String) value);
            if (Math.abs(Math.floor(d) - d) < 1E-10) {
                return Long.valueOf((long) d);
            } else {
                return Double.valueOf(d);
            }
        } else {
            return Integer.valueOf(0);
        }
    }

    public String getValueAsString() {
        Object value = getValue();
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return value.toString();
        }
    }

    public String getValueAsXMLEscapedString() {
        return XMLUtil.escape(getValueAsString());
    }

    public String getElementName() {
        return type.elementName();
    }

}
