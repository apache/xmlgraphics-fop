/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// FOP
import org.apache.fop.fo.FOUserAgent;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.render.Renderer;
import org.apache.fop.configuration.ConfigurationReader;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.tools.DocumentInputSource;
import org.apache.fop.tools.DocumentReader;

import org.apache.fop.render.pdf.PDFRenderer;

// Avalon
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

// DOM
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

// SAX
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

// Java
import java.io.*;
import java.util.*;

/**
 * Primary class that drives overall FOP process.
 * <P>
 * The simplest way to use this is to instantiate it with the
 * InputSource and OutputStream, then set the renderer desired, and
 * calling run();
 * <P>
 * Here is an example use of Driver which outputs PDF:
 *
 * <PRE>
 * Driver driver = new Driver(new InputSource (args[0]),
 * new FileOutputStream(args[1]));
 * driver.setRenderer(RENDER_PDF);
 * driver.run();
 * </PRE>
 * If neccessary, calling classes can call into the lower level
 * methods to setup and
 * render. Methods can be called to set the
 * Renderer to use, the (possibly multiple) ElementMapping(s) to
 * use and the OutputStream to use to output the results of the
 * rendering (where applicable). In the case of the Renderer and
 * ElementMapping(s), the Driver may be supplied either with the
 * object itself, or the name of the class, in which case Driver will
 * instantiate the class itself. The advantage of the latter is it
 * enables runtime determination of Renderer and ElementMapping(s).
 * <P>
 * Once the Driver is set up, the render method
 * is called. Depending on whether DOM or SAX is being used, the
 * invocation of the method is either render(Document) or
 * buildFOTree(Parser, InputSource) respectively.
 * <P>
 * A third possibility may be used to build the FO Tree, namely
 * calling getContentHandler() and firing the SAX events yourself.
 * <P>
 * Once the FO Tree is built, the format() and render() methods may be
 * called in that order.
 * <P>
 * Here is an example use of Driver which outputs to AWT:
 *
 * <PRE>
 * Driver driver = new Driver();
 * driver.setRenderer(new org.apache.fop.render.awt.AWTRenderer(translator));
 * driver.render(parser, fileInputSource(args[0]));
 * </PRE>
 */
public class Driver {

    /**
     * Render to PDF. OutputStream must be set
     */
    public static final int RENDER_PDF = 1;

    /**
     * Render to a GUI window. No OutputStream neccessary
     */
    public static final int RENDER_AWT = 2;

    /**
     * Render to MIF. OutputStream must be set
     */
    public static final int RENDER_MIF = 3;

    /**
     * Render to XML. OutputStream must be set
     */
    public static final int RENDER_XML = 4;

    /**
     * Render to PRINT. No OutputStream neccessary
     */
    public static final int RENDER_PRINT = 5;

    /**
     * Render to PCL. OutputStream must be set
     */
    public static final int RENDER_PCL = 6;

    /**
     * Render to Postscript. OutputStream must be set
     */
    public static final int RENDER_PS = 7;

    /**
     * Render to Text. OutputStream must be set
     */
    public static final int RENDER_TXT = 8;

    /**
     * Render to SVG. OutputStream must be set
     */
    public static final int RENDER_SVG = 9;

    /**
     * the FO tree builder
     */
    private FOTreeBuilder _treeBuilder;

    /**
     * the area tree that is the result of formatting the FO tree
     */
    private AreaTree _areaTree;

    /**
     * the renderer to use to output the area tree
     */
    private Renderer _renderer;

    /**
     * the source of the FO file
     */
    private InputSource _source;

    /**
     * the stream to use to output the results of the renderer
     */
    private OutputStream _stream;

    /**
     * The XML parser to use when building the FO tree
     */
    private XMLReader _reader;

    /**
     * If true, full error stacks are reported
     */
    private boolean _errorDump = false;

    /**
     * the system resources that FOP will use
     */
    private Logger log;
    private FOUserAgent userAgent = null;

