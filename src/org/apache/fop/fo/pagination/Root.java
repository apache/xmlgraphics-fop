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
package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.apps.FOPException;

/**
 * Class modeling the fo:root object. Contains page masters, root extensions,
 * page-sequences.
 *
 * @see <a href="@XSLFO-STD@#fo_root" target="_xslfostd">@XSLFO-STDID@
 *     &para;6.4.2</a>
 */
public class Root extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new Root(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new Root.Maker();
    }

    LayoutMasterSet layoutMasterSet;
    /**
     * Store a page sequence over a sequence change. The next sequence will
     * get the page number from this and also take care of forced pages.
     */
    PageSequence pageSequence;

    protected Root(FObj parent, PropertyList propertyList,
                        String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);
        // this.properties.get("media-usage");

        if (parent != null) {
            throw new FOPException("root must be root element", systemId, line, column);
        }
    }

    public void setPageSequence(PageSequence pageSequence) {
        this.pageSequence=pageSequence;
    }

    public PageSequence getPageSequence() {
        return this.pageSequence;
    }

    public LayoutMasterSet getLayoutMasterSet() {
        return this.layoutMasterSet;
    }

    public void setLayoutMasterSet(LayoutMasterSet layoutMasterSet) {
        this.layoutMasterSet = layoutMasterSet;
    }

    public String getName() {
        return "fo:root";
    }
}
