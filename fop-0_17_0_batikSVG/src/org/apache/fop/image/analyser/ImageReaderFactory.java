/*
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

package org.apache.fop.image.analyser;

// Java
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

// FOP
import org.apache.fop.image.FopImageException;

/**
 * Factory for ImageReader objects.
 * @author Pankaj Narula
 * @version 1.0
 */
public class ImageReaderFactory {
	static protected Vector formats = null;

	/**
		* ImageReader maker.
		* @param in image input stream
		* @return ImageReader object
		* @exception FopImageException  an error occured during creation or
		*                               image type is not supported
		*/
	static public ImageReader Make(InputStream in)
		throws FopImageException {

		// need to use a config file and remove static methods
		formats = new Vector();
		formats.addElement(new JPEGReader());
		formats.addElement(new BMPReader());
		formats.addElement(new GIFReader());
		formats.addElement(new PNGReader());
		formats.addElement(new TIFFReader());
		formats.addElement(new SVGReader());
		//

		ImageReader reader;
		BufferedInputStream bis = new BufferedInputStream(in);
		Enumeration itr = formats.elements();
		try {
			while (itr.hasMoreElements()) {
				reader = (ImageReader) itr.nextElement();
				if (reader.verifySignature(bis)) {
					return reader;
				}
			}
		} catch (IOException ex) {
			throw new FopImageException(ex.getMessage());
		}
		return null;
	}
}

