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

import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.pagination.ColorProfile;
import org.apache.fop.fo.pagination.ConditionalPageMasterReference;
import org.apache.fop.fo.pagination.Declarations;
import org.apache.fop.fo.extensions.ExtensionObj;
import org.apache.fop.fo.extensions.Bookmarks;
import org.apache.fop.fo.extensions.Label;
import org.apache.fop.fo.extensions.Outline;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.flow.BidiOverride;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.pagination.LayoutMasterSet;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.pagination.PageMasterReference;
import org.apache.fop.fo.pagination.RepeatablePageMasterReference;
import org.apache.fop.fo.pagination.SinglePageMasterReference;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.RegionBASE;
import org.apache.fop.fo.pagination.RegionBA;
import org.apache.fop.fo.pagination.RegionAfter;
import org.apache.fop.fo.pagination.RegionBefore;
import org.apache.fop.fo.pagination.RegionSE;
import org.apache.fop.fo.pagination.RegionEnd;
import org.apache.fop.fo.pagination.RegionStart;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.RepeatablePageMasterAlternatives;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableFooter;
import org.apache.fop.fo.flow.TableHeader;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.flow.Float;
import org.apache.fop.fo.flow.InitialPropertySet;
import org.apache.fop.fo.flow.MultiCase;
import org.apache.fop.fo.flow.MultiProperties;
import org.apache.fop.fo.flow.MultiPropertySet;
import org.apache.fop.fo.flow.MultiSwitch;
import org.apache.fop.fo.flow.MultiToggle;
import org.apache.fop.fo.flow.TableAndCaption;
import org.apache.fop.fo.flow.TableCaption;
import org.apache.fop.fo.extensions.svg.SVGObj;
import org.apache.fop.fo.extensions.svg.SVGElement;

/**
 * <p>Implements the GoF Visitor design pattern to allow access to the FOTree
 * hierarchy without knowing what subclass of FONode is being accessed.
 * To preserve inheritance, and simulate the polymorphism that would exist if
 * the subclass methods were embedded in the visited hierarchy, the default for
 * each method (except serveFONode(FONode)) is to run the the serveXXXX method
 * that corresponds to the superclass of the FOTree child element. Thus, any
 * method that is overridden in a subclass of FOTreeVisitor will affect not only
 * FOTree elements of the class that method was written for, but also all
 * subclasses of that class.</p>
 * <p>Caveat: Because there appears to be no way for one
 * object to cast objects from another class to their superclass, this
 * superclass logic has been hard-coded into FOTreeVisitor. So, for example,
 * serveFObj(FObj) runs the following:</p>
 * <pre><code>    serveFONode((FONode)node);</code></pre>
 * <p>If FObj ceases to be a direct subclass of FONode, then the above may cause
 * problems.</p>
 */
public interface FOTreeVisitor {

    /**
     * @param node FONode object to process
     */
    public void serveFONode(FONode node);

    /**
     * @param node FObj object to process
     */
    public void serveFObj(FObj node);

    /**
     * @param node BlockContainer object to process
     */
    public void serveBlockContainer(BlockContainer node);

    /**
     * @param node Character object to process
     */
    public void serveCharacter(Character node);

    /**
     * @param node ColorProfile object to process
     */
    public void serveColorProfile(ColorProfile node);

    /**
     * @param node ConditionalPageMasterReference object to process
     */
    public void serveConditionalPageMasterReference(ConditionalPageMasterReference node);

    /**
     * @param node Declarations object to process
     */
    public void serveDeclarations(Declarations node);

    /**
     * @param node ExtensionObj object to process
     */
    public void serveExtensionObj(ExtensionObj node);

    /**
     * @param node Bookmarks object to process
     */
    public void serveBookmarks(Bookmarks node);

    /**
     * @param node Label object to process
     */
    public void serveLabel(Label node);

    /**
     * @param node Outline object to process
     */
    public void serveOutline(Outline node);

    /**
     * @param node ExternalGraphic object to process
     */
    public void serveExternalGraphic(ExternalGraphic node);

    /**
     * @param node Flow object to process
     */
    public void serveFlow(Flow node);

    /**
     * @param node StaticContent object to process
     */
    public void serveStaticContent(StaticContent node);

    /**
     * @param node FObjMixed object to process
     */
    public void serveFObjMixed(FObjMixed node);

    /**
     * @param node BidiOverride object to process
     */
    public void serveBidiOverride(BidiOverride node);

    /**
     * @param node Block object to process
     */
    public void serveBlock(Block node);

    /**
     * @param node Inline object to process
     */
    public void serveInline(Inline node);

    /**
     * @param node BasicLink object to process
     */
    public void serveBasicLink(BasicLink node);

    /**
     * @param node Leader object to process
     */
    public void serveLeader(Leader node);

    /**
     * @param node Marker object to process
     */
    public void serveMarker(Marker node);

    /**
     * @param node RetrieveMarker object to process
     */
    public void serveRetrieveMarker(RetrieveMarker node);

    /**
     * @param node Title object to process
     */
    public void serveTitle(Title node);

    /**
     * @param node Wrapper object to process
     */
    public void serveWrapper(Wrapper node);

    /**
     * @param node Footnote object to process
     */
    public void serveFootnote(Footnote node);

    /**
     * @param node FootnoteBody object to process
     */
    public void serveFootnoteBody(FootnoteBody node);

