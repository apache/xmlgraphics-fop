/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */


package org.apache.fop.configuration;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import org.apache.fop.messaging.MessageHandler;

/**
 *  a configuration class for all general configuration aspects except those
 *  related to specific renderers. All configuration is stored
 *  in key / value pairs. The value can be a String, a list of Strings
 *  or a map, containing a list of key / value pairs.
 *
 */
public class Configuration {

    /** defines role types */
    public final static int STANDARD = 0;
    public final static int PDF = 1;
    public final static int AWT = 2;

    /** stores the configuration information */
    private static Hashtable standardConfiguration = new Hashtable(30);;
    private static Hashtable pdfConfiguration  = new Hashtable(20);
    private static Hashtable awtConfiguration  = new Hashtable(20);

    /** contains a Hashtable of existing Hashtables */    
    private static Hashtable configuration = new Hashtable(3);

    /** loads the configuration types into the configuration Hashtable */
    static {
        configuration.put("standard",standardConfiguration);
        configuration.put("pdf",pdfConfiguration);
        configuration.put("awt",awtConfiguration);
    }

    public static Hashtable getConfiguration() {
        return configuration;
    }

    /**
     * general access method
     *
     * @param key a string containing the key value for the configuration value
     *        role detemines the configuration target
     * @return Object containing the value; normally you would use one of the
     *                convenience methods, which return the correct form.
     *         null   if the key is not defined.
     */
    public static Object getValue (String key, int role) {
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
    };

    /**
     *  convenience methods to access strings values in the configuration
     *  @param key a string containing the key value for the configuration value
     *         role detemines the configuration target
     *  @return String a string containing the value
     *          null   if the key is not defined.
     */
    public static String getStringValue(String key, int role) {
        Object obj = Configuration.getValue (key, role);
        if (obj instanceof String) {
            return (String) obj;
        } else {
            return null;
        }
    };

    /**
     *  convenience methods to access int values in the configuration
     *  @param key a string containing the key value for the configuration value
     *         role detemines the configuration target
     *  @return int a int containing the value
     *          -1   if the key is not defined.
     */
    public static int getIntValue(String key, int role) {
        Object obj = Configuration.getValue (key, role);
        if (obj instanceof String) {
            return Integer.parseInt((String) obj);
        } else {
            return -1;
        }
    };

    /**
     *  convenience methods to access boolean values in the configuration
     *  @param key a string containing the key value for the configuration value
     *         role detemines the configuration target
     *  @return Boolean true or false as value
     *          null   if the key is not defined.
     */
    public static Boolean getBooleanValue(String key, int role) {
        Object obj = Configuration.getValue (key, role);
		if (obj instanceof String) {
			return new Boolean((String)obj); 
		} else if (obj instanceof Boolean) {
			return (Boolean) obj; 
		} else {
			return null;
		}
    };

    /**
     *  convenience methods to access list values in the configuration
     *  @param key a string containing the key value for the configuration value
     *         role detemines the configuration target
     *  @return Vector a Vector containing the values
     *          null   if the key is not defined.
     */
    public static Vector getListValue(String key, int role) {
        Object obj = Configuration.getValue (key, role);
        if (obj instanceof Vector) {
            return (Vector) obj;
        } else {
            return null;
        }
    };

    /**
     *  convenience methods to access map/hashtable values in the configuration
     *  @param key a string containing the key value for the configuration value
     *         role detemines the configuration target
     *  @return Hashtable a Hashtable containing the values
     *          null   if the key is not defined.
     */
    public static Hashtable getHashtableValue(String key, int role) {
        Object obj = Configuration.getValue (key, role);
        if (obj instanceof Hashtable) {
            return (Hashtable) obj;
        } else {
            return null;
        }
    };


    /**
     * convenience method which retrieves some configuration information
     * from the standard configuration
     *
     * @param key a string containing the key value for the configuration value
     * @return Object containing the value; normally you would use one of the
     *                convenience methods, which return the correct form.
     *         null   if the key is not defined.
     */
    public static Object getValue (String key) {
        return Configuration.getValue(key, Configuration.STANDARD);
    }

