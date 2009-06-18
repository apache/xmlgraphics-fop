/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

import java.lang.reflect.*;


// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

// Imported java.io classes
import java.io.*;

// FOP
import org.apache.fop.tools.xslt.XSLTransform;

/*
add url constructer
*/

/**
 * XSLTInputHandler basically takes an xmlfile and transforms it with an xsltfile
 * and the resulting xsl:fo document is input for Fop.
 */
public class XSLTInputHandler extends InputHandler {

    File xmlfile, xsltfile;
    boolean useOldTransform = false;
    boolean gotParser = false;

    public XSLTInputHandler(File xmlfile, File xsltfile) {
        this.xmlfile = xmlfile;
        this.xsltfile = xsltfile;
    }

    /**
     * overwrites the method of the super class to return the xmlfile
     */
    public InputSource getInputSource() {
        if(!gotParser) {
            throw new IllegalStateException("The method getParser() must be called and the parser used when using XSLTInputHandler");
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
                    writer = new FileWriter(tmpFile);
                } else {
                    writer = new StringWriter();
                }

                XSLTransform.transform(xmlfile.getCanonicalPath(),
                                       xsltfile.getCanonicalPath(), writer);

                writer.flush();
                writer.close();

                if (tmpFile != null) {
                    reader = new FileReader(tmpFile);
                } else {
                    // create a input source containing the xsl:fo file which can be fed to Fop
                    reader = new StringReader(writer.toString());
                }
                return new InputSource(reader);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            return fileInputSource(xmlfile);
        }

    }

    /**
     * This looks to see if the Trax api is supported and uses that to
     * get an XMLFilter. Otherwise, it falls back to using DOM documents
     *
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

