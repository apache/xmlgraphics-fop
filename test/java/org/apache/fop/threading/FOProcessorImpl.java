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

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

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
import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;

/**
 * Default implementation of the FOProcessor interface using FOP.
 */
public class FOProcessorImpl extends AbstractLogEnabled
            implements FOProcessor, Configurable, Initializable {

    private FopFactory fopFactory = FopFactory.newInstance();
    private TransformerFactory factory = TransformerFactory.newInstance();
    private String userconfig;
    private String mime;
    private String fileExtension;

    /** {@inheritDoc} */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.userconfig = configuration.getChild("userconfig").getValue(null);
        this.mime = configuration.getChild("mime").getValue(MimeConstants.MIME_PDF);
        this.fileExtension = configuration.getChild("extension").getValue(".pdf");
    }

    /** {@inheritDoc} */
    public void initialize() throws Exception {
        if (this.userconfig != null) {
            getLogger().debug("Setting user config: " + userconfig);
            fopFactory.setUserConfig(this.userconfig);
        }
    }

    /** {@inheritDoc} */
    public void process(Source src, Templates templates, OutputStream out)
                throws org.apache.fop.apps.FOPException, java.io.IOException {
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.setBaseURL(src.getSystemId());
        try {
            URL url = new URL(src.getSystemId());
            String filename = FilenameUtils.getName(url.getPath());
            foUserAgent.getEventBroadcaster().addEventListener(new AvalonAdapter(filename));
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

    private class AvalonAdapter implements EventListener {

        private String filename;

        public AvalonAdapter(String filename) {
            this.filename = filename;
        }

        public void processEvent(Event event) {
            String msg = EventFormatter.format(event);
            EventSeverity severity = event.getSeverity();
            if (severity == EventSeverity.INFO) {
                //getLogger().info(filename + ": " + msg);
            } else if (severity == EventSeverity.WARN) {
                //getLogger().warn(filename + ": "  + msg);
            } else if (severity == EventSeverity.ERROR) {
                getLogger().error(filename + ": "  + msg);
            } else if (severity == EventSeverity.FATAL) {
                getLogger().fatalError(filename + ": "  + msg);
            } else {
                assert false;
            }
        }

    }
}