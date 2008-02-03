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
 
package org.apache.fop.fo.expr;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.pagination.ColorProfile;
import org.apache.fop.fo.pagination.Declarations;
import org.apache.fop.fo.properties.ColorProperty;
import org.apache.fop.fo.properties.Property;

/**
 * Implements the rgb-icc() function.
 */
class ICCColorFunction extends FunctionBase {
    
    /**
     * rgb-icc takes a variable number of arguments. 
     * At least 4 should be passed - returns -4 
     * {@inheritDoc} 
     */
    public int nbArgs() {
        return -4;
    }
    
    /** {@inheritDoc} */
    public PercentBase getPercentBase() {
        return new RGBColorFunction.RGBPercentBase();
    }

    /** {@inheritDoc} */
    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        StringBuffer sb = new StringBuffer();

        // Map color profile NCNAME to src from declarations/color-profile element
        String colorProfileName = args[3].getString();
        Declarations decls = pInfo.getFO().getRoot().getDeclarations();
        ColorProfile cp = null;
        if (decls == null) {
            //function used in a color-specification 
            //on a FO occurring:
            //a) before the fo:declarations,
            //b) or in a document without fo:declarations?
            //=> return the sRGB fallback
            Property[] rgbArgs = new Property[3];
            System.arraycopy(args, 0, rgbArgs, 0, 3);
            return new RGBColorFunction().eval(rgbArgs, pInfo);
        } else {
            cp = decls.getColorProfile(colorProfileName);
            if (cp == null) {
                PropertyException pe = new PropertyException("The " + colorProfileName 
                        + " color profile was not declared");
                pe.setPropertyInfo(pInfo);
                throw pe;
            }
        }
        String src = cp.getSrc();
        
        // rgb-icc is replaced with fop-rgb-icc which has an extra fifth argument containing the 
        // color profile src attribute as it is defined in the color-profile declarations element.
        sb.append("fop-rgb-icc(" + args[0]);
        for (int ix = 1; ix < args.length; ix++) {
            if (ix == 3) {
                sb.append("," + colorProfileName); 
                sb.append(",\"" + src + "\""); 
            } else {
                sb.append("," + args[ix]);
            }
        }
        sb.append(")");
        FOUserAgent ua = (pInfo == null
                ? null
                : (pInfo.getFO() == null ? null : pInfo.getFO().getUserAgent()));
        return ColorProperty.getInstance(ua, sb.toString());
    }


}
