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

package org.apache.fop.threading;

import java.io.File;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;

/**
 * Testbed for multi-threading tests. The class can run a configurable set of task a number of
 * times in a configurable number of threads to easily reproduce multi-threading issues.
 */
public class FOPTestbed extends AbstractLogEnabled
            implements Configurable, Initializable {

    private int repeat;
    private List taskList = new java.util.ArrayList();
    private int threads;
    private File outputDir;
    private Configuration fopCfg;
    private Processor foprocessor;
    private boolean writeToDevNull;

    private int counter = 0;

    private List results = Collections.synchronizedList(new java.util.LinkedList());

    /** {@inheritDoc} */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.threads = configuration.getChild("threads").getValueAsInteger(10);
        this.outputDir = new File(configuration.getChild("output-dir").getValue());
        this.writeToDevNull = configuration.getChild("devnull").getValueAsBoolean(false);
        Configuration tasks = configuration.getChild("tasks");
        this.repeat = tasks.getAttributeAsInteger("repeat", 1);
        Configuration[] entries = tasks.getChildren("task");
        for (int i = 0; i < entries.length; i++) {
            this.taskList.add(new TaskDef(entries[i]));
        }
        this.fopCfg = configuration.getChild("processor");
    }

    /** {@inheritDoc} */
    public void initialize() throws Exception {
        this.foprocessor = createFOProcessor();
    }

    /**
     * Starts the stress test.
     */
    public void doStressTest() {
        getLogger().info("Starting stress test...");
        long start = System.currentTimeMillis();
        this.counter = 0;

        //Initialize threads
        ThreadGroup workerGroup = new ThreadGroup("FOP workers");
        List threadList = new java.util.LinkedList();
        for (int ti = 0; ti < this.threads; ti++) {
            TaskRunner runner = new TaskRunner();
            ContainerUtil.enableLogging(runner, getLogger());
            Thread thread = new Thread(workerGroup, runner, "Worker- " + ti);
            threadList.add(thread);
        }

        //Start threads
        Iterator i = threadList.iterator();
        while (i.hasNext()) {
            ((Thread)i.next()).start();
        }

        //Wait for threads to end
        while (threadList.size() > 0) {
            Thread t = (Thread)threadList.get(0);
            if (!t.isAlive()) {
                threadList.remove(0);
                continue;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                //ignore
            }
        }
        long duration = System.currentTimeMillis() - start;

        report(duration);
    }

    private void report(long duration) {
        int count = this.results.size();
        int failures = 0;
        long bytesWritten = 0;
        System.out.println("Report on " + count + " tasks:");
        Iterator iter = this.results.iterator();
        while (iter.hasNext()) {
            Result res = (Result)iter.next();
            if (res.failure != null) {
                System.out.println("FAIL: " + (res.end - res.start) + " " + res.task);
                System.out.println("  -> " + res.failure.getMessage());
                failures++;
            } else {
                System.out.println("good: " + (res.end - res.start) + " " + res.filesize
                        + " " + res.task);
                bytesWritten += res.filesize;
            }
        }
        System.out.println("Stress test duration: " + duration + "ms");
        if (failures > 0) {
            System.out.println(failures + " failures of " + count + " documents!!!");
        } else {
            float mb = 1024f * 1024f;
            System.out.println("Bytes written: " + (bytesWritten / mb) + " MB, "
                    + (bytesWritten * 1000 / duration) + " bytes / sec");
            System.out.println("NO failures with " + count + " documents.");
        }
    }

    private class TaskRunner extends AbstractLogEnabled implements Runnable {

        public void run() {
            try {
                for (int r = 0; r < repeat; r++) {
                    Iterator i = taskList.iterator();
                    while (i.hasNext()) {
                        TaskDef def = (TaskDef)i.next();
                        final Task task = new Task(def, counter++, foprocessor);
                        ContainerUtil.enableLogging(task, getLogger());
                        task.execute();
                    }
                }
            } catch (Exception e) {
                getLogger().error("Thread ended with an exception", e);
            }
        }

    }

    /**
     * Creates a new FOProcessor.
     * @return the newly created instance
     */
    public Processor createFOProcessor() {
        try {
            Class clazz = Class.forName(this.fopCfg.getAttribute("class",
                    "org.apache.fop.threading.FOProcessorImpl"));
            Processor fop = (Processor)clazz.newInstance();
            ContainerUtil.enableLogging(fop, getLogger());
            ContainerUtil.configure(fop, this.fopCfg);
            ContainerUtil.initialize(fop);
            return fop;
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error creating FO Processor", e);
        }
    }


    private class TaskDef {
        private String fo;
        private String xml;
        private String xslt;
        private Templates templates;

        public TaskDef(String fo) {
            this.fo = fo;
        }

        public TaskDef(Configuration cfg) throws ConfigurationException {
            this.fo = cfg.getAttribute("fo", null);
            if (this.fo == null) {
                this.xml = cfg.getAttribute("xml");
                this.xslt = cfg.getAttribute("xslt", null);
                if (this.xslt != null) {
                    TransformerFactory factory = TransformerFactory.newInstance();
                    Source xsltSource = new StreamSource(new File(xslt));
                    try {
                        this.templates = factory.newTemplates(xsltSource);
                    } catch (TransformerConfigurationException tce) {
                        throw new ConfigurationException("Invalid XSLT", tce);
                    }
                }
            }
        }

        public String getFO() {
            return this.fo;
        }

        public String getXML() {
            return this.xml;
        }

        public Templates getTemplates() {
            return this.templates;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (this.fo != null) {
                sb.append("fo=");
                sb.append(this.fo);
            } else {
                sb.append("xml=");
                sb.append(this.xml);
                sb.append(" xslt=");
                sb.append(this.xslt);
            }
            return sb.toString();
        }
    }


    private class Task extends AbstractLogEnabled implements Executable {

        private TaskDef def;
        private int num;
        private Processor fop;

        public Task(TaskDef def, int num, Processor fop) {
            this.def = def;
            this.num = num;
            this.fop = fop;
        }


        public void execute() throws Exception {
            getLogger().info("Processing: " + def);
            long start = System.currentTimeMillis();
            try {
                DecimalFormat df = new DecimalFormat("00000");
                File outfile = new File(outputDir, df.format(num) + fop.getTargetFileExtension());
                OutputStream out;
                if (writeToDevNull) {
                    out = new NullOutputStream();
                } else {
                    out = new java.io.FileOutputStream(outfile);
                    out = new java.io.BufferedOutputStream(out);
                }
                CountingOutputStream cout = new CountingOutputStream(out);
                try {
                    Source src;
                    Templates templates;

                    if (def.getFO() != null) {
                        src = new StreamSource(new File(def.getFO()));
                        templates = null;
                    } else {
                        src = new StreamSource(new File(def.getXML()));
                        templates = def.getTemplates();
                    }
                    fop.process(src, templates, cout);
                } finally {
                    IOUtils.closeQuietly(cout);
                }
                results.add(new Result(def, start, System.currentTimeMillis(),
                        cout.getByteCount()));
            } catch (Exception e) {
                results.add(new Result(def, start, System.currentTimeMillis(), e));
                throw e;
            }
        }
    }

    private static class Result {

        private TaskDef task;
        private long start;
        private long end;
        private long filesize;
        private Throwable failure;

        public Result(TaskDef task, long start, long end, long filesize) {
            this(task, start, end, null);
            this.filesize = filesize;
        }

        public Result(TaskDef task, long start, long end, Throwable failure) {
            this.task = task;
            this.start = start;
            this.end = end;
            this.failure = failure;
        }
    }

}
