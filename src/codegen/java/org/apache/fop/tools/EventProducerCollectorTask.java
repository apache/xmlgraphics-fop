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

package org.apache.fop.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;

public class EventProducerCollectorTask extends Task {

    private List filesets = new java.util.ArrayList();
    private File modelFile;
    private File translationFile;
    
    /** {@inheritDoc} */
    public void execute() throws BuildException {
        try {
            EventProducerCollector collector = new EventProducerCollector();
            processFileSets(collector);
            getModelFile().getParentFile().mkdirs();
            collector.saveModelToXML(getModelFile());
            log("Event model written to " + getModelFile());
            if (getTranslationFile() != null) {
                updateTranslationFile();
            }
        } catch (ClassNotFoundException e) {
            throw new BuildException(e);
        } catch (EventConventionException ece) {
            throw new BuildException(ece);
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
    }
    
    private static final String MODEL2TRANSLATION = "model2translation.xsl";
    private static final String MERGETRANSLATION = "merge-translation.xsl";
    
    protected void updateTranslationFile() throws IOException {
        try {
            boolean resultExists = getTranslationFile().exists();
            SAXTransformerFactory tFactory
                = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
            
            //Generate fresh generated translation file as template
            Source src = new StreamSource(getModelFile());
            StreamSource xslt1 = new StreamSource(
                    getClass().getResourceAsStream(MODEL2TRANSLATION));
            if (xslt1.getInputStream() == null) {
                throw new FileNotFoundException(MODEL2TRANSLATION + " not found");
            }
            DOMResult domres = new DOMResult();
            Transformer transformer = tFactory.newTransformer(xslt1);
            transformer.transform(src, domres);
            final Node generated = domres.getNode();
            
            Node sourceDocument;
            if (resultExists) {
                //Load existing translation file into memory (because we overwrite it later)
                src = new StreamSource(getTranslationFile());
                domres = new DOMResult();
                transformer = tFactory.newTransformer();
                transformer.transform(src, domres);
                sourceDocument = domres.getNode();
            } else {
                //Simply use generated as source document
                sourceDocument = generated;
            }

            //Generate translation file (with potentially new translations)
            src = new DOMSource(sourceDocument);
            Result res = new StreamResult(getTranslationFile());
            StreamSource xslt2 = new StreamSource(
                    getClass().getResourceAsStream(MERGETRANSLATION));
            if (xslt2.getInputStream() == null) {
                throw new FileNotFoundException(MERGETRANSLATION + " not found");
            }
            transformer = tFactory.newTransformer(xslt2);
            transformer.setURIResolver(new URIResolver() {
                public Source resolve(String href, String base) throws TransformerException {
                    if ("my:dom".equals(href)) {
                        return new DOMSource(generated);
                    }
                    return null;
                }
            });
            if (resultExists) {
                transformer.setParameter("generated-url", "my:dom");
            }
            transformer.transform(src, res);
        } catch (TransformerException te) {
            throw new IOException(te.getMessage());
        }
    }

    protected void processFileSets(EventProducerCollector collector)
            throws IOException, EventConventionException, ClassNotFoundException {
        Iterator iter = filesets.iterator();
        while (iter.hasNext()) {
            FileSet fs = (FileSet)iter.next();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            File directory = fs.getDir(getProject());
            for (int i = 0, c = srcFiles.length; i < c; i++) {
                String filename = srcFiles[i];
                File src = new File(directory, filename);
                collector.scanFile(src, filename);
            }
        }
    }

    public void addFileset(FileSet set) {
        filesets.add(set);
    }
    
    public void setModelFile(File f) {
        this.modelFile = f;
    }
    
    public File getModelFile() {
        return this.modelFile;
    }
    
    public void setTranslationFile(File f) {
        this.translationFile = f;
    }
    
    public File getTranslationFile() {
        return this.translationFile;
    }
    
    public static void main(String[] args) {
        try {
            Project project = new Project();

            EventProducerCollectorTask generator = new EventProducerCollectorTask();
            generator.setProject(project);
            project.setName("Test");
            FileSet fileset = new FileSet();
            fileset.setDir(new File("test/java"));
            
            FilenameSelector selector = new FilenameSelector();
            selector.setName("**/*.java");
            fileset.add(selector);
            generator.addFileset(fileset);
            
            File targetDir = new File("build/codegen1");
            targetDir.mkdirs();
            
            generator.setModelFile(new File("D:/out.xml"));
            generator.setTranslationFile(new File("D:/out1.xml"));
            generator.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
