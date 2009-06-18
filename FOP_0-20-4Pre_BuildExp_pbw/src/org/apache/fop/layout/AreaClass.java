/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.apps.FOPException;

public class AreaClass {
    public static String UNASSIGNED = "unassigned";

    public static String XSL_NORMAL = "xsl-normal";
    public static String XSL_ABSOLUTE = "xsl-absolute";
    public static String XSL_FOOTNOTE = "xsl-footnote";
    public static String XSL_SIDE_FLOAT = "xsl-side-float";
    public static String XSL_BEFORE_FLOAT = "xsl-before-float";

    // checker method
    public static String setAreaClass(String areaClass) throws FOPException {
        if (areaClass.equals(XSL_NORMAL) || areaClass.equals(XSL_ABSOLUTE)
                || areaClass.equals(XSL_FOOTNOTE)
                || areaClass.equals(XSL_SIDE_FLOAT)
                || areaClass.equals(XSL_BEFORE_FLOAT))
            return areaClass;
        else
            throw new FOPException("Unknown area class '" + areaClass + "'");
    }

}

