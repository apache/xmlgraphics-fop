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

package org.apache.fop.render.iform;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BookmarkData;
import org.apache.fop.area.CTM;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.Trait.Background;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.xml.AbstractXMLRenderer;
import org.xml.sax.SAXException;

public class IFRenderer extends AbstractXMLRenderer {

    /** logging instance */
    protected static Log log = LogFactory.getLog("IFRenderer");

    /** XML MIME type */
    public static final String IF_MIME_TYPE = MimeConstants.MIME_FOP_IF;
    private boolean pageSeqStarted = false;

    private String currentText;

    private Area parentArea;
    
    /**
     * Main constructor
     */
    public IFRenderer() {
        context = new RendererContext(this, IF_MIME_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    public void startRenderer(OutputStream outputStream)
                throws IOException {
        log.debug("Rendering areas to intermediate format XML");
        super.startRenderer(outputStream);
        if (userAgent.getProducer() != null) {
            comment("Produced by " + userAgent.getProducer());
        }
        startElement("document");
        startElement("content");
        startElement("svg");
    }

    /** {@inheritDoc} */
    public void startPageSequence(LineArea seqTitle) {
        if (pageSeqStarted) {
            endElement("pageSet");            
        }
        pageSeqStarted = true;
        startElement("pageSet");
    }

    /**
     * {@inheritDoc}
     */
    public void stopRenderer() throws IOException {
        if (pageSeqStarted) {
            endElement("pageSet");            
        }
        endElement("svg");
        endElement("content");
        endElement("document");
        super.stopRenderer();
        log.debug("Written out intermediate format XML");
    }

//    /**
//     * {@inheritDoc}
//     */
//    protected void renderFlow(NormalFlow flow) {
//        log.debug("renderFlow() " + flow);
//        super.renderFlow(flow);
//    }
//    
    /**
     * {@inheritDoc}
     */
    protected void renderBlock(Block block) {
        log.debug("renderBlock() " + block);
        addTraitAttributes(block);
        startElement("g", atts);
        this.parentArea = block;
        super.renderBlock(block);
        this.parentArea = null;
        endElement("g");
    }
        
    /**
     * Renders an fo:foreing-object.
     * @param fo the foreign object
     * @param pos the position of the foreign object
     * @see org.apache.fop.render.AbstractRenderer#renderForeignObject(ForeignObject, Rectangle2D)
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        log.debug("renderForeignObject() fo=" + fo + ", pos=" + pos);
        super.renderForeignObject(fo, pos);
    }
    
    /**
     * {@inheritDoc}
     */
    public void renderPage(PageViewport page) throws IOException, FOPException {
        log.debug("renderPage() " + page);
        atts.clear();
        startElement("page");
        handlePageExtensionAttachments(page);
        super.renderPage(page);
        endElement("page");
    }
         
    private boolean parentHasTrait(Integer traitKey, Object trait) {
        return ((parentArea != null
                && parentArea.hasTrait(traitKey)
                && parentArea.getTrait(traitKey).equals(trait)));        
    }
    
    /**
     * Adds attributes from traits of an Area.
     * @param area Area to extract traits from
     */
    protected void addTraitAttributes(Area area) {
        Object trait = area.getTrait(Trait.FONT);
        if (trait != null && !parentHasTrait(Trait.FONT, trait)) {
            FontTriplet fontTriplet = (FontTriplet)trait;
            addAttribute("font-family", fontTriplet.getName());
            int weight = fontTriplet.getWeight();
            if (weight != Font.WEIGHT_NORMAL) {
                addAttribute("font-weight", weight);
            }
            String style = fontTriplet.getStyle();
            if (!Font.STYLE_NORMAL.equals(style)) {
                addAttribute("text-decoration", style);
            }
        }                
        trait = area.getTrait(Trait.FONT_SIZE);
        if (trait != null && !parentHasTrait(Trait.FONT_SIZE, trait)) {
            addAttribute("font-size", ((Integer)trait).intValue() / 1000);
        }
        trait = area.getTrait(Trait.COLOR);
        if (trait != null && !parentHasTrait(Trait.COLOR, trait)) {
            addColorAttribute("stroke", (Color)trait);
        }
        trait = area.getTrait(Trait.BACKGROUND);
        if (trait != null && !parentHasTrait(Trait.BACKGROUND, trait)) {
            addColorAttribute("fill", ((Background)trait).getColor());
        }
    }

    private void addColorAttribute(String attrName, Color col) {
        ColorSpace colSpace = col.getColorSpace();
        int colSpaceType = colSpace.getType();
        StringBuffer colStr = new StringBuffer();
        if (colSpace != null) {
            if (colSpaceType == ColorSpace.TYPE_RGB) {
                colStr.append("rgb(");
            } else if (colSpaceType == ColorSpace.TYPE_CMYK) {
                colStr.append("icc-color(myCMYK,");
            } else if (colSpaceType == ColorSpace.TYPE_GRAY) {
                colStr.append("icc-color(myGRAY,");
            }
            float[] colComp = col.getColorComponents(null);
            for (int i = 0; i < colComp.length; i++) {
                colStr.append((int)(colComp[i] * 255));
                colStr.append(",");
            }
            colStr.replace(colStr.length() - 1, colStr.length(), ")");
        }
        addAttribute(attrName, colStr.toString());
    }
    
    /**
     * {@inheritDoc}
     */
    protected void renderText(TextArea text) {
        log.debug("renderText() " + text);
        atts.clear();
        this.currentText = "";
        addAttribute("x", text.getIPD());
        addAttribute("y", text.getAllocBPD());
        addTraitAttributes(text);
        startElement("text", atts);
        super.renderText(text);
        try {
            handler.characters(currentText.toCharArray(), 0, currentText.length());
        } catch (SAXException e) {
            handleSAXException(e);
        }
        this.currentText = null;
//        addAttribute("font-family", "Helvetica");
        endElement("text");
    }
    
    /**
     * {@inheritDoc}
     */
    protected void renderWord(WordArea word) {
        log.debug("renderWord() " + word);
        super.renderWord(word);
        this.currentText += word.getWord();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void renderSpace(SpaceArea space) {
        log.debug("renderSpace() " + space);
        super.renderSpace(space);
        this.currentText += space.getSpace();
    }
    
    /**
     * {@inheritDoc}
     */
    public void renderImage(Image image, Rectangle2D pos) {
        log.debug("renderImage() image=" + image + ", pos=" + pos);
        super.renderImage(image, pos);
    }
    
    protected void handleExtensionAttachments(List attachments) {
        log.debug("handleExtensionAttachments() " + attachments);
        if (attachments != null && attachments.size() > 0) {
        }
    }
    
    protected void endVParea() {
        log.debug("endVParea()");
    }

    protected void renderInlineAreaBackAndBorders(InlineArea area) {
        log.debug("renderInlineAreaBackAndBorders() " + area);
    }

    protected void startVParea(CTM ctm, Rectangle2D clippingRect) {
        log.debug("startVParea() ctm=" + ctm + ", rect=" + clippingRect);                
    }

    protected void renderBookmarkTree(BookmarkData odi) {
        log.debug("renderBookmarkTree() odi=" + odi);                        
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return IF_MIME_TYPE;
    }
}
