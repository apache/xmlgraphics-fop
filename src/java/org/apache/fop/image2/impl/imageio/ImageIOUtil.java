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

package org.apache.fop.image2.impl.imageio;

import javax.imageio.metadata.IIOMetadata;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.fop.image2.ImageSize;
import org.apache.fop.util.UnitConv;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper and convenience methods for ImageIO.
 */
public class ImageIOUtil {

    /**
     * Extracts the resolution information from the standard ImageIO metadata.
     * @param iiometa the metadata provided by ImageIO
     * @param size the image size object
     */
    public static void extractResolution(IIOMetadata iiometa, ImageSize size) {
        if (iiometa != null && iiometa.isStandardMetadataFormatSupported()) {
            Element metanode = (Element)iiometa.getAsTree("javax_imageio_1.0");
            Element dim = getChild(metanode, "Dimension");
            if (dim != null) {
                Element child;
                double dpiHorz = size.getDpiHorizontal();
                double dpiVert = size.getDpiVertical();
                child = getChild(dim, "HorizontalPixelSize");
                if (child != null) {
                    dpiHorz = UnitConv.IN2MM
                            / Float.parseFloat(child.getAttribute("value"));
                }
                child = getChild(dim, "VerticalPixelSize");
                if (child != null) {
                    dpiVert = UnitConv.IN2MM
                            / Float.parseFloat(child.getAttribute("value"));
                }
                size.setResolution(dpiHorz, dpiVert);
                size.calcSizeFromPixels();
            }
        }
    }
    
    private static Element getChild(Element el, String name) {
        NodeList nodes = el.getElementsByTagName(name);
        if (nodes.getLength() > 0) {
            return (Element)nodes.item(0);
        } else {
            return null;
        }
    }
    
    /**
     * Dumps the content of an IIOMetadata instance to System.out.
     * @param iiometa the metadata
     */
    public static void dumpMetadataToSystemOut(IIOMetadata iiometa) {
        String[] metanames = iiometa.getMetadataFormatNames();
        for (int j = 0; j < metanames.length; j++) {
            System.out.println("--->" + metanames[j]);
            dumpNodeToSystemOut(iiometa.getAsTree(metanames[j]));
        } 
    }
    
    /**
     * Serializes a W3C DOM node to a String and dumps it to System.out.
     * @param node a W3C DOM node
     */
    private static void dumpNodeToSystemOut(Node node) {
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            Source src = new DOMSource(node);
            Result res = new StreamResult(System.out);
            trans.transform(src, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