    public static final String getParserClassName() {
        try { 
            return javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getXMLReader().getClass().getName();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            return null;
        } catch (org.xml.sax.SAXException e) {
            return null;
        }
    }

    /**
     * create a new Driver
     */
    public Driver() {
        _stream = null;
    }

    public Driver(InputSource source, OutputStream stream) {
        this();
        _source = source;
        _stream = stream;
    }

    public void initialize() {
        _stream = null;
        _treeBuilder = new FOTreeBuilder(); 
        _treeBuilder.setUserAgent(getUserAgent());
        setupDefaultMappings();
    }

    public void setUserAgent(FOUserAgent agent) {
        userAgent = agent;
    }

    private FOUserAgent getUserAgent() {
        if(userAgent == null) {
            userAgent = new FOUserAgent();
            userAgent.setLogger(getLogger());
            String base = org.apache.fop.configuration.Configuration.getStringValue("baseDir");
            userAgent.setBaseURL(base);
        }
        return userAgent;
    }

    public void setLogger(Logger logger) {
        log = logger;
    }

    private Logger getLogger() {
        if(log == null) {
	    log = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
	    log.error("Logger not set");
	}

        return log;
    }

    /**
     * Resets the Driver so it can be reused. Property and element
     * mappings are reset to defaults.
     * The output stream is cleared. The renderer is cleared.
     */
    public synchronized void reset() {
        _areaTree = null;
        _source = null;
        _stream = null;
        _reader = null;
        _treeBuilder.reset();
    }

    public boolean hasData() {
        return (_treeBuilder.hasData());
    }

    /**
     * Set the error dump option
     * @param dump if true, full stacks will be reported to the error log
     */
    public void setErrorDump(boolean dump) {
        _errorDump = dump;
    }

    /**
     * Set the OutputStream to use to output the result of the Renderer
     * (if applicable)
     * @param stream the stream to output the result of rendering to
     *
     */
    public void setOutputStream(OutputStream stream) {
        _stream = stream;
    }

    /**
     * Set the source for the FO document. This can be a normal SAX
     * InputSource, or an DocumentInputSource containing a DOM document.
     * @see DocumentInputSource
     */
    public void setInputSource(InputSource source) {
        _source = source;
    }

    /**
     * Sets the reader used when reading in the source. If not set,
     * this defaults to a basic SAX parser.
     */
    public void setXMLReader(XMLReader reader) {
        _reader = reader;
    }

    /**
     * Sets all the element and property list mappings to their default values.
     *
     */
    public void setupDefaultMappings() {
        addElementMapping("org.apache.fop.fo.FOElementMapping");
        addElementMapping("org.apache.fop.svg.SVGElementMapping");
        addElementMapping("org.apache.fop.extensions.ExtensionElementMapping");

        // add mappings from available services
        Enumeration providers =
            Service.providers(org.apache.fop.fo.ElementMapping.class);
        if (providers != null) {
            while (providers.hasMoreElements()) {
                String str = (String)providers.nextElement();
                try {
                    addElementMapping(str);
                } catch (IllegalArgumentException e) {}

            }
        }
    }

