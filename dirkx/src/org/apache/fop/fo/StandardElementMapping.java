package org.apache.xml.fop.fo;

import org.apache.xml.fop.fo.flow.*;
import org.apache.xml.fop.fo.pagination.*;

public class StandardElementMapping implements ElementMapping {

    public void addToBuilder(FOTreeBuilder builder) {

	String uri = "http://www.w3.org/1999/XSL/Format";

	builder.addMapping(uri, "root", Root.maker()); 
	builder.addMapping(uri, "layout-master-set",
			   LayoutMasterSet.maker()); 
	builder.addMapping(uri, "simple-page-master",
			   SimplePageMaster.maker()); 
	builder.addMapping(uri, "region-body", RegionBody.maker()); 
	builder.addMapping(uri, "region-before", RegionBefore.maker()); 
	builder.addMapping(uri, "region-after", RegionAfter.maker()); 
	builder.addMapping(uri, "page-sequence", PageSequence.maker()); 
	builder.addMapping(uri, "sequence-specification",
			   SequenceSpecification.maker()); 
	builder.addMapping(uri, "sequence-specifier-single",
			   SequenceSpecifierSingle.maker()); 
	builder.addMapping(uri, "sequence-specifier-repeating",
			   SequenceSpecifierRepeating.maker()); 
	builder.addMapping(uri, "sequence-specifier-alternating",
			   SequenceSpecifierAlternating.maker()); 
	builder.addMapping(uri, "flow", Flow.maker()); 
	builder.addMapping(uri, "static-content",
			   StaticContent.maker());
	builder.addMapping(uri, "block", Block.maker()); 
	builder.addMapping(uri, "list-block", ListBlock.maker());
	builder.addMapping(uri, "list-item", ListItem.maker());
	builder.addMapping(uri, "list-item-label",
			   ListItemLabel.maker()); 
	builder.addMapping(uri, "list-item-body", ListItemBody.maker());
	builder.addMapping(uri, "page-number", PageNumber.maker());
	builder.addMapping(uri, "display-sequence",
			   DisplaySequence.maker()); 
	builder.addMapping(uri, "inline-sequence",
			   InlineSequence.maker()); 
	builder.addMapping(uri, "display-rule", DisplayRule.maker()); 
	builder.addMapping(uri, "display-graphic",
			   DisplayGraphic.maker());  
	builder.addMapping(uri, "table", Table.maker());
	builder.addMapping(uri, "table-column", TableColumn.maker());
	builder.addMapping(uri, "table-body", TableBody.maker());
	builder.addMapping(uri, "table-row", TableRow.maker());
	builder.addMapping(uri, "table-cell", TableCell.maker());
    }
}
