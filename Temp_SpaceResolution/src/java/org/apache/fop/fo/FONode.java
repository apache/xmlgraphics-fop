/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.svg.SVGElementMapping;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.util.CharUtilities;

/**
 * Base class for nodes in the XML tree
 */
public abstract class FONode implements Cloneable {

    /** the XSL-FO namespace URI */
    protected static final String FO_URI = FOElementMapping.URI;

    /** Parent FO node */
    protected FONode parent;

    /**  Marks location of this object from the input FO
     *   Call locator.getSystemId(), getLineNumber(),
     *   getColumnNumber() for file, line, column
     *   information
     */
    public Locator locator;
    //TODO Make private or protected and access via getLocator()

    /** Logger for fo-tree related messages **/
    protected static Log log = LogFactory.getLog(FONode.class);
    //TODO Remove getLogger() method!
    
    /**
     * Main constructor.
     * @param parent parent of this node
     */
    protected FONode(FONode parent) {
        this.parent = parent;
    }

    /**
     * Perform a shallow cloning operation,
     * set its parent, and optionally clean the list of child nodes
     * @param parent the intended parent of the clone
     * @param removeChildren if true, clean the list of child nodes
     * @return the cloned FO node
     * @throws FOPException if there's a problem while cloning the node
     */
    public FONode clone(FONode parent, boolean removeChildren)
                throws FOPException {
        FONode foNode = (FONode) clone();
        foNode.parent = parent;
        parent.addChildNode(foNode);
        return foNode;
    }

    /**
     * Perform a shallow cloning operation
     * 
     * @see java.lang.Object#clone()
     * @return the cloned object
     */
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Set the location information for this element
     * @param locator the org.xml.sax.Locator object
     */
    public void setLocator(Locator locator) {
        if (locator != null) {
            this.locator = locator;
        }
    }

    /** @return the location information for this element or null, if not available  */
    public Locator getLocator() {
        return this.locator;
    }
    
    /**
     * Recursively goes up the FOTree hierarchy until the fo:root is found,
     * which returns the parent FOEventHandler.
     * @return the FOEventHandler object that is the parent of the FO Tree
     */
    public FOEventHandler getFOEventHandler() {
        return parent.getFOEventHandler();
    }

