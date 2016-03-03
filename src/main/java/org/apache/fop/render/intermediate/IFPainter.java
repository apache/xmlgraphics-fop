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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.w3c.dom.Document;

import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;

/**
 * Interface used to paint whole documents layouted by Apache FOP.
 * <p>
 * Call sequence:
 * <p>
 * <pre>
 * startDocument()
 *   startDocumentHeader()
 *   [handleExtension()]*
 *   endDocumentHeader()
 *   [
 *   startPageSequence()
 *     [
 *     startPage()
 *       startPageHeader()
 *         [handleExtension()]*
 *       endPageHeader()
 *       startPageContent()
 *         (#pageContent)+
 *       endPageContent()
 *       startPageTrailer()
 *         (addTarget())*
 *       endPageTrailer()
 *     endPage()
 *     ]*
 *   endPageSequence()
 *   ]*
 *   startDocumentTrailer()
 *   [handleExtension()]*
 *   endDocumentTrailer()
 * endDocument()
 *
 * #box:
 * startBox()
 * (#pageContent)+
 * endBox()
 *
 * #pageContent:
 * (
 *   setFont() |
 *   drawText() |
 *   drawRect() |
 *   drawImage() |
 *   TODO etc. etc. |
 *   handleExtensionObject()
 * )
 * </pre>
 */
public interface IFPainter {

    /**
     * Starts a new viewport, establishing a new coordinate system. A viewport has a size and
     * can optionally be clipped. Corresponds to SVG's svg element.
     * @param transform the transformation matrix establishing the new coordinate system
     * @param size the size of the viewport
     * @param clipRect the clipping rectangle (may be null)
     * @throws IFException if an error occurs while handling this element
     */
    void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
        throws IFException;

    /**
     * Starts a new viewport, establishing a new coordinate system. A viewport has a size and
     * can optionally be clipped. Corresponds to SVG's svg element.
     * @param transforms a series of transformation matrices establishing the new coordinate system
     * @param size the size of the viewport
     * @param clipRect the clipping rectangle (may be null)
     * @throws IFException if an error occurs while handling this element
     */
    void startViewport(AffineTransform[] transforms, Dimension size, Rectangle clipRect)
        throws IFException;
    //For transform, Batik's org.apache.batik.parser.TransformListHandler/Parser can be used

    /**
     * Ends the current viewport and restores the previous coordinate system.
     * @throws IFException if an error occurs while handling this element
     */
    void endViewport() throws IFException;

    /**
     * Starts a new group of graphical elements. Corresponds to SVG's g element.
     * @param transforms a series of transformation matrices establishing the new coordinate system
     * @param layer an optional layer label (or null if none)
     * @throws IFException if an error occurs while handling this element
     */
    void startGroup(AffineTransform[] transforms, String layer) throws IFException;

    /**
     * Starts a new group of graphical elements. Corresponds to SVG's g element.
     * @param transform the transformation matrix establishing the new coordinate system
     * @param layer an optional layer label (or null if none)
     * @throws IFException if an error occurs while handling this element
     */
    void startGroup(AffineTransform transform, String layer) throws IFException;

    /**
     * Ends the current group and restores the previous coordinate system (and layer).
     * @throws IFException if an error occurs while handling this element
     */
    void endGroup() throws IFException;

    /**
     * Updates the current font.
     * @param family the font family (or null if there's no change)
     * @param style the font style (or null if there's no change)
     * @param weight the font weight (or null if there's no change)
     * @param variant the font variant (or null if there's no change)
     * @param size the font size (or null if there's no change)
     * @param color the text color (or null if there's no change)
     * @throws IFException if an error occurs while handling this event
     */
    void setFont(String family, String style, Integer weight, String variant, Integer size,
            Color color) throws IFException;

