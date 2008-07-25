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

package org.apache.fop.visual;

import java.io.File;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;

/**
 * Context object for the bitmap production.
 */
public class ProducerContext {

    private TransformerFactory tFactory;
    private Templates templates;
    private int targetResolution;
    private File targetDir;

    /**
     * @return the TransformerFactory to be used.
     */
    public TransformerFactory getTransformerFactory() {
        if (tFactory == null) {
            tFactory = TransformerFactory.newInstance();
        }
        return tFactory;
    }

    /**
     * @return the requested bitmap resolution in dpi for all bitmaps.
     */
    public int getTargetResolution() {
        return targetResolution;
    }

    /**
     * Sets the requested bitmap resolution in dpi for all bitmaps.
     * @param resolution the resolution in dpi
     */
    public void setTargetResolution(int resolution) {
        this.targetResolution = resolution;
    }

    /**
     * @return the XSLT stylesheet to preprocess the input files with.
     */
    public Templates getTemplates() {
        return templates;
    }

    /**
     * Sets an optional XSLT stylesheet which is used to preprocess all input files with.
     * @param templates the XSLT stylesheet
     */
    public void setTemplates(Templates templates) {
        this.templates = templates;
    }

    /**
     * @return the target directory for all produced bitmaps
     */
    public File getTargetDir() {
        return targetDir;
    }

    /**
     * Sets the target directory for all produced bitmaps.
     * @param targetDir the target directory
     */
    public void setTargetDir(File targetDir) {
        this.targetDir = targetDir;
    }
}
