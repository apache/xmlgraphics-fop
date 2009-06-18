/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */

package org.apache.fop.fo;

import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.pagination.*;

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
	builder.addMapping(uri, "block-container", BlockContainer.maker()); 
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
        builder.addMapping(uri, "inline-graphic",
                           InlineGraphic.maker());
	builder.addMapping(uri, "table", Table.maker());
	builder.addMapping(uri, "table-column", TableColumn.maker());
	builder.addMapping(uri, "table-body", TableBody.maker());
	builder.addMapping(uri, "table-row", TableRow.maker());
	builder.addMapping(uri, "table-cell", TableCell.maker());
	builder.addMapping(uri, "simple-link", SimpleLink.maker());
    }
}
