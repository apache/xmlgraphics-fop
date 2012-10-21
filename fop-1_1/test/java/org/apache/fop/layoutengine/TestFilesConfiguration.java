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

package org.apache.fop.layoutengine;

import java.io.File;

/**
 * A class that contains the information needed to run a suite of layout engine and FO tree
 * tests.
 */
public final class TestFilesConfiguration {

    private final File testDirectory;
    private final String singleTest;
    private final String testStartsWith;
    private final String testFileSuffix;
    private final String testSet;
    private final String disabledTests;
    private final boolean privateTests;

    private TestFilesConfiguration(Builder builder) {
        this.testDirectory = new File(builder.testDirectory);
        this.singleTest = builder.singleTest;
        this.testStartsWith = builder.testStartsWith;
        this.testFileSuffix = builder.testFileSuffix;
        this.testSet = builder.testSet;
        this.privateTests = builder.privateTests;
        this.disabledTests = builder.disabledTests;
    }

    /**
     * Returns the directory of the tests.
     * @return the test directory
     */
    public File getTestDirectory() {
        return testDirectory;
    }

    /**
     * Returns the name of the single test file to run.
     * @return the single test file name
     */
    public String getSingleTest() {
        return singleTest;
    }

    /**
     * Returns the string that must prefix the test file names.
     * @return the prefixing string
     */
    public String getStartsWith() {
        return testStartsWith;
    }

    /**
     * Returns the file suffix (i.e. ".xml" for XML files and ".fo" for FOs).
     * @return the file suffix
     */
    public String getFileSuffix() {
        return testFileSuffix;
    }

    /**
     * Returns the directory set of tests to be run.
     * @return the directory tests
     */
    public String getTestSet() {
        return testSet;
    }

    /**
     * Returns the name of the XML file containing the disabled tests.
     * @return a file name, may be null
     */
    public String getDisabledTests() {
        return disabledTests;
    }

    /**
     * Whether any private tests should be invoked.
     * @return true if private tests should be tested
     */
    public boolean hasPrivateTests() {
        return privateTests;
    }

    /**
     * A builder class that configures the data for running a suite of tests designed for the
     * layout engine and FOTree.
     */
    public static class Builder {

        private String testDirectory;
        private String singleTest;
        private String testStartsWith;
        private String testFileSuffix;
        private String testSet;
        private String disabledTests;
        private boolean privateTests;

        /**
         * Configures the test directory.
         * @param dir the test directory
         * @return {@code this}
         */
        public Builder testDir(String dir) {
            testDirectory = dir;
            return this;
        }

        /**
         * Configures the name of the single test to run.
         * @param singleProperty name of the property that determines the single test case
         * @return {@code this}
         */
        public Builder singleProperty(String singleProperty) {
            singleTest = getSystemProperty(singleProperty);
            return this;
        }

        /**
         * Configures the prefix that all test cases must match.
         * @param startsWithProperty name of the property that determines the common prefix
         * @return {@code this}
         */
        public Builder startsWithProperty(String startsWithProperty) {
            testStartsWith = getSystemProperty(startsWithProperty);
            return this;
        }

        /**
         * Configures the test file name suffix.
         * @param suffix the suffixing string
         * @return {@code this}
         */
        public Builder suffix(String suffix) {
            testFileSuffix = suffix;
            return this;
        }

        /**
         * Configures the name of the directory containing the set of tests.
         * @param testSet the directory of tests. If null, defaults to "standard-testcases"
         * @return {@code this}
         */
        public Builder testSet(String testSet) {
            this.testSet = testSet != null ? testSet : "standard-testcases";
            return this;
        }

        /**
         * Configures whether any tests are disabled.
         * @param disabledProperty name of the property that determines the file of
         * disabled test cases
         * @param defaultValue if the property was not defined, uses this file name
         * instead
         * @return {@code this}
         */
        public Builder disabledProperty(String disabledProperty, String defaultValue) {
            String property = getSystemProperty(disabledProperty);
            disabledTests = property != null ? property : defaultValue;
            return this;
        }

        /**
         * Configures whether private tests must be run or not.
         * @param privateTestsProperty name of the property containing the boolean switch
         * @return {@code this}
         */
        public Builder privateTestsProperty(String privateTestsProperty) {
            String property = getSystemProperty(privateTestsProperty);
            this.privateTests = property != null && property.equalsIgnoreCase("true");
            return this;
        }

        private String getSystemProperty(String property) {
            return System.getProperty(property);
        }

        /**
         * Creates the configuration instance.
         * @return a configuration instance configured by this builder
         */
        public TestFilesConfiguration build() {
            return new TestFilesConfiguration(this);
        }
    }
}
