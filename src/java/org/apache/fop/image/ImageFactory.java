/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

/* $Id: ImageFactory.java,v 1.7 2004/05/12 23:19:52 gmazza Exp $ */

package org.apache.fop.image;

// Java
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// FOP
import org.apache.fop.image.analyser.ImageReaderFactory;
import org.apache.fop.apps.FOUserAgent;


/**
 * Create FopImage objects (with a configuration file - not yet implemented).
 * @author Eric SCHAEFFER
 */
public class ImageFactory {

    /**
     * logging instance
     */
    protected static Log log = LogFactory.getLog(FopImage.class);

    private static ImageFactory factory = new ImageFactory();

    private ImageCache cache = new ContextImageCache(true);

    private ImageFactory() {
    }

    /**
     * Get static image factory instance.
     *
     * @return the image factory instance
     */
    public static ImageFactory getInstance() {
        return factory;
    }

    /**
     * Get the url string from a wrapped url.
     *
     * @param href the input wrapped url
     * @return the raw url
     */
    public static String getURL(String href) {
        /*
         * According to section 5.11 a <uri-specification> is:
         * "url(" + URI + ")"
         * according to 7.28.7 a <uri-specification> is:
         * URI
         * So handle both.
         */
        href = href.trim();
        if (href.startsWith("url(") && (href.indexOf(")") != -1)) {
            href = href.substring(4, href.indexOf(")")).trim();
            if (href.startsWith("'") && href.endsWith("'")) {
                href = href.substring(1, href.length() - 1);
            } else if (href.startsWith("\"") && href.endsWith("\"")) {
                href = href.substring(1, href.length() - 1);
            }
        } else {
            // warn
        }
        return href;
    }

    /**
     * Get the image from the cache or load.
     * If this returns null then the image could not be loaded
     * due to an error. Messages should be logged.
     * Before calling this the getURL(url) must be used.
     *
     * @param url the url for the image
     * @param context the user agent context
     * @return the fop image instance
     */
    public FopImage getImage(String url, FOUserAgent context) {
        return cache.getImage(url, context);
    }

    /**
     * Release an image from the cache.
     * This can be used if the renderer has its own cache of
     * the image.
     * The image should then be put into the weak cache.
     *
     * @param url the url for the image
     * @param context the user agent context
     */
    public void releaseImage(String url, FOUserAgent context) {
        cache.releaseImage(url, context);
    }

    /**
     * Release the context and all images in the context.
     *
     * @param context the context to remove
     */
    public void removeContext(FOUserAgent context) {
        cache.removeContext(context);
    }

    /**
     * Create an FopImage objects.
     * @param href the url for the image
     * @param ua the user agent context
     * @return the fop image instance
     */
    public static FopImage loadImage(String href, FOUserAgent ua) {

        InputStream in = openStream(href, ua);

        if (in == null) {
            return null;
        }

        // If not, check image type
        FopImage.ImageInfo imgInfo = null;
        try {
            imgInfo = ImageReaderFactory.make(
                          href, in, ua);
        } catch (Exception e) {
            log.error("Error while recovering image information ("
                    + href + ") : " + e.getMessage(), e);
            return null;
        }
        if (imgInfo == null) {
            try {
                in.close();
                in = null;
            } catch (Exception e) {
                log.debug("Error closing the InputStream for the image", e);
            }
            log.error("No ImageReader for this type of image ("
                    + href + ")");
            return null;
        }
        // Associate mime-type to FopImage class
        String imgMimeType = imgInfo.mimeType;
        String imgClassName = getImageClassName(imgMimeType);
        if (imgClassName == null) {
            log.error("Unsupported image type ("
                    + href + "): " + imgMimeType);
            return null;
        }

        // load the right image class
        // return new <FopImage implementing class>
        Object imageInstance = null;
        Class imageClass = null;
        try {
            imageClass = Class.forName(imgClassName);
            Class[] imageConstructorParameters = new Class[1];
            imageConstructorParameters[0] = org.apache.fop.image.FopImage.ImageInfo.class;
            Constructor imageConstructor =
              imageClass.getDeclaredConstructor(
                imageConstructorParameters);
            Object[] initArgs = new Object[1];
            initArgs[0] = imgInfo;
            imageInstance = imageConstructor.newInstance(initArgs);
        } catch (ClassNotFoundException cnfe) {
            log.error("Class " + imgClassName + " not found. Check that Jimi/JAI is in classpath");
            return null;
        } catch (java.lang.reflect.InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            String msg;
            if (t != null) {
                msg = t.getMessage();
            } else {
                msg = ex.getMessage();
            }
            log.error("Error creating FopImage object ("
                    + href + "): " + msg, (t == null) ? ex : t);
            return null;
        } catch (Exception ex) {
            log.error("Error creating FopImage object ("
                    + href + "): " + ex.getMessage(), ex);
            return null;
        }
        if (!(imageInstance instanceof org.apache.fop.image.FopImage)) {
            log.error("Error creating FopImage object ("
                    + href + "): " + "class "
                    + imageClass.getName()
                    + " doesn't implement org.apache.fop.image.FopImage interface");
            return null;
        }
        return (FopImage) imageInstance;
    }