    /**
     * Set the rendering type to use. Must be one of
     * <ul>
     * <li>RENDER_PDF
     * <li>RENDER_AWT
     * <li>RENDER_MIF
     * <li>RENDER_XML
     * <li>RENDER_PCL
     * <li>RENDER_PS
     * <li>RENDER_TXT
     * <li>RENDER_SVG
     * </ul>
     * @param renderer the type of renderer to use
     */
    public void setRenderer(int renderer) throws IllegalArgumentException {
        switch (renderer) {
        case RENDER_PDF:
            setRenderer(new org.apache.fop.render.pdf.PDFRenderer());
            break;
        case RENDER_AWT:
            throw new IllegalArgumentException("Use renderer form of setRenderer() for AWT");
        case RENDER_PRINT:
            throw new IllegalArgumentException("Use renderer form of setRenderer() for PRINT");
        case RENDER_PCL:
            setRenderer(new org.apache.fop.render.pcl.PCLRenderer());
            break;
        case RENDER_PS:
            setRenderer(new org.apache.fop.render.ps.PSRenderer());
            break;
        case RENDER_TXT:
            setRenderer(new org.apache.fop.render.txt.TXTRenderer());
            break;
        case RENDER_MIF:
            setRenderer(new org.apache.fop.render.mif.MIFRenderer());
            break;
        case RENDER_XML:
            setRenderer(new org.apache.fop.render.xml.XMLRenderer());
            break;
        case RENDER_SVG:
            setRenderer(new org.apache.fop.render.svg.SVGRenderer());
            break;
        default:
            throw new IllegalArgumentException("Unknown renderer type");
        }

    }

    /**
     * Set the Renderer to use
     * @param renderer the renderer instance to use
     */
    public void setRenderer(Renderer renderer) {
        renderer.setLogger(getLogger());
        renderer.setUserAgent(getUserAgent());
        _renderer = renderer;
    }

    public Renderer getRenderer() {
        return _renderer;
    }

    /**
     * @deprecated use renderer.setProducer(version) + setRenderer(renderer) or just setRenderer(renderer_type) which will use the default producer string.
     * @see #setRenderer(int)
     * @see #setRenderer(Renderer)
     */
    public void setRenderer(String rendererClassName, String version) {
        setRenderer(rendererClassName);
    }

    /**
     * Set the class name of the Renderer to use as well as the
     * producer string for those renderers that can make use of it.
     * @param rendererClassName classname of the renderer to use such as
     * "org.apache.fop.render.pdf.PDFRenderer"
     * @exception IllegalArgumentException if the classname was invalid.
     * @see #setRenderer(int)
     */
    public void setRenderer(String rendererClassName)
    throws IllegalArgumentException {
        try {
            _renderer =
                (Renderer)Class.forName(rendererClassName).newInstance();
            _renderer.setProducer(Version.getVersion());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find "
                                               + rendererClassName);
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate "
                                               + rendererClassName);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access "
                                               + rendererClassName);
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException(rendererClassName
                                               + " is not a renderer");
        }
    }

    /**
     * Add the given element mapping.
     * An element mapping maps element names to Java classes.
     *
     * @param mapping the element mappingto add
     */
    public void addElementMapping(ElementMapping mapping) {
        mapping.addToBuilder(_treeBuilder);
    }

    /**
     * add the element mapping with the given class name
     */
    public void addElementMapping(String mappingClassName)
    throws IllegalArgumentException {
        try {
            ElementMapping mapping =
                (ElementMapping)Class.forName(mappingClassName).newInstance();
            addElementMapping(mapping);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find "
                                               + mappingClassName);
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate "
                                               + mappingClassName);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access "
                                               + mappingClassName);
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException(mappingClassName
                                               + " is not an ElementMapping");
        }
    }

    /**
     * Returns the tree builder (a SAX ContentHandler).
     *
     * Used in situations where SAX is used but not via a FOP-invoked
     * SAX parser. A good example is an XSLT engine that fires SAX
     * events but isn't a SAX Parser itself.
     */
    public ContentHandler getContentHandler() {
        StreamRenderer streamRenderer = new StreamRenderer(_stream, _renderer);
        streamRenderer.setLogger(getLogger());
        _treeBuilder.setLogger(getLogger());
        _treeBuilder.setUserAgent(getUserAgent());
        _treeBuilder.setStreamRenderer(streamRenderer);

        return _treeBuilder;
    }

    /**
     * Build the formatting object tree using the given SAX Parser and
     * SAX InputSource
     */
    public synchronized void render(XMLReader parser, InputSource source)
    throws FOPException {
        parser.setContentHandler(getContentHandler());
        try {
            parser.parse(source);
        } catch (SAXException e) {
            if (e.getException() instanceof FOPException) {
                throw (FOPException)e.getException();
            } else {
                throw new FOPException(e);
            }
        }
        catch (IOException e) {
            throw new FOPException(e);
        }
    }

    /**
     * Build the formatting object tree using the given DOM Document
     */
    public synchronized void render(Document document)
    throws FOPException {

        try {
            DocumentInputSource source = new DocumentInputSource(document);
            DocumentReader reader = new DocumentReader();
            reader.setContentHandler(getContentHandler());
            reader.parse(source);
        } catch (SAXException e) {
            throw new FOPException(e);
        }
        catch (IOException e) {
            throw new FOPException(e);
        }

    }

    /**
     * Dumps an error
     */
    public void dumpError(Exception e) {
        if (_errorDump) {
            Logger log = getLogger();
            if (e instanceof SAXException) {
                log.error("", e);
                if (((SAXException)e).getException() != null) {
                    log.error("", ((SAXException)e).getException());
                }
            } else if (e instanceof FOPException) {
                e.printStackTrace();
                if (((FOPException)e).getException() != null) {
                    log.error("", ((FOPException)e).getException());
                }
            } else {
                log.error("", e);
            }
        }
    }

    /* Set up the system buffers */

    public void setBufferFile(File bufferFile) {
    }

    /**
     * Runs the formatting and renderering process using the previously set
     * inputsource and outputstream
     */
    public synchronized void run() throws IOException, FOPException {
        if (_renderer == null) {
            setRenderer(RENDER_PDF);
        }

        if (_source == null) {
            throw new FOPException("InputSource is not set.");
        }

        if (_reader == null) {
            if (!(_source instanceof DocumentInputSource)) {
                _reader = ConfigurationReader.createParser();
            }
        }

        if (_source instanceof DocumentInputSource) {
            render(((DocumentInputSource)_source).getDocument());
        } else {
            render(_reader, _source);
        }
    }

}

