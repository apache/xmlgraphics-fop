/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools;

import org.apache.fop.apps.*;
import org.apache.fop.configuration.*;
import org.apache.fop.area.*;
import org.apache.fop.area.inline.*;
import org.apache.fop.area.inline.Character;
import org.apache.fop.render.*;
import org.apache.fop.render.pdf.*;
import org.apache.fop.render.svg.*;
import org.apache.fop.render.xml.*;

import org.apache.log.*;
import org.apache.log.format.*;
import org.apache.log.output.io.*;
import org.apache.log.output.*;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * Area tree tester.
 * The purpose of this class is to create and render an area tree
 * for the purpose of testing the area tree and rendering.
 * This covers the set of possible properties that can be set
 * on the area tree for rendering.
 * Tests: different renderers, saving and loading pages with serialization
 * out of order rendering
 */
public class AreaTreeBuilder {
    private Logger log;
    String baseName = "temp";

    /**
     */
    public static void main(String[] args) {
        AreaTreeBuilder atb = new AreaTreeBuilder();

        atb.runTests();
    }

    public AreaTreeBuilder() {
        setupLogging();
    }

    private void setupLogging() {
        Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
        PatternFormatter formatter = new PatternFormatter("[%{priority}]: %{message}\n%{throwable}");

        LogTarget target = null;
        target = new StreamTarget(System.out, formatter);

        hierarchy.setDefaultLogTarget(target);
        log = hierarchy.getLoggerFor("test");
        log.setPriority(Priority.DEBUG);
    }

    /**
     *
     */
    protected void runTests() {
        log.debug("Starting tests");
        runTest();
        log.debug("Finished");
    }

    /**
     */
    protected void runTest() {
        AreaTree.StorePagesModel sm = AreaTree.createStorePagesModel();
        TreeLoader tl = new TreeLoader();
        tl.setTreeModel(sm);
        try {
            InputStream is =
              new BufferedInputStream(new FileInputStream("doc.xml"));
            tl.buildAreaTree(is);
            renderAreaTree(sm);
        } catch (IOException e) {
            log.error("error reading file" + e.getMessage(), e);
        }
    }

    protected void renderAreaTree(AreaTree.StorePagesModel sm) {
        try {
            OutputStream os = new BufferedOutputStream(
                                new FileOutputStream(baseName + ".xml"));

            Renderer rend = new XMLRenderer();
            //Renderer rend = new PDFRenderer();
            rend.setLogger(log);
            rend.startRenderer(os);

            int count = 0;
            int seqc = sm.getPageSequenceCount();
            while (count < seqc) {
                Title title = sm.getTitle(count);
                rend.startPageSequence(title);
                int pagec = sm.getPageCount(count);
                int c = 0;
                while (c < pagec) {
                    PageViewport page = sm.getPage(count, c);
                    c++;
                    rend.renderPage(page);
                }
                count++;
            }

            rend.stopRenderer();
            os.close();
        } catch (Exception e) {
            log.error("error rendering output", e);
        }
    }


}

// this loads an area tree from an xml file
// the xml format is the same as the xml renderer output
class TreeLoader {
    AreaTree areaTree;
    AreaTree.AreaTreeModel model;
    TreeLoader() {

    }

    public void setTreeModel(AreaTree.AreaTreeModel mo) {
        model = mo;
    }

    public void buildAreaTree(InputStream is) {
        Document doc = null;
        try {
            doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().
                  newDocumentBuilder().parse(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Element root = null;
        root = doc.getDocumentElement();

        areaTree = new AreaTree();
        areaTree.setTreeModel(model);

        readAreaTree(root);
    }

    public void readAreaTree(Element root) {

        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("pageSequence")) {
                readPageSequence((Element) obj);
            }
        }

    }

