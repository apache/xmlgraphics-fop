/*
 * Copyright 1999-2003,2005 The Apache Software Foundation.
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
 
package embedding;

//Java
import java.io.File;
import java.io.IOException;

//JAXP
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXResult;

//Avalon
import org.apache.avalon.framework.ExceptionUtil;

//FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.fo.Constants;

/**
 * This class demonstrates the use of the AWT Viewer.
 */
public class ExampleAWTViewer {

    public void viewFO(File fo)
                throws IOException, FOPException, TransformerException {

        //Setup FOP
        Fop fop = new Fop(Constants.RENDER_AWT);

        try {

            //Load XSL-FO file (you can also do an XSL transformation here)
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            Source src = new StreamSource(fo);
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);

        } catch (Exception e) {
            if (e instanceof FOPException) {
                throw (FOPException)e;
            }
            throw new FOPException(e);
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("FOP ExampleAWTViewer\n");
            System.out.println("Preparing...");

            //Setup directories
            File baseDir = new File(".");
            File outDir = new File(baseDir, "out");
            outDir.mkdirs();

            //Setup input and output files
            File fofile = new File(baseDir, "xml/fo/helloworld.fo");

            System.out.println("Input: XSL-FO (" + fofile + ")");
            System.out.println("Output: AWT Viewer");
            System.out.println();
            System.out.println("Starting AWT Viewer...");

            ExampleAWTViewer app = new ExampleAWTViewer();
            app.viewFO(fofile);

            System.out.println("Success!");
        } catch (Exception e) {
            System.err.println(ExceptionUtil.printStackTrace(e));
            System.exit(-1);
        }
    }
}
