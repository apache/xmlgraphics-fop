/*
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * $Id$
 */

package org.apache.fop.fo;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.flow.*;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;

/**
 * Data class for common data and methods relating to Flow Objects.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Id$
 */

public class FObjects {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String packageNamePrefix = Fop.fopPackage;

    /**
     * Create a singleton FObjects object
     */
    public static final FObjects fobjects;
    static {
        fobjects = new FObjects();
    }

    /**
     * @return the singleton
     */
    public static final FObjects getFObjects() {
        return fobjects;
    }

    /**
     * FObjects cannot be instantiated
     */
    private FObjects() {}

    /**
     * A String[] array of the fo class names.  This array is
     * effectively 1-based, with the first element being unused.
     * The array is initialized in a static initializer by converting the
     * fo names from the array FObjectNames.foLocalNames into class names by
     * converting the first character of every component word to upper case,
     * removing all punctuation characters and prepending the prefix 'Fo'.
     *  It can be indexed by the fo name constants defined in the
     * <tt>FObjectNames</tt> class.
     */
    private static final String[] foClassNames;

    /**
     * A String[] array of the fo class package names.  This array is
     * effectively 1-based, with the first element being unused.
     * The array is initialized in a static initializer by constructing
     * the package name from the common package prefix set in the field
     * <tt>packageNamePrefix</tt>, the package name suffix associated with
     * the fo local names in the <tt>FObjectNames.foLocalNames</tt> array,
     * the the class name which has been constructed in the
     * <tt>foClassNames</tt> array here.
     *  It can be indexed by the fo name constants defined in the
     * <tt>FObjectNames</tt> class.
     */
    private static final String[] foPkgClassNames;

    /**
     * An Class[] array containing Class objects corresponding to each of the
     * class names in the foClassNames array.  It is initialized in a static
     * initializer in parallel with the creation of the class names in the
     * foClassNames array.  It can be indexed by the class name constants
     * defined in this file.
     *
     * It is not guaranteed that there exists a class corresponding to each of
     * the FlowObjects defined in this file.
     */
    private final Constructor[] foConstructors
                        = new Constructor[FObjectNames.foLocalNamesLength];

    /**
     * The default constructor arguments for an FObject. <b>N.B.</b> not
     * all subclasses of <tt>FONode</tt> use this constructor; e.g.
     * <tt>FoRoot</tt>, <tt>FoPageSequence</tt> &amp; <tt>FoFlow</tt>.
     * Generally these FObjects are not invoked through reflection.  If such
     * invocation becomes necessary for a particular class, a contructor of
     * this kind must be added to the class.
     * <p>At present, the only difference is in the addition of the
     * <tt>int.class</tt> constructor argument.
     */
    protected static final Class[] defaultConstructorArgs =
        new Class[] {
            FOTree.class
            ,FONode.class
            ,FOPageSeqNode.class
            ,FoXmlEvent.class
            ,int.class
    };
    
    /**
     * A HashMap whose elements are an integer index value keyed by an
     * fo local name.  The index value is the index of the fo local name in
     * the FObjectNames.foLocalNames[] array.
     * It is initialized in a static initializer.
     */
    private static final HashMap foToIndex;

    /**
     * A HashMap whose elements are an integer index value keyed by the name
     * of a fo class.  The index value is the index of the fo
     * class name in the foClassNames[] array.  It is initialized in a
     * static initializer.
     */
    private static final HashMap foClassToIndex;

    static {
        String prefix = packageNamePrefix + ".";
        String foPrefix = "Fo";
        int namei = 0;	// Index of localName in FObjectNames.foLocalNames
        int pkgi = 1;	// Index of package suffix in foLocalNames

        foClassNames    = new String[FObjectNames.foLocalNamesLength];
        foPkgClassNames = new String[FObjectNames.foLocalNamesLength];
        foToIndex       = new HashMap(
                (int)(FObjectNames.foLocalNamesLength / 0.75) + 1);
        foClassToIndex  = new HashMap(
                (int)(FObjectNames.foLocalNamesLength / 0.75) + 1);

        for (int i = 1; i < FObjectNames.foLocalNamesLength; i++) {
            String cname = foPrefix;
            String foName;
            String pkgname;
            try {
                foName = FObjectNames.getFOName(i);
                pkgname = FObjectNames.getFOPkg(i);
            } catch (FOPException fex) {
                throw new RuntimeException(fex.getMessage());
            }
            StringTokenizer stoke =
                    new StringTokenizer(foName, "-");
            while (stoke.hasMoreTokens()) {
                String token = stoke.nextToken();
                String pname = new Character(
                                    Character.toUpperCase(token.charAt(0))
                                ).toString() + token.substring(1);
                cname = cname + pname;
            }
            foClassNames[i] = cname;

            // Set up the array of class package names
            // Set up the array of Class objects, indexed by the fo
            // constants.
            String name = prefix + pkgname + "." + cname;
            foPkgClassNames[i] = name;

            // Set up the foToIndex Hashmap with the name of the
            // flow object as a key, and the integer index as a value
            if (foToIndex.put(foName,
                                        Ints.consts.get(i)) != null) {
                throw new RuntimeException(
                    "Duplicate values in propertyToIndex for key " +
                    foName);
            }

            // Set up the foClassToIndex Hashmap with the name of the
            // fo class as a key, and the integer index as a value
            
            if (foClassToIndex.put(foClassNames[i],
                                    Ints.consts.get(i)) != null) {
                throw new RuntimeException(
                    "Duplicate values in foClassToIndex for key " +
                    foClassNames[i]);
            }

        }
    }

