/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id$
 */

package org.apache.fop.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
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

    protected Logger logger = Logger.getLogger(Fop.fopPackage);
    /**
     * defines role types
     */
    public final static int STANDARD = 0;
    public final static int PDF = 1;
    public final static int AWT = 2;

    /**
     * stores the configuration information
     */
    private HashMap standardConfiguration = new HashMap();
    private HashMap pdfConfiguration = new HashMap();
    private HashMap awtConfiguration = new HashMap();

    /**
     * contains a HashMap of existing HashMaps
     */
    private HashMap configuration = new HashMap(3);
    
    public Configuration() {
        configuration.put("standard", standardConfiguration);
        configuration.put("pdf", pdfConfiguration);
        configuration.put("awt", awtConfiguration);
    }

    public Configuration(int role, HashMap config) {
        this();
        setRole(role, config);
    }
    
    public void setRole(int role, HashMap config) {
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
    
    public HashMap getConfiguration() {
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
    public Object getValue(String key, int role) {
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
    public String getStringValue(String key, int role) {
        Object obj = getValue(key, role);
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
    public int getIntValue(String key, int role) {
        Object obj = getValue(key, role);
        if (obj instanceof String) {
            return Integer.parseInt((String)obj);
        } else if (obj instanceof Integer) {
            return ((Integer)obj).intValue();
        } else {
            return -1;
        }
    }


    /**
     * convenience methods to access Boolean values in the configuration
     * @param key a string containing the key for the configuration value
     * role determines the configuration target
     * @return Boolean true or false as value
     * null   if the key is not defined.
     */
    public Boolean getBooleanObject(String key, int role) {
        Object obj = getValue(key, role);
        if (obj instanceof String) {
            return new Boolean((String)obj);
        } else if (obj instanceof Boolean) {
            return (Boolean)obj;
        } else {
            return null;
        }
    }

    /**
     * Convenience method for accessing boolean values in the configuration
     * @param key the key for the configuration entry
     * @param role the configuration target
     * @return the boolean value of the key value if defined, or false
     */
    public boolean isTrue(String key, int role) {
        Boolean bval = getBooleanObject(key, role);
        if (bval == null) return false;
        return bval.booleanValue();
    }

    /**
     * convenience methods to access list values in the configuration
     * @param key a string containing the key for the configuration value
     * role determines the configuration target
     * @return ArrayList a ArrayList containing the values
     * null   if the key is not defined.
     */
    public ArrayList getListValue(String key, int role) {
        Object obj = getValue(key, role);
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
    public HashMap getHashMapValue(String key, int role) {
        Object obj = getValue(key, role);
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
    public Object getHashMapEntry(String map, Object key, int role) {
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
    public Object getValue(String key) {
        return getValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access strings values in the standard
     * configuration
     *
     * @param key a string containing the key for the configuration value
     * @return String a string containing the value
     * null   if the key is not defined.
     */
    public String getStringValue(String key) {
        return getStringValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access int values in the standard configuration
     *
     * @param key a string containing the key for the configuration value
     * @return int a int containing the value
     * -1   if the key is not defined.
     */
    public int getIntValue(String key) {
        return getIntValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access Boolean values in the configuration
     *
     * @param key a string containing the key for the configuration value
     * @return boolean true or false as value
     * null   if the key is not defined.
     */
    public Boolean getBooleanObject(String key) {
        return getBooleanObject(key, Configuration.STANDARD);
    }

    /**
     * Convenience method for accessing boolean values in the configuration
     * @param key the key for the configuration entry
     * @return the boolean value of the key value if defined, or false
     */
    public boolean isTrue(String key) {
        Boolean bval = getBooleanObject(key);
        if (bval == null) return false;
        return bval.booleanValue();
    }

    /**
     * convenience methods to access list values in the standard configuration
     *
     * @param key a string containing the key for the configuration value
     * @return ArrayList a ArrayList containing the values
     * null   if the key is not defined.
     */
    public ArrayList getListValue(String key) {
        return getListValue(key, Configuration.STANDARD);
    }

    /**
     * convenience methods to access map/hashtable values in the standard
     * configuration
     *
     * @param key a string containing the key for the configuration value
     * @return HashMap a HashMap containing the values
     * null   if the key is not defined.
     */
    public HashMap getHashMapValue(String key) {
        return getHashMapValue(key, Configuration.STANDARD);
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
    public Object getHashMapEntry(String map, Object key) {
        return getHashMapEntry(map, key, STANDARD);
    }


    /**
     * method to access fonts values in the standard configuration
     *
     * @return HashMap a HashMap containing the values
     * null   if the key is not defined.
     */
    public ArrayList getFonts() {
        return (ArrayList)getValue("fonts",
                                              Configuration.STANDARD);
    }

    /**
     * initializes this configuration
     * @param config contains the configuration information
     */
    public void setup(int role, HashMap config) {
    }

    /**
     * adds information to the configuration map/hashtable in key,value form
     * @param key a string containing the key for the configuration value
     * value the configuration information
     * role determines the configuration target
     * @param value an Object containing the value;
     * can be a String, a Boolean, and Integer, an ArrayList or a HashMap
     */
    public void put(String key, Object value, int role) {
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

    public void put(String key, Object value) {
        put(key, value, Configuration.STANDARD);
    }

    /**
     * debug methods, which writes out all information in this configuration
     */
    public void dumpConfiguration() {
        StringBuffer msg = new StringBuffer();
        logger.setLevel(Level.CONFIG);
        HashMap[] configs = {
            standardConfiguration, pdfConfiguration, awtConfiguration
        };
        for (int i = 0; i < configs.length; i++) {
            msg.append("\n----------------------");
            HashMap configuration = configs[i];
            Iterator iter = configuration.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                msg.append("\nkey: " + key);
                Object value = configuration.get(key);
                if (value instanceof String) {
                    msg.append("   value: " + value);
                } else if (value instanceof Boolean) {
                    msg.append
                            ("   value: " + ((Boolean)value).booleanValue());
                } else if (value instanceof Integer) {
                    msg.append
                                ("   value: " + ((Integer)value).intValue());
                } else if (value instanceof ArrayList) {
                    ArrayList list = (ArrayList)value;
                    Iterator iterator = list.iterator();
                    msg.append("   values: ");
                    if (iterator.hasNext()) {
                        msg.append("<" + iterator.next() + ">");
                    }
                    while (iterator.hasNext()) {
                        msg.append(", <" + iterator.next() + ">");
                    }
                } else if (value instanceof HashMap) {
                    HashMap map = (HashMap)value;
                    Iterator iterator = map.keySet().iterator();
                    msg.append("   values: ");
                    while (iterator.hasNext()) {
                        String tmp = (String)iterator.next();
                        msg.append("\n    " + tmp + ":" + map.get(tmp));
                    }
                }
            }
        }
        msg.append("\n");
        logger.config(msg.toString());
    }

}