    /**
     * Returns the user agent for the node.
     * @return FOUserAgent
     */
    public FOUserAgent getUserAgent() {
        return getFOEventHandler().getUserAgent();
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
     * @param parent the property list of the parent node
     * @throws FOPException for errors or inconsistencies in the attributes
    */
    public void processNode(String elementName, Locator locator, 
            Attributes attlist, PropertyList parent) throws FOPException {
        if (log.isDebugEnabled()) {
            log.debug("Unhandled element: " + elementName 
                    + (locator != null ? " at " + getLocatorString(locator) : ""));
        }
    }

    /**
     * Create a property list for this node. Return null if the node does not
     * need a property list.
     * @param parent the closest parent propertylist. 
     * @param foEventHandler The FOEventHandler where the PropertyListMaker 
     *              instance can be found.
     * @return A new property list.
     * @throws FOPException if there's a problem during processing
     */
    protected PropertyList createPropertyList(PropertyList parent, FOEventHandler foEventHandler) 
                throws FOPException {
        return null;
    }

    /**
     * Checks to make sure, during SAX processing of input document, that the
     * incoming node is valid for the this (parent) node (e.g., checking to
     * see that fo:table is not an immediate child of fo:root)
     * called within FObj constructor
     * @param loc location in the FO source file
     * @param namespaceURI namespace of incoming node
     * @param localName (e.g. "table" for "fo:table")
     * @throws ValidationException if incoming node not valid for parent
     */
    protected void validateChildNode(Locator loc, String namespaceURI, String localName) 
            throws ValidationException {
        //nop
    }

    /**
     * Adds characters (does nothing here)
     * @param data array of characters containing text to be added
     * @param start starting array element to add
     * @param end ending array element to add
     * @param pList currently applicable PropertyList 
     * @param locator location in fo source file.
     * @throws FOPException if there's a problem during processing
     */
    protected void addCharacters(char[] data, int start, int end,
                                 PropertyList pList,
                                 Locator locator) throws FOPException {
        // ignore
    }

    /**
     * Called after processNode() is called. Subclasses can do additional processing.
     * @throws FOPException if there's a problem during processing
     */
    protected void startOfNode() throws FOPException {
        // do nothing by default
   }

    /**
     * Primarily used for making final content model validation checks
     * and/or informing the FOEventHandler that the end of this FO
     * has been reached.
     * @throws FOPException if there's a problem during processing
     */
    protected void endOfNode() throws FOPException {
        // do nothing by default
    }

    /**
     * Adds a node as a child of this node. The default implementation of this method
     * just ignores any child node being added.
     * @param child child node to be added to the childNodes of this node
     * @throws FOPException if there's a problem during processing
     */
    protected void addChildNode(FONode child) throws FOPException {
        // do nothing by default
    }

    /**
     * Removes a child node. Used by the child nodes to remove themselves, for
     * example table-body if it has no children.
     * @param child child node to be removed
     */
    public void removeChild(FONode child) {
        //nop
    }

    /**
     * @return the parent node of this node
     */
    public FONode getParent() {
        return this.parent;
    }

    /**
     * Return an iterator over all the child nodes of this FObj.
     * @return A ListIterator.
     */
    public ListIterator getChildNodes() {
        return null;
    }

    /**
     * Return an iterator over the object's child nodes starting
     * at the pased node.
     * @param childNode First node in the iterator
     * @return A ListIterator or null if child node isn't a child of
     * this FObj.
     */
    public ListIterator getChildNodes(FONode childNode) {
        return null;
    }

    /**
     * @return an iterator for the characters in this node
     */
    public CharIterator charIterator() {
        return new OneCharIterator(CharUtilities.CODE_EOT);
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
        } else {
            return "(Namespace URI: \"" + namespaceURI + "\", "
                    + "Local Name: \"" + localName + "\")";
        }
    }

    /**
     * Helper function to standardize property error exceptions
     * (e.g., not specifying either an internal- or an external-destination
     * property for an FO:link)
     * @param problem text to display that indicates the problem
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void attributeError(String problem) 
                throws ValidationException {
        throw new ValidationException(errorText(locator) + getName() 
                + ", " + problem, locator);
    }

    /**
     * Helper function to standardize attribute warnings
     * (e.g., currently unsupported properties)
     * @param problem text to display that indicates the problem
     */
    protected void attributeWarning(String problem) {
        log.warn(warningText(locator) + getName() + ", " + problem);
    }

