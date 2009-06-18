/*
 *
 * Copyright 2004 The Apache Software Foundation.
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
 * Created on 8/03/2004
 * $Id$
 */
package org.apache.fop.configuration;

import java.io.InputStream;
import java.net.URL;

import org.apache.fop.apps.FOPException;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class ConfigurationResource {
    
    /**
     * Convenience class for common functionality required by the config
     * files.
     * @param fname the configuration file name.
     * @param classob the requesting class
     * @return an <tt>InputStream</tt> generated through a call to
     * <tt>getResourceAsStream</tt> on the context <tt>ClassLoader</tt>
     * or the <tt>ClassLoader</tt> for the conf class provided as an argument.
     */
    public static InputStream getResourceFile(String fname, Class classob)
    throws FOPException
    {
        InputStream configfile = null;
        
        // Try to use Context Class Loader to load the properties file.
        try {
            java.lang.reflect.Method getCCL =
                Thread.class.getMethod("getContextClassLoader", new Class[0]);
            if (getCCL != null) {
                ClassLoader contextClassLoader =
                    (ClassLoader)getCCL.invoke(Thread.currentThread(),
                            new Object[0]);
                configfile = contextClassLoader.getResourceAsStream(fname);
            }
        } catch (Exception e) {}
        
        // the entry /conf/config.xml refers to a directory conf
        // which is a sibling of org
        if (configfile == null)
            configfile = classob.getResourceAsStream(fname);
        if (configfile == null) {
            throw new FOPException(
                    "can't find configuration file " + fname);
        }
        return configfile;
    }
    
    /**
     * Convenience class for common functionality required by the config
     * files.
     * @param fname the configuration file name.
     * @param classob the requesting class
     * @return a <tt>URL</tt> generated through a call to
     * <tt>getResource</tt> on the context <tt>ClassLoader</tt>
     * or the <tt>ClassLoader</tt> for the conf class provided as an argument.
     */
    public static URL getResourceUrl(String fname, Class classob)
    throws FOPException
    {
        URL configUrl = null;
        
        // Try to use Context Class Loader to load the properties file.
        try {
            java.lang.reflect.Method getCCL =
                Thread.class.getMethod("getContextClassLoader", new Class[0]);
            if (getCCL != null) {
                ClassLoader contextClassLoader =
                    (ClassLoader)getCCL.invoke(Thread.currentThread(),
                            new Object[0]);
                configUrl = contextClassLoader.getResource(fname);
            }
        } catch (Exception e) {}
        
        // the entry /conf/config.xml refers to a directory conf
        // which is a sibling of org
        if (configUrl == null)
            configUrl = classob.getResource(fname);
        if (configUrl == null) {
            throw new FOPException(
                    "can't find configuration file " + fname);
        }
        return configUrl;
    }
}
