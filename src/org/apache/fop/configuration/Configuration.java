/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.configuration;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.io.File;
import java.net.URL;
import org.apache.fop.messaging.MessageHandler;

/**
 * a configuration class for all general configuration aspects except those
 * related to specific renderers. All configuration is stored
 * in key / value pairs. The value can be a String, a list of Strings
 * or a map, containing a list of key / value pairs.
 *
 */
public class Configuration {

    /**
     * defines role types
     */
    public static final int STANDARD = 0;
    public static final int PDF = 1;
    public static final int AWT = 2;

    /**
     * stores the configuration information
     */
    private static Map standardConfiguration = new java.util.HashMap(30);
    private static Map pdfConfiguration = new java.util.HashMap(20);
    private static Map awtConfiguration = new java.util.HashMap(20);

    /**
     * contains a Map of existing Maps
     */
    private static Map configuration = new java.util.HashMap(3);

    //URL cache
    private static URL cachedBaseURL = null;
    private static URL cachedFontBaseURL = null;

    /**
     * loads the configuration types into the configuration Map
     */
    static {
        configuration.put("standard", standardConfiguration);
        configuration.put("pdf", pdfConfiguration);
        configuration.put("awt", awtConfiguration);
    }

    public static Map getConfiguration() {
        return configuration;
    }

    /**
     * general access method
     *
     * @param key a string containing the key value for the configuration value
     * @param role detemines the configuration target
     * @return Object containing the value; normally you would use one of the
     * convenience methods, which return the correct form,
     * null   if the key is not defined.
     */
    public static Object getValue(String key, int role) {
        switch (role) {
        case Configuration.STANDARD:
            return standardConfiguration.get(key);
        case Configuration.PDF:
            return pdfConfiguration.get(key);
        case Configuration.AWT:
            return awtConfiguration.get(key);
        default:
            return standardConfiguration.get(key);
        }
    }


    /**
     * convenience methods to access strings values in the configuration
     * @param key a string containing the key value for the configuration value
     * @param role detemines the configuration target
     * @return String a string containing the value,
     * null   if the key is not defined.
     */
    public static String getStringValue(String key, int role) {
        Object obj = Configuration.getValue(key, role);
        if (obj instanceof String) {
            return (String)obj;
        } else {
            return null;
        }
    }


    /**
     * convenience methods to access int values in the configuration
     * @param key a string containing the key value for the configuration value
     * @param role detemines the configuration target
     * @return int a int containing the value,
     * -1   if the key is not defined.
     */
    public static int getIntValue(String key, int role) {
        Object obj = Configuration.getValue(key, role);
        if (obj instanceof String) {
            return Integer.parseInt((String)obj);
        } else {
            return -1;
        }
    }


    /**
     * convenience methods to access boolean values in the configuration
     * @param key a string containing the key value for the configuration value
     * @param role detemines the configuration target
     * @return Boolean true or false as value,
     * null   if the key is not defined.
     */
    public static Boolean getBooleanValue(String key, int role) {
        Object obj = Configuration.getValue(key, role);
        if (obj instanceof String) {
            return new Boolean((String)obj);
        } else if (obj instanceof Boolean) {
            return (Boolean)obj;
        } else {
            return null;
        }
    }


    /**
     * convenience methods to access list values in the configuration
     * @param key a string containing the key value for the configuration value
     * @param role detemines the configuration target
     * @return List a List containing the values,
     * null   if the key is not defined.
     */
    public static List getListValue(String key, int role) {
        Object obj = Configuration.getValue(key, role);
        if (obj instanceof List) {
            return (List)obj;
        } else {
            return null;
        }
    }


    /**
     * convenience methods to access Map values in the configuration
     * @param key a string containing the key value for the configuration value
     * @param role detemines the configuration target
     * @return Map a Map containing the values
     * null   if the key is not defined.
     */
    public static Map getMapValue(String key, int role) {
        Object obj = Configuration.getValue(key, role);
        if (obj instanceof Map) {
            return (Map)obj;
        } else {
            return null;
        }
    }


