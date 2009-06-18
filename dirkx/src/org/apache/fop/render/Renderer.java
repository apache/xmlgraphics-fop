package org.apache.xml.fop.render;

// FOP
import org.apache.xml.fop.svg.SVGArea;
import org.apache.xml.fop.image.ImageArea;
import org.apache.xml.fop.layout.*;

// Java
import java.io.PrintWriter;
import java.io.IOException;

/**
 * interface implement by all renderers.
 *
 * a Renderer implementation takes areas/spaces and produces output in
 * some format.
 */
public interface Renderer {
    
    /** set up the given FontInfo */
    public void setupFontInfo(FontInfo fontInfo);

    /** set the producer of the rendering */
    public void setProducer(String producer);

    /** render the given area tree to the given writer */
    public void render(AreaTree areaTree, PrintWriter writer) throws IOException;
 
    /** render the given area container */
    public void renderAreaContainer(AreaContainer area);

    /** render the given block area */
    public void renderBlockArea(BlockArea area);

    /** render the given display space */
    public void renderDisplaySpace(DisplaySpace space);

    /** render the given SVG area */
    public void renderSVGArea(SVGArea area);

    /** render the given image area */
    public void renderImageArea(ImageArea area);

    /** render the given inline area */
    public void renderInlineArea(InlineArea area);

    /** render the given inline space */
    public void renderInlineSpace(InlineSpace space);

    /** render the given line area */
    public void renderLineArea(LineArea area);

    /** render the given page */
    public void renderPage(Page page);

    /** render the given rule area */
    public void renderRuleArea(RuleArea area);
}
