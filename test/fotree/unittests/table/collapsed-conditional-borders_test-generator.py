#!/usr/bin/python
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# $Id$

"""A testcase generator for conditional borders and the collapsing border model, in the FO
tree.
Computes all the possible combinations of borders (retained/discarded, with/without header
and footer, etc.) and generates the corresponding tables together with the expected
resolved borders.

The two functions to call are generateTestCases and generateTestCasesHeaderFooter; each of
them returns a complete FO file on stdout and a table of resolved borders on stderr, to be
included in the Java test case. This is all a bit rough be enough to get the testcases
generated.

Type definitions:
    border specification:
    {'length': <string, e.g. '4pt'>,
     'cond': <'retain' or 'discard'>
     'color': <string, e.g. 'black'>
    }
"""

import sys;
import copy;

fo_table  = 0
fo_column = 1
fo_body   = 2
fo_row    = 3
fo_cell   = 4

def printFOStart():
    print '<?xml version="1.0" standalone="no"?>'
    print '<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">'
    print '  <fo:layout-master-set>'
    print '    <fo:simple-page-master master-name="page" page-height="20cm" page-width="15cm" margin="1cm">'
    print '      <fo:region-body/>'
    print '    </fo:simple-page-master>'
    print '  </fo:layout-master-set>'
    print '  <fo:page-sequence master-reference="page" font-size="14pt">'
    print '    <fo:flow flow-name="xsl-region-body">'
    print

def printFOEnd():
    print '    </fo:flow>'
    print '  </fo:page-sequence>'
    print '</fo:root>'

def printBorder(side, border, indent):
    """Prints out the border specifications.
    Params:
        side: one of 'before', 'after', 'start', 'end'
        border: a border specification
        indent: for pretty-printing, string of whitespaces to put before the border
    """
    print indent + '  border-' + side + '-width.length="' + border['length'] + '"'
    print indent + '  border-' + side + '-width.conditionality="' + border['cond'] + '"'
    print indent + '  border-' + side + '-style="solid"'
    print indent + '  border-' + side + '-color="' + border['color'] + '"'

class TableGenerator:
    """Generates on stdout tables with no header and footer, with the border
    specifications passed to this object; and on stderr the corresponding resolved borders
    in a Java array, for inclusion into the test case. As soon as a pair of border sets is
    recorded, a table is generated.
    """

    fobjs = [
            ('<fo:table width="10cm" space-before="12pt" table-layout="fixed"', '>'),
            ('<fo:table-column column-width="proportional-column-width(1)"', '/>'),
            ('<fo:table-body', '>'),
            ('<fo:table-row', '>'),
            ('<fo:table-cell', '>')
            ]

    bordersBefore = None
    resBefore = {}
    """The comma between each table; nothing before the first one."""
    separator = ''

    def addBorderSet(self, borderSet, resolution):
        """Records a new border set, and prints out a table if its number is even.
        The first set will be used for borders-before, the second one for borders-after.

        Params:
        borderSet: a list of 5 border specifications for resp. table, table-column,
        table-body, table-row and table-cell
        resolution: the resolved border for the rest case (for the normal and leading
        cases the resolution is always the same)
        """
        if not self.bordersBefore:
            self.bordersBefore = borderSet
            self.resBefore = resolution
        else:
            # First the table
            for i in range(5):
                fobj = self.fobjs[i]
                indent = ' ' * (6 + 2*i)
                print indent + fobj[0]
                printBorder('before', self.bordersBefore[i], indent)
                printBorder('after', borderSet[i], indent)
                print indent + fobj[1]
            print '                <fo:block>Cell</fo:block>'
            print '              </fo:table-cell>'
            print '            </fo:table-row>'
            print '          </fo:table-body>'
            print '      </fo:table>'
            print
            # Then the resolution
            sys.stderr.write(self.separator + '{')
            comma = ''
            for beforeAfter in [self.resBefore, resolution]:
                sys.stderr.write(comma + '{border' + beforeAfter['length']
                        + ', Color.' + beforeAfter['color'] + '}')
                comma = ', '
            sys.stderr.write('}')
            self.separator = ',\n'
            # Reset
            self.bordersBefore = None
            self.resBefore = {}

