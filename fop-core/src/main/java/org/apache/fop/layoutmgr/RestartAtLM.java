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
package org.apache.fop.layoutmgr;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.layoutmgr.inline.LineLayoutManager;

/**
 * Class to find the restart layoutmanager for changing IPD
 */
class RestartAtLM {
    protected boolean invalidPosition;
    private Position lineBreakPosition;
    private int positionIndex;

    protected LayoutManager getRestartAtLM(AbstractBreaker breaker, PageBreakingAlgorithm alg,
                                           boolean ipdChangesOnNextPage, boolean onLastPageAndIPDChanges,
                                           boolean visitedBefore, AbstractBreaker.BlockSequence blockList, int start) {
        lineBreakPosition = null;
        BreakingAlgorithm.KnuthNode optimalBreak = ipdChangesOnNextPage ? alg.getBestNodeBeforeIPDChange() : alg
                .getBestNodeForLastPage();
        if (onLastPageAndIPDChanges && visitedBefore && breaker.originalRestartAtLM == null) {
            optimalBreak = null;
        }
        findPositionIndex(breaker, optimalBreak, alg, start);
        if (!breaker.getPageProvider().foUserAgent.isLegacyLastPageChangeIPD()
                && breaker.positionAtBreak.getLM() instanceof BlockLayoutManager) {
            findPositionIndexForBlockLayout(breaker, optimalBreak, alg);
        }
        if (ipdChangesOnNextPage || (breaker.positionAtBreak != null && breaker.positionAtBreak.getIndex() > -1)) {
            breaker.firstElementsForRestart = Collections.EMPTY_LIST;
            if (ipdChangesOnNextPage) {
                if (breaker.containsNonRestartableLM(breaker.positionAtBreak)) {
                    if (alg.getIPDdifference() > 0) {
                        EventBroadcaster eventBroadcaster = breaker.getCurrentChildLM().getFObj()
                                .getUserAgent().getEventBroadcaster();
                        BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider
                                .get(eventBroadcaster);
                        eventProducer.nonRestartableContentFlowingToNarrowerPage(this);
                    }
                    breaker.firstElementsForRestart = new LinkedList<>();
                    boolean boxFound = false;
                    Iterator iter = blockList.listIterator(positionIndex + 1);
                    Position position = null;
                    while (iter.hasNext()
                            && (position == null || breaker.containsNonRestartableLM(position))) {
                        positionIndex++;
                        KnuthElement element = (KnuthElement) iter.next();
                        position = element.getPosition();
                        if (element.isBox()) {
                            boxFound = true;
                            breaker.firstElementsForRestart.add(element);
                        } else if (boxFound) {
                            breaker.firstElementsForRestart.add(element);
                        }
                    }
                    if (position instanceof SpaceResolver.SpaceHandlingBreakPosition) {
                        /* Retrieve the original position wrapped into this space position */
                        breaker.positionAtBreak = position.getPosition();
                    } else {
                        breaker.positionAtBreak = null;
                    }
                }
            }
        }
        LayoutManager restartAtLM = null;
        if (ipdChangesOnNextPage || !(breaker.positionAtBreak != null && breaker.positionAtBreak.getIndex() > -1)) {
            if (breaker.positionAtBreak != null && breaker.positionAtBreak.getIndex() == -1) {
                Position position;
                Iterator iter = blockList.listIterator(positionIndex + 1);
                do {
                    KnuthElement nextElement = (KnuthElement) iter.next();
                    position = nextElement.getPosition();
                } while (position == null
                        || position instanceof SpaceResolver.SpaceHandlingPosition
                        || position instanceof SpaceResolver.SpaceHandlingBreakPosition
                        && position.getPosition().getIndex() == -1);
                LayoutManager surroundingLM = breaker.positionAtBreak.getLM();
                while (position.getLM() != surroundingLM) {
                    position = position.getPosition();
                }
                if (position.getPosition() == null) {
                    if (!breaker.getPageProvider().foUserAgent.isLegacyInvalidBreakPosition()) {
                        breaker.firstElementsForRestart = new LinkedList<>();
                        breaker.positionAtBreak = new LeafPosition(surroundingLM, positionIndex + 1);

                        return surroundingLM;
                    }

                    if (!position.getLM().getFObj().isForceKeepTogether()) {
                        position.getLM().getFObj().setForceKeepTogether(true);
                        invalidPosition = true;
                    }

                    return null;
                }
                restartAtLM = position.getPosition().getLM();
            }
            if (onLastPageAndIPDChanges && restartAtLM != null) {
                if (breaker.originalRestartAtLM == null) {
                    breaker.originalRestartAtLM = restartAtLM;
                } else {
                    restartAtLM = breaker.originalRestartAtLM;
                }
                breaker.firstElementsForRestart = Collections.EMPTY_LIST;
            }
        }
        if (onLastPageAndIPDChanges && !visitedBefore && breaker.positionAtBreak.getPosition() != null) {
            restartAtLM = breaker.positionAtBreak.getPosition().getLM();
        }
        if (lineBreakPosition != null && restartAtLM instanceof BlockLayoutManager) {
            breaker.positionAtBreak = lineBreakPosition;
        }
        return restartAtLM;
    }

    private void findPositionIndex(AbstractBreaker breaker, BreakingAlgorithm.KnuthNode optimalBreak,
                                  PageBreakingAlgorithm alg, int start) {
        positionIndex = (optimalBreak != null) ? optimalBreak.position : start;
        for (int i = positionIndex; i < alg.par.size(); i++) {
            KnuthElement elementAtBreak = alg.getElement(i);
            if (elementAtBreak.getPosition() == null) {
                elementAtBreak = alg.getElement(0);
            }
            breaker.positionAtBreak = elementAtBreak.getPosition();
            /* Retrieve the original position wrapped into this space position */
            breaker.positionAtBreak = breaker.positionAtBreak.getPosition();
            if (breaker.positionAtBreak != null) {
                this.positionIndex = i;
                return;
            }
        }
    }

    private void findPositionIndexForBlockLayout(AbstractBreaker breaker, BreakingAlgorithm.KnuthNode optimalBreak,
                                  PageBreakingAlgorithm alg) {
        int positionIndex = (optimalBreak != null) ? optimalBreak.position : 0;
        for (int i = positionIndex; i < alg.par.size(); i++) {
            KnuthElement elementAtBreak = alg.getElement(i);
            if (elementAtBreak.getPosition() == null) {
                elementAtBreak = alg.getElement(0);
            }
            Position positionAtBreak = elementAtBreak.getPosition();
            /* Retrieve the original position wrapped into this space position */
            positionAtBreak = positionAtBreak.getPosition();
            if (positionAtBreak != null) {
                findLineBreakPosition(positionAtBreak);
                if (lineBreakPosition != null) {
                    breaker.positionAtBreak = positionAtBreak;
                    this.positionIndex = i;
                }
                break;
            }
        }
    }

    private void findLineBreakPosition(Position positionAtBreak) {
        while (positionAtBreak instanceof NonLeafPosition) {
            positionAtBreak = positionAtBreak.getPosition();
            if (positionAtBreak instanceof LineLayoutManager.LineBreakPosition) {
                lineBreakPosition = positionAtBreak;
            }
        }
    }
}
