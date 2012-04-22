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
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.pagination.ColorProfile;
import org.apache.fop.fo.pagination.Declarations;
import org.apache.fop.fo.properties.ColorProperty;
import org.apache.fop.fo.properties.Property;

/**
 * Implements the rgb-named-color() function.
 * @since XSL-FO 2.0
 */
class NamedColorFunction extends FunctionBase {

    /** {@inheritDoc} */
    public int getRequiredArgsCount() {
        return 5;
    }

    /** {@inheritDoc} */
    @Override
    public PercentBase getPercentBase() {
        return new NamedPercentBase();
    }

    /** {@inheritDoc} */
    public Property eval(Property[] args, PropertyInfo pInfo) throws PropertyException {
        // Map color profile NCNAME to src from declarations/color-profile element
        String colorProfileName = args[3].getString();
        String colorName = args[4].getString();

        Declarations decls = (pInfo.getFO() != null
                ? pInfo.getFO().getRoot().getDeclarations()
                : null);
        ColorProfile cp = null;
        if (decls != null) {
            cp = decls.getColorProfile(colorProfileName);
        }
        if (cp == null) {
            PropertyException pe = new PropertyException("The " + colorProfileName
                    + " color profile was not declared");
            pe.setPropertyInfo(pInfo);
            throw pe;
        }

        float red = 0;
        float green = 0;
        float blue = 0;
        red = args[0].getNumber().floatValue();
        green = args[1].getNumber().floatValue();
        blue = args[2].getNumber().floatValue();
        /* Verify rgb replacement arguments */
        if ((red < 0 || red > 255)
                || (green < 0 || green > 255)
                || (blue < 0 || blue > 255)) {
            throw new PropertyException("sRGB color values out of range. "
                    + "Arguments to rgb-named-color() must be [0..255] or [0%..100%]");
        }

        // rgb-named-color is replaced with fop-rgb-named-color which has an extra argument
        // containing the color profile src attribute as it is defined in the color-profile
        // declarations element.
        StringBuffer sb = new StringBuffer();
        sb.append("fop-rgb-named-color(");
        sb.append(red / 255f);
        sb.append(',').append(green / 255f);
        sb.append(',').append(blue / 255f);
        sb.append(',').append(colorProfileName);
        sb.append(',').append(cp.getSrc());
        sb.append(", '").append(colorName).append('\'');
        sb.append(")");

        return ColorProperty.getInstance(pInfo.getUserAgent(), sb.toString());
    }

    private static final class NamedPercentBase implements PercentBase {

        /** {@inheritDoc} */
        public int getBaseLength(PercentBaseContext context) throws PropertyException {
            return 0;
        }

        /** {@inheritDoc} */
        public double getBaseValue() {
            return 255f;
        }

        /** {@inheritDoc} */
        public int getDimension() {
            return 0;
        }
    }
}
