/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.HashMap;
import org.apache.fop.fo.properties.WritingMode;
import org.apache.fop.apps.FOPException;

public class PropertyList extends HashMap {

    private byte[] wmtable = null;    // writing-mode values
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int HEIGHT = 4;
    public static final int WIDTH = 5;

    public static final int START = 0;
    public static final int END = 1;
    public static final int BEFORE = 2;
    public static final int AFTER = 3;
    public static final int BLOCKPROGDIM = 4;
    public static final int INLINEPROGDIM = 5;

    private static final String[] sAbsNames = new String[] {
        "left", "right", "top", "bottom", "height", "width"
    };

    private static final String[] sRelNames = new String[] {
        "start", "end", "before", "after", "block-progression-dimension",
        "inline-progression-dimension"
    };

    static private final HashMap wmtables = new HashMap(4);
    {
        wmtables.put(new Integer(WritingMode.LR_TB),    /* lr-tb */
        new byte[] {
            START, END, BEFORE, AFTER, BLOCKPROGDIM, INLINEPROGDIM
        });
        wmtables.put(new Integer(WritingMode.RL_TB),    /* rl-tb */
        new byte[] {
            END, START, BEFORE, AFTER, BLOCKPROGDIM, INLINEPROGDIM
        });
        wmtables.put(new Integer(WritingMode.TB_RL),    /* tb-rl */
        new byte[] {
            AFTER, BEFORE, START, END, INLINEPROGDIM, BLOCKPROGDIM
        });
    }

    private PropertyListBuilder builder;
    private PropertyList parentPropertyList = null;
    String namespace = "";
    String element = "";
    FObj fobj = null;

    public PropertyList(PropertyList parentPropertyList, String space,
                        String el) {
        this.parentPropertyList = parentPropertyList;
        this.namespace = space;
        this.element = el;
    }

    public void setFObj(FObj fobj) {
        this.fobj = fobj;
    }

    public FObj getFObj() {
        return this.fobj;
    }

