/*-- $Id$ --

 ============================================================================
					 The Apache Software License, Version 1.1
 ============================================================================

	Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of	source code must  retain the above copyright  notice,
	this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
	this list of conditions and the following disclaimer in the documentation
	and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
	include  the following	acknowledgment:  "This product includes  software
	developed  by the  Apache Software Foundation  (http://www.apache.org/)."
	Alternately, this  acknowledgment may  appear in the software itself,  if
	and wherever such third-party acknowledgments normally appear.

 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
	endorse  or promote  products derived  from this  software without	prior
	written permission. For written permission, please contact
	apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
	"Apache" appear  in their name,  without prior written permission  of the
	Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR	PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT	OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)	HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,	WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR	OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software	consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software	Foundation and was	originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */

package org.apache.fop.render.pdf;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.svg.PathPoint;
import org.apache.fop.pdf.*;
import org.apache.fop.layout.*;
import org.apache.fop.image.*;

import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.css.*;
import org.w3c.dom.svg.SVGLength;

// Java
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.awt.Rectangle;
import java.util.Vector;
import java.util.Hashtable;

/**
 * Renderer that renders SVG to PDF
 */
public class SVGRenderer {

		/** the PDF Document being created */
		protected PDFDocument pdfDoc;

		protected FontState fontState;

		/** the /Resources object of the PDF document being created */
		//	protected PDFResources pdfResources;

		/** the current stream to add PDF commands to */
		StringWriter currentStream = new StringWriter();

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

	// The toRadians() and toDegrees() methods of the Math class are not available in JDK 1.1, so reproduce here
    /**
     * Converts an angle measured in degrees to the equivalent angle
     * measured in radians.
     *
     * @param   angdeg   an angle, in degrees
     * @return  the measurement of the angle <code>angdeg</code>
     *          in radians.
     * @since   JDK1.2
     */
    public static double toRadians(double angdeg) {
	return angdeg / 180.0 * Math.PI;
    }
    /**
     * Converts an angle measured in radians to the equivalent angle
     * measured in degrees.
     *
     * @param   angrad   an angle, in radians
     * @return  the measurement of the angle <code>angrad</code>
     *          in degrees.
     * @since   JDK1.2
     */
    public static double toDegrees(double angrad) {
	return angrad * 180.0 / Math.PI;
    }

		/**
		 * create the SVG renderer
		 */
		public SVGRenderer(FontState fs, PDFDocument doc, String font,
											 int size, int xpos, int ypos) {
				pdfDoc = doc;
				currentFontName = font;
				currentFontSize = size;
				currentYPosition = ypos;
				currentXPosition = xpos;
				fontState = fs;
		}

		public String getString() {
				return currentStream.toString();
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
						if (n instanceof SVGTests) {
								SVGTests graphic = (SVGTests) n;
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

		/**
		 * add a line to the current stream
		 *
		 * @param x1 the start x location in millipoints
		 * @param y1 the start y location in millipoints
		 * @param x2 the end x location in millipoints
		 * @param y2 the end y location in millipoints
		 * @param th the thickness in millipoints
		 * @param r the red component
		 * @param g the green component
		 * @param b the blue component
		 */
		protected void addLine(float x1, float y1, float x2, float y2,
													 DrawingInstruction di) {
				String str;
				PDFNumber pdfNumber = new PDFNumber();
				str = "" + pdfNumber.doubleOut(x1) + " " + pdfNumber.doubleOut(y1) + " m " + pdfNumber.doubleOut(x2) + " " + pdfNumber.doubleOut(y2) + " l";
				if (di != null && di.fill)
						currentStream.write(str + " f\n"); // ??
				currentStream.write(str + " S\n");
		}

		/**
		 * Add an SVG circle
		 * Uses bezier curves to approximate the shape of a circle.
		 */
		protected void addCircle(float cx, float cy, float r,
														 DrawingInstruction di) {
				PDFNumber pdfNumber = new PDFNumber();
				String str;
				str = "" + pdfNumber.doubleOut(cx) + " " + pdfNumber.doubleOut((cy - r)) + " m\n" + "" +
							pdfNumber.doubleOut((cx + 21 * r / 40f)) + " " + pdfNumber.doubleOut((cy - r)) + " " +
							pdfNumber.doubleOut((cx + r)) + " " + pdfNumber.doubleOut((cy - 21 * r / 40f)) + " " +
							pdfNumber.doubleOut((cx + r)) + " " + pdfNumber.doubleOut(cy) + " c\n" + "" + pdfNumber.doubleOut((cx + r)) + " " +
							pdfNumber.doubleOut((cy + 21 * r / 40f)) + " " + pdfNumber.doubleOut((cx + 21 * r / 40f)) +
							" " + pdfNumber.doubleOut((cy + r)) + " " + pdfNumber.doubleOut(cx) + " " + pdfNumber.doubleOut((cy + r)) + " c\n" +
							"" + pdfNumber.doubleOut((cx - 21 * r / 40f)) + " " + pdfNumber.doubleOut((cy + r)) + " " +
							pdfNumber.doubleOut(cx - r) + " " + pdfNumber.doubleOut(cy + 21 * r / 40f) + " " +
							pdfNumber.doubleOut(cx - r) + " " + pdfNumber.doubleOut(cy) + " c\n" + "" + pdfNumber.doubleOut(cx - r) + " " +
							pdfNumber.doubleOut(cy - 21 * r / 40f) + " " + pdfNumber.doubleOut(cx - 21 * r / 40f) +
							" " + pdfNumber.doubleOut(cy - r) + " " + pdfNumber.doubleOut(cx) + " " + pdfNumber.doubleOut(cy - r) + " c\n";

				currentStream.write(str);
				doDrawing(di);
		}

		/**
		 * Add an SVG ellips
		 * Uses bezier curves to approximate the shape of an ellipse.
		 */
		protected void addEllipse(float cx, float cy, float rx, float ry,
															DrawingInstruction di) {
				PDFNumber pdfNumber = new PDFNumber();
				String str;
				str = "" + pdfNumber.doubleOut(cx) + " " + pdfNumber.doubleOut(cy - ry) + " m\n" + "" +
							pdfNumber.doubleOut(cx + 21 * rx / 40f) + " " + pdfNumber.doubleOut(cy - ry) + " " +
							pdfNumber.doubleOut(cx + rx) + " " + pdfNumber.doubleOut(cy - 21 * ry / 40f) + " " +
							pdfNumber.doubleOut(cx + rx) + " " + pdfNumber.doubleOut(cy) + " c\n" + "" + pdfNumber.doubleOut(cx + rx) + " " +
							pdfNumber.doubleOut(cy + 21 * ry / 40f) + " " + pdfNumber.doubleOut(cx + 21 * rx / 40f) +
							" " + pdfNumber.doubleOut(cy + ry) + " " + pdfNumber.doubleOut(cx) + " " + pdfNumber.doubleOut(cy + ry) +
							" c\n" + "" + pdfNumber.doubleOut(cx - 21 * rx / 40f) + " " + pdfNumber.doubleOut(cy + ry) +
							" " + pdfNumber.doubleOut(cx - rx) + " " + pdfNumber.doubleOut(cy + 21 * ry / 40f) + " " +
							pdfNumber.doubleOut(cx - rx) + " " + pdfNumber.doubleOut(cy) + " c\n" + "" + pdfNumber.doubleOut(cx - rx) + " " +
							pdfNumber.doubleOut(cy - 21 * ry / 40f) + " " + pdfNumber.doubleOut(cx - 21 * rx / 40f) +
							" " + pdfNumber.doubleOut(cy - ry) + " " + pdfNumber.doubleOut(cx) + " " + pdfNumber.doubleOut(cy - ry) + " c\n";
				currentStream.write(str);
				doDrawing(di);
		}

		/**
		 * add an SVG rectangle to the current stream.
		 * If there are curved edges then these are rendered using bezier curves.
		 *
		 * @param x the x position of left edge
		 * @param y the y position of top edge
		 * @param w the width
		 * @param h the height
		 * @param rx the x radius curved edge
		 * @param ry the y radius curved edge
		 */
		protected void addRect(float x, float y, float w, float h,
													 float rx, float ry, DrawingInstruction di) {
				PDFNumber pdfNumber = new PDFNumber();
				String str = "";
				if (rx == 0.0 && ry == 0.0) {
						str = "" + pdfNumber.doubleOut(x) + " " + pdfNumber.doubleOut(y) + " " + pdfNumber.doubleOut(w) + " " + pdfNumber.doubleOut(h) + " re\n";
				} else {
						if (ry == 0.0)
								ry = rx;
						if (rx > w / 2.0f)
								rx = w / 2.0f;
						if (ry > h / 2.0f)
								ry = h / 2.0f;
						str = "" + pdfNumber.doubleOut(x + rx) + " " + pdfNumber.doubleOut(y) + " m\n";
						str += "" + pdfNumber.doubleOut(x + w - rx) + " " + pdfNumber.doubleOut(y) + " l\n";
						str += "" + pdfNumber.doubleOut(x + w - 19 * rx / 40) + " " + pdfNumber.doubleOut(y) + " " +
									 pdfNumber.doubleOut(x + w) + " " + pdfNumber.doubleOut(y + 19 * ry / 40) + " " +
									 pdfNumber.doubleOut(x + w) + " " + pdfNumber.doubleOut(y + ry) + " c\n";
						str += "" + pdfNumber.doubleOut(x + w) + " " + pdfNumber.doubleOut(y + h - ry) + " l\n";
						str += "" + pdfNumber.doubleOut(x + w) + " " + pdfNumber.doubleOut(y + h - 19 * ry / 40) + " " +
									 pdfNumber.doubleOut(x + w - 19 * rx / 40) + " " + pdfNumber.doubleOut(y + h) + " " +
									 pdfNumber.doubleOut(x + w - rx) + " " + pdfNumber.doubleOut(y + h) + " c\n";
						str += "" + pdfNumber.doubleOut(x + rx) + " " + pdfNumber.doubleOut(y + h) + " l\n";
						str += "" + pdfNumber.doubleOut(x + 19 * rx / 40) + " " + pdfNumber.doubleOut(y + h) + " " + pdfNumber.doubleOut(x) +
									 " " + pdfNumber.doubleOut(y + h - 19 * ry / 40) + " " + pdfNumber.doubleOut(x) + " " +
									 pdfNumber.doubleOut(y + h - ry) + " c\n";
						str += "" + pdfNumber.doubleOut(x) + " " + pdfNumber.doubleOut(y + ry) + " l\n";
						str += "" + pdfNumber.doubleOut(x) + " " + pdfNumber.doubleOut(y + 19 * ry / 40) + " " +
									 pdfNumber.doubleOut(x + 19 * rx / 40) + " " + pdfNumber.doubleOut(y) + " " + pdfNumber.doubleOut(x + rx) +
									 " " + pdfNumber.doubleOut(y) + " c\n";
				}
				currentStream.write(str);
				doDrawing(di);
		}

		/**
		 * Adds an SVG path to the current streem.
		 * An SVG path is made up of a list of drawing instructions that are rendered
		 * out in order.
		 * Arcs don't work.
		 */
		protected void addPath(SVGPathSegList points, int posx, int posy,
													 DrawingInstruction di) {
				PDFNumber pdfNumber = new PDFNumber();
				SVGPathSeg pathmoveto = null;
				float lastx = 0;
				float lasty = 0;
				float lastmovex = 0;
				float lastmovey = 0;
				float[] cxs;
				float tempx;
				float tempy;
				float lastcx = 0;
				float lastcy = 0;
				for (int count = 0; count < points.getNumberOfItems(); count++) {
						SVGPathSeg pc = (SVGPathSeg) points.getItem(count);
//						float[] vals = pc.getValues();
						switch (pc.getPathSegType()) {
								case SVGPathSeg.PATHSEG_MOVETO_ABS:
										pathmoveto = pc;
										SVGPathSegMovetoAbs mta = (SVGPathSegMovetoAbs)pc;
										lastx = mta.getX();
										lasty = mta.getY();
										currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " m\n");
										lastcx = 0;
										lastcy = 0;
										lastmovex = lastx;
										lastmovey = lasty;
										break;
								case SVGPathSeg.PATHSEG_MOVETO_REL:
										// the test cases seem to interprete this command differently
										// it seems if there is an 'm' then the current path is closed
										// then the point is move to a place relative to the point
										// after doing the close
										SVGPathSegMovetoRel mtr = (SVGPathSegMovetoRel)pc;
										if (pathmoveto == null) {
												lastx += mtr.getX();
												lasty += mtr.getY();
												pathmoveto = pc;
												currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " m\n");
										} else {
												lastx += mtr.getX();
												lasty += mtr.getY();
												pathmoveto = pc;
												currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " l\n");
										}
										lastmovex = lastx;
										lastmovey = lasty;
										lastcx = 0;
										lastcy = 0;
										break;
								case SVGPathSeg.PATHSEG_LINETO_ABS:
										SVGPathSegLinetoAbs lta = (SVGPathSegLinetoAbs)pc;
										lastx = lta.getX();
										lasty = lta.getY();
										currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " l\n");
										lastcx = 0;
										lastcy = 0;
										break;
								case SVGPathSeg.PATHSEG_LINETO_REL:
										SVGPathSegLinetoRel ltr = (SVGPathSegLinetoRel)pc;
										lastx += ltr.getX();
										lasty += ltr.getY();
										currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " l\n");
										lastcx = 0;
										lastcy = 0;
										break;
								case SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS:
										SVGPathSegLinetoVerticalAbs lva = (SVGPathSegLinetoVerticalAbs)pc;
										lasty = lva.getY();
										currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " l\n");
										lastcx = 0;
										lastcy = 0;
										break;
								case SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL:
										SVGPathSegLinetoVerticalRel lvr = (SVGPathSegLinetoVerticalRel)pc;
										lasty += lvr.getY();
										currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " l\n");
										lastcx = 0;
										lastcy = 0;
										break;
								case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS:
										SVGPathSegLinetoHorizontalAbs lha = (SVGPathSegLinetoHorizontalAbs)pc;
										lastx = lha.getX();
										currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " l\n");
										lastcx = 0;
										lastcy = 0;
										break;
								case SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL:
										SVGPathSegLinetoHorizontalRel lhr = (SVGPathSegLinetoHorizontalRel)pc;
										lastx += lhr.getX();
										currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " l\n");
										lastcx = 0;
										lastcy = 0;
										break;
								case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS:
										SVGPathSegCurvetoCubicAbs cca = (SVGPathSegCurvetoCubicAbs)pc;
										lastx = cca.getX2();
										lasty = cca.getY2();
										lastcx = cca.getX1();
										lastcy = cca.getY1();
										currentStream.write(pdfNumber.doubleOut(cca.getX()) + " " + pdfNumber.doubleOut(cca.getY()) +
																				" " + pdfNumber.doubleOut(lastcx) + " " + pdfNumber.doubleOut(lastcy) + " " +
																				pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " c\n");
										break;
								case SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL:
										SVGPathSegCurvetoCubicRel ccr = (SVGPathSegCurvetoCubicRel)pc;
										currentStream.write(pdfNumber.doubleOut(ccr.getX() + lastx) + " " +
																				pdfNumber.doubleOut(ccr.getY() + lasty) + " " +
																				pdfNumber.doubleOut(ccr.getX1() + lastx) + " " +
																				pdfNumber.doubleOut(ccr.getY1() + lasty) + " " +
																				pdfNumber.doubleOut(ccr.getX2() + lastx) + " " +
																				pdfNumber.doubleOut(ccr.getY2() + lasty) + " c\n");
										lastcx = ccr.getX1() + lastx;
										lastcy = ccr.getY1() + lasty;
										lastx += ccr.getX2();
										lasty += ccr.getY2();
										break;
