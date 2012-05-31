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

package org.apache.fop.apps;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.fop.apps.FopConfBuilder.RendererConfBuilder;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.intermediate.IFDocumentHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractRendererConfiguratorTest<T extends PrintRendererConfigurator, B extends RendererConfBuilder> {

    protected final String mimeType;
    protected FOUserAgent userAgent;
    protected T sut;
    protected IFDocumentHandler docHandler;
    private final Class<B> builderClass;

    public abstract void setUpDocumentHandler();

    public AbstractRendererConfiguratorTest(String mimeType, Class<B> builderClass,
            Class<? extends IFDocumentHandler> docHandlerClass) {
        this.mimeType = mimeType;
        this.builderClass = builderClass;
        this.docHandler = mock(docHandlerClass);
        when(this.docHandler.getMimeType()).thenReturn(mimeType);
    }

    protected B createBuilder() {
        return builderClass.cast(new FopConfBuilder().startRendererConfig(builderClass));
    }

    protected abstract T createConfigurator();

    protected void parseConfig(RendererConfBuilder builder)
            throws ConfigurationException, SAXException, IOException {
        userAgent = FopFactory.newInstance(
                new File(".").toURI(), builder.endRendererConfig().build()).newFOUserAgent();
        setUpDocumentHandler();
        sut = createConfigurator();
        sut.configure(docHandler);
    }
}
