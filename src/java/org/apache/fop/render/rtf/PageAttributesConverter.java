/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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


package org.apache.fop.render.rtf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

//FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfPage;


/**  Converts simple-page-master attributes into strings as defined in RtfPage.
 *  @author Christopher Scott, scottc@westinghouse.com
 *  @author Peter Herweg, pherweg@web.de
 */

class PageAttributesConverter {

    private static Log log = new SimpleLog("FOP/RTF");

    /** convert xsl:fo attributes to RTF text attributes */
    static RtfAttributes convertPageAttributes(SimplePageMaster pagemaster) {
        RtfAttributes attrib = new RtfAttributes();
        
        try {
            FoUnitsConverter converter = FoUnitsConverter.getInstance();
            
            float fPageTop = 0;
            float fPageBottom = 0;
            Property p = null;
            Float f = null;
            
            Region before = pagemaster.getRegion(Constants.FO_REGION_BEFORE);
            Region body   = pagemaster.getRegion(Constants.FO_REGION_BODY);
            Region after  = pagemaster.getRegion(Constants.FO_REGION_AFTER);
            
            if ((p = pagemaster.getProperty(Constants.PR_PAGE_WIDTH)) != null) {
                f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.PAGE_WIDTH,
                    (int)converter.convertToTwips(f.toString() + "pt"));
            }
            
            if ((p = pagemaster.getProperty(Constants.PR_PAGE_HEIGHT)) != null) {
                f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.PAGE_HEIGHT,
                    (int)converter.convertToTwips(f.toString() + "pt"));
            }
         
            if ((p = pagemaster.getProperty(Constants.PR_MARGIN_TOP)) != null) {
                fPageTop = p.getLength().getValue() / 1000f;
            }

            if ((p = pagemaster.getProperty(Constants.PR_MARGIN_BOTTOM)) != null) {
                fPageBottom = p.getLength().getValue() / 1000f;
            }

            if ((p = pagemaster.getProperty(Constants.PR_MARGIN_LEFT)) != null) {
                f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.MARGIN_LEFT,
                    (int)converter.convertToTwips(f.toString() + "pt"));
            }
            if ((p = pagemaster.getProperty(Constants.PR_MARGIN_RIGHT)) != null) {
                f = new Float(p.getLength().getValue() / 1000f);
                attrib.set(RtfPage.MARGIN_RIGHT,
                    (int)converter.convertToTwips(f.toString() + "pt"));
            }
            
            //region-body attributes
            float fBodyTop = fPageTop;
            float fBodyBottom = fPageBottom;
            
            if (body != null) {
                if ((p = body.getProperty(Constants.PR_MARGIN_TOP)) != null) {
                    fBodyTop += p.getLength().getValue() / 1000f;
                }
            
                if ((p = body.getProperty(Constants.PR_MARGIN_BOTTOM)) != null) {
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
                if ((p = before.getProperty(Constants.PR_MARGIN_TOP)) != null) {
                    fBeforeTop += p.getLength().getValue() / 1000f;
                }
            }

            f = new Float(fBeforeTop);
            attrib.set(RtfPage.HEADERY,
                    (int)converter.convertToTwips(f.toString() + "pt"));

            //region-after attributes
            float fAfterBottom = fPageBottom;
            
            if (after != null) {
                if ((p = after.getProperty(Constants.PR_MARGIN_BOTTOM)) != null) {
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
