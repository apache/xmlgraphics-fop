/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

//package com.eastpoint.chrysalis;
package org.apache.fop.render.pcl;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.fo.properties.*;
import org.apache.fop.svg.PathPoint;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.layout.*;
import org.apache.fop.image.*;

import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.css.*;

import org.apache.fop.dom.svg.*;

// Java
import java.util.Enumeration;
import java.util.Vector;

/**
 * Renderer that renders SVG to PCL
 */
public class PCLSVGRenderer
{
	FontState fontState;

    /** the current stream to add PCL commands to */
    PCLStream currentStream;

    /** the current (internal) font name */
    protected String currentFontName;

    /** the current font size in millipoints */
    protected int currentFontSize;

    /** the current vertical position in millipoints from bottom */
    protected int currentYPosition = 0;

    /** the current horizontal position in millipoints from left */
    protected int currentXPosition = 0;

	/** the current colour for use in svg */
	private PDFColor currentColour = new PDFColor(0, 0, 0);

	private PCLRenderer renderer;

	final boolean debug = false;

	private int pageHeight;
	private int rendxoffset;

    /**
     * create the SVG renderer
     */
    public PCLSVGRenderer(PCLRenderer rend, FontState fs, String font, int size, int xpos, int ypos, int ph, int xo)
    {
		renderer = rend;
		currentFontName = font;
		currentFontSize = size;
		currentYPosition = ypos;
		currentXPosition = xpos;
		fontState = fs;

		currentStream = rend.currentStream;

		pageHeight = ph;
		rendxoffset = xo;
    }

    /**
     * Renders an SVG element in an SVG document.
     * This renders each of the child elements.
     */
    protected void renderSVG(SVGSVGElement svg, int x, int y) {
        NodeList nl = svg.getChildNodes();
        for (int count = 0; count < nl.getLength(); count++) {
            Node n = nl.item(count);
            if (n instanceof SVGElement) {
                renderElement((SVGElement) n, x, y);
            }
        }
    }

    public void renderGArea(SVGGElement area, int posx, int posy) {
        NodeList nl = area.getChildNodes();
        for (int count = 0; count < nl.getLength(); count++) {
            Node n = nl.item(count);
            if (n instanceof SVGElement) {
                renderElement((SVGElement) n, posx, posy);
            }
        }
    }

