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
package org.apache.fop.fo;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.fop.fo.properties.FOPropertyMapping;
import org.apache.fop.fo.flow.Flow;
import org.apache.fop.fo.flow.StaticContent;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.BidiOverride;
import org.apache.fop.fo.flow.InitialPropertySet;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.TableAndCaption;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableCaption;
import org.apache.fop.fo.flow.TableHeader;
import org.apache.fop.fo.flow.TableFooter;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.MultiSwitch;
import org.apache.fop.fo.flow.MultiCase;
import org.apache.fop.fo.flow.MultiToggle;
import org.apache.fop.fo.flow.MultiProperties;
import org.apache.fop.fo.flow.MultiPropertySet;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.LayoutMasterSet;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.SinglePageMasterReference;
import org.apache.fop.fo.pagination.RepeatablePageMasterReference;
import org.apache.fop.fo.pagination.RepeatablePageMasterAlternatives;
import org.apache.fop.fo.pagination.ConditionalPageMasterReference;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.RegionBefore;
import org.apache.fop.fo.pagination.RegionAfter;
import org.apache.fop.fo.pagination.RegionStart;
import org.apache.fop.fo.pagination.RegionEnd;

public class StandardElementMapping implements ElementMapping {
    private static HashMap foObjs = null;

    private static synchronized void setupFO() {

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
    }

    public void addToBuilder(TreeBuilder builder) {
        setupFO();
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

