/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * The RTF library of the FOP project consists of voluntary contributions made by
 * many individuals on behalf of the Apache Software Foundation and was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and contributors of
 * the jfor project (www.jfor.org), who agreed to donate jfor to the FOP project.
 * For more information on the Apache Software Foundation, please
 * see <http://www.apache.org/>.
 */
package org.apache.fop.rtf.rtflib.testdocs;

import java.io.File;
import java.io.IOException;
//import org.apache.fop.rtf.rtflib.jfor.main.JForVersionInfo;

/**  Create test RTF documents from classes found in this package.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 */

public class CreateTestDocuments {
	public static final String TESTDOCS_PACKAGE = "org.apache.fop.rtf.rtflib.testdocs";

	/** List of all TestDocument subclasses from this package */
	private final static String [] classNames = {
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
		if(!outDir.isDirectory() || !outDir.canWrite()) {
			throw new IOException("output directory (" + outDir + ") must exist and be writable");
		}

		for(int i=0; i < classNames.length; i++) {
			createOneTestDocument(classNames[i],outDir);
		}
	}

	/** instantiate one TestDocument and let it generate its document */
	void createOneTestDocument(String className,File outDir)
	throws Exception {
		className = TESTDOCS_PACKAGE + "." + className;
		TestDocument td = null;
		try {
			td = (TestDocument)Class.forName(className).newInstance();
		} catch(Exception e) {
			throw new Exception("unable to instantiate '" + className + " as a TestDocument object: " + e);
		}
		td.setOutputDir(outDir);
		td.generateOutput();
	}

	/** execute this to create test documents from all classes listed in classNames array */
	public static void main(String args[])
	throws Exception {
		if(args.length < 1) {
			System.err.println("usage: CreateTestDocuments <output directory>");
			System.exit(1);
		}

//		System.err.println("CreateTestDocuments - using " + JForVersionInfo.getLongVersionInfo());
		System.err.println("Generates documents to test the jfor RTF library.");
		final File outDir = new File(args[0]);
		new CreateTestDocuments(outDir);
		System.err.println("CreateTestDocuments - all done.");
		System.exit(0);
	}
}
