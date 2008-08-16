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

package org.apache.fop.prototype;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.fop.prototype.breaking.Alternative;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.knuth.KnuthElement;


/**
 * A class that creates a graph of layouts in the dot format and renders it into PDF.
 */
public class Dot {

    private static final boolean DISPLAY_DEMERITS = false;

    private static String getLayoutID(Layout l) {
        return String.valueOf(l.hashCode());
    }

    private static String getLabel(KnuthElement element) {
        return element.getLabel();
    }

    private static void printConnection(PrintStream s, Layout previous, Layout l, boolean dashed,
            double demerits) {
        s.printf("  \"%s\" -> \"%s\" [", getLayoutID(previous), getLayoutID(l));
        if (dashed) {
            s.print("style=dashed ");
        }
        if (DISPLAY_DEMERITS) {
            s.print("label=\"");
            if (demerits < 1000000.0) {
                s.printf("%,d", Math.round(demerits));
            } else {
                s.printf("%.3g", demerits);
            }
            s.print('"');
        }
        s.println(']');
    }

    private static void dumpLayout(Layout l, PrintStream s, Set<Layout> handled) {
        if (!handled.contains(l)) {
            handled.add(l);
            if (l.getPrevious() == null) {
                // This is the starting layout
                s.print("  \"");
                s.print(getLayoutID(l));
                s.println("\" [label=\"\" shape=box width=0.2 height=0.2]");
            } else if (l.getProgress().getTotalLength() == 0) {
                // This is a final layout
                dumpLayout(l.getPrevious(), s, handled);
            } else {
                Layout previousLayout = l.getPrevious();
                if (previousLayout.getProgress().getTotalLength() == 0
                        && previousLayout.getPrevious() != null) {
                    /*
                     * The previous layout corresponds to a paragraph starting at the top
                     * of a page. It will be empty so go directly to the previous page.
                     */
                    previousLayout = previousLayout.getPrevious();
                }
                dumpLayout(previousLayout, s, handled);
                StringBuilder label = new StringBuilder(
                        "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"left\">");
                String shape = "";
                if (l.isPage()) {
                    shape = "shape=box ";
                }
                for (KnuthElement e: l.getElements()) {
                    label.append(getLabel(e));
                    label.append("</td></tr><tr><td align=\"left\">");
                }
                label.append("</td></tr></table>");
                s.printf("  \"%s\" [%slabel=<%s>]%n", getLayoutID(l), shape, label);
                printConnection(s, previousLayout, l, false, l.getDemerits());
                if (l.getAlternatives() != null) {
                    for (Alternative a: l.getAlternatives()) {
                        Layout altLayout = a.getLayout();
                        if (altLayout.getProgress().getTotalLength() == 0) {
                            // Paragraph starting at top of page, see above
                            altLayout = altLayout.getPrevious();
                        }
                        dumpLayout(altLayout, s, handled);
                        printConnection(s, altLayout, l, true, a.getDemerits());
                    }
                }
            }
        }
    }

    public static void createGraph(Iterable<Layout> layouts) throws Exception {
        PrintStream s = new PrintStream(new File("/tmp/res.dot"));
        Set<Layout> handled = new HashSet<Layout>();
//      page="8.27,11.69"
//      size="11,16"
//      margin="0.3"
        s.println("digraph ActiveNodes  {");
        s.println("nodesep=.5; ranksep=1.5");
        s.println("node [shape=none fontname=\"Nimbus Roman No9 L\"]");
        s.println("edge [dir=none]");
        for (Layout l: layouts) {
            dumpLayout(l, s, handled);
        }
        s.println('}');
        s.close();
        Process p = Runtime.getRuntime().exec(
                new String[] { "dot", "-Tpdf", "-o/tmp/res.pdf", "/tmp/res.dot" });
        p.waitFor();
        InputStream err = p.getErrorStream();
        int b;
        while ((b = err.read()) >= 0) {
            System.err.write(b);
        }
        err.close();
        int exitValue = p.exitValue();
        if (exitValue != 0) {
            System.err.println("dot exited with value " + exitValue);
        }
    }
}
