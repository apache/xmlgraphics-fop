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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.fo.extensions.svg.SVGElementMapping;
import org.apache.fop.fo.flow.ChangeBar;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.utils.text.AdvancedMessageFormat.Function;

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

    /** The list of active change bars for the given node */
    protected List<ChangeBar> nodeChangeBarList;

    /** The list of active change bars for the start of the given node  */
    protected List<ChangeBar> startOfNodeChangeBarList;

    /**
     * Marks the location of this object from the input FO
     *   <br>Call <code>locator.getSystemId()</code>,
     *   <code>getLineNumber()</code>,
     *   <code>getColumnNumber()</code> for file, line, column
     *   information
     */
    protected Locator locator;

    /** Logger for fo-tree related messages **/
    protected static final Log log = LogFactory.getLog(FONode.class);

    /**
     * Base constructor
     *
     * @param parent parent of this node
     */
    protected FONode(FONode parent) {
        this.parent = parent;
    }

    /**
     * Performs a shallow cloning operation, sets the clone's parent,
     * and optionally cleans the list of child nodes
     *
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

    /** {@inheritDoc} */
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
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

    /**
     * Returns the <code>Locator</code> containing the location information for this
     * element, or <code>null</code> if not available
     *
     * @return the location information for this element or <code>null</code>, if not available
     */
    public Locator getLocator() {
        return this.locator;
    }

    /**
     * Recursively goes up the FOTree hierarchy until the <code>fo:root</code>
     * is found, which returns the parent <code>FOEventHandler</code>.
     * <br>(see also: {@link org.apache.fop.fo.pagination.Root#getFOEventHandler()})
     *
     * @return the FOEventHandler object that is the parent of the FO Tree
     */
    public FOEventHandler getFOEventHandler() {
        return parent.getFOEventHandler();
    }

    /**
     * Returns the context class providing information used during FO tree building.
     * @return the builder context
     */
    public FOTreeBuilderContext getBuilderContext() {
        return parent.getBuilderContext();
    }

    /**
     * Indicates whether this node is a child of an fo:marker.
     * @return true if this node is a child of an fo:marker
     */
    protected boolean inMarker() {
        return getBuilderContext().inMarker();
    }

    /**
     * Tests if the given element is a change bar element.
     *
     * @param namespaceURI The name space of the element
     * @param localName The local name of the element
     * @return A boolean value true if the given element is a change bar element
     */
    public boolean isChangeBarElement(String namespaceURI, String localName) {
        return FO_URI.equals(namespaceURI)
                && (localName.equals("change-bar-begin") || localName.equals("change-bar-end"));
    }

    /**
     * Returns the user agent that is associated with the
     * tree's <code>FOEventHandler</code>.
     *
     * @return the user agent
     */
    public FOUserAgent getUserAgent() {
        return getFOEventHandler().getUserAgent();
    }

    /**
     * Returns the logger for the node.
     *
     * @return the logger
     */
    public Log getLogger() {
        return log;
    }

    /**
     * Initialize the node with its name, location information, and attributes
     * The attributes must be used immediately as the sax attributes
     * will be altered for the next element.
     *
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
     *
     * @param pList the closest parent propertylist.
     * @param foEventHandler The FOEventHandler where the PropertyListMaker
     *              instance can be found.
     * @return A new property list.
     * @throws FOPException if there's a problem during processing
     */
    protected PropertyList createPropertyList(
                                PropertyList pList,
                                FOEventHandler foEventHandler)
                throws FOPException {
        return null;
    }

    /**
     * Checks to make sure, during SAX processing of input document, that the
     * incoming node is valid for this (parent) node (e.g., checking to
     * see that <code>fo:table</code> is not an immediate child of <code>fo:root</code>)
     * called from {@link FOTreeBuilder#startElement(String, String, String, Attributes)}
     * before constructing the child {@link FObj}.
     *
     * @param loc location in the FO source file
     * @param namespaceURI namespace of incoming node
     * @param localName name of the incoming node (without namespace prefix)
     * @throws ValidationException if incoming node not valid for parent
     */
    protected void validateChildNode(
                                Locator loc,
                                String namespaceURI,
                                String localName)
            throws ValidationException {
        //nop
    }

    /**
     * Static version of {@link FONode#validateChildNode(Locator, String, String)} that
     * can be used by subclasses that need to validate children against a different node
     * (for example: <code>fo:wrapper</code> needs to check if the incoming node is a
     *  valid child to its parent)
     *
     * @param fo    the {@link FONode} to validate against
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
     * Adds characters. Does nothing by default. To be overridden in subclasses
     * that allow <code>#PCDATA</code> content.
     *
     * @param data array of characters containing text to be added
     * @param start starting array element to add
     * @param end ending array element to add
     * @param pList currently applicable PropertyList
     * @param locator location in the XSL-FO source file.
     * @throws FOPException if there's a problem during processing
     * @deprecated Please override {@link #characters(char[], int, int, PropertyList, Locator)}
     *          instead!
     */
    @Deprecated
    protected void addCharacters(char[] data, int start, int end,
                                 PropertyList pList,
                                 Locator locator) throws FOPException {
        // ignore
    }

    /**
     * Adds characters. Does nothing by default. To be overridden in subclasses
     * that allow <code>#PCDATA</code> content.
     *
     * @param data array of characters containing text to be added
     * @param start starting array element to add
     * @param length number of elements to add
     * @param pList currently applicable PropertyList
     * @param locator location in the XSL-FO source file.
     * @throws FOPException if there's a problem during processing
     */
    protected void characters(char[] data, int start, int length,
                                 PropertyList pList,
                                 Locator locator) throws FOPException {
        addCharacters(data, start, start + length, pList, locator);
    }

    /**
     * Called after processNode() is called. Subclasses can do additional processing.
     *
     * @throws FOPException if there's a problem during processing
     */
    public void startOfNode() throws FOPException {
        // do nothing by default
   }

    /**
     * Primarily used for making final content model validation checks
     * and/or informing the {@link FOEventHandler} that the end of this FO
     * has been reached.
     * The default implementation simply calls {@link #finalizeNode()}, without
     * sending any event to the {@link FOEventHandler}.
     * <p><i>Note: the recommended way to override this method in subclasses is</i></p>
     * <p><code>super.endOfNode(); // invoke finalizeNode()</code></p>
     * <p><code>getFOEventHandler().endXXX(); // send endOfNode() notification</code></p>
     *
     * @throws FOPException if there's a problem during processing
     */
    public void endOfNode() throws FOPException {
        this.finalizeNode();
    }

    /**
     * Adds a node as a child of this node. The default implementation of this method
     * just ignores any child node being added.
     *
     * @param child child node to be added to the childNodes of this node
     * @throws FOPException if there's a problem during processing
     */
    protected void addChildNode(FONode child) throws FOPException {
        // do nothing by default
    }

    /**
     * Removes a child node. Used by the child nodes to remove themselves, for
     * example table-body if it has no children.
     *
     * @param child child node to be removed
     */
    public void removeChild(FONode child) {
        //nop
    }

    /**
     * Finalize this node.
     * This method can be overridden by subclasses to perform finishing
     * tasks (cleanup, validation checks, ...) without triggering
     * endXXX() events in the {@link FOEventHandler}.
     * The method is called by the default {@link #endOfNode()}
     * implementation.
     *
     * @throws FOPException in case there was an error
     */
    public void finalizeNode() throws FOPException {
        // do nothing by default
    }

    /**
     * Return the parent node of this node
     *
     * @return the parent node of this node
     */
    public FONode getParent() {
        return this.parent;
    }

    /**
     * Return an iterator over all the child nodes of this node.
     *
     * @return the iterator over the FO's childnodes
     */
    public FONodeIterator getChildNodes() {
        return null;
    }

    /**
     * Return an iterator over the object's child nodes starting
     * at the passed node.
     *
     * @param childNode First node in the iterator
     * @return the iterator, or <code>null</code> if
     *  the given node is not a child of this node.
     */
    public FONodeIterator getChildNodes(FONode childNode) {
        return null;
    }

    /**
     * Return a {@link CharIterator} over all characters in this node
     *
     * @return an iterator for the characters in this node
     */
    public CharIterator charIterator() {
        return new OneCharIterator(CharUtilities.CODE_EOT);
    }

    /**
     * Helper function to obtain standard usage prefix for FOP related
     * namespace URIs.
     * @param namespaceURI URI of node found
     *         (e.g., "http://www.w3.org/1999/XSL/Format")
     * @return the prefix or null if none
     */
    public static String getNodePrefix(String namespaceURI) {
        if (namespaceURI.equals(FOElementMapping.URI)) {
            return "fo";
        } else if (namespaceURI.equals(ExtensionElementMapping.URI)) {
            return "fox";
        } else if (namespaceURI.equals(InternalElementMapping.URI)) {
            return "foi";
        } else if (namespaceURI.equals(SVGElementMapping.URI)) {
            return "svg";
        } else {
            return null;
        }
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
        String prefix = getNodePrefix(namespaceURI);
        if (prefix != null) {
            return prefix + ":" + localName;
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
        return FOValidationEventProducer.Provider.get(
                getUserAgent().getEventBroadcaster());
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
     * (e.g., two <code>fo:declarations</code> within <code>fo:root</code>)
     *
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
     * (e.g., <code>fo:layout-master-set</code> appearing after <code>fo:page-sequence</code>)
     *
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
     * (e.g., <code>fo:block</code> appearing immediately under <code>fo:root</code>)
     *
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param nsURI namespace URI of incoming invalid node
     * @param lName local name (i.e., no prefix) of incoming node
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void invalidChildError(Locator loc, String nsURI, String lName)
                throws ValidationException {
        invalidChildError(loc, getName(), nsURI, lName, null);
    }

    /**
     * Helper function to return "invalid child" exceptions with more
     * complex validation rules (i.e., needing more explanation of the problem)
     *
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param parentName the name of the parent element
     * @param nsURI namespace URI of incoming offending node
     * @param lName local name (i.e., no prefix) of incoming offending node
     * @param ruleViolated name of the rule violated (used to lookup a resource in a bundle)
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void invalidChildError(Locator loc, String parentName, String nsURI, String lName,
                String ruleViolated)
                throws ValidationException {
        String prefix = getNodePrefix(nsURI);
        QName qn; // qualified name of offending node
        if (prefix != null) {
            qn = new QName(nsURI, prefix, lName);
        } else {
            qn = new QName(nsURI, lName);
        }
        getFOValidationEventProducer().invalidChild(this, parentName, qn, ruleViolated, loc);
    }

    /**
     * Helper function to return "not supported child" exceptions. Note that the child is valid, just not
     * supported yet by FOP.
     *
     * @param loc org.xml.sax.Locator object of the error (*not* parent node)
     * @param nsURI namespace URI of incoming invalid node
     * @param lName local name (i.e., no prefix) of incoming node
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void notSupportedChildError(Locator loc, String nsURI, String lName) throws ValidationException {
        getFOValidationEventProducer().notSupportedChild(this, getName(), new QName(nsURI, lName), loc);
    }

    /**
     * Helper function to throw an error caused by missing mandatory child elements.
     * (e.g., <code>fo:layout-master-set</code> not having any <code>fo:page-master</code>
     *  child element.
     *
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
     *
     * @param propertyName the name of the missing property.
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void missingPropertyError(String propertyName)
                throws ValidationException {
        getFOValidationEventProducer().missingProperty(this, getName(), propertyName, locator);
    }



    /**
     * Helper function to throw an error caused by an invalid property
     *
     * @param propertyName the name of the property.
     * @param propertyValue the value of the property.
     * @param e optional property parsing exception.
     * @throws ValidationException the validation error provoked by the method call
     */
    protected void invalidPropertyValueError(String propertyName, String propertyValue, Exception e)
                throws ValidationException {
        getFOValidationEventProducer().invalidPropertyValue(this, getName(), propertyName,
                propertyValue,  new PropertyException(e), locator);
    }

    /**
     * Helper function to return "Error(line#/column#)" string for
     * above exception messages
     *
     * @param loc org.xml.sax.Locator object
     * @return String opening error text
     */
    protected static String errorText(Locator loc) {
        return "Error(" + getLocatorString(loc) + "): ";
    }

    /**
     * Helper function to return "Warning(line#/column#)" string for
     * warning messages
     *
     * @param loc org.xml.sax.Locator object
     * @return String opening warning text
     */
    protected static String warningText(Locator loc) {
        return "Warning(" + getLocatorString(loc) + "): ";
    }

    /**
     * Helper function to format a Locator instance.
     *
     * @param loc org.xml.sax.Locator object
     * @return String the formatted text
     */
    public static String getLocatorString(Locator loc) {
        if (loc == null) {
            return "Unknown location";
        } else {
            return loc.getLineNumber() + ":" + loc.getColumnNumber();
        }
    }

    /**
     * Decorates a log or warning message with context information on the given node.
     *
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
     */
    // [GA] remove deprecation - no alternative specified
    //  @deprecated Not localized! Should rename getContextInfoAlt() to getContextInfo() when done!
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
     * extensive information gathering inside the FO tree. All text returned by this method that
     * is not extracted from document content needs to be locale-independent.
     * @return a String containing context information
     */
    protected String getContextInfoAlt() {
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

    /** Function for AdvancedMessageFormat to retrieve context info from an FONode. */
    public static class GatherContextInfoFunction implements Function {

        /** {@inheritDoc} */
        public Object evaluate(Map params) {
            Object obj = params.get("source");
            if (obj instanceof PropertyList) {
                PropertyList propList = (PropertyList)obj;
                obj = propList.getFObj();
            }
            if (obj instanceof FONode) {
                FONode node = (FONode)obj;
                return node.getContextInfoAlt();
            }
            return null;
        }

        /** {@inheritDoc} */
        public Object getName() {
            return "gatherContextInfo";
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
     *
     * @return the root node
     */
    public Root getRoot() {
        return parent.getRoot();
    }

    /**
     * Returns the fully qualified name of the node
     *
     * @return the fully qualified name of this node
     */
    public String getName() {
        return getName(getNormalNamespacePrefix());
    }

    /**
     * Returns the fully qualified name of the node
     *
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
     *
     * @return the local name of this node
     */
    public abstract String getLocalName();

    /**
     * Returns the normally used namespace prefix for this node
     *
     * @return the normally used namespace prefix for this kind of node (ex. "fo" for XSL-FO)
     */
    public abstract String getNormalNamespacePrefix();

    /**
     * Returns the namespace URI for this node
     *
     * @return the namespace URI for this node
     */
    public String getNamespaceURI() {
        return null;
    }

    /**
     * Returns the {@link Constants} class integer value of this node
     *
     * @return the integer enumeration of this FO (e.g. {@link Constants#FO_ROOT})
     *      if a formatting object, {@link Constants#FO_UNKNOWN_NODE} otherwise
     */
    public int getNameId() {
        return Constants.FO_UNKNOWN_NODE;
    }

    /**
     * This method is overridden by extension elements and allows the extension element
     * to return a pass-through attachment which the parent formatting objects should simply
     * carry with them but otherwise ignore. This mechanism is used to pass non-standard
     * information from the FO tree through to the layout engine and the renderers.
     *
     * @return the extension attachment if one is created by the extension element, null otherwise.
     */
    public ExtensionAttachment getExtensionAttachment() {
        return null;
    }

    /**
     * This method is overridden by extension elements and allows the extension element to return
     * a {@link ContentHandlerFactory}. This factory can create ContentHandler implementations that
     * handle foreign XML content by either building up a specific DOM, a Java object or something
     * else.
     *
     * @return the <code>ContentHandlerFactory</code> or <code>null</code> if not applicable
     */
    public ContentHandlerFactory getContentHandlerFactory() {
        return null;
    }

    /**
     * Returns <code>true</code> if <code>fo:marker</code> is allowed as
     * a child node.
     * <br>To be overridden <i>only</i> in extension nodes that need it.
     *
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
     * Determine if node has a delimited text range boundary. N.B. that we report
     * this to be true by default, while specific subclasses override this method to report false.
     * @param boundary one of {EN_BEFORE, EN_AFTER, or EN_BOTH} enumeration constants
     * @return true if indicated boundary (or boundaries) constitute a delimited text range
     * boundary.
     */
    public boolean isDelimitedTextRangeBoundary(int boundary) {
        return true;
    }

    /**
     * Collect the sequence of delimited text ranges, where each new
     * range is pushed onto RANGES.
     * @param ranges a stack of delimited text ranges
     * @return the (possibly) updated stack of delimited text ranges
     */
    public Stack<DelimitedTextRange> collectDelimitedTextRanges(Stack<DelimitedTextRange> ranges) {
        // if boundary before, then push new range
        if (isRangeBoundaryBefore()) {
            maybeNewRange(ranges);
        }
        // get current range, if one exists
        DelimitedTextRange currentRange;
        if (ranges.size() > 0) {
            currentRange = ranges.peek();
        } else {
            currentRange = null;
        }
        // proceses this node
        ranges = collectDelimitedTextRanges(ranges, currentRange);
        // if boundary after, then push new range
        if (isRangeBoundaryAfter()) {
            maybeNewRange(ranges);
        }
        return ranges;
    }

    /**
     * Collect the sequence of delimited text ranges, where each new
     * range is pushed onto RANGES, where default implementation collects ranges
     * of child nodes.
     * @param ranges a stack of delimited text ranges
     * @param currentRange the current range or null (if none)
     * @return the (possibly) updated stack of delimited text ranges
     */
    protected Stack<DelimitedTextRange> collectDelimitedTextRanges(
            Stack<DelimitedTextRange> ranges, DelimitedTextRange currentRange) {

        for (FONodeIterator it = getChildNodes(); (it != null) && it.hasNext();) {
            ranges = it.next().collectDelimitedTextRanges(ranges);
        }
        return ranges;
    }

    /**
     * Determine if this node is a new bidi RANGE block item.
     * @return true if this node is a new bidi RANGE block item
     */
    public boolean isBidiRangeBlockItem() {
        return false;
    }

    /**
     * Conditionally add a new delimited text range to RANGES. Always add new
     * range unless there are no ranges on the stack yet and this node is not a block item.
     * @param ranges stack of delimited text ranges
     * @return new range (if constructed and pushed onto stack) or current range (if any) or null
     */
    private DelimitedTextRange maybeNewRange(Stack<DelimitedTextRange> ranges) {
        DelimitedTextRange rCur = !ranges.empty() ? ranges.peek() : null;
        DelimitedTextRange rNew;
        if ((rCur != null) || isBidiRangeBlockItem()) {
            rNew = new DelimitedTextRange(this);
        } else {
            rNew = null;
        }
        if (rNew != null) {
            ranges.push(rNew);
        } else {
            rNew = rCur;
        }
        return rNew;
    }

    private boolean isRangeBoundaryBefore() {
        return isDelimitedTextRangeBoundary(Constants.EN_BEFORE);
    }

    private boolean isRangeBoundaryAfter() {
        return isDelimitedTextRangeBoundary(Constants.EN_AFTER);
    }

    /**
     * Base iterator interface over a FO's children, offering three methods on top of the base interface
     * methods.
     */
    public interface FONodeIterator extends ListIterator<FONode> {

        /** @return the next node */
        FONode next();

        /** @return the previous node */
        FONode previous();

        /**
         * Replace the node at the current index with the given <code>newNode</code>.
         *
         * @param newNode the new node
         */
        void set(FONode newNode);

        /**
         * Add the given <code>newNode</code> at the current position.
         *
         * @param newNode the new node
         */
        void add(FONode newNode);

        /** @return <code>true</code> if there is a next node, <code>false</code> otherwise */
        boolean hasNext();

        /** @return <code>true</code> if there is a previous node, <code>false</code> otherwise */
        boolean hasPrevious();

        /** @return the current index */
        int nextIndex();

        /** @return the previous index */
        int previousIndex();

        /**
         * Removes the node at the current position.
         */
        void remove();

        /**
         * Returns the parent node for this iterator's list
         * of child nodes
         *
         * @return  the parent node
         */
        FObj parent();

        /**
         * Returns the first node in the list, and decreases the index,
         * so that a subsequent call to <code>hasPrevious()</code> will
         * return <code>false</code>
         *
         * @return the first node in the list
         */
        FONode first();

        /**
         * Returns the last node in the list, and advances the
         * current position, so that a subsequent call to <code>hasNext()</code>
         * will return <code>false</code>
         *
         * @return the last node in the list
         */
        FONode last();

    }

    /**
     * Returns the list of active change bars.
     *
     * @return The list of active change bars
     */
    public List<ChangeBar> getChangeBarList() {
        return nodeChangeBarList;
    }

    /**
     * Sets the structure tree element.
     *
     * @param structureTreeElement set.
     */
    public void setStructureTreeElement(StructureTreeElement structureTreeElement) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the structure tree element associated to this object.
     *
     * @return the structure tree element
     */
    public StructureTreeElement getStructureTreeElement() {
        return null;
    }

}
