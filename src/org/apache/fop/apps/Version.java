/*
 * $Id$
 * 
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
 */

package org.apache.fop.apps;

import java.lang.Character;
import java.util.StringTokenizer;

/**
 * class representing the version of FOP.
 */
public class Version {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    private static final int tagPrefixLen = new String("$Name: ").length();
    private static final int revPrefixLen = new String("$Revision: ").length();
    private static final String unassigned = "FOP Developers Build";
    private static final int NONE = 0;
    private static final int DIGIT = 1;
    private static final int DIGITDASH = 2;
    private static final int DIGITDASHDASH = 3;
    
    /**
     * get the build version of FOP
     *
     * <p>Assumes that the CVS tag in the <em>Name</em> keyword is the tag
     * under which the source code for the build was checked out from
     * CVS.
     * <p>Conforms to the following conventions, assuming that the Name
     * keyword can only contain alphanumerics, hyphens and underlines.<br>
     * `_' Underline is always replaced by a space.<br>
     * `-' Hyphen is unchanged, <em>except</em> that<br>
     * a single hyphen occurring between digits is replaced by a dot `.'<br>
     * a pair of hyphens occurring between digits is replaced by a single
     * hyphen `-'.<br>
     * All other characters are unchanged.
     *
     * @return the version string
     */
    public static String getVersion() {
        String name = getName();
        if (name.equals("")) return unassigned;
        StringTokenizer strtok = new StringTokenizer(name, "_-", true);
        // return delimiters as tokens
        StringBuffer buf = new StringBuffer(name.length());
        int state = NONE;
        while (strtok.hasMoreTokens()) {
            String tok = strtok.nextToken();
            System.out.println(tok);
            char firstchar = tok.charAt(0);
            switch (firstchar) {
            case '-':
                switch (state) {
                case DIGIT:
                    state = DIGITDASH;
                    break;
                    
                case DIGITDASH:
                    state = DIGITDASHDASH;
                    break;

                case DIGITDASHDASH:
                    buf.append("--");
                    
                default:  // DIGITDASHDASH falls through here
                    buf.append('-');
                    state = NONE;
                    break;
                } // end of switch (state)
                
                break;

            case '_':
                tok = " ";  // Change underscore to space unconditionally
                // Fall through to default processing

            default:
                switch (state) {
                case DIGITDASHDASH:
                    if (Character.isDigit(firstchar)) {
                        buf.append('-');
                        break;
                    }
                    buf.append("--");
                    break;
                    
                case DIGITDASH:
                    if (Character.isDigit(firstchar)) {
                        buf.append('.');
                        break;
                    }
                    buf.append('-');
                    break;

                } // end of switch (state)
                
                buf.append(tok);  // Append the (non-dash) token
                //  Check the last character of the (non-dash) token
                if (Character.isDigit(tok.charAt(tok.length() - 1))) {
                    state = DIGIT;
                } else {
                    state = NONE;
                }
                
            } // end of switch (firstchar)
            
        }
        return buf.toString();
    }

    /**
     * @return String containing the data contents of the Revision keyword
     */
    public static String getRevision() {
        if (revision.equals("$Revision" + "$")) return "";
        // 2 is the length of the suffix - " $"
        return revision.substring(revPrefixLen, revision.length() - 2);
    }

    /**
     * @return String containing the data contents of the Name keyword
     */
    public static String getName() {
        if (tag.equals("$Name" + "$")) return "";
        // 2 is the length of the suffix - " $"
        return tag.substring(tagPrefixLen, tag.length() - 2);
    }

}
