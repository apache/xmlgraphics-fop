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
package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Status;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.apps.FOPException;

public class ListItemBody extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new ListItemBody(parent, propertyList,
                                    systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new ListItemBody.Maker();
    }

    public ListItemBody(FObj parent, PropertyList propertyList,
                        String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);
    }

    public String getName() {
        return "fo:list-item-body";
    }

    public int layout(Area area) throws FOPException {
        if (this.marker == START) {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // this.properties.get("id");
            // this.properties.get("keep-together");

            this.marker = 0;
            // initialize id
            String id = this.properties.get("id").getString();
            try {
                area.getIDReferences().initializeID(id, area);
            }
            catch(FOPException e) {
                if (!e.isLocationSet()) {
                    e.setLocation(systemId, line, column);
                }
                throw e;
            }
        }

        /*
         * For calculating the lineage - The fo:list-item-body formatting object
         * does not generate any areas. The fo:list-item-body formatting object
         * returns the sequence of areas created by concatenating the sequences
         * of areas returned by each of the children of the fo:list-item-body.
         */

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FObj fo = (FObj)children.get(i);

            int status;
            if (Status.isIncomplete((status = fo.layout(area)))) {
                this.marker = i;
                if ((i == 0) && (status == Status.AREA_FULL_NONE)) {
                    return Status.AREA_FULL_NONE;
                } else {
                    return Status.AREA_FULL_SOME;
                }
            }
        }
        return Status.OK;
    }

}
