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

/* $Id$ */

package org.apache.fop.fo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.ElementMapping.Maker;
import org.apache.fop.fo.pagination.Root;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX Handler that passes parsed data to the various
 * FO objects, where they can be used either to build
 * an FO Tree, or used by Structure Renderers to build
 * other data structures.
 */
public class FOTreeBuilder extends DefaultHandler {

    /**
     * Table mapping element names to the makers of objects
     * representing formatting objects.
     */
    protected Map fobjTable = new java.util.HashMap();

    /**
     * Set of mapped namespaces.
     */
    protected Set namespaces = new java.util.HashSet();

    /**
     * Current formatting object being handled
     */
    protected FONode currentFObj = null;

    /**
     * The root of the formatting object tree
     */
    protected Root rootFObj = null;

    /**
     * The class that handles formatting and rendering to a stream
     * (mark-fop@inomial.com)
     */
    private FOInputHandler foInputHandler;

    private FOUserAgent userAgent;

    /** The FOTreeControl object managing the FO Tree that is being built */
    public FOTreeControl foTreeControl;

    /** The SAX locator object maneging the line and column counters */
    private Locator locator; 
    
    /**
     * Default constructor
     */
    public FOTreeBuilder() {
        setupDefaultMappings();
    }

    private Logger getLogger() {
        return userAgent.getLogger();
    }

    /**
     * Sets the user agent
     * @param ua the user agent
     */
    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    private FOUserAgent getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the structure handler to receive events.
     * @param foih FOInputHandler instance
     */
    public void setFOInputHandler(FOInputHandler foih) {
        this.foInputHandler = foih;
    }

    /**
     * Sets all the element and property list mappings to their default values.
     *
     */
    private void setupDefaultMappings() {
        addElementMapping("org.apache.fop.fo.FOElementMapping");
        addElementMapping("org.apache.fop.fo.extensions.svg.SVGElementMapping");
        addElementMapping("org.apache.fop.fo.extensions.svg.BatikExtensionElementMapping");
        addElementMapping("org.apache.fop.fo.extensions.ExtensionElementMapping");

        // add mappings from available services
        Iterator providers =
            Service.providers(ElementMapping.class);
        if (providers != null) {
            while (providers.hasNext()) {
                String str = (String)providers.next();
                try {
                    addElementMapping(str);
                } catch (IllegalArgumentException e) {
                    getLogger().warn("Error while adding element mapping", e);
                }

            }
        }
    }

    /**
     * Add the given element mapping.
     * An element mapping maps element names to Java classes.
     *
     * @param mapping the element mappingto add
     */
    public void addElementMapping(ElementMapping mapping) {
        this.fobjTable.put(mapping.getNamespaceURI(), mapping.getTable());
        this.namespaces.add(mapping.getNamespaceURI().intern());
    }

    /**
     * Add the element mapping with the given class name.
     * @param mappingClassName the class name representing the element mapping.
     * @throws IllegalArgumentException if there was not such element mapping.
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
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate "
                                               + mappingClassName);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access "
                                               + mappingClassName);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(mappingClassName
                                               + " is not an ElementMapping");
        }
    }

    /**
     * SAX Handler for locator
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
    
    /**
     * SAX Handler for characters
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] data, int start, int length) {
        if (currentFObj != null) {
            currentFObj.addCharacters(data, start, start + length, locator);
        }
    }

    /**
     * SAX Handler for the end of an element
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String rawName)
                throws SAXException {
        currentFObj.end();
        currentFObj = currentFObj.getParent();
    }

    /**
     * SAX Handler for the start of the document
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        rootFObj = null;    // allows FOTreeBuilder to be reused
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Building formatting object tree");
        }
        foInputHandler.startDocument();
    }

    /**
     * SAX Handler for the end of the document
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        rootFObj = null;
        currentFObj = null;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Parsing of document complete");
        }
        foInputHandler.endDocument();
    }

    /**
     * SAX Handler for the start of an element
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI, String localName, String rawName,
                             Attributes attlist) throws SAXException {
        /* the formatting object started */
        FONode fobj;

