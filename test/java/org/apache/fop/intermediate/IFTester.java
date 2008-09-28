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

package org.apache.fop.intermediate;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.AreaTreeParser;
import org.apache.fop.area.RenderPagesModel;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.layoutengine.EvalCheck;
import org.apache.fop.layoutengine.TrueCheck;
import org.apache.fop.render.intermediate.IFRenderer;
import org.apache.fop.render.intermediate.IFSerializer;
import org.apache.fop.util.DelegatingContentHandler;

/**
 * Does tests on the intermediate format.
 */
public class IFTester {

    private static final Map IF_CHECK_CLASSES = new java.util.HashMap();

    static {
        IF_CHECK_CLASSES.put("true", TrueCheck.class);
        IF_CHECK_CLASSES.put("eval", EvalCheck.class);
    }

    private FopFactory fopFactory = FopFactory.newInstance();

    private SAXTransformerFactory tfactory
                = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    private File backupDir;

    /**
     * Main constructor
     * @param backupDir an optional directory in which to write the serialized
     *                  intermediate format file (may be null)
     */
    public IFTester(File backupDir) {
        this.backupDir = backupDir;
    }

    /**
     * Factory method to create IF checks from DOM elements.
     * @param el DOM element to create the check from
     * @return The newly create check
     */
    protected IFCheck createIFCheck(Element el) {
        String name = el.getTagName();
        Class clazz = (Class)IF_CHECK_CLASSES.get(name);
        if (clazz != null) {
            try {
                Constructor c = clazz.getDeclaredConstructor(new Class[] {Node.class});
                IFCheck instance = (IFCheck)c.newInstance(new Object[] {el});
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Error while instantiating check '"
                        + name + "': " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("No check class found: " + name);
        }
    }

    private Document createIF(Document areaTreeXML) throws TransformerException {
        try {
            FOUserAgent ua = fopFactory.newFOUserAgent();

            IFRenderer ifRenderer = new IFRenderer();
            ifRenderer.setUserAgent(ua);

            IFSerializer serializer = new IFSerializer();
            DOMResult result = new DOMResult();
            serializer.setResult(result);
            ifRenderer.setDocumentHandler(serializer);

            ua.setRendererOverride(ifRenderer);
            FontInfo fontInfo = new FontInfo();
            //Construct the AreaTreeModel that will received the individual pages
            final AreaTreeModel treeModel = new RenderPagesModel(ua,
                    null, fontInfo, null);

            //Iterate over all intermediate files
            AreaTreeParser parser = new AreaTreeParser();
            ContentHandler handler = parser.getContentHandler(treeModel, ua);

            DelegatingContentHandler proxy = new DelegatingContentHandler() {

                public void endDocument() throws SAXException {
                    super.endDocument();
                    //Signal the end of the processing.
                    //The renderer can finalize the target document.
                    treeModel.endDocument();
                }

            };
            proxy.setDelegateContentHandler(handler);

            Transformer transformer = tfactory.newTransformer();
            transformer.transform(new DOMSource(areaTreeXML), new SAXResult(proxy));

            return (Document)result.getNode();
        } catch (Exception e) {
            throw new TransformerException(
                    "Error while generating intermediate format file: " + e.getMessage(), e);
        }
    }

    /**
     * Runs the intermediate format checks.
     * @param testFile the original test file
     * @param checksRoot the root element containing the IF checks
     * @param areaTreeXML the area tree XML
     * @throws TransformerException if an error occurs while transforming the content
     */
    public void doIFChecks(File testFile, Element checksRoot, Document areaTreeXML)
                throws TransformerException {
        Document ifDocument = createIF(areaTreeXML);
        if (this.backupDir != null) {
            Transformer transformer = tfactory.newTransformer();
            Source src = new DOMSource(ifDocument);
            File targetFile = new File(this.backupDir, testFile.getName() + ".if.xml");
            Result res = new StreamResult(targetFile);
            transformer.transform(src, res);
        }

        //First create check before actually running them
        List checks = new java.util.ArrayList();
        NodeList nodes = checksRoot.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                checks.add(createIFCheck((Element)node));
            }
        }

        if (checks.size() == 0) {
            throw new RuntimeException("No checks are available!");
        }

        //Run the actual tests now that we know that the checks themselves are ok
        doIFChecks(checks, ifDocument);
    }

    private void doIFChecks(List checks, Document ifDocument) {
        Iterator i = checks.iterator();
        while (i.hasNext()) {
            IFCheck check = (IFCheck)i.next();
            check.check(ifDocument);
        }
    }

}
