/*
 * $Id$
 * Copyright (C) 2002-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package embedding;

//Hava
import java.io.File;
import java.io.IOException;

//JAXP
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

//Avalon
import org.apache.avalon.framework.ExceptionUtil;

import embedding.model.*;


/**
 * This class demonstrates the conversion of an arbitrary object file to an 
 * XML file.
 */
public class ExampleObj2XML {

    public void convertProjectTeam2XML(ProjectTeam team, File xml) 
                throws IOException, TransformerException {
                    
        //Setup XSLT
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        /* Note:
           We use the identity transformer, no XSL transformation is done.
           The transformer is basically just used to serialize the 
           generated document to XML. */
    
        //Setup input
        Source src = team.getSourceForProjectTeam();
    
        //Setup output
        Result res = new StreamResult(xml);

        //Start XSLT transformation
        transformer.transform(src, res);
    }


    public static ProjectTeam createSampleProjectTeam() {
        ProjectTeam team = new ProjectTeam();
        team.setProjectName("Rule the Galaxy");
        team.addMember(new ProjectMember("Emperor Palpatine", "lead", "palpatine@empire.gxy"));
        team.addMember(new ProjectMember("Lord Darth Vader", "Jedi-Killer", "vader@empire.gxy"));
        team.addMember(new ProjectMember("Grand Moff Tarkin", "Planet-Killer", "tarkin@empire.gxy"));
        team.addMember(new ProjectMember("Admiral Motti", "Death Star operations", "motti@empire.gxy"));
        return team;
    }


    public static void main(String[] args) {
        try {
            System.out.println("FOP ExampleObj2XML\n");
            System.out.println("Preparing...");
            
            //Setup directories
            File baseDir = new File(".");
            File outDir = new File(baseDir, "out");
            outDir.mkdirs();

            //Setup input and output
            File xmlfile = new File(outDir, "ResultObj2XML.xml");

            System.out.println("Input: a ProjectTeam object");
            System.out.println("Output: XML (" + xmlfile + ")");
            System.out.println();
            System.out.println("Serializing...");

            ExampleObj2XML app = new ExampleObj2XML();
            app.convertProjectTeam2XML(createSampleProjectTeam(), xmlfile);
            
            System.out.println("Success!");
        } catch (Exception e) {
            System.err.println(ExceptionUtil.printStackTrace(e));
            System.exit(-1);
        }
    }
}
