/*
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * $Id$
 */

package org.apache.fop.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.fop.apps.Fop;

/**
 * a configuration class for all general configuration aspects except those
 * related to specific renderers. All configuration is stored in
 * key.value pairs. The value can be a String, a Boolean, an Integer,
 * or a list of Strings or a map, containing a list of key.value pairs.
 *
 */
public class Configuration {

    protected static final Logger logger = Logger.getLogger(Fop.fopPackage);
    /**
     * defines role types
     */
    public final static int STANDARD = 0;
    public final static int PDF = 1;
    public final static int AWT = 2;

    /**
     * stores the configuration information
     */
    private static HashMap standardConfiguration = new HashMap(30);
    private static HashMap pdfConfiguration = new HashMap(20);
    private static HashMap awtConfiguration = new HashMap(20);

    /**
     * contains a HashMap of existing HashMaps
     */
    private static HashMap configuration = new HashMap(3);

    /**
     * loads the configuration types into the configuration HashMap
     */
    static {
        configuration.put("standard", standardConfiguration);
        configuration.put("pdf", pdfConfiguration);
        configuration.put("awt", awtConfiguration);
    }

    public static HashMap getConfiguration() {
        return configuration;
    }

    /**
     * general access method
     *
     * @param key a string containing the key for the configuration value
     * role determines the configuration target
     * @return Object containing the value; normally you would use one of the
     * convenience methods, which return the correct form.
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
     * @param key a string containing the key for the configuration value
     * role determines the configuration target
     * @return String a string containing the value
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
     * @param key a string containing the key for the configuration value
     * role determines the configuration target
     * @return int a int containing the value
     * -1   if the key is not defined.
     */
    public static int getIntValue(String key, int role) {
        Object obj = Configuration.getValue(key, role);
        if (obj instanceof String) {
            return Integer.parseInt((String)obj);
        } else if (obj instanceof Integer) {
            return ((Integer)obj).intValue();
        } else {
            return -1;
        }
    }


    /**
     * convenience methods to access boolean values in the configuration
     * @param key a string containing the key for the configuration value
     * role determines the configuration target
     * @return Boolean true or false as value
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
     * @param key a string containing the key for the configuration value
     * role determines the configuration target
     * @return ArrayList a ArrayList containing the values
     * null   if the key is not defined.
     */
    public static ArrayList getListValue(String key, int role) {
        Object obj = Configuration.getValue(key, role);
        if (obj instanceof ArrayList) {
            return (ArrayList)obj;
        } else {
            return null;
        }
    }


    /**
     * convenience methods to access map/hashtable values in the configuration
     * @param key a string containing the key for the configuration value
     * role determines the configuration target
     * @return HashMap a HashMap containing the values
     * null   if the key is not defined.
     */
    public static HashMap getHashMapValue(String key, int role) {
        Object obj = Configuration.getValue(key, role);
        if (obj instanceof HashMap) {
            return (HashMap)obj;
        } else {
            return null;
        }
    }


    /**
     * Convenience method to access values in HashMap entries in the
     * configuration.
     * @param map a string containing the key for the configuration value.
     * <i>N.B.</i> this is the key of the <tt>HashMap</tt> itself.
     * @param key an object containing the key for an entry in the
     * <tt>HashMap</tt> indexed by <i>map</i>.
     * @param role determines the configuration target
     * @return an <tt>Object</tt> containing the value associated with
     * <i>key</i>, or <tt>null</tt>
     * if the map is not defined, the <i>key</i>entry is not defined,
     * or the <i>key</i>entry is itself <tt>null</tt>.
     */
    public static Object getHashMapEntry(String map, Object key, int role) {
        Object obj = getValue(map, role);
        if (obj instanceof HashMap) {
            return ((HashMap)obj).get(key);
        } else {
            return null;
        }
    }


