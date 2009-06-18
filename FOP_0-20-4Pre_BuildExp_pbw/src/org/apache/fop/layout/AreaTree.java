/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.StreamRenderer;
import org.apache.fop.fo.flow.StaticContent;
import org.apache.fop.svg.*;
import org.apache.fop.render.Renderer;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.extensions.ExtensionObj;
import org.apache.fop.fo.pagination.PageSequence;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

/*
 * Modified by Mark Lillywhite, mark-fop@inomial.com. No longer keeps
   a list of pages in the tree, instead these are passed to the
   StreamRenderer. No longer keeps it's own list of IDReferences;
   these are handled by StreamRenderer. In many ways StreamRenderer
   has taken over from AreaTree and possibly this might be a better
   place to deal with things in the future..?<P>
   
   Any extensions added to the AreaTree while generating a page
   are given to the Page for the renderer to deal with.
  */

public class AreaTree {

    /**
     * object containing information on available fonts, including
     * metrics
     */
    FontInfo fontInfo;

    /**
     * List of root extension objects
     */
    Vector rootExtensions = null;

    private StreamRenderer streamRenderer;

    public AreaTree(StreamRenderer streamRenderer) {
        this.streamRenderer = streamRenderer;
    }

    public void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }

    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    public Page getNextPage(Page current, boolean isWithinPageSequence,
                            boolean isFirstCall) {
        //return streamRenderer.getNextPage(current, isWithinPageSequence,isFirstCall);
	return null; // This will go away in new layout!
    }

    public Page getPreviousPage(Page current, boolean isWithinPageSequence,
                                boolean isFirstCall) {
        //return streamRenderer.getPreviousPage(current,isWithinPageSequence,isFirstCall);
	return null; // This will go away in new layout!
    }

    public void addPage(Page page)
    throws FOPException {
//         try {
//             page.setExtensions(rootExtensions);
//             rootExtensions = null;
//             streamRenderer.queuePage(page);
//         } catch (IOException e) {
//             throw new FOPException(e);
//         }
    }

    public IDReferences getIDReferences() {
        return streamRenderer.getIDReferences();
    }

    public void addExtension(ExtensionObj obj) {
        if(rootExtensions ==null)
            rootExtensions = new Vector();
        rootExtensions.addElement(obj);
    }

}
