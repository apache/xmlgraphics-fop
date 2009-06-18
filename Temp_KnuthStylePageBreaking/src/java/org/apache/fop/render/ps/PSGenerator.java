/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.awt.geom.AffineTransform;
import java.io.OutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

/**
 * This class is used to output PostScript code to an OutputStream.
 *
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: PSGenerator.java,v 1.3 2003/03/07 09:46:30 jeremias Exp $
 */
public class PSGenerator {

    /** 
     * Indicator for the PostScript interpreter that the value is provided 
     * later in the document (mostly in the %%Trailer section).
     */
    public static final AtendIndicator ATEND = new AtendIndicator() {
    };

    private OutputStream out;
    private boolean commentsEnabled = true;
    
    private Stack graphicsStateStack = new Stack();
    private PSState currentState;
    private DecimalFormat df3 = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
    private DecimalFormat df1 = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.US));
    private DecimalFormat df5 = new DecimalFormat("0.#####", new DecimalFormatSymbols(Locale.US));

    private StringBuffer tempBuffer = new StringBuffer(256);

    /** @see java.io.FilterOutputStream **/
    public PSGenerator(OutputStream out) {
        this.out = out;
        this.currentState = new PSState();
        this.graphicsStateStack.push(this.currentState);
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
     * (Hardcoded to level 3 for the moment.)
     * @return the PostScript level
     */
    public int getPSLevel() {
        return 3; 
    }

    /**
     * Writes a newline character to the OutputStream.
     * 
     * @throws IOException In case of an I/O problem
     */
    public final void newLine() throws IOException {
        out.write('\n');
    }

    /**
     * Formats a double value for PostScript output.
     * 
     * @param value value to format
     * @return the formatted value
     */
    public String formatDouble(double value) {
        return df1.format(value);
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
                    || (text.indexOf(" ") >= 0) 
                    || forceParentheses) {
                        
                sb.append("(");
                for (int i = 0; i < text.length(); i++) {
                    final char c = text.charAt(i);
                    escapeChar(c, sb);
                }
                sb.append(")");
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
        
        PSState state = (PSState)this.currentState.clone();
        this.graphicsStateStack.push(this.currentState);
        this.currentState = state;
    }
    
    /** 
     * Restores the last graphics state of the rendering engine.
     * @exception IOException In case of an I/O problem
     */
    public void restoreGraphicsState() throws IOException {
        writeln("grestore");
        this.currentState = (PSState)this.graphicsStateStack.pop();
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
        writeln("[" + formatDouble5(a) + " "
                    + formatDouble5(b) + " "
                    + formatDouble5(c) + " "
                    + formatDouble5(d) + " "
                    + formatDouble5(e) + " "
                    + formatDouble5(f) + "] concat");
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
        concatMatrix(matrix);                   
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
     * Returns the current graphics state.
     * @return the current graphics state
     */
    public PSState getCurrentState() {
        return this.currentState;
    }

    
    /** Used for the ATEND constant. See there. */
    private static interface AtendIndicator {
    }

}
