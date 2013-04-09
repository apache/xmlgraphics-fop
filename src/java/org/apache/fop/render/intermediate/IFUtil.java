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

import org.apache.xmlgraphics.util.DoubleFormatUtil;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.FontInfo;

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
        StringBuffer buf = new StringBuffer();
        DoubleFormatUtil.formatDouble(value, 6, 6, buf);
        return buf.toString();
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
     * object the configuration from the {@link org.apache.fop.apps.FopFactory} will be used.
     * Otherwise, a default font configuration will be set up.
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
            configurator.setupFontInfo(documentHandler.getMimeType(), fontInfo);
            documentHandler.setFontInfo(fontInfo);
        } else {
            documentHandler.setDefaultFontInfo(fontInfo);
        }
    }

    /**
     * Sets up the fonts on a document handler. If the document handler provides a configurator
     * object the configuration from the {@link org.apache.fop.apps.FopFactory} will be used.
     * Otherwise, a default font configuration will be set up.
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

    /**
     * Convert the general gpos 'dp' adjustments to the older 'dx' adjustments.
     * This utility method is used to provide backward compatibility in implementations
     * of IFPainter that have not yet been upgraded to the general position adjustments format.
     * @param dp an array of 4-tuples, expressing [X,Y] placment
     * adjustments and [X,Y] advancement adjustments, in that order (may be null)
     * @param count if <code>dp</code> is not null, then a count of dp values to convert
     * @return if <code>dp</code> is not null, then an array of adjustments to the current
     * x position prior to rendering individual glyphs; otherwise, null
     */
    public static int[] convertDPToDX (int[][] dp, int count) {
        int[] dx;
        if (dp != null) {
            dx = new int [ count ];
            for (int i = 0, n = count; i < n; i++) {
                if (dp [ i ] != null) {
                    dx [ i ] = dp [ i ] [ 0 ];      // xPlaAdjust[i]
                }
            }
        } else {
            dx = null;
        }
        return dx;
    }

    /**
     * Convert the general gpos 'dp' adjustments to the older 'dx' adjustments.
     * This utility method is used to provide backward compatibility in implementations
     * of IFPainter that have not yet been upgraded to the general position adjustments format.
     * @param dp an array of 4-tuples, expressing [X,Y] placment
     * adjustments and [X,Y] advancement adjustments, in that order (may be null)
     * @return if <code>dp</code> is not null, then an array of adjustments to the current
     * x position prior to rendering individual glyphs; otherwise, null
     */
    public static int[] convertDPToDX (int[][] dp) {
        return convertDPToDX (dp, (dp != null) ? dp.length : 0);
    }

    /**
     * Convert the general gpos 'dp' adjustments to the older 'dx' adjustments.
     * This utility method is used to provide backward compatibility in implementations
     * of IFPainter that have not yet been upgraded to the general position adjustments format.
     * @param dx an array of adjustments to the current x position prior to rendering
     * individual glyphs or null
     * @param count if <code>dx</code> is not null, then a count of dx values to convert
     * @return if <code>dx</code> is not null, then an array of 4-tuples, expressing [X,Y]
     * placment adjustments and [X,Y] advancement adjustments, in that order; otherwise, null
     */
    public static int[][] convertDXToDP (int[] dx, int count) {
        int[][] dp;
        if (dx != null) {
            dp = new int [ count ] [ 4 ];
            for (int i = 0, n = count; i < n; i++) {
                int[] pa = dp [ i ];
                int   d  = dx [ i ];
                pa [ 0 ] = d;                   // xPlaAdjust[i]
                pa [ 2 ] = d;                   // xAdvAdjust[i]
            }
        } else {
            dp = null;
        }
        return dp;
    }

    /**
     * Convert the general gpos 'dp' adjustments to the older 'dx' adjustments.
     * This utility method is used to provide backward compatibility in implementations
     * of IFPainter that have not yet been upgraded to the general position adjustments format.
     * @param dx an array of adjustments to the current x position prior to rendering
     * individual glyphs or null
     * @return if <code>dx</code> is not null, then an array of 4-tuples, expressing [X,Y]
     * placment adjustments and [X,Y] advancement adjustments, in that order; otherwise, null
     */
    public static int[][] convertDXToDP (int[] dx) {
        return convertDXToDP (dx, (dx != null) ? dx.length : 0);
    }

    /**
     * Determine if position adjustment is the identity adjustment, i.e., no non-zero adjustment.
     * @param pa a 4-tuple, expressing [X,Y] placment and [X,Y] advance adjuustments (may be null)
     * @return true if <code>dp</code> is null or contains no non-zero adjustment
     */
    public static boolean isPAIdentity (int[] pa) {
        if (pa == null) {
            return true;
        } else {
            for (int k = 0; k < 4; k++) {
                if (pa[k] != 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Determine if position adjustments is the identity adjustment, i.e., no non-zero adjustment.
     * @param dp an array of 4-tuples, expressing [X,Y] placment
     * adjustments and [X,Y] advancement adjustments, in that order (may be null)
     * @return true if <code>dp</code> is null or contains no non-zero adjustment
     */
    public static boolean isDPIdentity (int[][] dp) {
        if (dp == null) {
            return true;
        } else {
            for (int i = 0, n = dp.length; i < n; i++) {
                if (!isPAIdentity (dp[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Determine if position adjustments comprises only DX adjustments as encoded by
     * {@link #convertDPToDX}. Note that if given a set of all all zero position
     * adjustments, both this method and {@link #isDPIdentity} will return true;
     * however, this method may return true when {@link #isDPIdentity} returns false.
     * @param dp an array of 4-tuples, expressing [X,Y] placment
     * adjustments and [X,Y] advancement adjustments, in that order (may be null)
     * @return true if <code>dp</code> is not null and contains only xPlaAdjust
     * and xAdvAdjust values consistent with the output of {@link #convertDPToDX}.
     */
    public static boolean isDPOnlyDX (int[][] dp) {
        if (dp == null) {
            return false;
        } else {
            for (int i = 0, n = dp.length; i < n; i++) {
                int[] pa = dp[i];
                if ((pa != null) && (pa[0] != pa[2])) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Adjust a position adjustments array. If both <code>paDst</code> and <code>paSrc</code> are
     * non-null, then <code>paSrc[i]</code> is added to <code>paDst[i]</code>.
     * @param paDst a 4-tuple, expressing [X,Y] placment
     * and [X,Y] advance adjuustments (may be null)
     * @param paSrc a 4-tuple, expressing [X,Y] placment
     * and [X,Y] advance adjuustments (may be null)
     */
    public static void adjustPA (int[] paDst, int[] paSrc) {
        if ((paDst != null) && (paSrc != null)) {
            assert paDst.length == 4;
            assert paSrc.length == 4;
            for (int i = 0; i < 4; i++) {
                paDst[i] += paSrc[i];
            }
        }
    }

    /**
     * Copy entries from position adjustments.
     * @param dp an array of 4-tuples, expressing [X,Y] placment
     * adjustments and [X,Y] advancement adjustments, in that order
     * @param offset starting offset from which to copy
     * @param count number of entries to copy
     * @return a deep copy of the count position adjustment entries start at
     * offset
     */
    public static int[][] copyDP (int[][] dp, int offset, int count) {
        if ((dp == null) || (offset > dp.length) || ((offset + count) > dp.length)) {
            throw new IllegalArgumentException();
        } else {
            int[][] dpNew = new int [ count ] [];
            for (int i = 0, n = count; i < n; i++) {
                int[] paSrc = dp [ i + offset ];
                if (paSrc != null) {
                    int[] paDst = new int [ 4 ];
                    for (int k = 0; k < 4; k++) {
                        paDst [ k ] = paSrc [ k ];
                    }
                    dpNew [ i ] = paDst;
                }
            }
            return dpNew;
        }
    }

}
