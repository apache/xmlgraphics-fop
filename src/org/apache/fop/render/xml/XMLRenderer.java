/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.xml;

// FOP
import org.apache.fop.svg.*;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.image.ImageArea;
import org.apache.fop.area.*;
import org.apache.fop.area.inline.*;
import org.apache.fop.pdf.*;
import org.apache.fop.fo.properties.LeaderPattern;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.apps.FOPException;

import org.apache.log.Logger;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * Renderer that renders areas to XML for debugging purposes.
 */
public class XMLRenderer extends AbstractRenderer {
boolean startedSequence = false;

    public void setProducer(String producer) {
    }

    /**
     * indentation to use for pretty-printing the XML
     */
    protected int indent = 0;

    /**
     * the application producing the XML
     */
    protected String producer;

    /**
     * the writer used to output the XML
     */
    protected PrintWriter writer;

    /**
     * options
     */
    private boolean consistentOutput = false;

    public XMLRenderer() {}

    /**
     * write out spaces to make indent
     */
    protected void writeIndent() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < this.indent; i++) {
            s = s.append("  ");
        }
        this.writer.write(s.toString());
    }

    /**
     * write out an element
     *
     * @param element the full text of the element including tags
     */
    protected void writeElement(String element) {
        writeIndent();
        this.writer.write(element + "\n");
    }

    /**
     * write out an empty-element-tag
     *
     * @param tag the text of the tag
     */
    protected void writeEmptyElementTag(String tag) {
        writeIndent();
        this.writer.write(tag + "\n");
    }

    /**
     * write out an end tag
     *
     * @param tag the text of the tag
     */
    protected void writeEndTag(String tag) {
        this.indent--;
        writeIndent();
        this.writer.write(tag + "\n");
    }

    /**
     * write out a start tag
     *
     * @param tag the text of the tag
     */
    protected void writeStartTag(String tag) {
        writeIndent();
        this.writer.write(tag + "\n");
        this.indent++;
    }

    /**
     * set up the font info
     *
     * @param fontInfo the font info object to set up
     */
    public void setupFontInfo(FontInfo fontInfo) {

        /* use PDF's font setup to get PDF metrics */
        org.apache.fop.render.pdf.FontSetup.setup(fontInfo);
    }

    private boolean isCoarseXml() {
        return ((Boolean)options.get("fineDetail")).booleanValue();
    }

    /**
    */
    public void startRenderer(OutputStream outputStream)
    throws IOException {
        log.debug("rendering areas to XML");
        this.writer = new PrintWriter(outputStream);
        this.writer.write( "<?xml version=\"1.0\"?>\n<!-- produced by " +
                           this.producer + " -->\n");
        writeStartTag("<areaTree>");
    }

    /**
    */
    public void stopRenderer()
    throws IOException {
        writeEndTag("</pageSequence>");
        writeEndTag("</areaTree>");
        this.writer.flush();
        log.debug("written out XML");
    }

    public void renderPage(PageViewport page)
    throws IOException, FOPException {
        writeStartTag("<pageViewport>");
        writeStartTag("<page>");
super.renderPage(page);
        writeEndTag("</page>");
        writeEndTag("</pageViewport>");
    }

    public void startPageSequence(Title seqTitle) {
if(startedSequence) {
        writeEndTag("</pageSequence>");
}
startedSequence = true;
        writeStartTag("<pageSequence>");
if(seqTitle != null) {
        writeStartTag("<title>");
        List children = seqTitle.getInlineAreas();

        for (int count = 0; count < children.size(); count++) {
            InlineArea inline = (InlineArea) children.get(count);
            inline.render(this);
        }

        writeEndTag("</title>");
}
    }

    protected void renderRegionViewport(RegionViewport port) {
        if(port != null) {
        writeStartTag("<regionViewport>");
        Region region = port.getRegion();
        if (region.getRegionClass() == Region.BEFORE) {
        writeStartTag("<regionBefore>");
            renderRegion(region);
        writeEndTag("</regionBefore>");
        } else if (region.getRegionClass() == Region.START) {
        writeStartTag("<regionStart>");
            renderRegion(region);
        writeEndTag("</regionStart>");
        } else if (region.getRegionClass() == Region.BODY) {
        writeStartTag("<regionBody>");
            renderBodyRegion((BodyRegion)region);
        writeEndTag("</regionBody>");
        } else if (region.getRegionClass() == Region.END) {
        writeStartTag("<regionEnd>");
            renderRegion(region);
        writeEndTag("</regionEnd>");
        } else if (region.getRegionClass() == Region.AFTER) {
        writeStartTag("<regionAfter>");
            renderRegion(region);
        writeEndTag("</regionAfter>");
        }
        writeEndTag("</regionViewport>");
}


}

    protected void renderBeforeFloat(BeforeFloat bf) {
        writeStartTag("<beforeFloat>");
super.renderBeforeFloat(bf);
        writeEndTag("</beforeFloat>");
}

    protected void renderFootnote(Footnote footnote) {
        writeStartTag("<footnote>");
super.renderFootnote(footnote);
        writeEndTag("</footnote>");
}

    protected void renderMainReference(MainReference mr) {
        writeStartTag("<mainReference>");
super.renderMainReference(mr);
        writeEndTag("</mainReference>");
}

    protected void renderBlock(Block block) {
        writeStartTag("<block>");
super.renderBlock(block);
        writeEndTag("</block>");
    }

    protected void renderLineArea(LineArea line) {
        writeStartTag("<lineArea>");
super.renderLineArea(line);
        writeEndTag("</lineArea>");
    }

    public void renderCharacter(org.apache.fop.area.inline.Character ch) {    
writeElement("<char>" + ch.getChar() + "</char>");
    }    

    public void renderInlineSpace(Space space) {
writeElement("<space width=\"" + space.getWidth() + "\"/>");
    }

}
