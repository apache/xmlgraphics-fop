/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;
import org.apache.fop.fo.FOUserAgent;
import org.apache.fop.image.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.pdf.*;
import org.apache.fop.image.*;
import org.apache.fop.extensions.*;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.render.pdf.fonts.LazyFont;

import org.apache.fop.area.*;
import org.apache.fop.area.inline.*;
import org.apache.fop.area.inline.Character;
import org.apache.fop.layout.FontState;

import org.w3c.dom.Document;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;

/*
TODO:

viewport clipping
word rendering and optimistion
pdf state optimisation
line and border
leader
background pattern
user agent xml (svg) rendering
orientation
writing mode
text decoration

 */

/**
 * Renderer that renders areas to PDF
 *
 */
public class PDFRenderer extends PrintRenderer {
    public static final String mimeType = "application/pdf";

    /**
     * the PDF Document being created
     */
    protected PDFDocument pdfDoc;

    // map of pages using the PageViewport as the key
    // this is used for prepared pages that cannot be immediately
    // rendered
    protected HashMap pages = null;

    // page references are stored using the PageViewport as the key
    // when a reference is made the PageViewport is used
    // for pdf this means we need the pdf page reference
    protected HashMap pageReferences = new HashMap();

    protected String producer;

    protected OutputStream ostream;

    /**
     * the /Resources object of the PDF document being created
     */
    protected PDFResources pdfResources;

    /**
     * the current stream to add PDF commands to
     */
    PDFStream currentStream;

    /**
     * the current annotation list to add annotations to
     */
    PDFAnnotList currentAnnotList;

    /**
     * the current page to add annotations to
     */
    PDFPage currentPage;

    // drawing state
    PDFColor currentColor;
    String currentFontName = "";
    int currentFontSize = 0;
    int pageHeight;

    /**
     * true if a TJ command is left to be written
     */
    boolean textOpen = false;

    /**
     * the previous Y coordinate of the last word written.
     * Used to decide if we can draw the next word on the same line.
     */
    int prevWordY = 0;

    /**
     * the previous X coordinate of the last word written.
     * used to calculate how much space between two words
     */
    int prevWordX = 0;

    /**
     * The width of the previous word. Used to calculate space between
     */
    int prevWordWidth = 0;

    /**
     * reusable word area string buffer to reduce memory usage
     */
    private StringBuffer _wordAreaPDF = new StringBuffer();

    /**
     * create the PDF renderer
     */
    public PDFRenderer() {
    }

