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


/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.render.rtf.rtflib.testdocs;

import java.io.File;
import java.io.IOException;
//import org.apache.fop.render.rtf.rtflib.jfor.main.JForVersionInfo;

/**  Create test RTF documents from classes found in this package.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 */

public class CreateTestDocuments {

    /**
     * package name for the testdocs
     */
    public static final String TESTDOCS_PACKAGE = "org.apache.fop.render.rtf.rtflib.testdocs";

    /** List of all TestDocument subclasses from this package */
    private static final String [] CLASS_NAMES = {
        "SimpleDocument",
        "TextAttributes",
        "SimpleTable",
        "SimpleLists",
        "ListInTable",
        "Whitespace",
        "MergedTableCells",
        "NestedTable",
        "ExternalGraphic",
        "BasicLink",
        "ParagraphAlignment"
    };

    CreateTestDocuments(File outDir)
    throws Exception {
        if (!outDir.isDirectory() || !outDir.canWrite()) {
            throw new IOException("output directory (" + outDir + ") must exist and be writable");
        }

        for (int i = 0; i < CLASS_NAMES.length; i++) {
            createOneTestDocument(CLASS_NAMES[i], outDir);
        }
    }

    /** instantiate one TestDocument and let it generate its document */
    void createOneTestDocument(String className, File outDir)
            throws Exception {
        className = TESTDOCS_PACKAGE + "." + className;
        TestDocument td = null;
        try {
            td = (TestDocument)Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new Exception("unable to instantiate '" + className
                    + " as a TestDocument object: " + e);
        }
        td.setOutputDir(outDir);
        try {
            td.generateOutput();
        } catch (Exception e) {
            System.err.println("Error while generating test RTF document:");
            e.printStackTrace();
        }
    }

    /** execute this to create test documents from all classes listed in classNames array
     * @param args String array of arguments
     * @throws Exception for errors
     */
    public static void main(String[] args)
    throws Exception {
        if (args.length < 1) {
            System.err.println("usage: CreateTestDocuments <output directory>");
            System.exit(1);
        }

//        System.err.println("CreateTestDocuments - using " + JForVersionInfo.getLongVersionInfo());
        System.err.println("Generates documents to test the RTF library.");
        final File outDir = new File(args[0]);
        new CreateTestDocuments(outDir);
        System.err.println("CreateTestDocuments - all done.");
        System.exit(0);
    }
}