    public FObj getParentFObj() {
        if (parentPropertyList != null) {
            return parentPropertyList.getFObj();
        } else
            return null;
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propertyName The name of the property whose value is desired.
     * It may be a compound name, such as space-before.optimum.
     * @return The value if the property is explicitly set or set by
     * a shorthand property, otherwise null.
     */
    public Property getExplicitOrShorthand(String propertyName) {
        /* Handle request for one part of a compound property */
        int sepchar = propertyName.indexOf('.');
        String baseName;
        if (sepchar > -1) {
            baseName = propertyName.substring(0, sepchar);
        } else
            baseName = propertyName;
        Property p = getExplicitBaseProp(baseName);
        if (p == null) {
            p = builder.getShorthand(this, namespace, element, baseName);
        }
        if (p != null && sepchar > -1) {
            return builder.getSubpropValue(namespace, element, baseName, p,
                                           propertyName.substring(sepchar
                                           + 1));
        }
        return p;
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propertyName The name of the property whose value is desired.
     * It may be a compound name, such as space-before.optimum.
     * @return The value if the property is explicitly set, otherwise null.
     */
    public Property getExplicit(String propertyName) {
        /* Handle request for one part of a compound property */
        int sepchar = propertyName.indexOf('.');
        if (sepchar > -1) {
            String baseName = propertyName.substring(0, sepchar);
            Property p = getExplicitBaseProp(baseName);
            if (p != null) {
                return this.builder.getSubpropValue(namespace, element,
                                                    baseName, p,
                                                    propertyName.substring(sepchar
                                                    + 1));
            } else
                return null;
        }
        return (Property)super.get(propertyName);
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propertyName The name of the base property whose value is desired.
     * @return The value if the property is explicitly set, otherwise null.
     */
    public Property getExplicitBaseProp(String propertyName) {
        return (Property)super.get(propertyName);
    }

    /**
     * Return the value of this property inherited by this FO.
     * Implements the inherited-property-value function.
     * The property must be inheritable!
     * @param propertyName The name of the property whose value is desired.
     * @return The inherited value, otherwise null.
     */
    public Property getInherited(String propertyName) {
        if (builder != null) {
            if (parentPropertyList != null
                    && builder.isInherited(namespace, element,
                                           propertyName)) {
                return parentPropertyList.get(propertyName);
            } else {
                // return the "initial" value
                try {
                    return builder.makeProperty(this, namespace, element,
                                                propertyName);
                } catch (org.apache.fop.apps.FOPException e) {
                    //log.error("Exception in getInherited(): property="
                    //                       + propertyName + " : " + e);
                }
            }
        }
        return null;    // No builder or exception in makeProperty!
    }

    /*
     * If the property is a relative property with a corresponding absolute
     * value specified, the absolute value is used. This is also true of
     * the inheritance priority (I think...)
     * If the property is an "absolute" property and it isn't specified, then
     * we try to compute it from the corresponding relative property: this
     * happends in computeProperty.
     */
    private Property findProperty(String propertyName, boolean bTryInherit) {
        Property p = null;
        if (builder.isCorrespondingForced(this, namespace, element,
                                          propertyName)) {
            p = builder.computeProperty(this, namespace, element,
                                        propertyName);
        } else {
            p = getExplicitBaseProp(propertyName);
            if (p == null) {
                p = this.builder.computeProperty(this, namespace, element,
                                                 propertyName);
            }
            if (p == null) {    // check for shorthand specification
                p = builder.getShorthand(this, namespace, element,
                                         propertyName);
            }
            if (p == null
                    && bTryInherit) {    // else inherit (if has parent and is inheritable)
                if (this.parentPropertyList != null
                        && builder.isInherited(namespace, element,
                                               propertyName)) {
                    p = parentPropertyList.findProperty(propertyName, true);
                }
            }
        }
        return p;
    }


    /**
     * Return the property on the current FlowObject if it is specified, or if a
     * corresponding property is specified. If neither is specified, it returns null.
     */
    public Property getSpecified(String propertyName) {
        return get(propertyName, false, false);
    }


    /**
     * Return the property on the current FlowObject. If it isn't set explicitly,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     */
    public Property get(String propertyName) {
        return get(propertyName, true, true);
    }

    /**
     * Return the property on the current FlowObject. Depending on the passed flags,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     */
    private Property get(String propertyName, boolean bTryInherit,
                         boolean bTryDefault) {

        if (builder == null) {
            //log.error("OH OH, builder has not been set");
        }
            /* Handle request for one part of a compound property */
        int sepchar = propertyName.indexOf('.');
        String subpropName = null;
        if (sepchar > -1) {
            subpropName = propertyName.substring(sepchar + 1);
            propertyName = propertyName.substring(0, sepchar);
        }

        Property p = findProperty(propertyName, bTryInherit);
        if (p == null && bTryDefault) {    // default value for this FO!
            try {
                p = this.builder.makeProperty(this, namespace, element,
                                              propertyName);
            } catch (FOPException e) {
                // don't know what to do here
            }
        }

        // if value is inherit then get computed value from
        // parent
        if(p != null && "inherit".equals(p.getString())) {
            if (this.parentPropertyList != null) {
                p = parentPropertyList.get(propertyName, true, false);
            }
        }

        if (subpropName != null && p != null) {
            return this.builder.getSubpropValue(namespace, element,
                                                propertyName, p, subpropName);
        } else
            return p;
    }

    public void setBuilder(PropertyListBuilder builder) {
        this.builder = builder;
    }

    public String getNameSpace() {
        return namespace;
    }

    public String getElement() {
        return element;
    }

    /**
     * Return the "nearest" specified value for the given property.
     * Implements the from-nearest-specified-value function.
     * @param propertyName The name of the property whose value is desired.
     * @return The computed value if the property is explicitly set on some
     * ancestor of the current FO, else the initial value.
     */
    public Property getNearestSpecified(String propertyName) {
        Property p = null;
        for (PropertyList plist = this; p == null && plist != null;
                plist = plist.parentPropertyList) {
            p = plist.getExplicit(propertyName);
        }
        if (p == null) {
            // If no explicit setting found, return initial (default) value.
            try {
                p = this.builder.makeProperty(this, namespace, element,
                                              propertyName);
            } catch (FOPException e) {
                //log.error("Exception in getNearestSpecified(): property="
                //                       + propertyName + " : " + e);
            }
        }
        return p;
    }

    /**
     * Return the value of this property on the parent of this FO.
     * Implements the from-parent function.
     * @param propertyName The name of the property whose value is desired.
     * @return The computed value on the parent or the initial value if this
     * FO is the root or is in a different namespace from its parent.
     */
    public Property getFromParent(String propertyName) {
        if (parentPropertyList != null) {
            return parentPropertyList.get(propertyName);
        } else if (builder != null) {
            // return the "initial" value
            try {
                return builder.makeProperty(this, namespace, element,
                                            propertyName);
            } catch (org.apache.fop.apps.FOPException e) {
                //log.error("Exception in getFromParent(): property="
                //                       + propertyName + " : " + e);
            }
        }
        return null;    // No builder or exception in makeProperty!
    }

    /**
     * Given an absolute direction (top, bottom, left, right),
     * return the corresponding writing model relative direction name
     * for the flow object. Uses the stored writingMode.
     */
    public String wmAbsToRel(int absdir) {
        if (wmtable != null) {
            return sRelNames[wmtable[absdir]];
        } else
            return "";
    }

    /**
     * Given a writing mode relative direction (start, end, before, after)
     * return the corresponding absolute direction name
     * for the flow object. Uses the stored writingMode.
     */
    public String wmRelToAbs(int reldir) {
        if (wmtable != null) {
            for (int i = 0; i < wmtable.length; i++) {
                if (wmtable[i] == reldir)
                    return sAbsNames[i];
            }
        }
        return "";
    }

    /**
     * Set the writing mode traits for the FO with this property list.
     */
    public void setWritingMode(int writingMode) {
        this.wmtable = (byte[])wmtables.get(new Integer(writingMode));
    }

}

