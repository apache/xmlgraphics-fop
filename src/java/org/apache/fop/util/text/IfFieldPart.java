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

public class IfFieldPart implements Part {
    
    protected String fieldName;
    protected String ifValue;
    protected String elseValue;
    
    public IfFieldPart(String fieldName, String values) {
        this.fieldName = fieldName;
        parseValues(values);
    }

    protected void parseValues(String values) {
        String[] parts = values.split(AdvancedMessageFormat.COMMA_SEPARATOR_REGEX, 2);
        if (parts.length == 2) {
            ifValue = AdvancedMessageFormat.unescapeComma(parts[0]);
            elseValue = AdvancedMessageFormat.unescapeComma(parts[1]);
        } else {
            ifValue = AdvancedMessageFormat.unescapeComma(values);
        }
    }
    
    public void write(StringBuffer sb, Map params) {
        boolean isTrue = isTrue(params);
        if (isTrue) {
            sb.append(ifValue);
        } else if (elseValue != null) {
            sb.append(elseValue);
        }
    }

    protected boolean isTrue(Map params) {
        Object obj = params.get(fieldName);
        if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        } else {
            return (obj != null);
        }
    }

    public boolean isGenerated(Map params) {
        return isTrue(params) || (elseValue != null);
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "{" + this.fieldName + ", if...}";
    }
    
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