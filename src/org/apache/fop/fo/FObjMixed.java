/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.layout.Area;
import org.apache.fop.layout.TextState;
import org.apache.fop.apps.FOPException;

/**
 * base class for representation of mixed content formatting objects
 * and their processing
 */
public abstract class FObjMixed extends FObj {

    // Textdecoration
    protected TextState textState;

    private StringBuffer textBuffer;

    protected FObjMixed(FObj parent, PropertyList propertyList)
      throws FOPException {
        super(parent, propertyList);
        textState = propMgr.getTextDecoration(parent);

    }

    public TextState getTextState() {
        return textState;
    }

    protected void addCharacters(char data[], int start, int length) {
        if ( textBuffer==null ) {
          textBuffer = new StringBuffer();
        }
        textBuffer.append(data,start,length);
    }

    private final void finalizeText() {
        if (textBuffer != null && textBuffer.length() > 0) {
            FOText ft = new FOText(textBuffer, this);
            ft.setTextState(textState);
            super.addChild(ft);
            textBuffer.setLength(0);
        }
    }

    protected void end() {
        finalizeText();
        textBuffer=null;
    }

    protected void addChild(FONode child) {
        finalizeText();
        super.addChild(child);
    }

    public int layout(Area area) throws FOPException {

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
            int status;
            if (Status.isIncomplete((status = fo.layout(area)))) {
                this.marker = i;
                return status;
            }
        }
        return Status.OK;
    }

}