    public void readPageSequence(Element root) {
        Title title = null;
        boolean started = false;
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("title")) {
                if (started) {
                    // problem
                } else {
                    title = readTitle((Element) obj);
                    model.startPageSequence(title);
                    started = true;
                }
            } else if (obj.getNodeName().equals("pageViewport")) {
                if (!started) {
                    model.startPageSequence(null);
                    started = true;
                }
                PageViewport viewport = readPageViewport((Element) obj);
                areaTree.addPage(viewport);
            }
        }
    }

    public Title readTitle(Element root) {
        Title title = new Title();
        List childs = getInlineAreas(root);
        for (int i = 0; i < childs.size(); i++) {
            InlineArea obj = (InlineArea) childs.get(i);
            title.addInlineArea(obj);
        }
        return title;
    }

    public PageViewport readPageViewport(Element root) {
        String bounds = root.getAttribute("bounds");
        PageViewport viewport = null;
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("page")) {
                Page page = readPage((Element) obj);
                viewport = new PageViewport(page);
            }
        }
        return viewport;
    }

    public Page readPage(Element root) {
        String bounds = root.getAttribute("bounds");
        Page page = new Page();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("regionViewport")) {
                readRegionViewport(page, (Element) obj);
            }
        }
        return page;
    }

    public RegionViewport readRegionViewport(Page page, Element root) {
        RegionViewport reg = new RegionViewport();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("regionBefore")) {
                reg.setRegion(readRegion((Element) obj, Region.BEFORE));
                page.setRegion(Region.BEFORE, reg);
            } else if (obj.getNodeName().equals("regionStart")) {
                reg.setRegion(readRegion((Element) obj, Region.START));
                page.setRegion(Region.START, reg);
            } else if (obj.getNodeName().equals("regionBody")) {
                reg.setRegion(readRegion((Element) obj, Region.BODY));
                page.setRegion(Region.BODY, reg);
            } else if (obj.getNodeName().equals("regionEnd")) {
                reg.setRegion(readRegion((Element) obj, Region.END));
                page.setRegion(Region.END, reg);
            } else if (obj.getNodeName().equals("regionAfter")) {
                reg.setRegion(readRegion((Element) obj, Region.AFTER));
                page.setRegion(Region.AFTER, reg);
            }
        }

        return reg;
    }

    public Region readRegion(Element root, int type) {
        Region reg;
        if (type == Region.BODY) {
            reg = new BodyRegion();
        } else {
            reg = new Region(type);
        }
        List blocks = getBlocks(root);
        for (int i = 0; i < blocks.size(); i++) {
            Block obj = (Block) blocks.get(i);
            reg.addBlock(obj);
        }
        return reg;
    }

    List getBlocks(Element root) {
        ArrayList list = new ArrayList();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("block")) {
                Block block = new Block();
                addBlockChildren(block, (Element) obj);
                list.add(block);
            }
        }
        return list;
    }

    protected void addBlockChildren(Block block, Element root) {
        NodeList childs = root.getChildNodes();
        int type = -1;
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("block")) {
                if (type == 2) {
                    // error
                }
                Block b = new Block();
                addBlockChildren(b, (Element) obj);
                block.addBlock(b);
                type = 1;
            } else if (obj.getNodeName().equals("lineArea")) {
                if (type == 1) {
                    // error
                }
                LineArea line = new LineArea();
                List inlines = getInlineAreas((Element)obj);
        for (int j = 0; j < inlines.size(); j++) {
            InlineArea inline = (InlineArea) inlines.get(j);
            line.addInlineArea(inline);
        }

                block.addLineArea(line);
                type = 2;
            }
        }
    }

    // children of element are inline areas
    List getInlineAreas(Element root) {
        ArrayList list = new ArrayList();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("char")) {
                Character ch =
                  new Character(getString((Element) obj).charAt(0));
                list.add(ch);
            } else if (obj.getNodeName().equals("space")) {
                Space space = new Space();
                String width = ((Element) obj).getAttribute("width");
                int w = Integer.parseInt(width);
                space.setWidth(w);
                list.add(space);
            } else if (obj.getNodeName().equals("container")) {
            } else if (obj.getNodeName().equals("viewport")) {
            } else if (obj.getNodeName().equals("leader")) {
            } else {
            }
        }
        return list;
    }

    public String getString(Element ele) {
        String str = "";
        NodeList childs = ele.getChildNodes();
        if (childs.getLength() == 0) {
            return null;
        }
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            str = str + obj.getNodeValue();
        }
        return str;
    }


}

