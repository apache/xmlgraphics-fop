/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Id$
 */

package org.apache.fop.fo;

import java.lang.Character;

import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

// Only for tree property set partitions
import java.util.BitSet;
import java.util.Iterator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.PropNames;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datastructs.ROBitSet;

/**
 * Data class relating sets of properties to Flow Objects.
 */

public class FOPropertySets {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String XSLNamespace =
                                        "http://www.w3.org/1999/XSL/Format";

    public static final String packageNamePrefix = "org.apache.fop";

    /**
     * Constants for the set of attributes of interest with FONodes
     */
    public static final int
              NO_SET = 0
           ,ROOT_SET = 1
   ,DECLARATIONS_SET = 2
         ,LAYOUT_SET = 3
     ,SEQ_MASTER_SET = 4
        ,PAGESEQ_SET = 5
           ,FLOW_SET = 6
         ,STATIC_SET = 7
         ,MARKER_SET = 8

           ,LAST_SET = MARKER_SET
                     ;

    public static String getAttrSetName(int attrSet) throws FOPException {
        switch (attrSet) {
        case ROOT_SET:
            return "ROOT";
        case DECLARATIONS_SET:
            return "DECLARATIONS";
        case LAYOUT_SET:
            return "LAYOUT";
        case SEQ_MASTER_SET:
            return "SEQ_MASTER";
        case PAGESEQ_SET:
            return "PAGESEQ";
        case FLOW_SET:
            return "FLOW";
        case STATIC_SET:
            return "STATIC";
        case MARKER_SET:
            return "MARKER";
        }
        throw new FOPException("Invalid attribute set: " + attrSet);
    }

    public static ROBitSet getAttrROBitSet(int attrSet)
            throws FOPException
    {
        switch (attrSet) {
        case ROOT_SET:
            return allProps;
        case DECLARATIONS_SET:
            return declarationsAll;
        case LAYOUT_SET:
            return layoutMasterSet;
        case SEQ_MASTER_SET:
            return seqMasterSet;
        case PAGESEQ_SET:
            return pageSeqSet;
        case FLOW_SET:
            return flowAllSet;
        case STATIC_SET:
            return staticAllSet;
        case MARKER_SET:
            return markerAllSet;
        }
        throw new FOPException("Invalid attribute set: " + attrSet);
    }

    public static int getFoIndex(String name) {
        return ((Integer)(foToIndex.get(name))).intValue();
    }

    public static String getClassName(int foIndex) {
        return foClassNames[foIndex];
    }

    public static Class getClass(int foIndex) {
        return foClasses[foIndex];
    }

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
    private static final String[] foClassPackages;

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
    private static final Class[] foClasses;

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
    // Following are the sets of properties which apply to particular
    // subtrees of the FO Tree.  This whole section is probably redundant.
    // If it is restored to full functioning, the public BitSet objects
    // must be replaced with unmodifiableSets.

    /**
     * Set of all properties available at fo:root.
     */
    public static final ROBitSet allProps;

    /**
     * set of all properties which are
     * usable within the declarations subtree.
     */
    public static final ROBitSet declarationsAll;

    /**
     * set of all properties which are
     * usable within the page-sequence-master-set subtree.
     */
    public static final ROBitSet seqMasterSet;

    /**
     * set of all properties which are
     * usable within the layout-master-set subtree.
     */
    public static final ROBitSet layoutMasterSet;

    /**
     * set of all properties which are
     * usable within the page sequence subtree.
     */
    public static final ROBitSet pageSeqSet;

    /**
     * set of all properties which are
     * usable within the fo:flow subtree.
     */
    public static final ROBitSet flowAllSet;

    /**
     * set of all properties which are
     * usable <i>within</i> the fo:marker subtree.
     */
    public static final ROBitSet markerAllSet;

    /**
     * set of all properties which are
     * usable within the fo:static-content subtree.
     */
    public static final ROBitSet staticAllSet;