    /**
     * convenience method which retrieves some configuration information
     * from the standard configuration
     *
     * @param key a string containing the key for the configuration value
     * @return Object containing the value; normally you would use one of the
     * convenience methods, which return the correct form.
     * null   if the key is not defined.
     */
    public static Object getValue(String key) {
        return Configuration.getValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access strings values in the standard
     * configuration
     *
     * @param key a string containing the key for the configuration value
     * @return String a string containing the value
     * null   if the key is not defined.
     */
    public static String getStringValue(String key) {
        return Configuration.getStringValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access int values in the standard configuration
     *
     * @param key a string containing the key for the configuration value
     * @return int a int containing the value
     * -1   if the key is not defined.
     */
    public static int getIntValue(String key) {
        return Configuration.getIntValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access boolean values in the configuration
     *
     * @param key a string containing the key for the configuration value
     * @return boolean true or false as value
     * null   if the key is not defined.
     */
    public static Boolean getBooleanValue(String key) {
        return Configuration.getBooleanValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access list values in the standard configuration
     *
     * @param key a string containing the key for the configuration value
     * @return ArrayList a ArrayList containing the values
     * null   if the key is not defined.
     */
    public static ArrayList getListValue(String key) {
        return Configuration.getListValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access map/hashtable values in the standard
     * configuration
     *
     * @param key a string containing the key for the configuration value
     * @return HashMap a HashMap containing the values
     * null   if the key is not defined.
     */
    public static HashMap getHashMapValue(String key) {
        return Configuration.getHashMapValue(key, Configuration.STANDARD);
    }


    /**
     * Convenience method to access values in HashMap entries in the
     * standard configuration.
     * @param map a string containing the key for the configuration value.
     * <i>N.B.</i> this is the key of the <tt>HashMap</tt> itself.
     * @param key an object containing the key for an entry in the
     * <tt>HashMap</tt> indexed by <i>map</i>.
     * @return an <tt>Object</tt> containing the value associated with
     * <i>key</i>, or <tt>null</tt>
     * if the map is not defined, the <i>key</i>entry is not defined,
     * or the <i>key</i>entry is itself <tt>null</tt>.
     */
    public static Object getHashMapEntry(String map, Object key) {
        return Configuration.getHashMapEntry(map, key, STANDARD);
    }


    /**
     * method to access fonts values in the standard configuration
     *
     * @return HashMap a HashMap containing the values
     * null   if the key is not defined.
     */
    public static ArrayList getFonts() {
        return (ArrayList)Configuration.getValue("fonts",
                                              Configuration.STANDARD);
    }

    /**
     * initializes this configuration
     * @param config contains the configuration information
     */
    public static void setup(int role, HashMap config) {
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
            logger.warning(
            "Can't setup configuration. Unknown configuration role/target"
            );
        }
    }

    /**
     * adds information to the configuration map/hashtable in key,value form
     * @param key a string containing the key for the configuration value
     * value the configuration information
     * role determines the configuration target
     * @param value an Object containing the value;
     * can be a String, a Boolean, and Integer, an ArrayList or a HashMap
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
            logger.warning("Unknown role for new configuration entry."
                                   + " Putting key:" + key + " - value:"
                                   + value + " into standard configuration.");
        }
    }


    /**
     * adds information to the standard configuration hashmap in key.value form
     * @param key a string containing the key for the configuration value
     * value the configuration information
     * role determines the configuration target
     * @param value an Object containing the value;
     * can be a String, a Boolean, an Integer, an ArrayList or a HashMap
     */

    public static void put(String key, Object value) {
        Configuration.put(key, value, Configuration.STANDARD);
    }

    /**
     * debug methods, which writes out all information in this configuration
     */
    public static void dumpConfiguration() {
        String key;
        Object value;
        ArrayList list;
        HashMap map, configuration;
        Iterator iterator;
        String tmp;
        System.out.println("Dumping configuration: ");
        HashMap[] configs = {
            standardConfiguration, pdfConfiguration, awtConfiguration
        };
        for (int i = 0; i < configs.length; i++) {
            logger.config("----------------------");
            configuration = configs[i];
            Iterator iter = configuration.keySet().iterator();
            while (iter.hasNext()) {
                key = (String)iter.next();
                logger.config("key: " + key);
                value = configuration.get(key);
                if (value instanceof String) {
                    logger.config("   value: " + value);
                } else if (value instanceof Boolean) {
                    logger.config
                            ("   value: " + ((Boolean)value).booleanValue());
                } else if (value instanceof Integer) {
                    logger.config
                                ("   value: " + ((Integer)value).intValue());
                } else if (value instanceof ArrayList) {
                    list = (ArrayList)value;
                    iterator = list.iterator();
                    StringBuffer msg = new StringBuffer("   values: ");
                    while (iterator.hasNext()) {
                        msg.append(iterator.next() + " - ");
                    }
                    logger.config(msg.toString());
                } else if (value instanceof HashMap) {
                    map = (HashMap)value;
                    iterator = map.keySet().iterator();
                    StringBuffer msg = new StringBuffer("   values: ");
                    while (iterator.hasNext()) {
                        tmp = (String)iterator.next();
                        msg.append(" " + tmp + ":" + map.get(tmp));
                    }
                    logger.config(msg.toString());
                }
            }
        }
    }

}

