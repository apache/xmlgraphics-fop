/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.fop.fo.properties.FOPropertyMapping;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.pagination.*;

public class StandardElementMapping implements ElementMapping {
    private static HashMap foObjs = null;

    public synchronized void addToBuilder(TreeBuilder builder) {

        if(foObjs == null) {
            foObjs = new HashMap();

            // Declarations and Pagination and Layout Formatting Objects
            foObjs.put("root", Root.maker());
            foObjs.put("declarations", Declarations.maker());
            foObjs.put("color-profile", ColorProfile.maker());
            foObjs.put("page-sequence", PageSequence.maker());
            foObjs.put("layout-master-set", LayoutMasterSet.maker());
            foObjs.put("page-sequence-master",
                           PageSequenceMaster.maker());
            foObjs.put("single-page-master-reference",
                           SinglePageMasterReference.maker());
            foObjs.put("repeatable-page-master-reference",
                           RepeatablePageMasterReference.maker());
            foObjs.put("repeatable-page-master-alternatives",
                           RepeatablePageMasterAlternatives.maker());
            foObjs.put("conditional-page-master-reference",
                           ConditionalPageMasterReference.maker());
            foObjs.put("simple-page-master",
                           SimplePageMaster.maker());
            foObjs.put("region-body", RegionBody.maker());
            foObjs.put("region-before", RegionBefore.maker());
            foObjs.put("region-after", RegionAfter.maker());
            foObjs.put("region-start", RegionStart.maker());
            foObjs.put("region-end", RegionEnd.maker());
            foObjs.put("flow", Flow.maker());
            foObjs.put("static-content", StaticContent.maker());
            foObjs.put("title", Title.maker());

            // Block-level Formatting Objects
            foObjs.put("block", Block.maker());
            foObjs.put("block-container", BlockContainer.maker());

            // Inline-level Formatting Objects
            foObjs.put("bidi-override", BidiOverride.maker());
            foObjs.put("character",
                           org.apache.fop.fo.flow.Character.maker());
            foObjs.put("initial-property-set",
                           InitialPropertySet.maker());
            foObjs.put("external-graphic", ExternalGraphic.maker());
            foObjs.put("instream-foreign-object",
                           InstreamForeignObject.maker());
            foObjs.put("inline", Inline.maker());
            foObjs.put("inline-container", InlineContainer.maker());
            foObjs.put("leader", Leader.maker());
            foObjs.put("page-number", PageNumber.maker());
            foObjs.put("page-number-citation",
                           PageNumberCitation.maker());

            // Formatting Objects for Tables
            foObjs.put("table-and-caption", TableAndCaption.maker());
            foObjs.put("table", Table.maker());
            foObjs.put("table-column", TableColumn.maker());
            foObjs.put("table-caption", TableCaption.maker());
            foObjs.put("table-header", TableHeader.maker());
            foObjs.put("table-footer", TableFooter.maker());
            foObjs.put("table-body", TableBody.maker());
            foObjs.put("table-row", TableRow.maker());
            foObjs.put("table-cell", TableCell.maker());

            // Formatting Objects for Lists
            foObjs.put("list-block", ListBlock.maker());
            foObjs.put("list-item", ListItem.maker());
            foObjs.put("list-item-body", ListItemBody.maker());
            foObjs.put("list-item-label", ListItemLabel.maker());

            // Dynamic Effects: Link and Multi Formatting Objects
            foObjs.put("basic-link", BasicLink.maker());
            foObjs.put("multi-switch", MultiSwitch.maker());
            foObjs.put("multi-case", MultiCase.maker());
            foObjs.put("multi-toggle", MultiToggle.maker());
            foObjs.put("multi-properties", MultiProperties.maker());
            foObjs.put("multi-property-set",
                           MultiPropertySet.maker());

            // Out-of-Line Formatting Objects
            foObjs.put("float",
                           org.apache.fop.fo.flow.Float.maker());
            foObjs.put("footnote", Footnote.maker());
            foObjs.put("footnote-body", FootnoteBody.maker());

            // Other Formatting Objects
            foObjs.put("wrapper", Wrapper.maker());
            foObjs.put("marker", Marker.maker());
            foObjs.put("retrieve-marker", RetrieveMarker.maker());
        }

        String uri = "http://www.w3.org/1999/XSL/Format";
        builder.addMapping(uri, foObjs);

        builder.addPropertyList(uri, FOPropertyMapping.getGenericMappings());
        /* Add any element mappings */
        for (Iterator iter = FOPropertyMapping.getElementMappings().iterator();
                iter.hasNext(); ) {
            String elem = (String)iter.next();
            builder.addElementPropertyList(uri, elem,
                                           FOPropertyMapping.getElementMapping(elem));
        }

    }

}
