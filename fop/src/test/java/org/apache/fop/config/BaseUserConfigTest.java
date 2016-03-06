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

package org.apache.fop.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.pdf.BasePDFTest;

import static org.apache.fop.FOPTestUtils.getBaseDir;

/**
 * Basic runtime test for FOP's font configuration. It is used to verify that
 * nothing obvious is broken after compiling.
 */
public abstract class BaseUserConfigTest extends BasePDFTest {

    protected DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();

    /** logging instance */
    protected Log log = LogFactory.getLog(BaseUserConfigTest.class);


    public BaseUserConfigTest(InputStream confStream) throws SAXException, IOException {
        super(confStream);
    }

    /**
     * @see org.apache.fop.render.pdf.BasePDFTest#init()
     */
    protected void init() {
        // do nothing
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

    /** get base config directory */
    protected static String getBaseConfigDir() {
        return "test/config/";
    }
}
