/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

// FOP
import org.apache.fop.apps.FOPException;				   
import org.apache.fop.fo.flow.StaticContent;
import org.apache.fop.svg.*;
import org.apache.fop.render.Renderer;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.extensions.ExtensionObj;

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

    /** List of root extension objects */
    Vector rootExtensions = new Vector();
    

    IDReferences idReferences = new IDReferences();

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

    public IDReferences getIDReferences()
    {
        return idReferences;
    }

    public void addExtension(ExtensionObj obj) 
    {
	rootExtensions.addElement(obj);
    }
    
    public Vector getExtensions() 
    {
	return rootExtensions;
    }
    

}
