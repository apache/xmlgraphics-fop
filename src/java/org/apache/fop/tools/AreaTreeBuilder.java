/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.tools;

// Java
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.StringTokenizer;

// JAXP
import javax.xml.parsers.DocumentBuilderFactory;

// DOM
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

// Batik
import org.apache.batik.dom.svg.SVGDOMImplementation;

// FOP
import org.apache.fop.area.Area;
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.BeforeFloat;
import org.apache.fop.area.Block;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Flow;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MainReference;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Span;
import org.apache.fop.area.StorePagesModel;
import org.apache.fop.area.Title;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Character;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.pdf.PDFRenderer;
import org.apache.fop.render.svg.SVGRenderer;
import org.apache.fop.render.xml.XMLRenderer;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fonts.FontMetrics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;


/**
 * Area tree tester.
 * The purpose of this class is to create and render an area tree
 * for the purpose of testing the area tree and rendering.
 * This covers the set of possible properties that can be set
 * on the area tree for rendering.
 * As this is not for general purpose there is no attempt to handle
 * invalid area tree xml.
 *
 * Tests: different renderers, saving and loading pages with serialization
 * out of order rendering
 */
public class AreaTreeBuilder {

    /**
     * logging instance
     */
    protected Log logger = null;

    /**
     * Sets the Commons-Logging instance for this class
     * @param logger The Commons-Logging instance
     */
    public void setLogger(Log logger) {
        this.logger = logger;
    }

    /**
     * Returns the Commons-Logging instance for this class
     * @return  The Commons-Logging instance
     */
    protected Log getLogger() {
        return logger;
    }

    /**
     * Main method
     * @param args command line arguments
     */
    public static void main(String[] args) {
        AreaTreeBuilder atb = new AreaTreeBuilder();
        SimpleLog logger = new SimpleLog("FOP");
        logger.setLevel(SimpleLog.LOG_LEVEL_DEBUG);
        atb.setLogger(logger);

        atb.runTests(args[0], args[1], args[2]);
        System.exit(0);
    }

    /**
     * Run the tests.
     * @param in input filename
     * @param type output format
     * @param out output filename
     */
    protected void runTests(String in, String type, String out) {
        logger.debug("Starting tests");
        runTest(in, type, out);
        logger.debug("Finished");
    }

    /**
     * Run a test.
     * @param in input filename
     * @param type output format
     * @param out output filename
     */
    protected void runTest(String in, String type, String out) {
        Renderer rend = null;
        if ("xml".equals(type)) {
            rend = new XMLRenderer();
        } else if ("pdf".equals(type)) {
            rend = new PDFRenderer();
        } else if ("svg".equals(type)) {
            rend = new SVGRenderer();
        }

        FontInfo fontInfo = new FontInfo();
        rend.setupFontInfo(fontInfo);
        FOUserAgent ua = new FOUserAgent();
        rend.setUserAgent(ua);

        StorePagesModel sm = AreaTree.createStorePagesModel();
        TreeLoader tl = new TreeLoader(rend, fontInfo);
        tl.setLogger(logger);
        tl.setTreeModel(sm);
        try {
            InputStream is =
              new java.io.BufferedInputStream(new java.io.FileInputStream(in));
            tl.buildAreaTree(is);
            renderAreaTree(sm, rend, out);
        } catch (IOException e) {
            logger.error("error reading file" + e.getMessage(), e);
        }
    }

    /**
     * Renders an area tree to a target format using a renderer.
     * @param sm area tree pages
     * @param rend renderer to use for output
     * @param out target filename
     */
    protected void renderAreaTree(StorePagesModel sm,
                                  Renderer rend, String out) {
        try {
            OutputStream os =
              new java.io.BufferedOutputStream(new java.io.FileOutputStream(out));

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
                    // save the page to a stream for testing
                    /*ObjectOutputStream tempstream = new ObjectOutputStream(
                                                      new BufferedOutputStream(
                                                        new FileOutputStream("temp.ser")));
                    page.savePage(tempstream);
                    tempstream.close();
                    File temp = new File("temp.ser");
                    getLogger().debug("page serialized to: " + temp.length());
                    temp = null;
                    ObjectInputStream in = new ObjectInputStream(
                                             new BufferedInputStream(
                                               new FileInputStream("temp.ser")));
                    page.loadPage(in);
                    in.close();*/

                    rend.renderPage(page);
                }
                count++;
            }

