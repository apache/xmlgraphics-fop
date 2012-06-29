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

package org.apache.fop.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;


/**
 * Write the Apache license text in various forms
 */
public final class License {

    private License() {
    }

    /**
     * The Apache license text as a string array
     */
    public static final String[] LICENSE
    = {"Licensed to the Apache Software Foundation (ASF) under one or more",
       "contributor license agreements.  See the NOTICE file distributed with",
       "this work for additional information regarding copyright ownership.",
       "The ASF licenses this file to You under the Apache License, Version 2.0",
       "(the \"License\"); you may not use this file except in compliance with",
       "the License.  You may obtain a copy of the License at",
       "",
       "     http://www.apache.org/licenses/LICENSE-2.0",
       "",
       "Unless required by applicable law or agreed to in writing, software",
       "distributed under the License is distributed on an \"AS IS\" BASIS,",
       "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
       "See the License for the specific language governing permissions and",
       "limitations under the License."
    };

    /**
     * The subversion Id keyword line
     */
    public static final String ID = "$Id$";

    /**
     * Calculate the maximum line length in the Apache license text
     * for use in formatting
     */
    private static int maxLength;
    static {
        int j = 0;
        for (int i = 0; i < LICENSE.length; ++i) {
            if (j < LICENSE[i].length()) {
                j = LICENSE[i].length();
            }
        }
        maxLength = j;
    }

    /**
     * Write the Apache license text as commented lines for a Java file
     * @param w the writer which writes the comment
     * @throws IOException if the write operation fails
     */
    public static void writeJavaLicenseId(Writer w) throws IOException {
        w.write("/*\n");
        for (int i = 0; i < LICENSE.length; ++i) {
            if (LICENSE[i].equals("")) {
                w.write(" *\n");
            } else {
                w.write(" * " + LICENSE[i] + "\n");
            }
        }
        w.write(" */\n");
        w.write("\n");
        w.write("/* " + ID + " */\n");
    }

    /**
     * Write the Apache license text as commented lines for an XML file
     * @param w the writer which writes the comment
     * @throws IOException if the write operation fails
     */
    public static void writeXMLLicenseId(Writer w) throws IOException {
        for (int i = 0; i < LICENSE.length; ++i) {
            w.write(String.format("<!-- %-" + maxLength + "s -->\n", new Object[] {LICENSE[i]}));
        }
        w.write("\n");
        w.write("<!-- " + ID + " -->\n");
    }

    /**
     * For testing purposes
     * @param args optional, --java or --xml
     * @throws IOException if the write operation fails
     */
    public static void main(String[] args) throws IOException {
        StringWriter w = new StringWriter();
        if (args.length == 0 || args[0].equals("--java")) {
            writeJavaLicenseId(w);
        } else if (args[0].equals("--xml")) {
            writeXMLLicenseId(w);
        }
        System.out.println(w.toString());
    }

}
