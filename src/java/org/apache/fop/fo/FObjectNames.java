/*
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
 * $Id$
 */

package org.apache.fop.fo;

import java.util.HashMap;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Ints;

/**
 * Data class containing the Flow Object names and associated integer
 * constants.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Rev$ $Name$
 */

public class FObjectNames {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Constant for matching Flow Object defined in <i>XSLFO</i>.
     */
    public static final int
                                  NO_FO = 0,
                             BASIC_LINK = 1,
                          BIDI_OVERRIDE = 2,
                                  BLOCK = 3,
                        BLOCK_CONTAINER = 4,
                              CHARACTER = 5,
                          COLOR_PROFILE = 6,
      CONDITIONAL_PAGE_MASTER_REFERENCE = 7,
                           DECLARATIONS = 8,
                       EXTERNAL_GRAPHIC = 9,
                                  FLOAT = 10,
                                   FLOW = 11,
                               FOOTNOTE = 12,
                          FOOTNOTE_BODY = 13,
                   INITIAL_PROPERTY_SET = 14,
                                 INLINE = 15,
                       INLINE_CONTAINER = 16,
                INSTREAM_FOREIGN_OBJECT = 17,
                      LAYOUT_MASTER_SET = 18,
                                 LEADER = 19,
                             LIST_BLOCK = 20,
                              LIST_ITEM = 21,
                         LIST_ITEM_BODY = 22,
                        LIST_ITEM_LABEL = 23,
                                 MARKER = 24,
                             MULTI_CASE = 25,
                       MULTI_PROPERTIES = 26,
                     MULTI_PROPERTY_SET = 27,
                           MULTI_SWITCH = 28,
                           MULTI_TOGGLE = 29,
                            PAGE_NUMBER = 30,
                   PAGE_NUMBER_CITATION = 31,
                          PAGE_SEQUENCE = 32,
                   PAGE_SEQUENCE_MASTER = 33,
                           REGION_AFTER = 34,
                          REGION_BEFORE = 35,
                            REGION_BODY = 36,
                             REGION_END = 37,
                           REGION_START = 38,
    REPEATABLE_PAGE_MASTER_ALTERNATIVES = 39,
       REPEATABLE_PAGE_MASTER_REFERENCE = 40,
                        RETRIEVE_MARKER = 41,
                                   ROOT = 42,
                     SIMPLE_PAGE_MASTER = 43,
           SINGLE_PAGE_MASTER_REFERENCE = 44,
                         STATIC_CONTENT = 45,
                                  TABLE = 46,
                      TABLE_AND_CAPTION = 47,
                             TABLE_BODY = 48,
                          TABLE_CAPTION = 49,
                             TABLE_CELL = 50,
                           TABLE_COLUMN = 51,
                           TABLE_FOOTER = 52,
                           TABLE_HEADER = 53,
                              TABLE_ROW = 54,
                                  TITLE = 55,
                                WRAPPER = 56,
                                 PCDATA = 57,

                                LAST_FO = PCDATA;

