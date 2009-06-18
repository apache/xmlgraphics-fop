/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.mathml;

import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.image.analyser.XMLReader;
import org.apache.fop.image.FopImage;
import org.w3c.dom.Document;

import java.util.HashMap;

import net.sourceforge.jeuclid.MathBase;
import net.sourceforge.jeuclid.DOMMathBuilder;

public class MathMLElementMapping implements ElementMapping {

    public static final String URI = "http://www.w3.org/1998/Math/MathML";

    private static HashMap foObjs = null;

    private static synchronized void setupMathML() {
        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("math", new ME());
            foObjs.put(DEFAULT, new MathMLMaker());

            XMLReader.setConverter(URI, new MathMLConverter());
        }
    }

    public void addToBuilder(FOTreeBuilder builder) {
        setupMathML();
        builder.addMapping(URI, foObjs);
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
            }
            return null;

        }
    }

}
