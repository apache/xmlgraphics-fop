/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

package org.apache.fop.threading;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXResult;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.avalon.framework.activity.Initializable;

public class FOProcessorImpl extends AbstractLogEnabled
            implements FOProcessor, Configurable, Initializable {

    private FopFactory fopFactory = FopFactory.newInstance();
    private TransformerFactory factory = TransformerFactory.newInstance();
    private String baseDir;
    private String fontBaseDir;
    private String userconfig;
    private boolean strokeSVGText;

    public void configure(Configuration configuration) throws ConfigurationException {
        this.baseDir = configuration.getChild("basedir").getValue(null);
        this.fontBaseDir = configuration.getChild("fontbasedir").getValue(null);
        this.userconfig = configuration.getChild("userconfig").getValue(null);
        this.strokeSVGText = configuration.getChild("strokesvgtext").getValueAsBoolean(true);
    }


    public void initialize() throws Exception {
        /*
        org.apache.fop.messaging.MessageHandler.setScreenLogger(getLogger());
        if (userconfig != null) {
            getLogger().info("Using user config: "+userconfig);
            InputStream in = org.apache.fop.tools.URLBuilder.buildURL(userconfig).openStream();
            try {
                new org.apache.fop.apps.Options(in);
            } finally {
                in.close();
            }
        }
        if (this.baseDir != null) {
            getLogger().info("Setting base dir: "+baseDir);
            org.apache.fop.configuration.Configuration.put("baseDir", this.baseDir);
        }
        if (this.fontBaseDir != null) {
            getLogger().info("Setting font base dir: "+fontBaseDir);
            org.apache.fop.configuration.Configuration.put("fontBaseDir", this.fontBaseDir);
        }
        String value = (this.strokeSVGText?"true":"false");
        org.apache.fop.configuration.Configuration.put("strokeSVGText", value);
        */
    }


    public void process(InputStream in, Templates templates, OutputStream out) 
                throws org.apache.fop.apps.FOPException, java.io.IOException {
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

        try {
            Transformer transformer;
            if (templates == null) {
                transformer = factory.newTransformer();
            } else {
                transformer = templates.newTransformer();
            }
            Source src = new StreamSource(in);
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
        } catch (TransformerException e) {
            throw new FOPException(e);
        }
    }

}