/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.pagination.*;

public class FOElementMapping implements ElementMapping {
    private static HashMap foObjs = null;

    public synchronized void addToBuilder(FOTreeBuilder builder) {

        if(foObjs == null) {
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
            foObjs.put("table-footer", new TF());
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

        String uri = "http://www.w3.org/1999/XSL/Format";
        builder.addMapping(uri, foObjs);
    }

    class R extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Root(parent);
        }
    }

    class Dec extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Declarations(parent);
        }
    }

    class CP extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new ColorProfile(parent);
        }
    }

    class PS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PageSequence(parent);
        }
    }

    class LMS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new LayoutMasterSet(parent);
        }
    }

    class PSM extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PageSequenceMaster(parent);
        }
    }

    class SPMR extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new SinglePageMasterReference(parent);
        }
    }

    class RPMR extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new RepeatablePageMasterReference(parent);
        }
    }

    class RPMA extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new RepeatablePageMasterAlternatives(parent);
        }
    }

    class CPMR extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new ConditionalPageMasterReference(parent);
        }
    }

    class SPM extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new SimplePageMaster(parent);
        }
    }

    class RB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new RegionBody(parent);
        }
    }

    class RBefore extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new RegionBefore(parent);
        }
    }

    class RA extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new RegionAfter(parent);
        }
    }

    class RS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new RegionStart(parent);
        }
    }

    class RE extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new RegionEnd(parent);
        }
    }

    class Fl extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Flow(parent);
        }
    }

    class SC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new StaticContent(parent);
        }
    }

    class T extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Title(parent);
        }
    }

    class B extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Block(parent);
        }
    }

    class BC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new BlockContainer(parent);
        }
    }

    class BO extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new BidiOverride(parent);
        }
    }

    class Ch extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Character(parent);
        }
    }

    class IPS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new InitialPropertySet(parent);
        }
    }

    class EG extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new ExternalGraphic(parent);
        }
    }

    class IFO extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new InstreamForeignObject(parent);
        }
    }

    class In extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Inline(parent);
        }
    }

    class IC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new InlineContainer(parent);
        }
    }

    class L extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Leader(parent);
        }
    }

    class PN extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PageNumber(parent);
        }
    }

    class PNC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PageNumberCitation(parent);
        }
    }

    class TAC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new TableAndCaption(parent);
        }
    }

    class Ta extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Table(parent);
        }
    }

    class TC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new TableColumn(parent);
        }
    }

    class TCaption extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new TableCaption(parent);
        }
    }

    class TH extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new TableHeader(parent);
        }
    }

    class TF extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new TableFooter(parent);
        }
    }

    class TB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new TableBody(parent);
        }
    }

    class TR extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new TableRow(parent);
        }
    }

    class TCell extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new TableCell(parent);
        }
    }

    class LB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new ListBlock(parent);
        }
    }

    class LI extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new ListItem(parent);
        }
    }

    class LIB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new ListItemBody(parent);
        }
    }

    class LIL extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new ListItemLabel(parent);
        }
    }

    class BL extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new BasicLink(parent);
        }
    }

    class MS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new MultiSwitch(parent);
        }
    }

    class MC extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new MultiCase(parent);
        }
    }

    class MT extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new MultiToggle(parent);
        }
    }

    class MP extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new MultiProperties(parent);
        }
    }

    class MPS extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new MultiPropertySet(parent);
        }
    }

    class F extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new org.apache.fop.fo.flow.Float(parent);
        }
    }

    class Foot extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Footnote(parent);
        }
    }

    class FB extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new FootnoteBody(parent);
        }
    }

    class W extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Wrapper(parent);
        }
    }

    class M extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Marker(parent);
        }
    }

    class RM extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new RetrieveMarker(parent);
        }
    }
}