// code stolen from org.apache.batik.util and modified slightly
// does what sun.misc.Service probably does, but it cannot be relied on.
// hopefully will be part of standard jdk sometime.

/**
 * This class loads services present in the class path.
 */
class Service {

    static Hashtable providerMap = new Hashtable();

    public static synchronized Enumeration providers(Class cls) {
        ClassLoader cl = cls.getClassLoader();
        // null if loaded by bootstrap class loader
        if(cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        String serviceFile = "META-INF/services/" + cls.getName();

        // System.out.println("File: " + serviceFile);

        Vector v = (Vector)providerMap.get(serviceFile);
        if (v != null)
            return v.elements();

        v = new Vector();
        providerMap.put(serviceFile, v);

        Enumeration e;
        try {
            e = cl.getResources(serviceFile);
        } catch (IOException ioe) {
            return v.elements();
        }

        while (e.hasMoreElements()) {
            try {
                java.net.URL u = (java.net.URL)e.nextElement();
                // System.out.println("URL: " + u);

                InputStream is = u.openStream();
                Reader r = new InputStreamReader(is, "UTF-8");
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();
                while (line != null) {
                    try {
                        // First strip any comment...
                        int idx = line.indexOf('#');
                        if (idx != -1)
                            line = line.substring(0, idx);

                        // Trim whitespace.
                        line = line.trim();

                        // If nothing left then loop around...
                        if (line.length() == 0) {
                            line = br.readLine();
                            continue;
                        }
                        // System.out.println("Line: " + line);

                        // Try and load the class
                        // Object obj = cl.loadClass(line).newInstance();
                        // stick it into our vector...
                        v.add(line);
                    } catch (Exception ex) {
                        // Just try the next line
                    }

                    line = br.readLine();
                }
            } catch (Exception ex) {
                // Just try the next file...
            }

        }
        return v.elements();
    }

}