    /**
     * set the PDF document's producer
     *
     * @param producer string indicating application producing PDF
     */
    public void setProducer(String prod) {
        producer = prod;
    }

    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
        PDFXMLHandler xmlHandler = new PDFXMLHandler();
        //userAgent.setDefaultXMLHandler(mimeType, xmlHandler);
        String svg = "http://www.w3.org/2000/svg";
        userAgent.addXMLHandler(mimeType, svg, xmlHandler);
    }

    public void startRenderer(OutputStream stream) throws IOException {
        ostream = stream;
        this.pdfDoc = new PDFDocument();
        this.pdfDoc.setProducer(producer);
        pdfDoc.outputHeader(stream);
    }

    public void stopRenderer() throws IOException {
        FontSetup.addToResources(this.pdfDoc, fontInfo);
        pdfDoc.outputTrailer(ostream);

        this.pdfDoc = null;
        ostream = null;
    }

    public boolean supportsOutOfOrder() {
        return true;
    }

    /**
     * The pdf page is prepared by making the page.
     * The page is made in the pdf document without any contents
     * and then stored to add the contents later.
     * The page objects is stored using the area tree PageViewport
     * as a key.
     */
    public void preparePage(PageViewport page) {
        this.pdfResources = this.pdfDoc.getResources();
    
        Rectangle2D bounds = page.getViewArea();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        currentPage = this.pdfDoc.makePage(this.pdfResources,
                                           (int) Math.round(w / 1000), (int) Math.round(h / 1000));
        if(pages == null) {
            pages = new HashMap();
        }
        pages.put(page, currentPage);
        pageReferences.put(page, currentPage.referencePDF());
    }

    /**
     * This method creates a pdf stream for the current page
     * uses it as the contents of a new page. The page is written
     * immediately to the output stream.
     */
    public void renderPage(PageViewport page) throws IOException,
    FOPException {
        if(pages != null && (currentPage = (PDFPage)pages.get(page)) != null) {
            pages.remove(page);
        } else {
            this.pdfResources = this.pdfDoc.getResources();
            Rectangle2D bounds = page.getViewArea();
            double w = bounds.getWidth();
            double h = bounds.getHeight();
            pageHeight = (int)h;
            currentPage = this.pdfDoc.makePage(this.pdfResources, 
                                           (int) Math.round(w / 1000), (int) Math.round(h / 1000));
            pageReferences.put(page, currentPage.referencePDF());
        }
        currentStream = this.pdfDoc.makeStream();
	// Transform origin at top left to origin at bottom left
	currentStream.add("1 0 0 -1 0 " +
			  (int) Math.round(pageHeight / 1000) + " cm\n");
        //currentStream.add("BT\n");

        Page p = page.getPage();
        renderPageAreas(p);

        //currentStream.add("ET\n");

        currentPage.setContents(currentStream);
        this.pdfDoc.addPage(currentPage);
        this.pdfDoc.output(ostream);
    }


    protected void startVParea(CTM ctm) {
	// Set the given CTM in the graphics state
	currentStream.add("q\n");
	// multiply with current CTM
	currentStream.add(ctm.toPDFctm() + " cm\n");
	// Set clip?
  currentStream.add("BT\n");
    }

    protected void endVParea() {
  currentStream.add("ET\n");
	currentStream.add("Q\n");
    }

    protected void renderRegion(RegionReference region) {
	// Draw a rectangle so we can see it!
	// x=0,y=0,w=ipd,h=bpd
	super.renderRegion(region);
    }

    protected void renderLineArea(LineArea line) {
	super.renderLineArea(line);
	closeText();
    }

    public void renderCharacter(Character ch) {

        super.renderCharacter(ch);
    }

    public void renderWord(Word word) {
            StringBuffer pdf = new StringBuffer();

            FontState fs = null;

            List props = word.getTraitList();
            for(int count = 0; count < props.size(); count++) {
                Trait pro = (Trait)props.get(count);
                if(pro.propType == Trait.FONT_STATE) {
                    fs = (FontState)pro.data;
                }
            }
            String name = fs.getFontName();
            int size = fs.getFontSize();

            // This assumes that *all* CIDFonts use a /ToUnicode mapping
            Font f = (Font)fs.getFontInfo().getFonts().get(name);
            boolean useMultiByte = f.isMultiByte();

            // String startText = useMultiByte ? "<FEFF" : "(";
            String startText = useMultiByte ? "<" : "(";
            String endText = useMultiByte ? "> " : ") ";

            updateFont(name, size, pdf);
            updateColor(true, pdf);

            int rx = currentBlockIPPosition;
            // int bl = pageHeight - currentBPPosition;
            int bl = currentBPPosition + word.getOffset();

            // Set letterSpacing
            //float ls = fs.getLetterSpacing() / this.currentFontSize;
            //pdf.append(ls).append(" Tc\n");

            if (!textOpen || bl != prevWordY) {
                closeText();

                pdf.append("1 0 0 -1 " + (rx / 1000f) + " " + (bl / 1000f)
                           + " Tm [" + startText);
                prevWordY = bl;
                textOpen = true;
            } else {
                // express the space between words in thousandths of an em
                int space = prevWordX - rx + prevWordWidth;
                float emDiff = (float)space / (float)currentFontSize * 1000f;
                // this prevents a problem in Acrobat Reader and other viewers
                // where large numbers cause text to disappear or default to
                // a limit
                if (emDiff < -33000) {
                    closeText();

                    pdf.append("1 0 0 1 " + (rx / 1000f) + " " + (bl / 1000f)
                               + " Tm [" + startText);
                    textOpen = true;
                } else {
                    pdf.append(Float.toString(emDiff));
                    pdf.append(" ");
                    pdf.append(startText);
                }
            }
            prevWordWidth = word.getWidth();
            prevWordX = rx;

        String s = word.getWord();

        escapeText(s, fs, useMultiByte, pdf);
        pdf.append(endText);

        currentStream.add(pdf.toString());

        super.renderWord(word);
    }

    public void escapeText(String s, FontState fs, boolean useMultiByte, StringBuffer pdf) {
        String startText = useMultiByte ? "<" : "(";
        String endText = useMultiByte ? "> " : ") ";

        boolean kerningAvailable = false;
        HashMap kerning = null;
        kerning = fs.getKerning();
        if (kerning != null &&!kerning.isEmpty()) {
            kerningAvailable = true;
        }

        int l = s.length();

        for (int i = 0; i < l; i++) {
            char ch = fs.mapChar(s.charAt(i));

            if (!useMultiByte) {
                if (ch > 127) {
                    pdf.append("\\");
                    pdf.append(Integer.toOctalString((int)ch));
                } else {
                    switch (ch) {
                        case '(':
                        case ')':
                        case '\\':
                            pdf.append("\\");
                            break;
                    }
                    pdf.append(ch);
                }
            } else {
                pdf.append(getUnicodeString(ch));
            }

            if (kerningAvailable && (i + 1) < l) {
                addKerning(pdf, (new Integer((int)ch)),
                           (new Integer((int)fs.mapChar(s.charAt(i + 1)))),
                           kerning, startText, endText);
            }
        }
    }

    /**
     * Convert a char to a multibyte hex representation
     */
    private String getUnicodeString(char c) {
        StringBuffer buf = new StringBuffer(4);

        byte[] uniBytes = null;
        try {
            char[] a = {c};
            uniBytes = new String(a).getBytes("UnicodeBigUnmarked");
        } catch (java.io.UnsupportedEncodingException e) {
            // This should never fail
        }

        for (int i = 0; i < uniBytes.length; i++) {
            int b = (uniBytes[i] < 0) ? (int)(256 + uniBytes[i])
                    : (int)uniBytes[i];

            String hexString = Integer.toHexString(b);
            if (hexString.length() == 1)
                buf = buf.append("0" + hexString);
            else
                buf = buf.append(hexString);
        }
        return buf.toString();
    }

    private void addKerning(StringBuffer buf, Integer ch1, Integer ch2,
                            HashMap kerning, String startText,
                            String endText) {
        HashMap kernPair = (HashMap)kerning.get(ch1);

        if (kernPair != null) {
            Integer width = (Integer)kernPair.get(ch2);
            if (width != null) {
                buf.append(endText).append(-(width.intValue())).append(' ').append(startText);
            }
        }
    }

    /**
     * Checks to see if we have some text rendering commands open
     * still and writes out the TJ command to the stream if we do
     */
    private void closeText() {
        if (textOpen) {
            currentStream.add("] TJ\n");
            textOpen = false;
            prevWordX = 0;
            prevWordY = 0;
        }
    }

    private void updateColor(boolean fill, StringBuffer pdf) {
        /*PDFColor areaColor = null;
        if (this.currentFill instanceof PDFColor) {
            areaColor = (PDFColor)this.currentFill;
        }

        if (areaColor == null || areaColor.red() != (double)area.getRed()
                || areaColor.green() != (double)area.getGreen()
                || areaColor.blue() != (double)area.getBlue()) {

            areaColor = new PDFColor((double)area.getRed(),
                                     (double)area.getGreen(),
                                     (double)area.getBlue());

            closeText();
            this.currentFill = areaColor;
            pdf.append(this.currentFill.getColorSpaceOut(true));
        }*/
    }

    private void updateFont(String name, int size, StringBuffer pdf) {
        if ((!name.equals(this.currentFontName))
                || (size != this.currentFontSize)) {
            closeText();

            this.currentFontName = name;
            this.currentFontSize = size;
            pdf = pdf.append("/" + name + " " + (size / 1000) + " Tf\n");
        }
    }

    public void renderImage(Image image) {
        String url = image.getURL();
        ImageFactory fact = ImageFactory.getInstance();
        FopImage fopimage = fact.getImage(url, userAgent);
        if(fopimage == null) {
            return;
        }
        if(!fopimage.load(FopImage.DIMENSIONS, userAgent)) {
            return;
        }
        String mime = fopimage.getMimeType();
        if("text/xml".equals(mime)) {
            if(!fopimage.load(FopImage.ORIGINAL_DATA, userAgent)) {
                return;
            }
            Document doc = ((XMLImage)fopimage).getDocument();
            String ns = ((XMLImage)fopimage).getNameSpace();

            renderDocument(doc, ns);

        } else if("image/svg+xml".equals(mime)) {
            if(!fopimage.load(FopImage.ORIGINAL_DATA, userAgent)) {
                return;
            }
            Document doc = ((XMLImage)fopimage).getDocument();
            String ns = ((XMLImage)fopimage).getNameSpace();

            renderDocument(doc, ns);

        } else if("image/eps".equals(mime)) {
            if(!fopimage.load(FopImage.ORIGINAL_DATA, userAgent)) {
                return;
            }
            int xobj = pdfDoc.addImage(fopimage);
            fact.releaseImage(url, userAgent);
        } else if("image/jpg".equals(mime)) {
            if(!fopimage.load(FopImage.ORIGINAL_DATA, userAgent)) {
                return;
            }
            int xobj = pdfDoc.addImage(fopimage);
            fact.releaseImage(url, userAgent);
        } else {
            if(!fopimage.load(FopImage.BITMAP, userAgent)) {
                return;
            }
            int xobj = pdfDoc.addImage(fopimage);
            fact.releaseImage(url, userAgent);

            closeText();
            int w = fopimage.getWidth();
            int h = fopimage.getHeight();

            currentStream.add("ET\nq\n" + ((float)w) + " 0 0 "
                              + ((float)-h) + " "
                              + (((float)currentBlockIPPosition) / 1000f) + " "
                              + (((float)(currentBPPosition - 1000 * h)) / 1000f) + " cm\n" + "/Im"
                              + xobj + " Do\nQ\nBT\n");
        }

    }

    public void renderForeignObject(ForeignObject fo) {
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        renderDocument(doc, ns);
    }

    public void renderDocument(Document doc, String ns) {
        RendererContext context;
        context = new RendererContext(mimeType);
        context.setUserAgent(userAgent);

        context.setProperty(PDFXMLHandler.PDF_DOCUMENT, pdfDoc);
        context.setProperty(PDFXMLHandler.PDF_STREAM, currentStream);
        context.setProperty(PDFXMLHandler.PDF_X, new Integer(currentBlockIPPosition));
        context.setProperty(PDFXMLHandler.PDF_Y, new Integer(currentBPPosition));
        FontState fs = null;
            try {
                fs = new FontState(fontInfo, "Helvetica", "",
                                          "", 12 * 1000, 0);
            } catch (org.apache.fop.apps.FOPException fope) {
                fope.printStackTrace();
            }

        context.setProperty(PDFXMLHandler.PDF_FONT_STATE, fs);
        context.setProperty(PDFXMLHandler.PDF_FONT_NAME, currentFontName);
        context.setProperty(PDFXMLHandler.PDF_FONT_SIZE, new Integer(currentFontSize));
        context.setProperty(PDFXMLHandler.PDF_XPOS, new Integer(currentBlockIPPosition));
        context.setProperty(PDFXMLHandler.PDF_YPOS, new Integer(currentBPPosition));
        closeText();
        currentStream.add("ET\n");
        userAgent.renderXML(context, doc, ns);
        currentStream.add("BT\n");

    }

    public void renderViewport(Viewport viewport) {
        /*if (clip && w != 0 && h != 0) {
            currentStream.add(x / 1000f + " " + y / 1000f + " m\n");
            currentStream.add((x + w) / 1000f + " " + y / 1000f + " l\n");
            currentStream.add((x + w) / 1000f + " " + (y - h) / 1000f
                              + " l\n");
            currentStream.add(x / 1000f + " " + (y - h) / 1000f + " l\n");
            currentStream.add("h\n");
            currentStream.add("W\n");
            currentStream.add("n\n");
        }*/
        super.renderViewport(viewport);
    }

    public void renderLeader(Leader area) {
        currentStream.add("ET\n");
        currentStream.add((((float)currentBlockIPPosition) / 1000f) + " "
                          + ((currentBPPosition + area.getOffset()) / 1000f) + " m\n");
        currentStream.add(((currentBlockIPPosition + area.getWidth()) / 1000f) + " " + ((currentBPPosition + area.getOffset()) / 1000f) + " l\n");
        currentStream.add("S\n");
        currentStream.add("BT\n");
        super.renderLeader(area);
    }
}

