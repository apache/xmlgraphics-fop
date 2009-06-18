/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.StreamRenderer;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.layoutmgr.InlineStackingBPLayoutManager;
import org.apache.fop.layoutmgr.LMiter;

import java.util.List;

/**
 * base class for representation of mixed content formatting objects
 * and their processing
 */
public class FObjMixed extends FObj {
    TextInfo textInfo = null;
    protected FontInfo fontInfo = null;

    public FObjMixed(FONode parent) {
        super(parent);
    }

    public void setStreamRenderer(StreamRenderer st) {
        fontInfo = st.getFontInfo();
    }

    public void addLayoutManager(List lms) {
         lms.add(new InlineStackingBPLayoutManager(this,
			         new LMiter(children.listIterator())));
      // set start and end properties for this element, id, etc.
//         int numChildren = this.children.size();
//         for (int i = 0; i < numChildren; i++) {
//             Object o = children.get(i);
//             if (o instanceof FObj) {
//                 FObj fo = (FObj) o;
//                 fo.addLayoutManager(lms);
//             }
//         }
    }

    protected void addCharacters(char data[], int start, int length) {
        if(textInfo == null) {
	    // Really only need one of these, but need to get fontInfo
	    // stored in propMgr for later use.
	    propMgr.setFontInfo(fontInfo);
	    textInfo = propMgr.getTextLayoutProps(fontInfo);
        }

        FOText ft = new FOText(data, start, length, textInfo);
        ft.setLogger(log);
        addChild(ft);
    }

    public Status layout(Area area) throws FOPException {

        if (this.properties != null) {
            Property prop = this.properties.get("id");
            if (prop != null) {
                String id = prop.getString();

                if (this.marker == START) {
                    if (area.getIDReferences() != null)
                        area.getIDReferences().createID(id);
                    this.marker = 0;
                }

                if (this.marker == 0) {
                    if (area.getIDReferences() != null)
                        area.getIDReferences().configureID(id, area);
                }
            }
        }

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode) children.get(i);
            Status status;
            if ((status = fo.layout(area)).isIncomplete()) {
                this.marker = i;
                return status;
            }
        }
        return new Status(Status.OK);
    }

    public CharIterator charIterator() {
        return new RecursiveCharIterator(this);
    }

}

