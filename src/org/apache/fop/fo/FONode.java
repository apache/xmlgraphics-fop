/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.StreamRenderer;
import org.apache.fop.layout.Area;
import org.apache.fop.util.CharUtilities;

// Avalon
import org.apache.avalon.framework.logger.Logger;

import org.xml.sax.Attributes;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * base class for nodes in the XML tree
 *
 */
abstract public class FONode {
    protected FOUserAgent userAgent;
    protected FONode parent;
    protected String name;

    protected Logger log;

    protected FONode(FONode parent) {
        this.parent = parent;
    }

    public void setName(String str) {
        name = str;
    }

    public void setLogger(Logger logger) {
        log = logger;
    }

    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    public void setStreamRenderer(StreamRenderer st) {
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
    }

    /**
     * returns the name of the object
     * @return the name of this object
     */
    public String getName() {
        return this.name;
    }

    /**
     * adds characters (does nothing here)
     * @param data text
     * @param start start position
     * @param length length of the text
     */
    protected void addCharacters(char data[], int start, int length) {
        // ignore
    }

    /**
     * generates the area or areas for this formatting object
     * and adds these to the area. This method should always be
     * overridden by all sub classes
     *
     * @param area
     */
    public Status layout(Area area) throws FOPException {
        // should always be overridden
        return new Status(Status.OK);
    }

    /**
     *
     */
    protected void start() {
        // do nothing by default
    }

    /**
     *
     */
    protected void end() {
        // do nothing by default
    }

    protected void addChild(FONode child) {
    }

    public FONode getParent() {
        return this.parent;
    }

    /**
     * Return an iterator over all the children of this FObj.
     * @return A ListIterator.
     */
    public ListIterator getChildren() {
	return null;
    }

    /**
     * Return an iterator over the object's children starting
     * at the pased node.
     * @param childNode First node in the iterator
     * @return A ListIterator or null if childNode isn't a child of
     * this FObj.
     */
    public ListIterator getChildren(FONode childNode) {
	return null;
    }

    public CharIterator charIterator() {
	return new OneCharIterator(CharUtilities.CODE_EOT);
    }

}
