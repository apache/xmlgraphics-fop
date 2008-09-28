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

package org.apache.fop.render.pcl;

import java.awt.Dimension;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;

/**
 * {@code IFDocumentHandler} implementation that produces PCL 5.
 */
public class PCLDocumentHandler extends AbstractBinaryWritingIFDocumentHandler
            implements PCLConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(PCLDocumentHandler.class);

    /** Utility class for handling all sorts of peripheral tasks around PCL generation. */
    protected PCLRenderingUtil pclUtil;

    /** The PCL generator */
    private PCLGenerator gen;

    private PCLPageDefinition currentPageDefinition;

    /** contains the pageWith of the last printed page */
    private long pageWidth = 0;
    /** contains the pageHeight of the last printed page */
    private long pageHeight = 0;

    /**
     * Default constructor.
     */
    public PCLDocumentHandler() {
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return false;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_PCL;
    }

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent ua) {
        super.setUserAgent(ua);
        this.pclUtil = new PCLRenderingUtil(ua);
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return null; //No configurator, yet.
    }

    PCLRenderingUtil getPCLUtil() {
        return this.pclUtil;
    }

    PCLGenerator getPCLGenerator() {
        return this.gen;
    }

    /** @return the target resolution */
    protected int getResolution() {
        int resolution = (int)Math.round(getUserAgent().getTargetResolution());
        if (resolution <= 300) {
            return 300;
        } else {
            return 600;
        }
    }

    //----------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        try {
            if (getUserAgent() == null) {
                throw new IllegalStateException(
                        "User agent must be set before starting PDF generation");
            }
            if (this.outputStream == null) {
                throw new IllegalStateException("OutputStream hasn't been set through setResult()");
            }
            log.debug("Rendering areas to PCL...");
            this.gen = new PCLGenerator(this.outputStream, getResolution());

            if (!pclUtil.isPJLDisabled()) {
                gen.universalEndOfLanguage();
                gen.writeText("@PJL COMMENT Produced by " + getUserAgent().getProducer() + "\n");
                if (getUserAgent().getTitle() != null) {
                    gen.writeText("@PJL JOB NAME = \"" + getUserAgent().getTitle() + "\"\n");
                }
                gen.writeText("@PJL SET RESOLUTION = " + getResolution() + "\n");
                gen.writeText("@PJL ENTER LANGUAGE = PCL\n");
            }
            gen.resetPrinter();
            gen.setUnitOfMeasure(getResolution());
            gen.setRasterGraphicsResolution(getResolution());
        } catch (IOException e) {
            throw new IFException("I/O error in startDocument()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
            gen.separateJobs();
            gen.resetPrinter();
            if (!pclUtil.isPJLDisabled()) {
                gen.universalEndOfLanguage();
            }
        } catch (IOException ioe) {
            throw new IFException("I/O error in endDocument()", ioe);
        }
        super.endDocument();
    }

    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, Dimension size) throws IFException {

        try {
            //TODO Add support for paper-source and duplex-mode
            /*
            //Paper source
            String paperSource = page.getForeignAttributeValue(
                    new QName(PCLElementMapping.NAMESPACE, null, "paper-source"));
            if (paperSource != null) {
                gen.selectPaperSource(Integer.parseInt(paperSource));
            }

            // Is Page duplex?
            String pageDuplex = page.getForeignAttributeValue(
                    new QName(PCLElementMapping.NAMESPACE, null, "duplex-mode"));
            if (pageDuplex != null) {
                gen.selectDuplexMode(Integer.parseInt(pageDuplex));
            }*/

            //Page size
            final long pagewidth = size.width;
            final long pageheight = size.height;
            selectPageFormat(pagewidth, pageheight);
        } catch (IOException ioe) {
            throw new IFException("I/O error in startPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        return new PCLPainter(this, this.currentPageDefinition);
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            //Eject page
            gen.formFeed();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (false) {
            //TODO Handle extensions
        } else {
            log.debug("Don't know how to handle extension object. Ignoring: "
                    + extension + " (" + extension.getClass().getName() + ")");
        }
    }

    private void selectPageFormat(long pagewidth, long pageheight) throws IOException {
        //Only set the page format if it changes (otherwise duplex printing won't work)
        if ((pagewidth != this.pageWidth) || (pageheight != this.pageHeight))  {
            this.pageWidth = pagewidth;
            this.pageHeight = pageheight;

            this.currentPageDefinition = PCLPageDefinition.getPageDefinition(
                    pagewidth, pageheight, 1000);

            if (this.currentPageDefinition == null) {
                this.currentPageDefinition = PCLPageDefinition.getDefaultPageDefinition();
                log.warn("Paper type could not be determined. Falling back to: "
                        + this.currentPageDefinition.getName());
            }
            if (log.isDebugEnabled()) {
                log.debug("page size: " + currentPageDefinition.getPhysicalPageSize());
                log.debug("logical page: " + currentPageDefinition.getLogicalPageRect());
            }

            if (this.currentPageDefinition.isLandscapeFormat()) {
                gen.writeCommand("&l1O"); //Landscape Orientation
            } else {
                gen.writeCommand("&l0O"); //Portrait Orientation
            }
            gen.selectPageSize(this.currentPageDefinition.getSelector());

            gen.clearHorizontalMargins();
            gen.setTopMargin(0);
        }
    }

}
