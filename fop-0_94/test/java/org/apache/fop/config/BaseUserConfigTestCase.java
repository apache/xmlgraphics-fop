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

/* $Id: $ */

package org.apache.fop.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.pdf.BasePDFTestCase;
import org.xml.sax.SAXException;

/**
 * Basic runtime test for FOP's font configuration. It is used to verify that 
 * nothing obvious is broken after compiling.
 */
public abstract class BaseUserConfigTestCase extends BasePDFTestCase {

    protected DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();

    /** logging instance */
    protected Log log = LogFactory.getLog(BaseUserConfigTestCase.class);


    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public BaseUserConfigTestCase(String name) {
        super(name);
    }

    /**
     * @see org.apache.fop.render.pdf.BasePDFTestCase#init()
     */
    protected void init() {
        // do nothing
    }

    protected void initConfig() throws Exception {
        fopFactory.setUserConfig(getUserConfig());                
    }

    protected void convertFO() throws Exception {
        final File baseDir = getBaseDir();
        final String fontFOFilePath = getFontFOFilePath();
        File foFile = new File(baseDir, fontFOFilePath);
        final boolean dumpOutput = false;
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        convertFO(foFile, foUserAgent, dumpOutput);
    }
    
    /**
     * get test FOP config File
     * @return fo test filepath
     */
    protected String getFontFOFilePath() {
        return "test/xml/bugtests/font.fo";
    }

    /**
     * get test FOP Configuration
     * @return fo test filepath
     * @throws IOException 
     * @throws SAXException 
     * @throws ConfigurationException 
     */
    protected Configuration getUserConfig(String configString) throws ConfigurationException, SAXException, IOException {
        return cfgBuilder.build(new ByteArrayInputStream(configString.getBytes()));
    }

    /** get base config directory */
    protected String getBaseConfigDir() {
        return "test/config";
    }

    /**
     * @return user config File
     */
    abstract protected String getUserConfigFilename();

    /*
     * @see junit.framework.TestCase#getName()
     */
    public String getName() {
        return getUserConfigFilename();
    }

    protected File getUserConfigFile() {
        return new File(getBaseConfigDir() + File.separator + getUserConfigFilename());
    }

    /**
     * get test FOP Configuration
     * @return fo test filepath
     * @throws IOException 
     * @throws SAXException 
     * @throws ConfigurationException 
     */
    protected Configuration getUserConfig() throws ConfigurationException, SAXException, IOException {
        return cfgBuilder.buildFromFile(getUserConfigFile());
    }        
}