    /**
     * @param node FOText object to process
     */
    public void serveFOText(FOText node);

    /**
     * @param node InlineContainer object to process
     */
    public void serveInlineContainer(InlineContainer node);

    /**
     * @param node InstreamForeignObject object to process
     */
    public void serveInstreamForeignObject(InstreamForeignObject node);

    /**
     * @param node LayoutMasterSet object to process
     */
    public void serveLayoutMasterSet(LayoutMasterSet node);

    /**
     * @param node ListBlock object to process
     */
    public void serveListBlock(ListBlock node);

    /**
     * @param node ListItem object to process
     */
    public void serveListItem(ListItem node);

    /**
     * @param node ListItemBody object to process
     */
    public void serveListItemBody(ListItemBody node);

    /**
     * @param node ListItemLabel object to process
     */
    public void serveListItemLabel(ListItemLabel node);

    /**
     * @param node PageMasterReference object to process
     */
    public void servePageMasterReference(PageMasterReference node);

    /**
     * @param node RepeatablePageMasterReference object to process
     */
    public void serveRepeatablePageMasterReference(RepeatablePageMasterReference node);

    /**
     * @param node SinglePageMasterReference object to process
     */
    public void serveSinglePageMasterReference(SinglePageMasterReference node);

    /**
     * @param node PageNumber object to process
     */
    public void servePageNumber(PageNumber node);

    /**
     * @param node PageNumberCitation object to process
     */
    public void servePageNumberCitation(PageNumberCitation node);

    /**
     * @param node PageSequence object to process
     */
    public void servePageSequence(PageSequence node);

    /**
     * @param node PageSequenceMaster object to process
     */
    public void servePageSequenceMaster(PageSequenceMaster node);

    /**
     * @param node Region object to process
     */
    public void serveRegion(Region node);

    /**
     * @param node RegionBASE object to process
     */
    public void serveRegionBASE(RegionBASE node);

    /**
     * @param node RegionBA object to process
     */
    public void serveRegionBA(RegionBA node);

    /**
     * @param node RegionAfter object to process
     */
    public void serveRegionAfter(RegionAfter node);

    /**
     * @param node RegionBefore object to process
     */
    public void serveRegionBefore(RegionBefore node);

    /**
     * @param node RegionSE object to process
     */
    public void serveRegionSE(RegionSE node);

    /**
     * @param node RegionEnd object to process
     */
    public void serveRegionEnd(RegionEnd node);

    /**
     * @param node RegionStart object to process
     */
    public void serveRegionStart(RegionStart node);

    /**
     * @param node RegionBody object to process
     */
    public void serveRegionBody(RegionBody node);

    /**
     * @param node RepeatablePageMasterAlternatives object to process
     */
    public void serveRepeatablePageMasterAlternatives(RepeatablePageMasterAlternatives node);

    /**
     * @param node Root object to process
     */
    public void serveRoot(Root node);

    /**
     * @param node SimplePageMaster object to process
     */
    public void serveSimplePageMaster(SimplePageMaster node);

    /**
     * @param node Table object to process
     */
    public void serveTable(Table node);

    /**
     * @param node TableBody object to process
     */
    public void serveTableBody(TableBody node);

    /**
     * @param node TableFooter object to process
     */
    public void serveTableFooter(TableFooter node);

    /**
     * @param node TableHeader object to process
     */
    public void serveTableHeader(TableHeader node);

    /**
     * @param node TableCell object to process
     */
    public void serveTableCell(TableCell node);

    /**
     * @param node TableColumn object to process
     */
    public void serveTableColumn(TableColumn node);

    /**
     * @param node TableRow object to process
     */
    public void serveTableRow(TableRow node);

    /**
     * @param node ToBeImplementedElement object to process
     */
    public void serveToBeImplementedElement(ToBeImplementedElement node);

    /**
     * @param node Float object to process
     */
    public void serveFloat(Float node);

    /**
     * @param node InitialPropertySet object to process
     */
    public void serveInitialPropertySet(InitialPropertySet node);

    /**
     * @param node MultiCase object to process
     */
    public void serveMultiCase(MultiCase node);

    /**
     * @param node MultiProperties object to process
     */
    public void serveMultiProperties(MultiProperties node);

    /**
     * @param node MultiPropertySet object to process
     */
    public void serveMultiPropertySet(MultiPropertySet node);

    /**
     * @param node MultiSwitch object to process
     */
    public void serveMultiSwitch(MultiSwitch node);

    /**
     * @param node MultiToggle object to process
     */
    public void serveMultiToggle(MultiToggle node);

    /**
     * @param node TableAndCaption object to process
     */
    public void serveTableAndCaption(TableAndCaption node);

    /**
     * @param node TableCaption object to process
     */
    public void serveTableCaption(TableCaption node);

    /**
     * @param node Unknown object to process
     */
    public void serveUnknown(Unknown node);

    /**
     * @param node XMLObj object to process
     */
    public void serveXMLObj(XMLObj node);

    /**
     * @param node SVGObj object to process
     */
    public void serveSVGObj(SVGObj node);

    /**
     * @param node SVGElement object to process
     */
    public void serveSVGElement(SVGElement node);

    /**
     * @param node UnknownXMLObj object to process
     */
    public void serveUnknownXMLObj(UnknownXMLObj node);

    /**
     * @param node XMLElement object to process
     */
    public void serveXMLElement(XMLElement node);

}

