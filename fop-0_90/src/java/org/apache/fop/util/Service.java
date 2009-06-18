/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//code stolen from org.apache.batik.util and modified slightly
//does what sun.misc.Service probably does, but it cannot be relied on.
//hopefully will be part of standard jdk sometime.

/**
 * This class loads services present in the class path.
 */
public class Service {

     private static Map providerMap = new java.util.Hashtable();

     public static synchronized Iterator providers(Class cls) {
         ClassLoader cl = cls.getClassLoader();
         // null if loaded by bootstrap class loader
         if (cl == null) {
             cl = ClassLoader.getSystemClassLoader();
         }
         String serviceFile = "META-INF/services/" + cls.getName();

         // log.debug("File: " + serviceFile);

         List lst = (List)providerMap.get(serviceFile);
         if (lst != null) {
             return lst.iterator();
         }

         lst = new java.util.Vector();
         providerMap.put(serviceFile, lst);

         Enumeration e;
         try {
             e = cl.getResources(serviceFile);
         } catch (IOException ioe) {
             return lst.iterator();
         }

         while (e.hasMoreElements()) {
             try {
                 java.net.URL u = (java.net.URL)e.nextElement();
                 //log.debug("URL: " + u);

                 InputStream is = u.openStream();
                 Reader r = new InputStreamReader(is, "UTF-8");
                 BufferedReader br = new BufferedReader(r);

                 String line = br.readLine();
                 while (line != null) {
                     try {
                         // First strip any comment...
                         int idx = line.indexOf('#');
                         if (idx != -1) {
                             line = line.substring(0, idx);
                         }

                         // Trim whitespace.
                         line = line.trim();

                         // If nothing left then loop around...
                         if (line.length() == 0) {
                             line = br.readLine();
                             continue;
                         }
                         // log.debug("Line: " + line);

                         // Try and load the class
                         // Object obj = cl.loadClass(line).newInstance();
                         // stick it into our vector...
                         lst.add(line);
                     } catch (Exception ex) {
                         // Just try the next line
                     }

                     line = br.readLine();
                 }
             } catch (Exception ex) {
                 // Just try the next file...
             }

         }
         return lst.iterator();
     }

 }