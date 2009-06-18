package org.apache.xml.fop.render.xml;

// FOP
import org.apache.xml.fop.svg.*;
import org.apache.xml.fop.render.Renderer;
import org.apache.xml.fop.image.ImageArea;
import org.apache.xml.fop.layout.*;
import org.apache.xml.fop.pdf.*;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Renderer that renders areas to XML for debugging purposes.
 */
public class XMLRenderer implements Renderer {
    
    /** indentation to use for pretty-printing the XML */
    protected int indent = 0;

    /** the application producing the XML */
    protected String producer;

    /** the writer used to output the XML */
    protected PrintWriter writer;

    /**
     * set the document's producer
     *
     * @param producer string indicating application producing the XML
     */
    public void setProducer(String producer) {
	this.producer = producer;
    }

    /**
     * render the areas into XML
     *
     * @param areaTree the laid-out area tree
     * @param writer the PrintWriter to give the XML to
     */
    public void render(AreaTree areaTree, PrintWriter writer)
	throws IOException {
	System.err.println("rendering areas to XML");
	this.writer = writer;
	this.writer.write("<?xml version=\"1.0\"?>\n<!-- produced by "
			  + this.producer + " -->\n");
	writeStartTag("<AreaTree>");
	Enumeration e = areaTree.getPages().elements();
	while (e.hasMoreElements()) {
	    this.renderPage((Page) e.nextElement());
	}
	writeEndTag("</AreaTree>");
	this.writer.flush();
	System.err.println("written out XML");
    }

    /**
     * write out spaces to make indent
     */
    protected void writeIndent() {
        StringBuffer s = new StringBuffer();
	for (int i= 0; i<this.indent; i++) {
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
        this.writer.write(element+"\n");
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
	org.apache.xml.fop.render.pdf.FontSetup.setup(fontInfo);
    }

    /**
     * render an area container to XML
     *
     * @param area the area container to render
     */
    public void renderAreaContainer(AreaContainer area) {
	writeStartTag("<AreaContainer>");
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    Box b = (Box) e.nextElement();
	    b.render(this);
	}
	writeEndTag("</AreaContainer>");
    }

    /**
     * render a block area to XML
     *
     * @param area the block area to render
     */
    public void renderBlockArea(BlockArea area) {
	writeStartTag("<BlockArea start-indent=\""
		      + area.getStartIndent()
		      + "\" end-indent=\""
		      + area.getEndIndent() + "\">");
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    Box b = (Box) e.nextElement();
	    b.render(this);
	}
	writeEndTag("</BlockArea>");
    }

    /**
     * render a display space to XML
     *
     * @param space the space to render
     */
    public void renderDisplaySpace(DisplaySpace space) {
	writeEmptyElementTag("<DisplaySpace size=\""
			     + space.getSize() +"\"/>");
    }

    /**
     * render an SVG area to XML
     *
     * @param area the area to render
     */
    public void renderSVGArea(SVGArea area) {
	writeEmptyElementTag("<SVG/>");
    }

    /**
     * render an image area to XML
     *
     * @param area the area to render
     */
    public void renderImageArea(ImageArea area) {
	writeEmptyElementTag("<ImageArea/>");
    }

    /**
     * render an inline area to XML
     *
     * @param area the area to render
     */
    public void renderInlineArea(InlineArea area) {
	String fontWeight = area.getFontState().getFontWeight();
	StringBuffer sb = new StringBuffer();
	String s = area.getText();
	int l = s.length();
	for (int i=0; i < l; i++) {
	    char ch = s.charAt(i);
	    if (ch>127)
		sb = sb.append("&#"+(int)ch+";");
	    else
		sb = sb.append(ch);
	}
	writeElement("<InlineArea font-weight=\""
		     + fontWeight + "\" red=\""
		     + area.getRed() + "\" green=\""
		     + area.getGreen() + "\" blue = \""
		     + area.getBlue() + " width = \""
		     + area.getContentWidth() + "\">" + sb.toString()
		     + "</InlineArea>");
    }

    /**
     * render an inline space to XML
     *
     * @param space the space to render
     */
    public void renderInlineSpace(InlineSpace space) {
	writeEmptyElementTag("<InlineSpace size=\""
			     + space.getSize() +"\"/>");
    }

    /**
     * render a line area to XML
     *
     * @param area the area to render
     */
    public void renderLineArea(LineArea area) {
	String fontWeight = area.getFontState().getFontWeight();
	writeStartTag("<LineArea font-weight=\""
		      + fontWeight + "\">");
	Enumeration e = area.getChildren().elements();
	while (e.hasMoreElements()) {
	    Box b = (Box)e.nextElement();
	    b.render(this);
	}
	writeEndTag("</LineArea>");
    }

    /**
     * render a page to XML
     *
     * @param page the page to render
     */
    public void renderPage(Page page) {
	AreaContainer body, before, after;
	writeStartTag("<Page>");
	body = page.getBody();
	before = page.getBefore();
	after = page.getAfter();
	if (before != null) {
	    renderAreaContainer(before);
	}
	renderAreaContainer(body);
	if (after != null) {
	    renderAreaContainer(after);
	}
	writeEndTag("</Page>");
    }

    /**
     * render a rule area to XML
     *
     * @param area the area to render
     */
    public void renderRuleArea(RuleArea area) {
	writeEmptyElementTag("<Rule start-indent=\""
			     + area.getStartIndent()
			     + "\" end-indent=\""
			     + area.getEndIndent()
			     + "\" rule-thickness=\""
			     + area.getRuleThickness()
			     + "\" red=\"" + area.getRed()
			     + "\" green=\"" + area.getGreen()
			     + "\" blue = \"" + area.getBlue()
			     + "\"/>");
    }
}
