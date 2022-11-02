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

package org.apache.fop.fo.properties;

import java.util.List;

import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Class encapsulating functionality for the <a href="http://www.w3.org/TR/xsl/#background-position">
 * <code>background-position</code></a> shorthand.
 */

public class BackgroundPositionShorthand extends ListProperty {

    /**
     * Inner class for creating instances of this property
     *
     */
    public static class Maker extends ListProperty.Maker {

        /**
         * Construct an instance of a Maker for the given property.
         *
         * @param propId The Constant ID of the property to be made.
         */
        public Maker(int propId) {
            super(propId);
        }


        /**
         * {@inheritDoc}
         * If only <code>background-position-horizontal</code> is
         * specified, <code>background-position-vertical</code> is set
         * to "50%".
         */
        public Property make(PropertyList propertyList, String value, FObj fo)
                throws PropertyException {
            Property p = super.make(propertyList, value, fo);
            if (p.getList().size() == 1) {
                /* only background-position-horizontal specified
                 * through the shorthand, as a length or percentage:
                 * background-position-vertical=50% (see: XSL-FO 1.1 -- 7.31.2)
                 */
                PropertyMaker m = FObj.getPropertyMakerFor(
                                    Constants.PR_BACKGROUND_POSITION_VERTICAL);
                p.getList().add(1, m.make(propertyList, "50%", fo));
            }
            return p;
        }

        private static final class Dimension1PercentBase implements PercentBase {
            /** {@inheritDoc} */
            public int getBaseLength(PercentBaseContext context) throws PropertyException {
                return 0;
            }

            /** {@inheritDoc} */
            public double getBaseValue() {
                return 0;
            }

            /** {@inheritDoc} */
            public int getDimension() {
                return 1;
            }
        }

        private static final Dimension1PercentBase DIMENSION_1_PERCENT_BASE
                = new Dimension1PercentBase();

        /**
         * {@inheritDoc}
         * Returns a {@link org.apache.fop.datatypes.PercentBase} whose
         * <code>getDimension()</code> returns 1.
         */
        public PercentBase getPercentBase(PropertyList pl) {
            return DIMENSION_1_PERCENT_BASE;
        }
    }


    /**
     * Inner class to provide shorthand parsing capabilities
     *
     */
    public static class Parser extends GenericShorthandParser {

        /** {@inheritDoc} */
        public Property getValueForProperty(int propId,
                                            Property property,
                                            PropertyMaker maker,
                                            PropertyList propertyList)
                        throws PropertyException {

            int index = -1;
            List propList = property.getList();
            if (propId == Constants.PR_BACKGROUND_POSITION_HORIZONTAL) {
                index = 0;
            } else if (propId == Constants.PR_BACKGROUND_POSITION_VERTICAL) {
                index = 1;
            }
            if (index >= 0) {
                return maker.convertProperty(
                        (Property) propList.get(index),
                        propertyList,
                        propertyList.getFObj());
            } // else: invalid index? shouldn't happen...
            return null;
        }
    }

}