class TableHFGenerator:
    """Generates on stdout tables with headers and footers, and the border specifications
    passed to this object; and on stderr the corresponding resolved borders in a Java
    array."""

    fobjs = [
            ['<fo:table width="10cm" space-before="12pt" table-layout="fixed"', '>'],
            ['<fo:table-column column-width="proportional-column-width(1)"', '/>'],
            ['<fo:table-header', '>'],
            ['<fo:table-row', '>'],
            ['<fo:table-cell', '>']
            ]

    borderHeader = [] # border-before for the header.
    borderFooter = [] # border-after for the footer.
    bordersBody = []  # borders for the cells in the body.

    def addBorderHeader(self, borders, resolution):
        self.borderHeader.append((borders, resolution))

    def addBorderFooter(self, borders, resolution):
        self.borderFooter.append((borders, resolution))

    def addBordersBody(self, borders, resolution):
        self.bordersBody.append((borders, resolution))

    def finish(self):
        """Prints out the tables and the resolved borders."""
        separator = ''  # The comma between each table, none before the first one
        for tableNum in range(len(self.borderHeader)):
            # First the table
            print '      <!-- Table ' + str(tableNum) + ' -->'
            for i in range(2):
                fobj = self.fobjs[i]
                indent = ' ' * (6 + 2*i)
                print indent + fobj[0]
                printBorder('before', self.borderHeader[tableNum][0][i], indent)
                printBorder('after', self.borderFooter[tableNum][0][i], indent)
                print indent + fobj[1]
            self.fobjs[fo_body][0] = '<fo:table-header'
            for i in range(2, 5):
                fobj = self.fobjs[i]
                indent = ' ' * (6 + 2*i)
                print indent + fobj[0]
                printBorder('before', self.borderHeader[tableNum][0][i], indent)
                printBorder('after', self.bordersBody[tableNum][0][i-2], indent)
                print indent + fobj[1]
            print '                <fo:block>Header</fo:block>'
            print '              </fo:table-cell>'
            print '            </fo:table-row>'
            print '          </fo:table-header>'
            self.fobjs[fo_body][0] = '<fo:table-footer'
            for i in range(2, 5):
                fobj = self.fobjs[i]
                indent = ' ' * (6 + 2*i)
                print indent + fobj[0]
                printBorder('before', self.bordersBody[tableNum][0][i+7], indent)
                printBorder('after', self.borderFooter[tableNum][0][i], indent)
                print indent + fobj[1]
            print '                <fo:block>Footer</fo:block>'
            print '              </fo:table-cell>'
            print '            </fo:table-row>'
            print '          </fo:table-footer>'
            self.fobjs[fo_body][0] = '<fo:table-body'
            for i in range(2, 5):
                fobj = self.fobjs[i]
                indent = ' ' * (6 + 2*i)
                print indent + fobj[0]
                printBorder('before', {'length': '4pt', 'cond': 'discard', 'color': 'black'}, indent)
                printBorder('after', self.bordersBody[tableNum][0][i+1], indent)
                print indent + fobj[1]
            print '                <fo:block>Cell1</fo:block>'
            print '              </fo:table-cell>'
            print '            </fo:table-row>'
            print '          </fo:table-body>'
            for i in range(2, 5):
                fobj = self.fobjs[i]
                indent = ' ' * (6 + 2*i)
                print indent + fobj[0]
                printBorder('before', self.bordersBody[tableNum][0][i+4], indent)
                printBorder('after', {'length': '4pt', 'cond': 'discard', 'color': 'blue'}, indent)
                print indent + fobj[1]
            print '                <fo:block>Cell1</fo:block>'
            print '              </fo:table-cell>'
            print '            </fo:table-row>'
            print '          </fo:table-body>'
            print '      </fo:table>'
            print
            # Then the resolutions
            sys.stderr.write(separator + '{')
            comma = ''
            for resHeadFoot in [self.borderHeader[tableNum][1], self.borderFooter[tableNum][1]]:
                for firstRest in ['first', 'rest']:
                    sys.stderr.write(comma + '{border'
                            + resHeadFoot[firstRest]['length']
                            + ', Color.' + resHeadFoot[firstRest]['color']
                            + '}')
                    comma = ', '
            resBody = self.bordersBody[tableNum][1]
            for i in range(4):
                for normLeadRest in ['normal', 'lead', 'rest']:
                    sys.stderr.write(', {border'
                            + resBody[i][normLeadRest][0]
                            + ', Color.' + resBody[i][normLeadRest][1]
                            + '}')
            sys.stderr.write('}')
            separator = ',\n'

