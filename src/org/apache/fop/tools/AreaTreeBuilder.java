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
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontState;
import org.apache.fop.fo.FOUserAgent;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

import java.io.*;
import java.util.*;

import java.awt.geom.Rectangle2D;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;

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
    private Logger log = new ConsoleLogger(ConsoleLogger.LEVEL_DEBUG);

    /**
     */
    public static void main(String[] args) {
        AreaTreeBuilder atb = new AreaTreeBuilder();

        atb.runTests(args[0], args[1], args[2]);
        System.exit(0);
    }

    /**
     *
     */
    protected void runTests(String in, String type, String out) {
        log.debug("Starting tests");
        runTest(in, type, out);
        log.debug("Finished");
    }

    /**
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
        FontInfo fi = new FontInfo();
        rend.setupFontInfo(fi);
        rend.setUserAgent(new FOUserAgent());

        AreaTree.StorePagesModel sm = AreaTree.createStorePagesModel();
        TreeLoader tl = new TreeLoader(fi);
        tl.setTreeModel(sm);
        try {
            InputStream is =
              new BufferedInputStream(new FileInputStream(in));
            tl.buildAreaTree(is);
            renderAreaTree(sm, rend, out);
        } catch (IOException e) {
            log.error("error reading file" + e.getMessage(), e);
        }
    }

    protected void renderAreaTree(AreaTree.StorePagesModel sm,
                                  Renderer rend, String out) {
        try {
            OutputStream os =
              new BufferedOutputStream(new FileOutputStream(out));

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
                    // save the page to a stream for testing
                    ObjectOutputStream tempstream = new ObjectOutputStream(
                                                      new BufferedOutputStream(
                                                        new FileOutputStream("temp.ser")));
                    page.savePage(tempstream);
                    tempstream.close();
                    File temp = new File("temp.ser");
                    log.debug("page serialized to: " + temp.length());
                    temp = null;
                    ObjectInputStream in = new ObjectInputStream(
                                             new BufferedInputStream(
                                               new FileInputStream("temp.ser")));
                    page.loadPage(in);
                    in.close();

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
    FontInfo fontInfo;
    FontState currentFontState;

    TreeLoader(FontInfo fi) {
        fontInfo = fi;
    }

    public void setTreeModel(AreaTree.AreaTreeModel mo) {
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
                reg.setRegion(readRegion((Element) obj, RegionReference.BEFORE));
                page.setRegion(RegionReference.BEFORE, reg);
            } else if (obj.getNodeName().equals("regionStart")) {
                reg.setRegion(readRegion((Element) obj, RegionReference.START));
                page.setRegion(RegionReference.START, reg);
            } else if (obj.getNodeName().equals("regionBody")) {
                reg.setRegion(readRegion((Element) obj, RegionReference.BODY));
                page.setRegion(RegionReference.BODY, reg);
            } else if (obj.getNodeName().equals("regionEnd")) {
                reg.setRegion(readRegion((Element) obj, RegionReference.END));
                page.setRegion(RegionReference.END, reg);
            } else if (obj.getNodeName().equals("regionAfter")) {
                reg.setRegion(readRegion((Element) obj, RegionReference.AFTER));
                page.setRegion(RegionReference.AFTER, reg);
            }
        }

        return reg;
    }

    public RegionReference readRegion(Element root, int type) {
        RegionReference reg;
        if (type == RegionReference.BODY) {
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
        ArrayList list = new ArrayList();
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
        ArrayList list = new ArrayList();
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
        ArrayList list = new ArrayList();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("block")) {
                Block block = new Block();
                List props = getProperties((Element) obj);
                for (int count = 0; count < props.size(); count++) {
                    block.addTrait((Trait) props.get(count));
                }
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
                List props = getProperties((Element) obj);
                for (int count = 0; count < props.size(); count++) {
                    line.addTrait((Trait) props.get(count));
                }
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
        ArrayList list = new ArrayList();
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node obj = childs.item(i);
            if (obj.getNodeName().equals("char")) {
                Character ch =
                  new Character(getString((Element) obj).charAt(0));
                addProperties((Element) obj, ch);
                try {
                    currentFontState =
                      new FontState(fontInfo, "sans-serif", "normal",
                                    "normal", 12000, 0);
                } catch (FOPException e) {
                    e.printStackTrace();
                }

                ch.setWidth(currentFontState.width(ch.getChar()));
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
                try {
                    currentFontState =
                      new FontState(fontInfo, "sans-serif", "normal",
                                    "normal", 12000, 0);
                } catch (FOPException e) {
                    e.printStackTrace();
                }
                Word word = getWord((Element) obj);
                Trait prop = new Trait();
                prop.propType = Trait.FONT_STATE;
                prop.data = currentFontState;
                word.addTrait(prop);
                if (word != null) {
                    list.add(word);
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
                //System.out.println(obj.getNodeName());
                Element rootEle = (Element) obj;
                String space = rootEle.getAttribute("xmlns");
                if (svgNS.equals(space)) {
                    try {
                        DocumentBuilderFactory fact =
                          DocumentBuilderFactory.newInstance();
                        fact.setNamespaceAware(true);

                        doc = fact. newDocumentBuilder().newDocument();
                        Node node = doc.importNode(obj, true);
                        doc.appendChild(node);
                        DOMImplementation impl =
                          SVGDOMImplementation.getDOMImplementation();
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
                        doc = fact. newDocumentBuilder().newDocument();
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
            leader.setRuleStyle(Leader.SOLID);
        } else if ("dotted".equals(rs)) {
            leader.setRuleStyle(Leader.DOTTED);
        } else if ("dashed".equals(rs)) {
            leader.setRuleStyle(Leader.DASHED);
        } else if ("double".equals(rs)) {
            leader.setRuleStyle(Leader.DOUBLE);
        } else if ("groove".equals(rs)) {
            leader.setRuleStyle(Leader.GROOVE);
        } else if ("ridge".equals(rs)) {
            leader.setRuleStyle(Leader.RIDGE);
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
        addProperties(root, leader);
        return leader;
    }

    Word getWord(Element root) {
        String str = getString(root);
        Word word = new Word();
        word.setWord(str);
        addProperties(root, word);
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += currentFontState.width(str.charAt(count));
        }
        word.setWidth(width);
        word.setOffset(currentFontState.getCapHeight());

        return word;
    }

    public void addProperties(Element ele, InlineArea inline) {
        List props = getProperties(ele);
        for (int count = 0; count < props.size(); count++) {
            inline.addTrait((Trait) props.get(count));
        }
        String str = ele.getAttribute("width");

    }

    public List getProperties(Element ele) {
        ArrayList list = new ArrayList();
        String str = ele.getAttribute("props");
        StringTokenizer st = new StringTokenizer(str, ";");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int index = tok.indexOf(":");
            String id = tok.substring(0, index);
            String val = tok.substring(index + 1);
            Trait prop = new Trait();
            if ("internal-link".equals(id)) {
                prop.propType = Trait.INTERNAL_LINK;
                prop.data = val;
                list.add(prop);
            } else if ("external-link".equals(id)) {
                prop.propType = Trait.EXTERNAL_LINK;
                prop.data = val;
                list.add(prop);
            } else if ("font-family".equals(id)) {
                prop.propType = Trait.FONT_FAMILY;
                prop.data = val;
                list.add(prop);
            } else if ("font-size".equals(id)) {
                prop.propType = Trait.FONT_SIZE;
                prop.data = Integer.valueOf(val);
                list.add(prop);
            } else if ("font-weight".equals(id)) {
                prop.propType = Trait.FONT_WEIGHT;
                prop.data = val;
                list.add(prop);
            } else if ("font-style".equals(id)) {
                prop.propType = Trait.FONT_STYLE;
                prop.data = val;
                list.add(prop);
            } else if ("color".equals(id)) {
                prop.propType = Trait.COLOR;
                prop.data = val;
                list.add(prop);
            } else if ("background".equals(id)) {
                prop.propType = Trait.BACKGROUND;
                prop.data = val;
                list.add(prop);
            } else if ("underline".equals(id)) {
                prop.propType = Trait.UNDERLINE;
                prop.data = new Boolean(val);
                list.add(prop);
            } else if ("overline".equals(id)) {
                prop.propType = Trait.OVERLINE;
                prop.data = new Boolean(val);
                list.add(prop);
            } else if ("linethrough".equals(id)) {
                prop.propType = Trait.LINETHROUGH;
                prop.data = new Boolean(val);
                list.add(prop);
            } else if ("offset".equals(id)) {
                prop.propType = Trait.OFFSET;
                prop.data = Integer.valueOf(val);
                list.add(prop);
            } else if ("shadow".equals(id)) {
                prop.propType = Trait.SHADOW;
                prop.data = val;
                list.add(prop);
            }
        }
        return list;
    }

    public List getRanges(Element ele) {
        ArrayList list = new ArrayList();
        String str = ele.getAttribute("ranges");
        StringTokenizer st = new StringTokenizer(str, ";");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
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

