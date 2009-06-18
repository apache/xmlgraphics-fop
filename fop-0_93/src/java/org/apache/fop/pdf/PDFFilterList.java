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
 
package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

// commons logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Avalon
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * This class represents a list of PDF filters to be applied when serializing 
 * the output of a PDF object.
 */
public class PDFFilterList {

    /** Key for the default filter */
    public static final String DEFAULT_FILTER = "default";
    /** Key for the filter used for normal content*/
    public static final String CONTENT_FILTER = "content";
    /** Key for the filter used for images */
    public static final String IMAGE_FILTER = "image";
    /** Key for the filter used for JPEG images */
    public static final String JPEG_FILTER = "jpeg";
    /** Key for the filter used for TIFF images */
    public static final String TIFF_FILTER = "tiff";
    /** Key for the filter used for fonts */
    public static final String FONT_FILTER = "font";
    /** Key for the filter used for metadata */
    public static final String METADATA_FILTER = "metadata";

    private List filters = new java.util.ArrayList();

    private boolean ignoreASCIIFilters = false;
    
    /**
     * logging instance
     */
    protected static Log logger = LogFactory.getLog("org.apache.fop.render");
    
    /**
     * Default constructor.
     * <p>
     * The flag for ignoring ASCII filters defaults to false.
     */
    public PDFFilterList() {
        //nop
    }
    
    /**
     * Use this descriptor if you want to have ASCII filters (such as ASCIIHex
     * and ASCII85) ignored, for example, when encryption is active.
     * @param ignoreASCIIFilters true if ASCII filters should be ignored
     */
    public PDFFilterList(boolean ignoreASCIIFilters) {
        this.ignoreASCIIFilters = ignoreASCIIFilters;
    }

    /**
     * Indicates whether the filter list is already initialized.
     * @return true if more there are filters present
     */
    public boolean isInitialized() {
        return this.filters.size() > 0;
    }

    /**
     * Add a filter for compression of the stream. Filters are
     * applied in the order they are added. This should always be a
     * new instance of the particular filter of choice. The applied
     * flag in the filter is marked true after it has been applied to the
     * data.
     * @param filter filter to add
     */
    public void addFilter(PDFFilter filter) {
        if (filter != null) {
            if (this.ignoreASCIIFilters && filter.isASCIIFilter()) {
                return; //ignore ASCII filter
            }
            filters.add(filter);
        }
    }
    
    /**
     * Add a filter for compression of the stream by name.
     * @param filterType name of the filter to add
     */
    public void addFilter(String filterType) {
        if (filterType == null) {
            return;
        }
        if (filterType.equals("flate")) {
            addFilter(new FlateFilter());
        } else if (filterType.equals("null")) {
            addFilter(new NullFilter());
        } else if (filterType.equals("ascii-85")) {
            if (this.ignoreASCIIFilters) {
                return; //ignore ASCII filter
            }
            addFilter(new ASCII85Filter());
        } else if (filterType.equals("ascii-hex")) {
            if (this.ignoreASCIIFilters) {
                return; //ignore ASCII filter
            }
            addFilter(new ASCIIHexFilter());
        } else if (filterType.equals("")) {
            return;
        } else {
            throw new IllegalArgumentException(
                "Unsupported filter type in stream-filter-list: " + filterType);
        }
    }

    /**
     * Checks the filter list for the filter and adds it in the correct
     * place if necessary.
     * @param pdfFilter the filter to check / add
     */
    public void ensureFilterInPlace(PDFFilter pdfFilter) {
        if (this.filters.size() == 0) {
            addFilter(pdfFilter);
        } else {
            if (!(this.filters.get(0).equals(pdfFilter))) {
                this.filters.add(0, pdfFilter);
            }
        }
    }

