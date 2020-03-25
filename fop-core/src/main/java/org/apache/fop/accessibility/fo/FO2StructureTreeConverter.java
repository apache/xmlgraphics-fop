/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.accessibility.fo;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.SAXException;

import org.apache.fop.accessibility.Accessibility;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fo.DelegatingFOEventHandler;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.fo.flow.AbstractRetrieveMarker;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.PageNumberCitationLast;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TableFooter;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;

/**
 * Allows to create the structure tree of an FO document, by converting FO
 * events into appropriate structure tree events.
 */
public class FO2StructureTreeConverter extends DelegatingFOEventHandler {

    /** The top of the {@link converters} stack. */
    protected FOEventHandler converter;

    private Stack<FOEventHandler> converters = new Stack<FOEventHandler>();

    private final StructureTreeEventTrigger structureTreeEventTrigger;

    /** The descendants of some elements like fo:leader must be ignored. */
    private final FOEventHandler eventSwallower = new FOEventHandler() {
    };

    private final Map<AbstractRetrieveMarker, State> states = new HashMap<AbstractRetrieveMarker, State>();

    private static final class State {

        private final FOEventHandler converter;

        private final Stack<FOEventHandler> converters;

        @SuppressWarnings("unchecked")
        State(FO2StructureTreeConverter o) {
            this.converter = o.converter;
            this.converters = (Stack<FOEventHandler>) o.converters.clone();
        }

    }

    private Event root = new Event((Event) null);
    private Event currentNode = root;

    private void startContent(Event event, boolean hasContent) {
        if (getUserAgent().isKeepEmptyTags()) {
            event.run();
        } else {
            Event node = new Event(currentNode);
            event.hasContent = hasContent;
            node.add(event);
            currentNode.add(node);
            currentNode = node;
        }
    }

    private void content(Event event, boolean hasContent) {
        if (getUserAgent().isKeepEmptyTags()) {
            event.run();
        } else {
            currentNode.add(event);
            event.hasContent = hasContent;
        }
    }

    private void endContent(Event event) {
        if (getUserAgent().isKeepEmptyTags()) {
            event.run();
        } else {
            currentNode.add(event);
            currentNode = currentNode.parent;
            if (currentNode == root) {
                root.run();
            }
        }
    }

    /**
     * Creates a new instance.
     *
     * @param structureTreeEventHandler the object that will hold the structure tree
     * @param delegate the FO event handler that must be wrapped by this instance
     */
    public FO2StructureTreeConverter(StructureTreeEventHandler structureTreeEventHandler,
            FOEventHandler delegate) {
        super(delegate);
        this.structureTreeEventTrigger = new StructureTreeEventTrigger(structureTreeEventHandler);
        this.converter = structureTreeEventTrigger;
    }

    @Override
    public void startDocument() throws SAXException {
        converter.startDocument();
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        converter.endDocument();
        super.endDocument();
    }

    @Override
    public void startRoot(Root root) {
       converter.startRoot(root);
       super.startRoot(root);
    }

    @Override
    public void endRoot(Root root) {
        converter.endRoot(root);
        super.endRoot(root);
    }

    @Override
    public void startPageSequence(PageSequence pageSeq) {
        converter.startPageSequence(pageSeq);
        super.startPageSequence(pageSeq);
    }

    @Override
    public void endPageSequence(PageSequence pageSeq) {
        converter.endPageSequence(pageSeq);
        super.endPageSequence(pageSeq);
    }

