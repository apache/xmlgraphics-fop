/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.fop.svg;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.text.AttributedCharacterIterator;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.text.*;
import org.apache.batik.gvt.renderer.*;

import org.apache.fop.layout.*;

/**
 * Renders the attributed character iterator of a <tt>TextNode</tt>.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 */
public class PDFTextPainter implements TextPainter {
FontState fontState;

public PDFTextPainter(FontState fs)
{
fontState = fs;
}

    /**
     * Paints the specified attributed character iterator using the
     * specified Graphics2D and context and font context.
     * @param node the TextNode to paint
     * @param g2d the Graphics2D to use
     * @param context the rendering context.
     */
    public void paint(TextNode node,
               Graphics2D g2d,
               GraphicsNodeRenderContext context)
    {
System.out.println("PDFText paint");
String txt = node.getText();
Point2D loc = node.getLocation();
g2d.drawString(txt, (float)loc.getX(), (float)loc.getY());
}

    /**
     * Initiates a text selection on a particular AttributedCharacterIterator,
     * using the text/font metrics employed by this TextPainter instance.
     * @param x the x coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param y the y coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param aci the AttributedCharacterIterator describing the text
     * @param context the GraphicsNodeRenderContext to use when doing text layout.
     * @return an instance of Mark which encapsulates the state necessary to
     * implement hit testing and text selection.
     */
    public Mark selectAt(double x, double y, AttributedCharacterIterator aci,
                         TextNode node, GraphicsNodeRenderContext context)
{
System.out.println("PDFText selectAt");
return null;
}

    /**
     * Continues a text selection on a particular AttributedCharacterIterator,
     * using the text/font metrics employed by this TextPainter instance.
     * @param x the x coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param y the y coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param aci the AttributedCharacterIterator describing the text
     * @param context the GraphicsNodeRenderContext to use when doing text layout.
     * @return an instance of Mark which encapsulates the state necessary to
     * implement hit testing and text selection.
     */
    public Mark selectTo(double x, double y, Mark beginMark,
                            AttributedCharacterIterator aci,
                            TextNode node, GraphicsNodeRenderContext context)
{
System.out.println("PDFText selectTo");
return null;
}

    /**
     * Select all of the text represented by an AttributedCharacterIterator,
     * using the text/font metrics employed by this TextPainter instance.
     * @param x the x coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param y the y coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param aci the AttributedCharacterIterator describing the text
     * @param context the GraphicsNodeRenderContext to use when doing text layout.
     * @return an instance of Mark which encapsulates the state necessary to
     * implement hit testing and text selection.
     */
    public Mark selectAll(double x, double y,
                            AttributedCharacterIterator aci,
                            TextNode node, GraphicsNodeRenderContext context)
{
System.out.println("PDFText selectAll");
return null;
}


    /**
     * Selects the first glyph in the text node.
     */
    public Mark selectFirst(double x, double y,
                            AttributedCharacterIterator aci,
                            TextNode node,
                            GraphicsNodeRenderContext context)
{
System.out.println("PDFText selectFirst");
return null;
}


    /**
     * Selects the last glyph in the text node.
     */
    public Mark selectLast(double x, double y,
                            AttributedCharacterIterator aci,
                            TextNode node,
                            GraphicsNodeRenderContext context)
{
System.out.println("PDFText selectLast");
return null;
}

    /*
     * Get an array of index pairs corresponding to the indices within an
     * AttributedCharacterIterator regions bounded by two Marks.
     * Note that the instances of Mark passed to this function
     * <em>must come</em>
     * from the same TextPainter that generated them via selectAt() and
     * selectTo(), since the TextPainter implementation may rely on hidden
     * implementation details of its own Mark implementation.
     */
    public int[] getSelected(AttributedCharacterIterator aci,
                             Mark start, Mark finish)
{
System.out.println("PDFText getSelected");
return null;
}


    /*
     * Get a Shape in userspace coords which encloses the textnode
     * glyphs bounded by two Marks.
     * Note that the instances of Mark passed to this function
     * <em>must come</em>
     * from the same TextPainter that generated them via selectAt() and
     * selectTo(), since the TextPainter implementation may rely on hidden
     * implementation details of its own Mark implementation.
     */
     public Shape getHighlightShape(Mark beginMark, Mark endMark)
{
System.out.println("PDFText getHighlightShape");
return null;
}

    /*
     * Get a Shape in userspace coords which defines the textnode glyph outlines.
     * @param node the TextNode to measure
     * @param frc the font rendering context.
     * @param includeDecoration whether to include text decoration
     *            outlines.
     * @param includeStroke whether to create the "stroke shape outlines"
     *            instead of glyph outlines.
     */
     public Shape getShape(TextNode node, FontRenderContext frc)
{
System.out.println("PDFText getShape");
return null;
}

    /*
     * Get a Shape in userspace coords which defines the textnode glyph outlines.
     * @param node the TextNode to measure
     * @param frc the font rendering context.
     * @param includeDecoration whether to include text decoration
     *            outlines.
     * @param includeStroke whether to create the "stroke shape outlines"
     *            instead of glyph outlines.
     */
     public Shape getDecoratedShape(TextNode node, FontRenderContext frc)
{
System.out.println("PDFText getDecoratedShape");
return null;
}

    /*
     * Get a Rectangle2D in userspace coords which encloses the textnode
     * glyphs composed from an AttributedCharacterIterator.
     * @param node the TextNode to measure
     * @param g2d the Graphics2D to use
     * @param context rendering context.
     */
     public Rectangle2D getBounds(TextNode node,
               FontRenderContext frc)
{
System.out.println("PDFText getBounds");
return null;
}

    /*
     * Get a Rectangle2D in userspace coords which encloses the textnode
     * glyphs composed from an AttributedCharacterIterator, inclusive of
     * glyph decoration (underline, overline, strikethrough).
     * @param node the TextNode to measure
     * @param g2d the Graphics2D to use
     * @param context rendering context.
     */
     public Rectangle2D getDecoratedBounds(TextNode node,
               FontRenderContext frc)
{
System.out.println("PDFText getDecoratedBounds");
return null;
}

    /*
     * Get a Rectangle2D in userspace coords which encloses the
     * textnode glyphs (as-painted, inclusive of decoration and stroke, but
     * exclusive of filters, etc.) composed from an AttributedCharacterIterator.
     * @param node the TextNode to measure
     * @param g2d the Graphics2D to use
     * @param context rendering context.
     */
     public Rectangle2D getPaintedBounds(TextNode node,
               FontRenderContext frc)
{
System.out.println("PDFText getPaintedBounds");
return null;
}


}

