/*
 * $Id$
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Id$
 */

package org.apache.fop.fo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.flow.FoPcdata;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.XMLEvent;

/**
 * Data class for common data and methods relating to Flow Objects.
 */

public class FObjects {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String packageNamePrefix = "org.apache.fop";

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
            ,FoXMLEvent.class
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
     * objects, which require an XMLEvent argument.  Use only when it is
     * known that no CHARACTERS event will be passed.
     * @param foTree
     * @param parent
     * @param event the <code>FoXMLEvent</code> event that triggered the
     * generation of this FO
     * @param stateFlags
     * @return the new FO node
     * @throws FOPException
     */
    public Object makeFlowObject(FOTree foTree,
                             FONode parent, FoXMLEvent event, int stateFlags)
        throws FOPException
    {
        Class foclass;
        Object[] args = new Object[] {
            foTree, parent, event, new Integer(stateFlags)
        };
        int foType = event.getFoType();

        if (foType <= 0 || foType > FObjectNames.foLocalNamesLength)
            throw new FOPException
                    ("Illegal FO type value: " + foType);
        try {
            if (foConstructors[foType] == null) {
                // Generate the constructor object
                foclass = Class.forName(foPkgClassNames[foType]);
                foConstructors[foType] =
                        foclass.getConstructor(defaultConstructorArgs);
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

    /**
     * This method generates generates new FO objects, including FoPcdata
     * objects.  It is more general in this sense than the overloaded
     * version which takes the <code>FoXMLEvent event</code> parameter.
     * objects, which require an XMLEvent argument.
     * @param foTree
     * @param parent
     * @param event the <code>XMLEvent</code> which triggered the generation
     * of this fo
     * @param stateFlags
     * @return
     * @throws FOPException
     */
    public Object makeFlowObject(FOTree foTree,
            FONode parent, XMLEvent event, int stateFlags)
    throws FOPException
    {
        if (event instanceof FoXMLEvent) {
            return makeFlowObject(
                    foTree, parent, (FoXMLEvent)event, stateFlags);
        }
        if (event.getType() != XMLEvent.CHARACTERS) {
            throw new FOPException(
                    "Attempt to makeFlowObject() with XMLEvent for event type "
                    + XMLEvent.eventTypeName(event.getType()));
        }
        return new FoPcdata(foTree, parent, event, stateFlags);
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

