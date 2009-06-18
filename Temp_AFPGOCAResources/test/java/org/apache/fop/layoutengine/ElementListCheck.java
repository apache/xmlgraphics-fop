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

package org.apache.fop.layoutengine;

import java.util.Iterator;
import java.util.List;

import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Check implementation that checks a Knuth element list.
 */
public class ElementListCheck implements LayoutEngineCheck {

    private String category;
    private String id;
    private int index = -1;
    private Element checkElement;

    /**
     * Creates a new instance from a DOM node.
     * @param node DOM node that defines this check
     */
    public ElementListCheck(Node node) {
        this.category = node.getAttributes().getNamedItem("category").getNodeValue();
        if (node.getAttributes().getNamedItem("id") != null) {
            this.id = node.getAttributes().getNamedItem("id").getNodeValue();
        }
        if (!haveID()) {
            if (node.getAttributes().getNamedItem("index") != null) {
                String s = node.getAttributes().getNamedItem("index").getNodeValue();
                this.index = Integer.parseInt(s);
            }
        }
        this.checkElement = (Element)node;
    }

    /**
     * @see org.apache.fop.layoutengine.LayoutEngineCheck
     */
    public void check(LayoutResult result) {
        ElementListCollector.ElementList elementList = findElementList(result);
        NodeList children = checkElement.getChildNodes();
        int pos = -1;
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                pos++;
                Element domEl = (Element)node;
                KnuthElement knuthEl = (KnuthElement)elementList.getElementList().get(pos);
                if ("skip".equals(domEl.getLocalName())) {
                    pos += Integer.parseInt(getElementText(domEl)) - 1;
                } else if ("box".equals(domEl.getLocalName())) {
                    if (!(knuthEl instanceof KnuthBox)) {
                        fail("Expected KnuthBox"
                                + " at position " + pos
                                + " but got: " + knuthEl.getClass().getName());
                    }
                    if (domEl.getAttribute("w").length() > 0) {
                        int w = Integer.parseInt(domEl.getAttribute("w"));
                        if (w != knuthEl.getW()) {
                            fail("Expected w=" + w
                                    + " at position " + pos
                                    + " but got: " + knuthEl.getW());
                        }
                    }
                    if ("true".equals(domEl.getAttribute("aux"))) {
                        if (!knuthEl.isAuxiliary()) {
                            fail("Expected auxiliary box"
                                    + " at position " + pos);
                        }
                    }
                    if ("false".equals(domEl.getAttribute("aux"))) {
                        if (knuthEl.isAuxiliary()) {
                            fail("Expected a normal, not an auxiliary box"
                                    + " at position " + pos);
                        }
                    }
                } else if ("penalty".equals(domEl.getLocalName())) {
                    if (!(knuthEl instanceof KnuthPenalty)) {
                        fail("Expected KnuthPenalty "
                                + " at position " + pos
                                + " but got: " + knuthEl.getClass().getName());
                    }
                    KnuthPenalty pen = (KnuthPenalty)knuthEl;
                    if (domEl.getAttribute("w").length() > 0) {
                        int w = Integer.parseInt(domEl.getAttribute("w"));
                        if (w != knuthEl.getW()) {
                            fail("Expected w=" + w
                                    + " at position " + pos
                                    + " but got: " + knuthEl.getW());
                        }
                    }
                    if (domEl.getAttribute("p").length() > 0) {
                        if ("<0".equals(domEl.getAttribute("p"))) {
                            if (knuthEl.getP() >= 0) {
                                fail("Expected p<0"
                                        + " at position " + pos
                                        + " but got: " + knuthEl.getP());
                            }
                        } else if (">0".equals(domEl.getAttribute("p"))) {
                            if (knuthEl.getP() <= 0) {
                                fail("Expected p>0"
                                        + " at position " + pos
                                        + " but got: " + knuthEl.getP());
                            }
                        } else {
                            int p;
                            if ("INF".equalsIgnoreCase(domEl.getAttribute("p"))) {
                                p = KnuthPenalty.INFINITE;
                            } else if ("INFINITE".equalsIgnoreCase(domEl.getAttribute("p"))) {
                                p = KnuthPenalty.INFINITE;
                            } else if ("-INF".equalsIgnoreCase(domEl.getAttribute("p"))) {
                                p = -KnuthPenalty.INFINITE;
                            } else if ("-INFINITE".equalsIgnoreCase(domEl.getAttribute("p"))) {
                                p = -KnuthPenalty.INFINITE;
                            } else {
                                p = Integer.parseInt(domEl.getAttribute("p"));
                            }
                            if (p != knuthEl.getP()) {
                                fail("Expected p=" + p
                                        + " at position " + pos
                                        + " but got: " + knuthEl.getP());
                            }
                        }
                    }
                    if ("true".equals(domEl.getAttribute("flagged"))) {
                        if (!pen.isFlagged()) {
                            fail("Expected flagged penalty"
                                    + " at position " + pos);
                        }
                    } else if ("false".equals(domEl.getAttribute("flagged"))) {
                        if (pen.isFlagged()) {
                            fail("Expected non-flagged penalty"
                                    + " at position " + pos);
                        }
                    }
                    if ("true".equals(domEl.getAttribute("aux"))) {
                        if (!pen.isAuxiliary()) {
                            fail("Expected auxiliary penalty"
                                    + " at position " + pos);
                        }
                    } else if ("false".equals(domEl.getAttribute("aux"))) {
                        if (pen.isAuxiliary()) {
                            fail("Expected non-auxiliary penalty"
                                    + " at position " + pos);
                        }
                    }
                } else if ("glue".equals(domEl.getLocalName())) {
                    if (!(knuthEl instanceof KnuthGlue)) {
                        fail("Expected KnuthGlue"
                                + " at position " + pos
                                + " but got: " + knuthEl.getClass().getName());
                    }
                    KnuthGlue glue = (KnuthGlue)knuthEl;
                    if (domEl.getAttribute("w").length() > 0) {
                        int w = Integer.parseInt(domEl.getAttribute("w"));
                        if (w != knuthEl.getW()) {
                            fail("Expected w=" + w
                                    + " at position " + pos
                                    + " but got: " + knuthEl.getW());
                        }
                    }
                    if (domEl.getAttribute("y").length() > 0) {
                        int stretch = Integer.parseInt(domEl.getAttribute("y"));
                        if (stretch != knuthEl.getY()) {
                            fail("Expected y=" + stretch
                                    + " (stretch) at position " + pos
                                    + " but got: " + knuthEl.getY());
                        }
                    }
                    if (domEl.getAttribute("z").length() > 0) {
                        int shrink = Integer.parseInt(domEl.getAttribute("z"));
                        if (shrink != knuthEl.getZ()) {
                            fail("Expected z=" + shrink
                                    + " (shrink) at position " + pos
                                    + " but got: " + knuthEl.getZ());
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Invalid child node for 'element-list': "
                            + domEl.getLocalName()
                            + " at position " + pos + " (" + this + ")");
                }

            }
        }
        pos++;
        if (elementList.getElementList().size() > pos) {
            fail("There are "
                    + (elementList.getElementList().size() - pos)
                    + " unchecked elements at the end of the list");
        }
    }

    private void fail(String msg) {
        throw new RuntimeException(msg + " (" + this + ")");
    }

    private boolean haveID() {
        return (this.id != null && this.id.length() > 0);
    }

    private ElementListCollector.ElementList findElementList(LayoutResult result) {
        List candidates = new java.util.ArrayList();
        Iterator iter = result.getElementListCollector().getElementLists().iterator();
        while (iter.hasNext()) {
            ElementListCollector.ElementList el = (ElementListCollector.ElementList)iter.next();
            if (el.getCategory().equals(category)) {
                if (haveID() && this.id.equals(el.getID())) {
                    candidates.add(el);
                    break;
                } else if (!haveID()) {
                    candidates.add(el);
                }
            }
        }
        if (candidates.size() == 0) {
            throw new ArrayIndexOutOfBoundsException("Requested element list not found");
        } else if (index >= 0) {
            return (ElementListCollector.ElementList)candidates.get(index);
        } else {
            return (ElementListCollector.ElementList)candidates.get(0);
        }
    }

    private static String getElementText(Element el) {
        StringBuffer sb = new StringBuffer();
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Text) {
                sb.append(((Text)node).getData());
            } else if (node instanceof CDATASection) {
                sb.append(((CDATASection)node).getData());
            }
        }
        return sb.toString();
    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer("element-list");
        sb.append(" category=").append(category);
        if (haveID()) {
            sb.append(" id=").append(id);
        } else if (index >= 0) {
            sb.append(" index=").append(index);
        }
        return sb.toString();
    }
}
