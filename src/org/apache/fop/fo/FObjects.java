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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.HashMap;
import java.util.StringTokenizer;

// Only for tree property set partitions

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.PropNames;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.xml.FoXMLEvent;

/**
 * Data class for common data and methods relating to Flow Objects.
 */

public class FObjects {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String packageNamePrefix = "org.apache.fop";

    public static final FObjects fobjects;
    static {
        //try {
            fobjects = new FObjects();
        //} catch (FOPException e) {
            //throw new RuntimeException(e.getMessage());
        //}
    }

    public static final FObjects getFObjects() {
        return fobjects;
    }

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
                        = new Constructor[FObjectNames.foLocalNames.length];

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

        foClassNames    = new String[FObjectNames.foLocalNames.length];
        foPkgClassNames = new String[FObjectNames.foLocalNames.length];
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
            String name = pkgname + "." + cname;
            foPkgClassNames[i] = name;

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

    public Object makeFlowObject(FOTree foTree,
                                 FONode parent, FoXMLEvent event, int attrSet)
        throws FOPException
    {
        Class foclass;
        Object[] args = new Object[] {
            foTree, parent, event, Ints.consts.get(attrSet)
        };
        int foType = event.getFoType();

        if (foType <= 0 || foType > FObjectNames.foLocalNames.length)
            throw new FOPException
                    ("Illegal FO type value: " + foType);
        try {
            if (foConstructors[foType] == null) {
                // Generate the constructor object
                foclass = Class.forName(foPkgClassNames[foType]);
                foConstructors[foType] =
                        foclass.getConstructor(FONode.defaultConstructorArgs);
            }
            // Now generate a new instance
            return foConstructors[foType].newInstance(args);
        } catch (ClassNotFoundException e) {
            throw new FOPException(e);
        } catch (NoSuchMethodException e) {
            throw new FOPException(e);
        } catch (IllegalAccessException e) {
            throw new FOPException(e);
        } catch (InstantiationException e) {
            throw new FOPException(e);
        } catch (InvocationTargetException e) {
            throw new FOPException(e);
        }
    }

    public static int getFoIndex(String name) {
        return ((Integer)(foToIndex.get(name))).intValue();
    }

    public static String getClassName(int foIndex) {
        return foClassNames[foIndex];
    }

    public static String getPkgClassName(int foIndex) {
        return foPkgClassNames[foIndex];
    }

    public Constructor getConstructor(int foIndex) {
        return foConstructors[foIndex];
    }

}