    /**
     * Helper function to standardize "too many" error exceptions
     * (e.g., two fo:declarations within fo:root)
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param nsURI namespace URI of incoming invalid node
     * @param lName local name (i.e., no prefix) of incoming node 
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void tooManyNodesError(Locator loc, String nsURI, String lName) 
                throws ValidationException {
        throw new ValidationException(errorText(loc) + "For " + getName() 
            + ", only one " + getNodeString(nsURI, lName) + " may be declared.", 
            loc);
    }

    /**
     * Helper function to standardize "too many" error exceptions
     * (e.g., two fo:declarations within fo:root)
     * This overrloaded method helps make the caller code better self-documenting
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param offendingNode incoming node that would cause a duplication.
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void tooManyNodesError(Locator loc, String offendingNode) 
                throws ValidationException {
        throw new ValidationException(errorText(loc) + "For " + getName() 
            + ", only one " + offendingNode + " may be declared.", loc);
    }

    /**
     * Helper function to standardize "out of order" exceptions
     * (e.g., fo:layout-master-set appearing after fo:page-sequence)
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param tooLateNode string name of node that should be earlier in document
     * @param tooEarlyNode string name of node that should be later in document
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void nodesOutOfOrderError(Locator loc, String tooLateNode, 
        String tooEarlyNode) throws ValidationException {
        throw new ValidationException(errorText(loc) + "For " + getName() + ", " + tooLateNode 
            + " must be declared before " + tooEarlyNode + ".", loc);
    }
    
    /**
     * Helper function to return "invalid child" exceptions
     * (e.g., fo:block appearing immediately under fo:root)
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param nsURI namespace URI of incoming invalid node
     * @param lName local name (i.e., no prefix) of incoming node 
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void invalidChildError(Locator loc, String nsURI, String lName) 
                throws ValidationException {
        invalidChildError(loc, nsURI, lName, null);
    }
    
    /**
     * Helper function to return "invalid child" exceptions with more
     * complex validation rules (i.e., needing more explanation of the problem)
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param nsURI namespace URI of incoming invalid node
     * @param lName local name (i.e., no prefix) of incoming node
     * @param ruleViolated text explanation of problem
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void invalidChildError(Locator loc, String nsURI, String lName,
                String ruleViolated)
                throws ValidationException {
        throw new ValidationException(errorText(loc) + getNodeString(nsURI, lName) 
            + " is not a valid child element of " + getName() 
            + ((ruleViolated != null) ? ": " + ruleViolated : "."), loc);
    }

    /**
     * Helper function to throw an error caused by missing mandatory child elements.
     * E.g., fo:layout-master-set not having any page-master child element.
     * @param contentModel The XSL Content Model for the fo: object or a similar description 
     *                     indicating the necessary child elements.
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void missingChildElementError(String contentModel)
                throws ValidationException {
        throw new ValidationException(errorText(locator) + getName() 
            + " is missing child elements. \nRequired Content Model: " 
            + contentModel, locator);
    }

    /**
     * Helper function to throw an error caused by missing mandatory properties
     * @param propertyName the name of the missing property.
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void missingPropertyError(String propertyName)
                throws ValidationException {
        throw new ValidationException(errorText(locator) + getName()
            + " is missing required \"" + propertyName + "\" property.", locator);
    }

    /**
     * Helper function to return "Error(line#/column#)" string for
     * above exception messages
     * @param loc org.xml.sax.Locator object
     * @return String opening error text
     */
    protected static String errorText(Locator loc) {
        return "Error(" + getLocatorString(loc) + "): ";
    }

    /**
     * Helper function to return "Warning(line#/column#)" string for
     * warning messages
     * @param loc org.xml.sax.Locator object
     * @return String opening warning text
     */
    protected static String warningText(Locator loc) {
        return "Warning(" + getLocatorString(loc) + "): ";
    }
    
    /**
     * Helper function to format a Locator instance.
     * @param loc org.xml.sax.Locator object
     * @return String the formatted text
     */
    public static String getLocatorString(Locator loc) {
        if (loc == null) {
            return "Unknown location";
        } else {
            return loc.getLineNumber() + "/" + loc.getColumnNumber();
        }
    }

    /**
     * Returns the root node of this tree
     * @return the root node
     */
    public Root getRoot() {
        return parent.getRoot();
    }

    /**
     * Returns the name of the node
     * @return the name of this node
     */
    public String getName() {
        return null;
    }

    /**
     * Returns the Constants class integer value of this node
     * @return the integer enumeration of this FO (e.g., FO_ROOT)
     *      if a formatting object, FO_UNKNOWN_NODE otherwise
     */
    public int getNameId() {
        return Constants.FO_UNKNOWN_NODE;
    }

    /**
     * This method is overridden by extension elements and allows the extension element
     * to return a pass-through attachment which the parent formatting objects should simply
     * carry with them but otherwise ignore. This mechanism is used to pass non-standard 
     * information from the FO tree through to the layout engine and the renderers. 
     * @return the extension attachment if one is created by the extension element, null otherwise.
     */
    public ExtensionAttachment getExtensionAttachment() {
        return null;
    }
    
}

