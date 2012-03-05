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

package org.apache.fop.complexscripts.bidi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.complexscripts.bidi.UnicodeBidiAlgorithm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * <p>Test case for Unicode Bidi Algorithm.</p>
 * @author Glenn Adams
 */
public class BidiAlgorithmTestCase {

    /**
     * logging instance
     */
    private static final Log log = LogFactory.getLog(BidiAlgorithmTestCase.class);                                      // CSOK: ConstantNameCheck

    /**
     * Concatenated array of <test-set,test-sequence> tuples
     * specifying which sequences are to be excluded from testing,
     * where -1 for either component is a wildcard.
     */
    private static final int[] EXCLUSIONS = {
        // no exclusions
    };

    /**
     * Concatenated array of <test-set,test-sequence> tuples
     * specifying which sequences are to be included in testing, where
     * -1 for either component is a wildcard.
     */
    private static final int[] INCLUSIONS = {
        -1, -1                                  // all sequences
    };

    /**
     * Concatenated array of <start,end> tuples expressing ranges of
     * test sets to be tested, where -1 in the end position signifies
     * all remaining test sets.
     */
    private static final int[] TEST_SET_RANGES = {
        0, -1                                   // all test sets
    };

    // instrumentation
    private int includedSequences;
    private int excludedSequences;
    private int passedSequences;

    @Test
    public void testBidiAlgorithm() throws Exception {
        String ldPfx = BidiTestData.LD_PFX;
        int ldCount = BidiTestData.LD_CNT;
        for ( int i = 0; i < ldCount; i++ ) {
            int[] da = BidiTestData.readTestData ( ldPfx, i );
            if ( da != null ) {
                testBidiAlgorithm ( i, da );
            } else {
                fail ( "unable to read bidi test data for resource at index " + i );
            }
        }
        // ensure we passed all test sequences
        assertEquals ( "did not pass all test sequences", BidiTestData.NUM_TEST_SEQUENCES, passedSequences );
        if ( log.isDebugEnabled() ) {
            log.debug ( "Included Sequences : " + includedSequences );
            log.debug ( "Excluded Sequences : " + excludedSequences );
            log.debug( "Passed Sequences   : " + passedSequences );
        }
    }

    private void testBidiAlgorithm ( int testSet, int[] da ) throws Exception {
        if ( da.length < 1 ) {
            fail ( "test data is empty" );
        } else if ( da.length < ( ( da[0] * 2 ) + 1 ) ) {
            fail ( "test data is truncated" );
        } else {
            int k = 0;
            // extract level count
            int n = da[k++];
            // extract level array
            int[] la = new int [ n ];
            for ( int i = 0; i < n; i++ ) {
                la[i] = da[k++];
            }
            // extract reorder array
            int[] ra = new int [ n ];
            for ( int i = 0; i < n; i++ ) {
                ra[i] = da[k++];
            }
            // extract and test each test sequence
            int testSequence = 0;
            int[] ta = new int [ n ];
            while ( ( k + ( 1 + n ) ) <= da.length ) {
                int bs = da[k++];
                for ( int i = 0; i < n; i++ ) {
                    ta[i] = da[k++];
                }
                if ( includeSequence ( testSet, testSequence ) ) {
                    includedSequences++;
                    if ( ! excludeSequence ( testSet, testSequence ) ) {
                        if ( testBidiAlgorithm ( testSet, testSequence, la, ra, ta, bs ) ) {
                            passedSequences++;
                        }
                    } else {
                        excludedSequences++;
                    }
                }
                testSequence++;
            }
            // ensure we exhausted test data
            assertEquals ( "extraneous test data", da.length, k );
        }
    }

