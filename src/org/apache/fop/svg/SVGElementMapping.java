/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.svg;

import org.apache.fop.fo.DirectPropertyListBuilder;
import org.apache.fop.fo.TreeBuilder;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.apps.Driver;

import org.apache.batik.util.XMLResourceDescriptor;

import java.util.HashMap;

public class SVGElementMapping implements ElementMapping {

    private static HashMap foObjs = null;    
    
    private static synchronized void setupSVG() {

        if(foObjs == null) {
            // this sets the parser that will be used
            // by default (SVGBrokenLinkProvider)
            // normally the user agent value is used
            XMLResourceDescriptor.setXMLParserClassName(Driver.getParserClassName());

            foObjs = new HashMap();
            foObjs.put("svg", SVGElement.maker());
            foObjs.put("rect", SVGObj.maker("rect"));
            foObjs.put("line", SVGObj.maker("line"));
            foObjs.put("text", SVGObj.maker("text"));

            foObjs.put("font-face", SVGObj.maker("font-face"));
            foObjs.put("font-face-uri", SVGObj.maker("font-face-uri"));
            foObjs.put("font-face-src", SVGObj.maker("font-face-src"));
            foObjs.put("desc", SVGObj.maker("desc"));
            foObjs.put("title", SVGObj.maker("title"));
            foObjs.put("circle", SVGObj.maker("circle"));
            foObjs.put("ellipse", SVGObj.maker("ellipse"));
            foObjs.put("g", SVGObj.maker("g"));
            foObjs.put("polyline", SVGObj.maker("polyline"));
            foObjs.put("polygon", SVGObj.maker("polygon"));
            foObjs.put("defs", SVGObj.maker("defs"));
            foObjs.put("path", SVGObj.maker("path"));
            foObjs.put("use", SVGObj.maker("use"));
            foObjs.put("tspan", SVGObj.maker("tspan"));
            foObjs.put("tref", SVGObj.maker("tref"));
            foObjs.put("image", SVGObj.maker("image"));
            foObjs.put("style", SVGObj.maker("style"));

            foObjs.put("textPath", SVGObj.maker("textPath"));
            foObjs.put("clipPath", SVGObj.maker("clipPath"));
            foObjs.put("mask", SVGObj.maker("mask"));
            foObjs.put("linearGradient", SVGObj.maker("linearGradient"));
            foObjs.put("radialGradient", SVGObj.maker("radialGradient"));
            foObjs.put("stop", SVGObj.maker("stop"));
            foObjs.put("a", SVGObj.maker("a"));
            foObjs.put("switch", SVGObj.maker("switch"));
            foObjs.put("symbol", SVGObj.maker("symbol"));

            foObjs.put("pattern", SVGObj.maker("pattern"));

            foObjs.put("marker", SVGObj.maker("marker"));
            foObjs.put("animate", SVGObj.maker("animate"));
            foObjs.put("altGlyph", SVGObj.maker("altGlyph"));
            foObjs.put("font", SVGObj.maker("font"));
            foObjs.put("glyph", SVGObj.maker("glyph"));
            foObjs.put("missing-glyph", SVGObj.maker("missing-glyph"));
            foObjs.put("hkern", SVGObj.maker("hkern"));
            foObjs.put("vkern", SVGObj.maker("vkern"));
            foObjs.put("set", SVGObj.maker("set"));
            foObjs.put("animateMotion", SVGObj.maker("animateMotion"));
            foObjs.put("animateColor", SVGObj.maker("animateColor"));
            foObjs.put("animateTransform", SVGObj.maker("animateTransform"));
            foObjs.put("cursor", SVGObj.maker("cursor"));
            foObjs.put("filter", SVGObj.maker("filter"));

            foObjs.put("feFlood", SVGObj.maker("feFlood"));
            foObjs.put("feGaussianBlur", SVGObj.maker("feGaussianBlur"));
            foObjs.put("feOffset", SVGObj.maker("feOffset"));
            foObjs.put("feMerge", SVGObj.maker("feMerge"));
            foObjs.put("feMergeNode", SVGObj.maker("feMergeNode"));
        }
    }

    public void addToBuilder(TreeBuilder builder) {
        setupSVG();
        String uri = "http://www.w3.org/2000/svg";
        builder.addMapping(uri, foObjs);

        builder.addPropertyListBuilder(uri, new DirectPropertyListBuilder());
    }
}