    /**
       *  convenience methods to access strings values in the standard configuration
       *
       *  @param key a string containing the key value for the configuration value
       *  @return String a string containing the value
       *          null   if the key is not defined.
       */
    public static String getStringValue(String key) {
        return Configuration.getStringValue(key, Configuration.STANDARD);
    }

    /**
       *  convenience methods to access int values in the standard configuration
       *
       *  @param key a string containing the key value for the configuration value
       *  @return int a int containing the value
       *          -1   if the key is not defined.
       */
    public static int getIntValue(String key) {
        return Configuration.getIntValue(key, Configuration.STANDARD);
    }

    /**
       *  convenience methods to access boolean values in the configuration
       *
       *  @param key a string containing the key value for the configuration value
       *  @return boolean true or false as value
       *          null   if the key is not defined.
       */
    public static Boolean getBooleanValue(String key) {
        return Configuration.getBooleanValue(key, Configuration.STANDARD);
    }

    /**
       *  convenience methods to access list values in the standard configuration
       *
       *  @param key a string containing the key value for the configuration value
       *  @return Vector a Vector containing the values
       *          null   if the key is not defined.
       */
    public static Vector getListValue(String key) {
        return Configuration.getListValue(key, Configuration.STANDARD);
    }

    /**
       *  convenience methods to access map/hashtable values in the standard configuration
       *
       *  @param key a string containing the key value for the configuration value
       *  @return Hashtable a Hashtable containing the values
       *          null   if the key is not defined.
       */
    public static Hashtable getHashtableValue(String key) {
        return Configuration.getHashtableValue(key, Configuration.STANDARD);
    }


    /**
       *  method to access fonts values in the standard configuration
       *
       *  @param key a string containing the key value for the configuration value
       *  @return Hashtable a Hashtable containing the values
       *          null   if the key is not defined.
       */
	public static Vector getFonts() {
		return (Vector) Configuration.getValue("fonts", Configuration.STANDARD);
	}
	
    /**
       * initializes this configuration
       * @param config contains the configuration information
       */
    public static void setup(int role, Hashtable config) {
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
    }

    /**
       *  adds information to the configuration map/hashtable in key,value form
       *  @param key a string containing the key value for the configuration value
       *         value the configuration information
       *         role detemines the configuration target
       *  @param value an Object containing the value; can be a String, a Vector or a Hashtable
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
                MessageHandler.errorln(
                  "Unknown role for new configuration entry. " +
                  "Putting key:" + key + " - value:" + value + " into standard configuration.");
        }
    };

    /**
     *  adds information to the standard configuration map/hashtable in key,value form
     *  @param key a string containing the key value for the configuration value
     *         value the configuration information
     *         role detemines the configuration target
     *  @param value an Object containing the value; can be a String, a Vector or a Hashtable
     */

    public static void put(String key, Object value) {
        Configuration.put(key, value, Configuration.STANDARD);
    }

    /**
       *  debug methods, which writes out all information in this configuration
       */
    public static void dumpConfiguration() {
        String key;
        Object value;
        Vector list;
        Hashtable map, configuration;
        Enumeration enum;
        String tmp;
        System.out.println("Dumping configuration: ");
        Hashtable [] configs = {standardConfiguration,
                                pdfConfiguration, awtConfiguration};
        for (int i = 0; i < configs.length ; i++) {
            MessageHandler.logln("----------------------");
            configuration = configs[i];
            Enumeration enumeration = configuration.keys();
            while (enumeration.hasMoreElements()) {
                key = (String) enumeration.nextElement();
                MessageHandler.logln("key: " + key);
                value = configuration.get(key);
                if (value instanceof String) {
                    MessageHandler.logln("   value: " + value);
                } else if (value instanceof Vector) {
                    list = (Vector) value;
                    enum = list.elements();
                    MessageHandler.log("   values: ");
                    while (enum.hasMoreElements()) {
                        MessageHandler.log(enum.nextElement() + " - ");
                    }
                    MessageHandler.logln("");
                } else if (value instanceof Hashtable) {
                    map = (Hashtable) value;
                    enum = map.keys();
                    MessageHandler.log("   values: ");
                    while (enum.hasMoreElements()) {
                        tmp = (String) enum.nextElement();
                        MessageHandler.log(" " + tmp + ":" + map.get(tmp));
                    }
                    MessageHandler.logln("");
                }
            }
        }
    }



}

