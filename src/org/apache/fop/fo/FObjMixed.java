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


/**
 * base class for representation of mixed content formatting objects
 * and their processing
 */
public class FObjMixed extends FObj {
    TextInfo textInfo = null;
    FontInfo fontInfo=null;

    public FObjMixed(FONode parent) {
        super(parent);
    }

    public void setStreamRenderer(StreamRenderer st) {
	fontInfo = st.getFontInfo();
    }

    protected void addCharacters(char data[], int start, int length) {
        if(textInfo == null) {
	    textInfo = new TextInfo();

	    try {
		textInfo.fs = propMgr.getFontState(fontInfo);
	    } catch (FOPException fopex) {
		log.error("Error setting FontState for characters: " +
			  fopex.getMessage());
	    }

            ColorType c = getProperty("color").getColorType();
            textInfo.color = c;

            textInfo.verticalAlign =
                getProperty("vertical-align").getEnum();

            textInfo.wrapOption =
                getProperty("wrap-option").getEnum();
            textInfo.whiteSpaceCollapse =
                getProperty("white-space-collapse").getEnum();

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
            FONode fo = (FONode)children.get(i);
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