    /**
     * Create an FopImage objects.
     * @param href image URL as a String
     * @param ua user agent
     * @return a new FopImage object
     */
    protected static InputStream openStream(String href, FOUserAgent ua) {

        // Get the absolute URL
        URL absoluteURL = null;
        InputStream in = null;

        try {
            in = ua.getStream(href);
        } catch (IOException ioe) {
            log.error("Error while opening stream for ("
                    + href + "): " + ioe.getMessage(), ioe);
            return null;
        }
        if (in == null) {
            try {
                // try url as complete first, this can cause
                // a problem with relative uri's if there is an
                // image relative to where fop is run and relative
                // to the base dir of the document
                try {
                    absoluteURL = new URL(href);
                } catch (MalformedURLException mue) {
                    // if the href contains only a path then file is assumed
                    absoluteURL = new URL("file:" + href);
                }
                in = absoluteURL.openStream();
            } catch (MalformedURLException mfue) {
                log.error("Error with image URL: " + mfue.getMessage(), mfue);
                return null;
            } catch (Exception e) {
                // maybe relative
                if (ua.getBaseURL() == null) {
                    log.error("Error with image URL: " + e.getMessage()
                            + " and no base URL is specified", e);
                    return null;
                }
                try {
                    absoluteURL = new URL(ua.getBaseURL() + absoluteURL.getFile());
                } catch (MalformedURLException e_context) {
                    // pb context url
                    log.error("Invalid Image URL - error on relative URL: "
                            + e_context.getMessage(), e_context);
                    return null;
                }
            }
        } /* if (in == null) */

        try {
            if (in == null && absoluteURL != null) {
                in = absoluteURL.openStream();
            }
            if (in == null) {
                log.error("Could not resolve URI for image: " + href);
                return null;
            }

            //Decorate the InputStream with a BufferedInputStream
            return new java.io.BufferedInputStream(in);
        } catch (Exception e) {
            log.error("Error while opening stream for ("
                    + href + "): " + e.getMessage(), e);
            return null;
        }
    }

    private static String getImageClassName(String imgMimeType) {
        String imgClassName = null;
        if ("image/gif".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.GifImage";
            // imgClassName = "org.apache.fop.image.JAIImage";
        } else if ("image/jpeg".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.JpegImage";
            // imgClassName = "org.apache.fop.image.JAIImage";
        } else if ("image/bmp".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.BmpImage";
            // imgClassName = "org.apache.fop.image.JAIImage";
        } else if ("image/eps".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.EPSImage";
        } else if ("image/png".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.PNGImage";
            // imgClassName = "org.apache.fop.image.JAIImage";
        } else if ("image/tga".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.JimiImage";
            // imgClassName = "org.apache.fop.image.JAIImage";
        } else if ("image/tiff".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.TIFFImage";
            // imgClassName = "org.apache.fop.image.JAIImage";
        } else if ("image/svg+xml".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.XMLImage";
        } else if ("text/xml".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.XMLImage";
        }
        return imgClassName;
    }
}

/**
 * Basic image cache.
 * This keeps track of invalid images.
 */
class BasicImageCache implements ImageCache {

    private Set invalid = Collections.synchronizedSet(new java.util.HashSet());
    private Map contextStore = Collections.synchronizedMap(new java.util.HashMap());

    public FopImage getImage(String url, FOUserAgent context) {
        if (invalid.contains(url)) {
            return null;
        }
        return null;
    }

    public void releaseImage(String url, FOUserAgent context) {
        // do nothing
    }