            rend.stopRenderer();
            os.close();
        } catch (Exception e) {
            logger.error("error rendering output", e);
        }
    }


}

// this loads an area tree from an xml file
// the xml format is the same as the xml renderer output
class TreeLoader {
    private AreaTree areaTree;
    private AreaTreeModel model;
    private Renderer renderer;
    private FontInfo fontInfo;
    private Font currentFontState;
    private Log logger = null;

    TreeLoader(Renderer renderer, FontInfo fontInfo) {
        this.renderer = renderer;
        this.fontInfo = fontInfo;
    }

    /**
     * Sets the Commons-Logging instance for this class
     * @param logger The Commons-Logging instance
     */
    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public void setTreeModel(AreaTreeModel mo) {
        model = mo;
    }

    public void buildAreaTree(InputStream is) {
        Document doc = null;
        try {
            DocumentBuilderFactory fact =
              DocumentBuilderFactory.newInstance();
            fact.setNamespaceAware(true);
            doc = fact.newDocumentBuilder().parse(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Element root = null;
        root = doc.getDocumentElement();

        areaTree = new AreaTree(renderer);
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
        Rectangle2D bounds = getRectangle(root, "bounds");
        PageViewport viewport = null;
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("page")) {
                Page page = readPage((Element) obj);
                viewport = new PageViewport(page, bounds);
            }
        }
        return viewport;
    }

    public Page readPage(Element root) {
        //String bounds = root.getAttribute("bounds");
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

    Rectangle2D getRectangle(Element root, String attr) {
        String rect = root.getAttribute(attr);
        StringTokenizer st = new StringTokenizer(rect, " ");
        int x = 0, y = 0, w = 0, h = 0;
        if (st.hasMoreTokens()) {
            String tok = st.nextToken();
            x = Integer.parseInt(tok);
        }
        if (st.hasMoreTokens()) {
            String tok = st.nextToken();
            y = Integer.parseInt(tok);
        }
        if (st.hasMoreTokens()) {
            String tok = st.nextToken();
            w = Integer.parseInt(tok);
        }
        if (st.hasMoreTokens()) {
            String tok = st.nextToken();
            h = Integer.parseInt(tok);
        }
        Rectangle2D r2d = new Rectangle2D.Float(x, y, w, h);
        return r2d;
    }

    public RegionViewport readRegionViewport(Page page, Element root) {
        RegionViewport reg = new RegionViewport(getRectangle(root, "rect"));
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("regionBefore")) {
                reg.setRegion(readRegion((Element) obj, Region.BEFORE_CODE));
                page.setRegionViewport(Region.BEFORE_CODE, reg);
            } else if (obj.getNodeName().equals("regionStart")) {
                reg.setRegion(readRegion((Element) obj, Region.START_CODE));
                page.setRegionViewport(Region.START_CODE, reg);
            } else if (obj.getNodeName().equals("regionBody")) {
                reg.setRegion(readRegion((Element) obj, Region.BODY_CODE));
                page.setRegionViewport(Region.BODY_CODE, reg);
            } else if (obj.getNodeName().equals("regionEnd")) {
                reg.setRegion(readRegion((Element) obj, Region.END_CODE));
                page.setRegionViewport(Region.END_CODE, reg);
            } else if (obj.getNodeName().equals("regionAfter")) {
                reg.setRegion(readRegion((Element) obj, Region.AFTER_CODE));
                page.setRegionViewport(Region.AFTER_CODE, reg);
            }
        }

