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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xmlgraphics.util.Service;


/**
 * Formats messages based on a template and with a set of named parameters. This is similar to
 * {@link java.util.MessageFormat} but uses named parameters and supports conditional sub-groups.
 * <p>
 * Example:
 * </p>
 * <p><code>Missing field "{fieldName}"[ at location: {location}]!</code></p>
 * <ul>
 *   <li>Curly brackets ("{}") are used for fields.</li>
 *   <li>Square brackets ("[]") are used to delimit conditional sub-groups. A sub-group is
 *     conditional when all fields inside the sub-group have a null value. In the case, everything
 *     between the brackets is skipped.</li>
 * </ul>
 */
public class AdvancedMessageFormat {

    /** Regex that matches "," but not "\," (escaped comma) */
    static final String COMMA_SEPARATOR_REGEX = "(?<!\\\\),";
    
    private static final Map PART_FACTORIES = new java.util.HashMap();
    private static final List OBJECT_FORMATTERS = new java.util.ArrayList();
    private static final Map FUNCTIONS = new java.util.HashMap();
    
    private CompositePart rootPart;
    
    static {
        Iterator iter;
        iter = Service.providers(PartFactory.class, true);
        while (iter.hasNext()) {
            PartFactory factory = (PartFactory)iter.next();
            PART_FACTORIES.put(factory.getFormat(), factory);
        }
        iter = Service.providers(ObjectFormatter.class, true);
        while (iter.hasNext()) {
            OBJECT_FORMATTERS.add((ObjectFormatter)iter.next());
        }
        iter = Service.providers(Function.class, true);
        while (iter.hasNext()) {
            Function function = (Function)iter.next();
            FUNCTIONS.put(function.getName(), function);
        }
    }
    
    /**
     * Construct a new message format.
     * @param pattern the message format pattern.
     */
    public AdvancedMessageFormat(CharSequence pattern) {
        parsePattern(pattern);
    }
    
    private void parsePattern(CharSequence pattern) {
        rootPart = new CompositePart(false);
        StringBuffer sb = new StringBuffer();
        parseInnerPattern(pattern, rootPart, sb, 0);
    }
    
    private int parseInnerPattern(CharSequence pattern, CompositePart parent,
            StringBuffer sb, int start) {
        assert sb.length() == 0;
        int i = start;
        int len = pattern.length();
        loop:
        while (i < len) {
            char ch = pattern.charAt(i);
            switch (ch) {
            case '{':
                if (sb.length() > 0) {
                    parent.addChild(new TextPart(sb.toString()));
                    sb.setLength(0);
                }
                i++;
                while (i < len) {
                    ch = pattern.charAt(i);
                    if (ch == '}') {
                        i++;
                        break;
                    }
                    sb.append(ch);
                    i++;
                }
                parent.addChild(parseField(sb.toString()));
                sb.setLength(0);
                break;
            case ']':
                i++;
                break loop; //Current composite is finished
            case '[':
                if (sb.length() > 0) {
                    parent.addChild(new TextPart(sb.toString()));
                    sb.setLength(0);
                }
                i++;
                CompositePart composite = new CompositePart(true);
                parent.addChild(composite);
                i += parseInnerPattern(pattern, composite, sb, i);
                break;
            case '|':
                if (sb.length() > 0) {
                    parent.addChild(new TextPart(sb.toString()));
                    sb.setLength(0);
                }
                parent.newSection();
                i++;
                break;
            case '\\':
                if (i < len - 1) {
                    i++;
                    ch = pattern.charAt(i);
                }
                //no break here! Must be right before "default" section
            default:
                sb.append(ch);
                i++;
            }
        }
        if (sb.length() > 0) {
            parent.addChild(new TextPart(sb.toString()));
            sb.setLength(0);
        }
        return i - start;
    }
    
    private Part parseField(String field) {
        String[] parts = field.split(COMMA_SEPARATOR_REGEX, 3);
        String fieldName = parts[0];
        if (parts.length == 1) {
            if (fieldName.startsWith("#")) {
                return new FunctionPart(fieldName.substring(1));
            } else {
                return new SimpleFieldPart(fieldName);
            }
        } else {
            String format = parts[1];
            PartFactory factory = (PartFactory)PART_FACTORIES.get(format);
            if (factory == null) {
                throw new IllegalArgumentException(
                        "No PartFactory available under the name: " + format);
            }
            if (parts.length == 2) {
                return factory.newPart(fieldName, null);
            } else {
                return factory.newPart(fieldName, parts[2]);
            }
        }
    }

    private static Function getFunction(String functionName) {
        return (Function)FUNCTIONS.get(functionName);
    }

    /**
     * Formats a message with the given parameters.
     * @param params a Map of named parameters (Contents: <String, Object>)
     * @return the formatted message
     */
    public String format(Map params) {
        StringBuffer sb = new StringBuffer();
        rootPart.write(sb, params);
        return sb.toString();
    }

