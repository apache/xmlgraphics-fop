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
package org.apache.fop.mathml;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.image.analyser.XMLReader;
import org.apache.fop.image.FopImage;
import org.w3c.dom.Document;

import java.util.HashMap;

import net.sourceforge.jeuclid.MathBase;
import net.sourceforge.jeuclid.DOMMathBuilder;

/**
 * This class provides the element mapping for FOP.
 */
public class MathMLElementMapping extends ElementMapping {

    /** MathML Namespace */
    public static final String NAMESPACE = "http://www.w3.org/1998/Math/MathML"; 

    public MathMLElementMapping() {
        this.namespaceURI = NAMESPACE;
    }

    protected void initialize() {
        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("math", new ME());
            foObjs.put(DEFAULT, new MathMLMaker());

            XMLReader.setConverter(this.namespaceURI, new MathMLConverter());
        }
    }

    static class MathMLMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new MathMLObj(parent);
        }
    }

    static class ME extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new MathMLElement(parent);
        }
    }

    static class MathMLConverter implements XMLReader.Converter {
        public FopImage.ImageInfo convert(Document doc) {
            try {
                FopImage.ImageInfo info = new FopImage.ImageInfo();
                String fontname = "Helvetica";
                int fontstyle = 0;
                int inlinefontstyle = 0;
                int inlinefontsize = 12;
                int displayfontsize = 12;

                MathBase base = new MathBase(
                                  (new DOMMathBuilder(doc)).getMathRootElement(),
                                  fontname, fontstyle, inlinefontsize,
                                  displayfontsize);

                base.setDebug(false);

                info.data = MathMLElement.createSVG(base);

                info.width = base.getWidth();
                info.height = base.getHeight();

                info.mimeType = "image/svg+xml";
                info.str = "http://www.w3.org/2000/svg";

                return info;
            } catch (Throwable t) {
                /**@todo log that properly */
            }
            return null;

        }
    }

}
