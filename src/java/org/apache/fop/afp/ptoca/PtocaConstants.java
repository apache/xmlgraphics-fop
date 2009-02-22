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

package org.apache.fop.afp.ptoca;

/**
 * A collection of PTOCA constants.
 */
public interface PtocaConstants {

    /**
     * "Escape" sequence for normal PTOCA command sequences.
     */
    byte[] ESCAPE = new byte[] {0x2B, (byte)0xD3};

    /** Bit to set for chained control sequences */
    byte CHAIN_BIT = 1;

    /** Set Intercharacter Adjustment */
    byte SIA = (byte)0xC2;
    /** Set Variable Space Character Increment */
    byte SVI = (byte)0xC4;
    /** Absolute Move Inline */
    byte AMI = (byte)0xC6;
    /** Relative Move Inline */
    byte RMI = (byte)0xC8;

    /** Absolute Move Baseline */
    byte AMB = (byte)0xD2;

    /** Transparent Data */
    byte TRN = (byte)0xDA;

    /** Draw I-axis Rule */
    byte DIR = (byte)0xE4;
    /** Draw B-axis Rule */
    byte DBR = (byte)0xE6;

    /** Set Extended Text Color */
    byte SEC = (byte)0x80;

    /** Set Coded Font Local */
    byte SCFL = (byte)0xF0;
    /** Set Text Orientation */
    byte STO = (byte)0xF6;

    /** No Operation */
    byte NOP = (byte)0xF8;

    /** Maximum size of transparent data chunks */
    int TRANSPARENT_DATA_MAX_SIZE = 253;

}
