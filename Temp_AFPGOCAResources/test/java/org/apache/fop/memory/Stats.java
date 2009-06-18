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

package org.apache.fop.memory;

import java.util.Iterator;
import java.util.List;

class Stats {

    private static final int INTERVAL = 2000;

    private long startTime = System.currentTimeMillis();
    private long lastProgressDump = startTime;
    private int pagesProduced;

    private int totalPagesProduced;

    private int step;
    private int stepCount;

    private List samples = new java.util.LinkedList();

    public void checkStats() {
        long now = System.currentTimeMillis();
        if (now > lastProgressDump + INTERVAL) {
            dumpStats();
            reset();
        }
    }

    public void notifyPagesProduced(int count) {
        pagesProduced += count;
        totalPagesProduced += count;
    }

    public void reset() {
        pagesProduced = 0;
        lastProgressDump = System.currentTimeMillis();
    }

    public void dumpStats() {
        long duration = System.currentTimeMillis() - lastProgressDump;

        if (stepCount != 0) {
            int progress = 100 * step / stepCount;
            System.out.println("Progress: " + progress + "%, " + (stepCount - step) + " left");
        }

        long ppm = 60000 * pagesProduced / duration;
        System.out.println("Speed: " + ppm + "ppm");
        samples.add(new Sample((int)ppm));
    }

    public void dumpFinalStats() {
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Final statistics");
        System.out.println("Pages produced: " +totalPagesProduced);
        long ppm = 60000 * totalPagesProduced / duration;
        System.out.println("Average speed: " + ppm + "ppm");
    }

    public String getGoogleChartURL() {
        StringBuffer sb = new StringBuffer("http://chart.apis.google.com/chart?");
        //http://chart.apis.google.com/chart?cht=ls&chd=t:60,40&chs=250x100&chl=Hello|World
        sb.append("cht=ls");
        sb.append("&chd=t:");
        boolean first = true;
        int maxY = 0;
        Iterator iter = samples.iterator();
        while (iter.hasNext()) {
            Sample sample = (Sample)iter.next();
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(sample.ppm);
            maxY = Math.max(maxY, sample.ppm);
        }
        int ceilY = ((maxY / 1000) + 1) * 1000;
        sb.append("&chs=1000x300"); //image size
        sb.append("&chds=0,").append(ceilY); //data scale
        sb.append("&chg=0,20"); //scale steps
        sb.append("&chxt=y");
        sb.append("&chxl=0:|0|" + ceilY);
        return sb.toString();
    }

    private static class Sample {

        private int ppm;

        public Sample(int ppm) {
            this.ppm = ppm;
        }
    }

    public void progress(int step, int stepCount) {
        this.step = step;
        this.stepCount = stepCount;

    }

}
