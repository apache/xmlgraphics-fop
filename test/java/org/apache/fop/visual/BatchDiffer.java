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

package org.apache.fop.visual;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.writer.ImageWriterUtil;

import org.apache.fop.layoutengine.LayoutEngineTestUtils;

/**
 * This class is used to visually diff bitmap images created through various sources.
 * <p>
 * Here's what the configuration format looks like:
 * <p>
 * <pre>
 * <batch-diff>
 *   <source-directory>C:/Dev/FOP/trunk/test/layoutengine</source-directory>
 *   <filter-disabled>false</filter-disabled>
 *   <max-files>10</max-files>
 *   <target-directory>C:/Temp/diff-out</target-directory>
 *   <resolution>100</resolution>
 *   <stop-on-exception>false</stop-on-exception>
 *   <create-diffs>false</create-diffs>
 *   <stylesheet>C:/Dev/FOP/trunk/test/layoutengine/testcase2fo.xsl</stylesheet>
 *   <producers>
 *     <producer classname="org.apache.fop.visual.BitmapProducerJava2D">
 *       <delete-temp-files>false</delete-temp-files>
 *     </producer>
 *     <producer classname="org.apache.fop.visual.ReferenceBitmapLoader">
 *       <directory>C:/Temp/diff-bitmaps</directory>
 *     </producer>
 *   </producers>
 * </batch-diff>
 * </pre>
 * <p>
 * The optional "filter-disabled" element determines whether the source files should be filtered
 * using the same "disabled-testcases.txt" file used for the layout engine tests. Default: true
 * <p>
 * The optional "max-files" element controls how many files at maximum should be processed.
 * Default is to process all the files found.
 * <p>
 * The optional "resolution" element controls the requested bitmap resolution in dpi for the
 * generated bitmaps. Defaults to 72dpi.
 * <p>
 * The optional "stop-on-exception" element controls whether the batch should be aborted when
 * an exception is caught. Defaults to true.
 * <p>
 * The optional "create-diffs" element controls whether the diff images should be created.
 * Defaults to true.
 * <p>
 * The optional "stylesheet" element allows you to supply an XSLT stylesheet to preprocess all
 * source files with. Default: no stylesheet, identity transform.
 * <p>
 * The "producers" element contains a list of producer implementations with configuration.
 * The "classname" attribute specifies the fully qualified class name for the implementation.
 */
public class BatchDiffer {

    /** Logger */
    protected static Log log = LogFactory.getLog(BatchDiffer.class);

    /**
     * Prints the usage of this app to stdout.
     */
    public static void printUsage() {
        System.out.println("Usage:");
        System.out.println("java " + BatchDiffer.class.getName() + " <cfgfile>");
        System.out.println();
        System.out.println("<cfgfile>: Path to an XML file with the configuration"
                + " for the batch run.");
    }

    /**
     * Runs the batch.
     * @param cfgFile configuration file to use
     * @throws ConfigurationException In case of a problem with the configuration
     * @throws SAXException In case of a problem during SAX processing
     * @throws IOException In case of a I/O problem
     */
    public void runBatch(File cfgFile)
                throws ConfigurationException, SAXException, IOException {
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = cfgBuilder.buildFromFile(cfgFile);
        runBatch(cfg);
    }

