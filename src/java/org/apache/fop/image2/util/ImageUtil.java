/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.image2.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.zip.GZIPInputStream;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import org.apache.fop.image2.ImageSource;

/**
 * Helper and convenience methods for working with the image package.
 */
public class ImageUtil {

    /**
     * Returns the InputStream of a Source object.
     * @param src the Source object
     * @return the InputStream (or null if there's not InputStream available)
     */
    public static InputStream getInputStream(Source src) {
        if (src instanceof StreamSource) {
            return ((StreamSource)src).getInputStream();
        } else if (src instanceof ImageSource) {
            return new ImageInputStreamAdapter(((ImageSource)src).getImageInputStream());
        } else {
            return null;
        }
    }

    /**
     * Returns the ImageInputStream of a Source object.
     * @param src the Source object
     * @return the ImageInputStream (or null if there's not ImageInputStream available)
     */
    public static ImageInputStream getImageInputStream(Source src) {
        if (src instanceof ImageSource) {
            return ((ImageSource)src).getImageInputStream();
        } else {
            return null;
        }
    }

    /**
     * Returns the InputStream of a Source object. This method throws an IllegalArgumentException
     * if there's no InputStream instance available from the Source object.
     * @param src the Source object
     * @return the InputStream
     */
    public static InputStream needInputStream(Source src) {
        InputStream in = getInputStream(src); 
        if (in != null) {
            return in;
        } else {
            throw new IllegalArgumentException("Source must be a StreamSource with an InputStream"
                    + " or an ImageSource");
        }
    }
    
    /**
     * Returns the ImageInputStream of a Source object. This method throws an
     * IllegalArgumentException if there's no ImageInputStream instance available from the
     * Source object.
     * @param src the Source object
     * @return the ImageInputStream
     */
    public static ImageInputStream needImageInputStream(Source src) {
        ImageInputStream in = getImageInputStream(src); 
        if (in != null) {
            return in;
        } else {
            throw new IllegalArgumentException("Source must be an ImageSource");
        }
    }
    
    /**
     * Indicates whether the Source object has an InputStream instance.
     * @param src the Source object
     * @return true if an InputStream is available
     */
    public static boolean hasInputStream(Source src) {
        if (src instanceof StreamSource) {
            InputStream in = ((StreamSource)src).getInputStream(); 
            return (in != null);
        } else if (src instanceof ImageSource) {
            return hasImageInputStream(src);
        } else {
            return false;
        }
    }

    /**
     * Indicates whether the Source object has an ImageInputStream instance.
     * @param src the Source object
     * @return true if an ImageInputStream is available
     */
    public static boolean hasImageInputStream(Source src) {
        if (src instanceof ImageSource) {
            ImageInputStream in = ((ImageSource)src).getImageInputStream(); 
            if (in != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Closes the InputStreams or ImageInputStreams of Source objects. Any exception occurring
     * while closing the stream is ignored.
     * @param src the Source object
     */
    public static void closeQuietly(Source src) {
        if (src instanceof StreamSource) {
            StreamSource streamSource = (StreamSource)src; 
            IOUtils.closeQuietly(streamSource.getInputStream());
            streamSource.setInputStream(null);
            IOUtils.closeQuietly(streamSource.getReader());
            streamSource.setReader(null);
        } else if (src instanceof ImageSource) {
            ImageSource imageSource = (ImageSource)src;
            if (imageSource.getImageInputStream() != null) {
                try {
                    imageSource.getImageInputStream().close();
                } catch (IOException ioe) {
                    //ignore
                }
                imageSource.setImageInputStream(null);
            }
        } else {
            throw new IllegalArgumentException("Source not supported!");
        }
    }
    
    /**
     * Decorates an ImageInputStream so the flush*() methods are ignored and have no effect.
     * The decoration is implemented using a dynamic proxy.
     * @param in the ImageInputStream
     * @return the decorated ImageInputStream
     */
    public static ImageInputStream ignoreFlushing(final ImageInputStream in) {
        return (ImageInputStream)Proxy.newProxyInstance(in.getClass().getClassLoader(),
                new Class[] {ImageInputStream.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        String methodName = method.getName();
                        //Ignore calls to flush*()
                        if (!methodName.startsWith("flush")) {
                            return method.invoke(in, args);
                        } else {
                            return null;
                        }
                    }
                });
    }
    
    /**
     * GZIP header magic number bytes, like found in a gzipped
     * files, which are encoded in Intel format (i.&#x2e;e&#x2e; little indian).
     */
    private static final byte[] GZIP_MAGIC = {(byte)0x1f, (byte)0x8b};

    /**
     * Indicates whether an InputStream is GZIP compressed. The InputStream must support
     * mark()/reset().
     * @param in the InputStream (must return true on markSupported())
     * @return true if the InputStream is GZIP compressed
     * @throws IOException in case of an I/O error
     */
    public static boolean isGZIPCompressed(InputStream in) throws IOException {
        if (!in.markSupported()) {
            throw new IllegalArgumentException("InputStream must support mark()!");
        }
        byte[] data = new byte[2];
        in.mark(2);
        in.read(data);
        in.reset();
        return ((data[0] == GZIP_MAGIC[0]) && (data[1] == GZIP_MAGIC[1]));
    }
    
    /**
     * Decorates an InputStream with a BufferedInputStream if it doesn't support mark()/reset().
     * @param in the InputStream
     * @return the decorated InputStream
     */
    public static InputStream decorateMarkSupported(InputStream in) {
        if (in.markSupported()) {
            return in;
        } else {
            return new java.io.BufferedInputStream(in);
        }
    }
    
    /**
     * Automatically decorates an InputStream so it is buffered. Furthermore, it makes sure
     * it is decorated with a GZIPInputStream if the stream is GZIP compressed.
     * @param in the InputStream
     * @return the decorated InputStream
     * @throws IOException in case of an I/O error
     */
    public static InputStream autoDecorateInputStream(InputStream in) throws IOException {
        in = decorateMarkSupported(in);
        if (isGZIPCompressed(in)) {
            return new GZIPInputStream(in);
        }
        return in;
    }
    
}
