/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;
import org.apache.fop.svg.PathPoint;
import org.apache.fop.dom.svg.*;
import org.w3c.dom.svg.*;

import java.util.*;
/**
 * a PathData quantity in XSL
 * This class parses the string of path data and create a list of
 * object commands. It is up to renderers (or whatever) to interpret
 * the command properly.
 * eg. m at the start is an absolute moveto.
 *
 *
 * @author Keiron Liddle <keiron@aftexsw.com>
 */
public class PathData {
	Vector table = new Vector();

	/**
	 * set the PathData given a particular String specifying PathData and units
	 */
	public PathData (String len)
	{
		convert(len);
	}

	protected void convert(String len)
	{
		StringTokenizer st = new StringTokenizer(len, "MmLlHhVvCcSsQqTtAaZz", true);
		/*
		 * If there are two numbers and no spaces then it is assumed that all
		 * numbers are the same number of chars (3), otherwise there is an error
		 * not mentioned in spec.
		 */
		while(st.hasMoreTokens()) {
			String str = st.nextToken();
			int pos;
			if(str.equals("M")) {
				float[][] vals = getPoints(2, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_MOVETO_ABS, vals[count]));
					}
				}
			} else if(str.equals("m")) {
				// if first element treat as M
				// otherwise treat as implicit lineto, this is handled by renderers
				float[][] vals = getPoints(2, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_MOVETO_REL, vals[count]));
					}
				}
			} else if(str.equals("L")) {
				float[][] vals = getPoints(2, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_LINETO_ABS, vals[count]));
					}
				}
			} else if(str.equals("l")) {
				float[][] vals = getPoints(2, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_LINETO_REL, vals[count]));
					}
				}
			} else if(str.equals("H")) {
				float[][] vals = getPoints(1, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS, vals[count]));
					}
				}
			} else if(str.equals("h")) {
				float[][] vals = getPoints(1, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL, vals[count]));
					}
				}
			} else if(str.equals("V")) {
				float[][] vals = getPoints(1, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS, vals[count]));
					}
				}
			} else if(str.equals("v")) {
				float[][] vals = getPoints(1, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL, vals[count]));
					}
				}
			} else if(str.equals("C")) {
				float[][] vals = getPoints(6, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS, vals[count]));
					}
				}
			} else if(str.equals("c")) {
				float[][] vals = getPoints(6, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL, vals[count]));
					}
				}
			} else if(str.equals("S")) {
				float[][] vals = getPoints(4, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS, vals[count]));
					}
				}
			} else if(str.equals("s")) {
				float[][] vals = getPoints(4, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL, vals[count]));
					}
				}
			} else if(str.equals("Q")) {
				float[][] vals = getPoints(4, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS, vals[count]));
					}
				}
			} else if(str.equals("q")) {
				float[][] vals = getPoints(4, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL, vals[count]));
					}
				}
			} else if(str.equals("T")) {
				float[][] vals = getPoints(2, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS, vals[count]));
					}
				}
			} else if(str.equals("t")) {
				float[][] vals = getPoints(2, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL, vals[count]));
					}
				}
			} else if(str.equals("A")) {
				float[][] vals = getPoints(7, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_ARC_ABS, vals[count]));
					}
				}
			} else if(str.equals("a")) {
				float[][] vals = getPoints(7, st);
				if(vals != null) {
					for(int count = 0; count < vals.length; count++) {
						addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_ARC_REL, vals[count]));
					}
				}
			} else if(str.equals("Z") || str.equals("z")) {
				addSVGPathSeg(new SVGPathSegImpl(SVGPathSeg.PATHSEG_CLOSEPATH, null));
			}
		}
	}

	public Vector getPath()
	{
		return table;
	}

	public String toString()
	{
		return "";
	}

	float[][] getPoints(int num, StringTokenizer st)
	{
		float[] set;
		String str;
		int pos;
		float[][] ret = null;
		if(st.hasMoreTokens()) {
			str = st.nextToken();
			str = str.trim();
//			pos = str.indexOf(" ");
/*			if((str.indexOf(" ") == -1) && (str.indexOf(",") == -1) && (str.indexOf("-") == -1)) {
				int length = str.length();
				if((length % num) != 0) {
					// invalid number comb
				} else {
					// how do we determine the length of a single number?
				}
			} else {*/
			{
				StringTokenizer pointtok = new StringTokenizer(str, " ,-\n\r\t", true);
				int count = 0;
				Vector values = new Vector();
				set = new float[num];
				boolean neg;
				while(pointtok.hasMoreTokens()) {
					String point = null;
					String delim = pointtok.nextToken();
					if(delim.equals("-")) {
						neg = true;
						if(pointtok.hasMoreTokens()) {
							point = pointtok.nextToken();
						} else {
							break;
						}
					} else {
						neg = false;
						if(delim.equals(" ") || delim.equals(",") || delim.equals("\r") || delim.equals("\n") || delim.equals("\t")) {
							continue;
						}
						point = delim;
					}

					float pd = Float.valueOf(point).floatValue();
					if(neg)
						pd = -pd;
					set[count] = pd;
					count++;
					if(count == num) {
						values.addElement(set);
						set = new float[num];
						count = 0;
					}
				}
				count = 0;
				ret = new float[values.size()][];
				for(Enumeration e = values.elements(); e.hasMoreElements(); ) {
					ret[count++] = (float[])e.nextElement();
				}
			}
		}
		return ret;
	}

	protected void addSVGPathSeg(SVGPathSeg pc)
	{
		table.addElement(pc);
	}
}