/*								case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS:
										if (lastcx == 0) {
												lastcx = lastx;
										}
										if (lastcy == 0) {
												lastcy = lasty;
										}
										lastcx = lastx + (lastx - lastcx);
										lastcy = lasty + (lasty - lastcy);
										lastx = vals[2];
										lasty = vals[3];
										currentStream.write(pdfNumber.doubleOut(lastcx) + " " + pdfNumber.doubleOut(lastcy) + " " +
																				pdfNumber.doubleOut(vals[0]) + " " + pdfNumber.doubleOut(vals[1]) + " " +
																				pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " c\n");
										lastcx = vals[0];
										lastcy = vals[1];
										break;
								case SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL:
										if (lastcx == 0) {
												lastcx = lastx;
										}
										if (lastcy == 0) {
												lastcy = lasty;
										}
										lastcx = lastx + (lastx - lastcx);
										lastcy = lasty + (lasty - lastcy);
										currentStream.write(pdfNumber.doubleOut(lastcx) + " " + pdfNumber.doubleOut(lastcy) + " " +
																				pdfNumber.doubleOut(vals[0] + lastx) + " " +
																				pdfNumber.doubleOut(vals[1] + lasty) + " " +
																				pdfNumber.doubleOut(vals[2] + lastx) + " " +
																				pdfNumber.doubleOut(vals[3] + lasty) + " c\n");
										lastcx = (vals[0] + lastx);
										lastcy = (vals[1] + lasty);
										lastx += vals[2];
										lasty += vals[3];
										break;
								case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS:
										if (lastcx == 0) {
												lastcx = lastx;
										}
										if (lastcy == 0) {
												lastcy = lasty;
										}
										tempx = lastx;
										tempy = lasty;
										lastx = vals[2];
										lasty = vals[3];
										currentStream.write(pdfNumber.doubleOut(vals[0]) + " " + pdfNumber.doubleOut(vals[1]) + " " +
																				pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " y\n");
										cxs = calculateLastControl(tempx, tempy, lastx, lasty, -tempx + vals[0], -tempy + vals[1]);
										lastcx = cxs[0];
										lastcy = cxs[1];
										break;
								case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL:
										if (lastcx == 0) {
												lastcx = lastx;
										}
										if (lastcy == 0) {
												lastcy = lasty;
										}
										currentStream.write(pdfNumber.doubleOut(vals[0] + lastx) + " " + pdfNumber.doubleOut(vals[1] + lasty) + " " +
																				pdfNumber.doubleOut(vals[2] + lastx) + " " +
																				pdfNumber.doubleOut(vals[3] + lasty) + " y\n");
										cxs = calculateLastControl(lastx, lasty, lastx + vals[2], lasty + vals[3], vals[0], vals[1]);
										lastcx = cxs[0];
										lastcy = cxs[1];
										lastx += vals[2];
										lasty += vals[3];
										break;
								case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS:
										if (lastcx == 0) {
												lastcx = lastx;
										}
										if (lastcy == 0) {
												lastcy = lasty;
										}
										tempx = lastx;
										tempy = lasty;
										lastcx = lastx + (lastx - lastcx);
										lastcy = lasty + (lasty - lastcy);
										lastx = vals[0];
										lasty = vals[1];
										currentStream.write(pdfNumber.doubleOut(lastcx) + " " + pdfNumber.doubleOut(lastcy) + " " +
																				pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " y\n");
										cxs = calculateLastControl(tempx, tempy, lastx, lasty, -tempx + lastcx, -tempy + lastcy);
										lastcx = cxs[0];
										lastcy = cxs[1];
										break;
								case SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL:
										if (lastcx == 0) {
												lastcx = lastx;
										}
										if (lastcy == 0) {
												lastcy = lasty;
										}
										lastcx = lastx + (lastx - lastcx);
										lastcy = lasty + (lasty - lastcy);
										currentStream.write(pdfNumber.doubleOut(lastcx) + " " + pdfNumber.doubleOut(lastcy) + " " +
																				pdfNumber.doubleOut(vals[0] + lastx) + " " +
																				pdfNumber.doubleOut(vals[1] + lasty) + " y\n");
										cxs = calculateLastControl(lastx, lasty, lastx + vals[0], lasty + vals[1], -lastx + lastcx, -lasty + lastcy);
										lastcx = cxs[0];
										lastcy = cxs[1];
										lastx += vals[0];
										lasty += vals[1];
										break;
								case SVGPathSeg.PATHSEG_ARC_ABS:
										{
    									    addArc(lastx, lasty, vals[0], vals[1], vals[2], (vals[3] != 0.0), (vals[4] != 0.0),
	    								            vals[5], vals[6]);
											lastcx = 0; //??
											lastcy = 0; //??
											lastx = vals[5];
											lasty = vals[6];
										}
										break;
								case SVGPathSeg.PATHSEG_ARC_REL:
										{
    									    addArc(lastx, lasty, vals[0], vals[1], vals[2], (vals[3] != 0.0), (vals[4] != 0.0),
	    								            lastx + vals[5], lasty + vals[6]);
											lastcx = 0; //??
											lastcy = 0; //??
											lastx += vals[5];
											lasty += vals[6];
										}
										break;
								case SVGPathSeg.PATHSEG_CLOSEPATH:
										currentStream.write("h\n");
										pathmoveto = null;
										lastx = lastmovex;
										lasty = lastmovey;
										break;*/
						}
				}
				doDrawing(di);
		}

		/**
		 * Calculate the last control point for a bezier curve.
		 * This is used to find the last control point for a curve where
		 * only the first control point is specified.
		 * The control point is a reflection of the first control point
		 * which results in an even smooth curve. The curve is symmetrical.
		 */
		protected float[] calculateLastControl(float x1, float y1, float x2, float y2, float relx, float rely)
		{
				float vals[] = new float[2];
				relx = -relx;
				rely = -rely;
				float dist = (float)Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
				float costheta = (float)(x2 - x1) / dist;
				float sinetheta = (float)(y2 - y1) / dist;
				float temp = relx;
				relx = relx * costheta + rely * sinetheta;
				rely = -temp * sinetheta + rely * costheta;
				relx = -relx;
				temp = relx;
				relx = relx * costheta - rely * sinetheta;
				rely = temp * sinetheta + rely * costheta;
				vals[0] = x2 - relx;
				vals[1] = y2 - rely;
				return vals;
		}

        protected void addArc(double startx, double starty, double rx, double ry,
                                   double angle,
                                   boolean largeArcFlag,
                                   boolean sweepFlag,
                                   double x, double y)
        {
			PDFNumber pdfNumber = new PDFNumber();
            if((startx == x) && (starty == y)) {
                return;
            }
            // Ensure radii are valid
            if (rx == 0 || ry == 0) {
                currentStream.write(pdfNumber.doubleOut(x) + " " + pdfNumber.doubleOut(y) + " l\n");
                return;
            }
            if(rx < 0)
                rx = -rx;
            if(ry < 0)
                ry = -ry;
            // Convert angle from degrees to radians
            angle = toRadians(angle % 360.0);

            double x0 = startx;
            double y0 = starty;
            // Compute the half distance between the current and the final point
            double dx2 = (x0 - x) / 2.0;
            double dy2 = (y0 - y) / 2.0;

            double cosAngle = Math.cos(angle);
            double sinAngle = Math.sin(angle);

            // Step 1 : Compute (x1, y1)
            double x1 = (cosAngle * dx2 + sinAngle * dy2);
            double y1 = (-sinAngle * dx2 + cosAngle * dy2);

            double Prx = rx * rx;
            double Pry = ry * ry;
            double Px1 = x1 * x1;
            double Py1 = y1 * y1;
            // check that radii are large enough
            double radiiCheck = Px1/Prx + Py1/Pry;
            if (radiiCheck > 1) {
                rx = Math.sqrt(radiiCheck) * rx;
                ry = Math.sqrt(radiiCheck) * ry;
                Prx = rx * rx;
                Pry = ry * ry;
            }

            // Step 2 : Compute (cx1, cy1)
            double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
            double sq = ((Prx*Pry)-(Prx*Py1)-(Pry*Px1)) / ((Prx*Py1)+(Pry*Px1));
            sq = (sq < 0) ? 0 : sq;
            double coef = (sign * Math.sqrt(sq));
            double cx1 = coef * ((rx * y1) / ry);
            double cy1 = coef * -((ry * x1) / rx);

            // Step 3 : Compute (cx, cy) from (cx1, cy1)
            double sx2 = (x0 + x) / 2.0;
            double sy2 = (y0 + y) / 2.0;
            double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
            double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

            // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
            double ux = (x1 - cx1) / rx;
            double uy = (y1 - cy1) / ry;
            double vx = (-x1 - cx1) / rx;
            double vy = (-y1 - cy1) / ry;
            double p, n;
            // Compute the angle start
            n = Math.sqrt((ux * ux) + (uy * uy));
            p = ux; // (1 * ux) + (0 * uy)
            sign = (uy < 0) ? -1d : 1d;
            double angleStart = toDegrees(sign * Math.acos(p / n));

            // Compute the angle extent
            n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
            p = ux * vx + uy * vy;
            sign = (ux * vy - uy * vx < 0) ? -1d : 1d;
            double angleExtent = toDegrees(sign * Math.acos(p / n));
            if(!sweepFlag && angleExtent > 0) {
                angleExtent -= 360f;
            } else if (sweepFlag && angleExtent < 0) {
                angleExtent += 360f;
            }
            angleExtent %= 360f;
            angleStart %= 360f;

            // arc attempt with lines
//            System.out.println("start:" + angleStart + " : " + angleExtent + " sweep:" + sweepFlag);
            double newx = startx;
            double newy = starty;
            double currentAngle = angleStart + angle;
            boolean wrap = (sweepFlag ? angleStart > angleExtent : angleStart < angleExtent);
            boolean wrapped = false;

/*            newx = Math.cos(toRadians(angle)) * rx * Math.cos(toRadians(currentAngle)) - Math.sin(toRadians(angle)) * ry * Math.sin(toRadians(currentAngle)) + cx;
            newy = Math.sin(toRadians(angle)) * rx * Math.cos(toRadians(currentAngle)) + Math.cos(toRadians(angle)) * ry * Math.sin(toRadians(currentAngle)) + cy;
            System.out.println("ox:" + startx + " oy: " + starty + " nx:" + newx + " ny:" + newy);
            newx = Math.cos(toRadians(angle)) * rx * Math.cos(toRadians(angleStart + angleExtent)) - Math.sin(toRadians(angle)) * ry * Math.sin(toRadians(angleStart + angleExtent)) + cx;
            newy = Math.sin(toRadians(angle)) * rx * Math.cos(toRadians(angleStart + angleExtent)) + Math.cos(toRadians(angle)) * ry * Math.sin(toRadians(angleStart + angleExtent)) + cy;
            System.out.println("ox:" + x + " oy: " + y + " nx:" + newx + " ny:" + newy);*/

            while(true) {
                newx = Math.cos(toRadians(angle)) * rx * Math.cos(toRadians(currentAngle)) - Math.sin(toRadians(angle)) * ry * Math.sin(toRadians(currentAngle)) + cx;
                newy = Math.sin(toRadians(angle)) * rx * Math.cos(toRadians(currentAngle)) + Math.cos(toRadians(angle)) * ry * Math.sin(toRadians(currentAngle)) + cy;
                currentStream.write(pdfNumber.doubleOut(newx) + " " + pdfNumber.doubleOut(newy) + " l\n");
                currentAngle = (currentAngle + (sweepFlag ? 1 : -1) * 5.0);
                if((sweepFlag && currentAngle > (angleStart + angleExtent)) || (!sweepFlag && currentAngle < (angleStart + angleExtent))) {
                    break;
                }
            }
            currentStream.write(pdfNumber.doubleOut(x) + " " + pdfNumber.doubleOut(y) + " l\n");
        }

		/**
		 * Adds an SVG polyline or polygon.
		 * A polygon is merely a closed polyline.
		 * This is made up from a set of points that straight lines are drawn between.
		 */
		protected void addPolyline(SVGPointList points, DrawingInstruction di,
															 boolean close) {
				PDFNumber pdfNumber = new PDFNumber();
				SVGPoint pc;
				float lastx = 0;
				float lasty = 0;
				if (points.getNumberOfItems() > 0) {
						pc = (SVGPoint) points.getItem(0);
						lastx = pc.getX();
						lasty = pc.getY();
						currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " m\n");
				}
				for(int count = 1; count < points.getNumberOfItems(); count++) {
						pc = (SVGPoint) points.getItem(count);
						lastx = pc.getX();
						lasty = pc.getY();
						currentStream.write(pdfNumber.doubleOut(lastx) + " " + pdfNumber.doubleOut(lasty) + " l\n");
				}
				if (close)
						currentStream.write("h\n");
				doDrawing(di);
		}

		/**
		 * Writes the drawing instruction out to the current stream
		 * depending on what type of drawing is required.
		 */
		protected void doDrawing(DrawingInstruction di) {
				if (di == null) {
						currentStream.write("S\n");
				} else {
						if (di.fill) {
								if (di.stroke) {
										if (!di.nonzero)
												currentStream.write("B*\n");
										else
												currentStream.write("B\n");
								} else {
										if (!di.nonzero)
												currentStream.write("f*\n");
										else
												currentStream.write("f\n");
								}
						} else {
								//				if(di.stroke)
								currentStream.write("S\n");
						}
				}
		}

		/**
		 * Renders an svg image to the current stream.
		 * This uses the FopImageFactory to load the image and then renders it.
		 */
		public void renderImage(String href, float x, float y, float width,
														float height) {
				try {
						if (href.indexOf(":") == -1) {
								href = "file:" + href;
						}
						FopImage img = FopImageFactory.Make(href);
						PDFNumber pdfNumber = new PDFNumber();
						if (img instanceof SVGImage) {
								SVGSVGElement svg =
									((SVGImage) img).getSVGDocument().getRootElement();
								currentStream.write("q\n" + pdfNumber.doubleOut(width /
																		svg.getWidth().getBaseVal().getValue()) + " 0 0 " +
																		pdfNumber.doubleOut(height /
																		svg.getHeight().getBaseVal().getValue()) + " 0 0 cm\n");
								renderSVG(svg, (int) x * 1000, (int) y * 1000);
								currentStream.write("Q\n");
								//				renderSVG(svg);
						} else if (img != null) {
								int xObjectNum = this.pdfDoc.addImage(img);
								currentStream.write("q\n1 0 0 -1 0 " +
																		pdfNumber.doubleOut(2 * y + height) + " cm\n" + pdfNumber.doubleOut(width) + " 0 0 " +
																		pdfNumber.doubleOut(height) + " " + pdfNumber.doubleOut(x) + " " + pdfNumber.doubleOut(y) + " cm\n" + "/Im" +
																		xObjectNum + " Do\nQ\n");
								//				img.close();
						}
				} catch (Exception e) {
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
		 * Handles the construction of an SVG gradient.
		 * This gets the gradient element and creates the pdf info
		 * in the pdf document.
		 * The type of gradient is determined by what class the gradient element is.
		 */
		protected void handleGradient(String sp, DrawingInstruction di,
																	boolean fill, SVGElement area) {
				// should be a url to a gradient
				String url = (String) sp;
				if (url.startsWith("url(")) {
						String address;
						int b1 = url.indexOf("(");
						int b2 = url.indexOf(")");
						address = url.substring(b1 + 1, b2);
						SVGElement gi = null;
						gi = locateDef(address, area);
						if (gi instanceof SVGLinearGradientElement) {
								SVGLinearGradientElement linear =
									(SVGLinearGradientElement) gi;
								handleLinearGradient(linear, di, fill, area);
						} else if (gi instanceof SVGRadialGradientElement) {
								SVGRadialGradientElement radial =
									(SVGRadialGradientElement) gi;
								handleRadialGradient(radial, di, fill, area);
						} else if (gi instanceof SVGPatternElement) {
								SVGPatternElement pattern = (SVGPatternElement) gi;
								handlePattern(pattern, di, fill, area);
						} else {
								MessageHandler.errorln("WARNING Invalid fill reference :" +
																	 gi + ":" + address);
						}
				}
		}

		protected void handlePattern(SVGPatternElement pattern,
																 DrawingInstruction di, boolean fill, SVGElement area) {
				SVGAnimatedLength x, y, width, height;
				short pattUnits = SVGUnitTypes.SVG_UNIT_TYPE_UNKNOWN;
				NodeList stops = null;
				x = pattern.getX();
				y = pattern.getY();
				width = pattern.getWidth();
				height = pattern.getHeight();
				NodeList nl = pattern.getChildNodes();
				SVGPatternElement ref = (SVGPatternElement) locateDef(
																	pattern.getHref().getBaseVal(), pattern);
/*				while (ref != null) {
						if (x == null) {
								x = ref.getX();
								pattUnits = ref.getPatternUnits().getBaseVal();
						}
						if (y == null) {
								y = ref.getY();
						}
						if (width == null) {
								width = ref.getWidth();
						}
						if (height == null) {
								height = ref.getHeight();
						}
						if (nl.getLength() == 0) {
								nl = ref.getChildNodes();
						}
						ref = (SVGPatternElement) locateDef(
										ref.getHref().getBaseVal(), ref);
				}
				if (x == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 0);
						x = new SVGAnimatedLengthImpl(length);
				}
				if (y == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 0);
						y = new SVGAnimatedLengthImpl(length);
				}
				if (width == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 1);
						width = new SVGAnimatedLengthImpl(length);
				}
				if (height == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 1);
						height = new SVGAnimatedLengthImpl(length);
				}*/

				StringWriter realStream = currentStream;
				currentStream = new StringWriter();

				currentStream.write("q\n");
				// this makes the pattern the right way up, since it is outside the original
				// transform around the whole svg document
				currentStream.write("1 0 0 -1 0 " +
														height.getBaseVal().getValue() + " cm\n");
				for (int count = 0; count < nl.getLength(); count++) {
						Node n = nl.item(count);
						if (n instanceof SVGElement) {
								renderElement((SVGElement) n, 0, 0);
						}
				}
				currentStream.write("Q\n");

				double xval = x.getBaseVal().getValue() + currentXPosition / 1000f;
				double yval = -y.getBaseVal().getValue() + currentYPosition / 1000f;
/*				if (area instanceof SVGLocatable) {
						SVGRect bbox = ((SVGLocatable) area).getBBox();
						if (bbox != null) {
								//		        xval += bbox.getX();
								//		        yval -= bbox.getY();
						}
				}*/
				double widthval = width.getBaseVal().getValue();
				double heightval = height.getBaseVal().getValue();
				Vector bbox = new Vector();
				bbox.addElement(new Double(0));
				bbox.addElement(new Double(0));
				bbox.addElement(new Double(widthval));
				bbox.addElement(new Double(heightval));
				Vector translate = new Vector();
				// combine with pattern transform
				translate.addElement(new Double(1));
				translate.addElement(new Double(0));
				translate.addElement(new Double(0));
				translate.addElement(new Double(1));
				translate.addElement(new Double(xval));
				translate.addElement(new Double(yval));
				// need to handle PDFResources
				PDFPattern myPat =
					this.pdfDoc.makePattern(1, null, 1, 1, bbox, widthval,
																	heightval, translate, null, currentStream.getBuffer());

				currentStream = realStream;
				currentStream.write(myPat.getColorSpaceOut(fill));
				if (fill)
						di.fill = true;
				else
						di.stroke = true;
		}

		protected void handleLinearGradient(
			SVGLinearGradientElement linear, DrawingInstruction di,
			boolean fill, SVGElement area) {
				// first get all the gradient values
				// if values not present follow the href
				// the gradient units will be where the vals are specified
				// the spread method will be where there are stop elements
				SVGAnimatedLength ax1, ax2, ay1, ay2;
				short spread = SVGGradientElement.SVG_SPREADMETHOD_UNKNOWN;
				short gradUnits = SVGUnitTypes.SVG_UNIT_TYPE_UNKNOWN;
				NodeList stops = null;
				ax1 = linear.getX1();
				ax2 = linear.getX2();
				ay1 = linear.getY1();
				ay2 = linear.getY2();
				stops = linear.getChildNodes();
				SVGLinearGradientElement ref = (SVGLinearGradientElement) locateDef(
																				 linear.getHref().getBaseVal(), linear);
/*				while (ref != null) {
						if (ax1 == null) {
								ax1 = ref.getX1();
								gradUnits = ref.getGradientUnits().getBaseVal();
						}
						if (ax2 == null) {
								ax2 = ref.getX2();
						}
						if (ay1 == null) {
								ay1 = ref.getY1();
						}
						if (ay2 == null) {
								ay2 = ref.getY2();
						}
						if (stops.getLength() == 0) {
								stops = ref.getChildNodes();
						}
						ref = (SVGLinearGradientElement) locateDef(
										ref.getHref().getBaseVal(), ref);
				}
				if (ax1 == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 0);
						ax1 = new SVGAnimatedLengthImpl(length);
				}
				if (ax2 == null) {
						// if x2 is not specified then it should be 100%
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 1);
						ax2 = new SVGAnimatedLengthImpl(length);
				}
				if (ay1 == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 0);
						ay1 = new SVGAnimatedLengthImpl(length);
				}
				if (ay2 == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 0);
						ay2 = new SVGAnimatedLengthImpl(length);
				}*/
				SVGAnimatedTransformList an = linear.getGradientTransform();
				SVGMatrix transform = null;
				if(an != null)
						transform = an.getBaseVal().consolidate().getMatrix();
				Vector theCoords = null;
				if (gradUnits == SVGUnitTypes.SVG_UNIT_TYPE_UNKNOWN)
						gradUnits = linear.getGradientUnits().getBaseVal();
				// spread: pad (normal), reflect, repeat
				spread = linear.getSpreadMethod().getBaseVal();
				if (gradUnits == SVGUnitTypes.SVG_UNIT_TYPE_USERSPACEONUSE) {
						if (area instanceof SVGTransformable) {
								SVGTransformable tf = (SVGTransformable) area;
								double x1, y1, x2, y2;
								x1 = ax1.getBaseVal().getValue();
								y1 = -ay1.getBaseVal().getValue();
								x2 = ax2.getBaseVal().getValue();
								y2 = -ay2.getBaseVal().getValue();
								SVGMatrix matrix = tf.getScreenCTM();
								if(transform != null)
										matrix = matrix.multiply(transform);
								double oldx = x1;
								x1 = matrix.getA() * x1 + matrix.getC() * y1 +
										 matrix.getE();
								y1 = matrix.getB() * oldx + matrix.getD() * y1 -
										 matrix.getF();
								oldx = x2;
								x2 = matrix.getA() * x2 + matrix.getC() * y2 +
										 matrix.getE();
								y2 = matrix.getB() * oldx + matrix.getD() * y2 -
										 matrix.getF();
								theCoords = new Vector();
								if (spread == SVGGradientElement.SVG_SPREADMETHOD_REFLECT) {
								} else if (spread ==
										SVGGradientElement.SVG_SPREADMETHOD_REFLECT) {
								} else {
										theCoords.addElement(
											new Double(currentXPosition / 1000f + x1));
										theCoords.addElement(
											new Double(currentYPosition / 1000f + y1));
										theCoords.addElement(
											new Double(currentXPosition / 1000f + x2));
										theCoords.addElement(
											new Double(currentYPosition / 1000f + y2));
								}
						}
/*				} else if (area instanceof SVGLocatable) {
						SVGRect rect = ((SVGLocatable) area).getBBox();
						if (rect != null) {
								theCoords = new Vector();
								SVGLength val;
								val = ax1.getBaseVal();
								if (val.getUnitType() ==
												SVGLength.SVG_LENGTHTYPE_PERCENTAGE || gradUnits ==
												SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
										theCoords.addElement(
											new Double(currentXPosition / 1000f +
																 rect.getX() +
																 val.getValue() * rect.getWidth()));
								} else {
										theCoords.addElement(
											new Double(currentXPosition / 1000f +
																 val.getValue()));
								}
								val = ay1.getBaseVal();
								if (val.getUnitType() ==
												SVGLength.SVG_LENGTHTYPE_PERCENTAGE || gradUnits ==
												SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
										theCoords.addElement(
											new Double(currentYPosition / 1000f -
																 rect.getY() -
																 val.getValue() * rect.getHeight()));
								} else {
										theCoords.addElement(
											new Double(currentYPosition / 1000f -
																 val.getValue()));
								}
								val = ax2.getBaseVal();
								if (val.getUnitType() ==
												SVGLength.SVG_LENGTHTYPE_PERCENTAGE || gradUnits ==
												SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
										theCoords.addElement(
											new Double(currentXPosition / 1000f +
																 rect.getX() +
																 val.getValue() * rect.getWidth()));
								} else {
										theCoords.addElement(
											new Double(currentXPosition / 1000f +
																 val.getValue()));
								}
								val = ay2.getBaseVal();
								if (val.getUnitType() ==
												SVGLength.SVG_LENGTHTYPE_PERCENTAGE || gradUnits ==
												SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
										theCoords.addElement(
											new Double(currentYPosition / 1000f -
																 rect.getY() -
																 val.getValue() * rect.getHeight()));
								} else {
										theCoords.addElement(
											new Double(currentYPosition / 1000f -
																 val.getValue()));
								}
						}*/
				}
				if (theCoords == null) {
						theCoords = new Vector();
						theCoords.addElement( new Double(currentXPosition / 1000f +
																						 ax1.getBaseVal().getValue()));
						theCoords.addElement( new Double(currentYPosition / 1000f -
																						 ay1.getBaseVal().getValue()));
						theCoords.addElement( new Double(currentXPosition / 1000f +
																						 ax2.getBaseVal().getValue()));
						theCoords.addElement( new Double(currentYPosition / 1000f -
																						 ay2.getBaseVal().getValue()));
				}

				Vector theExtend = new Vector();
				theExtend.addElement(new Boolean(true));
				theExtend.addElement(new Boolean(true));

				Vector theDomain = new Vector();
				theDomain.addElement(new Double(0));
				theDomain.addElement(new Double(1));

				Vector theEncode = new Vector();
				theEncode.addElement(new Double(0));
				theEncode.addElement(new Double(1));
				theEncode.addElement(new Double(0));
				theEncode.addElement(new Double(1));

				Vector theBounds = new Vector();
				theBounds.addElement(new Double(0));
				theBounds.addElement(new Double(1));

				Vector theFunctions = new Vector();

				NodeList nl = stops;
				Vector someColors = new Vector();
				float lastoffset = 0;
				Vector lastVector = null;
				SVGStopElement stop;
				if (nl.getLength() == 0) {
						// the color should be "none"
						if (fill)
								di.fill = false;
						else
								di.stroke = false;
						return;
				} else if (nl.getLength() == 1) {
						stop = (SVGStopElement) nl.item(0);
						CSSValue cv = stop.getPresentationAttribute("stop-color");
						if (cv == null) {
								// maybe using color
								cv = stop.getPresentationAttribute("color");
						}
						if (cv == null) {
								// problems
								MessageHandler.errorln("no stop-color or color in stop element");
								return;
						}
						PDFColor color = new PDFColor(0, 0, 0);
						if (cv != null &&
										cv.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) cv).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_RGBCOLOR) {
										RGBColor col =
											((CSSPrimitiveValue) cv).getRGBColorValue();
										CSSPrimitiveValue val;
										val = col.getRed();
										float red = val.getFloatValue(
																	CSSPrimitiveValue.CSS_NUMBER);
										val = col.getGreen();
										float green = val.getFloatValue(
																		CSSPrimitiveValue.CSS_NUMBER);
										val = col.getBlue();
										float blue = val.getFloatValue(
																	 CSSPrimitiveValue.CSS_NUMBER);
										color = new PDFColor(red, green, blue);
								}
						}
						currentStream.write(color.getColorSpaceOut(fill));
						if (fill)
								di.fill = true;
						else
								di.stroke = true;
						return;
				}
				for (int count = 0; count < nl.getLength(); count++) {
						stop = (SVGStopElement) nl.item(count);
						CSSValue cv = stop.getPresentationAttribute("stop-color");
						if (cv == null) {
								// maybe using color
								cv = stop.getPresentationAttribute("color");
						}
						if (cv == null) {
								// problems
								MessageHandler.errorln("no stop-color or color in stop element");
								continue;
						}
						PDFColor color = new PDFColor(0, 0, 0);
						if (cv != null &&
										cv.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) cv).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_RGBCOLOR) {
										RGBColor col =
											((CSSPrimitiveValue) cv).getRGBColorValue();
										CSSPrimitiveValue val;
										val = col.getRed();
										float red = val.getFloatValue(
																	CSSPrimitiveValue.CSS_NUMBER);
										val = col.getGreen();
										float green = val.getFloatValue(
																		CSSPrimitiveValue.CSS_NUMBER);
										val = col.getBlue();
										float blue = val.getFloatValue(
																	 CSSPrimitiveValue.CSS_NUMBER);
										color = new PDFColor(red, green, blue);
										currentColour = color;
								}
						}
						float offset = stop.getOffset().getBaseVal();
						Vector colVector = color.getVector();
						// create bounds from last to offset
						if (lastVector != null) {
								Vector theCzero = lastVector;
								Vector theCone = colVector;
								PDFFunction myfunc =
									this.pdfDoc.makeFunction(2, theDomain, null,
																					 theCzero, theCone, 1.0);
								theFunctions.addElement(myfunc);
						}
						lastoffset = offset;
						lastVector = colVector;
						someColors.addElement(color);
				}
				ColorSpace aColorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
				/*				PDFFunction myfunky = this.pdfDoc.makeFunction(3,
							theDomain, null,
							theFunctions, null,
							theEncode);
						PDFShading myShad = null;
						myShad = this.pdfDoc.makeShading(
							2, aColorSpace,
							null, null, false,
							theCoords, null, myfunky,theExtend);
						PDFPattern myPat = this.pdfDoc.makePattern(2, myShad, null, null, null);*/
				PDFPattern myPat = this.pdfDoc.createGradient(false, aColorSpace,
													 someColors, null, theCoords);
				currentStream.write(myPat.getColorSpaceOut(fill));
				if (fill)
						di.fill = true;
				else
						di.stroke = true;
		}

		protected void handleRadialGradient(
			SVGRadialGradientElement radial, DrawingInstruction di,
			boolean fill, SVGElement area) {
				// first get all the gradient values
				// if values not present follow the href
				// the gradient units will be where the vals are specified
				SVGAnimatedLength acx, acy, ar, afx, afy;
				short gradUnits = radial.getGradientUnits().getBaseVal();
				NodeList stops = null;
				acx = radial.getCx();
				acy = radial.getCy();
				ar = radial.getR();
				afx = radial.getFx();
				afy = radial.getFy();
				stops = radial.getChildNodes();
				SVGRadialGradientElement ref = (SVGRadialGradientElement) locateDef(
																				 radial.getHref().getBaseVal(), radial);
/*				while (ref != null) {
						if (acx == null) {
								acx = ref.getCx();
								gradUnits = ref.getGradientUnits().getBaseVal();
						}
						if (acy == null) {
								acy = ref.getCy();
						}
						if (ar == null) {
								ar = ref.getR();
						}
						if (afx == null) {
								afx = ref.getFx();
						}
						if (afy == null) {
								afy = ref.getFy();
						}
						if (stops.getLength() == 0) {
								stops = ref.getChildNodes();
						}
						ref = (SVGRadialGradientElement) locateDef(
										ref.getHref().getBaseVal(), ref);
				}
				if (acx == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 0.5f);
						acx = new SVGAnimatedLengthImpl(length);
				}
				if (acy == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 0.5f);
						acy = new SVGAnimatedLengthImpl(length);
				}
				if (ar == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 1);
						ar = new SVGAnimatedLengthImpl(length);
				}
				if (afx == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 0.5f);
						afx = new SVGAnimatedLengthImpl(length);
				}
				if (afy == null) {
						SVGLength length = new SVGLengthImpl();
						length.newValueSpecifiedUnits(
							SVGLength.SVG_LENGTHTYPE_PERCENTAGE, 0.5f);
						afy = new SVGAnimatedLengthImpl(length);
				}*/
				ColorSpace aColorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
				org.w3c.dom.NodeList nl = stops;
				SVGStopElement stop;
				if (nl.getLength() == 0) {
						// the color should be "none"
						if (fill)
								di.fill = false;
						else
								di.stroke = false;
						return;
				} else if (nl.getLength() == 1) {
						stop = (SVGStopElement) nl.item(0);
						CSSValue cv = stop.getPresentationAttribute("stop-color");
						if (cv == null) {
								// maybe using color
								cv = stop.getPresentationAttribute("color");
						}
						if (cv == null) {
								// problems
								MessageHandler.errorln("no stop-color or color in stop element");
								return;
						}
						PDFColor color = new PDFColor(0, 0, 0);
						if (cv != null &&
										cv.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) cv).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_RGBCOLOR) {
										RGBColor col =
											((CSSPrimitiveValue) cv).getRGBColorValue();
										CSSPrimitiveValue val;
										val = col.getRed();
										float red = val.getFloatValue(
																	CSSPrimitiveValue.CSS_NUMBER);
										val = col.getGreen();
										float green = val.getFloatValue(
																		CSSPrimitiveValue.CSS_NUMBER);
										val = col.getBlue();
										float blue = val.getFloatValue(
																	 CSSPrimitiveValue.CSS_NUMBER);
										color = new PDFColor(red, green, blue);
								}
						}
						currentStream.write(color.getColorSpaceOut(fill));
						if (fill)
								di.fill = true;
						else
								di.stroke = true;
						return;
				}
				Hashtable table = null;
				Vector someColors = new Vector();
				Vector theCoords = null;
				Vector theBounds = new Vector();
				// the coords should be relative to the current object
				// check value types, eg. %
				if (gradUnits == SVGUnitTypes.SVG_UNIT_TYPE_USERSPACEONUSE) {
						if (area instanceof SVGTransformable) {
								SVGTransformable tf = (SVGTransformable) area;
								double x1, y1, x2, y2;
								x1 = acx.getBaseVal().getValue();
								y1 = -acy.getBaseVal().getValue();
								x2 = afx.getBaseVal().getValue();
								y2 = -afy.getBaseVal().getValue();
								SVGMatrix matrix = tf.getScreenCTM();
								double oldx = x1;
								x1 = matrix.getA() * x1 + matrix.getB() * y1 +
										 matrix.getE();
								y1 = matrix.getC() * oldx + matrix.getD() * y1 +
										 matrix.getF();
								oldx = x2;
								x2 = matrix.getA() * x2 + matrix.getB() * y2 +
										 matrix.getE();
								y2 = matrix.getC() * oldx + matrix.getD() * y2 +
										 matrix.getF();
								theCoords = new Vector();
								//				if(spread == SVGGradientElement.SVG_SPREADMETHOD_REFLECT) {
								//				} else if(spread== SVGGradientElement.SVG_SPREADMETHOD_REFLECT) {
								//				} else {
								theCoords.addElement(
									new Double(currentXPosition / 1000f + x1));
								// the y val needs to be adjust by 2 * R * rotation
								// depending on if this value is from an x or y coord
								// before transformation
								theCoords.addElement(
									new Double(currentYPosition / 1000f - y1 +
														 (matrix.getC() - matrix.getD()) * 2 *
														 ar.getBaseVal().getValue()));
								theCoords.addElement(new Double(0));
								theCoords.addElement(
									new Double(currentXPosition / 1000f + x2));
								theCoords.addElement(
									new Double(currentYPosition / 1000f - y2 +
														 (matrix.getC() - matrix.getD()) * 2 *
														 ar.getBaseVal().getValue()));
								theCoords.addElement(
									new Double(ar.getBaseVal().getValue()));
								//				}
						}
/*				} else if (gradUnits ==
						SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX &&
						area instanceof SVGLocatable) {
						SVGRect rect = ((SVGLocatable) area).getBBox();
						if (rect != null) {
								theCoords = new Vector();
								SVGLength val;
								val = acx.getBaseVal();
								if (val.getUnitType() ==
												SVGLength.SVG_LENGTHTYPE_PERCENTAGE || gradUnits ==
												SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
										theCoords.addElement(
											new Double(currentXPosition / 1000f +
																 rect.getX() +
																 val.getValue() * rect.getWidth()));
								} else {
										theCoords.addElement(
											new Double(currentXPosition / 1000f +
																 val.getValue()));
								}
								val = acy.getBaseVal();
								if (val.getUnitType() ==
												SVGLength.SVG_LENGTHTYPE_PERCENTAGE || gradUnits ==
												SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
										theCoords.addElement(
											new Double(currentYPosition / 1000f -
																 rect.getY() -
																 val.getValue() * rect.getHeight()));
								} else {
										theCoords.addElement(
											new Double(currentYPosition / 1000f -
																 val.getValue()));
								}
								theCoords.addElement(new Double(0));
								val = afx.getBaseVal();
								if (val.getUnitType() ==
												SVGLength.SVG_LENGTHTYPE_PERCENTAGE || gradUnits ==
												SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
										theCoords.addElement(
											new Double(currentXPosition / 1000f +
																 rect.getX() +
																 val.getValue() * rect.getWidth()));
								} else {
										theCoords.addElement(
											new Double(currentXPosition / 1000f +
																 val.getValue()));
								}
								val = afy.getBaseVal();
								if (val.getUnitType() ==
												SVGLength.SVG_LENGTHTYPE_PERCENTAGE || gradUnits ==
												SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
										theCoords.addElement(
											new Double(currentYPosition / 1000f -
																 rect.getY() -
																 val.getValue() * rect.getHeight()));
								} else {
										theCoords.addElement(
											new Double(currentYPosition / 1000f -
																 val.getValue()));
								}
								val = ar.getBaseVal();
								if (val.getUnitType() ==
												SVGLength.SVG_LENGTHTYPE_PERCENTAGE || gradUnits ==
												SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
										theCoords.addElement(
											new Double(val.getValue() * rect.getHeight()));
								} else {
										theCoords.addElement(new Double(val.getValue()));
								}
						}*/
				}
				if (theCoords == null) {
						// percentage values are expressed according to the viewport.
/*						SVGElement vp =
							((GraphicElement) area).getNearestViewportElement();
						if (area instanceof SVGLocatable) {
								SVGRect rect = ((SVGLocatable) area).getBBox();
								if (rect != null) {
										theCoords = new Vector();
										SVGLength val = acx.getBaseVal();
										if (val.getUnitType() ==
														SVGLength.SVG_LENGTHTYPE_PERCENTAGE ||
														gradUnits ==
														SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
												theCoords.addElement(
													new Double(currentXPosition / 1000f +
																		 rect.getX() +
																		 val.getValue() * rect.getWidth()));
										} else {
												theCoords.addElement(
													new Double(currentXPosition / 1000f +
																		 val.getValue()));
										}
										val = acy.getBaseVal();
										if (val.getUnitType() ==
														SVGLength.SVG_LENGTHTYPE_PERCENTAGE ||
														gradUnits ==
														SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
												theCoords.addElement(
													new Double(currentYPosition / 1000f -
																		 rect.getY() -
																		 val.getValue() * rect.getHeight()));
										} else {
												theCoords.addElement(
													new Double(currentYPosition / 1000f -
																		 val.getValue()));
										}
										theCoords.addElement(new Double(0));
										val = afx.getBaseVal();
										if (val.getUnitType() ==
														SVGLength.SVG_LENGTHTYPE_PERCENTAGE ||
														gradUnits ==
														SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
												theCoords.addElement(
													new Double(currentXPosition / 1000f +
																		 rect.getX() +
																		 val.getValue() * rect.getWidth()));
										} else {
												theCoords.addElement(
													new Double(currentXPosition / 1000f +
																		 val.getValue()));
										}
										val = afy.getBaseVal();
										if (val.getUnitType() ==
														SVGLength.SVG_LENGTHTYPE_PERCENTAGE ||
														gradUnits ==
														SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
												theCoords.addElement(
													new Double(currentYPosition / 1000f -
																		 rect.getY() -
																		 val.getValue() * rect.getHeight()));
										} else {
												theCoords.addElement(
													new Double(currentYPosition / 1000f -
																		 val.getValue()));
										}
										val = ar.getBaseVal();
										if (val.getUnitType() ==
														SVGLength.SVG_LENGTHTYPE_PERCENTAGE ||
														gradUnits ==
														SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX) {
												theCoords.addElement( new Double(val.getValue() *
																												 rect.getHeight()));
										} else {
												theCoords.addElement(new Double(val.getValue()));
										}
								}
						}*/
				}
				if (theCoords == null) {
						theCoords = new Vector();
						theCoords.addElement( new Double(currentXPosition / 1000f +
																						 acx.getBaseVal().getValue()));
						theCoords.addElement( new Double(currentYPosition / 1000f -
																						 acy.getBaseVal().getValue()));
						theCoords.addElement(new Double(0));
						theCoords.addElement( new Double(currentXPosition / 1000f +
																						 afx.getBaseVal().getValue())); // Fx
						theCoords.addElement(
							new Double(currentYPosition / 1000f -
												 afy.getBaseVal().getValue())); // Fy
						theCoords.addElement(
							new Double(ar.getBaseVal().getValue()));
				}
				float lastoffset = 0;
				for (int count = 0; count < nl.getLength(); count++) {
						stop = (SVGStopElement) nl.item(count);
						CSSValue cv = stop.getPresentationAttribute("stop-color");
						if (cv == null) {
								// maybe using color
								cv = stop.getPresentationAttribute("color");
						}
						if (cv == null) {
								// problems
								MessageHandler.errorln("no stop-color or color in stop element");
								continue;
						}
						PDFColor color = new PDFColor(0, 0, 0);
						if (cv != null &&
										cv.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) cv).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_RGBCOLOR) {
										RGBColor col =
											((CSSPrimitiveValue) cv).getRGBColorValue();
										CSSPrimitiveValue val;
										val = col.getRed();
										float red = val.getFloatValue(
																	CSSPrimitiveValue.CSS_NUMBER);
										val = col.getGreen();
										float green = val.getFloatValue(
																		CSSPrimitiveValue.CSS_NUMBER);
										val = col.getBlue();
										float blue = val.getFloatValue(
																	 CSSPrimitiveValue.CSS_NUMBER);
										color = new PDFColor(red, green, blue);
								}
						}
						float offset = stop.getOffset().getBaseVal();
						// create bounds from last to offset
						lastoffset = offset;
						someColors.addElement(color);
				}
				PDFPattern myPat = this.pdfDoc.createGradient(true, aColorSpace,
													 someColors, theBounds, theCoords);

				currentStream.write(myPat.getColorSpaceOut(fill));
				if (fill)
						di.fill = true;
				else
						di.stroke = true;
		}

		/*
		 * This sets up the style for drawing objects.
		 * Should only set style for elements that have changes.
		 *
		 */
		// need mask drawing
		class DrawingInstruction {
				boolean stroke = false;
				boolean nonzero = false; // non-zero fill rule "f*", "B*" operator
				boolean fill = false;
				int linecap = 0; // butt
				int linejoin = 0; // miter
				int miterwidth = 8;
		}
		protected DrawingInstruction applyStyle(SVGElement area,
																						SVGStylable style) {
				DrawingInstruction di = new DrawingInstruction();
				CSSValue sp;
				sp = style.getPresentationAttribute("fill");
				if (sp != null) {
						if (sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_RGBCOLOR) {
										RGBColor col =
											((CSSPrimitiveValue) sp).getRGBColorValue();
										CSSPrimitiveValue val;
										val = col.getRed();
										float red = val.getFloatValue(
																	CSSPrimitiveValue.CSS_NUMBER);
										val = col.getGreen();
										float green = val.getFloatValue(
																		CSSPrimitiveValue.CSS_NUMBER);
										val = col.getBlue();
										float blue = val.getFloatValue(
																	 CSSPrimitiveValue.CSS_NUMBER);
										PDFColor fillColour = new PDFColor(red, green, blue);
										currentColour = fillColour;
										currentStream.write(fillColour.getColorSpaceOut(true));
										di.fill = true;
								} else if ( ((CSSPrimitiveValue) sp).getPrimitiveType() ==
										CSSPrimitiveValue.CSS_URI) {
										// gradient
										String str = ((CSSPrimitiveValue) sp).getCssText();
										handleGradient(str, di, true, area);
								} else if ( ((CSSPrimitiveValue) sp).getPrimitiveType() ==
										CSSPrimitiveValue.CSS_STRING) {
										String str = ((CSSPrimitiveValue) sp).getCssText();
										if (str.equals("none")) {
												di.fill = false;
										} else if (str.equals("currentColor")) {
												currentStream.write(
													currentColour.getColorSpaceOut(true));
												di.fill = true;
												//			    	} else {
												//				    	handleGradient(str, true, area);
										}
								}
						}
				} else {
						PDFColor fillColour = new PDFColor(0, 0, 0);
						currentStream.write(fillColour.getColorSpaceOut(true));
				}
				sp = style.getPresentationAttribute("fill-rule");
				if (sp != null) {
						if (sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_STRING) {
										if (sp.getCssText().equals("nonzero")) {
												di.nonzero = true;
										}
								}
						}
				} else {
				}
				sp = style.getPresentationAttribute("stroke");
				if (sp != null) {
						if (sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_RGBCOLOR) {
										RGBColor col =
											((CSSPrimitiveValue) sp).getRGBColorValue();
										CSSPrimitiveValue val;
										val = col.getRed();
										float red = val.getFloatValue(
																	CSSPrimitiveValue.CSS_NUMBER);
										val = col.getGreen();
										float green = val.getFloatValue(
																		CSSPrimitiveValue.CSS_NUMBER);
										val = col.getBlue();
										float blue = val.getFloatValue(
																	 CSSPrimitiveValue.CSS_NUMBER);
										PDFColor fillColour = new PDFColor(red, green, blue);
										currentStream.write(
											fillColour.getColorSpaceOut(false));
										di.stroke = true;
								} else if ( ((CSSPrimitiveValue) sp).getPrimitiveType() ==
										CSSPrimitiveValue.CSS_URI) {
										// gradient
										String str = ((CSSPrimitiveValue) sp).getCssText();
										handleGradient(str, di, false, area);
								} else if ( ((CSSPrimitiveValue) sp).getPrimitiveType() ==
										CSSPrimitiveValue.CSS_STRING) {
										String str = ((CSSPrimitiveValue) sp).getCssText();
										if (str.equals("none")) {
												di.stroke = false;
												//			    	} else {
												//				    	handleGradient(str, false, area);
										}
								}
						}
				} else {
						PDFColor fillColour = new PDFColor(0, 0, 0);
						currentStream.write(fillColour.getColorSpaceOut(false));
				}
				sp = style.getPresentationAttribute("stroke-linecap");
				if (sp != null) {
						if (sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_STRING) {
										String str = sp.getCssText();
										// butt, round ,square
										if (str.equals("butt")) {
												currentStream.write(0 + " J\n");
										} else if (str.equals("round")) {
												currentStream.write(1 + " J\n");
										} else if (str.equals("square")) {
												currentStream.write(2 + " J\n");
										}
								}
						}
				} else {
				}
				sp = style.getPresentationAttribute("stroke-linejoin");
				if (sp != null) {
						if (sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_STRING) {
										String str = sp.getCssText();
										if (str.equals("miter")) {
												currentStream.write(0 + " j\n");
										} else if (str.equals("round")) {
												currentStream.write(1 + " j\n");
										} else if (str.equals("bevel")) {
												currentStream.write(2 + " j\n");
										}
								}
						}
				} else {
				}
				sp = style.getPresentationAttribute("stroke-miterlimit");
				if (sp != null) {
						if (sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								float width;
								width = ((CSSPrimitiveValue) sp).getFloatValue(
													CSSPrimitiveValue.CSS_PT);
								PDFNumber pdfNumber = new PDFNumber();
								currentStream.write(pdfNumber.doubleOut(width) + " M\n");
						}
				} else {
				}
				sp = style.getPresentationAttribute("stroke-width");
				if (sp != null) {
						if (sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								float width;
								width = ((CSSPrimitiveValue) sp).getFloatValue(
													CSSPrimitiveValue.CSS_PT);
								PDFNumber pdfNumber = new PDFNumber();
								currentStream.write(pdfNumber.doubleOut(width) + " w\n");
						}
				}
				sp = style.getPresentationAttribute("stroke-dasharray");
				if (sp != null) {
						if (sp.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
								currentStream.write("[ ");
								CSSValueList list = (CSSValueList) sp;
								for (int count = 0; count < list.getLength(); count++) {
										CSSValue val = list.item(count);
										if (val.getCssValueType() ==
														CSSValue.CSS_PRIMITIVE_VALUE) {
												currentStream.write(
													((CSSPrimitiveValue) val).getFloatValue(
														CSSPrimitiveValue.CSS_NUMBER) + " ");
										}
								}
								currentStream.write("] ");
								sp = style.getPresentationAttribute("stroke-dashoffset");
								if (sp != null && sp.getCssValueType() ==
												CSSValue.CSS_PRIMITIVE_VALUE) {
										currentStream.write(
											((CSSPrimitiveValue) sp).getFloatValue(
												CSSPrimitiveValue.CSS_NUMBER) + " d\n");
								} else {
										currentStream.write("0 d\n");
								}
						}
				}
				sp = style.getPresentationAttribute("clip-path");
				if (sp != null) {
						String clipurl;
						if (sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_URI) {
										clipurl = ((CSSPrimitiveValue) sp).getCssText();
										if (clipurl.startsWith("url(")) {
												int b1 = clipurl.indexOf("(");
												int b2 = clipurl.indexOf(")");
												clipurl = clipurl.substring(b1 + 1, b2);
										}
										// get def of mask and set mask
										SVGElement graph = null;
										graph = locateDef(clipurl, area);
										if (graph != null) {
												MessageHandler.logln("clip path: " + graph);
												// render the clip path elements and make it the clip
												// renderElement(svgarea, graph, posx, posy);
										}
								}
						}
				}
				sp = style.getPresentationAttribute("mask");
				if (sp != null) {
						String maskurl;
						if (sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								if (((CSSPrimitiveValue) sp).getPrimitiveType() ==
												CSSPrimitiveValue.CSS_URI) {
										maskurl = ((CSSPrimitiveValue) sp).getCssText();
										//					System.out.println("mask: " + maskurl);
										// get def of mask and set mask
										if (maskurl.startsWith("url(")) {
												int b1 = maskurl.indexOf("(");
												int b2 = maskurl.indexOf(")");
												maskurl = maskurl.substring(b1 + 1, b2);
										}
										SVGElement graph = null;
										graph = locateDef(maskurl, area);
										if (graph != null) {
												MessageHandler.logln("mask: " + graph);
												//						SVGElement parent = graph.getGraphicParent();
												//						graph.setParent(area);
												//						renderElement(svgarea, graph, posx, posy);
												//						graph.setParent(parent);
										}
								}
						}
				}
				return di;
		}

		protected void applyTransform(SVGAnimatedTransformList trans) {
				PDFNumber pdfNumber = new PDFNumber();
				SVGTransformList list = trans.getBaseVal();
				for (int count = 0; count < list.getNumberOfItems(); count++) {
						SVGMatrix matrix =
							((SVGTransform) list.getItem(count)).getMatrix();
						currentStream.write(pdfNumber.doubleOut(matrix.getA(), 5) +
																" " + pdfNumber.doubleOut(matrix.getB(), 5) + " " +
																pdfNumber.doubleOut(matrix.getC(), 5) + " " +
																pdfNumber.doubleOut(matrix.getD(), 5) + " " +
																pdfNumber.doubleOut(matrix.getE(), 5) + " " +
																pdfNumber.doubleOut(matrix.getF(), 5) + " cm\n");
				}
		}

		/**
		 * Main rendering selection.
		 * This applies any transform and style and then calls the appropriate
		 * rendering method depending on the type of element.
		 */
		public void renderElement(SVGElement area, int posx, int posy) {
				int x = posx;
				int y = posy;
				//		CSSStyleDeclaration style = null;
				//		if(area instanceof SVGStylable)
				//			style = ((SVGStylable)area).getStyle();
				DrawingInstruction di = null;

				currentStream.write("q\n");
				if (area instanceof SVGTransformable) {
						SVGTransformable tf = (SVGTransformable) area;
//						SVGAnimatedTransformList trans = tf.getTransform();
//						if (trans != null) {
//								applyTransform(trans);
//						}
				}

				if (area instanceof SVGStylable) {
//						di = applyStyle(area, (SVGStylable) area);
				}

				if (area instanceof SVGRectElement) {
						SVGRectElement rg = (SVGRectElement) area;
						float rectx = rg.getX().getBaseVal().getValue();
						float recty = rg.getY().getBaseVal().getValue();
						float rx = 0;//rg.getRx().getBaseVal().getValue();
						float ry = 0;//rg.getRy().getBaseVal().getValue();
						float rw = rg.getWidth().getBaseVal().getValue();
						float rh = rg.getHeight().getBaseVal().getValue();
						addRect(rectx, recty, rw, rh, rx, ry, di);
				} else if (area instanceof SVGLineElement) {
						SVGLineElement lg = (SVGLineElement) area;
						float x1 = lg.getX1().getBaseVal().getValue();
						float y1 = lg.getY1().getBaseVal().getValue();
						float x2 = lg.getX2().getBaseVal().getValue();
						float y2 = lg.getY2().getBaseVal().getValue();
						addLine(x1, y1, x2, y2, di);
				} else if (area instanceof SVGTextElement) {
						//			currentStream.add("q\n");
						//			currentStream.add(1 + " " + 0 + " " + 0 + " " + 1 + " " + 0 + " " + 0 + " cm\n");
						currentStream.write("BT\n");
						renderText((SVGTextElement) area, 0, 0, di);
						currentStream.write("ET\n");
						//			currentStream.add("Q\n");
				} else if (area instanceof SVGCircleElement) {
						SVGCircleElement cg = (SVGCircleElement) area;
						float cx = cg.getCx().getBaseVal().getValue();
						float cy = cg.getCy().getBaseVal().getValue();
						float r = cg.getR().getBaseVal().getValue();
						addCircle(cx, cy, r, di);
				} else if (area instanceof SVGEllipseElement) {
						SVGEllipseElement cg = (SVGEllipseElement) area;
						float cx = cg.getCx().getBaseVal().getValue();
						float cy = cg.getCy().getBaseVal().getValue();
						float rx = cg.getRx().getBaseVal().getValue();
						float ry = cg.getRy().getBaseVal().getValue();
						addEllipse(cx, cy, rx, ry, di);
				} else if (area instanceof SVGPathElement) {
//						addPath(((SVGPathElement) area).getPathSegList(), posx,
//										posy, di);
				} else if (area instanceof SVGPolylineElement) {
//						addPolyline(((SVGPolylineElement) area).getPoints(), di, false);
				} else if (area instanceof SVGPolygonElement) {
//						addPolyline(((SVGPolygonElement) area).getPoints(), di, true);
				} else if (area instanceof SVGGElement) {
						renderGArea((SVGGElement) area, x, y);
				} else if (area instanceof SVGUseElement) {
						SVGUseElement ug = (SVGUseElement) area;
						String ref = ug.getHref().getBaseVal();
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
										currentStream.write("q\n");
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
										currentStream.write(usex + " " + usey + " m\n");
										currentStream.write((usex + usewidth) + " " +
																				usey + " l\n");
										currentStream.write((usex + usewidth) + " " +
																				(usey + useheight) + " l\n");
										currentStream.write(usex + " " +
																				(usey + useheight) + " l\n");
										currentStream.write("h\n");
										currentStream.write("W\n");
										currentStream.write("n\n");
										currentStream.write(scaleX + " 0 0 " + scaleY +
																				" " + usex + " " + usey + " cm\n");
										renderSymbol(symbol, posx, posy);
										currentStream.write("Q\n");
								} else {
										renderElement(graph, posx, posy);
								}
								//				graph.setParent(parent);
						}
						else {
								MessageHandler.logln("Use Element: " + ref + " not found");
						}
				} else if (area instanceof SVGImageElement) {
						SVGImageElement ig = (SVGImageElement) area;
						renderImage(ig.getHref().getBaseVal(), ig.getX().getBaseVal().getValue(), ig.getY().getBaseVal().getValue(), ig.getWidth().getBaseVal().getValue(), ig.getHeight().getBaseVal().getValue());
				} else if (area instanceof SVGSVGElement) {
						currentStream.write("q\n");
						SVGSVGElement svgel = (SVGSVGElement) area;
						float svgx = 0;
/*						if (svgel.getX() != null)
								svgx = svgel.getX().getBaseVal().getValue();
						float svgy = 0;
						if (svgel.getY() != null)
								svgy = svgel.getY().getBaseVal().getValue();
						currentStream.write(1 + " 0 0 " + 1 + " " + svgx + " " +
																svgy + " cm\n");
						renderSVG(svgel, (int)(x + 1000 * svgx),
											(int)(y + 1000 * svgy));*/
						currentStream.write("Q\n");
						//		} else if (area instanceof SVGSymbolElement) {
						// 'symbol' element is not rendered (except by 'use')
				} else if (area instanceof SVGAElement) {
						SVGAElement ael = (SVGAElement) area;
						org.w3c.dom.NodeList nl = ael.getChildNodes();
						for (int count = 0; count < nl.getLength(); count++) {
								org.w3c.dom.Node n = nl.item(count);
								if (n instanceof SVGElement) {
/*										if (n instanceof GraphicElement) {
												SVGRect rect = ((GraphicElement) n).getBBox();
												if (rect != null) {
														/*							currentAnnotList = this.pdfDoc.makeAnnotList();
																					currentPage.setAnnotList(currentAnnotList);
																					String dest = linkSet.getDest();
																					int linkType = linkSet.getLinkType();
																					currentAnnotList.addLink(
																						this.pdfDoc.makeLink(lrect.getRectangle(), dest, linkType));
																					currentAnnotList = null;
														 * }
										}*/
										renderElement((SVGElement) n, posx, posy);
								}
						}
				} else if (area instanceof SVGSwitchElement) {
						handleSwitchElement(posx, posy, (SVGSwitchElement) area);
				}
				// should be done with some cleanup code, so only
				// required values are reset.
				currentStream.write("Q\n");
		}

		/**
		 * Todo: underline, linethrough, textpath
		 */
		public void renderText(SVGTextElement tg, float x, float y,
													 DrawingInstruction di) {
				SVGTextRenderer str = new SVGTextRenderer(fontState, tg, x, y);
//				str.renderText(tg);
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
						if (ch > 127) {
								pdf = pdf.append("\\");
								pdf = pdf.append(Integer.toOctalString((int) ch));
								currentX += fs.width(ch) / 1000f;
								inbetween = true;
								addedspace = false;
						} else {
								switch (ch) {
										case '(' :
												pdf = pdf.append("\\(");
												currentX += fs.width(ch) / 1000f;
												inbetween = true;
												addedspace = false;
												break;
										case ')' :
												pdf = pdf.append("\\)");
												currentX += fs.width(ch) / 1000f;
												inbetween = true;
												addedspace = false;
												break;
										case '\\' :
												pdf = pdf.append("\\\\");
												currentX += fs.width(ch) / 1000f;
												inbetween = true;
												addedspace = false;
												break;
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
				}
				currentStream.write(pdf.toString());
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

				SVGTextRenderer(FontState fontState, SVGTextElement tg,
												float x, float y) {
						fs = fontState;

						PDFNumber pdfNumber = new PDFNumber();
/*						SVGTransformList trans = tg.getTransform().getBaseVal();
						matrix = trans.consolidate().getMatrix();
						transstr = (pdfNumber.doubleOut(matrix.getA()) + " " +
												pdfNumber.doubleOut(matrix.getB()) + " " +
												pdfNumber.doubleOut(matrix.getC()) + " " +
												pdfNumber.doubleOut(-matrix.getD()) + " ");*/
						transstr = "1 0 0 1 ";
						this.x = x;
						this.y = y;
				}

				void renderText(SVGTextElement te) {
						float xoffset = 0;

/*						if (te.anchor.getEnum() != TextAnchor.START) {
								// This is a bit of a hack: The code below will update
								// the current position, so all I have to do is to
								// prevent that the code will write anything to the
								// PDF stream...
								StringWriter oldStream = currentStream;
								currentStream = new StringWriter ();
								
								renderText (te, 0f, true);

								float width = currentX - te.x;
								currentStream = oldStream;

								if (te.anchor.getEnum() == TextAnchor.END) {
										xoffset = -width;
								} else if (te.anchor.getEnum() == TextAnchor.MIDDLE) {
										xoffset = -width/2;
								}
						}*/

						renderText (te, xoffset, false);
				}

				void renderText(SVGTextElement te, float xoffset, boolean getWidthOnly) {
//						DrawingInstruction di = applyStyle(te, te);
//						if (di.fill) {
//								if (di.stroke) {
//										currentStream.write("2 Tr\n");
//								} else {
										currentStream.write("0 Tr\n");
//								}
//						} else if (di.stroke) {
//								currentStream.write("1 Tr\n");
//						}
//						updateFont(te, fs);

						float tx = te.getX().getBaseVal().getValue();
						float ty = te.getY().getBaseVal().getValue();
						currentX = x + tx + xoffset;
						currentY = y + ty;
						baseX = currentX;
						baseY = currentY;
						NodeList nodel = te.getChildNodes();
						//		Vector list = te.textList;
						for (int count = 0; count < nodel.getLength(); count++) {
								Object o = nodel.item(count);
//								applyStyle(te, te);
								if (o instanceof CharacterData) {
										String str = ((CharacterData) o).getData();
										currentStream.write(transstr +
																				(currentX/* + matrix.getE()*/) + " " +
																				(baseY/* + matrix.getF()*/) + " Tm " + "(");
										boolean spacing = "preserve".equals(te.getXMLspace());
										currentX = addSVGStr(fs, currentX, str, spacing);
										currentStream.write(") Tj\n");
								} else if (o instanceof SVGTextPathElement) {
										SVGTextPathElement tpg = (SVGTextPathElement) o;
										String ref = tpg.getHref().getBaseVal();
										SVGElement graph = null;
										graph = locateDef(ref, tpg);
										if (graph instanceof SVGPathElement) {
												// probably not the best way to do this, should be able
												// to render without the style being set.
												//					GraphicImpl parent = graph.getGraphicParent();
												//					graph.setParent(tpg);
												// set text path??
												// how should this work
												//					graph.setParent(parent);
										}
								} else if (o instanceof SVGTRefElement) {
										SVGTRefElement trg = (SVGTRefElement) o;
										String ref = trg.getHref().getBaseVal();
										SVGElement element = locateDef(ref, trg);
										if (element instanceof SVGTextElement) {
												//					GraphicImpl parent = graph.getGraphicParent();
												//					graph.setParent(trg);
												SVGTextElement tele =
													(SVGTextElement) element;
												// the style should be from tele, but it needs to be placed as a child
												// of trg to work
/*												di = applyStyle(trg, trg);
												if (di.fill) {
														if (di.stroke) {
																currentStream.write("2 Tr\n");
														} else {
																currentStream.write("0 Tr\n");
														}
												} else if (di.stroke) {
														currentStream.write("1 Tr\n");
												}*/
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
														currentStream.write("/" +
																								fs.getFontName() + " " +
																								fs.getFontSize() / 1000f + " Tf\n");
												}
												//					graph.setParent(parent);
										}
								} else if (o instanceof SVGTSpanElement) {
										SVGTSpanElement tsg = (SVGTSpanElement) o;
//										applyStyle(tsg, tsg);
										boolean changed = false;
										FontState oldfs = fs;
//										changed = updateFont(tsg, fs);
										boolean spacing = "preserve".equals(tsg.getXMLspace());
										renderTextNodes(spacing, tsg.getChildNodes(),
																		tsg.getX().getBaseVal(),
																		tsg.getY().getBaseVal(),
																		tsg.getDx().getBaseVal(),
																		tsg.getDy().getBaseVal());

										//				currentX += fs.width(' ') / 1000f;
										if (changed) {
												fs = oldfs;
												currentStream.write("/" + fs.getFontName() +
																						" " + fs.getFontSize() / 1000f + " Tf\n");
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
										StringBuffer pdf = new StringBuffer();
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
												if (ch > 127) {
														pdf = pdf.append(transstr +
																						 (xpos + matrix.getE()) + " " +
																						 (ypos + matrix.getF()) + " Tm " +
																						 "(" + "\\" +
																						 Integer.toOctalString((int) ch) +
																						 ") Tj\n");
														currentX = xpos + fs.width(ch) / 1000f;
														currentY = ypos;
														charPos++;
														inbetween = true;
														addedspace = false;
												} else {
														switch (ch) {
																case '(' :
																		pdf = pdf.append(transstr +
																										 (xpos + matrix.getE()) +
																										 " " + (ypos +
																														matrix.getF()) + " Tm " +
																										 "(" + "\\(" + ") Tj\n");
																		currentX = xpos + fs.width(ch) / 1000f;
																		currentY = ypos;
																		charPos++;
																		inbetween = true;
																		addedspace = false;
																		break;
																case ')' :
																		pdf = pdf.append(transstr +
																										 (xpos + matrix.getE()) +
																										 " " + (ypos +
																														matrix.getF()) + " Tm " +
																										 "(" + "\\)" + ") Tj\n");
																		currentX = xpos + fs.width(ch) / 1000f;
																		currentY = ypos;
																		charPos++;
																		inbetween = true;
																		addedspace = false;
																		break;
																case '\\' :
																		pdf = pdf.append(transstr +
																										 (xpos + matrix.getE()) +
																										 " " + (ypos +
																														matrix.getF()) + " Tm " +
																										 "(" + "\\\\" + ") Tj\n");
																		currentX = xpos + fs.width(ch) / 1000f;
																		currentY = ypos;
																		charPos++;
																		inbetween = true;
																		addedspace = false;
																		break;
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
																		pdf = pdf.append(transstr +
																										 (xpos + matrix.getE()) +
																										 " " + (ypos +
																														matrix.getF()) + " Tm " +
																										 "(" + ch + ") Tj\n");
																		currentX = xpos + fs.width(ch) / 1000f;
																		currentY = ypos;
																		charPos++;
																		inbetween = true;
																		break;
														}
												}
												currentStream.write(pdf.toString());
										}
								}
						}
				}

				protected boolean updateFont(SVGStylable style, FontState fs) {
						boolean changed = false;
						String fontFamily = fs.getFontFamily();
						CSSValue sp = style.getPresentationAttribute("font-family");
						if (sp != null &&
										sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
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
										sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
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
										sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
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
										sp.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
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
										// FIX-ME: should get the font-variant property
										fs = new FontState(fs.getFontInfo(), fontFamily,
																			 fontStyle, fontWeight, (int)(newSize * 1000),
																			 FontVariant.NORMAL);
								} catch (Exception fope) {
								}
								this.fs = fs;

								currentStream.write("/" + fs.getFontName() + " " +
																		newSize + " Tf\n");
						} else {
								if (!currentFontName.equals(fs.getFontName()) ||
												currentFontSize != fs.getFontSize()) {
										//				currentFontName = fs.getFontName();
										//				currentFontSize = fs.getFontSize();
										currentStream.write("/" + fs.getFontName() + " " +
																				(fs.getFontSize() / 1000) + " Tf\n");
								}
						}
						return changed;
				}
		}
}
