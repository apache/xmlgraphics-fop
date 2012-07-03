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

package org.apache.fop.render.intermediate.util;

import java.awt.Dimension;
import java.util.Locale;

import javax.xml.transform.Result;

import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFDocumentNavigationHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;

/**
 * This class is a simple proxy that delegates all method calls to another {@link IFDocumentHandler}
 * instance.
 */
public class IFDocumentHandlerProxy implements IFDocumentHandler {

    /** the delegate IFDocumentHandler */
    protected IFDocumentHandler delegate;

    /**
     * Creates a new proxy instance.
     * @param delegate the delegate instance
     */
    public IFDocumentHandlerProxy(IFDocumentHandler delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return this.delegate.supportsPagesOutOfOrder();
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return this.delegate.getMimeType();
    }

    /** {@inheritDoc} */
    public IFContext getContext() {
        return this.delegate.getContext();
    }

    /** {@inheritDoc} */
    public FontInfo getFontInfo() {
        return this.delegate.getFontInfo();
    }

    /** {@inheritDoc} */
    public void setFontInfo(FontInfo fontInfo) {
        this.delegate.setFontInfo(fontInfo);
    }

    /** {@inheritDoc} */
    public void setDefaultFontInfo(FontInfo fontInfo) {
        this.delegate.setDefaultFontInfo(fontInfo);
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return this.delegate.getConfigurator();
    }

    /** {@inheritDoc} */
    public IFDocumentNavigationHandler getDocumentNavigationHandler() {
        return this.delegate.getDocumentNavigationHandler();
    }

    /** {@inheritDoc} */
    public StructureTreeEventHandler getStructureTreeEventHandler() {
        return this.delegate.getStructureTreeEventHandler();
    }

    /** {@inheritDoc} */
    public void setResult(Result result) throws IFException {
        this.delegate.setResult(result);
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        this.delegate.startDocument();
    }

    /** {@inheritDoc} */
    public void setDocumentLocale(Locale locale) {
         this.delegate.setDocumentLocale(locale);

    }

    /** {@inheritDoc} */
    public void startDocumentHeader() throws IFException {
        this.delegate.startDocumentHeader();
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        this.delegate.endDocumentHeader();
    }

    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        this.delegate.startPageSequence(id);
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, String pageMasterName, Dimension size)
            throws IFException {
        this.delegate.startPage(index, name, pageMasterName, size);
    }

    /** {@inheritDoc} */
    public void startPageHeader() throws IFException {
        this.delegate.startPageHeader();
    }

    /** {@inheritDoc} */
    public void endPageHeader() throws IFException {
        this.delegate.endPageHeader();
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        return this.delegate.startPageContent();
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        this.delegate.endPageContent();
    }

    /** {@inheritDoc} */
    public void startPageTrailer() throws IFException {
        this.delegate.startPageTrailer();
    }

    /** {@inheritDoc} */
    public void endPageTrailer() throws IFException {
        this.delegate.endPageTrailer();
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        this.delegate.endPage();
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        this.delegate.endPageSequence();
    }

    /** {@inheritDoc} */
    public void startDocumentTrailer() throws IFException {
        this.delegate.startDocumentTrailer();
    }

    /** {@inheritDoc} */
    public void endDocumentTrailer() throws IFException {
        this.delegate.endDocumentTrailer();
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        this.delegate.endDocument();
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        this.delegate.handleExtensionObject(extension);
    }

}
