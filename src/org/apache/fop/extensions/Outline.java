/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.*;
import org.apache.fop.pdf.PDFGoTo;
import org.apache.fop.pdf.PDFAction;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.apps.FOPException;

import java.util.*;

import org.xml.sax.Attributes;

public class Outline extends ExtensionObj {
    private Label _label;
    private ArrayList _outlines = new ArrayList();

    private String _internalDestination;
    private String _externalDestination;

    /**
     * The parent outline object if it exists
     */
    private Outline _parentOutline;

    /**
     * an opaque renderer context object, e.g. PDFOutline for PDFRenderer
     */
    private Object _rendererObject;

    public Outline(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        _internalDestination =
            attlist.getValue(null, "internal-destination");
        _externalDestination =
            attlist.getValue(null, "external-destination");
        if (_externalDestination != null &&!_externalDestination.equals("")) {
            log.warn("fox:outline external-destination not supported currently.");
        }

        if (_internalDestination == null || _internalDestination.equals("")) {
            log.warn("fox:outline requires an internal-destination.");
        }

        for (FONode node = getParent(); node != null;
                node = node.getParent()) {
            if (node instanceof Outline) {
                _parentOutline = (Outline)node;
                break;
            }
        }

    }

    protected void addChild(FONode obj) {
        if (obj instanceof Label) {
            _label = (Label)obj;
        } else if (obj instanceof Outline) {
            _outlines.add(obj);
        }
    }

    public void setRendererObject(Object o) {
        _rendererObject = o;
    }

    public Object getRendererObject() {
        return _rendererObject;
    }

    public Outline getParentOutline() {
        return _parentOutline;
    }

    public Label getLabel() {
        return _label == null ? new Label(this) : _label;
    }

    public ArrayList getOutlines() {
        return _outlines;
    }

    public String getInternalDestination() {
        return _internalDestination;
    }

}

