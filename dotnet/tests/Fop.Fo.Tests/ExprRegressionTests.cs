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

/// <summary>
/// Regression tests guarding the carve-outs: the context/layout-dependent functions
/// <c>proportional-column-width(N)</c>, <c>label-end()</c> and <c>body-start()</c> must still behave
/// exactly as before the expression evaluator was introduced.
/// </summary>
public sealed class ExprRegressionTests
{
    private static FoRoot Parse(string flowMarkup)
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
                  {flowMarkup}
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        return FoTreeBuilder.ParseString(fo);
    }

    [Fact]
    public void ProportionalColumnWidthStillParsesAsProportional()
    {
        FoRoot root = Parse("""
            <fo:table table-layout="fixed" width="300pt">
              <fo:table-column column-width="proportional-column-width(2)"/>
              <fo:table-column column-width="proportional-column-width(1)"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell><fo:block>a</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>b</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """);

        var columns = root.PageSequences.First().Flow!.ChildObjects
            .OfType<FoTable>().Single().Columns.ToList();

        ColumnWidthSpec first = columns[0].ColumnWidth;
        ColumnWidthSpec second = columns[1].ColumnWidth;

        Assert.Equal(ColumnWidthKind.Proportional, first.Kind);
        Assert.Equal(2, first.Value, 3);
        Assert.Equal(ColumnWidthKind.Proportional, second.Kind);
        Assert.Equal(1, second.Value, 3);
    }

    [Fact]
    public void ParseColumnWidthHelperIsUnchanged()
    {
        // Direct check of the static parser used by FoTableColumn.ColumnWidth.
        ColumnWidthSpec spec = FoTableColumn.ParseColumnWidth("proportional-column-width(3)", 12_000);
        Assert.Equal(ColumnWidthKind.Proportional, spec.Kind);
        Assert.Equal(3, spec.Value, 3);

        Assert.Equal(ColumnWidthKind.Absolute, FoTableColumn.ParseColumnWidth("100pt", 12_000).Kind);
        Assert.Equal(ColumnWidthKind.Percent, FoTableColumn.ParseColumnWidth("40%", 12_000).Kind);
        Assert.Equal(ColumnWidthKind.Auto, FoTableColumn.ParseColumnWidth("auto", 12_000).Kind);
    }

    [Fact]
    public void ListProvisionalGeometryUnchanged()
    {
        // A list using label-end()/body-start() in provisional values must not crash and must keep
        // the default provisional geometry (these layout-side functions are not resolved here).
        FoRoot root = Parse("""
            <fo:list-block provisional-distance-between-starts="24pt" provisional-label-separation="6pt">
              <fo:list-item>
                <fo:list-item-label end-indent="label-end()"><fo:block>1.</fo:block></fo:list-item-label>
                <fo:list-item-body start-indent="body-start()"><fo:block>first</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """);

        FoListBlock list = root.PageSequences.First().Flow!.ChildObjects.OfType<FoListBlock>().Single();

        Assert.Equal(24_000, list.ProvisionalDistanceBetweenStarts.Millipoints, 3);
        Assert.Equal(6_000, list.ProvisionalLabelSeparation.Millipoints, 3);

        // The label end-indent / body start-indent declared as label-end()/body-start() should fall
        // back to their default (zero) length rather than throwing, since those are deferred.
        FoListItem item = list.Items.Single();
        Assert.Equal(0, item.Label!.Properties.GetLength("end-indent", FoLength.Zero).Millipoints, 3);
        Assert.Equal(0, item.Body!.Properties.GetLength("start-indent", FoLength.Zero).Millipoints, 3);
    }
}
