/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Rev$ $Name$
 */

package org.apache.fop.fo;

/**
 * Data class containing the Flow Object names and associated integer
 * constants.
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

                                LAST_FO = WRAPPER;

    /**
     * Array containing the local names of all of the elements in the
     * <i>FO</i> namespace and the package name suffix of the Object
     * representing the FO.  The current package name prefix is
     * "org.apache.fop", but this may be varied without affecting the array.
     * The array is effectively 1-based as the zero
     * index does not correspond to any FO element.  The list of
     * <tt>int</tt> constants must be kept in sync with this array, as the
     * constants are used to index into the array.
     */
    public static final String[][] foLocalNames = {
                                 { "no-fo", ""                }  //0
                           ,{ "basic-link", "fo.sequences"    }  //1
                        ,{ "bidi-override", "fo.sequences"    }  //2
                                ,{ "block", "fo.sequences"    }  //3
                      ,{ "block-container", "fo.sequences"    }  //4
                            ,{ "character", "fo.sequences"    }  //5
                        ,{ "color-profile", "fo.declarations" }  //6
    ,{ "conditional-page-master-reference", "fo.pagination"   }  //7
                         ,{ "declarations", "fo.declarations" }  //8
                     ,{ "external-graphic", "fo.sequences"    }  //9
                                ,{ "float", "fo.sequences"    }  //10
                                 ,{ "flow", "fo.sequences"    }  //11
                             ,{ "footnote", "fo.sequences"    }  //12
                        ,{ "footnote-body", "fo.sequences"    }  //13
                 ,{ "initial-property-set", "fo.sequences"    }  //14
                               ,{ "inline", "fo.sequences"    }  //15
                     ,{ "inline-container", "fo.sequences"    }  //16
              ,{ "instream-foreign-object", "fo.sequences"    }  //17
                    ,{ "layout-master-set", "fo.pagination"   }  //18
                               ,{ "leader", "fo.sequences"    }  //19
                           ,{ "list-block", "fo.sequences"    }  //20
                            ,{ "list-item", "fo.sequences"    }  //21
                       ,{ "list-item-body", "fo.sequences"    }  //22
                      ,{ "list-item-label", "fo.sequences"    }  //23
                               ,{ "marker", "fo.sequences"    }  //24
                           ,{ "multi-case", "fo.sequences"    }  //25
                     ,{ "multi-properties", "fo.sequences"    }  //26
                   ,{ "multi-property-set", "fo.sequences"    }  //27
                         ,{ "multi-switch", "fo.sequences"    }  //28
                         ,{ "multi-toggle", "fo.sequences"    }  //29
                          ,{ "page-number", "fo.sequences"    }  //30
                 ,{ "page-number-citation", "fo.sequences"    }  //31
                        ,{ "page-sequence", "fo.sequences"    }  //32
                 ,{ "page-sequence-master", "fo.pagination"   }  //33
                         ,{ "region-after", "fo.pagination"   }  //34
                        ,{ "region-before", "fo.pagination"   }  //35
                          ,{ "region-body", "fo.pagination"   }  //36
                           ,{ "region-end", "fo.pagination"   }  //37
                         ,{ "region-start", "fo.pagination"   }  //38
  ,{ "repeatable-page-master-alternatives", "fo.pagination"   }  //39
     ,{ "repeatable-page-master-reference", "fo.pagination"   }  //40
                      ,{ "retrieve-marker", "fo.sequences"    }  //41
                                 ,{ "root", "fo"              }  //42
                   ,{ "simple-page-master", "fo.pagination"   }  //43
         ,{ "single-page-master-reference", "fo.pagination"   }  //44
                       ,{ "static-content", "fo.sequences"    }  //45
                                ,{ "table", "fo.sequences"    }  //46
                    ,{ "table-and-caption", "fo.sequences"    }  //47
                           ,{ "table-body", "fo.sequences"    }  //48
                        ,{ "table-caption", "fo.sequences"    }  //49
                           ,{ "table-cell", "fo.sequences"    }  //50
                         ,{ "table-column", "fo.sequences"    }  //51
                         ,{ "table-footer", "fo.sequences"    }  //52
                         ,{ "table-header", "fo.sequences"    }  //53
                            ,{ "table-row", "fo.sequences"    }  //54
                                ,{ "title", "fo.sequences"    }  //55
                              ,{ "wrapper", "fo.sequences"    }  //56
    };
}