def generateTestCases():
    """Generates testcases for table without header and footer."""
    def createAllCombinations():
        def createCombinations(n):
            if n == 0:
                allCombinations[0].append([])
            else:
                createCombinations(n-1)
                i = n
                while i > 0:
                    for combinations in allCombinations[i-1]:
                        allCombinations[i].append(copy.copy(combinations) + [n-1])
                    i = i - 1
        allCombinations = [[] for i in range(6)]
        createCombinations(5)
        return allCombinations

    printFOStart()
    tableGenerator = TableGenerator()

    defaultBorders = []
    for color in ['black', 'red', 'magenta', 'blue', 'yellow']:
        defaultBorders.append({'length': '4pt', 'cond': 'discard', 'color': color})
    defaultBorders[fo_table]['length'] = '8pt'

    resolution = {'length': '0pt', 'color': 'black'}
    tableGenerator.addBorderSet(defaultBorders, resolution)
    for combinations in createAllCombinations()[1:]:
        for combination in combinations:
            retainedBorders = copy.deepcopy(defaultBorders)
            for index in combination:
                retainedBorders[index]['cond'] = 'retain'
            for index in combination:
                finalBorders = copy.deepcopy(retainedBorders)
                if index != fo_table:
                    finalBorders[index]['length'] = '6pt'
                if fo_table in combination:
                    resolution = {'length': '8pt', 'color': 'black'}
                else:
                    resolution = {'length': '6pt', 'color': finalBorders[index]['color']}
                tableGenerator.addBorderSet(finalBorders, resolution)
    printFOEnd()