    /**
     * Runs the batch
     * @param cfg Configuration for the batch
     * @throws TransformerConfigurationException
     */
    public void runBatch(Configuration cfg) {
        try {
            ProducerContext context = new ProducerContext();
            context.setTargetResolution(cfg.getChild("resolution").getValueAsInteger(72));
            String xslt = cfg.getChild("stylesheet").getValue(null);
            if (xslt != null) {
                try {
                    context.setTemplates(context.getTransformerFactory().newTemplates(
                            new StreamSource(xslt)));
                } catch (TransformerConfigurationException e) {
                    log.error("Error setting up stylesheet", e);
                    throw new RuntimeException("Error setting up stylesheet");
                }
            }
            BitmapProducer[] producers = getProducers(cfg.getChild("producers"));

            //Set up directories
            File srcDir = new File(cfg.getChild("source-directory").getValue());
            if (!srcDir.exists()) {
                throw new RuntimeException("source-directory does not exist: " + srcDir);
            }
            final File targetDir = new File(cfg.getChild("target-directory").getValue());
            targetDir.mkdirs();
            if (!targetDir.exists()) {
                throw new RuntimeException("target-directory is invalid: " + targetDir);
            }
            context.setTargetDir(targetDir);

            boolean stopOnException = cfg.getChild("stop-on-exception").getValueAsBoolean(true);
            final boolean createDiffs = cfg.getChild("create-diffs").getValueAsBoolean(true);

            //RUN!

            IOFileFilter filter = new SuffixFileFilter(new String[] {".xml", ".fo"});
            //Same filtering as in layout engine tests
            if (cfg.getChild("filter-disabled").getValueAsBoolean(true)) {
                filter = LayoutEngineTestUtils.decorateWithDisabledList(filter);
            }
            String manualFilter = cfg.getChild("manual-filter").getValue(null);
            if (manualFilter != null) {
                if (manualFilter.indexOf('*') < 0) {
                    manualFilter = manualFilter + '*';
                }
                filter = new AndFileFilter(
                        new WildcardFileFilter(manualFilter), filter);
            }

            int maxfiles = cfg.getChild("max-files").getValueAsInteger(-1);
            Collection files = FileUtils.listFiles(srcDir, filter, null);
            Iterator i = files.iterator();
            while (i.hasNext()) {
                final File f = (File)i.next();
                try {
                    log.info("---=== " + f + " ===---");
                    long[] times = new long[producers.length];
                    final BufferedImage[] bitmaps = new BufferedImage[producers.length];
                    for (int j = 0; j < producers.length; j++) {
                        times[j] = System.currentTimeMillis();
                        bitmaps[j] = producers[j].produce(f, j, context);
                        times[j] = System.currentTimeMillis() - times[j];
                    }
                    if (log.isDebugEnabled()) {
                        StringBuffer sb = new StringBuffer("Bitmap production times: ");
                        for (int j = 0; j < producers.length; j++) {
                            if (j > 0) {
                                sb.append(", ");
                            }
                            sb.append(times[j]).append("ms");
                        }
                        log.debug(sb.toString());
                    }
                    //Create combined image
                    if (bitmaps[0] == null) {
                        throw new RuntimeException("First producer didn't return a bitmap for "
                                + f + ". Cannot continue.");
                    }

                    Runnable runnable = new Runnable() {
                        public void run() {
                            try {
                                saveBitmaps(targetDir, f, createDiffs, bitmaps);
                            } catch (IOException e) {
                                log.error("IO error while saving bitmaps: " + e.getMessage());
                            }
                        }
                    };
                    //This speeds it up a little on multi-core CPUs (very cheap, I know)
                    new Thread(runnable).start();
                } catch (RuntimeException e) {
                    log.error("Catching RE on file " + f + ": " + e.getMessage());
                    if (stopOnException) {
                        log.error("rethrowing...");
                        throw e;
                    }
                }
                maxfiles = (maxfiles < 0 ? maxfiles : maxfiles - 1);
                if (maxfiles == 0) {
                    break;
                }
            }
        } catch (IOException ioe) {
            log.error("I/O problem while processing", ioe);
            throw new RuntimeException("I/O problem: " + ioe.getMessage());
        } catch (ConfigurationException e) {
            log.error("Error while configuring BatchDiffer", e);
            throw new RuntimeException("Error while configuring BatchDiffer: " + e.getMessage());
        }
    }

    private void saveBitmaps(File targetDir, File srcFile, boolean createDiffs,
            BufferedImage[] bitmaps) throws IOException {
        BufferedImage combined = BitmapComparator.buildCompareImage(bitmaps);

        //Save combined bitmap as PNG file
        File outputFile = new File(targetDir, srcFile.getName() + "._combined.png");
        ImageWriterUtil.saveAsPNG(combined, outputFile);

        if (createDiffs) {
            for (int k = 1; k < bitmaps.length; k++) {
                BufferedImage diff = BitmapComparator.buildDiffImage(
                        bitmaps[0], bitmaps[k]);
                outputFile = new File(targetDir, srcFile.getName() + "._diff" + k + ".png");
                ImageWriterUtil.saveAsPNG(diff, outputFile);
            }
        }
    }

    private BitmapProducer[] getProducers(Configuration cfg) {
        Configuration[] children = cfg.getChildren("producer");
        BitmapProducer[] producers = new BitmapProducer[children.length];
        for (int i = 0; i < children.length; i++) {
            try {
                Class clazz = Class.forName(children[i].getAttribute("classname"));
                producers[i] = (BitmapProducer)clazz.newInstance();
                ContainerUtil.configure(producers[i], children[i]);
            } catch (Exception e) {
                log.error("Error setting up producers", e);
                throw new RuntimeException("Error while setting up producers");
            }
        }
        return producers;
    }

    /**
     * Main method.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Configuration file is missing!");
                printUsage();
                System.exit(-1);
            }
            File cfgFile = new File(args[0]);
            if (!cfgFile.exists()) {
                System.err.println("Configuration file cannot be found: " + args[0]);
                printUsage();
                System.exit(-1);
            }

            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            BatchDiffer differ = new BatchDiffer();
            differ.runBatch(cfgFile);

            System.out.println("Regular exit...");
        } catch (Exception e) {
            System.out.println("Exception caught...");
            e.printStackTrace();
        }
    }

}
