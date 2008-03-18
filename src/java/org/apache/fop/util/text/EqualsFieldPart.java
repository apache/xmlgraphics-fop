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

public class EqualsFieldPart extends IfFieldPart {
    
    private String equalsValue;
    
    public EqualsFieldPart(String fieldName, String values) {
        super(fieldName, values);
    }

    /** {@inheritDoc} */
    protected void parseValues(String values) {
        String[] parts = AdvancedMessageFormat.COMMA_SEPARATOR_REGEX.split(values, 3);
        this.equalsValue = parts[0];
        if (parts.length == 1) {
            throw new IllegalArgumentException(
                    "'equals' format must have at least 2 parameters");
        }
        if (parts.length == 3) {
            ifValue = AdvancedMessageFormat.unescapeComma(parts[1]);
            elseValue = AdvancedMessageFormat.unescapeComma(parts[2]);
        } else {
            ifValue = AdvancedMessageFormat.unescapeComma(parts[1]);
        }
    }
    
    protected boolean isTrue(Map params) {
        Object obj = params.get(fieldName);
        if (obj != null) {
            return String.valueOf(obj).equals(this.equalsValue);
        } else {
            return false;
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return "{" + this.fieldName + ", equals " + this.equalsValue + "}";
    }
    
    public static class Factory implements PartFactory {

        /** {@inheritDoc} */
        public Part newPart(String fieldName, String values) {
            return new EqualsFieldPart(fieldName, values);
        }

        /** {@inheritDoc} */
        public String getFormat() {
            return "equals";
        }
        
    }
}