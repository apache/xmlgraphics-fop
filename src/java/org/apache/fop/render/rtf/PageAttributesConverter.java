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
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.expr.NumericOp;
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
        FOPRtfAttributes attrib = new FOPRtfAttributes();
        
        try {
            Region before = pagemaster.getRegion(Constants.FO_REGION_BEFORE);
            Region body   = pagemaster.getRegion(Constants.FO_REGION_BODY);
            Region after  = pagemaster.getRegion(Constants.FO_REGION_AFTER);
            
            attrib.set(RtfPage.PAGE_WIDTH, pagemaster.getProperty(Constants.PR_PAGE_WIDTH).getLength());
            attrib.set(RtfPage.PAGE_HEIGHT, pagemaster.getProperty(Constants.PR_PAGE_HEIGHT).getLength());
            
            Length pageTop = pagemaster.getProperty(Constants.PR_MARGIN_TOP).getLength();
            Length pageBottom = pagemaster.getProperty(Constants.PR_MARGIN_BOTTOM).getLength();
            Length pageLeft = pagemaster.getProperty(Constants.PR_MARGIN_LEFT).getLength();
            Length pageRight = pagemaster.getProperty(Constants.PR_MARGIN_RIGHT).getLength();

            Length bodyTop = pageTop;
            Length bodyBottom = pageBottom;
            Length bodyLeft = pageLeft;
            Length bodyRight = pageRight;

            if (body != null) {
                // Should perhaps be replaced by full reference-area handling.
                bodyTop = (Length) NumericOp.addition(pageTop, body.getProperty(Constants.PR_MARGIN_TOP).getLength());
                bodyBottom = (Length) NumericOp.addition(pageBottom, body.getProperty(Constants.PR_MARGIN_BOTTOM).getLength());
                bodyLeft = (Length) NumericOp.addition(pageLeft, body.getProperty(Constants.PR_MARGIN_LEFT).getLength());
                bodyRight = (Length) NumericOp.addition(pageRight, body.getProperty(Constants.PR_MARGIN_RIGHT).getLength());
            }
            
            attrib.set(RtfPage.MARGIN_TOP, bodyTop);
            attrib.set(RtfPage.MARGIN_BOTTOM, bodyBottom);
            attrib.set(RtfPage.MARGIN_LEFT, bodyLeft);
            attrib.set(RtfPage.MARGIN_RIGHT, bodyRight);

            //region-before attributes
            Length beforeTop = pageTop;
            if (before != null) {
                beforeTop = (Length) NumericOp.addition(pageTop, before.getProperty(Constants.PR_MARGIN_TOP).getLength());
            }
            attrib.set(RtfPage.HEADERY, beforeTop);

            //region-after attributes
            Length afterBottom = pageBottom;
            if (after != null) {
                afterBottom = (Length) NumericOp.addition(pageBottom, after.getProperty(Constants.PR_MARGIN_BOTTOM).getLength());
            }
            attrib.set(RtfPage.FOOTERY, beforeTop);
        } catch (Exception e) {
            log.error("Exception in convertPageAttributes: " 
                + e.getMessage() + "- page attributes ignored");
            attrib = new FOPRtfAttributes();
        }

        return attrib;
    }
}