    private boolean includeTestSet ( int testSet ) {
        for ( int i = 0, n = TEST_SET_RANGES.length / 2; i < n; i++ ) {
            int s = TEST_SET_RANGES [ ( i * 2 ) + 0 ];
            int e = TEST_SET_RANGES [ ( i * 2 ) + 1 ];
            if ( testSet >= s ) {
                if ( ( e < 0 ) || ( testSet <= e ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean includeSequence ( int testSet, int testSequence ) {
        if ( ! includeTestSet ( testSet ) ) {
            return false;
        } else {
            for ( int i = 0, n = INCLUSIONS.length / 2; i < n; i++ ) {
                int setno = INCLUSIONS [ ( i * 2 ) + 0 ];
                int seqno = INCLUSIONS [ ( i * 2 ) + 1 ];
                if ( setno < 0 ) {
                    if ( seqno < 0 ) {
                        return true;
                    } else if ( seqno == testSequence ) {
                        return true;
                    }
                } else if ( setno == testSet ) {
                    if ( seqno < 0 ) {
                        return true;
                    } else if ( seqno == testSequence ) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private boolean excludeSequence ( int testSet, int testSequence ) {
        for ( int i = 0, n = EXCLUSIONS.length / 2; i < n; i++ ) {
            int setno = EXCLUSIONS [ ( i * 2 ) + 0 ];
            int seqno = EXCLUSIONS [ ( i * 2 ) + 1 ];
            if ( setno < 0 ) {
                if ( seqno < 0 ) {
                    return true;
                } else if ( seqno == testSequence ) {
                    return true;
                }
            } else if ( setno == testSet ) {
                if ( seqno < 0 ) {
                    return true;
                } else if ( seqno == testSequence ) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean testBidiAlgorithm ( int testSet, int testSequence, int[] la, int[] ra, int[] ta, int bs ) throws Exception {
        boolean passed = true;
        int n = la.length;
        if ( ra.length != n ) {
            fail ( "bad reorder array length, expected " + n + ", got " + ra.length );
        } else if ( ta.length != n ) {
            fail ( "bad test array length, expected " + n + ", got " + ta.length );
        } else {
            // auto-LTR
            if ( ( bs & 1 ) != 0 ) {
                // auto-LTR is performed at higher level
            }
            // LTR
            if ( ( bs & 2 ) != 0 ) {
                int[] levels = UnicodeBidiAlgorithm.resolveLevels ( null, ta, 0, new int [ n ], true );
                if ( ! verifyResults ( la, levels, ta, 0, testSet, testSequence ) ) {
                    passed = false;
                }
            }
            // RTL
            if ( ( bs & 4 ) != 0 ) {
                int[] levels = UnicodeBidiAlgorithm.resolveLevels ( null, ta, 1, new int [ n ], true );
                if ( ! verifyResults ( la, levels, ta, 1, testSet, testSequence ) ) {
                    passed = false;
                }
            }
        }
        return passed;
    }

    private boolean verifyResults ( int[] laExp, int[] laOut, int[] ta, int dl, int testSet, int testSequence ) {
        if ( laOut.length != laExp.length ) {
            fail ( "output levels array length mismatch, expected " + laExp.length + ", got " + laOut.length );
            return false;
        } else {
            int numMatch = 0;
            for ( int i = 0, n = laExp.length; i < n; i++ ) {
                if ( laExp[i] >= 0 ) {
                    int lo = laOut[i];
                    int le = laExp[i];
                    if ( lo != le ) {
                        assertEquals ( getMismatchMessage ( testSet, testSequence, i, dl ), le, lo );
                    } else {
                        numMatch++;
                    }
                } else {
                    numMatch++;
                }
            }
            return numMatch == laExp.length;
        }
    }

    private String getMismatchMessage ( int testSet, int testSequence, int seqIndex, int defaultLevel ) {
        StringBuffer sb = new StringBuffer();
        sb.append ( "level mismatch for default level " );
        sb.append ( defaultLevel );
        sb.append ( " at sequence index " );
        sb.append ( seqIndex );
        sb.append ( " in test sequence " );
        sb.append ( testSequence );
        sb.append ( " of test set " );
        sb.append ( testSet );
        return sb.toString();
    }

}
