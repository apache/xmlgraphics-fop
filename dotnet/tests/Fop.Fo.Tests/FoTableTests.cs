// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using Fop.Fo;

using Xunit;

namespace Fop.Fo.Tests;

/// <summary>Parsing + property tests for the table formatting objects.</summary>
public sealed class FoTableTests
{
    private static FoTable FirstTable(string tableMarkup)
    {
        string fo = $"""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" color="#000000">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="400pt" page-height="400pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  {tableMarkup}
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        return root.PageSequences.First().Flow!.ChildObjects.OfType<FoTable>().Single();
    }

    [Fact]
    public void ParsesTableStructure()
    {
        FoTable table = FirstTable("""
            <fo:table width="300pt" table-layout="fixed">
              <fo:table-column column-width="100pt"/>
              <fo:table-column column-width="200pt"/>
              <fo:table-header>
                <fo:table-row><fo:table-cell><fo:block>H1</fo:block></fo:table-cell></fo:table-row>
              </fo:table-header>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell><fo:block>a</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>b</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
              <fo:table-footer>
                <fo:table-row><fo:table-cell><fo:block>F1</fo:block></fo:table-cell></fo:table-row>
              </fo:table-footer>
            </fo:table>
            """);

        Assert.Equal(300_000, table.Width!.Value.Millipoints, 3);
        Assert.Equal(TableLayout.Fixed, table.TableLayout);
        Assert.Equal(2, table.Columns.Count());
        Assert.NotNull(table.Header);
        Assert.NotNull(table.Footer);
        FoTableBody body = Assert.Single(table.Bodies);

        FoTableRow row = Assert.Single(body.Rows);
        Assert.Equal(2, row.Cells.Count());

        FoTableCell first = row.Cells.First();
        Assert.Equal(1, first.NumberColumnsSpanned);
        Assert.Equal(1, first.NumberRowsSpanned);
        FoBlock block = Assert.Single(first.Blocks);
        Assert.Equal("a", block.Children.OfType<FOText>().Single().Text);
    }

    [Fact]
    public void TableWidthAutoIsNull()
    {
        FoTable table = FirstTable("<fo:table><fo:table-body><fo:table-row><fo:table-cell>"
            + "<fo:block>x</fo:block></fo:table-cell></fo:table-row></fo:table-body></fo:table>");
        Assert.Null(table.Width);
        Assert.Equal(TableLayout.Auto, table.TableLayout);
    }

    [Fact]
    public void CellSpansParse()
    {
        FoTable table = FirstTable("""
            <fo:table>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell number-columns-spanned="3" number-rows-spanned="2">
                    <fo:block>x</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """);
        FoTableCell cell = table.Bodies.Single().Rows.Single().Cells.Single();
        Assert.Equal(3, cell.NumberColumnsSpanned);
        Assert.Equal(2, cell.NumberRowsSpanned);
    }

    [Fact]
    public void RowHeightAndColumnNumberParse()
    {
        FoTable table = FirstTable("""
            <fo:table>
              <fo:table-column column-number="2" column-width="50pt"/>
              <fo:table-body>
                <fo:table-row height="40pt">
                  <fo:table-cell column-number="2"><fo:block>x</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """);

        FoTableColumn column = table.Columns.Single();
        Assert.Equal(2, column.ColumnNumber);

        FoTableRow row = table.Bodies.Single().Rows.Single();
        Assert.Equal(40_000, row.Height!.Value.Millipoints, 3);
        Assert.Equal(2, row.Cells.Single().ColumnNumber);
    }

    [Fact]
    public void CellBoxResolves()
    {
        FoTable table = FirstTable("""
            <fo:table>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell border="1pt solid #000000" padding="2pt" background-color="#ff0000">
                    <fo:block>x</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """);
        BoxProperties box = table.Bodies.Single().Rows.Single().Cells.Single().Box;
        Assert.True(box.HasBorder);
        Assert.True(box.HasBackground);
        Assert.Equal(2_000, box.PaddingTop.Millipoints, 3);
    }

    // ----- column-width specification parsing --------------------------------------------

    [Fact]
    public void ColumnWidth_Absolute()
    {
        ColumnWidthSpec spec = FoTableColumn.ParseColumnWidth("120pt", 12_000);
        Assert.Equal(ColumnWidthKind.Absolute, spec.Kind);
        Assert.Equal(120_000, spec.Value, 3);
        Assert.Equal(120_000, spec.ResolveMpt(500_000)!.Value, 3);
    }

    [Fact]
    public void ColumnWidth_Percent()
    {
        ColumnWidthSpec spec = FoTableColumn.ParseColumnWidth("25%", 12_000);
        Assert.Equal(ColumnWidthKind.Percent, spec.Kind);
        Assert.Equal(25, spec.Value, 3);
        Assert.Equal(50_000, spec.ResolveMpt(200_000)!.Value, 3);
    }

    [Fact]
    public void ColumnWidth_Proportional()
    {
        ColumnWidthSpec spec = FoTableColumn.ParseColumnWidth("proportional-column-width(3)", 12_000);
        Assert.Equal(ColumnWidthKind.Proportional, spec.Kind);
        Assert.Equal(3, spec.Value, 3);
        Assert.Null(spec.ResolveMpt(200_000));
    }

    [Fact]
    public void ColumnWidth_AutoOrUnsupported()
    {
        Assert.Equal(ColumnWidthKind.Auto, FoTableColumn.ParseColumnWidth(null, 12_000).Kind);
        Assert.Equal(ColumnWidthKind.Auto, FoTableColumn.ParseColumnWidth("auto", 12_000).Kind);
        Assert.Equal(ColumnWidthKind.Auto, FoTableColumn.ParseColumnWidth("garbage", 12_000).Kind);
    }

    [Fact]
    public void NumberColumnsRepeated_Default()
    {
        FoTable table = FirstTable("""
            <fo:table>
              <fo:table-column column-width="10pt" number-columns-repeated="4"/>
              <fo:table-body><fo:table-row><fo:table-cell><fo:block>x</fo:block></fo:table-cell></fo:table-row></fo:table-body>
            </fo:table>
            """);
        Assert.Equal(4, table.Columns.Single().NumberColumnsRepeated);
    }
}
