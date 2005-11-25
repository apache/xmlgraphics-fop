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
 
package org.apache.fop.plan;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.image.analyser.XMLReader;
import org.apache.fop.image.FopImage;
import org.w3c.dom.Document;

import java.util.HashMap;

/**
 * This class provides the element mapping for FOP.
 */
public class PlanElementMapping extends ElementMapping {

    /** Plan Namespace */
    public static final String NAMESPACE = "http://xmlgraphics.apache.org/fop/plan"; 

    public PlanElementMapping() {
        this.namespaceURI = NAMESPACE;
    }

    protected void initialize() {
        if (foObjs == null) {
            foObjs = new java.util.HashMap();
            foObjs.put("plan", new PE());
            foObjs.put(DEFAULT, new PlanMaker());

            XMLReader.setConverter(this.namespaceURI, new PlanConverter());
        }
    }

    static class PlanMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PlanObj(parent);
        }
    }

    static class PE extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PlanElement(parent);
        }
    }

    static class PlanConverter implements XMLReader.Converter {
        public FopImage.ImageInfo convert(Document doc) {
            try {
                PlanRenderer pr = new PlanRenderer();
                pr.setFontInfo("Helvetica", 12);
                FopImage.ImageInfo info = new FopImage.ImageInfo();
                info.data = pr.createSVGDocument(doc);
                info.width = (int)pr.getWidth();
                info.height = (int)pr.getHeight();
                info.mimeType = "image/svg+xml";
                info.str = "http://www.w3.org/2000/svg";
    
                return info;
            } catch (Throwable t) {
                /**@todo Log this properly! */
            }
            return null;
        }
    }

}