    public void invalidateImage(String url, FOUserAgent context) {
        // cap size of invalid list
        if (invalid.size() > 100) {
            invalid.clear();
        }
        invalid.add(url);
    }

    public void removeContext(FOUserAgent context) {
        // do nothing
    }
}

/**
 * This is the context image cache.
 * This caches images on the basis of the given context.
 * Common images in different contexts are currently not handled.
 * There are two possiblities, each context handles its own images
 * and renderers can cache information or images are shared and
 * all information is retained.
 * Once a context is removed then all images are placed into a
 * weak hashmap so they may be garbage collected.
 */
class ContextImageCache implements ImageCache {

    // if this cache is collective then images can be shared
    // among contexts, this implies that the base directory
    // is either the same or does not effect the images being
    // loaded
    private boolean collective;
    private Map contextStore = Collections.synchronizedMap(new java.util.HashMap());
    private Set invalid = null;
    private Map weakStore = null;

    public ContextImageCache(boolean col) {
        collective = col;
        if (collective) {
            weakStore = Collections.synchronizedMap(new java.util.WeakHashMap());
            invalid = Collections.synchronizedSet(new java.util.HashSet());
        }
    }

    // sync around lookups and puts
    // another sync around load for a particular image
    public FopImage getImage(String url, FOUserAgent context) {
        ImageLoader im = null;
        // this protects the finding or creating of a new
        // ImageLoader for multi threads
        synchronized (this) {
            if (collective && invalid.contains(url)) {
                return null;
            }
            Context con = (Context) contextStore.get(context);
            if (con == null) {
                con = new Context(context, collective);
                contextStore.put(context, con);
            } else {
                if (con.invalid(url)) {
                    return null;
                }
                im = con.getImage(url);
            }
            if (im == null && collective) {
                Iterator i = contextStore.values().iterator();
                while (i.hasNext()) {
                    Context c = (Context)i.next();
                    if (c != con) {
                        im = c.getImage(url);
                        if (im != null) {
                            break;
                        }
                    }
                }
                if (im == null) {
                    im = (ImageLoader) weakStore.get(url);
                }
            }

            if (im != null) {
                con.putImage(url, im);
            } else {
                im = con.getImage(url, this);
            }
        }

        // the ImageLoader is synchronized so images with the
        // same url will not be loaded at the same time
        if (im != null) {
            return im.loadImage();
        }
        return null;
    }

    public void releaseImage(String url, FOUserAgent context) {
        Context con = (Context) contextStore.get(context);
        if (con != null) {
            if (collective) {
                ImageLoader im = con.getImage(url);
                weakStore.put(url, im);
            }
            con.releaseImage(url);
        }
    }

    public void invalidateImage(String url, FOUserAgent context) {
        if (collective) {
            // cap size of invalid list
            if (invalid.size() > 100) {
                invalid.clear();
            }
            invalid.add(url);
        }
        Context con = (Context) contextStore.get(context);
        if (con != null) {
            con.invalidateImage(url);
        }
    }

    public void removeContext(FOUserAgent context) {
        Context con = (Context) contextStore.get(context);
        if (con != null) {
            if (collective) {
                Map images = con.getImages();
                weakStore.putAll(images);
            }
            contextStore.remove(context);
        }
    }

    class Context {
        private Map images = Collections.synchronizedMap(new java.util.HashMap());
        private Set invalid = null;
        private FOUserAgent userAgent;

        public Context(FOUserAgent ua, boolean inv) {
            userAgent = ua;
            if (inv) {
                invalid = Collections.synchronizedSet(new java.util.HashSet());
            }
        }

        public ImageLoader getImage(String url, ImageCache c) {
            if (images.containsKey(url)) {
                return (ImageLoader) images.get(url);
            }
            ImageLoader loader = new ImageLoader(url, c, userAgent);
            images.put(url, loader);
            return loader;
        }

        public void putImage(String url, ImageLoader image) {
            images.put(url, image);
        }

        public ImageLoader getImage(String url) {
            return (ImageLoader) images.get(url);
        }

        public void releaseImage(String url) {
            images.remove(url);
        }

        public Map getImages() {
            return images;
        }

        public void invalidateImage(String url) {
            invalid.add(url);
        }

        public boolean invalid(String url) {
            return invalid.contains(url);
        }

    }

}