    /**
     * Draws text. The initial coordinates (x and y) point to the starting point at the normal
     * baseline of the font. The parameters letterSpacing, wordSpacing and the array dx are
     * optional and can be used to influence character positioning (for example, for kerning).
     * @param x X-coordinate of the starting point of the text
     * @param y Y-coordinate of the starting point of the text
     * @param letterSpacing additional spacing between characters (may be 0)
     * @param wordSpacing additional spacing between words (may be 0)
     * @param dp an array of 4-tuples, expressing [X,Y] placment
     * adjustments and [X,Y] advancement adjustments, in that order (may be null); if
     * not null, then adjustments.length must be the same as text.length()
     * @param text the text
     * @throws IFException if an error occurs while handling this event
     */
    void drawText(int x, int y, int letterSpacing, int wordSpacing,
            int[][] dp, String text) throws IFException;

    /**
     * Restricts the current clipping region with the given rectangle.
     * @param rect the rectangle's coordinates and extent
     * @throws IFException if an error occurs while handling this event
     */
    void clipRect(Rectangle rect) throws IFException;
    //TODO clipRect() shall be considered temporary until verified with SVG and PCL


    /**
     * Restricts the current clipping region to the inner border.
     * @param rect the rectangle's coordinates and extent
     * @param bpsBefore the border segment on the before-side (top)
     * @param bpsAfter the border segment on the after-side (bottom)
     * @param bpsStart the border segment on the start-side (left)
     * @param bpsEnd the border segment on the end-side (right)
     * @throws IFException if an error occurs while handling this event
     */
    void clipBackground(Rectangle rect,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) throws IFException;


    /**
     * TODO Painter-specific rounded borders logic required background drawing to be
     * made optional.  A future refactoring of the rounded borders code should aim to make
     * the need for this abstraction obsolete
     * @param bpsBefore the before border
     * @param bpsAfter the after border
     * @param bpsStart the start border
     * @param bpsEnd the end border
     * @return true if and only if background drawing is required
     */
    boolean isBackgroundRequired(BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd);

    /**
     * Fills a rectangular area.
     * @param rect the rectangle's coordinates and extent
     * @param fill the fill paint
     * @throws IFException if an error occurs while handling this event
     */
    void fillRect(Rectangle rect, Paint fill) throws IFException;

    /**
     * Draws a border rectangle. The border segments are specified through {@link BorderProps}
     * instances.
     * @param rect the rectangle's coordinates and extent
     * @param top the border segment on the top edge
     * @param bottom the border segment on the bottom edge
     * @param left the border segment on the left edge
     * @param right the border segment on the right edge
     * @param innerBackgroundColor the color of the inner background
     * @throws IFException if an error occurs while handling this event
     */
    void drawBorderRect(Rectangle rect,
            BorderProps top, BorderProps bottom,
            BorderProps left, BorderProps right, Color innerBackgroundColor) throws IFException;

    /**
     * Draws a line. NOTE: Currently, only horizontal lines are implemented!
     * @param start the start point of the line
     * @param end the end point of the line
     * @param width the line width
     * @param color the line color
     * @param style the line style (using the Constants.EN_* constants for the rule-style property)
     * @throws IFException if an error occurs while handling this event
     */
    void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
            throws IFException;

    /**
     * Draws an image identified by a URI inside a given rectangle. This is the equivalent to
     * an fo:external-graphic in XSL-FO.
     * @param uri the image's URI
     * @param rect the rectangle in which the image shall be painted
     * @throws IFException if an error occurs while handling this event
     */
    void drawImage(String uri, Rectangle rect) throws IFException;

    /**
     * Draws an image (represented by a DOM document) inside a given rectangle. This is the
     * equivalent to an fo:instream-foreign-object in XSL-FO.
     * @param doc the DOM document containing the foreign object
     * @param rect the rectangle in which the image shall be painted
     * @throws IFException if an error occurs while handling this event
     */
    void drawImage(Document doc, Rectangle rect) throws IFException;
    //Note: For now, all foreign objects are handled as DOM documents. At the moment, all known
    //implementations use a DOM anyway, so optimizing this to work with SAX wouldn't result in
    //any performance benefits. The IFRenderer itself has a DOM anyway. Only the IFParser could
    //potentially profit if there's an image handler that can efficiently deal with the foreign
    //object without building a DOM.

    //etc. etc.

}
