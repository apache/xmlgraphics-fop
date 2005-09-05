/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
 
package org.apache.fop.render.ps;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.OutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.Source;

/**
 * This class is used to output PostScript code to an OutputStream.
 *
 * @author <a href="mailto:fop-dev@xmlgraphics.apache.org">Apache FOP Development Team</a>
 * @version $Id$
 */
public class PSGenerator {

    /** 
     * Indicator for the PostScript interpreter that the value is provided 
     * later in the document (mostly in the %%Trailer section).
     */
    public static final AtendIndicator ATEND = new AtendIndicator() {
    };

    /** Line feed used by PostScript */
    public static final char LF = '\n';
    
    private OutputStream out;
    private boolean commentsEnabled = true;
    
    private Stack graphicsStateStack = new Stack();
    private PSState currentState;
    //private DecimalFormat df3 = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
    private DecimalFormat df3 = new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.US));
    private DecimalFormat df5 = new DecimalFormat("0.#####", new DecimalFormatSymbols(Locale.US));

    private StringBuffer tempBuffer = new StringBuffer(256);

    /** @see java.io.FilterOutputStream **/
    public PSGenerator(OutputStream out) {
        this.out = out;
        this.currentState = new PSState();
        //this.graphicsStateStack.push(this.currentState);
    }
    
    /**
     * Returns the OutputStream the PSGenerator writes to.
     * @return the OutputStream
     */
    public OutputStream getOutputStream() {
        return this.out;
    }

    /**
     * Returns the selected PostScript level. 
     * (Hardcoded to level 2 for the moment.)
     * @return the PostScript level
     */
    public int getPSLevel() {
        return 2; 
    }
    
    /**
     * Attempts to resolve the given URI. PSGenerator should be subclasses to provide more
     * sophisticated URI resolution.
     * @param uri URI to access
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved. 
     */
    public Source resolveURI(String uri) {
        return new javax.xml.transform.stream.StreamSource(uri);
    }
    
    /**
     * Writes a newline character to the OutputStream.
     * 
     * @throws IOException In case of an I/O problem
     */
    public final void newLine() throws IOException {
        out.write(LF);
    }

    /**
     * Formats a double value for PostScript output.
     * 
     * @param value value to format
     * @return the formatted value
     */
    public String formatDouble(double value) {
        return df3.format(value);
    }

    /**
     * Formats a double value for PostScript output (higher resolution).
     * 
     * @param value value to format
     * @return the formatted value
     */
    public String formatDouble5(double value) {
        return df5.format(value);
    }

    /**
     * Writes a PostScript command to the stream.
     *
     * @param cmd              The PostScript code to be written.
     * @exception IOException  In case of an I/O problem
     */
    public void write(String cmd) throws IOException {
        /* @todo Check disabled until clarification.
        if (cmd.length() > 255) {
            throw new RuntimeException("PostScript command exceeded limit of 255 characters");
        } */
        out.write(cmd.getBytes("US-ASCII"));
    }

    /**
     * Writes a PostScript command to the stream and ends the line.
     *
     * @param cmd              The PostScript code to be written.
     * @exception IOException  In case of an I/O problem
     */
    public void writeln(String cmd) throws IOException {
        write(cmd);
        newLine();
    }

    /**
     * Writes a comment to the stream and ends the line. Output of comments can 
     * be disabled to reduce the size of the generated file. 
     * 
     * @param comment          comment to write
     * @exception IOException  In case of an I/O problem
     */
    public void commentln(String comment) throws IOException {
        if (this.commentsEnabled) {
            writeln(comment);
        }
    }

    /**
     * Writes encoded data to the PostScript stream.
     *
     * @param cmd              The encoded PostScript code to be written.
     * @exception IOException  In case of an I/O problem
     */
    public void writeByteArr(byte[] cmd) throws IOException {
        out.write(cmd);
        newLine();
    }
    
    
    /**
     * Flushes the OutputStream.
     * 
     * @exception IOException In case of an I/O problem
     */
    public void flush() throws IOException {
        out.flush();
    }


    /**
     * Escapes a character conforming to the rules established in the PostScript
     * Language Reference (Search for "Literal Text Strings").
     * @param c character to escape
     * @param target target StringBuffer to write the escaped character to
     */
    public static final void escapeChar(char c, StringBuffer target) {
        if (c > 127) {
            target.append("\\");
            target.append(Integer.toOctalString(c));
        } else {
            switch (c) {
                case '\n':
                    target.append("\\n");
                    break;
                case '\r':
                    target.append("\\r");
                    break;
                case '\t':
                    target.append("\\t");
                    break;
                case '\b':
                    target.append("\\b");
                    break;
                case '\f':
                    target.append("\\f");
                    break;
                case '\\':
                    target.append("\\\\");
                    break;
                case '(':
                    target.append("\\(");
                    break;
                case ')':
                    target.append("\\)");
                    break;
                default:
                    target.append(c);
            }
        }
    }


    /**
     * Converts text by applying escaping rules established in the DSC specs.
     * @param text Text to convert
     * @return String The resulting String
     */
    public static final String convertStringToDSC(String text) {
        return convertStringToDSC(text, false);
    }


    /**
     * Converts text by applying escaping rules established in the DSC specs.
     * @param text Text to convert
     * @param forceParentheses Force the use of parentheses
     * @return String The resulting String
     */
    public static final String convertStringToDSC(String text, 
                                                  boolean forceParentheses) {
        if ((text == null) || (text.length() == 0)) {
            return "()";
        } else {
            int initialSize = text.length();
            initialSize += initialSize / 2;
            StringBuffer sb = new StringBuffer(initialSize);
            if ((Long.getLong(text) != null) 
                    || (text.indexOf(' ') >= 0) 
                    || forceParentheses) {
                        
                sb.append('(');
                for (int i = 0; i < text.length(); i++) {
                    final char c = text.charAt(i);
                    escapeChar(c, sb);
                }
                sb.append(')');
                return sb.toString();
            } else {
                return text;
            }
        }
    }


    /**
     * Writes a DSC comment to the output stream.
     * @param name Name of the DSC comment
     * @exception IOException In case of an I/O problem
     * @see org.apache.fop.render.ps.DSCConstants
     */
    public void writeDSCComment(String name) throws IOException {
        writeln("%%" + name);
    }


    /**
     * Writes a DSC comment to the output stream. The parameter to the DSC 
     * comment can be any object. The object is converted to a String as
     * necessary.
     * @param name Name of the DSC comment
     * @param param Single parameter to the DSC comment
     * @exception IOException In case of an I/O problem
     * @see org.apache.fop.render.ps.DSCConstants
     */
    public void writeDSCComment(String name, Object param) throws IOException {
        writeDSCComment(name, new Object[] {param});    
    }

        
    /**
     * Writes a DSC comment to the output stream. The parameters to the DSC 
     * comment can be any object. The objects are converted to Strings as
     * necessary. Please see the source code to find out what parameters are
     * currently supported.
     * @param name Name of the DSC comment
     * @param params Array of parameters to the DSC comment
     * @exception IOException In case of an I/O problem
     * @see org.apache.fop.render.ps.DSCConstants
     */
    public void writeDSCComment(String name, Object[] params) throws IOException {
        tempBuffer.setLength(0);
        tempBuffer.append("%%");
        tempBuffer.append(name);
        if ((params != null) && (params.length > 0)) {
            tempBuffer.append(": ");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    tempBuffer.append(" ");
                }
                
                if (params[i] instanceof String) {
                    tempBuffer.append(convertStringToDSC((String)params[i]));
                } else if (params[i] instanceof AtendIndicator) {
                    tempBuffer.append("(atend)");
                } else if (params[i] instanceof Double) {
                    tempBuffer.append(df3.format(params[i]));
                } else if (params[i] instanceof Number) {
                    tempBuffer.append(params[i].toString());
                } else if (params[i] instanceof Date) {
                    DateFormat datef = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    tempBuffer.append(convertStringToDSC(datef.format((Date)params[i])));
                } else if (params[i] instanceof PSResource) {
                    tempBuffer.append(((PSResource)params[i]).getResourceSpecification());
                } else {
                    throw new IllegalArgumentException("Unsupported parameter type: " 
                            + params[i].getClass().getName());
                }
            }
        }
        writeln(tempBuffer.toString());
    }
    
    
    /**
     * Saves the graphics state of the rendering engine.
     * @exception IOException In case of an I/O problem
     */
    public void saveGraphicsState() throws IOException {
        writeln("gsave");
        
        PSState state = new PSState(this.currentState, false);
        this.graphicsStateStack.push(this.currentState);
        this.currentState = state;
    }
    
    /** 
     * Restores the last graphics state of the rendering engine.
     * @return true if the state was restored, false if there's a stack underflow.
     * @exception IOException In case of an I/O problem
     */
    public boolean restoreGraphicsState() throws IOException {
        if (this.graphicsStateStack.size() > 0) {
            writeln("grestore");
            this.currentState = (PSState)this.graphicsStateStack.pop();
            return true;
        } else {
            return false;
        }
    }
    
    
    /**
     * Returns the current graphics state.
     * @return the current graphics state
     */
    public PSState getCurrentState() {
        return this.currentState;
    }

    /**
     * Concats the transformation matrix.
     * @param a A part
     * @param b B part
     * @param c C part
     * @param d D part
     * @param e E part
     * @param f F part
     * @exception IOException In case of an I/O problem
     */
    public void concatMatrix(double a, double b,
            double c, double d, 
            double e, double f) throws IOException {
        AffineTransform at = new AffineTransform(a, b, c, d, e, f);
        concatMatrix(at);
        
    }
    
    /**
     * Concats the transformations matrix.
     * @param matrix Matrix to use
     * @exception IOException In case of an I/O problem
     */
    public void concatMatrix(double[] matrix) throws IOException {
        concatMatrix(matrix[0], matrix[1], 
                     matrix[2], matrix[3], 
                     matrix[4], matrix[5]);
    }
                                
    /**
     * Concats the transformations matric.
     * @param at the AffineTransform whose matrix to use
     * @exception IOException In case of an I/O problem
     */
    public void concatMatrix(AffineTransform at) throws IOException {
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        getCurrentState().concatMatrix(at);
        writeln("[" + formatDouble5(matrix[0]) + " "
                + formatDouble5(matrix[1]) + " "
                + formatDouble5(matrix[2]) + " "
                + formatDouble5(matrix[3]) + " "
                + formatDouble5(matrix[4]) + " "
                + formatDouble5(matrix[5]) + "] concat");
    }
               
    /**
     * Adds a rectangle to the current path.
     * @param x upper left corner
     * @param y upper left corner
     * @param w width
     * @param h height
     * @exception IOException In case of an I/O problem
     */
    public void defineRect(double x, double y, double w, double h) 
                throws IOException {
        writeln(formatDouble(x) 
            + " " + formatDouble(y) 
            + " " + formatDouble(w) 
            + " " + formatDouble(h) 
            + " re");
    }
    
    /**
     * Establishes the specified line cap style.
     * @param linecap the line cap style (0, 1 or 2) as defined by the setlinecap command.
     * @exception IOException In case of an I/O problem
     */
    public void useLineCap(int linecap) throws IOException {
        if (getCurrentState().useLineCap(linecap)) {
            writeln(linecap + " setlinecap");
        }
    }
                                
    /**
     * Establishes the specified line width.
     * @param width the line width as defined by the setlinewidth command.
     * @exception IOException In case of an I/O problem
     */
    public void useLineWidth(double width) throws IOException {
        if (getCurrentState().useLineWidth(width)) {
            writeln(formatDouble(width) + " setlinewidth");
        }
    }
                                
    /**
     * Establishes the specified dash pattern.
     * @param pattern the dash pattern as defined by the setdash command.
     * @exception IOException In case of an I/O problem
     */
    public void useDash(String pattern) throws IOException {
        if (pattern == null) {
            pattern = PSState.DEFAULT_DASH;
        }
        if (getCurrentState().useDash(pattern)) {
            writeln(pattern + " setdash");
        }
    }
                                
    /**
     * Establishes the specified color (RGB).
     * @param col the color as defined by the setrgbcolor command.
     * @exception IOException In case of an I/O problem
     */
    public void useRGBColor(Color col) throws IOException {
        if (col == null) {
            col = PSState.DEFAULT_RGB_COLOR;
        }
        if (getCurrentState().useColor(col)) {
            float[] comps = col.getColorComponents(null);
            writeln(formatDouble(comps[0])
                    + " " + formatDouble(comps[1])
                    + " " + formatDouble(comps[2])
                    + " setrgbcolor");
        }
    }
    
    /**
     * Establishes the specified font and size.
     * @param name name of the font for the "F" command (see FOP Std Proc Set)
     * @param size size of the font
     * @exception IOException In case of an I/O problem
     */
    public void useFont(String name, float size) throws IOException {
        if (getCurrentState().useFont(name, size)) {
            writeln(name + " " + formatDouble(size) + " F");
        }
    }
    
    private Set documentSuppliedResources;
    private Set documentNeededResources;
    private Set pageResources;
    
    /**
     * Notifies the generator that a new page has been started and that the page resource
     * set can be cleared.
     */
    public void notifyStartNewPage() {
        if (pageResources != null) {
            pageResources.clear();
        }
    }
    
    /**
     * Notifies the generator about the usage of a resource on the current page.
     * @param res the resource being used
     * @param needed true if this is a needed resource, false for a supplied resource
     */
    public void notifyResourceUsage(PSResource res, boolean needed) {
        if (pageResources == null) {
            pageResources = new java.util.HashSet();
        }
        pageResources.add(res);
        if (needed) {
            if (documentNeededResources == null) {
                documentNeededResources = new java.util.HashSet();
            }
            documentNeededResources.add(res);
        } else {
            if (documentSuppliedResources == null) {
                documentSuppliedResources = new java.util.HashSet();
            }
            documentSuppliedResources.add(res);
        }
    }

    /**
     * Indicates whether a particular resource is supplied, rather than needed.
     * @param res the resource
     * @return true if the resource is registered as being supplied.
     */
    public boolean isResourceSupplied(PSResource res) {
        return documentSuppliedResources.contains(res);
    }

    /**
     * Writes a DSC comment for the accumulated used resources, either at page level or
     * at document level.
     * @param pageLevel true if the DSC comment for the page level should be generated, 
     *                  false for the document level (in the trailer)
     * @exception IOException In case of an I/O problem
     */
    public void writeResources(boolean pageLevel) throws IOException {
        if (pageLevel) {
            writeResourceComment(DSCConstants.PAGE_RESOURCES, pageResources);
        } else {
            writeResourceComment(DSCConstants.DOCUMENT_NEEDED_RESOURCES, 
                    documentNeededResources);
            writeResourceComment(DSCConstants.DOCUMENT_SUPPLIED_RESOURCES, 
                    documentSuppliedResources);
        }
    }
    
    private void writeResourceComment(String name, Set resources) throws IOException {
        if (resources == null || resources.size() == 0) {
            return;
        }
        tempBuffer.setLength(0);
        tempBuffer.append("%%");
        tempBuffer.append(name);
        tempBuffer.append(" ");
        boolean first = true;
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            if (!first) {
                writeln(tempBuffer.toString());
                tempBuffer.setLength(0);
                tempBuffer.append("%%+ ");
            }
            PSResource res = (PSResource)i.next();
            tempBuffer.append(res.getResourceSpecification());
            first = false;
        }
        writeln(tempBuffer.toString());
    }
    
    /** Used for the ATEND constant. See there. */
    private static interface AtendIndicator {
    }


}
