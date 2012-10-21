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

package org.apache.fop.threading;

import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.commons.io.FilenameUtils;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * Default implementation of the {@link Processor} interface using FOP.
 */
public class FOProcessorImpl extends AbstractLogEnabled
            implements Processor, Configurable, Initializable {

    private FopFactory fopFactory;
    private TransformerFactory factory = TransformerFactory.newInstance();
    private URI userconfig;
    private String mime;
    private String fileExtension;

    /** {@inheritDoc} */
    public void configure(Configuration configuration) throws ConfigurationException {
        try {
            this.userconfig = new URI(configuration.getChild("userconfig").getValue(null));
            this.mime = configuration.getChild("mime").getValue(MimeConstants.MIME_PDF);
            this.fileExtension = configuration.getChild("extension").getValue(".pdf");
        } catch (URISyntaxException use) {
            throw new RuntimeException(use);
        }
    }

    public void initialize() throws Exception {
        if (this.userconfig != null) {
            getLogger().debug("Setting user config: " + userconfig);
            fopFactory = FopFactory.newInstance(new File(userconfig));
        } else {
            fopFactory = FopFactory.newInstance(new File(".").toURI());
        }
    }

    /** {@inheritDoc} 
     * @throws URISyntaxException 
     * @throws SAXException */
    public void process(Source src, Templates templates, OutputStream out)
            throws java.io.IOException, URISyntaxException, SAXException {
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        try {
            URL url = new URL(src.getSystemId());
            String filename = FilenameUtils.getName(url.getPath());
            foUserAgent.getEventBroadcaster().addEventListener(
                    new AvalonAdapter(getLogger(), filename));
        } catch (MalformedURLException mfue) {
            throw new RuntimeException(mfue);
        }
        Fop fop = fopFactory.newFop(this.mime, foUserAgent, out);

        try {
            Transformer transformer;
            if (templates == null) {
                transformer = factory.newTransformer();
            } else {
                transformer = templates.newTransformer();
            }
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
        } catch (TransformerException e) {
            throw new FOPException(e);
        }
    }

    /** {@inheritDoc} */
    public String getTargetFileExtension() {
        return this.fileExtension;
    }
}
