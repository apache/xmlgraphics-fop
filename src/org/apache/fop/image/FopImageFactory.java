/*-- $Id$ --
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.
 */

package org.apache.fop.image;

// Java
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Constructor;
import java.util.Hashtable;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.image.analyser.ImageReaderFactory;
import org.apache.fop.image.analyser.ImageReader;

/**
 * create FopImage objects (with a configuration file - not yet implemented).
 * @author Eric SCHAEFFER
 */
public class FopImageFactory {

  private static Hashtable m_urlMap = new Hashtable();

  /**
   * create an FopImage objects.
   * @param href image URL as a String
   * @return a new FopImage object
   * @exception java.net.MalformedURLException bad URL
   * @exception FopImageException an error occured during construction
   */
  public static FopImage Make(String href)
    throws MalformedURLException, FopImageException {

    // Get the absolute URL
    URL absoluteURL = null;
    //		try {
    absoluteURL = new URL(href);
    /*
    		}
    		catch (MalformedURLException e) {
    			// maybe relative
    			URL context_url = null;
    			try {
    				context_url = new URL(); // how to get the context URL ?
    				try {
    					absoluteURL = new URL(context_url, ref);
    				}
    				catch (MalformedURLException e_abs) {
    					// not found
    					MessageHandler.errorln(
                "Invalid Image URL : " +
                e_abs.getMessage() +
                "(base URL " + context_url.toString() + ")"
                );
    					return null;
    				}
    			}
    			catch (MalformedURLException e_context) {
    				// pb context url
    				MessageHandler.errorln("Invalid Image URL - error on relative URL : " + e_context.getMessage());
    				return null;
    			}
    		}
    */

    // check if already created
    FopImage imageObject =
      (FopImage) m_urlMap.get(absoluteURL.toString());
    if (imageObject != null)
      return imageObject;

    // If not, check image type
    ImageReader imgReader = null;
    try {
      imgReader =
        ImageReaderFactory.Make(absoluteURL.openStream());
    } catch (Exception e) {
      throw new FopImageException(
        "Error while recovering Image Informations (" +
        absoluteURL.toString() + ") : " + e.getMessage());
    }
    if (imgReader == null)
      throw new FopImageException("No ImageReader for this type of image (" +
                                  absoluteURL.toString() + ")");
    // Associate mime-type to FopImage class
    String imgMimeType = imgReader.getMimeType();
    String imgClassName = null;
    if ("image/gif".equals(imgMimeType)) {
      imgClassName = "org.apache.fop.image.GifJpegImage";
    } else if ("image/jpeg".equals(imgMimeType)) {
      imgClassName = "org.apache.fop.image.GifJpegImage";
    } else if ("image/bmp".equals(imgMimeType)) {
      imgClassName = "org.apache.fop.image.BmpImage";
    } else if ("image/png".equals(imgMimeType)) {
      imgClassName = "org.apache.fop.image.JimiImage";
    } else if ("image/tga".equals(imgMimeType)) {
      imgClassName = "org.apache.fop.image.JimiImage";
    } else if ("image/tiff".equals(imgMimeType)) {
      imgClassName = "org.apache.fop.image.JimiImage";
    } else if ("image/svg-xml".equals(imgMimeType)) {
      imgClassName = "org.apache.fop.image.SVGImage";
    }
    if (imgClassName == null)
      throw new FopImageException("Unsupported image type (" +
                                  absoluteURL.toString() + ") : " + imgMimeType);

    // load the right image class
    // return new <FopImage implementing class>
    Object imageInstance = null;
    Class imageClass = null;
    try {
      imageClass = Class.forName(imgClassName);
      Class[] imageConstructorParameters = new Class[2];
      imageConstructorParameters[0] = Class.forName("java.net.URL");
      imageConstructorParameters[1] = Class.forName("org.apache.fop.image.analyser.ImageReader");
      Constructor imageConstructor =
        imageClass.getDeclaredConstructor(
          imageConstructorParameters);
      Object[] initArgs = new Object[2];
      initArgs[0] = absoluteURL;
      initArgs[1] = imgReader;
      imageInstance = imageConstructor.newInstance(initArgs);
    } catch (Exception ex) {
      throw new FopImageException(
        "Error creating FopImage object (" +
        absoluteURL.toString() + ") : " + ex.getMessage());
    }
    if (! (imageInstance instanceof org.apache.fop.image.FopImage)) {
      throw new FopImageException(
        "Error creating FopImage object (" +
        absoluteURL.toString() + ") : " + "class " +
        imageClass.getName() + " doesn't implement org.apache.fop.image.FopImage interface");
    }
    m_urlMap.put(absoluteURL.toString(), imageInstance);
    return (FopImage) imageInstance;
  }
}

