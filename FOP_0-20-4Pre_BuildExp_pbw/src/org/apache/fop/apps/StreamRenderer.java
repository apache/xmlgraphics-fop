package org.apache.fop.apps;

import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;

import org.xml.sax.SAXException;

import org.apache.fop.layout.FontInfo;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.Title;
import org.apache.fop.render.Renderer;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.extensions.ExtensionObj;
import org.apache.fop.fo.pagination.PageSequence;

import org.apache.avalon.framework.logger.Logger;

/**
  This class acts as a bridge between the XML:FO parser
  and the formatting/rendering classes. It will queue
  PageSequences up until all the IDs required by them
  are satisfied, at which time it will render the
  pages.<P>
 
  StreamRenderer is created by Driver and called from
  FOTreeBuilder when a PageSequence is created,
  and AreaTree when a Page is formatted.<P>
*/
public class StreamRenderer {
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
      The list of pages waiting to be renderered.
    */
    private Vector renderQueue = new Vector();

    /**
      The current set of IDReferences, passed to the
      areatrees and pages. This is used by the AreaTree
      as a single map of all IDs.
    */
    private IDReferences idReferences = new IDReferences();

    /**
     * The list of extensions.
     */
    private Vector extensions = new Vector();

    private Logger log;

    /**
     * The current AreaTree for the PageSequence being rendered.
     */
    private AreaTree areaTree;
    private AreaTree.StorePagesModel atModel;

    public StreamRenderer(OutputStream outputStream, Renderer renderer) {
        this.outputStream = outputStream;
        this.renderer = renderer;

        this.areaTree = new AreaTree();
        this.atModel = AreaTree.createStorePagesModel();
        areaTree.setTreeModel(atModel);
    }

    public void setLogger(Logger logger) {
        log = logger;
    }

    public IDReferences getIDReferences() {
        return idReferences;
    }

    public void addExtension(ExtensionObj ext) {
        extensions.addElement(ext);
    }

