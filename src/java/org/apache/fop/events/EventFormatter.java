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

import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.fop.util.XMLResourceBundle;

/**
 * Converts events into human-readable, localized messages.
 */
public class EventFormatter {

    private static ResourceBundle defaultBundle = XMLResourceBundle.getXMLBundle(
            EventFormatter.class.getName(), EventFormatter.class.getClassLoader());
    
    public static String format(Event event) {
        return format(event, defaultBundle);
    }
    
    public static String format(Event event, ResourceBundle bundle) {
        String template = bundle.getString(event.getEventID());
        return format(event, template);
    }

    public static String format(Event event, String template) {
        Map params = event.getParams();
        Pattern p = Pattern.compile("\\$\\{[^\\}]+\\}");
        Matcher m = p.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String formatElement = m.group();
            formatElement = formatElement.substring(2, formatElement.length() - 1);
            //TODO Handle conditional sub-formats
            //TODO Add advanced formatting like in MessageFormat here
            String key = formatElement;
            if (!params.containsKey(key)) {
                throw new IllegalArgumentException(
                        "Message template contains unsupported variable key: " + key);
            }
            Object obj = params.get(key);
            String value;
            if (obj == null) {
                value = "";
            } else {
                value = obj.toString();
            }
            m.appendReplacement(sb, value);
        }
        m.appendTail(sb);
        
        return sb.toString();
    }

}
