/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
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
