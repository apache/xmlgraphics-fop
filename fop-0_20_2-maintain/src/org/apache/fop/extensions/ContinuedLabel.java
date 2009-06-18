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
package org.apache.fop.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Status;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AreaTree;

/**
 * Implement continued labels for table header/footer.
 * Content of this element must be an fo:inline.
 */
public class ContinuedLabel extends ExtensionObj {

    private FObj containingTable=null;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new ContinuedLabel(parent, propertyList,
                                      systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new ContinuedLabel.Maker();
    }

    public ContinuedLabel(FObj parent, PropertyList propertyList,
                          String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);

        // Find ancestor table
        for (; parent!=null ; parent = parent.getParent()) {
            if (parent.getName().equals("fo:table")) {
                this.containingTable=parent;
                break;
            }
        }
    }


    public String getName() {
        return "fop:continued-label";
    }


    /**
     * If we are within a cell in a table-header or table-footer object
     * and this is not the first generated area for the table, then generate
     * an inline area and put the content in it.
     * @param area The parent area.
     * @return Value indicating where all, some or none of the content
     * was placed in the current parent area.
     */
    public int layout(Area area) throws FOPException {
        if (this.marker == START) {
            this.marker = 0;
        }

        // See if ancestor table has generated any areas yet.
        // Note: areasGenerated was already public so I use it, but this
        // is definitely not very good style!
        if (containingTable!=null && containingTable.areasGenerated > 0) {
            int numChildren = this.children.size();
            for (int i = this.marker; i < numChildren; i++) {
                FONode fo = (FONode)children.get(i);
                int status;
                if (Status.isIncomplete(status = fo.layout(area))) {
                    this.marker = i;
                    return status;
                }
            }
        }
        return Status.OK;
    }

    /**
     * Null implementation.
     */
    public void format(AreaTree areaTree) throws FOPException {
    }

    /**
     * Removes property id from IDReferences.
     * This overrides the generic FObj function since ID has no meaning
     * on a continued-label. However, for now, it propagates to its children
     * since we don't prevent them from creating IDs. This should probably be
     * fixed!
     * @param idReferences the id to remove
     */
    public void removeID(IDReferences idReferences) {
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            FONode child = (FONode)children.get(i);
            if ((child instanceof FObj)) {
                ((FObj)child).removeID(idReferences);
            }
        }
    }
}
