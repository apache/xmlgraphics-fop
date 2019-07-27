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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.quote;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.extensions.Link;
import org.apache.fop.render.intermediate.extensions.URIAction;
import org.apache.fop.render.pdf.PDFDocumentHandler;

@RunWith(Parameterized.class)
public class PDFLinkTestCase {
    private String target;
    private String expected;

    public PDFLinkTestCase(String target, String expected) {
        this.target = target;
        this.expected = expected;
    }

    @Parameters
    public static Collection links() {
        return Arrays.asList(new Object[][] {
            // Windows absolute paths
            {"c:\\foobar.txt", quote("<< /Type /Filespec /F (c:/foobar.txt)")}, //0
            {"c:\\foo bar.txt", quote("<< /Type /Filespec /F (c:/foo bar.txt)")},
            {"c:\\foo\\bar.txt", quote("<< /Type /Filespec /F (c:/foo/bar.txt)")},
            {"c:\\foo\\bar 2.txt", quote("<< /Type /Filespec /F (c:/foo/bar 2.txt)")},

            // Windows absolute paths using "/"
            {"c:/foo bar.txt", quote("<< /Type /Filespec /F (c:/foo bar.txt)")}, //4
            {"c:/foo/bar.txt", quote("<< /Type /Filespec /F (c:/foo/bar.txt)")},
            {"c:/foo/bar 2.txt", quote("<< /Type /Filespec /F (c:/foo/bar 2.txt)")},

            // Linux absolute paths
            {"/foobar.txt", quote("<< /Type /Filespec /F (/foobar.txt)")}, //7
            {"/foo/bar.txt", quote("<< /Type /Filespec /F (/foo/bar.txt)")},
            {"/foo/bar 2.txt", quote("<< /Type /Filespec /F (/foo/bar 2.txt)")},
            {"/foo bar.txt", quote("<< /Type /Filespec /F (/foo bar.txt)")},

            // Relative paths
            {"foobar.txt", quote("<< /URI (foobar.txt)")}, //11
            {"foo bar.txt", quote("<< /URI (foo%20bar.txt)")},
            {"./foobar.txt", quote("<< /URI (./foobar.txt)")},
            {"./foo bar.txt", quote("<< /URI (./foo%20bar.txt)")},
            {"../foobar.txt", quote("<< /URI (../foobar.txt)")},
            {"../foo bar.txt", quote("<< /URI (../foo%20bar.txt)")},

            // Windows network paths
            {"\\\\foo\\bar.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar.txt)")}, //17
            {"\\\\foo\\bar 2.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar 2.txt)")},
            {"\\\\foo\\a\\bar.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar.txt)")},
            {"\\\\foo\\a\\bar 2.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar 2.txt)")},

            // Windows network path using "/"
            {"//foo/a/bar.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar.txt)")}, // 21
            {"//foo/a/bar 2.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar 2.txt)")},

            // Non ASCII
            // foo bar.txt (unicode)
            {"\uFF46\uFF4F\uFF4F\u3000\uFF42\uFF41\uFF52.txt", quote(
                    "<< /URI (%EF%BD%86%EF%BD%8F%EF%BD%8F%E3%80%80%EF%BD%82%EF%BD%81%EF%BD%92.txt)")}, //23
            // c:/foo/foo bar.txt (unicode)
            {"c:/foo/\uFF46\uFF4F\uFF4F\u3000\uFF42\uFF41\uFF52.txt", quote(
                    "<< /Type /Filespec /F "
                            + "<FEFF0063003A002F0066006F006F002FFF46FF4FFF4F3000FF42FF41FF52002E007400780074> "
                            + "/UF <FEFF0063003A002F0066006F006F002FFF46FF4FFF4F3000FF42FF41FF52002E007400780074>")},
            // \\foo\bar 2\foo bar.txt (unicode)
            {"\\\\foo\\bar 2\\\uFF46\uFF4F\uFF4F\u3000\uFF42\uFF41\uFF52.txt", quote("<< /Type /Filespec /F "
            + "<FEFF005C005C0066006F006F005C00620061007200200032005CFF46FF4FFF4F3000FF42FF41FF52002E007400780074> "
            + "/UF "
            + "<FEFF005C005C0066006F006F005C00620061007200200032005CFF46FF4FFF4F3000FF42FF41FF52002E007400780074>")},

            // PDF, Windows absolute paths
            {"c:\\foobar.pdf", quote("<< /Type /Filespec /F (c:/foobar.pdf)")}, //26
            {"c:\\foo bar.pdf", quote("<< /Type /Filespec /F (c:/foo bar.pdf)")},
            {"c:\\foo\\bar.pdf", quote("<< /Type /Filespec /F (c:/foo/bar.pdf)")},
            {"c:\\foo\\bar 2.pdf", quote("<< /Type /Filespec /F (c:/foo/bar 2.pdf)")},

            // PDF, Linux absolute paths
            {"/foobar.pdf", quote("<< /Type /Filespec /F (/foobar.pdf)")}, //30
            {"/foo bar.pdf", quote("<< /Type /Filespec /F (/foo bar.pdf)")},
            {"/foo/bar.pdf", quote("<< /Type /Filespec /F (/foo/bar.pdf)")},
            {"/foo/bar 2.pdf", quote("<< /Type /Filespec /F (/foo/bar 2.pdf)")},

            // PDF, Relative paths
            {"foobar.pdf", quote("<< /URI (foobar.pdf)")}, //34
            {"foo bar.pdf", quote("<< /URI (foo%20bar.pdf)")},
            {"./foobar.pdf", quote("<< /URI (./foobar.pdf)")},
            {"./foo bar.pdf", quote("<< /URI (./foo%20bar.pdf)")},
            {"../foobar.pdf", quote("<< /URI (../foobar.pdf)")},
            {"../foo bar.pdf", quote("<< /URI (../foo%20bar.pdf)")},

            // PDF, Windows network paths
            {"\\\\foo\\bar.pdf", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar.pdf)")}, //40
            {"\\\\foo\\bar 2.pdf", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar 2.pdf)")},
            {"\\\\foo\\a\\bar.pdf", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar.pdf)")},
            {"\\\\foo\\a\\bar 2.pdf", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar 2.pdf)")},

            // PDF with fragments, Windows absolute paths
            {"c:\\foobar.pdf#page=2", quote("<< /Type /Filespec /F (c:/foobar.pdf)") + ".*" + quote("/S /GoToR") + ".*"
                    + quote("/D [ 2 /XYZ null null null ]")}, //44
            {"c:\\foo bar.pdf#dest=aa", quote("<< /Type /Filespec /F (c:/foo bar.pdf)") + ".*" + quote("/S /GoToR")
                    + ".*" + quote("/D (aa)")},
            {"c:\\foo\\bar.pdf#page=2", quote("<< /Type /Filespec /F (c:/foo/bar.pdf)") + ".*" + quote("/S /GoToR")
                    + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"c:\\foo\\bar 2.pdf#dest=aa", quote("<< /Type /Filespec /F (c:/foo/bar 2.pdf)") + ".*" + quote("/S /GoToR")
                    + ".*" + quote("/D (aa)")},

            // PDF with fragments, Windows absolute paths using "/"
            {"c:/foo bar.pdf#page=2", quote("<< /Type /Filespec /F (c:/foo bar.pdf)") + ".*" + quote("/S /GoToR")
                    + ".*" + quote("/D [ 2 /XYZ null null null ]")}, //48
            {"c:/foo/bar.pdf#dest=aa", quote("<< /Type /Filespec /F (c:/foo/bar.pdf)") + ".*" + quote("/S /GoToR")
                    + ".*" + quote("/D (aa)")},
            {"c:/foo/bar 2.pdf#page=2", quote("<< /Type /Filespec /F (c:/foo/bar 2.pdf)") + ".*" + quote("/S /GoToR")
                    + ".*" + quote("/D [ 2 /XYZ null null null ]")},

            // PDF with fragments, Linux absolute paths
            {"/foobar.pdf#dest=aa", quote("<< /Type /Filespec /F (/foobar.pdf)") + ".*" + quote("/S /GoToR") + ".*"
                    + quote("/D (aa)")}, //51
            {"/foo/bar.pdf#page=2", quote("<< /Type /Filespec /F (/foo/bar.pdf)") + ".*" + quote("/S /GoToR") + ".*"
                    + quote("/D [ 2 /XYZ null null null ]")},
            {"/foo/bar 2.pdf#dest=aa", quote("<< /Type /Filespec /F (/foo/bar 2.pdf)") + ".*" + quote("/S /GoToR")
                    + ".*" + quote("/D (aa)")},
            {"/foo bar.pdf#page=2", quote("<< /Type /Filespec /F (/foo bar.pdf)") + ".*" + quote("/S /GoToR") + ".*"
                    + quote("/D [ 2 /XYZ null null null ]")},

            // PDF with fragments, Relative paths
            {"foobar.pdf#dest=aa", quote("<< /URI (foobar.pdf#dest=aa)")}, //55
            {"foo bar.pdf#page=2", quote("<< /URI (foo%20bar.pdf#page=2)")},
            {"./foobar.pdf#dest=aa", quote("<< /URI (./foobar.pdf#dest=aa)")},
            {"./foo bar.pdf#page=2", quote("<< /URI (./foo%20bar.pdf#page=2)")},
            {"../foobar.pdf#dest=aa", quote("<< /URI (../foobar.pdf#dest=aa)")},
            {"../foo bar.pdf#page=2", quote("<< /URI (../foo%20bar.pdf#page=2)")},

            // PDF with fragments, Windows network paths
            {"\\\\foo\\bar.pdf#dest=aa", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")}, //61
            {"\\\\foo\\bar 2.pdf#page=2", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar 2.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"\\\\foo\\a\\bar.pdf#dest=aa", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"\\\\foo\\a\\bar 2.pdf#page=2", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar 2.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},

            // file:// prefix, Windows absolute paths
            {"file://c:\\foobar.txt", quote("<< /Type /Filespec /F (c:/foobar.txt)")}, //65
            {"file://c:\\foo bar.txt", quote("<< /Type /Filespec /F (c:/foo bar.txt)")},
            {"file://c:\\foo\\bar.txt", quote("<< /Type /Filespec /F (c:/foo/bar.txt)")},
            {"file://c:\\foo\\bar 2.txt", quote("<< /Type /Filespec /F (c:/foo/bar 2.txt)")},

            // file:// prefix, Windows absolute paths using "/"
            {"file://c:/foo bar.txt", quote("<< /Type /Filespec /F (c:/foo bar.txt)")}, //69
            {"file://c:/foo/bar.txt", quote("<< /Type /Filespec /F (c:/foo/bar.txt)")},
            {"file://c:/foo/bar 2.txt", quote("<< /Type /Filespec /F (c:/foo/bar 2.txt)")},

            // file:// prefix, Linux absolute paths
            {"file:///foobar.txt", quote("<< /Type /Filespec /F (/foobar.txt)")}, //72
            {"file:///foo/bar.txt", quote("<< /Type /Filespec /F (/foo/bar.txt)")},
            {"file:///foo/bar 2.txt", quote("<< /Type /Filespec /F (/foo/bar 2.txt)")},
            {"file:///foo bar.txt", quote("<< /Type /Filespec /F (/foo bar.txt)")},

            // file:// prefix, Relative paths
            {"file://foobar.txt", quote("<< /Type /Filespec /F (foobar.txt)")}, //76
            {"file://foo bar.txt", quote("<< /Type /Filespec /F (foo bar.txt)")},
            {"file://./foobar.txt", quote("<< /Type /Filespec /F (./foobar.txt)")},
            {"file://./foo bar.txt", quote("<< /Type /Filespec /F (./foo bar.txt)")},
            {"file://../foobar.txt", quote("<< /Type /Filespec /F (../foobar.txt)")},
            {"file://../foo bar.txt", quote("<< /Type /Filespec /F (../foo bar.txt)")},

            // file:// prefix, Windows network paths
            {"file://\\\\foo\\bar.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar.txt)")}, //82
            {"file://\\\\foo\\bar 2.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar 2.txt)")},
            {"file://\\\\foo\\a\\bar.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar.txt)")},
            {"file://\\\\foo\\a\\bar 2.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar 2.txt)")},

            // file:// prefix, Windows network path using "/"
            {"file:////foo/a/bar.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar.txt)")}, // 86
            {"file:////foo/a/bar 2.txt", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar 2.txt)")},
            {"file:////foobar.txt", quote("<< /Type /Filespec /F (\\\\\\\\foobar.txt)")},

            // Proper file:// for windows paths
            {"file:///c:/foo%20bar.txt", quote("<< /Type /Filespec /F (c:/foo bar.txt)")}, //89
            {"file:///c:/foo/bar.txt", quote("<< /Type /Filespec /F (c:/foo/bar.txt)")},
            {"file:///c:/foo/bar%202.txt", quote("<< /Type /Filespec /F (c:/foo/bar 2.txt)")},

            // Proper file:// for linux paths
            {"file:///foo/bar%202.txt", quote("<< /Type /Filespec /F (/foo/bar 2.txt)")}, //92

            // file:// PDF, Windows absolute paths
            {"file://c:\\foobar.pdf", quote("<< /Type /Filespec /F (c:/foobar.pdf)")}, //93
            {"file://c:\\foo bar.pdf", quote("<< /Type /Filespec /F (c:/foo bar.pdf)")},
            {"file://c:\\foo\\bar.pdf", quote("<< /Type /Filespec /F (c:/foo/bar.pdf)")},
            {"file://c:\\foo\\bar 2.pdf", quote("<< /Type /Filespec /F (c:/foo/bar 2.pdf)")},

            // file:// PDF, Linux absolute paths
            {"file:///foobar.pdf", quote("<< /Type /Filespec /F (/foobar.pdf)")}, //97
            {"file:///foo bar.pdf", quote("<< /Type /Filespec /F (/foo bar.pdf)")},
            {"file:///foo/bar.pdf", quote("<< /Type /Filespec /F (/foo/bar.pdf)")},
            {"file:///foo/bar 2.pdf", quote("<< /Type /Filespec /F (/foo/bar 2.pdf)")},

            // file:// PDF, Relative paths
            {"file://foobar.pdf", quote("<< /Type /Filespec /F (foobar.pdf)")}, //101
            {"file://foo bar.pdf", quote("<< /Type /Filespec /F (foo bar.pdf)")},
            {"file://./foobar.pdf", quote("<< /Type /Filespec /F (./foobar.pdf)")},
            {"file://./foo bar.pdf", quote("<< /Type /Filespec /F (./foo bar.pdf)")},
            {"file://../foobar.pdf", quote("<< /Type /Filespec /F (../foobar.pdf)")},
            {"file://../foo bar.pdf", quote("<< /Type /Filespec /F (../foo bar.pdf)")},

            // file:// PDF, Windows network paths
            {"file://\\\\foo\\bar.pdf", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar.pdf)")}, //107
            {"file://\\\\foo\\bar 2.pdf", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar 2.pdf)")},
            {"file://\\\\foo\\a\\bar.pdf", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar.pdf)")},
            {"file://\\\\foo\\a\\bar 2.pdf", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar 2.pdf)")},

            // Proper file:// for windows paths
            {"file:///c:/foo%20bar.pdf", quote("<< /Type /Filespec /F (c:/foo bar.pdf)")}, //111
            {"file:///c:/foo/bar.pdf", quote("<< /Type /Filespec /F (c:/foo/bar.pdf)")},
            {"file:///c:/foo/bar%202.pdf", quote("<< /Type /Filespec /F (c:/foo/bar 2.pdf)")},

            // Proper file:// PDF, for linux paths
            {"file:///foo/bar%202.pdf", quote("<< /Type /Filespec /F (/foo/bar 2.pdf)")}, //114

            // file:// PDF with fragments, Windows absolute paths
            {"file://c:\\foobar.pdf#page=2", quote("<< /Type /Filespec /F (c:/foobar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")}, //115
            {"file://c:\\foo bar.pdf#dest=aa", quote("<< /Type /Filespec /F (c:/foo bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"file://c:\\foo\\bar.pdf#page=2", quote("<< /Type /Filespec /F (c:/foo/bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"file://c:\\foo\\bar 2.pdf#dest=aa", quote("<< /Type /Filespec /F (c:/foo/bar 2.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")},

            // file:// PDF with fragments, Windows absolute paths using "/"
            {"file://c:/foo bar.pdf#page=2", quote("<< /Type /Filespec /F (c:/foo bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")}, //119
            {"file://c:/foo/bar.pdf#dest=aa", quote("<< /Type /Filespec /F (c:/foo/bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"file://c:/foo/bar 2.pdf#page=2", quote("<< /Type /Filespec /F (c:/foo/bar 2.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},

            // file:// PDF with fragments, Linux absolute paths
            {"file:///foobar.pdf#dest=aa", quote("<< /Type /Filespec /F (/foobar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")}, //122
            {"file:///foo/bar.pdf#page=2", quote("<< /Type /Filespec /F (/foo/bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"file:///foo/bar 2.pdf#dest=aa", quote("<< /Type /Filespec /F (/foo/bar 2.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"file:///foo bar.pdf#page=2", quote("<< /Type /Filespec /F (/foo bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},

            // file:// PDF with fragments, Relative paths
            {"file://foobar.pdf#dest=aa", quote("<< /Type /Filespec /F (foobar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")}, //126
            {"file://foo bar.pdf#page=2", quote("<< /Type /Filespec /F (foo bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"file://./foobar.pdf#dest=aa", quote("<< /Type /Filespec /F (./foobar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"file://./foo bar.pdf#page=2", quote("<< /Type /Filespec /F (./foo bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"file://../foobar.pdf#dest=aa", quote("<< /Type /Filespec /F (../foobar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"file://../foo bar.pdf#page=2", quote("<< /Type /Filespec /F (../foo bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},

            // file:// PDF with fragments, Windows network paths
            {"file://\\\\foo\\bar.pdf#dest=aa", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")}, //132
            {"file://\\\\foo\\bar 2.pdf#page=2", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar 2.pdf)")
                    + ".*" + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"file://\\\\foo\\a\\bar.pdf#dest=aa", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar.pdf)")
                    + ".*" + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"file://\\\\foo\\a\\bar 2.pdf#page=2",
                    quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar 2.pdf)") + ".*"
                            + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},

            // Proper file:// PDF with fragments, Windows network paths
            {"file:////foo/bar.pdf#dest=aa", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")}, //136
            {"file:////foo/bar%202.pdf#page=2", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\bar 2.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"file:////foo/a/bar.pdf#dest=aa", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar.pdf)")
                    + ".*" + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"file:////foo/a/bar%202.pdf#page=2", quote("<< /Type /Filespec /F (\\\\\\\\foo\\\\a\\\\bar 2.pdf)")
                    + ".*" + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},

            // Proper file:// PDF, for linux paths
            {"file:///foo/bar%202.pdf#page=2", quote("<< /Type /Filespec /F (/foo/bar 2.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")}, //140

            // file: Relative paths
            {"file:foobar.txt", quote("<< /Type /Filespec /F (foobar.txt)")}, //141
            {"file:foo bar.txt", quote("<< /Type /Filespec /F (foo bar.txt)")},
            {"file:./foobar.txt", quote("<< /Type /Filespec /F (./foobar.txt)")},
            {"file:./foo bar.txt", quote("<< /Type /Filespec /F (./foo bar.txt)")},
            {"file:../foobar.txt", quote("<< /Type /Filespec /F (../foobar.txt)")},
            {"file:../foo bar.txt", quote("<< /Type /Filespec /F (../foo bar.txt)")},
            {"file:\uFF46\uFF4F\uFF4F\u3000\uFF42\uFF41\uFF52.txt",
                    quote("<< /Type /Filespec /F <FEFFFF46FF4FFF4F3000FF42FF41FF52002E007400780074> "
                            + "/UF <FEFFFF46FF4FFF4F3000FF42FF41FF52002E007400780074>")},

            // file: PDF Relative paths
            {"file:foobar.pdf", quote("<< /Type /Filespec /F (foobar.pdf)")}, //148
            {"file:foo bar.pdf", quote("<< /Type /Filespec /F (foo bar.pdf)")},
            {"file:./foobar.pdf", quote("<< /Type /Filespec /F (./foobar.pdf)")},
            {"file:./foo bar.pdf", quote("<< /Type /Filespec /F (./foo bar.pdf)")},
            {"file:../foobar.pdf", quote("<< /Type /Filespec /F (../foobar.pdf)")},
            {"file:../foo bar.pdf", quote("<< /Type /Filespec /F (../foo bar.pdf)")},

            // file: PDF with fragments, Relative paths
            {"file:foobar.pdf#dest=aa", quote("<< /Type /Filespec /F (foobar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")}, //154
            {"file:foo bar.pdf#page=2", quote("<< /Type /Filespec /F (foo bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"file:./foobar.pdf#dest=aa", quote("<< /Type /Filespec /F (./foobar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"file:./foo bar.pdf#page=2", quote("<< /Type /Filespec /F (./foo bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},
            {"file:../foobar.pdf#dest=aa", quote("<< /Type /Filespec /F (../foobar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D (aa)")},
            {"file:../foo bar.pdf#page=2", quote("<< /Type /Filespec /F (../foo bar.pdf)") + ".*"
                    + quote("/S /GoToR") + ".*" + quote("/D [ 2 /XYZ null null null ]")},

            // file: prefix, Windows absolute paths
            {"file:c:\\foobar.txt", quote("<< /Type /Filespec /F (c:/foobar.txt)")}, //160
            {"file:c:\\foo bar.txt", quote("<< /Type /Filespec /F (c:/foo bar.txt)")},
            {"file:c:\\foo\\bar.txt", quote("<< /Type /Filespec /F (c:/foo/bar.txt)")},
            {"file:c:\\foo\\bar 2.txt", quote("<< /Type /Filespec /F (c:/foo/bar 2.txt)")},

            // PDF, Linux absolute paths
            {"file:/foobar.pdf", quote("<< /Type /Filespec /F (/foobar.pdf)")}, //164
            {"file:/foo bar.pdf", quote("<< /Type /Filespec /F (/foo bar.pdf)")},
            {"file:/foo%20bar.pdf", quote("<< /Type /Filespec /F (/foo bar.pdf)")},
            {"file:/foo/bar.pdf", quote("<< /Type /Filespec /F (/foo/bar.pdf)")},
            {"file:/foo/bar 2.pdf", quote("<< /Type /Filespec /F (/foo/bar 2.pdf)")},
            {"file:/foo/bar%202.pdf", quote("<< /Type /Filespec /F (/foo/bar 2.pdf)")},

            // Web links
            {"https://xmlgraphics.apache.org/fop/", quote("<< /URI (https://xmlgraphics.apache.org/fop/)")}, //170
            {"http://xmlgraphics.apache.org/fop/", quote("<< /URI (http://xmlgraphics.apache.org/fop/)")},
            {"https://xmlgraphics.apache.org/fop/examples.html",
                    quote("<< /URI (https://xmlgraphics.apache.org/fop/examples.html)")},
            {"https://xmlgraphics.apache.org/fop/fo/fonts.fo.pdf",
                    quote("<< /URI (https://xmlgraphics.apache.org/fop/fo/fonts.fo.pdf)")},
            {"https://xmlgraphics.apache.org/fop/fo/fonts.fo.pdf#page=2",
                    quote("<< /URI (https://xmlgraphics.apache.org/fop/fo/fonts.fo.pdf#page=2)")},
            {"https://xmlgraphics.apache.org/fop/fo/fonts.fo",
                    quote("<< /URI (https://xmlgraphics.apache.org/fop/fo/fonts.fo)")},

            // HTML files
            {"examples.html#foo", quote("<< /URI (examples.html#foo)")}, //177
            {"examples.html?foo#bar", quote("/URI (examples.html?foo#bar)")},
            {"examples.html", quote("<< /URI (examples.html)")},
            {"file:examples.html", quote("<< /Type /Filespec /F (examples.html)")},

            // parenthesis
            {"simple_report_(version2.pdf", quote("<< /URI (simple_report_\\(version2.pdf)")}
        });
    }

    @Test
    public void testLinks() throws IFException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        PDFDocumentHandler docHandler = new PDFDocumentHandler(new IFContext(ua));
        docHandler.setFontInfo(new FontInfo());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        docHandler.setResult(new StreamResult(out));
        docHandler.startDocument();
        docHandler.startPage(0, "", "", new Dimension());
        docHandler.getDocumentNavigationHandler().renderLink(new Link(
                new URIAction(target, false), new Rectangle()));
        docHandler.endDocument();

        // Normalize spaces between word for easier testing
        String outString = out.toString().replaceAll("\\s+", " ");

        Pattern r = Pattern.compile(expected);
        Matcher m = r.matcher(outString);
        Assert.assertTrue(m.find());
    }
}
