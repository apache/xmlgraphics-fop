/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.fop.render.xml;

// FOP
import org.apache.fop.svg.*;
import org.apache.fop.render.Renderer;
import org.apache.fop.image.ImageArea;
import org.apache.fop.layout.*;
import org.apache.fop.pdf.*;

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
	org.apache.fop.render.pdf.FontSetup.setup(fontInfo);
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
