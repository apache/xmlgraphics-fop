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
//Author:       Eric SCHAEFFER
//Description:  create FopImage objects (with a configuration file)


package org.apache.fop.image;

// Java
import java.io.IOException;
import org.apache.fop.messaging.MessageHandler;
import java.net.*;
import java.lang.reflect.*;
import java.util.Hashtable;

public class FopImageFactory {

	private static Hashtable m_urlMap = new Hashtable();

	public static FopImage Make(String href) throws MalformedURLException, FopImageException {

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
					MessageHandler.errorln("Invalid Image URL : " + e_abs.getMessage() + "(base URL " + context_url.toString() + ")");
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
		FopImage imageObject = (FopImage) m_urlMap.get(absoluteURL.toString());
		if (imageObject != null) return imageObject;

		// If not, check the content type and create a new one

		String contentType = null;
		// try to get content type by URL
		try {
			URLConnection url_conn = absoluteURL.openConnection();
			contentType = url_conn.getContentType();
		} catch (IOException e) {
			throw new FopImageException("Error while recovering Image Type (" + absoluteURL.toString() + ") : " + e.getMessage());
		}

		// try get content type by extension if the first method failed
		if (contentType == null) {
			String stringURL = absoluteURL.toString();
			int extensionStart = stringURL.lastIndexOf(".");
			if ( ( extensionStart > 0 ) && ( extensionStart < stringURL.length() - 1 ) ) {
				String extensionURL = stringURL.substring(extensionStart + 1, stringURL.length());

				// BETTER : use the configuration file to associate an extension to a mime-type

				if ( 	extensionURL.equalsIgnoreCase("gif") ) {
					contentType = "image/gif";
				} else if (	( extensionURL.equalsIgnoreCase("jpeg") ) || 
								( extensionURL.equalsIgnoreCase("jpg") ) || 
								( extensionURL.equalsIgnoreCase("jpe") ) ) {
					contentType = "image/jpeg";
				} else if (	extensionURL.equalsIgnoreCase("png") ) {
					contentType = "image/png";
				} else if ( extensionURL.equalsIgnoreCase("tga") ) {
					contentType = "image/tga";
				} else if ( extensionURL.equalsIgnoreCase("dib") ) {
					contentType = "image/dib";
				} else if ( extensionURL.equalsIgnoreCase("ddb") ) {
					contentType = "image/ddb";
				} else if ( extensionURL.equalsIgnoreCase("bmp") ) {
					contentType = "image/bmp";
				} else if ( extensionURL.equalsIgnoreCase("pict") ) {
					contentType = "image/pict";
				} else if ( extensionURL.equalsIgnoreCase("psd") ) {
					contentType = "image/jpeg";
				} else if ( extensionURL.equalsIgnoreCase("ras") ) {
					contentType = "image/cmu-raster";
				} else if (	( extensionURL.equalsIgnoreCase("tiff") ) || 
								( extensionURL.equalsIgnoreCase("tif") ) ) {
					contentType = "image/tiff";
				} else if ( extensionURL.equalsIgnoreCase("xbm") ) {
					contentType = "image/xbm";
				} else if ( extensionURL.equalsIgnoreCase("xpm") ) {
					contentType = "image/xpm";
				} else if ( extensionURL.equalsIgnoreCase("ico") ) {
					contentType = "image/ico";
				} else if ( extensionURL.equalsIgnoreCase("cur") ) {
					contentType = "image/cur";
				} else if ( extensionURL.equalsIgnoreCase("pcx") ) {
					contentType = "image/pcx";
				}
			}
		}

		// if content type is still unknown
		if (contentType == null) {
			throw new FopImageException("Unknown image type (" + absoluteURL.toString() + ")");
		}

		// load the right image class (configuration file)
		// ...
		// return new <FopImage implementing class>
		// ...
		Object imageInstance = null;
		Class imageClass = null;
		try {
//			imageClass = Class.forName("org.apache.fop.image.JimiImage"); //ClassNotFoundException
			imageClass = Class.forName("org.apache.fop.image.GifJpegImage"); //ClassNotFoundException
			Class[] imageConstructorParameters = new Class[1];
			imageConstructorParameters[0] = Class.forName("java.net.URL");
			Constructor imageConstructor = imageClass.getDeclaredConstructor(imageConstructorParameters); //NoSuchMethodException, SecurityException
			Object[] initArgs = new Object[1];
			initArgs[0] = absoluteURL;
			imageInstance = imageConstructor.newInstance(initArgs); // InstanciationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
		} catch (ClassNotFoundException classex) {
//			throw new FopImageException("class " + "org.apache.fop.image.JimiImage" + " not found");
			throw new FopImageException("class " + "org.apache.fop.image.GifJpegImage" + " not found");
/*
		} catch (FopImageException fopex) {
			throw new FopImageException(fopex.getMessage());
*/
		} catch (Exception ex) {
			throw new FopImageException("class " + imageClass.getName() + " doesn't implement org.apache.fop.image.FopImage interface : " + ex.getMessage());
		}
		if (! (imageInstance instanceof org.apache.fop.image.FopImage)) {
			throw new FopImageException("class " + imageClass.getName() + " doesn't implement org.apache.fop.image.FopImage interface");
		}
		m_urlMap.put(absoluteURL.toString(), imageInstance);
		return (FopImage) imageInstance;

		// if no corresponding image class
//		throw new FopImageException(contentType + " not supported (" + absoluteURL.toString() + ")");
	}
}
