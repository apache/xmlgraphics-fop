/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.layout;

import org.apache.fop.apps.FOPException;

/**
 * This class defines area classes.
 * (todo) Would better be replaced by an Enum (Avalon or Commons)
 */
public class AreaClass {
    public static final String UNASSIGNED = "unassigned";

    public static final String XSL_NORMAL = "xsl-normal";
    public static final String XSL_ABSOLUTE = "xsl-absolute";
    public static final String XSL_FOOTNOTE = "xsl-footnote";
    public static final String XSL_SIDE_FLOAT = "xsl-side-float";
    public static final String XSL_BEFORE_FLOAT = "xsl-before-float";

    // checker method
    public static String setAreaClass(String areaClass) throws FOPException {
        if (areaClass.equals(XSL_NORMAL)
                || areaClass.equals(XSL_ABSOLUTE)
                || areaClass.equals(XSL_FOOTNOTE)
                || areaClass.equals(XSL_SIDE_FLOAT)
                || areaClass.equals(XSL_BEFORE_FLOAT)) {
            return areaClass;
        } else {
            throw new FOPException("Unknown area class '" + areaClass + "'");
        }
    }

}

