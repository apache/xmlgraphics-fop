/*
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

package org.apache.fop.rtf.renderer;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.ConsoleLogger;

//FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfPage;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Property;


/**  Converts simple-page-master attributes into strings as defined in RtfPage.
 *  @author Christopher Scott, scottc@westinghouse.com
 *  @author Peter Herweg, pherweg@web.de
 */

class PageAttributesConverter {

    private static Logger log = new ConsoleLogger();

    /** convert xsl:fo attributes to RTF text attributes */
    static RtfAttributes convertPageAttributes(PropertyList props, PropertyList defProps) {
        RtfAttributes attrib = null;

        try {
            Property p;

            if (defProps != null) {
                attrib = convertPageAttributes(defProps, null);
            } else {
                attrib = new RtfAttributes();
            }

            if ((p = props.get("page-width")) != null) {
                Float f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.PAGE_WIDTH,
                    (int)FoUnitsConverter.getInstance().convertToTwips(f.toString() + "pt"));
            }
            if ((p = props.get("page-height")) != null) {
                Float f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.PAGE_HEIGHT,
                    (int)FoUnitsConverter.getInstance().convertToTwips(f.toString() + "pt"));
            }
            if ((p = props.get("margin-top")) != null) {
                Float f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.MARGIN_TOP,
                    (int)FoUnitsConverter.getInstance().convertToTwips(f.toString() + "pt"));
            }
            if ((p = props.get("margin-bottom")) != null) {
                Float f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.MARGIN_BOTTOM,
                    (int)FoUnitsConverter.getInstance().convertToTwips(f.toString() + "pt"));
            }
            if ((p = props.get("margin-left")) != null) {
                Float f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.MARGIN_LEFT,
                    (int)FoUnitsConverter.getInstance().convertToTwips(f.toString() + "pt"));
            }
            if ((p = props.get("margin-right")) != null) {
                Float f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.MARGIN_RIGHT,
                    (int)FoUnitsConverter.getInstance().convertToTwips(f.toString() + "pt"));
            }
        } catch (FOPException e) {
            log.error("Exception in convertPageAttributes: " + e.getMessage() + "- page attributes ignored");
            attrib=new RtfAttributes();
        }

        return attrib;
    }
}
