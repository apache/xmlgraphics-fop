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

package org.apache.fop.fo;

// Java
import java.util.HashMap;

/**
 * Element mapping class for all XSL-FO elements.
 */
public class FOElementMapping extends ElementMapping {

    /**
     * Basic constructor; inititializes the namespace URI for the fo: namespace
     */
    public FOElementMapping() {
        namespaceURI = "http://www.w3.org/1999/XSL/Format";
    }

    /**
     * Initializes the collection of valid objects for the fo: namespace
     */
    protected void initialize() {
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
            foObjs.put("table-header", new TH());
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

    static class R extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.Root(parent);
        }
    }

    static class Dec extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.Declarations(parent);
        }
    }

    static class CP extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.ColorProfile(parent);
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
            return new org.apache.fop.fo.pagination.Flow(parent);
        }
    }

    static class SC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.StaticContent(parent);
        }
    }

    static class T extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.pagination.Title(parent);
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
    
    static class TH extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.TableHeader(parent);
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