    /**
     * This method generates generates new FO objects, except for FoPcdata
     * objects, which require an XmlEvent argument.  Use only when it is
     * known that no CHARACTERS event will be passed.
     * @param foTree the FO tree to which this node is being added
     * @param pageSequence the ancestor page sequence of this node
     * @param parent of this node
     * @param event the <code>FoXmlEvent</code> event that triggered the
     * generation of this FO
     * @param stateFlags set conditons determined by the ancestry of this
     * node.  These conditions are used to test validity of this node at this
     * point in the FO tree.
     * @return the new FO node
     * @throws FOPException
     */
    public Object makePageSeqFOChild(
            FOTree foTree, FONode pageSequence, FOPageSeqNode parent,
            FoXmlEvent event, int stateFlags)
        throws FOPException
    {
        int foType = event.getFoType();
        switch (foType) {
        case FObjectNames.BASIC_LINK:
            return new FoBasicLink(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.BIDI_OVERRIDE:
            return new FoBidiOverride(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.BLOCK:
            return new FoBlock(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.BLOCK_CONTAINER:
            return new FoBlockContainer(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.CHARACTER:
            return new FoCharacter(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.EXTERNAL_GRAPHIC:
            return new FoExternalGraphic(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.FLOAT:
            return new FoFloat(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.FOOTNOTE:
            return new FoFootnote(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.FOOTNOTE_BODY:
            return new FoFootnoteBody(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.INITIAL_PROPERTY_SET:
            return new FoInitialPropertySet(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.INLINE:
            return new FoInline(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.INLINE_CONTAINER:
            return new FoInlineContainer(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.INSTREAM_FOREIGN_OBJECT:
            return new FoInstreamForeignObject(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LEADER:
            return new FoLeader(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LIST_BLOCK:
            return new FoListBlock(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LIST_ITEM:
            return new FoListItem(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LIST_ITEM_BODY:
            return new FoListItemBody(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.LIST_ITEM_LABEL:
            return new FoListItemLabel(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MARKER:
            return new FoMarker(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_CASE:
            return new FoMultiCase(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_PROPERTIES:
            return new FoMultiProperties(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_PROPERTY_SET:
            return new FoMultiPropertySet(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_SWITCH:
            return new FoMultiSwitch(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.MULTI_TOGGLE:
            return new FoMultiToggle(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.PAGE_NUMBER:
            return new FoPageNumber(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.PAGE_NUMBER_CITATION:
            return new FoPageNumberCitation(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.RETRIEVE_MARKER:
            return new FoRetrieveMarker(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE:
            return new FoTable(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_AND_CAPTION:
            return new FoTableAndCaption(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_BODY:
            return new FoTableBody(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_CAPTION:
            return new FoTableCaption(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_CELL:
            return new FoTableCell(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_COLUMN:
            return new FoTableColumn(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_FOOTER:
            return new FoTableFooter(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_HEADER:
            return new FoTableHeader(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.TABLE_ROW:
            return new FoTableRow(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.WRAPPER:
            return new FoWrapper(foTree, pageSequence, parent, event, stateFlags);
        case FObjectNames.PCDATA:
            throw new FOPException(
                    "Attempt to make FoPcdata() with FoXmlEvent");
        default:
            throw new FOPException
            ("FO type value: " + foType + " not supported in makePageSeqFOChild");
            
        }
    }

    /**
     * This method generates generates new FO objects, including FoPcdata
     * objects.  It is more general in this sense than the overloaded
     * version which takes the <code>FoXmlEvent event</code> parameter.
     * @param foTree
     * @param parent
     * @param event the <code>XmlEvent</code> which triggered the generation
     * of this fo
     * @param stateFlags
     * @return
     * @throws FOPException
     */
    public Object makePageSeqFOChild(FOTree foTree, FONode pageSequence,
            FOPageSeqNode parent, XmlEvent event, int stateFlags)
    throws FOPException
    {
        if (event instanceof FoXmlEvent) {
            return makePageSeqFOChild(
                    foTree, pageSequence, parent, (FoXmlEvent)event, stateFlags);
        }
        if (event.getType() != XmlEvent.CHARACTERS) {
            throw new FOPException(
                    "Attempt to makeFlowObject() with XmlEvent for event type "
                    + XmlEvent.eventTypeName(event.getType()));
        }
        return new FoPcdata(foTree, pageSequence, parent, event, stateFlags);
    }

    /**
     * Get the index of an unqualified FO class name
     * @param name of the FO
     * @return the index
     */
    public static int getFoIndex(String name) {
        return ((Integer)(foToIndex.get(name))).intValue();
    }

    /**
     * Get the unqualified class name of the indicated FO
     * @param foIndex of the rwquired FO
     * @return the unqualified class name
     */
    public static String getClassName(int foIndex) {
        return foClassNames[foIndex];
    }

    /**
     * Get the fully-qualified class name of the indicated FO
     * @param foIndex of the required FO
     * @return the fully-qualified class name
     */
    public static String getPkgClassName(int foIndex) {
        return foPkgClassNames[foIndex];
    }

    /**
     * Get the <code>Constructor</code> object for a given FO
     * @param foIndex of the FO
     * @return the <code>Constructor</code>
     */
    public Constructor getConstructor(int foIndex) {
        return foConstructors[foIndex];
    }

}