    /**
     * Adds the default filters to this stream.
     * @param filters Map of filters
     * @param type which filter list to modify
     */
    public void addDefaultFilters(Map filters, String type) {
        List filterset = null;
        if (filters != null) {
            filterset = (List)filters.get(type);
            if (filterset == null) {
                filterset = (List)filters.get(DEFAULT_FILTER);
            }
        }
        if (filterset == null || filterset.size() == 0) {
            if (METADATA_FILTER.equals(type)) {
                //XMP metadata should not be embedded in clear-text
                addFilter(new NullFilter());
            } else if (JPEG_FILTER.equals(type)) {
                //JPEG is already well compressed
                addFilter(new NullFilter());
            } else if (TIFF_FILTER.equals(type)) {
                //CCITT-encoded images are already well compressed
                addFilter(new NullFilter());
            } else {
                // built-in default to flate
                addFilter(new FlateFilter());
            }
        } else {
            for (int i = 0; i < filterset.size(); i++) {
                String v = (String)filterset.get(i);
                addFilter(v);
            }
        }
    }

    /**
     * Apply the filters to the data
     * in the order given and return the /Filter and /DecodeParms
     * entries for the stream dictionary. If the filters have already
     * been applied to the data (either externally, or internally)
     * then the dictionary entries are built and returned.
     * @return a String representing the filter list
     */
    protected String buildFilterDictEntries() {
        if (filters != null && filters.size() > 0) {
            List names = new java.util.ArrayList();
            List parms = new java.util.ArrayList();

            // run the filters
            int nonNullParams = 0;
            for (int count = 0; count < filters.size(); count++) {
                PDFFilter filter = (PDFFilter)filters.get(count);
                // place the names in our local vector in reverse order
                if (filter.getName().length() > 0) {
                    names.add(0, filter.getName());
                    if (filter.getDecodeParms() != null) {
                        parms.add(0, filter.getDecodeParms());
                        nonNullParams++;
                    } else {
                        parms.add(0, null);
                    }
                }
            }

            // now build up the filter entries for the dictionary
            return buildFilterEntries(names) 
                    + (nonNullParams > 0 ? buildDecodeParms(parms) : "");
        }
        return "";

    }

    private String buildFilterEntries(List names) {
        int filterCount = 0;
        StringBuffer sb = new StringBuffer(64);
        for (int i = 0; i < names.size(); i++) {
            final String name = (String)names.get(i);
            if (name.length() > 0) {
                filterCount++;
                sb.append(name);
                sb.append(" ");
            }
        }
        if (filterCount > 0) {
            if (filterCount > 1) {
                return "/Filter [ " + sb.toString() + "]";
            } else {
                return "/Filter " + sb.toString();
            }
        } else {
            return "";
        }
    }

    private String buildDecodeParms(List parms) {
        StringBuffer sb = new StringBuffer();
        boolean needParmsEntry = false;
        sb.append("\n/DecodeParms ");

        if (parms.size() > 1) {
            sb.append("[ ");
        }
        for (int count = 0; count < parms.size(); count++) {
            String s = (String)parms.get(count);
            if (s != null) {
                sb.append(s);
                needParmsEntry = true;
            } else {
                sb.append("null");
            }
            sb.append(" ");
        }
        if (parms.size() > 1) {
            sb.append("]");
        }
        if (needParmsEntry) {
            return sb.toString();
        } else {
            return "";
        }
    }

    
    /**
     * Applies all registered filters as necessary. The method returns an 
     * OutputStream which will receive the filtered contents.
     * @param stream raw data output stream
     * @return OutputStream filtered output stream
     * @throws IOException In case of an I/O problem
     */
    public OutputStream applyFilters(OutputStream stream) throws IOException {
        OutputStream out = stream;
        if (filters != null) {
            for (int count = filters.size() - 1; count >= 0; count--) {
                PDFFilter filter = (PDFFilter)filters.get(count);
                out = filter.applyFilter(out);
            }
        }
        return out;
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

            if (!filterList.isEmpty() && logger.isDebugEnabled()) {
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
                logger.debug(debug.toString());
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
