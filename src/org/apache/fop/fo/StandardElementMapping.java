/*-- $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.fop.fo.properties.FOPropertyMapping;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.pagination.*;

public class StandardElementMapping implements ElementMapping {

    public void addToBuilder(TreeBuilder builder) {

        String uri = "http://www.w3.org/1999/XSL/Format";

        builder.addMapping(uri, "root", Root.maker());
        builder.addMapping(uri, "layout-master-set",
                           LayoutMasterSet.maker());
        builder.addMapping(uri, "simple-page-master",
                           SimplePageMaster.maker());
        builder.addMapping(uri, "region-body", RegionBody.maker());
        builder.addMapping(uri, "region-before", RegionBefore.maker());
        builder.addMapping(uri, "region-after", RegionAfter.maker());
        builder.addMapping(uri, "region-start", RegionStart.maker());
        builder.addMapping(uri, "region-end", RegionEnd.maker());
        builder.addMapping(uri, "page-sequence", PageSequence.maker());
        builder.addMapping(uri, "page-sequence-master",
                           PageSequenceMaster.maker());
        builder.addMapping(uri, "single-page-master-reference",
                           SinglePageMasterReference.maker());
        builder.addMapping(uri, "repeatable-page-master-reference",
                           RepeatablePageMasterReference.maker());
        builder.addMapping(uri, "conditional-page-master-reference",
                           ConditionalPageMasterReference.maker());
        builder.addMapping(uri, "repeatable-page-master-alternatives",
                           RepeatablePageMasterAlternatives.maker());
        builder.addMapping(uri, "flow", Flow.maker());
        builder.addMapping(uri, "static-content", StaticContent.maker());
        builder.addMapping(uri, "block", Block.maker());
        builder.addMapping(uri, "block-container", BlockContainer.maker());
        builder.addMapping(uri, "list-block", ListBlock.maker());
        builder.addMapping(uri, "list-item", ListItem.maker());
        builder.addMapping(uri, "list-item-label", ListItemLabel.maker());
        builder.addMapping(uri, "list-item-body", ListItemBody.maker());
        builder.addMapping(uri, "page-number", PageNumber.maker());
        builder.addMapping(uri, "page-number-citation",
                           PageNumberCitation.maker());
        builder.addMapping(uri, "display-sequence",
                           DisplaySequence.maker());
        builder.addMapping(uri, "inline", Inline.maker());
        builder.addMapping(uri, "external-graphic",
                           ExternalGraphic.maker());
        builder.addMapping(uri, "table", Table.maker());
        builder.addMapping(uri, "table-column", TableColumn.maker());
        builder.addMapping(uri, "table-header", TableHeader.maker());
        builder.addMapping(uri, "table-body", TableBody.maker());
        builder.addMapping(uri, "table-footer", TableFooter.maker());
        builder.addMapping(uri, "table-row", TableRow.maker());
        builder.addMapping(uri, "table-cell", TableCell.maker());
        builder.addMapping(uri, "basic-link", BasicLink.maker());
        builder.addMapping(uri, "instream-foreign-object",
                           InstreamForeignObject.maker());
        builder.addMapping(uri, "leader", Leader.maker());
        builder.addMapping(uri, "character",
                           org.apache.fop.fo.flow.Character.maker());
        builder.addMapping(uri, "footnote", Footnote.maker());
        builder.addMapping(uri, "footnote-body", FootnoteBody.maker());
        builder.addMapping(uri, "wrapper", Wrapper.maker());


        builder.addPropertyList(uri,
                                FOPropertyMapping.getGenericMappings());
        /* Add any element mappings */
        for (Enumeration e = FOPropertyMapping.getElementMappings();
                e.hasMoreElements();) {
            String elem = (String) e.nextElement();
            builder.addElementPropertyList(uri, elem,
                                           FOPropertyMapping.getElementMapping(elem));
        }

    }
}
