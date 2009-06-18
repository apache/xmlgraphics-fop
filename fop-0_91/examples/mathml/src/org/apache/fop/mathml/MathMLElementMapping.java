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
 
package org.apache.fop.mathml;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.image.analyser.XMLReader;
import org.apache.fop.image.FopImage;
import org.w3c.dom.Document;

import java.util.HashMap;

import net.sourceforge.jeuclid.MathBase;
import net.sourceforge.jeuclid.DOMMathBuilder;

/**
 * This class provides the element mapping for FOP.
 */
public class MathMLElementMapping extends ElementMapping {

    /** MathML Namespace */
    public static final String NAMESPACE = "http://www.w3.org/1998/Math/MathML"; 

    public MathMLElementMapping() {
        this.namespaceURI = NAMESPACE;
    }

    protected void initialize() {
        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("math", new ME());
            foObjs.put(DEFAULT, new MathMLMaker());

            XMLReader.setConverter(this.namespaceURI, new MathMLConverter());
        }
    }

    static class MathMLMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new MathMLObj(parent);
        }
    }

    static class ME extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new MathMLElement(parent);
        }
    }

    static class MathMLConverter implements XMLReader.Converter {
        public FopImage.ImageInfo convert(Document doc) {
            try {
                FopImage.ImageInfo info = new FopImage.ImageInfo();
                String fontname = "Helvetica";
                int fontstyle = 0;
                int inlinefontstyle = 0;
                int inlinefontsize = 12;
                int displayfontsize = 12;

                MathBase base = new MathBase(
                                  (new DOMMathBuilder(doc)).getMathRootElement(),
                                  fontname, fontstyle, inlinefontsize,
                                  displayfontsize);

                base.setDebug(false);

                info.data = MathMLElement.createSVG(base);

                info.width = base.getWidth();
                info.height = base.getHeight();

                info.mimeType = "image/svg+xml";
                info.str = "http://www.w3.org/2000/svg";

                return info;
            } catch (Throwable t) {
                /**@todo log that properly */
            }
            return null;

        }
    }

}
