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

package org.apache.fop.render.intermediate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.base14.Base14FontCollection;

/**
 * Abstract base class for binary-writing {@link IFDocumentHandler} implementations.
 */
public abstract class AbstractBinaryWritingIFDocumentHandler extends AbstractIFDocumentHandler {

    /** The output stream to write the document to */
    protected OutputStream outputStream;

    private boolean ownOutputStream;

    /** Font configuration */
    protected FontInfo fontInfo;

    public AbstractBinaryWritingIFDocumentHandler(IFContext ifContext) {
        super(ifContext);
    }

    /** {@inheritDoc} */
    public void setResult(Result result) throws IFException {
        if (result instanceof StreamResult) {
            StreamResult streamResult = (StreamResult)result;
            OutputStream out = streamResult.getOutputStream();
            if (out == null) {
                if (streamResult.getWriter() != null) {
                    throw new IllegalArgumentException(
                            "FOP cannot use a Writer. Please supply an OutputStream!");
                }
                try {
                    URL url = new URL(streamResult.getSystemId());
                    File f = FileUtils.toFile(url);
                    if (f != null) {
                        out = new java.io.FileOutputStream(f);
                    } else {
                        out = url.openConnection().getOutputStream();
                    }
                } catch (IOException ioe) {
                    throw new IFException("I/O error while opening output stream" , ioe);
                }
                out = new java.io.BufferedOutputStream(out);
                this.ownOutputStream = true;
            }
            if (out == null) {
                throw new IllegalArgumentException("Need a StreamResult with an OutputStream");
            }
            this.outputStream = out;
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported Result subclass: " + result.getClass().getName());
        }
    }

    /** {@inheritDoc} */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /** {@inheritDoc} */
    public void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }

    /** {@inheritDoc} */
    public void setDefaultFontInfo(FontInfo fontInfo) {
        FontManager fontManager = getUserAgent().getFontManager();
        FontCollection[] fontCollections = new FontCollection[] {
                new Base14FontCollection(fontManager.isBase14KerningEnabled())
        };

        FontInfo fi = (fontInfo != null ? fontInfo : new FontInfo());
        fi.setEventListener(new FontEventAdapter(getUserAgent().getEventBroadcaster()));
        fontManager.setup(fi, fontCollections);
        setFontInfo(fi);
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        super.startDocument();
        if (this.outputStream == null) {
            throw new IllegalStateException("OutputStream hasn't been set through setResult()");
        }
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        if (this.ownOutputStream) {
            IOUtils.closeQuietly(this.outputStream);
            this.outputStream = null;
        }
    }

}
