/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// FOP
import org.apache.fop.image.analyser.ImageReaderFactory;
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.configuration.Configuration;

// Java
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * create FopImage objects (with a configuration file - not yet implemented).
 * @author Eric SCHAEFFER
 */
public class FopImageFactory {

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
                absoluteURL = new URL(baseURL, absoluteURL.getFile());
            } catch (MalformedURLException e_context) {
                // pb context url
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
            imgClassName = getGenericImageClassName();
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
                Class.forName("org.apache.fop.image.JAIImage");
                m_genericImageClassName = "org.apache.fop.image.JAIImage";
            } catch (Exception ex) {
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

