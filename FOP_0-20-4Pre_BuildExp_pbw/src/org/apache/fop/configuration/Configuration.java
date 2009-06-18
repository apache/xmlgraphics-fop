/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
     * @param key a string containing the key value for the configuration value
     * role detemines the configuration target
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
     * @param key a string containing the key value for the configuration value
     * role detemines the configuration target
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
     * @param key a string containing the key value for the configuration value
     * role detemines the configuration target
     * @return int a int containing the value
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
     * role detemines the configuration target
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
     * @param key a string containing the key value for the configuration value
     * role detemines the configuration target
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
     * convenience methods to access hashmap values in the configuration
     * @param key a string containing the key value for the configuration value
     * role detemines the configuration target
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
     * @return int a int containing the value
     * -1   if the key is not defined.
     */
    public static int getIntValue(String key) {
        return Configuration.getIntValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access boolean values in the configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return boolean true or false as value
     * null   if the key is not defined.
     */
    public static Boolean getBooleanValue(String key) {
        return Configuration.getBooleanValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access list values in the standard configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return ArrayList a ArrayList containing the values
     * null   if the key is not defined.
     */
    public static ArrayList getListValue(String key) {
        return Configuration.getListValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access hashmap values in the standard configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return HashMap a HashMap containing the values
     * null   if the key is not defined.
     */
    public static HashMap getHashMapValue(String key) {
        return Configuration.getHashMapValue(key, Configuration.STANDARD);
    }


    /**
     * method to access fonts values in the standard configuration
     *
     * @param key a string containing the key value for the configuration value
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
            //log.error("Can't setup configuration. Unknown configuration role/target");
        }
    }

    /**
     * adds information to the configuration hashmap in key,value form
     * @param key a string containing the key value for the configuration value
     * value the configuration information
     * role detemines the configuration target
     * @param value an Object containing the value; can be a String, a ArrayList or a HashMap
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
            //log.error("Unknown role for new configuration entry. "
            //                       + "Putting key:" + key + " - value:"
            //                       + value + " into standard configuration.");
        }
    }

    ;

    /**
     * adds information to the standard configuration hashmap in key,value form
     * @param key a string containing the key value for the configuration value
     * value the configuration information
     * role detemines the configuration target
     * @param value an Object containing the value; can be a String, a ArrayList or a HashMap
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
        String tmp;
        System.out.println("Dumping configuration: ");
        HashMap[] configs = {
            standardConfiguration, pdfConfiguration, awtConfiguration
        };
        for (int i = 0; i < configs.length; i++) {
            //log.debug("----------------------");
            configuration = configs[i];
            Iterator iterator = configuration.keySet().iterator();
            while (iterator.hasNext()) {
                key = (String)iterator.next();
                //log.debug("key: " + key);
                value = configuration.get(key);
                if (value instanceof String) {
                    //log.debug("   value: " + value);
                } else if (value instanceof ArrayList) {
                    list = (ArrayList)value;
                    //log.debug("   values: ");
                    for (int count = 0; count < list.size(); count++) {
                        //log.debug(list.get(count) + " - ");
                    }
                } else if (value instanceof HashMap) {
                    map = (HashMap)value;
                    Iterator iter = map.keySet().iterator();
                    //log.debug("   values: ");
                    while (iter.hasNext()) {
                        tmp = (String)iter.next();
                        //log.debug(" " + tmp + ":" + map.get(tmp));
                    }
                }
            }
        }
    }



}

