/*
 * $Id: XSLTInputHandler.java,v 1.10 2003/02/27 10:13:05 jeremias Exp $
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
package org.apache.fop.apps;

// Imported java.io classes
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

// FOP
import org.apache.fop.tools.xslt.XSLTransform;

/**
 * XSLTInputHandler basically takes an XML file and transforms it with an XSLT
 * file and the resulting XSL-FO document is input for FOP.
 * @todo add URL constructor
 */
public class XSLTInputHandler extends InputHandler {

    private File xmlfile, xsltfile;
    private boolean useOldTransform = false;
    private boolean gotParser = false;

    /**
     * Main constructor
     * @param xmlfile XML file
     * @param xsltfile XSLT file
     */
    public XSLTInputHandler(File xmlfile, File xsltfile) {
        this.xmlfile = xmlfile;
        this.xsltfile = xsltfile;
    }

    /**
     * @see org.apache.fop.apps.InputHandler#getInputSource()
     */
    public InputSource getInputSource() {
        if (!gotParser) {
            throw new IllegalStateException("The method getParser() must be "
                    + "called and the parser used when using XSLTInputHandler");
        }
        if (useOldTransform) {
            try {
                java.io.Writer writer;
                java.io.Reader reader;
                File tmpFile = null;

                // create a Writer
                // the following is an ugly hack to allow processing of larger files
                // if xml file size is larger than 500 kb write the fo:file to disk
                if ((xmlfile.length()) > 500000) {
                    tmpFile = new File(xmlfile.getName() + ".fo.tmp");
                    writer = new java.io.FileWriter(tmpFile);
                } else {
                    writer = new java.io.StringWriter();
                }

                XSLTransform.transform(xmlfile.getCanonicalPath(),
                                       xsltfile.getCanonicalPath(), writer);

                writer.flush();
                writer.close();

                if (tmpFile != null) {
                    reader = new java.io.FileReader(tmpFile);
                } else {
                    // create a input source containing the xsl:fo file which can be fed to Fop
                    reader = new java.io.StringReader(writer.toString());
                }
                return new InputSource(reader);
            } catch (Exception ex) {
                ex.printStackTrace();
                /**@todo do proper logging of exceptions */
                return null;
            }
        } else {
            return fileInputSource(xmlfile);
        }

    }

    /**
     * This looks to see if the Trax api is supported and uses that to
     * get an XMLFilter. Otherwise, it falls back to using DOM documents
     * @return the created <code>XMLReader</code>
     * @throws FOPException if getting the parser fails
     * @see org.apache.fop.apps.InputHandler#getParser()
     */
    public XMLReader getParser() throws FOPException {
        gotParser = true;

        XMLReader result = null;
        try {
            // try trax first
            Class transformer =
                Class.forName("javax.xml.transform.Transformer");
            transformer =
                Class.forName("org.apache.fop.apps.TraxInputHandler");
            Class[] argTypes = {
                File.class, File.class
            };
            Method getFilterMethod = transformer.getMethod("getXMLFilter",
                    argTypes);
            File[] args = {
                xmlfile, xsltfile
            };
            Object obj = getFilterMethod.invoke(null, args);
            if (obj instanceof XMLReader) {
                result = (XMLReader)obj;
            }
        } catch (ClassNotFoundException ex) {
            throw new FOPException(ex);
        } catch (InvocationTargetException ex) {
            throw new FOPException(ex);
        } catch (IllegalAccessException ex) {
            throw new FOPException(ex);
        } catch (NoSuchMethodException ex) {
            throw new FOPException(ex);
        }
        // otherwise, use DOM documents via our XSLTransform tool class old style
        if (result == null) {
            useOldTransform = true;
            result = createParser();
        }
        return result;

    }

}

