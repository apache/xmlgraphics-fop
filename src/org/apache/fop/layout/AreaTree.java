package org.apache.xml.fop.layout;

// FOP
import org.apache.xml.fop.apps.FOPException;				   
import org.apache.xml.fop.fo.flow.StaticContent;
import org.apache.xml.fop.svg.*;
import org.apache.xml.fop.render.Renderer;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

public class AreaTree {

    /** object containing information on available fonts, including
	metrics */
    FontInfo fontInfo;
	
    /* list of all the pages */
    Vector pageList = new Vector();

    public void setFontInfo(FontInfo fontInfo) {
	this.fontInfo = fontInfo;
    }

    public FontInfo getFontInfo() {
	return this.fontInfo;
    }
    
    public void addPage(Page page) {
	this.pageList.addElement(page);
    }

    public Vector getPages() {
	return this.pageList;
    }
}