    static {

        // fill the BitSet of all properties
        BitSet allprops = new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
        allprops.set(1, PropNames.LAST_PROPERTY_INDEX);

        allProps = new ROBitSet(allprops);

        //root only set of properties - properties for exclusive use on the
        // root element
        BitSet rootonly = new BitSet(PropNames.MEDIA_USAGE + 1);
        rootonly.set(PropNames.MEDIA_USAGE);

        //declarations only set of properties - properties for exclusive use
        // in the declarations SUBTREE
        BitSet declarationsonly = new BitSet();
        declarationsonly.set(PropNames.COLOR_PROFILE_NAME);
        declarationsonly.set(PropNames.RENDERING_INTENT);

        // set of all declarations properties - properties which may be
        // used in the declarations SUBTREE
        BitSet declarationsall = (BitSet)declarationsonly.clone();
        declarationsall.set(PropNames.SRC);

        declarationsAll = new ROBitSet(declarationsall);

        // seq-master-only set of properties for exclusive use within
        // the page-sequence-master subtree
        BitSet seqmasteronly = new BitSet();
        seqmasteronly.set(PropNames.MAXIMUM_REPEATS);
        seqmasteronly.set(PropNames.PAGE_POSITION);
        seqmasteronly.set(PropNames.ODD_OR_EVEN);
        seqmasteronly.set(PropNames.BLANK_OR_NOT_BLANK);

        // seq-master-set set of properties for use within
        // the page-sequence-master subtree
        BitSet seqmasterset = (BitSet)seqmasteronly.clone();
        seqmasterset.set(PropNames.MASTER_NAME);
        seqmasterset.set(PropNames.MASTER_REFERENCE);

        seqMasterSet = new ROBitSet(seqmasterset);

        //layout-master-set only set of properties - properties for exclusive
        // use within the layout-master-set SUBTREE
        BitSet layoutmasteronly = (BitSet)seqmasteronly.clone();
        layoutmasteronly.set(PropNames.MASTER_NAME);
        layoutmasteronly.set(PropNames.PAGE_HEIGHT);
        layoutmasteronly.set(PropNames.PAGE_WIDTH);
        layoutmasteronly.set(PropNames.COLUMN_COUNT);
        layoutmasteronly.set(PropNames.COLUMN_GAP);
        layoutmasteronly.set(PropNames.REGION_NAME);
        layoutmasteronly.set(PropNames.EXTENT);
        layoutmasteronly.set(PropNames.PRECEDENCE);

        // set of all layout-master-set properties - properties which may be
        // used in the layout-master-set SUBTREE
        // Add the layout-master-set exclusive properties
        BitSet layoutmasterset = (BitSet)layoutmasteronly.clone();

        layoutmasterset.set(PropNames.MASTER_REFERENCE);
        layoutmasterset.set(PropNames.REFERENCE_ORIENTATION);
        layoutmasterset.set(PropNames.WRITING_MODE);
        layoutmasterset.set(PropNames.CLIP);
        layoutmasterset.set(PropNames.DISPLAY_ALIGN);
        layoutmasterset.set(PropNames.OVERFLOW);

        // Add the common margin properties - block
	layoutmasterset.or(PropertySets.marginBlockSet);
        // Add the common border properties
	layoutmasterset.or(PropertySets.borderSet);
        // Add the common padding properties
	layoutmasterset.or(PropertySets.paddingSet);
        // Add the common background properties
	layoutmasterset.or(PropertySets.backgroundSet);
        layoutMasterSet = new ROBitSet(layoutmasterset);

        BitSet flowonlyset = new BitSet();
        flowonlyset.set(PropNames.MARKER_CLASS_NAME);

        BitSet staticonlyset = new BitSet();
        staticonlyset.set(PropNames.RETRIEVE_CLASS_NAME);
        staticonlyset.set(PropNames.RETRIEVE_POSITION);
        staticonlyset.set(PropNames.RETRIEVE_BOUNDARY);

        // pageseqonly contains the properties which are exclusive to
        // fo:pagesequence
        BitSet pageseqonly = new BitSet();
        pageseqonly.set(PropNames.FORMAT);
        pageseqonly.set(PropNames.LETTER_VALUE);
        pageseqonly.set(PropNames.GROUPING_SEPARATOR);
        pageseqonly.set(PropNames.GROUPING_SIZE);
        pageseqonly.set(PropNames.INITIAL_PAGE_NUMBER);
        pageseqonly.set(PropNames.FORCE_PAGE_COUNT);

        // pageseqset may contain any of the exclusive elements of the
        // flow set or the static-content set, which may be accessed by
        // the from-nearest-specified-property() function.
        BitSet pageseqset = (BitSet)allprops.clone();
        pageseqset.andNot(rootonly);
        pageseqset.andNot(declarationsonly);
        pageseqset.andNot(layoutmasteronly);
        pageSeqSet = new ROBitSet(pageseqset);

        BitSet flowallset = (BitSet)pageseqset.clone();
        flowallset.andNot(pageseqonly);
        flowallset.andNot(staticonlyset);

        flowAllSet = new ROBitSet(flowallset);

        BitSet staticallset = (BitSet)pageseqset.clone();
        staticallset.andNot(pageseqonly);
        staticallset.andNot(flowonlyset);

        staticAllSet = new ROBitSet(staticallset);

        BitSet markerallset = (BitSet)flowallset.clone();
        markerallset.clear(PropNames.MARKER_CLASS_NAME);

        markerAllSet = new ROBitSet(markerallset);
    }

