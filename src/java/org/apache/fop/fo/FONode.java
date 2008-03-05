/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.NoSuchElementException;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.svg.SVGElementMapping;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.ContentHandlerFactory;

/**
 * Base class for nodes in the XML tree
 */
public abstract class FONode implements Cloneable {

    /** the XSL-FO namespace URI */
    protected static final String FO_URI = FOElementMapping.URI;
    /** FOP's proprietary extension namespace URI */
    protected static final String FOX_URI = ExtensionElementMapping.URI;

    /** Parent FO node */
    protected FONode parent;

    /** pointer to the sibling nodes */
    protected FONode[] siblings;

    /** 
     * Marks location of this object from the input FO
     *   Call locator.getSystemId(), getLineNumber(),
     *   getColumnNumber() for file, line, column
     *   information
     */
    protected Locator locator;

    /** Logger for fo-tree related messages **/
    protected static Log log = LogFactory.getLog(FONode.class);
    
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
     * @param cloneparent the intended parent of the clone
     * @param removeChildren if true, clean the list of child nodes
     * @return the cloned FO node
     * @throws FOPException if there's a problem while cloning the node
     */
    public FONode clone(FONode cloneparent, boolean removeChildren)
                throws FOPException {
        FONode foNode = (FONode) clone();
        foNode.parent = cloneparent;
        foNode.siblings = null;
        return foNode;
    }

    /**
     * Perform a shallow cloning operation
     * 
     * {@inheritDoc}
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
     * Bind the given <code>PropertyList</code> to this node
     * Does nothing by default. Subclasses should override this method 
     * in case they want to use the properties available on the 
     * <code>PropertyList</code>.
     * 
     * @param   propertyList    the <code>PropertyList</code>
     * @throws  FOPException if there was an error when
     *          processing the <code>PropertyList</code>
     */
    public void bind(PropertyList propertyList) throws FOPException {
        //nop
    }
    
