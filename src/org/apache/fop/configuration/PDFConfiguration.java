package org.apache.fop.configuration;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  a configuration class for information related to the pdf renderer. All configuration is stored
 *  in key / value pairs. The value can be a String, a list of Strings
 *  or a map, containing a list of key / value pairs.
 *
 */

public class PDFConfiguration {

  /** stores the configuration information */
  private static Hashtable configuration;

  /**
   * general access method
   *
   * @param key a string containing the key value for the configuration value
   * @return Object containing the value; normally you would use one of the
   *                convenience methods, which return the correct form.
   *         null   if the key is not defined.
   */
  public static Object getValue (String key){
    return configuration.get(key);
  };

  /**
   *  convenience methods to access strings values in the configuration
   *  @param key a string containing the key value for the configuration value
   *  @return String a string containing the value
   *          null   if the key is not defined.
   */
  public static String getStringValue(String key){
    Object obj = configuration.get(key);
    if (obj instanceof String) {
      return (String) obj;
    } else {
      return null;
    }
  };

  /**
   *  convenience methods to access int values in the configuration
   *  @param key a string containing the key value for the configuration value
   *  @return int a int containing the value
   *          -1   if the key is not defined.
   */
  public static int getIntValue(String key){
    Object obj = configuration.get(key);
    if (obj instanceof String) {
      return Integer.parseInt((String) obj);
    } else {
      return -1;
    }
  };

  /**
   *  convenience methods to access list values in the configuration
   *  @param key a string containing the key value for the configuration value
   *  @return Vector a Vector containing the values
   *          null   if the key is not defined.
   */
  public static Vector getListValue(String key){
    Object obj = configuration.get(key);
    if (obj instanceof Vector) {
      return (Vector) obj;
    } else {
      return null;
    }
  };

  /**
   *  convenience methods to access map/hashtable values in the configuration
   *  @param key a string containing the key value for the configuration value
   *  @return Hashtable a Hashtable containing the values
   *          null   if the key is not defined.
   */
  public static Hashtable getHashtableValue(String key){
    Object obj = configuration.get(key);
    if (obj instanceof Hashtable) {
      return (Hashtable) obj;
    } else {
      return null;
    }
  };

  /**
   *  adds information to the configuration map/hashtable in key,value form
   *  @param key a string containing the key value for the configuration value
   *  @param value an Object containing the value; can be a String, a Vector or a Hashtable
   */
  public static void put(String key,Object value){
    configuration.put(key,value);
  };

  /**
   *  debug methods, which writes out all information in this configuration
   */
  public static void dumpConfiguration() {
    String key;
    Object value;
    Vector list;
    Hashtable map;
    Enumeration enum;
    String tmp;
    System.out.println("Dumping standard configuration: ");
    Enumeration enumeration = configuration.keys();
    while (enumeration.hasMoreElements()) {
      key = (String) enumeration.nextElement();
      System.out.print("  key: " + key);
      value = configuration.get(key);
      if (value instanceof String) {
        System.out.println(" value: " + value);
      } else if (value instanceof Vector) {
        list = (Vector) value;
        enum = list.elements();
        System.out.print(" value: ");
        while (enum.hasMoreElements()) {
          System.out.print( enum.nextElement() + " - ");
        }
        System.out.println("");
      } else if (value instanceof Hashtable) {
          map = (Hashtable) value;
          enum = map.keys();
          while (enum.hasMoreElements()) {
            tmp = (String) enum.nextElement();
            System.out.print(" " + tmp + ":" + map.get(tmp));
          }
          System.out.println("");
      }
    }

  }

  /**
   * initializes this configuration
   * @param config contains the configuration information
   */
  public static void setup(Hashtable config){
    configuration = config;
  }

}
