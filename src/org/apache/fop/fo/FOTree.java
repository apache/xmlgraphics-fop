package org.apache.fop.fo;

import org.apache.fop.datastructs.Tree;
import org.apache.fop.datastructs.SyncedCircularBuffer;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.None;
import org.apache.fop.datatypes.TextDecorations;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyValue;
import org.apache.fop.fo.expr.PropertyTriplet;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyParser;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
 * FOTree.java
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * Created: Thu Aug  2 20:29:57 2001
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Id$
 */
/**
 * <tt>FOTree</tt> is the class that generates and maintains the FO Tree.
 * It runs as a thread, so it implements the <tt>run()</tt> method.
 */

public class FOTree extends Tree implements Runnable {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * The buffer from which the <tt>XMLEvent</tt>s from the parser will
     * be read.  <tt>protected</tt> so that FONode can access it.
     */
    SyncedCircularBuffer xmlevents;
    private Thread parserThread;
    private boolean errorDump;

    /**
     */
    protected PropertyParser exprParser;

    /**
     * Args array for refineParsingMethods[].invoke() calls
     */
    Object[] args = new Object[2];

    /**
     * An Array of LinkedList[].  Each LinkedList is a stack containing the
     * most recently specified value of a particular property.  The first
     * element of each stack will contain the initial value.
     * <p>
     * The array is indexed by the same index values that are defined as
     * constants in this file, and are the effective index values for the
     * PropNames.propertyNames and classNames arrays.
     * <p>
     *  LinkedList is part of the 1.2 Collections framework.
     */
    protected LinkedList[] propertyStacks;

    /**
     * A <tt>HashMap</tt> of arrays of <tt>Numeric[]</tt> keyed on the
     * index of properties which support MAPPED_NUMERIC data types.
     */
    private HashMap mappedNumericArrays = new HashMap();

    /**
     * @param xmlevents the buffer from which <tt>XMLEvent</tt>s from the
     * parser are read.
     */
    public FOTree(SyncedCircularBuffer xmlevents)
        throws PropertyException
    {
        super();
        errorDump = Configuration.getBooleanValue("debugMode").booleanValue();
        this.xmlevents = xmlevents;
        exprParser = new PropertyParser();

        // Initialise the propertyStacks
        propertyStacks = new LinkedList[PropNames.LAST_PROPERTY_INDEX + 1];
        for (int i = 0; i <= PropNames.LAST_PROPERTY_INDEX; i++)
            propertyStacks[i] = new LinkedList();
        // Initialize the FontSize first.  Any lengths defined in ems must
        // be resolved relative to the current font size.  This may happen
        // during setup of initial values.
        try {
            // Set the initial value
            propertyStacks[PropNames.FONT_SIZE].addLast
                    (new PropertyTriplet
                     (PropNames.FONT_SIZE,
                      PropertyConsts.getInitialValue(PropNames.FONT_SIZE)));
            PropertyValue prop =
                    getInitialSpecifiedValue(PropNames.FONT_SIZE);
            if ( ! (prop instanceof Numeric)
                 || ! ((Numeric)prop).isLength())
                throw new RuntimeException(
                        "Initial font-size is not a Length");
            propertyStacks[PropNames.FONT_SIZE].addLast
                    (new PropertyTriplet(PropNames.FONT_SIZE, prop, prop));
        } catch (PropertyException e) {
            throw new RuntimeException
                ("PropertyException: " + e.getMessage());
        }


        for (int i = 0; i <= PropNames.LAST_PROPERTY_INDEX; i++) {
            String cname = "";
            if (i == PropNames.FONT_SIZE) continue;
            try {
                // Set up the initial values for each property
                propertyStacks[i].addLast
                        (new PropertyTriplet
                         (i, PropertyConsts.getInitialValue(i)));
            }
            catch (PropertyException e) {
                throw new RuntimeException
                    ("PropertyException: " + e.getMessage());
            }
        }

        // Initialise the Numeric arrays for properties with
        // MAPPED_NUMERIC datatypes
        for (int i = 0; i <= PropNames.LAST_PROPERTY_INDEX; i++ ) {
            if ((PropertyConsts.dataTypes.get(i)
                 & Properties.MAPPED_NUMERIC) != 0) {
                try {
                    Numeric[] numarray =
                            (Numeric[])
                                ((Method) (
                                    PropertyConsts.mappedNumMethods
                                      .get(Ints.consts.get(i))
                                    ))
                                .invoke(null, null);
                    mappedNumericArrays.put(Ints.consts.get(i),numarray);
                } catch (IllegalAccessException e) {
                    throw new PropertyException(e.getMessage());
                } catch (InvocationTargetException e) {
                    throw new PropertyException(e.getMessage());
                }
            }
        }
    }

    /**
     * @return a <tt>Numeric</tt> containing the current font size
     * @exception PropertyException if current font size is not defined,
     * or is not expressed as a <tt>Numeric</tt>.
     */
    public Numeric currentFontSize() throws PropertyException {
        Numeric tmpval = (Numeric)
            (((PropertyTriplet)propertyStacks[PropNames.FONT_SIZE].getLast())
                .getComputed());
        if (tmpval == null)
            throw new PropertyException("'font-size' not computed.");
        try {
            return (Numeric)(tmpval.clone());
        } catch (CloneNotSupportedException e) {
            throw new PropertyException("Clone not supported.");
        }
    }