    /**
     * Set the location information for this element
     * @param locator the org.xml.sax.Locator object
     */
    public void setLocator(Locator locator) {
        if (locator != null) {
            //Create a copy of the locator so the info is preserved when we need to
            //give pointers during layout.
            this.locator = new LocatorImpl(locator);
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
     * Indicates whether this node is a child of an fo:marker.
     * @return true if this node is a child of an fo:marker
     */
    protected boolean inMarker() {
        return getFOEventHandler().inMarker();
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
     * @param pList the property list of the parent node
     * @throws FOPException for errors or inconsistencies in the attributes
    */
    public void processNode(String elementName, Locator locator, 
            Attributes attlist, PropertyList pList) throws FOPException {
        if (log.isDebugEnabled()) {
            log.debug("Unhandled element: " + elementName 
                    + (locator != null ? " at " + getLocatorString(locator) : ""));
        }
    }

    /**
     * Create a property list for this node. Return null if the node does not
     * need a property list.
     * @param pList the closest parent propertylist. 
     * @param foEventHandler The FOEventHandler where the PropertyListMaker 
     *              instance can be found.
     * @return A new property list.
     * @throws FOPException if there's a problem during processing
     */
    protected PropertyList createPropertyList(PropertyList pList, FOEventHandler foEventHandler) 
                throws FOPException {
        return null;
    }

    /**
     * Checks to make sure, during SAX processing of input document, that the
     * incoming node is valid for the this (parent) node (e.g., checking to
     * see that <code>fo:table</code> is not an immediate child of <code>fo:root</code>)
     * called from {@link FOTreeBuilder#startElement(String, String, String, Attributes)}
     * before constructing the child {@link FObj}.
     * 
     * @param loc location in the FO source file
     * @param namespaceURI namespace of incoming node
     * @param localName name of the incoming node (without namespace prefix)
     * @throws ValidationException if incoming node not valid for parent
     */
    protected void validateChildNode(Locator loc, String namespaceURI, String localName) 
            throws ValidationException {
        //nop
    }

    /**
     * Static version of {@link FONode#validateChildNode(Locator, String, String)} that
     * can be used by subclasses that need to validate children against a different node
     * (for example: <code>fo:wrapper</code> needs to check if the incoming node is a 
     *  valid child to its parent)
     *  
     * @param fo    the FONode to validate against
     * @param loc   location in the source file
     * @param namespaceURI  namespace of the incoming node
     * @param localName     name of the incoming node (without namespace prefix) 
     * @throws ValidationException if the incoming node is not a valid child for the given FO
     */
    protected static void validateChildNode(
                                FONode fo, 
                                Locator loc, 
                                String namespaceURI, 
                                String localName) 
            throws ValidationException {
        fo.validateChildNode(loc, namespaceURI, localName);
    }
    
    /**
     * Adds characters (does nothing here)
     * @param data array of characters containing text to be added
     * @param start starting array element to add
     * @param end ending array element to add
     * @param pList currently applicable PropertyList 
     * @param locator location in the XSL-FO source file.
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
    public FONodeIterator getChildNodes() {
        return null;
    }

    /**
     * Return an iterator over the object's child nodes starting
     * at the passed node.
     * @param childNode First node in the iterator
     * @return A ListIterator or null if child node isn't a child of
     * this FObj.
     */
    public FONodeIterator getChildNodes(FONode childNode) {
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
     * Returns an instance of the FOValidationEventProducer.
     * @return an event producer for FO validation
     */
    protected FOValidationEventProducer getFOValidationEventProducer() {
        return FOValidationEventProducer.Factory.create(
                getUserAgent().getEventBroadcaster());
    }
    
    /**
     * Helper function to standardize property error exceptions
     * (e.g., not specifying either an internal- or an external-destination
     * property for an FO:link)
     * @param problem text to display that indicates the problem
     * @throws ValidationException the validation error provoked by the method call
     * @deprecated Not localizable!
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
     * @deprecated Not localizable!
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
        tooManyNodesError(loc, new QName(nsURI, lName));
    }

    /**
     * Helper function to standardize "too many" error exceptions
     * (e.g., two fo:declarations within fo:root)
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param offendingNode the qualified name of the offending node
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void tooManyNodesError(Locator loc, QName offendingNode) 
                throws ValidationException {
        getFOValidationEventProducer().tooManyNodes(this, getName(), offendingNode, loc);
    }

    /**
     * Helper function to standardize "too many" error exceptions
     * (e.g., two fo:declarations within fo:root)
     * This overloaded method helps make the caller code better self-documenting
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param offendingNode incoming node that would cause a duplication.
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void tooManyNodesError(Locator loc, String offendingNode) 
                throws ValidationException {
        tooManyNodesError(loc, new QName(FO_URI, offendingNode));
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
        nodesOutOfOrderError(loc, tooLateNode, tooEarlyNode, false);
    }
    
    /**
     * Helper function to standardize "out of order" exceptions
     * (e.g., fo:layout-master-set appearing after fo:page-sequence)
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param tooLateNode string name of node that should be earlier in document
     * @param tooEarlyNode string name of node that should be later in document
     * @param canRecover indicates whether FOP can recover from this problem and continue working
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void nodesOutOfOrderError(Locator loc, String tooLateNode, 
            String tooEarlyNode, boolean canRecover) throws ValidationException {
        getFOValidationEventProducer().nodeOutOfOrder(this, getName(),
                tooLateNode, tooEarlyNode, canRecover, loc);
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
        //TODO Localize ruleViolated somehow!
        getFOValidationEventProducer().invalidChild(this, getName(),
                new QName(nsURI, lName), ruleViolated, loc);
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
        getFOValidationEventProducer().missingChildElement(this, getName(),
                contentModel, false, locator);
    }

    /**
     * Helper function to throw an error caused by missing mandatory child elements.
     * E.g., fo:layout-master-set not having any page-master child element.
     * @param contentModel The XSL Content Model for the fo: object or a similar description 
     *                     indicating the necessary child elements.
     * @param canRecover indicates whether FOP can recover from this problem and continue working
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void missingChildElementError(String contentModel, boolean canRecover)
                throws ValidationException {
        getFOValidationEventProducer().missingChildElement(this, getName(),
                contentModel, canRecover, locator);
    }

    /**
     * Helper function to throw an error caused by missing mandatory properties
     * @param propertyName the name of the missing property.
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void missingPropertyError(String propertyName)
                throws ValidationException {
        getFOValidationEventProducer().missingProperty(this, getName(), propertyName, locator);
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
     * Decorates a log or warning message with context information on the given node.
     * @param text the original message
     * @param node the context node
     * @return the decorated text
     */
    public static String decorateWithContextInfo(String text, FONode node) {
        if (node != null) {
            StringBuffer sb = new StringBuffer(text);
            sb.append(" (").append(node.getContextInfo()).append(")");
            return sb.toString();
        } else {
            return text;
        }
    }
    
    /**
     * Returns a String containing as much context information as possible about a node. Call
     * this method only in exceptional conditions because this method may perform quite extensive
     * information gathering inside the FO tree.
     * @return a String containing context information
     * @deprecated Not localized! Should rename getContextInfoAlt() to getContextInfo() when done!
     */
    public String getContextInfo() {
        StringBuffer sb = new StringBuffer();
        if (getLocalName() != null) {
            sb.append(getName());
            sb.append(", ");
        }
        if (this.locator != null) {
            sb.append("location: ");
            sb.append(getLocatorString(this.locator));
        } else {
            String s = gatherContextInfo();
            if (s != null) {
                sb.append("\"");
                sb.append(s);
                sb.append("\"");
            } else {
                sb.append("no context info available");
            }
        }
        if (sb.length() > 80) {
            sb.setLength(80);
        }
        return sb.toString();
    }
    
    /**
     * Returns a String containing as some context information about a node. It does not take the
     * locator into consideration and returns null if no useful context information can be found.
     * Call this method only in exceptional conditions because this method may perform quite
     * extensive information gathering inside the FO tree.
     * @return a String containing 
     */
    public String getContextInfoAlt() {
        String s = gatherContextInfo();
        if (s != null) {
            StringBuffer sb = new StringBuffer();
            if (getLocalName() != null) {
                sb.append(getName());
                sb.append(", ");
            }
            sb.append("\"");
            sb.append(s);
            sb.append("\"");
            return sb.toString();
        } else {
            return null;
        }
    }
    
    /**
     * Gathers context information for the getContextInfo() method.
     * @return the collected context information or null, if none is available
     */
    protected String gatherContextInfo() {
        return null;
    }
    
    /**
     * Returns the root node of this tree
     * @return the root node
     */
    public Root getRoot() {
        return parent.getRoot();
    }

    /**
     * Returns the fully qualified name of the node
     * @return the fully qualified name of this node
     */
    public String getName() {
        return getName(getNormalNamespacePrefix());
    }

    /**
     * Returns the fully qualified name of the node
     * @param prefix the namespace prefix to build the name with (may be null)
     * @return the fully qualified name of this node
     */
    public String getName(String prefix) {
        if (prefix != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(prefix).append(':').append(getLocalName());
            return sb.toString();
        } else {
            return getLocalName();
        }
    }

    /**
     * Returns the local name (i.e. without namespace prefix) of the node
     * @return the local name of this node
     */
    public abstract String getLocalName();

    /** @return the normally ussed namespace prefix for this kind of node (ex. "fo" for XSL-FO) */
    public abstract String getNormalNamespacePrefix();
    
    /** @return the namespace URI for this node */
    public String getNamespaceURI() {
        return null;
    }
    
    /**
     * Returns the {@link Constants} class integer value of this node
     * @return the integer enumeration of this FO (e.g. FO_ROOT)
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
    
    /**
     * This method is overridden by extension elements and allows the extension element to return
     * a ContentHandlerFactory. This factory can create ContentHandler implementations that handle
     * foreign XML content by either building up a specific DOM, a Java object or something else.
     * @return the ContentHandlerFactory or null if not applicable
     */
    public ContentHandlerFactory getContentHandlerFactory() {
        return null;
    }
    
    /**
     * @return true if markers are valid children
     */
    protected boolean canHaveMarkers() {
        int foId = getNameId();
        switch (foId) {
        case Constants.FO_BASIC_LINK:
        case Constants.FO_BIDI_OVERRIDE:
        case Constants.FO_BLOCK:
        case Constants.FO_BLOCK_CONTAINER:
        case Constants.FO_FLOW:
        case Constants.FO_INLINE:
        case Constants.FO_INLINE_CONTAINER:
        case Constants.FO_LIST_BLOCK:
        case Constants.FO_LIST_ITEM:
        case Constants.FO_LIST_ITEM_BODY:
        case Constants.FO_LIST_ITEM_LABEL:
        case Constants.FO_TABLE:
        case Constants.FO_TABLE_BODY:
        case Constants.FO_TABLE_HEADER:
        case Constants.FO_TABLE_FOOTER:
        case Constants.FO_TABLE_CELL:
        case Constants.FO_TABLE_AND_CAPTION:
        case Constants.FO_TABLE_CAPTION:
        case Constants.FO_WRAPPER:
            return true;
        default:
            return false;
        }
    }
    
    /**
     * This method is used when adding child nodes to a FO that already
     * contains at least one child. In this case, the new child becomes a
     * sibling to the previous one
     * 
     * @param precedingSibling  the previous child
     * @param followingSibling  the new child
     */
    protected static void attachSiblings(FONode precedingSibling, 
                                         FONode followingSibling) {
        if (precedingSibling.siblings == null) {
            precedingSibling.siblings = new FONode[2];
        }
        if (followingSibling.siblings == null) {
            followingSibling.siblings = new FONode[2];
        }
        precedingSibling.siblings[1] = followingSibling;
        followingSibling.siblings[0] = precedingSibling;
    }
    
    /**
     * Base iterator interface over a FO's children
     *
     */
    public interface FONodeIterator extends ListIterator {
        
        /**
         * Returns the parent node for this iterator's list 
         * of child nodes
         * @return  the parent node
         */
        FObj parentNode();
        
        /**
         * Convenience method with return type of FONode
         * (should be semantically equivalent to
         * <code>(FONode) next();</code>)
         * @return the next node (if any), as a type FONode
         */
        FONode nextNode();
        
        /**
         * Convenience method with return type of FONode
         * (should be semantically equivalent to
         * <code>(FONode) previous();</code>)
         * @return the previous node (if any), as a type FONode
         */
        FONode previousNode();
        
        /**
         * Returns the first node in the list, and decreases the index,
         * so that a subsequent call to hasPrevious() will return false
         * @return the first node in the list
         * @throws NoSuchElementException if the list is empty
         */
        FONode firstNode();
        
        /**
         * Returns the last node in the list, and advances the
         * current position, so that a subsequent call to hasNext()
         * will return false
         * @return the last node in the list
         * @throws NoSuchElementException if the list is empty
         */
        FONode lastNode();

    }
}
