package org.apache.fop.fo;

import org.apache.fop.datastructs.Tree;
import org.apache.fop.datastructs.SyncedCircularBuffer;
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
    protected final LinkedList[] propertyStacks;

    /**
     * @param xmlevents the buffer from which <tt>XMLEvent</tt>s from the
     * parser are read.
     */
    public FOTree(SyncedCircularBuffer xmlevents) {
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
            try {
                // Set the initial value
                PropertyConsts.initialValueMethods.get
                    (PropNames.FONT_SIZE).invoke(null, new Object[]{this});
            }
            catch (IllegalArgumentException e) {
                throw new RuntimeException(
                    "Illegal argument on \"" + e.getMessage()
                    + "\" in class FontSize");
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(
                    "Illegal access on \"" + e.getMessage()
                    + "\" in class FontSize");
            }
            catch (InvocationTargetException e) {
                Throwable targetex = e.getTargetException();
                throw new RuntimeException(
                    "Invocation target exception on \""
                    + targetex.getMessage() + "\" in class FontSize");
            }
            PropertyValue prop = Properties.FontSize.getInitialValue();
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
                Class vclass = PropertyConsts.propertyClasses.get(i);
                cname = vclass.getName();
                // Set up the initial values for each property
                // Note that initial (specified) values are stored as
                // unprocessed strings which can then be subject to the same
                // processing as actual specified strings.
                switch (PropertyConsts.getInitialValueType(i)) {
                case Properties.NOTYPE_IT:
                    propertyStacks[i].addLast(new PropertyTriplet(i, null));
                    break;
                case Properties.ENUM_IT:
                case Properties.BOOL_IT:
                case Properties.INTEGER_IT:
                case Properties.NUMBER_IT:
                case Properties.LENGTH_IT:
                case Properties.ANGLE_IT:
                case Properties.PERCENTAGE_IT:
                case Properties.CHARACTER_IT:
                case Properties.LITERAL_IT:
                case Properties.NAME_IT:
                case Properties.URI_SPECIFICATION_IT:
                case Properties.COLOR_IT:
                case Properties.TEXT_DECORATION_IT:
                    // Set the initial value
                    try {
                        PropertyConsts.initialValueMethods.get
                                (i).invoke(null, new Object[]{this});
                    }
                    catch (IllegalArgumentException e) {
                        throw new RuntimeException(
                            "Illegal argument on \"" + e.getMessage()
                            + "\" in class " + cname);
                    }
                    catch (IllegalAccessException e) {
                        throw new RuntimeException(
                            "Illegal access on \"" + e.getMessage()
                            + "\" in class " + cname);
                    }
                    catch (InvocationTargetException e) {
                        Throwable targetex = e.getTargetException();
                        throw new RuntimeException(
                            "Invocation target exception on \""
                            + targetex.getMessage() + "\" in class " + cname);
                    }

                    propertyStacks[i].addLast(new PropertyTriplet(
                        i,
                        (PropertyValue)
                        (PropertyConsts.
                         propertyClasses.get(i).
                         getDeclaredField("initialValue").get(null))
                        ));
                    break;
                case Properties.AUTO_IT:
                    propertyStacks[i].addLast
                        (new PropertyTriplet(i, new Auto(i)));
                    break;
                case Properties.NONE_IT:
                    propertyStacks[i].addLast
                        (new PropertyTriplet(i, new None(i)));
                    break;
                case Properties.AURAL_IT:
                    propertyStacks[i].addLast(new PropertyTriplet(i, null));
                    break;
                default:
                    throw new RuntimeException
                            ("Unknown initial value type "
                             + PropertyConsts.getInitialValueType(i)
                             + " for class " + cname);
                }
            }
            catch (NoSuchFieldException e) {
                throw new RuntimeException(
                            "Missing field \"" + e.getMessage() + "\""
                            + " in class " + cname);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(
                    "Illegal access on \"" + e.getMessage() + "\" in class " +
                    cname);
            }
            catch (PropertyException e) {
                throw new RuntimeException
                    ("PropertyException: " + e.getMessage());
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
    public PropertyTriplet getInitialValue(int index)
            throws PropertyException
    {
        return (PropertyTriplet)(propertyStacks[index].getFirst());
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
