/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.plan;

import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.image.analyser.XMLReader;
import org.apache.fop.image.FopImage;
import org.w3c.dom.Document;

import java.util.HashMap;

public class PlanElementMapping implements ElementMapping {

    public static final String URI = "http://xml.apache.org/fop/plan";

    private static HashMap foObjs = null;

    private static synchronized void setupPlan() {
        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("plan", new PE());
            foObjs.put(DEFAULT, new PlanMaker());

            XMLReader.setConverter(URI, new PlanConverter());
        }
    }

    public void addToBuilder(FOTreeBuilder builder) {
        setupPlan();
        builder.addMapping(URI, foObjs);
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
            } catch(Throwable t) {
            }
            return null;

        }
    }

}
