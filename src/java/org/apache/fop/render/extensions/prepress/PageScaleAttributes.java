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

package org.apache.fop.render.extensions.prepress;

import java.awt.geom.Point2D;
import java.text.MessageFormat;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.fo.extensions.ExtensionElementMapping;

/**
 * This class provides utility methods to parse the 'fox:scale' extension attribute.
 */
public final class PageScaleAttributes {

    /**
     * The extension 'scale' attribute for the simple-page-master element.
     */
    public static final QName EXT_PAGE_SCALE
            = new QName(ExtensionElementMapping.URI, null, "scale");


    /**
     * Utility classes should not have a public or default constructor
     */
    private PageScaleAttributes() {
    }

    /**
     * Compute scale parameters from given fox:scale attribute which has the format: scaleX [scaleY]
     * If scaleY is not defined, it equals scaleX.
     * @param scale scale attribute, input format: scaleX [scaleY]
     * @return the pair of (sx, sy) values
     */
    public static Point2D.Double getScaleAttributes(String scale) {
        final String err = "Extension 'scale' attribute has incorrect value(s): {0}";

        if (scale == null) {
            return null;
        }

        Point2D.Double result = null;

        try {
            String[] scales = scale.split(" ");
            if (scales.length > 0) {
                result = new Point2D.Double(Double.parseDouble(scales[0]),
                        Double.parseDouble(scales[0]));
            }
            if (scales.length > 1) {
                result.y = Double.parseDouble(scales[1]);
            }
            if (result.x <= 0 || result.y <= 0) {
                throw new IllegalArgumentException(MessageFormat.format(err, new Object[]{scale}));
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(MessageFormat.format(err, new Object[]{scale}));
        }

        return result;
    }
}
