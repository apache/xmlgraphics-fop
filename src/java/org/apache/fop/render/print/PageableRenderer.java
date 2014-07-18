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

package org.apache.fop.render.print;

import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.io.IOException;
import java.util.Map;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.PageViewport;
import org.apache.fop.render.java2d.Java2DRenderer;

/**
 * Renderer that prints through java.awt.PrintJob.
 * The actual printing is handled by Java2DRenderer
 * since both PrintRenderer and AWTRenderer need to
 * support printing.
 */
public class PageableRenderer extends Java2DRenderer implements Pageable {

    /**
     * Printing parameter: the pages to be printed (all, even or odd),
     * datatype: the strings "all", "even" or "odd" or one of PagesMode.*
     */
    public static final String PAGES_MODE = "even-odd";

    /**
     * Printing parameter: the page number (1-based) of the first page to be printed,
     * datatype: a positive Integer
     */
    public static final String START_PAGE = "start-page";

    /**
     * Printing parameter: the page number (1-based) of the last page to be printed,
     * datatype: a positive Integer
     */
    public static final String END_PAGE = "end-page";


    /** first valid page number (1-based) */
    protected int startNumber;
    /** last valid page number (1-based) */
    protected int endNumber = -1;

    /** indicates which pages are valid: odd, even or all */
    protected PagesMode mode = PagesMode.ALL;

    private PageFilter pageFilter;

    /**
     * Creates a new PageableRenderer.
     *
     * @param userAgent the user agent that contains configuration details. This cannot be null.
     */
    public PageableRenderer(FOUserAgent userAgent) {
        super(userAgent);
        Map rendererOptions = getUserAgent().getRendererOptions();
        processOptions(rendererOptions);
        this.pageFilter = new DefaultPageFilter();
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_FOP_PRINT;
    }

    private void processOptions(Map rendererOptions) {
        Object o = rendererOptions.get(PageableRenderer.PAGES_MODE);
        if (o != null) {
            if (o instanceof PagesMode) {
                this.mode = (PagesMode)o;
            } else if (o instanceof String) {
                this.mode = PagesMode.byName((String)o);
            } else {
                throw new IllegalArgumentException(
                        "Renderer option " + PageableRenderer.PAGES_MODE
                        + " must be an 'all', 'even', 'odd' or a PagesMode instance.");
            }
        }

        o = rendererOptions.get(PageableRenderer.START_PAGE);
        if (o != null) {
            this.startNumber = getPositiveInteger(o);
        }
        o = rendererOptions.get(PageableRenderer.END_PAGE);
        if (o != null) {
            this.endNumber = getPositiveInteger(o);
        }
        if (this.endNumber >= 0 && this.endNumber < this.endNumber) {
            this.endNumber = this.startNumber;
        }
    }

    /**
     * Converts an object into a positive integer value if possible. The method throws an
     * {@link IllegalArgumentException} if the value is invalid.
     * @param o the object to be converted
     * @return the positive integer
     */
    protected int getPositiveInteger(Object o) {
        if (o instanceof Integer) {
            Integer i = (Integer)o;
            if (i.intValue() < 1) {
                throw new IllegalArgumentException(
                        "Value must be a positive Integer");
            }
            return i.intValue();
        } else if (o instanceof String) {
            return Integer.parseInt((String)o);
        } else {
            throw new IllegalArgumentException(
                    "Value must be a positive integer");
        }
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        super.stopRenderer();

        if (endNumber == -1) {
            // was not set on command line
            endNumber = getNumberOfPages();
        }
    }

    /** {@inheritDoc} */
    protected void rememberPage(PageViewport pageViewport) {
        if (this.pageFilter.isValid(pageViewport)) {
            super.rememberPage(pageViewport);
        }
    }

    private interface PageFilter {
        boolean isValid(PageViewport page);
    }

    private class DefaultPageFilter implements PageFilter {

        public boolean isValid(PageViewport page) {
            int pageNum = page.getPageIndex() + 1;
            assert pageNum >= 0;
            if (pageNum < startNumber || (endNumber >= 0 && pageNum > endNumber)) {
                return false;
            } else if (mode != PagesMode.ALL) {
                if (mode == PagesMode.EVEN && (pageNum % 2 != 0)) {
                    return false;
                } else if (mode == PagesMode.ODD && (pageNum % 2 == 0)) {
                    return false;
                }
            }
            return true;
        }
    }

    /** {@inheritDoc} */
    public PageFormat getPageFormat(int pageIndex)
            throws IndexOutOfBoundsException {
        try {
            if (pageIndex >= getNumberOfPages()) {
                return null;
            }

            PageFormat pageFormat = new PageFormat();

            Paper paper = new Paper();

            Rectangle2D dim = getPageViewport(pageIndex).getViewArea();
            double width = dim.getWidth();
            double height = dim.getHeight();

            // if the width is greater than the height assume landscape mode
            // and swap the width and height values in the paper format
            if (width > height) {
                paper.setImageableArea(0, 0, height / 1000d, width / 1000d);
                paper.setSize(height / 1000d, width / 1000d);
                pageFormat.setOrientation(PageFormat.LANDSCAPE);
            } else {
                paper.setImageableArea(0, 0, width / 1000d, height / 1000d);
                paper.setSize(width / 1000d, height / 1000d);
                pageFormat.setOrientation(PageFormat.PORTRAIT);
            }
            pageFormat.setPaper(paper);
            return pageFormat;
        } catch (FOPException fopEx) {
            throw new IndexOutOfBoundsException(fopEx.getMessage());
        }
    }

    /** {@inheritDoc} */
    public Printable getPrintable(int pageIndex)
            throws IndexOutOfBoundsException {
        return this;
    }
}
