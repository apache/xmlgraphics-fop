/*
 * $Id$
 *
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */
package org.apache.fop.fo.properties;

import java.util.HashMap;

import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class WritingMode extends Property  {
    public static final int dataTypes = ENUM | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = NEW_TRAIT;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = ENUM_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int LR_TB = 1;
    public static final int RL_TB = 2;
    public static final int TB_RL = 3;
    public static final int LR = 1;
    public static final int RL = 2;
    public static final int TB = 3;
    public static final int MAX_WRITING_MODE = TB_RL;

    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.WRITING_MODE, LR_TB);
    }
    public static final int inherited = COMPUTED;

    public int getInherited() {
        return inherited;
    }

    private static final String[] rwEnums = {
        null
        ,"lr-tb"
        ,"rl-tb"
        ,"tb-rl"
    };

    private static final String[] rwEnumsSynon = {
            null
            ,"lr"
            ,"rl"
            ,"tb"
    };

    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap(9);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put(rwEnums[i],
                    Ints.consts.get(i));
            rwEnumHash.put(rwEnumsSynon[i],
                    Ints.consts.get(i));
        }
    }
    public int getEnumIndex(String enumval)
        throws PropertyException
    {
        Integer ii = (Integer)(rwEnumHash.get(enumval));
        if (ii == null)
            throw new PropertyException("Unknown ENUM value: " + enumval);
        return ii.intValue();
    }
    public String getEnumText(int index)
        throws PropertyException
    {
        if (index < 1 || index >= rwEnums.length)
            throw new PropertyException("index out of range: " + index);
        return rwEnums[index];
    }

    public static final int BEFORE = 1;
    public static final int AFTER = 2;
    public static final int START = 3;
    public static final int END = 4;

    public static final int MAX_EDGE = END;

    private static final String[] relativeEdges = {
            null
            ,"before"
            ,"after"
            ,"start"
            ,"end"
    };

    public static final int TOP = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;

    private static final String[] absoluteEdges = {
            null
            ,"top"
            ,"bottom"
            ,"left"
            ,"right"
    };

    private static final int[] lr_tbAbsRelMap = {
            0, BEFORE, AFTER, START, END
    };

    private static final int[] lr_tbRelAbsMap = {
            0, TOP, BOTTOM, LEFT, RIGHT
    };

    private static final int[] rl_tbAbsRelMap = {
            0, BEFORE, AFTER, END, START
    };

    private static final int[] rl_tbRelAbsMap = {
            0, TOP, BOTTOM, RIGHT, LEFT
    };

    private static final int[] tb_rlAbsRelMap = {
            0, START, END, AFTER, BEFORE
    };

    private static final int[] tb_rlRelAbsMap = {
            0, RIGHT, LEFT, TOP, BOTTOM
    };

    // top-to-bottom, left-to-right, e.g., Mongolian
    // Currently unused
    private static final int[] tb_lrAbsRelMap = {
            0, START, END, BEFORE, AFTER
    };

    private static int[] tb_lrRelAbsMap = {
            0, LEFT, RIGHT, TOP, BOTTOM
    };

    private static final ROIntArray[] relAbsROMaps = {
            null
            ,new ROIntArray(lr_tbRelAbsMap)
            ,new ROIntArray(rl_tbRelAbsMap)
            ,new ROIntArray(tb_rlRelAbsMap)
    };

    private static final ROIntArray[] absRelROMaps = {
            null
            ,new ROIntArray(lr_tbAbsRelMap)
            ,new ROIntArray(rl_tbAbsRelMap)
            ,new ROIntArray(tb_rlAbsRelMap)
    };

    /**
     * Gets the relative-to-absolute map for the given writing mode.
     * <code>writingMode</code> is the enumerated constant <code>int</code>
     * value of the writing mode, from the set <code>LR_TB</code>,
     * <code>RL_TB</code> and <code>TB_RL</code>.
     * The result is an <code>ROIntArray</code> containing the set of absolute
     * edge integer eumeration constants <code>TOP</code>, <code>BOTTOM</code>,
     * <code>LEFT</code> and <code>RIGHT</code>, indexed by the relative edge
     * integer eumeration constants <code>BEFORE</code>, <code>AFTER</code>,
     * <code>START</code> and <code>END</code>, as appropriate for the given
     * writing mode.
     * @param writingMode the enumerated value of the writing mode
     * @return an array mapping from relative edges to absolute edges for the
     * given writing mode.
     * 
     * @throws PropertyException if the writing mode is out of range
     */
    public static ROIntArray getRelAbsMap(int writingMode)
    throws PropertyException {
        if (writingMode > 0 && writingMode <= MAX_WRITING_MODE) {
            return relAbsROMaps[writingMode];
        }
        throw new PropertyException(
                "Writing mode out of range:" + writingMode);
    }

    /**
     * Gets the absolute-to-relative map for the given writing mode.
     * <code>writingMode</code> is the enumerated constant <code>int</code>
     * value of the writing mode, from the set <code>LR_TB</code>,
     * <code>RL_TB</code> and <code>TB_RL</code>.
     * The result is an <code>ROIntArray</code> containing the relative edge
     * integer eumeration constants <code>BEFORE</code>, <code>AFTER</code>,
     * <code>START</code> and <code>END</code>, indexed by the set of absolute
     * edge integer eumeration constants <code>TOP</code>, <code>BOTTOM</code>,
     * <code>LEFT</code> and <code>RIGHT</code>, as appropriate for the given
     * writing mode.
     * @param writingMode the enumerated value of the writing mode
     * @return an array mapping from absolute edges to relative edges for the
     * given writing mode.
     * 
     * @throws PropertyException if the writing mode is out of range
     */
    public static ROIntArray getAbsRelMap(int writingMode)
    throws PropertyException {
        if (writingMode > 0 && writingMode <= MAX_WRITING_MODE) {
            return absRelROMaps[writingMode];
        }
        throw new PropertyException(
                "Writing mode out of range:" + writingMode);
    }

    private static final int[][] absRelMaps = {
            null
            ,lr_tbAbsRelMap
            ,rl_tbAbsRelMap
            ,tb_rlAbsRelMap
    };

    /**
     * Gets the relative edge corresponding to the given absolute edge for the
     * given writing mode.
     * <code>writingMode</code> is the enumerated constant <code>int</code>
     * value of the writing mode, from the set <code>LR_TB</code>,
     * <code>RL_TB</code> and <code>TB_RL</code>.
     * <code>absoluteEdge</code> is the enumerated constant <code>int</code>
     * value of the absolute edge, from the set <code>TOP</code>,
     * <code>BOTTOM</code>, <code>LEFT</code> and <code>RIGHT</code>.
     * The result is from the enumerated constant <code>int</code> set
     * <code>BEFORE</code>, <code>AFTER</code>, <code>START</code> and
     * <code>END</code>, as appropriate for the given writing mode.
     * @param writingMode the enumeration value of the writing mode
     * @param absoluteEdge the enumeration value of the absolute edge
     * @return the enumeration value of the corresponding relative edge
     * @throws PropertyException if the writing mode or absolute edge is
     * out of range 
     */
    public static int getCorrespondingRelativeEdge(
            int writingMode, int absoluteEdge)
    throws PropertyException {
        if (writingMode <= 0 || writingMode > MAX_WRITING_MODE) {
            throw new PropertyException(
                    "Writing mode out of range:" + writingMode);
        }
        if (absoluteEdge <= 0 || absoluteEdge > MAX_EDGE) {
            throw new PropertyException(
                    "Absolute edge out of range:" + absoluteEdge);
        }
        return absRelMaps[writingMode][absoluteEdge];
    }

    private static final int[][] relAbsMaps = {
            null
            ,lr_tbRelAbsMap
            ,rl_tbRelAbsMap
            ,tb_rlRelAbsMap
    };


    /**
     * Gets the absolute edge corresponding to the given relative edge for the
     * given writing mode.
     * <code>writingMode</code> is the enumerated constant <code>int</code>
     * value of the writing mode, from the set <code>LR_TB</code>,
     * <code>RL_TB</code> and <code>TB_RL</code>.
     * <code>relativeEdge</code> is the enumerated constant <code>int</code>
     * value of the relative edge, from the set <code>BEFORE</code>,
     * <code>AFTER</code>, <code>START</code> and <code>END</code>.
     * The result is from the enumerated constant <code>int</code> set
     * <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code> and
     * <code>RIGHT</code>, as appropriate for the given writing mode.
     * @param writingMode the enumeration value of the writing mode
     * @param relativeEdge the enumeration value of the relative edge
     * @return the enumeration value of the corresponding absolute edge
     * @throws PropertyException if the writing mode or relative edge is
     * out of range 
     */
    public static int getCorrespondingAbsoluteEdge(
            int writingMode, int relativeEdge)
    throws PropertyException {
        if (writingMode <= 0 || writingMode > MAX_WRITING_MODE) {
            throw new PropertyException(
                    "Writing mode out of range:" + writingMode);
        }
        if (relativeEdge <= 0 || relativeEdge > MAX_EDGE) {
            throw new PropertyException(
                    "Relative edge out of range:" + relativeEdge);
        }
        return relAbsMaps[writingMode][relativeEdge];
    }

    /** Map of <code>isHorizontal</code> values for writing modes */
    private static final boolean[] horizontal = {
            false
            ,true   // lr_tb
            ,true   // rl_tb
            ,false  // tb_rl
    };

    /**
     * Mimics <code>isHorizontal</code> method from
     * <code>java.awt.ComponentOrientation</code>.
     * @param writingMode
     * @return
     * @throws PropertyException if the writing mode is invalid
     */
    public static boolean isHorizontal(int writingMode)
    throws PropertyException {
        if (writingMode <= 0 || writingMode > MAX_WRITING_MODE) {
            throw new PropertyException(
                    "Writing mode out of range:" + writingMode);
        }
        return horizontal[writingMode];
    }

    /** Map of <code>isLeftToRight</code> values for writing modes */
    private static final boolean[] leftToRight = {
            false
            ,true   // lr_tb
            ,false  // rl_tb
            ,false  // tb_rl
    };

    /**
     * Mimics <code>isLeftToRight</code> method from
     * <code>java.awt.ComponentOrientation</code>.
     * @param writingMode
     * @return
     * @throws PropertyException is the writing mode is invalid
     */
    public static boolean isLeftToRight(int writingMode)
    throws PropertyException {
        if (writingMode <= 0 || writingMode > MAX_WRITING_MODE) {
            throw new PropertyException(
                    "Writing mode out of range:" + writingMode);
        }
        return leftToRight[writingMode];
    }

}

