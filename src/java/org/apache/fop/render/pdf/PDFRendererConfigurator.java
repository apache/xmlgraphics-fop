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

package org.apache.fop.render.pdf;

import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;
import org.apache.fop.util.LogUtil;

/**
 * PDF renderer configurator 
 */
public class PDFRendererConfigurator extends PrintRendererConfigurator {

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PDFRendererConfigurator(FOUserAgent userAgent) {
        super(userAgent);
    }

    /**
     * Configure the PDF renderer.
     * Get the configuration to be used for pdf stream filters,
     * fonts etc.
     * @param renderer pdf renderer
     * @throws FOPException fop exception
     */
    public void configure(Renderer renderer) throws FOPException {
        Configuration cfg = super.getRendererConfig(renderer);
        if (cfg != null) {
            PDFRenderer pdfRenderer = (PDFRenderer)renderer;
            //PDF filters
            try {
                Map filterMap = buildFilterMapFromConfiguration(cfg);
                if (filterMap != null) {
                    pdfRenderer.setFilterMap(filterMap);
                }
            } catch (ConfigurationException e) {
                LogUtil.handleException(log, e, false);
            }
    
            super.configure(renderer);
    
            String s = cfg.getChild(PDFRenderer.PDF_A_MODE, true).getValue(null);
            if (s != null) {
                pdfRenderer.setAMode(PDFAMode.valueOf(s));
            }
            s = cfg.getChild(PDFRenderer.PDF_X_MODE, true).getValue(null);
            if (s != null) {
                pdfRenderer.setXMode(PDFXMode.valueOf(s));
            }
            s = cfg.getChild(PDFRenderer.KEY_OUTPUT_PROFILE, true).getValue(null);
            if (s != null) {
                pdfRenderer.setOutputProfileURI(s);
            }
        }
    }

    /**
     * Builds a filter map from an Avalon Configuration object.
     * @param cfg the Configuration object
     * @return Map the newly built filter map
     * @throws ConfigurationException if a filter list is defined twice
     */
    public static Map buildFilterMapFromConfiguration(Configuration cfg) 
                throws ConfigurationException {
        Map filterMap = new java.util.HashMap();
        Configuration[] filterLists = cfg.getChildren("filterList");
        for (int i = 0; i < filterLists.length; i++) {
            Configuration filters = filterLists[i];
            String type = filters.getAttribute("type", null);
            Configuration[] filt = filters.getChildren("value");
            List filterList = new java.util.ArrayList();
            for (int j = 0; j < filt.length; j++) {
                String name = filt[j].getValue();
                filterList.add(name);
            }
            
            if (type == null) {
                type = PDFFilterList.DEFAULT_FILTER;
            }
    
            if (!filterList.isEmpty() && log.isDebugEnabled()) {
                StringBuffer debug = new StringBuffer("Adding PDF filter");
                if (filterList.size() != 1) {
                    debug.append("s");
                }
                debug.append(" for type ").append(type).append(": ");
                for (int j = 0; j < filterList.size(); j++) {
                    if (j != 0) {
                        debug.append(", ");
                    }
                    debug.append(filterList.get(j));
                }
                log.debug(debug.toString());
            }
            
            if (filterMap.get(type) != null) {
                throw new ConfigurationException("A filterList of type '" 
                    + type + "' has already been defined");
            }
            filterMap.put(type, filterList);
        }
        return filterMap;                
    }
}
