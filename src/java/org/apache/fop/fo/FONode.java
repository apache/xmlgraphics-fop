/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo;

// Java
import java.util.ListIterator;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.commons.logging.Log;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.apps.FOUserAgent;

/**
 * base class for nodes in the XML tree
 *
 */
public abstract class FONode {
    /** Parent FO node */
    protected FONode parent;
    /** Name of the node */
    protected String name;

    /**
     * Main constructor.
     * @param parent parent of this node
     */
    protected FONode(FONode parent) {
        this.parent = parent;
    }

    /**
     * Sets the name of the node.
     * @param str the name
     */
    public void setName(String str) {
        name = str;
    }

    /**
     * Sets the name of the node.
     * @param str the name
     */
    public void setLocation(Locator locator) {
        // do nothing by default
    }
    
    /**
     * Returns the logger for the node.
     * @return the logger
     */
    public Log getLogger() {
        return getFOTreeControl().getLogger();
    }

    /**
     * Returns the user agent for the node.
     * @return FOUserAgent
     */
    public FOUserAgent getUserAgent() {
        return getFOTreeControl().getUserAgent();
    }

    /**
     * Do something with the attributes for this element
     * @param attlist Collection of attributes passed to us from the parser.
     * @throws FOPException for errors or inconsistencies in the attributes
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
    }

    /**
     * Returns the name of the object
     * @return the name of this object
     */
    public String getName() {
        return this.name;
    }

    /**
     * Adds characters (does nothing here)
     * @param data text
     * @param start start position
     * @param length length of the text
     * @param locator location in fo source file. 
     */
    protected void addCharacters(char data[], int start, int length,
                                 Locator locator) {
        // ignore
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

    /**
     * @param child child node to be added to the children of this node
     */
    protected void addChild(FONode child) {
    }

    /**
     * @return the parent node of this node
     */
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

    /**
     * @return an iterator for the characters in this node
     */
    public CharIterator charIterator() {
        return new OneCharIterator(CharUtilities.CODE_EOT);
    }

    /**
     * This is a quick check to see if it is a marker.
     * This is needed since there is no other quick way of checking
     * for a marker and not adding to the child list.
     *
     * @return true if this is a marker
     */
    protected boolean isMarker() {
        return false;
    }

    /**
     * Recursively goes up the FOTree hierarchy until the FONode is found,
     * which returns the parent Document.
     * @return the Document object that is the parent of this node.
     */
    public FOTreeControl getFOTreeControl() {
        return parent.getFOTreeControl();
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveFONode(this);
    }

}

