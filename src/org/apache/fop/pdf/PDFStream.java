/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.pdf;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Class representing a PDF stream.
 * <p>
 * A derivative of the PDF Object, a PDF Stream has not only a dictionary
 * but a stream of PDF commands. The stream of commands is where the real
 * work is done, the dictionary just provides information like the stream
 * length.
 */
public class PDFStream extends PDFObject {
    
    /** Key for the default filter */
    public static final String DEFAULT_FILTER = "default";
    /** Key for the filter used for normal content*/
    public static final String CONTENT_FILTER = "content";
    /** Key for the filter used for images */
    public static final String IMAGE_FILTER = "image";
    /** Key for the filter used for JPEG images */
    public static final String JPEG_FILTER = "jpeg";
    /** Key for the filter used for fonts */
    public static final String FONT_FILTER = "font";

    /**
     * The stream of PDF commands
     */
    protected StreamCache data;

    /**
     * The filters that should be applied
     */
    private List filters;

    /**
     * Create an empty stream object
     *
     * @param number the object's number
     */
    public PDFStream(int number) {
        super(number);
        try {
            data = StreamCache.createStreamCache();
        } catch (IOException ex) {
            /**@todo Log with Logger */
            ex.printStackTrace();
        }
        filters = new java.util.ArrayList();
    }

    /**
     * Append data to the stream
     *
     * @param s the string of PDF to add
     */
    public void add(String s) {
        try {
            data.getOutputStream().write(s.getBytes());
        } catch (IOException ex) {
            /**@todo Log with Logger */
            ex.printStackTrace();
        }

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
        } else if (filterType.equals("ascii-85")) {
            addFilter(new ASCII85Filter());
        } else if (filterType.equals("ascii-hex")) {
            addFilter(new ASCIIHexFilter());
        } else if (filterType.equals("")) {
            return;
        } else {
            throw new IllegalArgumentException(
                "Unsupported filter type in stream-filter-list: " + filterType);
        }
    }

    /**
     * Adds the default filters to this stream.
     * @param filters Map of filters
     * @param type which filter list to modify
     */
    public void addDefaultFilters(Map filters, String type) {
        List filterset = (List)filters.get(type);
        if (filterset == null) {
            filterset = (List)filters.get(DEFAULT_FILTER);
        }
        if (filterset == null || filterset.size() == 0) {
            // built-in default to flate
            //addFilter(new FlateFilter());
        } else {
            for (int i = 0; i < filterset.size(); i++) {
                String v = (String)filterset.get(i);
                addFilter(v);
            }
        }
    }

    /**
     * Append an array of xRGB pixels, ASCII Hex Encoding it first
     *
     * @param pixels the area of pixels
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     */
    public void addImageArray(int[] pixels, int width, int height) {
        try {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int p = pixels[i * width + j];
                    int r = (p >> 16) & 0xFF;
                    int g = (p >> 8) & 0xFF;
                    int b = (p) & 0xFF;
                    if (r < 16) {
                        data.getOutputStream().write('0');
                    }
                    data.getOutputStream().write(Integer.toHexString(r).getBytes());
                    if (g < 16) {
                        data.getOutputStream().write('0');
                    }
                    data.getOutputStream().write(Integer.toHexString(g).getBytes());
                    if (b < 16) {
                        data.getOutputStream().write('0');
                    }
                    data.getOutputStream().write(Integer.toHexString(b).getBytes());
                    data.getOutputStream().write(' ');
                }
            }
            data.getOutputStream().write(">\n".getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Used to set the contents of the PDF stream.
     * @param data the contents as a byte array
     * @throws IOException in case of an I/O problem
     */
    public void setData(byte[] data) throws IOException {
        this.data.reset();
        this.data.getOutputStream().write(data);
    }

    /*
    public byte[] getData() {
        return _data.toByteArray();
    }
    */

    /**
     * Returns the size of the content.
     * @return size of the content
     */
    public int getDataLength() {
        try {
            return data.getSize();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Represent as PDF.
     *
     * @return the PDF string.
     */
    public byte[] toPDF() {
        throw new UnsupportedOperationException("Use output(OutputStream) instead");
        /*
         * byte[] d = _data.toByteArray();
         * ByteArrayOutputStream s = new ByteArrayOutputStream();
         * String p = this.number + " " + this.generation
         * + " obj\n<< /Length " + (d.length+1)
         * + " >>\nstream\n";
         * s.write(p.getBytes());
         * s.write(d);
         * s.write("\nendstream\nendobj\n".getBytes());
         * return s.toByteArray();
         */
    }

    /**
     * Overload the base object method so we don't have to copy
     * byte arrays around so much
     * @see org.apache.fop.pdf.PDFObject#output(OutputStream)
     */
    protected int output(OutputStream stream) throws IOException {
        int length = 0;
        String filterEntry = applyFilters();
        byte[] p = (this.number + " " + this.generation + " obj\n<< /Length "
                    + (data.getSize() + 1) + " " + filterEntry
                    + " >>\n").getBytes();

        stream.write(p);
        length += p.length;
        length += outputStreamData(stream);
        p = "endobj\n".getBytes();
        stream.write(p);
        length += p.length;
        return length;

    }

    /**
     * Output just the stream data enclosed by stream/endstream markers
     * @param stream OutputStream to write to
     * @return int number of bytes written
     * @throws IOException in case of an I/O problem
     */
    protected int outputStreamData(OutputStream stream) throws IOException {
        int length = 0;
        byte[] p = "stream\n".getBytes();
        stream.write(p);
        length += p.length;
        data.outputStreamData(stream);
        length += data.getSize();
        data.close();
        p = "\nendstream\n".getBytes();
        stream.write(p);
        length += p.length;
        return length;

    }

    /**
     * Apply the filters to the data
     * in the order given and return the /Filter and /DecodeParms
     * entries for the stream dictionary. If the filters have already
     * been applied to the data (either externally, or internally)
     * then the dictionary entries are built and returned.
     * @return a String representing the filter list
     * @throws IOException in case of an I/O problem
     */
    protected String applyFilters() throws IOException {
        if (filters.size() > 0) {
            List names = new java.util.ArrayList();
            List parms = new java.util.ArrayList();

            // run the filters
            for (int count = 0; count < filters.size(); count++) {
                PDFFilter filter = (PDFFilter)filters.get(count);
                // apply the filter encoding if neccessary
                if (!filter.isApplied()) {
                    data.applyFilter(filter);
                    filter.setApplied(true);
                }
                // place the names in our local vector in reverse order
                names.add(0, filter.getName());
                parms.add(0, filter.getDecodeParms());
            }

            // now build up the filter entries for the dictionary
            return buildFilterEntries(names) + buildDecodeParms(parms);
        }
        return "";

    }

    private String buildFilterEntries(List names) {
        StringBuffer sb = new StringBuffer();
        sb.append("/Filter ");
        if (names.size() > 1) {
            sb.append("[ ");
        }
        for (int count = 0; count < names.size(); count++) {
            sb.append((String)names.get(count));
            sb.append(" ");
        }
        if (names.size() > 1) {
            sb.append("]");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String buildDecodeParms(List parms) {
        StringBuffer sb = new StringBuffer();
        boolean needParmsEntry = false;
        sb.append("/DecodeParms ");

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
        sb.append("\n");
        if (needParmsEntry) {
            return sb.toString();
        } else {
            return "";
        }
    }


}
