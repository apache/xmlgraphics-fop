/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.plan;

import java.io.*;
import org.w3c.dom.*;

import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        main.convert(args);
        System.exit(0);
    }

    public Main() {
    }

    public void convert(String[] params) {
        if (params.length != 2) {
            System.out.println("arguments: plan.xml output.svg");
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(params[0]);
            Document doc = createSVGDocument(fis);
            SVGTranscoder svgT = new SVGTranscoder();
            TranscoderInput input = new TranscoderInput(doc);
            Writer ostream = new FileWriter(params[1]);
            TranscoderOutput output = new TranscoderOutput(ostream);
            svgT.transcode(input, output);
            ostream.flush();
            ostream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Document createSVGDocument(InputStream is) {
        Document doc = null;

        Element svgRoot = null;
        try {
            //        DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
            //        String ns = GraphElementMapping.URI;
            //        doc = impl.createDocument(ns, "graph", null);
            doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().
                  newDocumentBuilder().parse(is);

            svgRoot = doc.getDocumentElement();

        } catch (Exception e) {
            e.printStackTrace();
        }
        PlanRenderer gr = new PlanRenderer();
        gr.setFontInfo("sansserif", 12);
        Document svgdoc = gr.createSVGDocument(doc);
        return svgdoc;
    }
}
