/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.layoutmgr;


/**
 * This class is used to pass information to the getNextBreakPoss()
 * method concerning hyphenation. A reference to an instance of the
 * class is contained in the LayoutContext object passed to each
 * LayoutManager. It contains information concerning the hyphenation
 * points in a word and the how many of those have previously been
 * processed by a Layout Manager to generate size information.
 */
public class HyphContext {
    private int[] m_hyphPoints;
    private int m_iCurOffset = 0;
    private int m_iCurIndex = 0;

    public HyphContext(int[] hyphPoints) {
        m_hyphPoints = hyphPoints;
    }

    public int getNextHyphPoint() {
        for (; m_iCurIndex < m_hyphPoints.length; m_iCurIndex++) {
            if (m_hyphPoints[m_iCurIndex] > m_iCurOffset) {
                return (m_hyphPoints[m_iCurIndex] - m_iCurOffset);
            }
        }
        return -1; // AT END!
    }

    public boolean hasMoreHyphPoints() {
        for (; m_iCurIndex < m_hyphPoints.length; m_iCurIndex++) {
            if (m_hyphPoints[m_iCurIndex] > m_iCurOffset) {
                return true;
            }
        }
        return false;
    }

    public void updateOffset(int iCharsProcessed) {
        m_iCurOffset += iCharsProcessed;
    }
}