    /** Index of FO names in foLocalNames array. */
    private static final int NAMEX = 0;
    /** Index of FO package string in foLocalNames array. */
    private static final int PACKAGEX = 1;
    /**
     * Array containing the local names of all of the elements in the
     * <i>FO</i> namespace and the package name suffix of the Object
     * representing the FO.  The current package name prefix is
     * Fop.fopPackage, but this may be varied without affecting the array.
     * The array is effectively 1-based as the zero
     * index does not correspond to any FO element.  The list of
     * <tt>int</tt> constants must be kept in sync with this array, as the
     * constants are used to index into the array.
     */
    private static final String[][] foLocalNames = {
                                 { "no-fo", ""                }  //0
                           ,{ "basic-link", "fo.flow"         }  //1
                        ,{ "bidi-override", "fo.flow"         }  //2
                                ,{ "block", "fo.flow"         }  //3
                      ,{ "block-container", "fo.flow"         }  //4
                            ,{ "character", "fo.flow"         }  //5
                        ,{ "color-profile", "fo.declarations" }  //6
    ,{ "conditional-page-master-reference", "fo.pagination"   }  //7
                         ,{ "declarations", "fo.declarations" }  //8
                     ,{ "external-graphic", "fo.flow"         }  //9
                                ,{ "float", "fo.flow"         }  //10
                                 ,{ "flow", "fo.flow"         }  //11
                             ,{ "footnote", "fo.flow"         }  //12
                        ,{ "footnote-body", "fo.flow"         }  //13
                 ,{ "initial-property-set", "fo.flow"         }  //14
                               ,{ "inline", "fo.flow"         }  //15
                     ,{ "inline-container", "fo.flow"         }  //16
              ,{ "instream-foreign-object", "fo.flow"         }  //17
                    ,{ "layout-master-set", "fo.pagination"   }  //18
                               ,{ "leader", "fo.flow"         }  //19
                           ,{ "list-block", "fo.flow"         }  //20
                            ,{ "list-item", "fo.flow"         }  //21
                       ,{ "list-item-body", "fo.flow"         }  //22
                      ,{ "list-item-label", "fo.flow"         }  //23
                               ,{ "marker", "fo.flow"         }  //24
                           ,{ "multi-case", "fo.flow"         }  //25
                     ,{ "multi-properties", "fo.flow"         }  //26
                   ,{ "multi-property-set", "fo.flow"         }  //27
                         ,{ "multi-switch", "fo.flow"         }  //28
                         ,{ "multi-toggle", "fo.flow"         }  //29
                          ,{ "page-number", "fo.flow"         }  //30
                 ,{ "page-number-citation", "fo.flow"         }  //31
                        ,{ "page-sequence", "fo.flow"         }  //32
                 ,{ "page-sequence-master", "fo.pagination"   }  //33
                         ,{ "region-after", "fo.pagination"   }  //34
                        ,{ "region-before", "fo.pagination"   }  //35
                          ,{ "region-body", "fo.pagination"   }  //36
                           ,{ "region-end", "fo.pagination"   }  //37
                         ,{ "region-start", "fo.pagination"   }  //38
  ,{ "repeatable-page-master-alternatives", "fo.pagination"   }  //39
     ,{ "repeatable-page-master-reference", "fo.pagination"   }  //40
                      ,{ "retrieve-marker", "fo.flow"         }  //41
                                 ,{ "root", "fo"              }  //42
                   ,{ "simple-page-master", "fo.pagination"   }  //43
         ,{ "single-page-master-reference", "fo.pagination"   }  //44
                       ,{ "static-content", "fo.flow"         }  //45
                                ,{ "table", "fo.flow"         }  //46
                    ,{ "table-and-caption", "fo.flow"         }  //47
                           ,{ "table-body", "fo.flow"         }  //48
                        ,{ "table-caption", "fo.flow"         }  //49
                           ,{ "table-cell", "fo.flow"         }  //50
                         ,{ "table-column", "fo.flow"         }  //51
                         ,{ "table-footer", "fo.flow"         }  //52
                         ,{ "table-header", "fo.flow"         }  //53
                            ,{ "table-row", "fo.flow"         }  //54
                                ,{ "title", "fo.flow"         }  //55
                              ,{ "wrapper", "fo.flow"         }  //56
                               ,{ "pcdata", "fo.flow"         }  //57
    };
    
    /**
     * Publicly visible length of the private foLocaNames array.
     */
    public static final int foLocalNamesLength = foLocalNames.length;

    /**
     * A HashMap whose elements are an integer index value keyed by an
     * fo local name.  The index value is the index of the fo local name in
     * the FObjectNames.foLocalNames[] array.
     * It is initialized in a static initializer.
     */
    private static final HashMap foToIndex = new HashMap(LAST_FO + 1);
    static {
        for (int i = 0; i <= LAST_FO; i++)
        // Set up the foToIndex Hashmap with the name of the
        // flow object as a key, and the integer index as a value
        if (foToIndex.put(foLocalNames[i][NAMEX], Ints.consts.get(i)) != null)
            throw new RuntimeException(
                "Duplicate values in propertyToIndex for key " +
                    foLocalNames[i][NAMEX]);
    }
    
    /**
     * Get the FObject index corresponding to the FObject name.
     * @param foName - the FO name.
     * @return the <tt>int</tt> index.
     * @throws FOPException
     */
    public static int getFOIndex(String foName)
                throws FOPException
    {
        Integer index = (Integer)(foToIndex.get(foName));
        if (index == null) throw new FOPException
                                        ("Unknown FObject name: " + foName);
        return index.intValue();
    }

    /**
     * Get the FObject name corresponding to the FO index.
     * @param foType <tt>int</tt> index of the FO type.
     * @return <tt>String</tt> name of the FO.
     * @exception FOPException if the FO index is invalid.
     */
    public static String getFOName(int foType)
    throws FOPException
    {
        if (foType < 0 || foType > LAST_FO)
            throw new FOPException
            ("getFOName: type is invalid: " + foType);
        return foLocalNames[foType][NAMEX];
    }
    
    /**
     * Get the FObject package name corresponding to the FO index.
     * @param foType <tt>int</tt> index of the FO type.
     * @return <tt>String</tt> package name of the FO.
     * @exception FOPException if the FO index is invalid.
     */
    public static String getFOPkg(int foType)
    throws FOPException
    {
        if (foType < 0 || foType > LAST_FO)
            throw new FOPException
            ("getFOPkg: type is invalid: " + foType);
        return foLocalNames[foType][PACKAGEX];
    }

}