    /**
     * Handles the SVG switch element.
     * The switch determines which of its child elements should be rendered
     * according to the required extensions, required features and system language.
     */
    protected void handleSwitchElement(int posx, int posy,
                                       SVGSwitchElement ael) {
        SVGStringList relist = ael.getRequiredExtensions();
        SVGStringList rflist = ael.getRequiredFeatures();
        SVGStringList sllist = ael.getSystemLanguage();
        NodeList nl = ael.getChildNodes();
        choices:
        for (int count = 0; count < nl.getLength(); count++) {
            org.w3c.dom.Node n = nl.item(count);
            // only render the first child that has a valid
            // test data
            if (n instanceof GraphicElement) {
                GraphicElement graphic = (GraphicElement) n;
                SVGStringList grelist = graphic.getRequiredExtensions();
                // if null it evaluates to true
                if (grelist != null) {
                    if (grelist.getNumberOfItems() == 0) {
                        if ((relist != null) &&
                                relist.getNumberOfItems() != 0) {
                            continue choices;
                        }
                    }
                    for (int i = 0; i < grelist.getNumberOfItems(); i++) {
                        String str = (String) grelist.getItem(i);
                        if (relist == null) {
                            // use default extension set
                            // currently no extensions are supported
                            //							if(!(str.equals("http:// ??"))) {
                            continue choices;
                            //							}
                        } else {
                        }
                    }
                }
                SVGStringList grflist = graphic.getRequiredFeatures();
                if (grflist != null) {
                    if (grflist.getNumberOfItems() == 0) {
                        if ((rflist != null) &&
                                rflist.getNumberOfItems() != 0) {
                            continue choices;
                        }
                    }
                    for (int i = 0; i < grflist.getNumberOfItems(); i++) {
                        String str = (String) grflist.getItem(i);
                        if (rflist == null) {
                            // use default feature set
                            if (!(str.equals("org.w3c.svg.static") ||
                                    str.equals("org.w3c.dom.svg.all"))) {
                                continue choices;
                            }
                        } else {
                            boolean found = false;
                            for (int j = 0;
                                    j < rflist.getNumberOfItems(); j++) {
                                if (rflist.getItem(j).equals(str)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                                continue choices;
                        }
                    }
                }
                SVGStringList gsllist = graphic.getSystemLanguage();
                if (gsllist != null) {
                    if (gsllist.getNumberOfItems() == 0) {
                        if ((sllist != null) &&
                                sllist.getNumberOfItems() != 0) {
                            continue choices;
                        }
                    }
                    for (int i = 0; i < gsllist.getNumberOfItems(); i++) {
                        String str = (String) gsllist.getItem(i);
                        if (sllist == null) {
                            // use default feature set
                            if (!(str.equals("en"))) {
                                continue choices;
                            }
                        } else {
                            boolean found = false;
                            for (int j = 0;
                                    j < sllist.getNumberOfItems(); j++) {
                                if (sllist.getItem(j).equals(str)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                                continue choices;
                        }
                    }
                }
                renderElement((SVGElement) n, posx, posy);
                // only render the first valid one
                break;
            }
        }
    }

    protected void addLine(float x1, float y1, float x2, float y2, PDFColor sc, float sw)
	{
if ( debug )
System.out.println("PCLSVGRenderer.addLine(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", " + sc + ", " + sw + ")");
		if ( x1 == x2 )
		{
			addRect(x1 - sw/2, y1, sw, y2 - y1 + 1, 0, 0, sc, null, 0);
		}
		else if ( y1 == y2 || (Math.abs(y1 - y2) <= 0.24) )	// 72/300=0.24
		{
			addRect(x1, y1 - sw/2, x2 - x1 + 1, sw, 0, 0, sc, null, 0);
		}
		else if ( sc != null )
		{
			// Do something for these?

			// Convert dimensions to pixels.
			float cfact = 300f / 72f;	// 300 dpi, 1pt=1/72in
			int	ix1 = (int)(x1 * cfact);
			int	iy1 = (int)(y1 * cfact);
			int	ix2 = (int)(x2 * cfact);
			int	iy2 = (int)(y2 * cfact);
			int	isw = (int)(sw * cfact);
			int origix;

			// Normalize
			if ( iy1 > iy2 )
			{
				int tmp = ix1;
				ix1 = ix2;
				ix2 = tmp;
				tmp = iy1;
				iy1 = iy2;
				iy2 = tmp;
			}
			if ( ix1 > ix2 )
			{
				origix = ix2;
				ix1 -=ix2;
				ix2 = 0;
			}
			else
			{
				origix = ix1;
				ix2 -= ix1;
				ix1 = 0;
			}

			// Convert line width to a pixel run length.
//System.out.println("PCLRenderer.addLine(" + ix1 + ", " + iy1 + ", " + ix2 + ", " + iy2 + ", " + isw + ")");
			int	runlen = (int)Math.sqrt(Math.pow(isw, 2) * (1 + Math.pow((ix1 - ix2) / (iy1 - iy2), 2)));
//System.out.println("PCLRenderer.addLine: runlen = " + runlen);

			// Set Transparency modes and select shading.
			currentStream.add("\033*v0n1O\033*c" + (int)(100 - ((0.3f * sc.red() + 0.59f * sc.green() + 0.11f * sc.blue()) * 100f)) + "G\033*v2T");

			// Draw the line.
			int	d, dx, dy;
			int	Aincr, Bincr;
			int	xincr = 1;
			int	x, y;


			dx = Math.abs(ix2 - ix1);
			dy = iy2 - iy1;

			if ( origix < 0 )
				MessageHandler.errorln("PCLSVGRenderer.addLine() WARNING: Horizontal position out of bounds.");

			if ( dx > dy )
			{
				xincr = dx / dy;

				// Move to starting position.
				currentStream.add("\033*p" + origix + "x" + iy1 + "Y");
				x = ix1 - runlen / 2;
				iy2 += (isw / 2);
				// Start raster graphics
				currentStream.add("\033*t300R\033*r" + dx + "s1A\033*b1M");
			}
			else
			{
				// Move to starting position.
				currentStream.add("\033*p" + (origix - runlen / 2) + "x" + iy1 + "Y");
				x = ix1;
				// Start raster graphics
				currentStream.add("\033*t300R\033*r1A\033*b1M");
			}

			if ( ix1 > ix2 )
				xincr *= -1;
			d = 2 * dx - dy;
			Aincr = 2 * (dx - dy);
			Bincr = 2 * dx;

			y = iy1;

			xferLineBytes(x, runlen, null, -1);
			
			for ( y = iy1 + 1 ; y <= iy2 ; y++ )
			{
				if ( d >= 0 )
				{
					x += xincr;
					d += Aincr;
				}
				else
					d += Bincr;
				xferLineBytes(x, runlen, null, -1);
			}

			// End raster graphics
			currentStream.add("\033*rB");
			// Return to regular print mode.
			currentStream.add("\033*v0t0n0O");
		}
    }

	private void xferLineBytes(int	startpos, int bitcount, Vector save, int start2)
	{
//System.out.println("PCLRenderer.xferLineBytes(" + startpos + ", " + bitcount + ")");
		int	curbitpos = 0;
		if ( start2 > 0 && start2 <= (startpos + bitcount) )
		{
			bitcount += (start2 - startpos);
			start2 = 0;
		}

		char bytes[] = new char[((start2>startpos?start2:startpos) + bitcount) / 4 + 2];
		int	dlen = 0;
		byte dbyte = 0;
		int	bytepos = 0;

		do
		{
			int bits2set;
			if ( startpos < 0 )
			{
				bits2set = bitcount + startpos;
				startpos = 0;
			}
			else
				bits2set = bitcount;

			byte bittype = 0;
			do
			{
				if ( bytepos > 0 )
				{
					int inc = startpos - curbitpos;
					if ( (inc) >=  (8 - bytepos) )
					{
						curbitpos += (8 - bytepos);
						bytepos = 0;
						bytes[dlen++] = (char)0;
						bytes[dlen++] = (char)dbyte;
						dbyte = 0;
					}
					else
					{
						bytepos += inc;
						dbyte = (byte)(dbyte ^ (byte)(Math.pow(2, 8 - bytepos) - 1));
						curbitpos += inc;
					}
				}

				// Set runs of whole bytes.
				int	setbytes = (startpos - curbitpos) / 8;
				if ( setbytes > 0 )
				{
					curbitpos += setbytes * 8;
					while ( setbytes > 0 )
					{
						if ( setbytes > 256 )
						{
							bytes[dlen++] = 0xFF;
							setbytes -= 256;
						}
						else
						{
							bytes[dlen++] = (char)((setbytes - 1) & 0xFF);
							setbytes = 0;
						}
						bytes[dlen++] = (char)bittype;
					}
				}
				// move to position in the first byte.
				if ( curbitpos < startpos )
				{
					if ( bytepos == 0 )
						dbyte = bittype;
					bytepos += startpos - curbitpos;
					dbyte = (byte)(dbyte ^ (byte)(Math.pow(2, 8 - bytepos) - 1));
					curbitpos += bytepos;
					startpos += bits2set;
				}
				else
				{
					startpos += bits2set;
				}

				if ( bittype == 0 )
					bittype = (byte)0xFF;
				else
					bittype = 7;
			} while ( bittype != 7 );

			if ( start2 > 0 )
			{
				startpos = start2;
				start2 = -1;
			}
			else
				startpos = -1;
		} while ( startpos >= 0 );
		if ( bytepos > 0 )
		{
			bytes[dlen++] = (char)0;
			bytes[dlen++] = (char)dbyte;
		}
		if ( save == null )
		{
			currentStream.add("\033*b" + dlen + "W");
			currentStream.add(new String(bytes, 0, dlen));
		}
		else
		{
			String line = "\033*b" + dlen + "W" + new String(bytes, 0, dlen);
			currentStream.add(line);
			save.addElement(line);
		}
	}

    /**
     * add a filled rectangle to the current stream
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param r the red component of edges
     * @param g the green component of edges
     * @param b the blue component of edges
     * @param fr the red component of the fill
     * @param fg the green component of the fill
     * @param fb the blue component of the fill
     */
    protected void addRect(float x, float y, float w, float h, float rx, float ry,
			   PDFColor fc, PDFColor sc, float sw)
	{
if ( debug )
System.out.println("PCLSVGRenderer.addRect(" + x + ", " + y + ", " + w + ", " + h + ", " + rx + ", " + ry + ", " + fc + ", " + sc + ", " + sw + ")");

		if ( x < 0 || y < 0 )
			MessageHandler.errorln("PCLSVGRenderer.addRect() WARNING: Position out of bounds.");

		if ( rx == 0 || ry == 0 )
		{
			if ( fc != null )
			{
				int fillshade = (int)(100 - ((0.3f * fc.red() + 0.59f * fc.green() + 0.11f * fc.blue()) * 100f));
				currentStream.add("\033*v0n1O\033&a" + (x * 10) + "h" + ((y * 10)) + "V"
									+ "\033*c" + (w * 10) + "h" + (h * 10) + "v" + fillshade + "g2P\033*v0n0O");
			}
			if ( sc != null && sw > 0 )
			{
				String lend = "v" + String.valueOf((int)(100 - ((0.3f * sc.red() + 0.59f * sc.green() + 0.11f * sc.blue()) * 100f))) + "g2P";
				currentStream.add("\033*v0n1O");
				currentStream.add("\033&a" + ((x - sw/2) * 10) + "h" + (((y - sw/2)) * 10) + "V"
									+ "\033*c" + ((w + sw) * 10) + "h" + ((sw) * 10) + lend);
				currentStream.add("\033&a" + ((x - sw/2) * 10) + "h" + (((y - sw/2)) * 10) + "V"
									+ "\033*c" + ((sw) * 10) + "h" + ((h + sw) * 10) + lend);
				currentStream.add("\033&a" + ((x + w - sw/2) * 10) + "h" + (((y - sw/2)) * 10) + "V"
									+ "\033*c" + ((sw) * 10) + "h" + ((h + sw) * 10) + lend);
				currentStream.add("\033&a" + ((x - sw/2) * 10) + "h" + (((y + h - sw/2)) * 10) + "V"
									+ "\033*c" + ((w + sw) * 10) + "h" + ((sw) * 10) + lend);
				currentStream.add("\033*v0n0O");
			}
		}
		else
		{
			// Convert dimensions to pixels.
			float cfact = 300f / 72f;	// 300 dpi, 1pt=1/72in
			int	ix = (int)(x * cfact);
			int	iy = (int)(y * cfact);
			int	iw = (int)(w * cfact);
			int	ih = (int)(h * cfact);
			int	irx = (int)(rx * cfact);
			int	iry = (int)(ry * cfact);
			int	isw = (int)(sw * cfact);
			int	longwidth = 0;
			int	pass = 0;
			PDFColor thecolor = null;

			do
			{
				if ( pass == 0 && fc != null )
				{
					thecolor = fc;
				}
				else if ( pass == 1 && sc != null )
				{
					int	iswdiv2 = isw / 2;
					thecolor = sc;
					ix -= iswdiv2;
					iy -= iswdiv2;
					irx += iswdiv2;
					iry += iswdiv2;
					iw += isw;
					ih += isw;
					longwidth = (int)(isw * 1.414);
				}
				else
					thecolor = null;


				if ( thecolor != null )
				{
					int		tx = 0;
					int		ty = iry;
					long	a = irx;
					long	b = iry;
					long	Asquared = (long)Math.pow(a, 2);
					long	TwoAsquared = 2 * Asquared;
					long	Bsquared = (long)Math.pow(b, 2);
					long	TwoBsquared = 2 * Bsquared;
					long	d = Bsquared - Asquared * b + Asquared / 4;
					long	dx = 0;
					long	dy = TwoAsquared * b;
					int		rectlen = iw - 2 * irx;
					Vector	bottomlines = new Vector();

					int x0 = tx;

					// Set Transparency modes and select shading.
					currentStream.add("\033*v0n1O\033*c" + (int)(100 - ((0.3f * thecolor.red() + 0.59f * thecolor.green() + 0.11f * thecolor.blue()) * 100f)) + "G\033*v2T");
					// Move to starting position.
					currentStream.add("\033*p" + ix + "x" + iy + "Y");
					// Start raster graphics
					currentStream.add("\033*t300R\033*r" + iw + "s1A\033*b1M");

					while ( dx < dy )
					{
						if ( d > 0 )
						{
							if ( pass == 0 || ty > (iry - isw) )
								xferLineBytes(irx - x0, rectlen + 2 * x0, bottomlines, -1);
							else
								xferLineBytes(irx - x0, longwidth, bottomlines, iw - irx + x0 - longwidth);
							x0 = tx + 1;
							ty--;
							dy -= TwoAsquared;
							d -= dy;
						}
						tx++;
						dx += TwoBsquared;
						d += Bsquared + dx;
					}

					d += (3 * (Asquared - Bsquared) / 2 - (dx + dy)) / 2;

					while ( ty > 0 )
					{
						if ( pass == 0 || ty >= (iry - isw) )
							xferLineBytes(irx - tx, rectlen + 2 * tx, bottomlines, -1);
						else
							xferLineBytes(irx - tx, isw, bottomlines, iw - irx + tx - isw);
						
						if ( d < 0 )
						{
							tx++;
							dx += TwoBsquared;
							d += dx;
						}
						ty--;
						dy -= TwoAsquared;
						d += Asquared - dy;
					}

					// Draw the middle part of the rectangle
					int	midlen = ih - 2 * iry;
					if ( midlen > 0 )
					{
						if ( pass == 0 )
							xferLineBytes(0, iw, null, -1);
						else
							xferLineBytes(0, isw, null, iw - isw);
						currentStream.add("\033*b3M");
						for ( int countr = midlen - 1 ; countr > 0 ; countr-- )
							currentStream.add("\033*b0W");
						currentStream.add("\033*b1M");
					}

					// Draw the bottom.
					for ( int countr = bottomlines.size() - 1 ; countr >= 0 ; countr-- )
						currentStream.add((String)bottomlines.elementAt(countr));

					// End raster graphics
					currentStream.add("\033*rB");
					// Return to regular print mode.
					currentStream.add("\033*v0t0n0O");
				}
				pass++;
			} while ( pass < 2 );
		}
    }

	// Add a polyline or polygon. Does not support fills yet!!!
    protected void addPolyline(Vector points, int posx, int posy, PDFColor fc, PDFColor sc, float sw, boolean close)
    {
		PathPoint pc;
    	float lastx = 0;
    	float lasty = 0;
    	float curx = 0;
    	float cury = 0;
    	float startx = 0;
    	float starty = 0;
    	Enumeration e = points.elements();
    	if(e.hasMoreElements())
    	{
    		pc = (PathPoint)e.nextElement();
			lastx = rendxoffset / 10 + pc.x + posx / 1000;
			lasty = ((pageHeight / 10) - posy/1000) + pc.y;
			startx = lastx;
			starty = lasty;
			//currentStream.add(lastx + " " + lasty + " m\n");
    	}
    	while(e.hasMoreElements())
    	{
    		pc = (PathPoint)e.nextElement();
			curx = rendxoffset / 10 + pc.x + posx / 1000;
			cury = ((pageHeight / 10) - posy/1000) + pc.y;
    		addLine(lastx, lasty, curx, cury, sc, sw);
			lastx = curx;
			lasty = cury;
			//currentStream.add(lastx + " " + lasty + " l\n");
    	}
    	if(close)
		{
    		addLine(lastx, lasty, startx, starty, sc, sw);
			//currentStream.add("h\n");
		}
		//doDrawing(di);
    }

	public void renderImage(String href, float x, float y, float width, float height)
	{
		if ( x < 0 || y < 0 )
			MessageHandler.errorln("PCLSVGRenderer.renderImage() WARNING: Position out of bounds.");

		try
		{
			if ( href.indexOf(":") == -1 )
				href = "file:" + href;
			FopImage img = FopImageFactory.Make(href);
			if(img != null)
			{
				if ( img instanceof SVGImage )
				{
					SVGSVGElement svg = ((SVGImage)img).getSVGDocument().getRootElement();
					renderSVG(svg, (int)x * 1000, (int)y * 1000);
				}
				else
				{
					currentStream.add("\033&a" + (x * 10) + "h" + (y * 10) + "V");
					renderer.printBMP(img, (int)x, (int)y, (int)width, (int)height);
				}

			}
		}
		catch(Exception e)
		{
			MessageHandler.errorln("could not add image to SVG: " + href);
		}
	}

    /**
     * A symbol has a viewbox and preserve aspect ratio.
     */
    protected void renderSymbol(SVGSymbolElement symbol, int x, int y) {
        NodeList nl = symbol.getChildNodes();
        for (int count = 0; count < nl.getLength(); count++) {
            Node n = nl.item(count);
            if (n instanceof SVGElement) {
                renderElement((SVGElement) n, x, y);
            }
        }
    }

	/**
	 * Main rendering selection.
	 * This applies any transform ans style and then calls the appropriate
	 * rendering method depending on the type of the element.
	 */
	public void renderElement(SVGElement area, int posx, int posy)
	{
if ( debug )
System.out.println("PCLRenderer.renderElement(" + fontState + ", " + area + ", " + posx + ", " + posy + ")");
		int x = posx;
		int y = posy;
		SVGStylable style = null;
		if ( area instanceof SVGStylable )
			style = (SVGStylable)area;
		PDFColor fillColour = null;
		PDFColor strokeColour = null;
		float strokeWidth = 1;

		//currentStream.add("q\n");
		//if( area instanceof SVGTransformable )
		//{
		//	SVGTransformable tf = (SVGTransformable)area;
		//	SVGAnimatedTransformList trans = tf.getTransform();
		//	SVGRect bbox = tf.getBBox();
		//	if(trans != null) {
		//		applyTransform(trans, bbox);
		//	}
		//}

		if(style != null)
		{
	        CSSValue sp;
	        sp = style.getPresentationAttribute("fill");
	        if (sp != null)
	        {
	            if (sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE)
	            {
	                if (((CSSPrimitiveValue) sp).getPrimitiveType() == CSSPrimitiveValue.CSS_RGBCOLOR)
	                {
	                    RGBColor col = ((CSSPrimitiveValue) sp).getRGBColorValue();
	                    CSSPrimitiveValue val;
	                    val = col.getRed();
	                    float red = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
	                    val = col.getGreen();
	                    float green = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
	                    val = col.getBlue();
	                    float blue = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
	                    fillColour = new PDFColor(red, green, blue);
	                    currentColour = fillColour;
	                }
	                else if ( ((CSSPrimitiveValue) sp).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING)
	                {
	                    String str = ((CSSPrimitiveValue) sp).getCssText();
	                    if ( str.equals("none") )
	                    {
	                        fillColour = null;
	                    }
	                    else if ( str.equals("currentColor") )
	                    {
	                        fillColour = currentColour;
	                    }
	                }
	            }
	        }
	        else
	        {
	            fillColour = new PDFColor(0, 0, 0);
	        }
	        sp = style.getPresentationAttribute("stroke");
	        if ( sp != null )
	        {
	            if ( sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE )
	            {
	                if ( ((CSSPrimitiveValue) sp).getPrimitiveType() == CSSPrimitiveValue.CSS_RGBCOLOR )
	                {
	                    RGBColor col = ((CSSPrimitiveValue) sp).getRGBColorValue();
	                    CSSPrimitiveValue val;
	                    val = col.getRed();
	                    float red = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
	                    val = col.getGreen();
	                    float green = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
	                    val = col.getBlue();
	                    float blue = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
	                    strokeColour = new PDFColor(red, green, blue);
	                }
	                else if ( ((CSSPrimitiveValue) sp).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING )
	                {
	                    String str = ((CSSPrimitiveValue) sp).getCssText();
	                    if (str.equals("none"))
	                    {
							strokeColour = null;
	                    }
	                }
	            }
	        }
	        else
	        {
	            strokeColour = new PDFColor(0, 0, 0);
	        }
	        sp = style.getPresentationAttribute("stroke-width");
	        if ( sp != null )
	        {
	            if ( sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE )
	            {
	                strokeWidth = ((CSSPrimitiveValue) sp).getFloatValue(CSSPrimitiveValue.CSS_PT);
	            }
	        }
		}

		if (area instanceof SVGRectElement)
		{
			SVGRectElement rg = (SVGRectElement)area;
			float rectx = rendxoffset / 10 + rg.getX().getBaseVal().getValue() + posx / 1000;
			float recty = ((pageHeight / 10) - posy/1000) + rg.getY().getBaseVal().getValue();
			float rx = rg.getRx().getBaseVal().getValue();
			float ry = rg.getRy().getBaseVal().getValue();
			float rw = rg.getWidth().getBaseVal().getValue();
			float rh = rg.getHeight().getBaseVal().getValue();
			addRect(rectx, recty, rw, rh, rx, ry, fillColour, strokeColour, strokeWidth);
		}
		else if (area instanceof SVGLineElement)
		{
			SVGLineElement lg = (SVGLineElement)area;
			float x1 = rendxoffset / 10 + lg.getX1().getBaseVal().getValue() + posx / 1000;
			float y1 = ((pageHeight / 10) - posy/1000) + lg.getY1().getBaseVal().getValue();
			float x2 = rendxoffset / 10 + lg.getX2().getBaseVal().getValue() + posx / 1000;
			float y2 = ((pageHeight / 10) - posy/1000) + lg.getY2().getBaseVal().getValue();
			addLine(x1,y1,x2,y2, strokeColour, strokeWidth);
		}
		else if (area instanceof SVGTextElementImpl)
		{
			//currentStream.add("BT\n");
			//renderText((SVGTextElementImpl)area, rendxoffset + posx / 1000f, ((float)(pageHeight / 10) - posy/1000f));
			//currentStream.add("ET\n");
			SVGTextRenderer str = new SVGTextRenderer(fontState, (SVGTextElementImpl)area, rendxoffset / 10 + posx / 1000f, ((float)(pageHeight / 10) - posy/1000f));
			str.renderText((SVGTextElementImpl)area);
		}
		else if (area instanceof SVGCircleElement)
		{
			SVGCircleElement cg = (SVGCircleElement)area;
			float cx = rendxoffset / 10 + cg.getCx().getBaseVal().getValue() + posx / 1000;
			float cy = ((pageHeight / 10) - posy/1000) + cg.getCy().getBaseVal().getValue();
			float r = cg.getR().getBaseVal().getValue();
			//addCircle(cx,cy,r, di);
			addRect(cx - r, cy - r, 2 * r, 2 * r, r, r, fillColour, strokeColour, strokeWidth);
		}
		else if (area instanceof SVGEllipseElement)
		{
			SVGEllipseElement cg = (SVGEllipseElement)area;
			float cx = rendxoffset / 10 + cg.getCx().getBaseVal().getValue() + posx / 1000;
			float cy = ((pageHeight / 10) - posy/1000) + cg.getCy().getBaseVal().getValue();
			float rx = cg.getRx().getBaseVal().getValue();
			float ry = cg.getRy().getBaseVal().getValue();
			//addEllipse(cx,cy,rx,ry, di);
			addRect(cx - rx, cy - ry, 2 * rx, 2 * ry, rx, ry, fillColour, strokeColour, strokeWidth);
		}
		else if (area instanceof SVGPathElementImpl)
		{
			//addPath(((SVGPathElementImpl)area).pathElements, posx, posy, di);
		}
		else if (area instanceof SVGPolylineElementImpl)
		{
			addPolyline(((SVGPolylineElementImpl)area).points, posx, posy, fillColour, strokeColour, strokeWidth, false);
		}
		else if (area instanceof SVGPolygonElementImpl)
		{
			addPolyline(((SVGPolylineElementImpl)area).points, posx, posy, fillColour, strokeColour, strokeWidth, true);
		}
		else if (area instanceof SVGGElementImpl)
		{
			renderGArea((SVGGElementImpl)area, x, y);
		}
		else if(area instanceof SVGUseElementImpl)
		{
            SVGUseElementImpl ug = (SVGUseElementImpl) area;
            String ref = ug.link;
            //			ref = ref.substring(1, ref.length());
            SVGElement graph = null;
            graph = locateDef(ref, ug);
            if (graph != null) {
                // probably not the best way to do this, should be able
                // to render without the style being set.
                //				SVGElement parent = graph.getGraphicParent();
                //				graph.setParent(area);
                // need to clip (if necessary) to the use area
                // the style of the linked element is as if it was
                // a direct descendant of the use element.

                // scale to the viewBox

                if (graph instanceof SVGSymbolElement) {
                    //currentStream.write("q\n");
                    SVGSymbolElement symbol = (SVGSymbolElement) graph;
                    SVGRect view = symbol.getViewBox().getBaseVal();
                    float usex = ug.getX().getBaseVal().getValue();
                    float usey = ug.getY().getBaseVal().getValue();
                    float usewidth = ug.getWidth().getBaseVal().getValue();
                    float useheight =
                      ug.getHeight().getBaseVal().getValue();
                    float scaleX;
                    float scaleY;
                    scaleX = usewidth / view.getWidth();
                    scaleY = useheight / view.getHeight();
                    //currentStream.write(usex + " " + usey + " m\n");
                    //currentStream.write((usex + usewidth) + " " +
                    //                    usey + " l\n");
                    //currentStream.write((usex + usewidth) + " " +
                    //                    (usey + useheight) + " l\n");
                    //currentStream.write(usex + " " +
                    //                    (usey + useheight) + " l\n");
                    //currentStream.write("h\n");
                    //currentStream.write("W\n");
                    //currentStream.write("n\n");
                    //currentStream.write(scaleX + " 0 0 " + scaleY +
                    //                    " " + usex + " " + usey + " cm\n");
                    renderSymbol(symbol, posx, posy);
                    //currentStream.write("Q\n");
                } else {
                    renderElement(graph, posx, posy);
                }
                //				graph.setParent(parent);
            }
            else
            {
                MessageHandler.logln("Use Element: " + ref + " not found");
            }
		}
		else if (area instanceof SVGImageElementImpl)
		{
			SVGImageElementImpl ig = (SVGImageElementImpl)area;
			renderImage(ig.link, ig.x, ig.y, ig.width, ig.height);
		}
		else if (area instanceof SVGSVGElement)
		{
            //currentStream.write("q\n");
            SVGSVGElement svgel = (SVGSVGElement) area;
            float svgx = 0;
            if (svgel.getX() != null)
                svgx = svgel.getX().getBaseVal().getValue();
            float svgy = 0;
            if (svgel.getY() != null)
                svgy = svgel.getY().getBaseVal().getValue();
            //currentStream.write(1 + " 0 0 " + 1 + " " + svgx + " " +
            //                    svgy + " cm\n");
            renderSVG(svgel, (int)(x + 1000 * svgx),
                      (int)(y + 1000 * svgy));
            //currentStream.write("Q\n");
            //		} else if (area instanceof SVGSymbolElement) {
            // 'symbol' element is not rendered (except by 'use')
		}
		else if (area instanceof SVGAElement)
		{
			SVGAElement ael = (SVGAElement)area;
			org.w3c.dom.NodeList nl = ael.getChildNodes();
			for ( int count = 0 ; count < nl.getLength() ; count++ )
			{
				org.w3c.dom.Node n = nl.item(count);
				if ( n instanceof SVGElement )
				{
					if ( n instanceof GraphicElement )
					{
						SVGRect rect = ((GraphicElement)n).getBBox();
						if ( rect != null )
						{
/*							currentAnnotList = this.pdfDoc.makeAnnotList();
							currentPage.setAnnotList(currentAnnotList);
							String dest = linkSet.getDest();
							int linkType = linkSet.getLinkType();
							currentAnnotList.addLink(
								this.pdfDoc.makeLink(lrect.getRectangle(), dest, linkType));
							currentAnnotList = null;
*/						}
					}
					renderElement((SVGElement)n, posx, posy);
				}
			}
		}
		else if ( area instanceof SVGSwitchElement )
		{
			handleSwitchElement(posx, posy, (SVGSwitchElement)area);
		}
		// should be done with some cleanup code, so only
		// required values are reset.
		//currentStream.add("Q\n");
	}

    /**
     * Adds an svg string to the output.
     * This handles the escaping of special pdf chars and deals with
     * whitespace.
     */
    protected float addSVGStr(FontState fs, float currentX, String str,
                              boolean spacing) {
        boolean inbetween = false;
        boolean addedspace = false;
        StringBuffer pdf = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch)
            {
                case '\t':
                case ' ':
                    if (spacing) {
                        pdf = pdf.append(' ');
                        currentX += fs.width(' ') / 1000f;
                    } else {
                        if (inbetween && !addedspace) {
                            addedspace = true;
                            pdf = pdf.append(' ');
                            currentX += fs.width(' ') / 1000f;
                        }
                    }
                    break;
                case '\n':
                case '\r':
                    if (spacing) {
                        pdf = pdf.append(' ');
                        currentX += fs.width(' ') / 1000f;
                    }
                    break;
                default:
                    addedspace = false;
                    pdf = pdf.append(ch);
                    currentX += fs.width(ch) / 1000f;
                    inbetween = true;
                    break;
            }
        }
        currentStream.add(pdf.toString());
        return currentX;
    }

    /**
     * Locates a defined element in an svg document.
     * Either gets the element defined by its "id" in the current
     * SVGDocument, or if the uri reference is to an external
     * document it loads the document and returns the element.
     */
    protected SVGElement locateDef(String ref, SVGElement currentElement) {
        int pos;
        ref = ref.trim();
        pos = ref.indexOf("#");
        if (pos == 0) {
            // local doc
            Document doc = currentElement.getOwnerDocument();
            Element ele =
              doc.getElementById(ref.substring(1, ref.length()));
            if (ele instanceof SVGElement) {
                return (SVGElement) ele;
            }
        } else if (pos != -1) {
            String href = ref.substring(0, pos);
            if (href.indexOf(":") == -1) {
                href = "file:" + href;
            }
            try {
                // this is really only to get a cached svg image
                FopImage img = FopImageFactory.Make(href);
                if (img instanceof SVGImage) {
                    SVGDocument doc = ((SVGImage) img).getSVGDocument();
                    Element ele = doc.getElementById(
                                    ref.substring(pos + 1, ref.length()));
                    if (ele instanceof SVGElement) {
                        return (SVGElement) ele;
                    }
                }
            } catch (Exception e) {
                MessageHandler.errorln(e.toString());
            }
        }
        return null;
    }

    /**
     * This class is used to handle the rendering of svg text.
     * This is so that it can deal with the recursive rendering
     * of text markup, while keeping track of the state and position.
     */
    class SVGTextRenderer {
        FontState fs;
        String transstr;
        float currentX;
        float currentY;
        float baseX;
        float baseY;
        SVGMatrix matrix;
        float x;
        float y;

        SVGTextRenderer(FontState fontState, SVGTextElementImpl tg,
                        float x, float y) {
            fs = fontState;

            //PDFNumber pdfNumber = new PDFNumber();
            SVGTransformList trans = tg.getTransform().getBaseVal();
            matrix = trans.consolidate().getMatrix();
            //transstr = (pdfNumber.doubleOut(matrix.getA()) + " " +
            //            pdfNumber.doubleOut(matrix.getB()) + " " +
            //            pdfNumber.doubleOut(matrix.getC()) + " " +
            //            pdfNumber.doubleOut(-matrix.getD()) + " ");
            this.x = x;
            this.y = y;
        }

		void renderText(SVGTextElementImpl te) {
				float xoffset = 0;

				if (te.anchor.getEnum() != TextAnchor.START) {
						// This is a bit of a hack: The code below will update
						// the current position, so all I have to do is to
						// prevent that the code will write anything to the
						// PCL stream...
						currentStream.setDoOutput(false);
						
						_renderText (te, 0f, true);

						float width = currentX - te.x;
						currentStream.setDoOutput(true);

						if (te.anchor.getEnum() == TextAnchor.END) {
								xoffset = -width;
						} else if (te.anchor.getEnum() == TextAnchor.MIDDLE) {
								xoffset = -width/2;
						}
				}

				_renderText (te, xoffset, false);
		}

		void _renderText(SVGTextElementImpl te, float xoffset, boolean getWidthOnly)
        {
            //DrawingInstruction di = applyStyle(te, te);
            //if (di.fill) {
            //    if (di.stroke) {
            //        currentStream.write("2 Tr\n");
            //    } else {
            //        currentStream.write("0 Tr\n");
            //    }
            //} else if (di.stroke) {
            //    currentStream.write("1 Tr\n");
            //}
            updateFont(te, fs);

            float tx = te.x;
            float ty = te.y;
            currentX = x + tx + xoffset;
            currentY = y + ty;
            baseX = currentX;
            baseY = currentY;
            NodeList nodel = te.getChildNodes();
            //		Vector list = te.textList;
            for (int count = 0; count < nodel.getLength(); count++) {
                Object o = nodel.item(count);
                //applyStyle(te, te);
                if ( o instanceof CharacterData )
                {
                    String str = ((CharacterData) o).getData();
                    //currentStream.write(transstr +
                    //                    (currentX + matrix.getE()) + " " +
                    //                    (baseY + matrix.getF()) + " Tm " + "(");
                    boolean spacing = "preserve".equals(te.getXMLspace());
                    //currentX = addSVGStr(fs, currentX, str, spacing);
                    //currentStream.write(") Tj\n");
					currentStream.add("\033&a" + (currentX + matrix.getE())*10 + "h" + (baseY + matrix.getF())*10 + "V");
                    currentX = addSVGStr(fs, currentX, str, spacing);
                }
                else if ( o instanceof SVGTextPathElementImpl )
                {
                    SVGTextPathElementImpl tpg = (SVGTextPathElementImpl) o;
                    String ref = tpg.str;
                    SVGElement graph = null;
                    graph = locateDef(ref, tpg);
                    if (graph instanceof SVGPathElementImpl) {
                        // probably not the best way to do this, should be able
                        // to render without the style being set.
                        //					GraphicImpl parent = graph.getGraphicParent();
                        //					graph.setParent(tpg);
                        // set text path??
                        // how should this work
                        //					graph.setParent(parent);
                    }
                } else if (o instanceof SVGTRefElementImpl) {
                    SVGTRefElementImpl trg = (SVGTRefElementImpl) o;
                    String ref = trg.ref;
                    SVGElement element = locateDef(ref, trg);
                    if (element instanceof SVGTextElementImpl) {
                        //					GraphicImpl parent = graph.getGraphicParent();
                        //					graph.setParent(trg);
                        SVGTextElementImpl tele =
                          (SVGTextElementImpl) element;
                        // the style should be from tele, but it needs to be placed as a child
                        // of trg to work
                        //di = applyStyle(trg, trg);
                        //if (di.fill) {
                        //    if (di.stroke) {
                        //        currentStream.write("2 Tr\n");
                        //    } else {
                        //        currentStream.write("0 Tr\n");
                        //    }
                        //} else if (di.stroke) {
                        //    currentStream.write("1 Tr\n");
                        //}
                        boolean changed = false;
                        FontState oldfs = fs;
                        changed = updateFont(te, fs);
                        NodeList nl = tele.getChildNodes();
                        boolean spacing =
                          "preserve".equals(trg.getXMLspace());
                        renderTextNodes(spacing, nl,
                                        trg.getX().getBaseVal(),
                                        trg.getY().getBaseVal(),
                                        trg.getDx().getBaseVal(),
                                        trg.getDy().getBaseVal());

                        if (changed) {
                            fs = oldfs;
                            //currentStream.write("/" +
                            //                    fs.getFontName() + " " +
                            //                    fs.getFontSize() / 1000f + " Tf\n");
                        }
                        //					graph.setParent(parent);
                    }
                } else if (o instanceof SVGTSpanElementImpl) {
                    SVGTSpanElementImpl tsg = (SVGTSpanElementImpl) o;
                    //applyStyle(tsg, tsg);
                    boolean changed = false;
                    FontState oldfs = fs;
                    changed = updateFont(tsg, fs);
                    boolean spacing = "preserve".equals(tsg.getXMLspace());
                    renderTextNodes(spacing, tsg.getChildNodes(),
                                    tsg.getX().getBaseVal(),
                                    tsg.getY().getBaseVal(),
                                    tsg.getDx().getBaseVal(),
                                    tsg.getDy().getBaseVal());

                    //				currentX += fs.width(' ') / 1000f;
                    if (changed) {
                        fs = oldfs;
                        //currentStream.write("/" + fs.getFontName() +
                        //                    " " + fs.getFontSize() / 1000f + " Tf\n");
                    }
                } else {
                    MessageHandler.errorln("Error: unknown text element " + o);
                }
            }
        }

        void renderTextNodes(boolean spacing, NodeList nl,
                             SVGLengthList xlist, SVGLengthList ylist,
                             SVGLengthList dxlist, SVGLengthList dylist) {
            boolean inbetween = false;
            boolean addedspace = false;
            int charPos = 0;
            float xpos = currentX;
            float ypos = currentY;

            for (int count = 0; count < nl.getLength(); count++) {
                Node n = nl.item(count);
                if (n instanceof CharacterData) {
                    //StringBuffer pdf = new StringBuffer();
                    String str = ((CharacterData) n).getData();
                    for (int i = 0; i < str.length(); i++) {
                        char ch = str.charAt(i);
                        xpos = currentX;
                        ypos = currentY;
                        if (ylist.getNumberOfItems() > charPos) {
                            ypos = baseY + (ylist.getItem(charPos)).
                                   getValue();
                        }
                        if (dylist.getNumberOfItems() > charPos) {
                            ypos = ypos + (dylist.getItem(charPos)).
                                   getValue();
                        }
                        if (xlist.getNumberOfItems() > charPos) {
                            xpos = baseX + (xlist.getItem(charPos)).
                                   getValue();
                        }
                        if (dxlist.getNumberOfItems() > charPos) {
                            xpos = xpos + (dxlist.getItem(charPos)).
                                   getValue();
                        }

                        switch (ch) {
                            case '\t':
                            case ' ':
                                if (spacing) {
                                    currentX = xpos + fs.width(' ') /
                                               1000f;
                                    currentY = ypos;
                                    charPos++;
                                } else {
                                    if (inbetween && !addedspace) {
                                        addedspace = true;
                                        currentX = xpos + fs.width(' ')
                                                   / 1000f;
                                        currentY = ypos;
                                        charPos++;
                                    }
                                }
                                break;
                            case '\n':
                            case '\r':
                                if (spacing) {
                                    currentX = xpos + fs.width(' ') /
                                               1000f;
                                    currentY = ypos;
                                    charPos++;
                                }
                                break;
                            default:
                                addedspace = false;
                                //pdf = pdf.append(transstr +
                                //                 (xpos + matrix.getE()) +
                                //                 " " + (ypos +
                                //                        matrix.getF()) + " Tm " +
                                //                 "(" + ch + ") Tj\n");
								currentStream.add("\033&a" + (xpos + matrix.getE())*10 + "h" + (ypos + matrix.getF())*10 + "V" + ch);
                                currentX = xpos + fs.width(ch) / 1000f;
                                currentY = ypos;
                                charPos++;
                                inbetween = true;
                                break;
                        }
                        //currentStream.write(pdf.toString());
                    }
                }
            }
        }

        protected boolean updateFont(SVGStylable style, FontState fs) {
            boolean changed = false;
            String fontFamily = fs.getFontFamily();
            CSSValue sp = style.getPresentationAttribute("font-family");
            if (sp != null &&
                    sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
                        CSSPrimitiveValue.CSS_STRING) {
                    fontFamily = sp.getCssText();
                }
            }
            if (!fontFamily.equals(fs.getFontFamily())) {
                changed = true;
            }
            String fontStyle = fs.getFontStyle();
            sp = style.getPresentationAttribute("font-style");
            if (sp != null &&
                    sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
                        CSSPrimitiveValue.CSS_STRING) {
                    fontStyle = sp.getCssText();
                }
            }
            if (!fontStyle.equals(fs.getFontStyle())) {
                changed = true;
            }
            String fontWeight = fs.getFontWeight();
            sp = style.getPresentationAttribute("font-weight");
            if (sp != null &&
                    sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
                        CSSPrimitiveValue.CSS_STRING) {
                    fontWeight = sp.getCssText();
                }
            }
            if (!fontWeight.equals(fs.getFontWeight())) {
                changed = true;
            }
            float newSize = fs.getFontSize() / 1000f;
            sp = style.getPresentationAttribute("font-size");
            if (sp != null &&
                    sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
                //		    if(((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
                newSize = ((CSSPrimitiveValue) sp).getFloatValue(
                            CSSPrimitiveValue.CSS_PT);
                //		    }
            }
            if (fs.getFontSize() / 1000f != newSize) {
                changed = true;
            }
            if (changed) {
                try {
					fs = new FontState(fs.getFontInfo(), fontFamily,
														 fontStyle, fontWeight, (int)(newSize * 1000),
														 FontVariant.NORMAL);
                } catch (Exception fope) {
                }
                this.fs = fs;

                //currentStream.write("/" + fs.getFontName() + " " +
                //                    newSize + " Tf\n");
				renderer.setFont(fs.getFontName(), newSize * 1000);
            } else {
                if (!currentFontName.equals(fs.getFontName()) ||
                        currentFontSize != fs.getFontSize()) {
                    //				currentFontName = fs.getFontName();
                    //				currentFontSize = fs.getFontSize();
                    //currentStream.write("/" + fs.getFontName() + " " +
                    //                    (fs.getFontSize() / 1000) + " Tf\n");
					renderer.setFont(fs.getFontName(), fs.getFontSize());
                }
            }
            return changed;
        }
    }
}