def generateTestCasesHeaderFooter():
    """Generates testcases for table with headers and footers."""
    def generateBordersHeaderFooter(tableGenerator):
        defaultBorders = [
                {'length': '4pt', 'cond': 'discard', 'color': 'black'}, # table
                {'length': '4pt', 'cond': 'discard', 'color': 'black'}, # table-column
                {'length': '4pt', 'cond': 'discard', 'color': 'blue'},  # table-body
                {'length': '4pt', 'cond': 'discard', 'color': 'blue'},  # table-row
                {'length': '4pt', 'cond': 'discard', 'color': 'blue'}   # table-cell
                ]
        defaultResolution = {
                'first': {'length': '4pt', 'color': 'blue'},
                'rest':  {'length': '4pt', 'color': 'blue'}
                }
        for (winner, other) in [(fo_table, fo_column), (fo_column, fo_table)]:
            borders = copy.deepcopy(defaultBorders)
            borders[winner]['length'] = '8pt'
            resolution = copy.deepcopy(defaultResolution)
            resolution['first'] = {'length': '8pt', 'color': 'black'}
            for border in [
                    ((other, '6pt', 'retain'),    'black'),
                    ((fo_body, '6pt', 'discard'), 'blue'),
                    ((fo_row, '6pt', 'discard'),  'blue'),
                    ((fo_cell, '6pt', 'discard'), 'blue')
                    ]:
                finalBorders = copy.deepcopy(borders)
                finalBorders[border[0][0]]['length'] = border[0][1]
                finalBorders[border[0][0]]['cond'] = border[0][2]
                finalResolution = copy.deepcopy(resolution)
                finalResolution['rest']['length'] = '6pt'
                finalResolution['rest']['color'] = border[1]
                tableGenerator.addBorderHeader(finalBorders, finalResolution)
                tableGenerator.addBorderFooter(finalBorders, finalResolution)

    def generateBordersBody(tableGenerator):
        # Named indices for readability
        header =  0
        rowh   =  1
        cellh  =  2
        body1  =  3
        row1   =  4
        cell1  =  5
        body2  =  6
        row2   =  7
        cell2  =  8
        footer =  9
        rowf   = 10
        cellf  = 11

        defaultBorders = [
                {'length': '4pt', 'cond': 'discard', 'color': 'red'},     # header
                {'length': '4pt', 'cond': 'discard', 'color': 'red'},     # header > row
                {'length': '4pt', 'cond': 'discard', 'color': 'red'},     # header > row > cell
                {'length': '4pt', 'cond': 'discard', 'color': 'black'},   # body1
                {'length': '4pt', 'cond': 'discard', 'color': 'black'},   # body1 > row
                {'length': '4pt', 'cond': 'discard', 'color': 'black'},   # body1 > row > cell
                {'length': '4pt', 'cond': 'discard', 'color': 'blue'},    # body2
                {'length': '4pt', 'cond': 'discard', 'color': 'blue'},    # body2 > row
                {'length': '4pt', 'cond': 'discard', 'color': 'blue'},    # body2 > row > cell
                {'length': '4pt', 'cond': 'discard', 'color': 'magenta'}, # footer
                {'length': '4pt', 'cond': 'discard', 'color': 'magenta'}, # footer > row
                {'length': '4pt', 'cond': 'discard', 'color': 'magenta'}  # footer > row > cell
                ]
        defaultResolution = [
                {'normal': ('4pt', 'black'), 'lead': ('4pt', 'black'), 'rest': ('4pt', 'red')},     # border-before cell 1
                {'normal': ('4pt', 'black'), 'lead': ('4pt', 'black'), 'rest': ('4pt', 'magenta')}, # border-after cell 1
                {'normal': ('4pt', 'blue'),  'lead': ('4pt', 'blue'),  'rest': ('4pt', 'red')},     # border-before cell 2
                {'normal': ('4pt', 'blue'),  'lead': ('4pt', 'blue'),  'rest': ('4pt', 'magenta')}  # border-after cell 2
                ]
        # The following contains changes to the default borders. Depending on the object
        # targeted (in header, footer, body1 or body2), the affected border is either before
        # or after (the other one keeping its default value):
        # - for header: border-after
        # - for body1: border-after
        # - for body2: border-before
        # - for footer: border-before
        for setting in [
                {'borders': [(body2, '8pt', 'discard'), (body1, '6pt', 'discard')], 'res': [
                    {'normal': ('4pt', 'black'), 'lead': ('4pt', 'black'), 'rest': ('4pt', 'red')},
                    {'normal': ('8pt', 'blue'),  'lead': ('6pt', 'black'), 'rest': ('4pt', 'magenta')},
                    {'normal': ('8pt', 'blue'),  'lead': ('8pt', 'blue'),  'rest': ('4pt', 'red')},
                    {'normal': ('4pt', 'blue'),  'lead': ('4pt', 'blue'),  'rest': ('4pt', 'magenta')}]},
                {'borders': [(row2,  '8pt', 'discard'), (row1,  '6pt', 'retain')], 'res': [
                    {'normal': ('4pt', 'black'), 'lead': ('4pt', 'black'), 'rest': ('4pt', 'red')},
                    {'normal': ('8pt', 'blue'),  'lead': ('6pt', 'black'), 'rest': ('6pt', 'black')},
                    {'normal': ('8pt', 'blue'),  'lead': ('8pt', 'blue'),  'rest': ('4pt', 'red')},
                    {'normal': ('4pt', 'blue'),  'lead': ('4pt', 'blue'),  'rest': ('4pt', 'magenta')}]},
                {'borders': [(cell2, '6pt', 'retain'), (cellh, '8pt', 'discard'), (cell1, '4pt', 'retain')], 'res': [
                    {'normal': ('8pt', 'red'),  'lead': ('8pt', 'red'),   'rest': ('8pt', 'red')},
                    {'normal': ('6pt', 'blue'), 'lead': ('4pt', 'black'), 'rest': ('4pt', 'black')},
                    {'normal': ('6pt', 'blue'), 'lead': ('8pt', 'red'),   'rest': ('8pt', 'red')},
                    {'normal': ('4pt', 'blue'), 'lead': ('4pt', 'blue'),  'rest': ('4pt', 'magenta')}]},
                {'borders': [(body2, '6pt',  'retain'), (rowh,  '8pt', 'discard'), (row1,  '4pt', 'retain')], 'res': [
                    {'normal': ('8pt', 'red'),  'lead': ('8pt', 'red'),   'rest': ('8pt', 'red')},
                    {'normal': ('6pt', 'blue'), 'lead': ('4pt', 'black'), 'rest': ('4pt', 'magenta')},
                    {'normal': ('6pt', 'blue'), 'lead': ('8pt', 'red'),   'rest': ('8pt', 'red')},
                    {'normal': ('4pt', 'blue'), 'lead': ('4pt', 'blue'),  'rest': ('4pt', 'magenta')}]},
                # Almost a copy-paste of the above, swapping 1 and 2, header and footer
                {'borders': [(body1, '8pt', 'discard'), (body2, '6pt', 'discard')], 'res': [
                    {'normal': ('4pt', 'black'), 'lead': ('4pt', 'black'), 'rest': ('4pt', 'red')},
                    {'normal': ('8pt', 'black'), 'lead': ('8pt', 'black'), 'rest': ('4pt', 'magenta')},
                    {'normal': ('8pt', 'black'), 'lead': ('6pt', 'blue'),  'rest': ('4pt', 'red')},
                    {'normal': ('4pt', 'blue'),  'lead': ('4pt', 'blue'),  'rest': ('4pt', 'magenta')}]},
                {'borders': [(cell1,  '8pt', 'discard'), (cell2,  '6pt', 'retain')], 'res': [
                    {'normal': ('4pt', 'black'), 'lead': ('4pt', 'black'), 'rest': ('4pt', 'red')},
                    {'normal': ('8pt', 'black'), 'lead': ('8pt', 'black'), 'rest': ('4pt', 'magenta')},
                    {'normal': ('8pt', 'black'), 'lead': ('6pt', 'blue'),  'rest': ('6pt', 'blue')},
                    {'normal': ('4pt', 'blue'),  'lead': ('4pt', 'blue'),  'rest': ('4pt', 'magenta')}]},
                {'borders': [(row1, '6pt', 'retain'), (footer, '8pt', 'discard'), (body2, '4pt', 'retain')], 'res': [
                    {'normal': ('4pt', 'black'),   'lead': ('4pt', 'black'),   'rest': ('4pt', 'red')},
                    {'normal': ('6pt', 'black'),   'lead': ('8pt', 'magenta'), 'rest': ('8pt', 'magenta')},
                    {'normal': ('6pt', 'black'),   'lead': ('4pt', 'blue'),    'rest': ('4pt', 'red')},
                    {'normal': ('8pt', 'magenta'), 'lead': ('8pt', 'magenta'), 'rest': ('8pt', 'magenta')}]},
                {'borders': [(body1, '8pt',  'retain'), (cellf,  '6pt', 'discard'), (row2,  '4pt', 'retain')], 'res': [
                    {'normal': ('4pt', 'black'),   'lead': ('4pt', 'black'),   'rest': ('4pt', 'red')},
                    {'normal': ('8pt', 'black'),   'lead': ('8pt', 'black'),   'rest': ('8pt', 'black')},
                    {'normal': ('8pt', 'black'),   'lead': ('4pt', 'blue'),    'rest': ('4pt', 'red')},
                    {'normal': ('6pt', 'magenta'), 'lead': ('6pt', 'magenta'), 'rest': ('6pt', 'magenta')}]}]:
            finalBorders = copy.deepcopy(defaultBorders)
            for border in setting['borders']:
                finalBorders[border[0]]['length'] = border[1]
                finalBorders[border[0]]['cond'] = border[2]
            tableGenerator.addBordersBody(finalBorders, setting['res'])

    tableGenerator = TableHFGenerator()
    printFOStart()
    generateBordersHeaderFooter(tableGenerator)
    generateBordersBody(tableGenerator)
    tableGenerator.finish()
    printFOEnd()

# Uncomment the appropriate line
#generateTestCases()
#generateTestCasesHeaderFooter()
