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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.svg.SVGElementMapping;



/**
 * base class for nodes in the XML tree
 *
 */
public abstract class FONode {

    /** Parent FO node */
    protected FONode parent;

    /** Name of the node */
    protected String name;

    /** Marks input file containing this object **/
    public String systemId;

    /** Marks line number of this object in the input file **/
    public int line;

    /** Marks column number of this object in the input file **/
    public int column;

    /** Logger for fo-tree related messages **/
    private static Log log = LogFactory.getLog(FONode.class);

    /**
     * Main constructor.
     * @param parent parent of this node
     */
    protected FONode(FONode parent) {
        this.parent = parent;
    }

    /**
     * Set the location information for this element
     * @param locator the org.xml.sax.Locator object
     */
    public void setLocation(Locator locator) {
        if (locator != null) {
            line = locator.getLineNumber();
            column = locator.getColumnNumber();
            systemId = locator.getSystemId();
        }
    }

    /**
     * Recursively goes up the FOTree hierarchy until the fo:root is found,
     * which returns the parent FOInputHandler.
     * @return the FOInputHandler object that is the parent of the FO Tree
     */
    public FOInputHandler getFOInputHandler() {
        return parent.getFOInputHandler();
    }

    /**
     * Returns the user agent for the node.
     * @return FOUserAgent
     */
    public FOUserAgent getUserAgent() {
        return getFOInputHandler().getUserAgent();
    }

    /**
     * Returns the logger for the node.
     * @return the logger
     */
    public Log getLogger() {
        return log;
    }

    /**
     * Initialize the node with its name, location information, and attributes
     * The attributes must be used immediately as the sax attributes
     * will be altered for the next element.
     * @param elementName element name (e.g., "fo:block")
     * @param locator Locator object (ignored by default)
     * @param attlist Collection of attributes passed to us from the parser.
     * @throws FOPException for errors or inconsistencies in the attributes
    */
    public void processNode(String elementName, Locator locator, Attributes attlist) throws FOPException {
        System.out.println("name = " + elementName);
        this.name = elementName;
    }

    /**
     * Returns the name of the object
     * @return the name of this object
     */
    public String getName() {
        return this.name;
    }

    /**
     * Checks to make sure, during SAX processing of input document, that the
     * incoming node is valid for the this (parent) node (e.g., checking to
     * see that fo:table is not an immediate child of fo:root)
     * called within FObj constructor
     * @param namespaceURI namespace of incoming node
     * @param localName (e.g. "table" for "fo:table")
     * @throws IllegalArgumentException if incoming node not valid for parent
     */
    protected void validateChildNode(Locator loc, String namespaceURI, String localName) {}

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
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveFONode(this);
    }

    /**
     * Helper function to standardize the names of all namespace URI - local
     * name pairs in text messages.
     * For readability, using fo:, fox:, svg:, for those namespaces even
     * though that prefix may not have been chosen in the document.
     * @param namespaceURI URI of node found 
     *         (e.g., "http://www.w3.org/1999/XSL/Format")
     * @param localName local name of node, (e.g., "root" for "fo:root")
     * @return the prefix:localname, if fo/fox/svg, or a longer representation
     * with the unabbreviated URI otherwise.
     */
    public static String getNodeString(String namespaceURI, String localName) {
        if (namespaceURI.equals(FOElementMapping.URI)) {
            return "fo:" + localName;
        } else if (namespaceURI.equals(ExtensionElementMapping.URI)) {
            return "fox:" + localName;
        } else if (namespaceURI.equals(SVGElementMapping.URI)) {
            return "svg:" + localName;
        } else
            return "(Namespace URI: \"" + namespaceURI + "\", " +
                "Local Name: \"" + localName + "\")";
    }

    /**
     * Helper function to standardize "too many" error exceptions
     * (e.g., two fo:declarations within fo:root)
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param offendingNode incoming node that would cause a duplication.
     */
    protected void tooManyNodesError(Locator loc, String offendingNode) {
        throw new IllegalArgumentException(
            errorText(loc) + getName() + ", only one " 
            + offendingNode + " may be declared.");
    }

    /**
     * Helper function to standardize "out of order" exceptions
     * (e.g., fo:layout-master-set appearing after fo:page-sequence)
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param tooLateNode string name of node that should be earlier in document
     * @param tooEarlyNode string name of node that should be later in document
     */
    protected void nodesOutOfOrderError(Locator loc, String tooLateNode, 
        String tooEarlyNode) {
        throw new IllegalArgumentException(
            errorText(loc) + getName() + ", " + tooLateNode 
            + " must be declared before " + tooEarlyNode + ".");
    }
    
    /**
     * Helper function to return "invalid child" exceptions
     * (e.g., fo:block appearing immediately under fo:root)
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param nsURI namespace URI of incoming invalid node
     * @param lName local name (i.e., no prefix) of incoming node 
     */
    protected void invalidChildError(Locator loc, String nsURI, String lName) {
        throw new IllegalArgumentException(
            errorText(loc) + getNodeString(nsURI, lName) + 
            " is not a valid child element of " + getName() + ".");
    }
    
    /**
     * Helper function to return missing child element errors
     * (e.g., fo:layout-master-set not having any page-master child element)
     * @param contentModel The XSL Content Model for the fo: object.
     * or a similar description indicating child elements needed.
     */
    protected void missingChildElementError(String contentModel) {
        throw new IllegalArgumentException(
            errorText(line, column) + getName() + " is missing child elements. \n" + 
            "Required Content Model: " + contentModel);
    }

    /**
     * Helper function to return "Error (line#/column#)" string for
     * above exception messages
     * @param loc org.xml.sax.Locator object
     * @return String opening error text
     */
    protected static String errorText(Locator loc) {
        return "Error(" + loc.getLineNumber() + "/" + loc.getColumnNumber() + "): ";
    }
    
    /**
     * Helper function to return "Error (line#/column#)" string for
     * above exception messages
     * @param lineNumber - line number of node with error
     * @param columnNumber - column number of node with error
     * @return String opening error text
     */
    protected static String errorText(int lineNumber, int columnNumber) {
        return "Error(" + lineNumber + "/" + columnNumber + "): ";
    }
}