    @Override
    public void startPageNumber(final PageNumber pagenum) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startPageNumber(pagenum);
            }
        }, true);
        super.startPageNumber(pagenum);
    }

    @Override
    public void endPageNumber(final PageNumber pagenum) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endPageNumber(pagenum);
            }
        });
        super.endPageNumber(pagenum);
    }

    @Override
    public void startPageNumberCitation(final PageNumberCitation pageCite) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startPageNumberCitation(pageCite);
            }
        }, true);
        super.startPageNumberCitation(pageCite);
    }

    @Override
    public void endPageNumberCitation(final PageNumberCitation pageCite) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endPageNumberCitation(pageCite);
            }
        });
        super.endPageNumberCitation(pageCite);
    }

    @Override
    public void startPageNumberCitationLast(final PageNumberCitationLast pageLast) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startPageNumberCitationLast(pageLast);
            }
        }, true);
        super.startPageNumberCitationLast(pageLast);
    }

    @Override
    public void endPageNumberCitationLast(final PageNumberCitationLast pageLast) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endPageNumberCitationLast(pageLast);
            }
        });
        super.endPageNumberCitationLast(pageLast);
    }

    @Override
    public void startStatic(final StaticContent staticContent) {
        handleStartArtifact(staticContent);
        startContent(new Event(this) {
            public void run() {
                eventHandler.startStatic(staticContent);
            }
        }, true);
        super.startStatic(staticContent);
    }

    @Override
    public void endStatic(final StaticContent staticContent) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endStatic(staticContent);
            }
        });
        handleEndArtifact(staticContent);
        super.endStatic(staticContent);
    }

    @Override
    public void startFlow(Flow fl) {
        converter.startFlow(fl);
        super.startFlow(fl);
    }

    @Override
    public void endFlow(Flow fl) {
        converter.endFlow(fl);
        super.endFlow(fl);
    }

    @Override
    public void startBlock(final Block bl) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startBlock(bl);
            }
        }, false);
        super.startBlock(bl);
    }

    @Override
    public void endBlock(final Block bl) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endBlock(bl);
            }
        });
        super.endBlock(bl);
    }

    @Override
    public void startBlockContainer(final BlockContainer blc) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startBlockContainer(blc);
            }
        }, false);
        super.startBlockContainer(blc);
    }

    @Override
    public void endBlockContainer(final BlockContainer blc) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endBlockContainer(blc);
            }
        });
        super.endBlockContainer(blc);
    }

    @Override
    public void startInline(final Inline inl) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startInline(inl);
            }
        }, true);
        super.startInline(inl);
    }

    @Override
    public void endInline(final Inline inl) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endInline(inl);
            }
        });
        super.endInline(inl);
    }

    @Override
    public void startTable(final Table tbl) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startTable(tbl);
            }
        }, true);
        super.startTable(tbl);
    }

    @Override
    public void endTable(final Table tbl) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endTable(tbl);
            }
        });
        super.endTable(tbl);
    }

    @Override
    public void startColumn(final TableColumn tc) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startColumn(tc);
            }
        }, true);
        super.startColumn(tc);
    }

    @Override
    public void endColumn(final TableColumn tc) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endColumn(tc);
            }
        });
        super.endColumn(tc);
    }

    @Override
    public void startHeader(final TableHeader header) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startHeader(header);
            }
        }, true);
        super.startHeader(header);
    }

    @Override
    public void endHeader(final TableHeader header) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endHeader(header);
            }
        });
        super.endHeader(header);
    }

    @Override
    public void startFooter(final TableFooter footer) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startFooter(footer);
            }
        }, true);
        super.startFooter(footer);
    }

    @Override
    public void endFooter(final TableFooter footer) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endFooter(footer);
            }
        });
        super.endFooter(footer);
    }

    @Override
    public void startBody(final TableBody body) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startBody(body);
            }
        }, true);
        super.startBody(body);
    }

    @Override
    public void endBody(final TableBody body) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endBody(body);
            }
        });
        super.endBody(body);
    }

    @Override
    public void startRow(final TableRow tr) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startRow(tr);
            }
        }, true);
        super.startRow(tr);
    }

    @Override
    public void endRow(final TableRow tr) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endRow(tr);
            }
        });
        super.endRow(tr);
    }

    @Override
    public void startCell(final TableCell tc) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startCell(tc);
            }
        }, true);
        super.startCell(tc);
    }

    @Override
    public void endCell(final TableCell tc) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endCell(tc);
            }
        });
        super.endCell(tc);
    }

    @Override
    public void startList(final ListBlock lb) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startList(lb);
            }
        }, true);
        super.startList(lb);
    }

    @Override
    public void endList(final ListBlock lb) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endList(lb);
            }
        });
        super.endList(lb);
    }

    @Override
    public void startListItem(final ListItem li) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startListItem(li);
            }
        }, true);
        super.startListItem(li);
    }

    @Override
    public void endListItem(final ListItem li) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endListItem(li);
            }
        });
        super.endListItem(li);
    }

    @Override
    public void startListLabel(final ListItemLabel listItemLabel) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startListLabel(listItemLabel);
            }
        }, true);
        super.startListLabel(listItemLabel);
    }

    @Override
    public void endListLabel(final ListItemLabel listItemLabel) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endListLabel(listItemLabel);
            }
        });
        super.endListLabel(listItemLabel);
    }

    @Override
    public void startListBody(final ListItemBody listItemBody) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startListBody(listItemBody);
            }
        }, true);
        super.startListBody(listItemBody);
    }

    @Override
    public void endListBody(final ListItemBody listItemBody) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endListBody(listItemBody);
            }
        });
        super.endListBody(listItemBody);
    }

    @Override
    public void startMarkup() {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startMarkup();
            }
        }, true);
        super.startMarkup();
    }

    @Override
    public void endMarkup() {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endMarkup();
            }
        });
        super.endMarkup();
    }

    @Override
    public void startLink(final BasicLink basicLink) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startLink(basicLink);
            }
        }, true);
        super.startLink(basicLink);
    }

    @Override
    public void endLink(final BasicLink basicLink) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endLink(basicLink);
            }
        });
        super.endLink(basicLink);
    }

    @Override
    public void image(final ExternalGraphic eg) {
        content(new Event(this) {
            public void run() {
                eventHandler.image(eg);
            }
        }, true);
        super.image(eg);
    }

    @Override
    public void pageRef() {
        content(new Event(this) {
            public void run() {
                eventHandler.pageRef();
            }
        }, true);
        super.pageRef();
    }

    @Override
    public void startInstreamForeignObject(final InstreamForeignObject ifo) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startInstreamForeignObject(ifo);
            }
        }, true);
        super.startInstreamForeignObject(ifo);
    }

    @Override
    public void endInstreamForeignObject(final InstreamForeignObject ifo) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endInstreamForeignObject(ifo);
            }
        });
        super.endInstreamForeignObject(ifo);
    }

    @Override
    public void startFootnote(final Footnote footnote) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startFootnote(footnote);
            }
        }, true);
        super.startFootnote(footnote);
    }

    @Override
    public void endFootnote(final Footnote footnote) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endFootnote(footnote);
            }
        });
        super.endFootnote(footnote);
    }

    @Override
    public void startFootnoteBody(final FootnoteBody body) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startFootnoteBody(body);
            }
        }, true);
        super.startFootnoteBody(body);
    }

    @Override
    public void endFootnoteBody(final FootnoteBody body) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endFootnoteBody(body);
            }
        });
        super.endFootnoteBody(body);
    }

    @Override
    public void startLeader(final Leader l) {
        converters.push(converter);
        converter = eventSwallower;
        startContent(new Event(this) {
            public void run() {
                eventHandler.startLeader(l);
            }
        }, false);
        super.startLeader(l);
    }

    @Override
    public void endLeader(final Leader l) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endLeader(l);
            }
        });
        converter = converters.pop();
        super.endLeader(l);
    }

    @Override
    public void startWrapper(final Wrapper wrapper) {
        handleStartArtifact(wrapper);
        startContent(new Event(this) {
            public void run() {
                eventHandler.startWrapper(wrapper);
            }
        }, true);
        super.startWrapper(wrapper);
    }

    @Override
    public void endWrapper(final Wrapper wrapper) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endWrapper(wrapper);
            }
        });
        handleEndArtifact(wrapper);
        super.endWrapper(wrapper);
    }

    @Override
    public void startRetrieveMarker(final RetrieveMarker retrieveMarker) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startRetrieveMarker(retrieveMarker);
            }
        }, true);
        saveState(retrieveMarker);
        super.startRetrieveMarker(retrieveMarker);
    }

    private void saveState(AbstractRetrieveMarker retrieveMarker) {
        states.put(retrieveMarker, new State(this));
    }

    @Override
    public void endRetrieveMarker(final RetrieveMarker retrieveMarker) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endRetrieveMarker(retrieveMarker);
            }
        });
        super.endRetrieveMarker(retrieveMarker);
    }

    @Override
    public void restoreState(final RetrieveMarker retrieveMarker) {
        restoreRetrieveMarkerState(retrieveMarker);
        content(new Event(this) {
            public void run() {
                eventHandler.restoreState(retrieveMarker);
            }
        }, true);
        super.restoreState(retrieveMarker);
    }

    @SuppressWarnings("unchecked")
    private void restoreRetrieveMarkerState(AbstractRetrieveMarker retrieveMarker) {
        State state = states.get(retrieveMarker);
        this.converter = state.converter;
        this.converters = (Stack<FOEventHandler>) state.converters.clone();
    }

    @Override
    public void startRetrieveTableMarker(final RetrieveTableMarker retrieveTableMarker) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startRetrieveTableMarker(retrieveTableMarker);
            }
        }, true);
        saveState(retrieveTableMarker);
        super.startRetrieveTableMarker(retrieveTableMarker);
    }

    @Override
    public void endRetrieveTableMarker(final RetrieveTableMarker retrieveTableMarker) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endRetrieveTableMarker(retrieveTableMarker);
            }
        });
        super.endRetrieveTableMarker(retrieveTableMarker);
    }

    @Override
    public void restoreState(final RetrieveTableMarker retrieveTableMarker) {
        restoreRetrieveMarkerState(retrieveTableMarker);
        currentNode.add(new Event(this) {
            public void run() {
                eventHandler.restoreState(retrieveTableMarker);
            }
        });
        super.restoreState(retrieveTableMarker);
    }

    @Override
    public void character(final Character c) {
        content(new Event(this) {
            public void run() {
                eventHandler.character(c);
            }
        }, true);
        super.character(c);
    }

    @Override
    public void characters(final FOText foText) {
        content(new Event(this) {
            public void run() {
                eventHandler.characters(foText);
            }
        }, foText.length() > 0);
        super.characters(foText);
    }

    @Override
    public void startExternalDocument(final ExternalDocument document) {
        startContent(new Event(this) {
            public void run() {
                eventHandler.startExternalDocument(document);
            }
        }, true);
        super.startExternalDocument(document);
    }

    @Override
    public void endExternalDocument(final ExternalDocument document) {
        endContent(new Event(this) {
            public void run() {
                eventHandler.endExternalDocument(document);
            }
        });
        super.endExternalDocument(document);
    }

    private void handleStartArtifact(CommonAccessibilityHolder fobj) {
        if (isArtifact(fobj)) {
            converters.push(converter);
            converter = eventSwallower;
        }
    }

    private void handleEndArtifact(CommonAccessibilityHolder fobj) {
        if (isArtifact(fobj)) {
            converter = converters.pop();
        }
    }

    private boolean isArtifact(CommonAccessibilityHolder fobj) {
        CommonAccessibility accessibility = fobj.getCommonAccessibility();
        return Accessibility.ROLE_ARTIFACT.equalsIgnoreCase(accessibility.getRole());
    }

}
