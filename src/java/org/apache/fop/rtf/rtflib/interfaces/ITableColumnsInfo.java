package org.apache.fop.rtf.rtflib.interfaces;

/*-----------------------------------------------------------------------------
 * jfor - Open-Source XSL-FO to RTF converter - see www.jfor.org
 *
 * ====================================================================
 * jfor Apache-Style Software License.
 * Copyright (c) 2002 by the jfor project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed
 * by the jfor project (http://www.jfor.org)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "jfor" must not be used to endorse
 * or promote products derived from this software without prior written
 * permission.  For written permission, please contact info@jfor.org.
 *
 * 5. Products derived from this software may not be called "jfor",
 * nor may "jfor" appear in their name, without prior written
 * permission of info@jfor.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE JFOR PROJECT OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * Contributor(s):
-----------------------------------------------------------------------------*/

/**  Used to get information about tables, for example when handling nested tables
 *     
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

//------------------------------------------------------------------------------
// $Id$
// $Log$
// Revision 1.1  2003/06/25 09:01:17  bdelacretaz
// additional jfor packages donated to the FOP project
//
// Revision 1.1  2002/08/12 09:40:02  bdelacretaz
// V0.7.1dev-e, contributions from Boris Poudérous for number-columns-spanned
// and vertical merging of tables cells.
//
//------------------------------------------------------------------------------

public interface ITableColumnsInfo
{
    float INVALID_COLUM_WIDTH = 200f;
    
    /** reset the column iteration index, meant to be called when creating a new row */
    void selectFirstColumn();

    /** increment the column iteration index */
    void selectNextColumn();

    /** get current column width according to column iteration index
     *  @return INVALID_COLUMN_WIDTH if we cannot find the value
     */
    float getColumnWidth();

     /** return current column iteration index */
     int getColumnIndex();

     /** return number of columns */
     int getNumberOfColumns();
}