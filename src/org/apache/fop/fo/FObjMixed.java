/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;

/**
 * base class for representation of mixed content formatting objects
 * and their processing
 */
public class FObjMixed extends FObj {
    FOText.TextInfo textInfo = null;

    public FObjMixed(FONode parent) {
        super(parent);
    }

    protected void addCharacters(char data[], int start, int length) {
        if(textInfo == null) {
	    textInfo = new FOText.TextInfo();
            String fontFamily =
                getProperty("font-family").getString();
            String fontStyle =
                getProperty("font-style").getString();
            String fontWeight =
                getProperty("font-weight").getString();
            int fontSize =
                getProperty("font-size").getLength().mvalue();
            // font-variant support
            // added by Eric SCHAEFFER
            int fontVariant =
                getProperty("font-variant").getEnum();

            //textInfo.fs = new FontState(area.getFontInfo(), fontFamily,
            //                        fontStyle, fontWeight, fontSize,
            //                        fontVariant);

            ColorType c = getProperty("color").getColorType();
            textInfo.red = c.red();
            textInfo.green = c.green();
            textInfo.blue = c.blue();

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
            FONode fo = (FONode)children.elementAt(i);
            Status status;
            if ((status = fo.layout(area)).isIncomplete()) {
                this.marker = i;
                return status;
            }
        }
        return new Status(Status.OK);
    }

}

