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

package org.apache.fop.cli;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.fop.apps.FOPException;

public class CommandLineOptions2TestCase {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    /**
     * Check that the parser detects the case where the -xsl switch has been omitted.
     * This detection prevents the XSL file being deleted.
     * @throws FOPException
     * @throws IOException
     */
    @Test
    public void testXslSwitchMissing() throws FOPException, IOException {
        final String xslFilename = "../fop/examples/embedding/xml/xslt/projectteam2fo.xsl";
        final String outputFilename = "out.pdf";
        exceptionRule.expect(FOPException.class);
        exceptionRule.expectMessage("Don't know what to do with " + outputFilename);
        // -xsl switch omitted.
        String cmdLine = "-xml ../fop/examples/embedding/xml/xml/projectteam.xml " + xslFilename + " " + outputFilename;
        String[] args = cmdLine.split(" ");
        CommandLineOptions clo = new CommandLineOptions();
        clo.parse(args);
    }

    /**
     * Check that the XSL file is not deleted in the case where the -xsl switch has been omitted.
     * @throws FOPException
     * @throws IOException
     */
    @Test
    public void testXslFileNotDeleted() {
        Main.SystemWrapper mockSystemWrapper = mock(Main.SystemWrapper.class);
        final String xslFilename = "../fop/examples/embedding/xml/xslt/projectteam2fo.xsl";
        // -xsl switch omitted.
        String cmdLine = "-xml ../fop/examples/embedding/xml/xml/projectteam.xml " + xslFilename + " out.pdf";
        String[] args = cmdLine.split(" ");
        Main.startFOP(args, mockSystemWrapper);
        verify(mockSystemWrapper).exit(1);
        File file = new File(xslFilename);
        assertTrue(file.exists());
    }
}