    /**
     * Set the initial value of a particular property
     * @param value <tt>PropertyValue</tt> to set
     * @exception <tt>PropertyException</tt>
     */
    public void setInitialValue(PropertyValue value)
        throws PropertyException
    {
        int property = value.getProperty();
        propertyStacks[property].addLast
                (new PropertyTriplet(property, value));
    }

    /**
     * @param property <tt>int</t> property index
     * @param enumIndex <tt>int</t> enumerated value index into array
     *  of mapped <tt>Numeric</tt> values
     * @return <tt>Numeric</tt> corresponding to the <tt>MAPPED_NUMERIC</tt>
     *  enumeration token for this property
     */
    public Numeric getMappedNumArrayValue(int property, int enumIndex)
    {
        return
            ((Numeric[])
                (mappedNumericArrays.get(Ints.consts.get(property)))
             )[enumIndex];
    }

    /**
     * @return a <tt>TextDecorations</tt> object containing the current
     * text decorations
     * @exception PropertyException if current text decorations are not
     * defined, or are not expressed as <tt>TextDecorations</tt>.
     */
    public TextDecorations currentTextDecorations() throws PropertyException {
        TextDecorations tmpval = (TextDecorations)
            (((PropertyTriplet)
              propertyStacks[PropNames.TEXT_DECORATION].getLast())
                .getComputed());
        if (tmpval == null)
            throw new PropertyException("'text-decoration' not computed.");
        try {
            return (TextDecorations)(tmpval.clone());
        } catch (CloneNotSupportedException e) {
            throw new PropertyException("Clone not supported.");
        }
    }

    /**
     * @param index: <tt>int</tt> property index.
     * @return a <tt>PropertyTriplet</tt> containing the latest property
     * value elements for the indexed property.
     */
    public PropertyTriplet getCurrentPropertyTriplet(int index)
            throws PropertyException
    {
        return (PropertyTriplet)(propertyStacks[index].getLast());
    }

    /**
     * @param index: <tt>int</tt> property index.
     * @return a <tt>PropertyTriplet</tt> containing the property
     * value elements at the top of the stack for the indexed property.
     */
    public PropertyTriplet popPropertyTriplet(int index)
            throws PropertyException
    {
        return (PropertyTriplet)(propertyStacks[index].removeLast());
    }

    /**
     * @param index: <tt>int</tt> property index.
     * @return a <tt>PropertyTriplet</tt> containing the property
     * value elements at the bottom of the stack for the indexed property.
     */
    public PropertyTriplet getInitialValueTriplet(int index)
            throws PropertyException
    {
        return (PropertyTriplet)(propertyStacks[index].getFirst());
    }

    /**
     * @param index: <tt>int</tt> property index.
     * @return a <tt>PropertyValue</tt> containing the <em>specified</em>
     * property value at the bottom of the stack for the indexed property.
     */
    public PropertyValue getInitialSpecifiedValue(int index)
            throws PropertyException
    {
        return ((PropertyTriplet)(propertyStacks[index].getFirst()))
                .getSpecified();
    }

    /**
     * @param index: <tt>int</tt> property index.
     * @param value a <tt>PropertyTriplet</tt> containing the property
     * value elements for the indexed property.
     */
    public void pushPropertyTriplet(int index, PropertyTriplet value)
            throws PropertyException
    {
        propertyStacks[index].addLast(value);
        return;
    }

    /**
     * @param index: <tt>int</tt> property index.
     * @return a <tt>PropertyValue</tt> containing the latest computed
     * property value for the indexed property.
     */
    public PropertyValue getCurrentComputed(int index)
            throws PropertyException
    {
        return getCurrentPropertyTriplet(index).getComputed();
    }

    /**
     * @param index: <tt>int</tt> property index.
     * @return a <tt>PropertyValue</tt> containing the inherited
     * property value for the indexed property; i.e. computed if available,
     * else specified.
     */
    public PropertyValue getCurrentInherited(int index)
            throws PropertyException
    {
        return getCurrentPropertyTriplet(index).getComputedOrSpecified();
    }

    public void setParserThread(Thread parserThread) {
        this.parserThread = parserThread;
    }

    /**
     * The <tt>run</tt> method() invoked by the call of <tt>start</tt>
     * on the thread in which runs off FOTree.
     */
    public void run() {
        FoRoot foRoot;
        XMLEvent event;
        try {
            // Dummy only - check the language and country setup
            System.out.println((String)Configuration.getHashMapEntry
                               ("countriesMap","AU"));
            System.out.println((String)Configuration.getHashMapEntry
                               ("languagesMap","EN"));
            System.out.println((String)Configuration.getHashMapEntry
                               ("scriptsMap","Pk"));
            // Let the parser look after STARTDOCUMENT and the correct
            // positioning of the root element
            event = XMLEvent.getStartElement
                    (xmlevents, XMLEvent.XSLNSpaceIndex, "root");
            //if (event != null) {
                //System.out.println("FOTree:" + event);
            //}
            foRoot = new FoRoot(this, event);
            foRoot.buildFoTree();
            System.out.println("Back from buildFoTree");
            XMLEvent.getEndDocument(xmlevents);
        } catch (Exception e) {
            if (errorDump) Driver.dumpError(e);
            if (parserThread != null) {
                try {
                    parserThread.interrupt();
                } catch (Exception ex) {} // Ignore
            }
            // Now propagate a Runtime exception
            throw new RuntimeException(e.getMessage());
        }
    }

}// FOTree
