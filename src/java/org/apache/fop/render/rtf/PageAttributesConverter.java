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

package org.apache.fop.render.rtf;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.ConsoleLogger;

//FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfPage;


/**  Converts simple-page-master attributes into strings as defined in RtfPage.
 *  @author Christopher Scott, scottc@westinghouse.com
 *  @author Peter Herweg, pherweg@web.de
 */

class PageAttributesConverter {

    private static Logger log = new ConsoleLogger();

    /** convert xsl:fo attributes to RTF text attributes */
    static RtfAttributes convertPageAttributes(SimplePageMaster pagemaster) {
        RtfAttributes attrib = new RtfAttributes();
        
        try {
            FoUnitsConverter converter = FoUnitsConverter.getInstance();
            
            float fPageTop = 0;
            float fPageBottom = 0;
            PropertyList props = null;                        
            Property p = null;
            Float f = null;
            
            Region before = pagemaster.getRegion("before");
            Region body   = pagemaster.getRegion("body");
            Region after  = pagemaster.getRegion("after");
            
            //page attributes
            props = pagemaster.propertyList;
            
            if ((p = props.get(Constants.PR_PAGE_WIDTH)) != null) {
                f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.PAGE_WIDTH,
                    (int)converter.convertToTwips(f.toString() + "pt"));
            }
            
            if ((p = props.get(Constants.PR_PAGE_HEIGHT)) != null) {
                f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.PAGE_HEIGHT,
                    (int)converter.convertToTwips(f.toString() + "pt"));
            }
         
            if ((p = props.get(Constants.PR_MARGIN_TOP)) != null) {
                fPageTop = p.getLength().getValue() / 1000f;
            }

            if ((p = props.get(Constants.PR_MARGIN_BOTTOM)) != null) {
                fPageBottom = p.getLength().getValue() / 1000f;
            }

            if ((p = props.get(Constants.PR_MARGIN_LEFT)) != null) {
                f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.MARGIN_LEFT,
                    (int)converter.convertToTwips(f.toString() + "pt"));
            }
            if ((p = props.get(Constants.PR_MARGIN_RIGHT)) != null) {
                f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.MARGIN_RIGHT,
                    (int)converter.convertToTwips(f.toString() + "pt"));
            }
            
            //region-body attributes
            float fBodyTop = fPageTop;
            float fBodyBottom = fPageBottom;
            
            if (body != null) {
                props = body.propertyList;
            
                if ((p = props.get(Constants.PR_MARGIN_TOP)) != null) {
                    fBodyTop += p.getLength().getValue() / 1000f;
                }
            
                if ((p = props.get(Constants.PR_MARGIN_BOTTOM)) != null) {
                    fBodyBottom += p.getLength().getValue() / 1000f;
                }
            }
            
            f = new Float(fBodyTop);
            attrib.set(RtfPage.MARGIN_TOP,
                    (int)converter.convertToTwips(f.toString() + "pt"));

            f = new Float(fBodyBottom);
            attrib.set(RtfPage.MARGIN_BOTTOM,
                    (int)converter.convertToTwips(f.toString() + "pt"));
            
            //region-before attributes
            float fBeforeTop = fPageTop;
                        
            if (before != null) {
                props = before.propertyList;
            
                if ((p = props.get(Constants.PR_MARGIN_TOP)) != null) {
                    fBeforeTop += p.getLength().getValue() / 1000f;
                }
            }

            f = new Float(fBeforeTop);
            attrib.set(RtfPage.HEADERY,
                    (int)converter.convertToTwips(f.toString() + "pt"));

            //region-after attributes
            float fAfterBottom = fPageBottom;
            
            if (after != null) {
                props = after.propertyList;
                
                if ((p = props.get(Constants.PR_MARGIN_BOTTOM)) != null) {
                    fAfterBottom += p.getLength().getValue() / 1000f;
                }             
            }
            
            f = new Float(fAfterBottom);
            attrib.set(RtfPage.FOOTERY,
                    (int)converter.convertToTwips(f.toString() + "pt"));

        } catch (FOPException e) {
            log.error("Exception in convertPageAttributes: " 
                + e.getMessage() + "- page attributes ignored");
            attrib = new RtfAttributes();
        }

        return attrib;
    }
}
