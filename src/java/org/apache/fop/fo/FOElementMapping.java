/*
 * $Id: FOElementMapping.java,v 1.5 2003/03/05 21:48:02 jeremias Exp $
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
package org.apache.fop.fo;

// Java
import java.util.HashMap;

/**
 * Element mapping class for all XSL-FO elements.
 */
public class FOElementMapping implements ElementMapping {
    
    private static HashMap foObjs = null;

    private static synchronized void setupFO() {
        if (foObjs == null) {
            foObjs = new HashMap();

            // Declarations and Pagination and Layout Formatting Objects
            foObjs.put("root", new R());
            foObjs.put("declarations", new Dec());
            foObjs.put("color-profile", new CP());
            foObjs.put("page-sequence", new PS());
            foObjs.put("layout-master-set", new LMS());
            foObjs.put("page-sequence-master",
                           new PSM());
            foObjs.put("single-page-master-reference",
                           new SPMR());
            foObjs.put("repeatable-page-master-reference",
                           new RPMR());
            foObjs.put("repeatable-page-master-alternatives",
                           new RPMA());
            foObjs.put("conditional-page-master-reference",
                           new CPMR());
            foObjs.put("simple-page-master",
                           new SPM());
            foObjs.put("region-body", new RB());
            foObjs.put("region-before", new RBefore());
            foObjs.put("region-after", new RA());
            foObjs.put("region-start", new RS());
            foObjs.put("region-end", new RE());
            foObjs.put("flow", new Fl());
            foObjs.put("static-content", new SC());
            foObjs.put("title", new T());

            // Block-level Formatting Objects
            foObjs.put("block", new B());
            foObjs.put("block-container", new BC());

            // Inline-level Formatting Objects
            foObjs.put("bidi-override", new BO());
            foObjs.put("character",
                           new Ch());
            foObjs.put("initial-property-set",
                           new IPS());
            foObjs.put("external-graphic", new EG());
            foObjs.put("instream-foreign-object",
                           new IFO());
            foObjs.put("inline", new In());
            foObjs.put("inline-container", new IC());
            foObjs.put("leader", new L());
            foObjs.put("page-number", new PN());
            foObjs.put("page-number-citation",
                           new PNC());

            // Formatting Objects for Tables
            foObjs.put("table-and-caption", new TAC());
            foObjs.put("table", new Ta());
            foObjs.put("table-column", new TC());
            foObjs.put("table-caption", new TCaption());
            foObjs.put("table-header", new TB());
            foObjs.put("table-footer", new TB());
            foObjs.put("table-body", new TB());
            foObjs.put("table-row", new TR());
            foObjs.put("table-cell", new TCell());

            // Formatting Objects for Lists
            foObjs.put("list-block", new LB());
            foObjs.put("list-item", new LI());
            foObjs.put("list-item-body", new LIB());
            foObjs.put("list-item-label", new LIL());

            // Dynamic Effects: Link and Multi Formatting Objects
            foObjs.put("basic-link", new BL());
            foObjs.put("multi-switch", new MS());
            foObjs.put("multi-case", new MC());
            foObjs.put("multi-toggle", new MT());
            foObjs.put("multi-properties", new MP());
            foObjs.put("multi-property-set",
                           new MPS());

            // Out-of-Line Formatting Objects
            foObjs.put("float",
                           new F());
            foObjs.put("footnote", new Foot());
            foObjs.put("footnote-body", new FB());

            // Other Formatting Objects
            foObjs.put("wrapper", new W());
            foObjs.put("marker", new M());
            foObjs.put("retrieve-marker", new RM());
        }

    }

    /**
     * @see org.apache.fop.fo.ElementMapping#addToBuilder(FOTreeBuilder)
     */
    public void addToBuilder(FOTreeBuilder builder) {
        setupFO();
        String uri = "http://www.w3.org/1999/XSL/Format";
        builder.addMapping(uri, foObjs);
    }

    static class R extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.Root(parent);
        }
    }

    static class Dec extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Declarations(parent);
        }
    }

    static class CP extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new ColorProfile(parent);
        }
    }

    static class PS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.PageSequence(parent);
        }
    }

    static class LMS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.LayoutMasterSet(parent);
        }
    }

    static class PSM extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.PageSequenceMaster(parent);
        }
    }

    static class SPMR extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.SinglePageMasterReference(parent);
        }
    }

    static class RPMR extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RepeatablePageMasterReference(parent);
        }
    }

    static class RPMA extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RepeatablePageMasterAlternatives(parent);
        }
    }

    static class CPMR extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.ConditionalPageMasterReference(parent);
        }
    }

    static class SPM extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.SimplePageMaster(parent);
        }
    }

    static class RB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionBody(parent);
        }
    }

    static class RBefore extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionBefore(parent);
        }
    }

    static class RA extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionAfter(parent);
        }
    }

    static class RS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionStart(parent);
        }
    }

    static class RE extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.RegionEnd(parent);
        }
    }

    static class Fl extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Flow(parent);
        }
    }

    static class SC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.StaticContent(parent);
        }
    }

    static class T extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Title(parent);
        }
    }

    static class B extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Block(parent);
        }
    }

    static class BC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.BlockContainer(parent);
        }
    }

    static class BO extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.BidiOverride(parent);
        }
    }

    static class Ch extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Character(parent);
        }
    }

    static class IPS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.InitialPropertySet(parent);
        }
    }

    static class EG extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ExternalGraphic(parent);
        }
    }

    static class IFO extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.InstreamForeignObject(parent);
        }
    }

    static class In extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Inline(parent);
        }
    }

    static class IC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.InlineContainer(parent);
        }
    }

    static class L extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Leader(parent);
        }
    }

    static class PN extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.PageNumber(parent);
        }
    }

    static class PNC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.PageNumberCitation(parent);
        }
    }

    static class TAC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.TableAndCaption(parent);
        }
    }

    static class Ta extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Table(parent);
        }
    }

    static class TC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.TableColumn(parent);
        }
    }

    static class TCaption extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.TableCaption(parent);
        }
    }

    static class TB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.TableBody(parent);
        }
    }

    static class TR extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.TableRow(parent);
        }
    }

    static class TCell extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.TableCell(parent);
        }
    }

    static class LB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ListBlock(parent);
        }
    }

    static class LI extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ListItem(parent);
        }
    }

    static class LIB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ListItemBody(parent);
        }
    }

    static class LIL extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.ListItemLabel(parent);
        }
    }

    static class BL extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.BasicLink(parent);
        }
    }

    static class MS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiSwitch(parent);
        }
    }

    static class MC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiCase(parent);
        }
    }

    static class MT extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiToggle(parent);
        }
    }

    static class MP extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiProperties(parent);
        }
    }

    static class MPS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.MultiPropertySet(parent);
        }
    }

    static class F extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Float(parent);
        }
    }

    static class Foot extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Footnote(parent);
        }
    }

    static class FB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.FootnoteBody(parent);
        }
    }

    static class W extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Wrapper(parent);
        }
    }

    static class M extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Marker(parent);
        }
    }

    static class RM extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.RetrieveMarker(parent);
        }
    }
}
