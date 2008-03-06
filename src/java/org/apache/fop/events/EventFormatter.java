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

package org.apache.fop.events;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.fop.util.XMLResourceBundle;
import org.apache.fop.util.text.AdvancedMessageFormat;
import org.apache.fop.util.text.AdvancedMessageFormat.Part;
import org.apache.fop.util.text.AdvancedMessageFormat.PartFactory;

/**
 * Converts events into human-readable, localized messages.
 */
public class EventFormatter {

    private static final Pattern INCLUDES_PATTERN = Pattern.compile("\\{\\{.+\\}\\}");
    
    private static ResourceBundle defaultBundle = XMLResourceBundle.getXMLBundle(
            EventFormatter.class.getName(), EventFormatter.class.getClassLoader());
    
    public static String format(Event event) {
        return format(event, defaultBundle);
    }
    
    public static String format(Event event, Locale locale) {
        ResourceBundle bundle = XMLResourceBundle.getXMLBundle(
                EventFormatter.class.getName(),
                locale,
                EventFormatter.class.getClassLoader());
        return format(event, bundle);
    }

    private static String format(Event event, ResourceBundle bundle) {
        String template = bundle.getString(event.getEventID());
        return format(event, processIncludes(template, bundle));
    }

    private static String processIncludes(String template, ResourceBundle bundle) {
        CharSequence input = template;
        int replacements;
        StringBuffer sb;
        do {
            sb = new StringBuffer(Math.max(16, input.length()));
            replacements = processIncludesInner(input, sb, bundle);
            input = sb;
        } while (replacements > 0);
        String s = sb.toString();
        return s;
    }

    private static int processIncludesInner(CharSequence template, StringBuffer sb,
            ResourceBundle bundle) {
        int replacements = 0;
        Matcher m = INCLUDES_PATTERN.matcher(template);
        while (m.find()) {
            String include = m.group();
            include = include.substring(2, include.length() - 2);
            m.appendReplacement(sb, bundle.getString(include));
            replacements++;
        }
        m.appendTail(sb);
        return replacements;
    }

    public static String format(Event event, String pattern) {
        AdvancedMessageFormat format = new AdvancedMessageFormat(pattern);
        Map params = new java.util.HashMap(event.getParams());
        params.put("source", event.getSource());
        params.put("severity", event.getSeverity());
        return format.format(params);
    }
    
    private static class LookupFieldPart implements Part {
        
        private String fieldName;
        
        public LookupFieldPart(String fieldName) {
            this.fieldName = fieldName;
        }

        public boolean isGenerated(Map params) {
            return getKey(params) != null;
        }

        public void write(StringBuffer sb, Map params) {
            sb.append(defaultBundle.getString(getKey(params)));
        }

        private String getKey(Map params) {
            return (String)params.get(fieldName);
        }
        
        /** {@inheritDoc} */
        public String toString() {
            return "{" + this.fieldName + ", lookup}";
        }
        
    }
    
    /** PartFactory for lookups. */
    public static class LookupFieldPartFactory implements PartFactory {

        /** {@inheritDoc} */
        public Part newPart(String fieldName, String values) {
            return new LookupFieldPart(fieldName);
        }

        /** {@inheritDoc} */
        public String getFormat() {
            return "lookup";
        }
        
    }

}