        return reg;
    }

    public RegionReference readRegion(Element root, int type) {
        RegionReference reg;
        if (type == Region.BODY_CODE) {
            BodyRegion br = new BodyRegion();
            NodeList childs = root.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node obj = childs.item(i);
                if (obj.getNodeName().equals("beforeFloat")) {
                    BeforeFloat bf = readBeforeFloat((Element) obj);
                    br.setBeforeFloat(bf);
                } else if (obj.getNodeName().equals("mainReference")) {
                    MainReference mr = readMainReference((Element) obj);
                    br.setMainReference(mr);
                } else if (obj.getNodeName().equals("footnote")) {
                    Footnote foot = readFootnote((Element) obj);
                    br.setFootnote(foot);
                }
            }
            reg = br;
        } else {
            reg = new RegionReference(type);
            List blocks = getBlocks(root);
            for (int i = 0; i < blocks.size(); i++) {
                Block obj = (Block) blocks.get(i);
                reg.addBlock(obj);
            }
        }
        reg.setCTM(new CTM());
        return reg;
    }

    public BeforeFloat readBeforeFloat(Element root) {
        BeforeFloat bf = new BeforeFloat();
        List blocks = getBlocks(root);
        for (int i = 0; i < blocks.size(); i++) {
            Block obj = (Block) blocks.get(i);
            bf.addBlock(obj);
        }
        return bf;
    }

    public MainReference readMainReference(Element root) {
        MainReference mr = new MainReference();
        List spans = getSpans(root);
        for (int i = 0; i < spans.size(); i++) {
            Span obj = (Span) spans.get(i);
            mr.addSpan(obj);
        }
        return mr;
    }

    List getSpans(Element root) {
        List list = new java.util.ArrayList();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("span")) {
                List flows = getFlows((Element) obj);
                Span span = new Span(flows.size());
                for (int j = 0; j < flows.size(); j++) {
                    Flow flow = (Flow) flows.get(j);
                    span.addFlow(flow);
                }
                list.add(span);
            }
        }
        return list;
    }

    List getFlows(Element root) {
        List list = new java.util.ArrayList();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("flow")) {
                Flow flow = new Flow();
                List blocks = getBlocks((Element) obj);
                for (int j = 0; j < blocks.size(); j++) {
                    Block block = (Block) blocks.get(j);
                    flow.addBlock(block);
                }
                list.add(flow);
            }
        }
        return list;
    }

    public Footnote readFootnote(Element root) {
        Footnote foot = new Footnote();
        List blocks = getBlocks(root);
        for (int i = 0; i < blocks.size(); i++) {
            Block obj = (Block) blocks.get(i);
            foot.addBlock(obj);
        }
        return foot;
    }


    List getBlocks(Element root) {
        List list = new java.util.ArrayList();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("block")) {
                Block block = new Block();
        addTraits((Element)obj, block);
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
                addTraits((Element) obj, line);
                String height = ((Element) obj).getAttribute("height");
                int h = Integer.parseInt(height);
                line.setHeight(h);

                List inlines = getInlineAreas((Element) obj);
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
        List list = new java.util.ArrayList();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("char")) {
                Character ch =
                  new Character(getString((Element) obj).charAt(0));
                addTraits((Element) obj, ch);
                String fname = fontInfo.fontLookup("sans-serif", "normal", Font.NORMAL);
                FontMetrics metrics = fontInfo.getMetricsFor(fname);
                currentFontState =
                    new Font(fname, metrics, 12000);

                ch.setWidth(currentFontState.getWidth(ch.getChar()));
                ch.setOffset(currentFontState.getCapHeight());
                list.add(ch);
            } else if (obj.getNodeName().equals("space")) {
                Space space = new Space();
                String width = ((Element) obj).getAttribute("width");
                int w = Integer.parseInt(width);
                space.setWidth(w);
                list.add(space);
            } else if (obj.getNodeName().equals("viewport")) {
                Viewport viewport = getViewport((Element) obj);
                if (viewport != null) {
                    list.add(viewport);
                }
            } else if (obj.getNodeName().equals("leader")) {
                Leader leader = getLeader((Element) obj);
                if (leader != null) {
                    list.add(leader);
                }
            } else if (obj.getNodeName().equals("word")) {
                String fname = fontInfo.fontLookup("sans-serif", "normal", Font.NORMAL);
                FontMetrics metrics = fontInfo.getMetricsFor(fname);
                currentFontState =
                    new Font(fname, metrics, 12000);
                TextArea text = getText((Element) obj);

                text.addTrait(Trait.FONT_NAME, fname);
                text.addTrait(Trait.FONT_SIZE, new Integer(12000));

                if (text != null) {
                    list.add(text);
                }
            } else {
            }
        }
        return list;
    }

    Viewport getViewport(Element root) {
        Area child = null;
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("container")) {
                child = getContainer((Element) obj);
            } else if (obj.getNodeName().equals("foreignObject")) {
                child = getForeignObject((Element) obj);
            } else if (obj.getNodeName().equals("image")) {
                child = getImage((Element) obj);
            }
        }
        if (child == null) {
            return null;
        }
        Viewport viewport = new Viewport(child);
        String str = root.getAttribute("width");
        if (str != null && !"".equals(str)) {
            int width = Integer.parseInt(str);
            viewport.setWidth(width);
        }
        return viewport;
    }

    Container getContainer(Element root) {
        Container cont = new Container();
        List blocks = getBlocks(root);
        for (int i = 0; i < blocks.size(); i++) {
            Block obj = (Block) blocks.get(i);
            cont.addBlock(obj);
        }
        return cont;
    }

    ForeignObject getForeignObject(Element root) {
        Document doc;
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj instanceof Element) {
                //getLogger().debug(obj.getNodeName());
                Element rootEle = (Element) obj;
                String space = rootEle.getAttribute("xmlns");
                if (svgNS.equals(space)) {
                    try {
                        DocumentBuilderFactory fact =
                          DocumentBuilderFactory.newInstance();
                        fact.setNamespaceAware(true);

                        doc = fact.newDocumentBuilder().newDocument();
                        Node node = doc.importNode(obj, true);
                        doc.appendChild(node);
                        //DOMImplementation impl =
                        //  SVGDOMImplementation.getDOMImplementation();
                        // due to namespace problem attributes are not cloned
                        // serializing causes an npe
                        //doc = DOMUtilities.deepCloneDocument(doc, impl);

                        ForeignObject fo = new ForeignObject(doc, svgNS);
                        return fo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        DocumentBuilderFactory fact =
                          DocumentBuilderFactory.newInstance();
                        fact.setNamespaceAware(true);
                        doc = fact.newDocumentBuilder().newDocument();
                        Node node = doc.importNode(obj, true);
                        doc.appendChild(node);
                        ForeignObject fo = new ForeignObject(doc, space);
                        return fo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    Image getImage(Element root) {
        String url = root.getAttribute("url");
        Image image = new Image(url);
        return image;
    }

    Leader getLeader(Element root) {
        Leader leader = new Leader();
        String rs = root.getAttribute("ruleStyle");
        if ("solid".equals(rs)) {
            leader.setRuleStyle(Constants.RuleStyle.SOLID);
        } else if ("dotted".equals(rs)) {
            leader.setRuleStyle(Constants.RuleStyle.DOTTED);
        } else if ("dashed".equals(rs)) {
            leader.setRuleStyle(Constants.RuleStyle.DASHED);
        } else if ("double".equals(rs)) {
            leader.setRuleStyle(Constants.RuleStyle.DOUBLE);
        } else if ("groove".equals(rs)) {
            leader.setRuleStyle(Constants.RuleStyle.GROOVE);
        } else if ("ridge".equals(rs)) {
            leader.setRuleStyle(Constants.RuleStyle.RIDGE);
        }
        String rt = root.getAttribute("ruleThickness");
        int thick = Integer.parseInt(rt);
        leader.setRuleThickness(thick);
        rt = root.getAttribute("width");
        if (rt != null && !"".equals(rt)) {
            thick = Integer.parseInt(rt);
            leader.setWidth(thick);
        }
        leader.setOffset(currentFontState.getCapHeight());
        addTraits(root, leader);
        return leader;
    }

    TextArea getText(Element root) {
        String str = getString(root);
        TextArea text = new TextArea();
        text.setTextArea(str);
        addTraits(root, text);
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += currentFontState.getWidth(str.charAt(count));
        }
        text.setWidth(width);
        text.setOffset(currentFontState.getCapHeight());

        return text;
    }


    public void addTraits(Element ele, Area area) {
        String str = ele.getAttribute("props");
        StringTokenizer st = new StringTokenizer(str, ";");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int index = tok.indexOf(":");
            String id = tok.substring(0, index);
            Object traitCode = Trait.getTraitCode(id);
            if (traitCode != null) {
                area.addTrait(traitCode,
                      Trait.makeTraitValue(traitCode,
                               tok.substring(index + 1)));
            } else {
                logger.error("Unknown trait: " + id);
            }
        }
    }

    public List getRanges(Element ele) {
        List list = new java.util.ArrayList();
        String str = ele.getAttribute("ranges");
        StringTokenizer st = new StringTokenizer(str, ";");
        while (st.hasMoreTokens()) {
            /*String tok =*/ st.nextToken();
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