    public void startRenderer()
    throws SAXException {
        pageCount = 0;

        if (MEM_PROFILE_WITH_GC)
            System.gc();		// This takes time but gives better results

        initialMemory = runtime.totalMemory() - runtime.freeMemory();
        startTime = System.currentTimeMillis();

        try {
            renderer.setupFontInfo(fontInfo);
            renderer.startRenderer(outputStream);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void stopRenderer()
    throws SAXException {
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
            System.gc();		// This takes time but gives better results

        long memoryNow = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = (memoryNow - initialMemory) / 1024L;

        log.debug("Initial heap size: " + (initialMemory/1024L) + "Kb");
        log.debug("Current heap size: " + (memoryNow/1024L) + "Kb");
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
    public void render(PageSequence pageSequence)
    throws FOPException {
        //areaTree.setFontInfo(fontInfo);

//         for(Enumeration e = extensions.elements(); e.hasMoreElements(); ) {
//             ExtensionObj ext = (ExtensionObj)e.nextElement();
// 	    ext.format(areaTree);
//         }
	pageSequence.format(areaTree);
    }


    private void processAreaTree() throws FOPException {
	int count = 0;
	int seqc = atModel.getPageSequenceCount();
	while (count < seqc) {
	    Title title = atModel.getTitle(count);
	    renderer.startPageSequence(title);
	    int pagec = atModel.getPageCount(count);
	    for (int c=0; c < pagec; c++) {
		try {
		    renderer.renderPage(atModel.getPage(count, c));
		} catch (java.io.IOException ioex) {
		    throw new FOPException("I/O Error rendering page", ioex);
		}
	    }
	    count++;
	}
	
    }

    public FontInfo getFontInfo() {
	return this.fontInfo;
    }

    // COMMENT OUT OLD PAGE MANAGEMENT CODE
//     public synchronized void queuePage(Page page)
//     throws FOPException, IOException {
//         /*
//           Try to optimise on the common case that there are
//           no pages pending and that all ID references are
//           valid on the current pages. This short-cuts the
//           pipeline and renders the area immediately.
//         */
//         if ((renderQueue.size() == 0) && idReferences.isEveryIdValid()) {
//             //renderer.render(page, outputStream);
//         } else {
//             addToRenderQueue(page);
//         }
//         pageCount++;
//     }

//     private synchronized void addToRenderQueue(Page page)
//     throws FOPException, IOException {
//         RenderQueueEntry entry = new RenderQueueEntry(page);
//         renderQueue.addElement(entry);

//         /*
//           The just-added entry could (possibly) resolve the
//           waiting entries, so we try to process the queue
//           now to see.
//         */
//         processQueue(false);
//     }

//     /**
//       Try to process the queue from the first entry forward.
//       If an entry can't be processed, then the queue can't
//       move forward, so return.
//     */
//     private synchronized void processQueue(boolean force)
//     throws FOPException, IOException {
//         while (renderQueue.size() > 0) {
//             RenderQueueEntry entry = (RenderQueueEntry) renderQueue.elementAt(0);
//             if ((!force) && (!entry.isResolved()))
//                 break;

//             //renderer.render(entry.getPage(), outputStream);

//             /* TODO
//             Enumeration rootEnumeration =
//             entry.getAreaTree().getExtensions().elements();
//             while (rootEnumeration.hasMoreElements())
//             renderTree.addExtension((ExtensionObj) rootEnumeration.nextElement());
//             */

//             renderQueue.removeElementAt(0);
//         }
//     }

//     /**
//       A RenderQueueEntry consists of the Page to be queued,
//       plus a list of outstanding ID references that need to be
//       resolved before the Page can be renderered.<P>
//     */
//     class RenderQueueEntry extends Object {
//         /*
//           The Page that has outstanding ID references.
//         */
//         private Page page;

//         /*
//           A list of ID references (names).
//         */
//         private Vector unresolvedIdReferences = new Vector();

//         public RenderQueueEntry(Page page) {
//             this.page = page;

//             Enumeration e = idReferences.getInvalidElements();
//             while (e.hasMoreElements())
//                 unresolvedIdReferences.addElement(e.nextElement());
//         }

//         public Page getPage() {
//             return page;
//         }

//         /**
//           See if the outstanding references are resolved
//           in the current copy of IDReferences.
//         */
//         public boolean isResolved() {
//             if ((unresolvedIdReferences.size() == 0) || idReferences.isEveryIdValid())
//                 return true;

//             //
//             // See if any of the unresolved references are still unresolved.
//             //
//             Enumeration e = unresolvedIdReferences.elements();
//             while (e.hasMoreElements())
//                 if (!idReferences.doesIDExist((String) e.nextElement()))
//                     return false;

//             unresolvedIdReferences.removeAllElements();
//             return true;
//         }
//     }
    
//        public Page getNextPage(Page current, boolean isWithinPageSequence,
//                             boolean isFirstCall) {
//         Page nextPage = null;
//         int pageIndex = 0;
//         if (isFirstCall)
//             pageIndex = renderQueue.size();
//         else
//             pageIndex = renderQueue.indexOf(current);
//         if ((pageIndex + 1) < renderQueue.size()) {
//             nextPage = (Page)renderQueue.elementAt(pageIndex + 1);
//             if (isWithinPageSequence
//                     &&!nextPage.getPageSequence().equals(current.getPageSequence())) {
//                 nextPage = null;
//             }
//         }
//         return nextPage;
//     }

//     public Page getPreviousPage(Page current, boolean isWithinPageSequence,
//                                 boolean isFirstCall) {
//         Page previousPage = null;
//         int pageIndex = 0;
//         if (isFirstCall)
//             pageIndex = renderQueue.size();
//         else
//             pageIndex = renderQueue.indexOf(current);
//         // System.out.println("Page index = " + pageIndex);
//         if ((pageIndex - 1) >= 0) {
//             previousPage = (Page)renderQueue.elementAt(pageIndex - 1);
//             PageSequence currentPS = current.getPageSequence();
//             // System.out.println("Current PS = '" + currentPS + "'");
//             PageSequence previousPS = previousPage.getPageSequence();
//             // System.out.println("Previous PS = '" + previousPS + "'");
//             if (isWithinPageSequence &&!previousPS.equals(currentPS)) {
//                 // System.out.println("Outside page sequence");
//                 previousPage = null;
//             }
//         }
//         return previousPage;
//     }
}
