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

// Fop
import org.apache.fop.configuration.Configuration;
import org.apache.fop.messaging.MessageHandler;

// Java
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * class representing a PDF stream.
 *
 * A derivative of the PDF Object, a PDF Stream has not only a dictionary
 * but a stream of PDF commands. The stream of commands is where the real
 * work is done, the dictionary just provides information like the stream
 * length.
 */
public class PDFStream extends PDFObject {

    /**
     * the stream of PDF commands
     */
    protected ByteArrayOutputStream _data;

    /**
     * the filters that should be applied
     */
    private List _filters;

    /**
     * create an empty stream object
     *
     * @param number the object's number
     */
    public PDFStream(int number) {
        super(number);
        _data = new ByteArrayOutputStream();
        _filters = new java.util.ArrayList();
    }

    /**
     * append data to the stream
     *
     * @param s the string of PDF to add
     */
    public void add(String s) {
        try {
            try {
                _data.write(s.getBytes(PDFDocument.ENCODING));
            } catch (UnsupportedEncodingException ue) {
                _data.write(s.getBytes());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Add a filter for compression of the stream. Filters are
     * applied in the order they are added. This should always be a
     * new instance of the particular filter of choice. The applied
     * flag in the filter is marked true after it has been applied to the
     * data.
     */
    public void addFilter(PDFFilter filter) {
        if (filter != null) {
            _filters.add(filter);
        }

    }

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
            MessageHandler.errorln("Unsupported filter type in stream-filter-list: "
                                   + filterType);
        }
    }


    protected void addDefaultFilters() {
        List filters = Configuration.getListValue("stream-filter-list",
                                                    Configuration.PDF);
        if (filters == null) {
            // try getting it as a String
            String filter = Configuration.getStringValue("stream-filter-list",
                    Configuration.PDF);
            if (filter == null) {
                // built-in default to flate
                addFilter(new FlateFilter());
            } else {
                addFilter(filter);
            }
        } else {
            for (int i = 0; i < filters.size(); i++) {
                String v = (String)filters.get(i);
                addFilter(v);
            }
        }
    }


    /**
     * append an array of xRGB pixels, ASCII Hex Encoding it first
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
                        _data.write('0');
                    }
                    try {
                        _data.write(Integer.toHexString(r).getBytes(PDFDocument.ENCODING));
                    } catch (UnsupportedEncodingException ue) {
                        _data.write(Integer.toHexString(r).getBytes());
                    }
                    if (g < 16) {
                        _data.write('0');
                    }
                    try {
                        _data.write(Integer.toHexString(g).getBytes(PDFDocument.ENCODING));
                    } catch (UnsupportedEncodingException ue) {
                        _data.write(Integer.toHexString(g).getBytes());
                    }
                    if (b < 16) {
                        _data.write('0');
                    }
                    try {
                        _data.write(Integer.toHexString(b).getBytes(PDFDocument.ENCODING));
                    } catch (UnsupportedEncodingException ue) {
                        _data.write(Integer.toHexString(b).getBytes());
                    }
                    _data.write(' ');
                }
            }
            try {
                _data.write(">\n".getBytes(PDFDocument.ENCODING));
            } catch (UnsupportedEncodingException ue) {
                _data.write(">\n".getBytes());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void setData(byte[] data) throws IOException {
        _data.reset();
        _data.write(data);
    }

    public byte[] getData() {
        return _data.toByteArray();
    }

    public int getDataLength() {
        return _data.size();
    }



    /**
     * represent as PDF.
     *
     * @return the PDF string.
     */
    /*
     * public byte[] toPDF() {
     * byte[] d = _data.toByteArray();
     * ByteArrayOutputStream s = new ByteArrayOutputStream();
     * String p = this.number + " " + this.generation
     * + " obj\n<< /Length " + (d.length+1)
     * + " >>\nstream\n";
     * s.write(p.getBytes());
     * s.write(d);
     * s.write("\nendstream\nendobj\n".getBytes());
     * return s.toByteArray();
     * }
     */
    public byte[] toPDF() {
        throw new RuntimeException();
    }


    // overload the base object method so we don't have to copy
    // byte arrays around so much
    protected int output(OutputStream stream) throws IOException {
        int length = 0;
        String filterEntry = applyFilters();
        String s = this.number + " " + this.generation + " obj\n<< /Length "
                    + (_data.size() + 1) + " " + filterEntry
                    + " >>\n";
        byte[] p;
        try {
            p = s.getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            p = s.getBytes();
        }
        stream.write(p);
        length += p.length;
        length += outputStreamData(stream);
        try {
            p = "endobj\n".getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            p = "endobj\n".getBytes();
        }
        stream.write(p);
        length += p.length;
        return length;

    }

    /**
     * Output just the stream data enclosed by stream/endstream markers
     */
    protected int outputStreamData(OutputStream stream) throws IOException {
        int length = 0;
        byte[] p;
        try {
            p = "stream\n".getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            p = "stream\n".getBytes();
        }
        stream.write(p);
        length += p.length;
        _data.writeTo(stream);
        length += _data.size();
        try {
            p = "\nendstream\n".getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            p = "\nendstream\n".getBytes();
        }
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
     */
    protected String applyFilters() throws IOException {
        if (_filters.size() > 0) {
            List names = new java.util.ArrayList();
            List parms = new java.util.ArrayList();

            // run the filters
            for (int i = 0; i < _filters.size(); i++) {
                PDFFilter filter = (PDFFilter)_filters.get(i);
                // apply the filter encoding if neccessary
                if (!filter.isApplied()) {
                    byte[] tmp = filter.encode(_data.toByteArray());
                    _data.reset();
                    _data.write(tmp);
                    filter.setApplied(true);
                }
                // place the names in our local List in reverse order
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
        for (int i = 0; i < names.size(); i++) {
            sb.append((String)names.get(i));
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
        for (int i = 0; i < parms.size(); i++) {
            String s = (String)parms.get(i);
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
