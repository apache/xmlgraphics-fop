/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render.ps;

import java.io.OutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

/**
 * This class is used to output PostScript code to an OutputStream.
 *
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id$
 */
public class PSGenerator {

    /** 
     * Indicator for the PostScript interpreter that the value is provided 
     * later in the document (mostly in the %%Trailer section).
     */
    public static final AtendIndicator ATEND = new AtendIndicator() {};

    private OutputStream out;

    private StringBuffer tempBuffer = new StringBuffer(256);

    /** @see java.io.FilterOutputStream **/
    public PSGenerator(OutputStream out) {
        this.out = out;
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
        NumberFormat nf = new java.text.DecimalFormat("0.#");
        return nf.format(value);
    }

    /**
     * Writes a PostScript command to the stream.
     *
     * @param cmd              The PostScript code to be written.
     * @exception IOException  In case of an I/O problem
     */
    public void write(String cmd) throws IOException {
        if (cmd.length() > 255) {
            throw new RuntimeException("PostScript command exceeded limit of 255 characters");
        }
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
                } else if (params[i] instanceof Number) {
                    tempBuffer.append(params[i].toString());
                } else if (params[i] instanceof Date) {
                    DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    tempBuffer.append(convertStringToDSC(df.format((Date)params[i])));
                } else {
                    throw new IllegalArgumentException("Unsupported parameter type: " 
                            + params[i].getClass().getName());
                }
            }
        }
        writeln(tempBuffer.toString());
    }
    
    
    /** Used for the ATEND constant. See there. */
    private static interface AtendIndicator {
    }

}
