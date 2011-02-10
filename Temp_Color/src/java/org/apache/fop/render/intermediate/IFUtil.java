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

package org.apache.fop.render.intermediate;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.util.DecimalFormatCache;

/**
 * Utility functions for the intermediate format.
 */
public final class IFUtil {

    private IFUtil() {
    }

    private static String format(double value) {
        if (value == -0.0) {
            //Don't allow negative zero because of testing
            //See http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.2.3
            value = 0.0;
        }
        return DecimalFormatCache.getDecimalFormat(6).format(value);
    }

    /**
     * Converts an {@link AffineTransform} instance to an SVG style transform method.
     * @param transform the transformation matrix
     * @param sb the StringBuffer to write the transform method to
     * @return the StringBuffer passed to this method
     */
    public static StringBuffer toString(AffineTransform transform, StringBuffer sb) {
        if (transform.isIdentity()) {
            return sb;
        }
        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        if (matrix[0] == 1 && matrix[3] == 1 && matrix[1] == 0 && matrix[2] == 0) {
            sb.append("translate(");
            sb.append(format(matrix[4]));
            if (matrix[5] != 0) {
                sb.append(',').append(format(matrix[5]));
            }
        } else {
            sb.append("matrix(");
            for (int i = 0; i < 6; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(format(matrix[i]));
            }
        }
        sb.append(')');
        return sb;
    }

    /**
     * Converts an {@link AffineTransform} array to an SVG style transform method sequence.
     * @param transforms the transformation matrix array
     * @param sb the StringBuffer to write the transform method sequence to
     * @return the StringBuffer passed to this method
     */
    public static StringBuffer toString(AffineTransform[] transforms, StringBuffer sb) {
        for (int i = 0, c = transforms.length; i < c; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            toString(transforms[i], sb);
        }
        return sb;
    }

    /**
     * Converts an {@link AffineTransform} array to an SVG style transform method sequence.
     * @param transforms the transformation matrix array
     * @return the formatted array
     */
    public static String toString(AffineTransform[] transforms) {
        return toString(transforms, new StringBuffer()).toString();
    }

    /**
     * Converts an {@link AffineTransform} instance to an SVG style transform method.
     * @param transform the transformation matrix
     * @return the formatted array
     */
    public static String toString(AffineTransform transform) {
        return toString(transform, new StringBuffer()).toString();
    }

    /**
     * Converts an array of integer coordinates into a space-separated string.
     * @param coordinates the coordinates
     * @return the space-separated array of coordinates
     */
    public static String toString(int[] coordinates) {
        if (coordinates == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, c = coordinates.length; i < c; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(Integer.toString(coordinates[i]));
        }
        return sb.toString();
    }

    /**
     * Converts a rectangle into a space-separated string.
     * @param rect the rectangle
     * @return the space-separated array of coordinates
     */
    public static String toString(Rectangle rect) {
        if (rect == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(rect.x).append(' ').append(rect.y).append(' ');
        sb.append(rect.width).append(' ').append(rect.height);
        return sb.toString();
    }

    /**
     * Sets up the fonts on a document handler. If the document handler provides a configurator
     * object the configuration from the {@link FopFactory} will be used. Otherwise,
     * a default font configuration will be set up.
     * @param documentHandler the document handler
     * @param fontInfo the font info object (may be null)
     * @throws FOPException if an error occurs while setting up the fonts
     */
    public static void setupFonts(IFDocumentHandler documentHandler, FontInfo fontInfo)
                throws FOPException {
        if (fontInfo == null) {
            fontInfo = new FontInfo();
        }
        if (documentHandler instanceof IFSerializer) {
            IFSerializer serializer = (IFSerializer)documentHandler;
            if (serializer.getMimickedDocumentHandler() != null) {
                //Use the mimicked document handler's configurator to set up fonts
                documentHandler = serializer.getMimickedDocumentHandler();
            }
        }
        IFDocumentHandlerConfigurator configurator = documentHandler.getConfigurator();
        if (configurator != null) {
            configurator.setupFontInfo(documentHandler, fontInfo);
        } else {
            documentHandler.setDefaultFontInfo(fontInfo);
        }
    }

    /**
     * Sets up the fonts on a document handler. If the document handler provides a configurator
     * object the configuration from the {@link FopFactory} will be used. Otherwise,
     * a default font configuration will be set up.
     * @param documentHandler the document handler
     * @throws FOPException if an error occurs while setting up the fonts
     */
    public static void setupFonts(IFDocumentHandler documentHandler) throws FOPException {
        setupFonts(documentHandler, null);
    }

    /**
     * Returns the MIME type of the output format that the given document handler is supposed to
     * handle. If the document handler is an {@link IFSerializer} it returns the MIME type of the
     * document handler it is mimicking.
     * @param documentHandler the document handler
     * @return the effective MIME type
     */
    public static String getEffectiveMIMEType(IFDocumentHandler documentHandler) {
        if (documentHandler instanceof IFSerializer) {
            IFDocumentHandler mimic = ((IFSerializer)documentHandler).getMimickedDocumentHandler();
            if (mimic != null) {
                return mimic.getMimeType();
            }
        }
        return documentHandler.getMimeType();
    }

}