    public interface Part {
        void write(StringBuffer sb, Map params);
        boolean isGenerated(Map params);
    }
    
    public interface PartFactory {
        Part newPart(String fieldName, String values);
        String getFormat();
    }
    
    public interface ObjectFormatter {
        void format(StringBuffer sb, Object obj);
        boolean supportsObject(Object obj);
    }
    
    public interface Function {
        Object evaluate(Map params);
        Object getName();
    }
    
    private static class TextPart implements Part {
        
        private String text;
        
        public TextPart(String text) {
            this.text = text;
        }
        
        public void write(StringBuffer sb, Map params) {
            sb.append(text);
        }
        
        public boolean isGenerated(Map params) {
            return true;
        }

        /** {@inheritDoc} */
        public String toString() {
            return this.text;
        }
    }
    
    private static class SimpleFieldPart implements Part {
        
        private String fieldName;
        
        public SimpleFieldPart(String fieldName) {
            this.fieldName = fieldName;
        }
        
        public void write(StringBuffer sb, Map params) {
            if (!params.containsKey(fieldName)) {
                throw new IllegalArgumentException(
                        "Message pattern contains unsupported field name: " + fieldName);
            }
            Object obj = params.get(fieldName);
            formatObject(obj, sb);
        }

        public boolean isGenerated(Map params) {
            Object obj = params.get(fieldName);
            return obj != null;
        }
        
        /** {@inheritDoc} */
        public String toString() {
            return "{" + this.fieldName + "}";
        }
    }
    
    public static void formatObject(Object obj, StringBuffer target) {
        if (obj instanceof String) {
            target.append(obj);
        } else {
            boolean handled = false;
            Iterator iter = OBJECT_FORMATTERS.iterator();
            while (iter.hasNext()) {
                ObjectFormatter formatter = (ObjectFormatter)iter.next();
                if (formatter.supportsObject(obj)) {
                    formatter.format(target, obj);
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                target.append(obj.toString());
            }
        }
    }
    
    private static class FunctionPart implements Part {
        
        private Function function;
        
        public FunctionPart(String functionName) {
            this.function = getFunction(functionName);
            if (this.function == null) {
                throw new IllegalArgumentException("Unknown function: " + functionName);
            }
        }
        
        public void write(StringBuffer sb, Map params) {
            Object obj = this.function.evaluate(params);
            formatObject(obj, sb);
        }

        public boolean isGenerated(Map params) {
            Object obj = this.function.evaluate(params);
            return obj != null;
        }
        
        /** {@inheritDoc} */
        public String toString() {
            return "{#" + this.function.getName() + "}";
        }
    }
    
    private static class CompositePart implements Part {
        
        protected List parts = new java.util.ArrayList();
        private boolean conditional;
        private boolean hasSections = false;
        
        public CompositePart(boolean conditional) {
            this.conditional = conditional;
        }
        
        private CompositePart(List parts) {
            this.parts.addAll(parts);
            this.conditional = true;
        }
        
        public void addChild(Part part) {
            if (part == null) {
                throw new NullPointerException("part must not be null");
            }
            if (hasSections) {
                CompositePart composite = (CompositePart)this.parts.get(this.parts.size() - 1);
                composite.addChild(part);
            } else {
                this.parts.add(part);
            }
        }
        
        public void newSection() {
            if (!hasSections) {
                List p = this.parts;
                //Dropping into a different mode...
                this.parts = new java.util.ArrayList();
                this.parts.add(new CompositePart(p));
                hasSections = true;
            }
            this.parts.add(new CompositePart(true));
        }
        
        public void write(StringBuffer sb, Map params) {
            if (hasSections) {
                Iterator iter = this.parts.iterator();
                while (iter.hasNext()) {
                    CompositePart part = (CompositePart)iter.next();
                    if (part.isGenerated(params)) {
                        part.write(sb, params);
                        break;
                    }
                }
            } else {
                if (isGenerated(params)) {
                    Iterator iter = this.parts.iterator();
                    while (iter.hasNext()) {
                        Part part = (Part)iter.next();
                        part.write(sb, params);
                    }
                }
            }
        }

        public boolean isGenerated(Map params) {
            if (hasSections) {
                Iterator iter = this.parts.iterator();
                while (iter.hasNext()) {
                    Part part = (Part)iter.next();
                    if (part.isGenerated(params)) {
                        return true;
                    }
                }
                return false;
            } else {
                if (conditional) {
                    Iterator iter = this.parts.iterator();
                    while (iter.hasNext()) {
                        Part part = (Part)iter.next();
                        if (!part.isGenerated(params)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        
        /** {@inheritDoc} */
        public String toString() {
            return this.parts.toString();
        }
    }
    
    
    static String unescapeComma(String string) {
        return string.replaceAll("\\\\,", ",");
    }
}
