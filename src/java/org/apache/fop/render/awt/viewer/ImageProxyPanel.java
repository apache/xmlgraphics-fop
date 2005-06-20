/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package org.apache.fop.render.awt.viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import javax.swing.JPanel;

import org.apache.fop.apps.FOPException;
import org.apache.fop.render.awt.AWTRenderer;

/**
 * Panel used to display a single page of a document.
 * This is basically a lazy-load display panel which
 * gets the size of the image for layout purposes but
 * doesn't get the actual image data until needed.
 * The image data is then accessed via a soft reference,
 * so it will be garbage collected when moving through
 * large documents.
 */
public class ImageProxyPanel extends JPanel {

    /** The reference to the BufferedImage storing the page data */
    private Reference imageRef;

    /** The maximum and preferred size of the panel */
    private Dimension size;

    /** The renderer. Shared with PreviewPanel and PreviewDialog. */
    private AWTRenderer renderer;

    /** The page to be rendered. */
    private int page;

    /**
     * Panel constructor. Doesn't allocate anything until needed.
     * @param renderer the AWTRenderer instance to use for painting
     * @param page initial page number to show
     */
    public ImageProxyPanel(AWTRenderer renderer, int page) {
        this.renderer = renderer;
        this.page = page;
        // Allows single panel to appear behind page display.
        // Important for textured L&Fs.
        setOpaque(false);
    }

    /**
     * @return the size of the page plus the border.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * @return the size of the page plus the border.
     */
    public Dimension getPreferredSize() {
        if (size == null) {
            try {
                Insets insets = getInsets();
                size = renderer.getPageImageSize(page);
                size = new Dimension(size.width + insets.left + insets.right,
                                                         size.height + insets.top + insets.bottom);
            } catch (FOPException fopEx) {
                // Arbitary size. Doesn't really matter what's returned here.
                return new Dimension(10, 10);
            }
        }
        return size;
    }

    /**
     * Sets the number of the page to be displayed and refreshes the display.
     * @param pg the page number
     */
    public void setPage(int pg) {
        if (page != pg) {
            page = pg;
            imageRef = null;
            repaint();
        }
    }

    /**
     * Gets the image data and paints it on screen. Will make
     * calls to getPageImage as required.
     * @see org.apache.fop.render.java2d.Java2DRenderer#getPageImage()
     */
    public synchronized void paintComponent(Graphics graphics) {
        try {
            if (isOpaque()) { //paint background
                graphics.setColor(getBackground());
                graphics.fillRect(0, 0, getWidth(), getHeight());
            }

            super.paintComponent(graphics);

            BufferedImage image = null;
            if (imageRef == null || imageRef.get() == null) {
                image = renderer.getPageImage(page);
                imageRef = new SoftReference(image);
            } else {
                image = (BufferedImage)imageRef.get();
            }

            int x = (getWidth() - image.getWidth()) / 2;
            int y = (getHeight() - image.getHeight()) / 2;
            
            graphics.drawImage(image, x, y, image.getWidth(), image.getHeight(), null);
        } catch (FOPException fopEx) {
            fopEx.printStackTrace();
        }
    }
}
