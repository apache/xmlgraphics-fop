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

package org.apache.fop.util.text;

import java.util.Map;

import org.apache.fop.util.text.AdvancedMessageFormat.Part;
import org.apache.fop.util.text.AdvancedMessageFormat.PartFactory;

/**
 * Defines an "if" field part that checks if field's value is true or false.
 * It returns either of two possible values attached as additional part parameters. Example:
 * <code>{field,if,Yes,No}</code>
 */
public class IfFieldPart implements Part {

    /** the field name for the part */
    protected String fieldName;
    /** the value being returned if the field is true */
    protected String ifValue;
    /** the value being returned if the field is false */
    protected String elseValue;

    /**
     * Creates a new "if" field part.
     * @param fieldName the field name
     * @param values the unparsed parameter values
     */
    public IfFieldPart(String fieldName, String values) {
        this.fieldName = fieldName;
        parseValues(values);
    }

    /**
     * Parses the parameter values
     * @param values the unparsed parameter values
     */
    protected void parseValues(String values) {
        String[] parts = AdvancedMessageFormat.COMMA_SEPARATOR_REGEX.split(values, 2);
        if (parts.length == 2) {
            ifValue = AdvancedMessageFormat.unescapeComma(parts[0]);
            elseValue = AdvancedMessageFormat.unescapeComma(parts[1]);
        } else {
            ifValue = AdvancedMessageFormat.unescapeComma(values);
        }
    }

    /** {@inheritDoc} */
    public void write(StringBuffer sb, Map params) {
        boolean isTrue = isTrue(params);
        if (isTrue) {
            sb.append(ifValue);
        } else if (elseValue != null) {
            sb.append(elseValue);
        }
    }

    /**
     * Indicates whether the field's value is true. If the field is not a boolen, it is true
     * if the field is not null.
     * @param params the message parameters
     * @return true the field's value as boolean
     */
    protected boolean isTrue(Map params) {
        Object obj = params.get(fieldName);
        if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        } else {
            return (obj != null);
        }
    }

    /** {@inheritDoc} */
    public boolean isGenerated(Map params) {
        return isTrue(params) || (elseValue != null);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "{" + this.fieldName + ", if...}";
    }

    /**
     * Part factory for "if".
     */
    public static class Factory implements PartFactory {

        /** {@inheritDoc} */
        public Part newPart(String fieldName, String values) {
            return new IfFieldPart(fieldName, values);
        }

        /** {@inheritDoc} */
        public String getFormat() {
            return "if";
        }

    }
}
