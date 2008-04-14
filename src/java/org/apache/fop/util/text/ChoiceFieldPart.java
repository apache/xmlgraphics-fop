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

import java.text.ChoiceFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.fop.util.text.AdvancedMessageFormat.Part;
import org.apache.fop.util.text.AdvancedMessageFormat.PartFactory;

/**
 * Defines a "choice" field part that works like {@link ChoiceFormat}.
 */
public class ChoiceFieldPart implements Part {
    
    private static final Pattern VARIABLE_REGEX = Pattern.compile("\\{([^\\}]+)\\}");
    
    private String fieldName;
    private ChoiceFormat choiceFormat;
    
    /**
     * Creates a new choice part.
     * @param fieldName the field name to work on
     * @param choicesPattern the choices pattern (as used by {@link ChoiceFormat})
     */
    public ChoiceFieldPart(String fieldName, String choicesPattern) {
        this.fieldName = fieldName;
        this.choiceFormat = new ChoiceFormat(choicesPattern);
    }

    /** {@inheritDoc} */
    public boolean isGenerated(Map params) {
        Object obj = params.get(fieldName);
        return obj != null;
    }

    /** {@inheritDoc} */
    public void write(StringBuffer sb, Map params) {
        Object obj = params.get(fieldName);
        Number num = (Number)obj;
        String result = this.choiceFormat.format(num.doubleValue());
        Matcher m = VARIABLE_REGEX.matcher(result);
        if (m.find()) {
            //Resolve inner variables
            AdvancedMessageFormat f = new AdvancedMessageFormat(result);
            f.format(params, sb);
        } else {
            sb.append(result);
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return "{" + this.fieldName + ",choice, ....}";
    }
    
    /** Factory for ChoiceFieldPart. */
    public static class Factory implements PartFactory {

        /** {@inheritDoc} */
        public Part newPart(String fieldName, String values) {
            return new ChoiceFieldPart(fieldName, values);
        }

        /** {@inheritDoc} */
        public String getFormat() {
            return "choice";
        }
        
    }

}