        /* the maker for the formatting object started */
        ElementMapping.Maker fobjMaker = findFOMaker(namespaceURI, localName);

        try {
            fobj = fobjMaker.make(currentFObj);
            fobj.setName(localName);
            fobj.setLocation(locator);
            fobj.handleAttrs(attlist);
        } catch (FOPException e) {
            throw new SAXException(e);
        }

        if (rootFObj == null) {
            if (!fobj.getName().equals("fo:root")) {
                throw new SAXException(new FOPException("Root element must"
                                                        + " be fo:root, not "
                                                        + fobj.getName()));
            }
            rootFObj = (Root)fobj;
            rootFObj.setFOTreeControl(foTreeControl);
        } else {
            currentFObj.addChild(fobj);
        }

        currentFObj = fobj;
    }

    /**
     * Finds the Maker used to create FO objects of a particular type
     * @param namespaceURI URI for the namespace of the element
     * @param localName name of the Element
     * @return the ElementMapping.Maker that can create an FO object for this element
     */
    public Maker findFOMaker(String namespaceURI, String localName) {
      Map table = (Map)fobjTable.get(namespaceURI);
      Maker fobjMaker = null;
      if (table != null) {
          fobjMaker = (ElementMapping.Maker)table.get(localName);
          // try default
          if (fobjMaker == null) {
              fobjMaker = (ElementMapping.Maker)table.get(ElementMapping.DEFAULT);
          }
      }

      if (fobjMaker == null) {
          if (getLogger().isWarnEnabled()) {
              getLogger().warn("Unknown formatting object " + namespaceURI + "^" + localName);
          }
          if (namespaces.contains(namespaceURI.intern())) {
              // fall back
              fobjMaker = new Unknown.Maker();
          } else {
              fobjMaker = new UnknownXMLObj.Maker(namespaceURI);
          }
      }
      return fobjMaker;
    }

    /**
     * Resets this object for another run.
     */
    public void reset() {
        currentFObj = null;
        rootFObj = null;
        foInputHandler = null;
    }

    /**
     * Indicates if data has been processed.
     * @return True if data has been processed
     */
    public boolean hasData() {
        return (rootFObj != null);
    }

}

// code stolen from org.apache.batik.util and modified slightly
// does what sun.misc.Service probably does, but it cannot be relied on.
// hopefully will be part of standard jdk sometime.

/**
 * This class loads services present in the class path.
 */
class Service {

    private static Map providerMap = new java.util.Hashtable();

    public static synchronized Iterator providers(Class cls) {
        ClassLoader cl = cls.getClassLoader();
        // null if loaded by bootstrap class loader
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        String serviceFile = "META-INF/services/" + cls.getName();

        // getLogger().debug("File: " + serviceFile);

        List lst = (List)providerMap.get(serviceFile);
        if (lst != null) {
            return lst.iterator();
        }

        lst = new java.util.Vector();
        providerMap.put(serviceFile, lst);

        Enumeration e;
        try {
            e = cl.getResources(serviceFile);
        } catch (IOException ioe) {
            return lst.iterator();
        }

        while (e.hasMoreElements()) {
            try {
                java.net.URL u = (java.net.URL)e.nextElement();
                //getLogger().debug("URL: " + u);

                InputStream is = u.openStream();
                Reader r = new InputStreamReader(is, "UTF-8");
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();
                while (line != null) {
                    try {
                        // First strip any comment...
                        int idx = line.indexOf('#');
                        if (idx != -1) {
                            line = line.substring(0, idx);
                        }

                        // Trim whitespace.
                        line = line.trim();

                        // If nothing left then loop around...
                        if (line.length() == 0) {
                            line = br.readLine();
                            continue;
                        }
                        // getLogger().debug("Line: " + line);

                        // Try and load the class
                        // Object obj = cl.loadClass(line).newInstance();
                        // stick it into our vector...
                        lst.add(line);
                    } catch (Exception ex) {
                        // Just try the next line
                    }

                    line = br.readLine();
                }
            } catch (Exception ex) {
                // Just try the next file...
            }

        }
        return lst.iterator();
    }

}

