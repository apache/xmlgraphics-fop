/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Iterator;

// FOP
import org.apache.fop.image.analyser.ImageReaderFactory;
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.fo.FOUserAgent;

// Avalon
import org.apache.avalon.framework.logger.Logger;

/*
handle context: base dir, logger, caching

 */

/**
 * create FopImage objects (with a configuration file - not yet implemented).
 * @author Eric SCHAEFFER
 */
public class ImageFactory {
    private static ImageFactory factory = new ImageFactory();
    ImageCache cache = new ContextImageCache(true);

    private ImageFactory() {}

    public static ImageFactory getInstance() {
        return factory;
    }

    public static String getURL(String href) {
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
     */
    public FopImage getImage(String url, FOUserAgent context) {
        return cache.getImage(url, context);
    }

    /**
     * Release an image from the cache.
     * This can be used if the renderer has its own cache of
     * the image.
     * The image should then be put into the weak cache.
     */
    public void releaseImage(String url, FOUserAgent context) {
        cache.releaseImage(url, context);
    }

    /**
     * create an FopImage objects.
     * @param href image URL as a String
     * @return a new FopImage object
     * @exception java.net.MalformedURLException bad URL
     * @exception FopImageException an error occured during construction
     */
    protected static FopImage loadImage(String href, String baseURL,
                                        FOUserAgent ua) {
        Logger log = ua.getLogger();
        // Get the absolute URL
        URL absoluteURL = null;
        InputStream imgIS = null;
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
            imgIS = absoluteURL.openStream();
        } catch (MalformedURLException e_context) {
            log.error("Error with image URL: " + e_context.getMessage(), e_context);
            return null;
        }
        catch (Exception e) {
            // maybe relative
            URL context_url = null;
            if (baseURL == null) {
                log.error("Error with image URL: " + e.getMessage() + " and no base directory is specified");
                return null;
            }
            try {
                absoluteURL = new URL(baseURL + absoluteURL.getFile());
            } catch (MalformedURLException e_context) {
                // pb context url
                log.error( "Invalid Image URL - error on relative URL : " +
                           e_context.getMessage(), e_context);
                return null;
            }
        }

        // If not, check image type
        FopImage.ImageInfo imgInfo = null;
        try {
            if (imgIS == null) {
                imgIS = absoluteURL.openStream();
            }
            imgInfo = ImageReaderFactory.make(
                          absoluteURL.toExternalForm(), imgIS, ua);
        } catch (Exception e) {
            log.error("Error while recovering Image Informations (" +
                      absoluteURL.toString() + ") : " + e.getMessage(), e);
            return null;
        }
        finally { if (imgIS != null) {
                  try {
                          imgIS.close();
                  } catch (IOException e) {}
                  }
            } if (imgInfo == null) {
            log.error("No ImageReader for this type of image (" +
                      absoluteURL.toString() + ")");
            return null;
        }
        // Associate mime-type to FopImage class
        String imgMimeType = imgInfo.mimeType;
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
            imgClassName = "org.apache.fop.image.JimiImage";
            // imgClassName = "org.apache.fop.image.JAIImage";
        } else if ("image/tga".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.JimiImage";
            // imgClassName = "org.apache.fop.image.JAIImage";
        } else if ("image/tiff".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.JimiImage";
            // imgClassName = "org.apache.fop.image.JAIImage";
        } else if ("image/svg+xml".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.XMLImage";
        } else if ("text/xml".equals(imgMimeType)) {
            imgClassName = "org.apache.fop.image.XMLImage";
        }
        if (imgClassName == null) {
            log.error("Unsupported image type (" +
                      absoluteURL.toString() + ") : " + imgMimeType);
            return null;
        }

        // load the right image class
        // return new <FopImage implementing class>
        Object imageInstance = null;
        Class imageClass = null;
        try {
            imageClass = Class.forName(imgClassName);
            Class[] imageConstructorParameters = new Class[2];
            imageConstructorParameters[0] = java.net.URL.class;
            imageConstructorParameters[1] = org.apache.fop.image.FopImage.ImageInfo.class;
            Constructor imageConstructor =
              imageClass.getDeclaredConstructor(
                imageConstructorParameters);
            Object[] initArgs = new Object[2];
            initArgs[0] = absoluteURL;
            initArgs[1] = imgInfo;
            imageInstance = imageConstructor.newInstance(initArgs);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            String msg;
            if (t != null) {
                msg = t.getMessage();
            } else {
                msg = ex.getMessage();
            }
            log.error("Error creating FopImage object (" +
                      absoluteURL.toString() + ") : " + msg, (t == null) ? ex:t);
            return null;
        }
        catch (Exception ex) {
            log.error("Error creating FopImage object (" +
                      "Error creating FopImage object (" +
                      absoluteURL.toString() + ") : " + ex.getMessage(), ex);
            return null;
        }
        if (!(imageInstance instanceof org.apache.fop.image.FopImage)) {
            log.error("Error creating FopImage object (" +
                      absoluteURL.toString() + ") : " + "class " +
                      imageClass.getName() + " doesn't implement org.apache.fop.image.FopImage interface");
            return null;
        }
        return (FopImage) imageInstance;
    }

}

class BasicImageCache implements ImageCache {
    Set invalid = Collections.synchronizedSet(new HashSet());
    Map contextStore = Collections.synchronizedMap(new HashMap());

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
    boolean collective;
    Map contextStore = Collections.synchronizedMap(new HashMap());
    Set invalid = null;
    Map weakStore = null;

    public ContextImageCache(boolean col) {
        collective = col;
        if(collective) {
            weakStore = Collections.synchronizedMap(new WeakHashMap());
            invalid = Collections.synchronizedSet(new HashSet());
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
                if(con.invalid(url)) {
                    return null;
                }
                im = con.getImage(url);
            }
            if(im == null && collective) {
                for(Iterator iter = contextStore.values().iterator(); iter.hasNext(); ) {
                    Context c = (Context)iter.next();
                    if(c != con) {
                        im = c.getImage(url);
                        if(im != null) {
                            break;
                        }
                    }
                }
                if(im == null) {
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
            if(collective) {
                ImageLoader im = con.getImage(url);
                weakStore.put(url, im);
            }
            con.releaseImage(url);
        }
    }

    public void invalidateImage(String url, FOUserAgent context) {
        if(collective) {
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
            if(collective) {
                Map images = con.getImages();
                weakStore.putAll(images);
            }
            contextStore.remove(context);
        }
    }

    class Context {
        Map images = Collections.synchronizedMap(new HashMap());
        Set invalid = null;
        FOUserAgent userAgent;

        public Context(FOUserAgent ua, boolean inv) {
            userAgent = ua;
            if(inv) {
                invalid = Collections.synchronizedSet(new HashSet());
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

