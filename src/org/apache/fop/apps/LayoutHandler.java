package org.apache.fop.apps;

import java.io.OutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.xml.sax.SAXException;

import org.apache.fop.layout.FontInfo;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.Title;
import org.apache.fop.render.Renderer;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.LayoutMasterSet;

import org.apache.avalon.framework.logger.Logger;

/**
 * Layout handler that receives the structure events.
 * This initiates layout processes and corresponding
 * rendering processes such as start/end.
 */
public class LayoutHandler extends StructureHandler {
    private static final boolean MEM_PROFILE_WITH_GC = false;

    /**
      Somewhere to get our stats from.
     */
    private Runtime runtime = Runtime.getRuntime();

    /**
      Keep track of the number of pages rendered.
     */
    int pageCount = 0;

    /**
      Keep track of heap memory allocated,
      for statistical purposes.
     */
    private long initialMemory;

    /**
      Keep track of time used by renderer.
     */
    private long startTime;

    /**
      The stream to which this rendering is to be
      written to. <B>Note</B> that some renderers
      do not render to a stream, and that this
      member can therefore be null.
     */
    private OutputStream outputStream;

    /**
      The renderer being used.
     */
    private Renderer renderer;

    /**
      The FontInfo for this renderer.
     */
    private FontInfo fontInfo = new FontInfo();

    /**
     * The current AreaTree for the PageSequence being rendered.
     */
    private AreaTree areaTree;
    private AreaTree.StorePagesModel atModel;

    /**
     * @param outputStream the stream that the result is rendered to
     * @param renderer the renderer to call
     * @param store if true then use the store pages model and keep the
     *              area tree in memory
     */
    public LayoutHandler(OutputStream outputStream, Renderer renderer,
                         boolean store) {
        this.outputStream = outputStream;
        this.renderer = renderer;

        this.areaTree = new AreaTree();
        this.atModel = AreaTree.createStorePagesModel();
        areaTree.setTreeModel(atModel);
    }

    public AreaTree getAreaTree() {
        return areaTree;
    }

    public void startDocument() throws SAXException {
        pageCount = 0;

        if (MEM_PROFILE_WITH_GC)
            System.gc(); // This takes time but gives better results

        initialMemory = runtime.totalMemory() - runtime.freeMemory();
        startTime = System.currentTimeMillis();

        try {
            renderer.setupFontInfo(fontInfo);
            // check that the "any,normal,400" font exists
            if(!fontInfo.isSetupValid()) {
                throw new SAXException(new FOPException("no default font defined by OutputConverter"));
            }
            renderer.startRenderer(outputStream);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endDocument() throws SAXException {
        /*
           Force the processing of any more queue elements,
           even if they are not resolved.
         */
        try {
            //processQueue(true);
            processAreaTree();
            renderer.stopRenderer();
        } catch (FOPException e) {
            throw new SAXException(e);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }

        if (MEM_PROFILE_WITH_GC)
            System.gc(); // This takes time but gives better results

        long memoryNow = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = (memoryNow - initialMemory) / 1024L;

        log.debug("Initial heap size: " + (initialMemory / 1024L) + "Kb");
        log.debug("Current heap size: " + (memoryNow / 1024L) + "Kb");
        log.debug("Total memory used: " + memoryUsed + "Kb");

        if (!MEM_PROFILE_WITH_GC) {
            log.debug("  Memory use is indicative; no GC was performed");
            log.debug("  These figures should not be used comparatively");
        }

        long timeUsed = System.currentTimeMillis() - startTime;

        log.debug("Total time used: " + timeUsed + "ms");
        log.debug("Pages rendered: " + pageCount);
        //log.debug("Avg render time: " + (timeUsed / pageCount) + "ms/page");
    }

    public void startPageSequence(PageSequence pageSeq, org.apache.fop.fo.Title seqTitle, LayoutMasterSet lms) {
        Title title = null;
        if(seqTitle != null) {
            title = seqTitle.getTitleArea();
        }
        areaTree.startPageSequence(title);
    }

    /**
       Format the PageSequence. The PageSequence
       formats Pages and adds them to the AreaTree,
       which subsequently calls the StreamRenderer
       instance (this) again to render the page.
       At this time the page might be printed
       or it might be queued. A page might not
       be renderable immediately if the IDReferences
       are not all valid. In this case we defer
       the rendering until they are all valid.
     */
    public void endPageSequence(PageSequence pageSequence)
    throws FOPException {
        //areaTree.setFontInfo(fontInfo);

        pageSequence.format(areaTree);
    }


    private void processAreaTree() throws FOPException {
        int count = 0;
        int seqc = atModel.getPageSequenceCount();
        while (count < seqc) {
            Title title = atModel.getTitle(count);
            renderer.startPageSequence(title);
            int pagec = atModel.getPageCount(count);
            for (int c = 0; c < pagec; c++) {
                try {
                    renderer.renderPage(atModel.getPage(count, c));
                } catch (java.io.IOException ioex) {
                    throw new FOPException("I/O Error rendering page",
                                           ioex);
                }
            }
            count++;
        }

    }

    public FontInfo getFontInfo() {
        return this.fontInfo;
    }
}

