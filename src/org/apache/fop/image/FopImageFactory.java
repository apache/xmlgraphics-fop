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
package org.apache.fop.image;

// FOP
import org.apache.fop.image.analyser.ImageReaderFactory;
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.messaging.MessageHandler;

// Java
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * create FopImage objects (with a configuration file - not yet implemented).
 * @author Eric SCHAEFFER
 */
public class FopImageFactory {
    // prevent instantiation
    protected FopImageFactory() {}

    private static Map m_urlMap = new java.util.HashMap();

    /**
     * The class name of the generic image handler.
     * Will be either jimi or jai depending on what
     * is available.
     */
    private static String m_genericImageClassName = null;

    /**
     * create an FopImage objects.
     * @param href image URL as a String
     * @return a new FopImage object
     * @exception java.net.MalformedURLException bad URL
     * @exception FopImageException an error occured during construction
     */
    public static synchronized FopImage Make(String href)
            throws MalformedURLException, FopImageException {

        /*
         * According to section 5.11 a <uri-specification> is:
         * "url(" + URI + ")"
         * according to 7.28.7 a <uri-specification> is:
         * URI
         * So handle both.
         */
        // Get the absolute URL
        URL absoluteURL = null;
        InputStream imgIS = null;
        href = href.trim();
        if(href.startsWith("url(") && (href.indexOf(")") != -1)) {
            href = href.substring(4, href.indexOf(")")).trim();
            if(href.startsWith("'") && href.endsWith("'")) {
                href = href.substring(1, href.length() - 1);
            } else if(href.startsWith("\"") && href.endsWith("\"")) {
                href = href.substring(1, href.length() - 1);
            }
        }

        // check if already created
        FopImage imageObject = (FopImage)m_urlMap.get(href);
        if (imageObject != null)
            return imageObject;

        try {
            // try url as complete first, this can cause
            // a problem with relative uri's if there is an
            // image relative to where fop is run and relative
            // to the base dir of the document
            try {
                absoluteURL = new URL(href);
            } catch (MalformedURLException mue) {
                // if the href contains onl a path then file is assumed
                absoluteURL = new URL("file:" + href);
            }
            imgIS = absoluteURL.openStream();
        } catch (MalformedURLException e_context) {
            throw new FopImageException("Error with image URL: "
                                        + e_context.getMessage());
        } catch (Exception e) {
            // maybe relative
            URL baseURL = Configuration.getBaseURL();

            if (baseURL == null) {
                throw new FopImageException("Error with image URL: "
                                             + e.getMessage()
                                             + " and no base URL is specified");
            }

            try {
                /*
                    This piece of code is based on the following statement in RFC2396 section 5.2:

                    3) If the scheme component is defined, indicating that the reference
                       starts with a scheme name, then the reference is interpreted as an
                       absolute URI and we are done.  Otherwise, the reference URI's
                       scheme is inherited from the base URI's scheme component.

                       Due to a loophole in prior specifications [RFC1630], some parsers
                       allow the scheme name to be present in a relative URI if it is the
                       same as the base URI scheme.  Unfortunately, this can conflict
                       with the correct parsing of non-hierarchical URI.  For backwards
                       compatibility, an implementation may work around such references
                       by removing the scheme if it matches that of the base URI and the
                       scheme is known to always use the <hier_part> syntax.

                    The URL class does not implement this work around, so we do.
                */

                String scheme = baseURL.getProtocol() + ":";
                if (href.startsWith(scheme)) {
                    href = href.substring(scheme.length());
                }
                absoluteURL = new URL(baseURL, href);
            } catch (MalformedURLException e_context) {
                throw new FopImageException("Invalid Image URL - error on relative URL : "
                                            + e_context.getMessage());
            }
        }

        // If not, check image type
        ImageReader imgReader = null;
        try {
            if (imgIS == null) {
                imgIS = absoluteURL.openStream();
            }
            imgReader = ImageReaderFactory.Make(absoluteURL.toExternalForm(),
                                                imgIS);
        } catch (Exception e) {
            throw new FopImageException("Error while recovering Image Informations ("
                                        + absoluteURL.toString() + ") : "
                                        + e.getMessage());
        }
        finally {
            if (imgIS != null) {
                try {
                    imgIS.close();
                } catch (IOException e) {}
            }
        }
        if (imgReader == null)
            throw new FopImageException("No ImageReader for this type of image ("
                                        + absoluteURL.toString() + ")");
            // Associate mime-type to FopImage class
        String imgMimeType = imgReader.getMimeType();
        String imgClassName = null;
        if ("image/gif".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.GifImage";
        } else if ("image/jpeg".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.JpegImage";
        } else if ("image/bmp".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.BmpImage";
        } else if ("image/png".equals(imgMimeType)) {
            imgClassName = getGenericImageClassName();
        } else if ("image/tga".equals(imgMimeType)) {
            imgClassName = getGenericImageClassName();
        } else if ("image/eps".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.EPSImage";
        } else if ("image/tiff".equals(imgMimeType)) {
            try {
                imgClassName = "org.apache.fop.image.TiffImage";
                Class.forName(imgClassName);
            } catch (Throwable t) {
                imgClassName = getGenericImageClassName();
            }
        } else if ("image/svg+xml".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.SVGImage";
        }
        if (imgClassName == null)
            throw new FopImageException("Unsupported image type ("
                                        + absoluteURL.toString() + ") : "
                                        + imgMimeType);

        // load the right image class
        // return new <FopImage implementing class>
        Object imageInstance = null;
        Class imageClass = null;
        try {
            imageClass = Class.forName(imgClassName);
            Class[] imageConstructorParameters = new Class[2];
            imageConstructorParameters[0] = Class.forName("java.net.URL");
            imageConstructorParameters[1] =
                Class.forName("org.apache.fop.image.analyser.ImageReader");
            Constructor imageConstructor =
                imageClass.getDeclaredConstructor(imageConstructorParameters);
            Object[] initArgs = new Object[2];
            initArgs[0] = absoluteURL;
            initArgs[1] = imgReader;
            imageInstance = imageConstructor.newInstance(initArgs);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            String msg;
            if (t != null) {
                msg = t.getMessage();
            } else {
                msg = ex.getMessage();
            }
            throw new FopImageException("Error creating FopImage object ("
                                        + absoluteURL.toString() + ") : "
                                        + msg);
        } catch (Exception ex) {
            throw new FopImageException("Error creating FopImage object ("
                                        + "Error creating FopImage object ("
                                        + absoluteURL.toString() + ") : "
                                        + ex.getMessage());
        }
        if (!(imageInstance instanceof org.apache.fop.image.FopImage)) {
            throw new FopImageException("Error creating FopImage object ("
                                        + absoluteURL.toString() + ") : "
                                        + "class " + imageClass.getName()
                                        + " doesn't implement org.apache.fop.image.FopImage interface");
        }
        m_urlMap.put(href, imageInstance);
        return (FopImage)imageInstance;
    }


    /**
     * Determines the class name of the generic image handler
     * This should really come from a config file but we leave this
     * to some future time.
     */
    private static String getGenericImageClassName() {

        if (m_genericImageClassName == null) {
            try {
                //this will throw a NoClassDefFoundError if JAI is not installed
                Class.forName("org.apache.fop.image.JAIImage");
                m_genericImageClassName = "org.apache.fop.image.JAIImage";
            } catch (Throwable t) {
                MessageHandler.logln("JAI support was not installed (read: not "
                    + "present at build time). Trying to use Jimi instead");
                /* on any exception assume Jai is not present and use Jimi instead */
                m_genericImageClassName = "org.apache.fop.image.JimiImage";
            }
        }
        return m_genericImageClassName;
    }


    /**
     * Clear the image cache.
     */
    public static synchronized void resetCache() {
        m_urlMap.clear();
    }
}