    /**
     * ReferenceArea trait mappings.  Immutable BitSet of FOs for which
     * the <tt>reference-area</tt> trait is true.
     */
    public static final ROBitSet isReferenceArea;
    static {
        BitSet refareas = new BitSet(FObjectNames.LAST_FO + 1);
        refareas.set(FObjectNames.SIMPLE_PAGE_MASTER);
        refareas.set(FObjectNames.REGION_AFTER);
        refareas.set(FObjectNames.REGION_BEFORE);
        refareas.set(FObjectNames.REGION_BODY);
        refareas.set(FObjectNames.REGION_END);
        refareas.set(FObjectNames.REGION_START);
        refareas.set(FObjectNames.BLOCK_CONTAINER);
        refareas.set(FObjectNames.INLINE_CONTAINER);
        refareas.set(FObjectNames.TABLE);
        refareas.set(FObjectNames.TABLE_CAPTION);
        refareas.set(FObjectNames.TABLE_CELL);
        refareas.set(FObjectNames.TITLE);

        isReferenceArea = new ROBitSet(refareas);
    }

    static {
        String prefix = packageNamePrefix + ".";
        String foPrefix = "Fo";
        int namei = 0;	// Index of localName in FObjectNames.foLocalNames
        int pkgi = 1;	// Index of package suffix in foLocalNames

        foClassNames    = new String[FObjectNames.foLocalNames.length];
        foClassPackages = new String[FObjectNames.foLocalNames.length];
        foClasses       = new Class[FObjectNames.foLocalNames.length];
        foToIndex       = new HashMap(FObjectNames.foLocalNames.length);
        foClassToIndex  = new HashMap(FObjectNames.foLocalNames.length);

        for (int i = 1;i < FObjectNames.foLocalNames.length; i++) {
            String cname = foPrefix;
            StringTokenizer stoke =
                    new StringTokenizer(FObjectNames.foLocalNames[i][namei],
                                        "-");
            while (stoke.hasMoreTokens()) {
                String token = stoke.nextToken();
                String pname = new Character(
                                    Character.toUpperCase(token.charAt(0))
                                ).toString() + token.substring(1);
                cname = cname + pname;
            }
            foClassNames[i] = cname;

            // Set up the array of class package names
            String pkgname = prefix + FObjectNames.foLocalNames[i][pkgi];

            // Set up the array of Class objects, indexed by the fo
            // constants.
            String name = prefix + cname;
            try {
                foClasses[i] = Class.forName(name);
            } catch (ClassNotFoundException e) {}

            // Set up the foToIndex Hashmap with the name of the
            // flow object as a key, and the integer index as a value
            if (foToIndex.put((Object) FObjectNames.foLocalNames[i][namei],
                                        Ints.consts.get(i)) != null) {
                throw new RuntimeException(
                    "Duplicate values in propertyToIndex for key " +
                    FObjectNames.foLocalNames[i][namei]);
            }

            // Set up the foClassToIndex Hashmap with the name of the
            // fo class as a key, and the integer index as a value
            
            if (foClassToIndex.put((Object) foClassNames[i],
                                    Ints.consts.get(i)) != null) {
                throw new RuntimeException(
                    "Duplicate values in foClassToIndex for key " +
                    foClassNames[i]);
            }

        }
    }

}

