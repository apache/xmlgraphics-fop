/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.xslt;

import java.io.*;
import java.lang.reflect.*;


public class XSLTransform {

    public static void transform(String xmlSource, String xslURL,
                                 String outputFile) throws Exception {
        Class[] argTypes = {
            String.class, String.class, String.class
        };
        Object[] params = {
            xmlSource, xslURL, outputFile
        };
        transform(params, argTypes);
    }

    public static void transform(org.w3c.dom.Document xmlSource,
                                 String xslURL,
                                 String outputFile) throws Exception {
        Class[] argTypes = {
            org.w3c.dom.Document.class, String.class, String.class
        };

        Object[] params = {
            xmlSource, xslURL, outputFile
        };
        transform(params, argTypes);

    }

    public static void transform(String xmlSource, String xslURL,
                                 Writer outputWriter) throws Exception {
        Class[] argTypes = {
            String.class, String.class, Writer.class
        };
        Object[] params = {
            xmlSource, xslURL, outputWriter
        };
        transform(params, argTypes);

    }

    public static void transform(org.w3c.dom.Document xmlSource,
                                 InputStream xsl,
                                 org.w3c.dom.Document outputDoc) throws Exception {
        Class[] argTypes = {
            org.w3c.dom.Document.class, InputStream.class,
            org.w3c.dom.Document.class
        };
        Object[] params = {
            xmlSource, xsl, outputDoc
        };
        transform(params, argTypes);

    }


    private static void transform(Object[] args,
                                  Class[] argTypes) throws Exception {
        Class transformer = getTransformClass();
        if (transformer != null) {
            Method transformMethod = getTransformMethod(transformer,
                                                        argTypes);
            if (transformMethod != null) {
                try {
                    transformMethod.invoke(null, args);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            } else {
                throw new Exception("transform method not found");
            }
        } else {
            throw new Exception("no transformer class found");
        }

    }


    private static Class getTransformClass() {
        try {
            // try trax first
            Class transformer =
                Class.forName("javax.xml.transform.Transformer");
            // ok, make sure we have a liaison to trax
            transformer =
                Class.forName("org.apache.fop.tools.xslt.TraxTransform");
            return transformer;

        } catch (ClassNotFoundException ex) {}
        // otherwise, try regular xalan1
        try {
            Class transformer =
                Class.forName("org.apache.xalan.xslt.XSLTProcessor");
            // get the liaison
            transformer =
                Class.forName("org.apache.fop.tools.xslt.Xalan1Transform");
            return transformer;
        } catch (ClassNotFoundException ex) {}
        return null;

    }


    private static Method getTransformMethod(Class c, Class[] argTypes) {
        // System.out.println("transformer class = "+c);

        try {
            // Class[] argTypes = new Class[args.length];
            for (int i = 0; i < argTypes.length; i++) {
                // argTypes[i] = args[i].getClass();
                // System.out.println("arg["+i+"] type = "+argTypes[i]);

            }

            Method transformer = c.getMethod("transform", argTypes);
            return transformer;

        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();

        }
        return null;
    }

}