    /**
     * convenience method which retrieves some configuration information
     * from the standard configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return Object containing the value; normally you would use one of the
     * convenience methods, which return the correct form.
     * null   if the key is not defined.
     */
    public static Object getValue(String key) {
        return Configuration.getValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access strings values in the standard configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return String a string containing the value
     * null   if the key is not defined.
     */
    public static String getStringValue(String key) {
        return Configuration.getStringValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access int values in the standard configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return int a int containing the value,
     * -1   if the key is not defined.
     */
    public static int getIntValue(String key) {
        return Configuration.getIntValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access boolean values in the configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return boolean true or false as value,
     * null   if the key is not defined.
     */
    public static Boolean getBooleanValue(String key) {
        return Configuration.getBooleanValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access list values in the standard configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return List a List containing the values,
     * null   if the key is not defined.
     */
    public static List getListValue(String key) {
        return Configuration.getListValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access Map values in the standard configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return Map a Map containing the values,
     * null   if the key is not defined.
     */
    public static Map getMapValue(String key) {
        return Configuration.getMapValue(key, Configuration.STANDARD);
    }


    /**
     * Method to access fonts values in the standard configuration
     *
     * @return List a List containing the values,
     * null   if the key is not defined.
     */
    public static List getFonts() {
        return (List)Configuration.getValue("fonts",
                                              Configuration.STANDARD);
    }


    private static URL buildBaseURL(String directory) throws java.net.MalformedURLException {
        if (directory == null) return null;
        File dir = new File(directory);
        if (dir.isDirectory()) {
            return dir.toURL();
        } else {
            URL baseURL = new URL(directory);
            return baseURL;
        }
    }

    public static URL getBaseURL() {
        if (cachedBaseURL != null) {
            return cachedBaseURL;
        } else {
            String baseDir = getStringValue("baseDir");
            try {
                URL url = buildBaseURL(baseDir);;
                cachedBaseURL = url;
                return url;
            } catch (java.net.MalformedURLException mfue) {
                throw new RuntimeException("Invalid baseDir specified: "+baseDir+" ("+mfue.getMessage()+")");
            }
        }
    }


    public static URL getFontBaseURL() {
        if (cachedFontBaseURL != null) {
            return cachedFontBaseURL;
        } else {
            URL url = null;
            String baseDir = getStringValue("fontBaseDir");
            if (baseDir == null) {
                url = getBaseURL();
            } else {
                try {
                    url = buildBaseURL(baseDir);
                } catch (java.net.MalformedURLException mfue) {
                    throw new RuntimeException("Invalid fontBaseDir specified: "+baseDir+" ("+mfue.getMessage()+")");
                }
            }
            cachedFontBaseURL = url;
            return url;
        }
    }

    /**
     * Initializes this configuration
     * @param role detemines the configuration target
     * @param config contains the configuration information
     */
    public static void setup(int role, Map config) {
        switch (role) {
        case Configuration.STANDARD:
            standardConfiguration = config;
            break;
        case Configuration.PDF:
            pdfConfiguration = config;
            break;
        case Configuration.AWT:
            awtConfiguration = config;
            break;
        default:
            MessageHandler.errorln("Can't setup configuration. Unknown configuration role/target");
        }
        invalidateURLCache();
    }

    /**
     * adds information to the configuration Map in key,value form
     * @param key a string containing the key value for the configuration value
     * @param value the configuration information; can be a String, a List or a Map
     * @param role detemines the configuration target
     */
    public static void put(String key, Object value, int role) {
        switch (role) {
        case Configuration.STANDARD:
            standardConfiguration.put(key, value);
            break;
        case Configuration.PDF:
            pdfConfiguration.put(key, value);
            break;
        case Configuration.AWT:
            awtConfiguration.put(key, value);
            break;
        default:
            standardConfiguration.put(key, value);
            MessageHandler.errorln("Unknown role for new configuration entry. "
                                   + "Putting key:" + key + " - value:"
                                   + value + " into standard configuration.");
        }
        invalidateURLCache();
    }

    /**
     * adds information to the standard configuration Map in key,value form
     * @param key a string containing the key value for the configuration value
     * value the configuration information
     * role detemines the configuration target
     * @param value an Object containing the value; can be a String, a List or a Map
     */

    public static void put(String key, Object value) {
        Configuration.put(key, value, Configuration.STANDARD);
    }

    private static void invalidateURLCache() {
        cachedBaseURL = null;
        cachedFontBaseURL = null;
    }

    /**
     * debug methods, which writes out all information in this configuration
     */
    public static void dumpConfiguration() {
        String key;
        Object value;
        List list;
        Map map, configuration;
        String tmp;
        System.out.println("Dumping configuration: ");
        Map[] configs = {
            standardConfiguration, pdfConfiguration, awtConfiguration
        };
        for (int i = 0; i < configs.length; i++) {
            MessageHandler.logln("----------------------");
            configuration = configs[i];
            Iterator iterator = configuration.keySet().iterator();
            while (iterator.hasNext()) {
                key = (String)iterator.next();
                MessageHandler.logln("key: " + key);
                value = configuration.get(key);
                if (value instanceof String) {
                    MessageHandler.logln("   value: " + value);
                } else if (value instanceof List) {
                    list = (List)value;
                    MessageHandler.log("   values: ");
                    for (int j = 0; j < list.size(); j++) {
                        MessageHandler.log(list.get(j) + " - ");
                    }
                    MessageHandler.logln("");
                } else if (value instanceof Map) {
                    map = (Map)value;
                    MessageHandler.log("   values: ");
                    Iterator it2 = map.keySet().iterator();
                    while (it2.hasNext()) {
                        tmp = (String)it2.next();
                        MessageHandler.log(" " + tmp + ":" + map.get(tmp));
                    }
                    MessageHandler.logln("");
                }
            }
        }
    }



}

