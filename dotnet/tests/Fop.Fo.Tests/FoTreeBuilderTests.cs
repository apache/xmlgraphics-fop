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

public class FoTreeBuilderTests
{
    private const string SampleFo = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="11pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
                margin-top="20mm" margin-left="25mm">
              <fo:region-body margin-top="10mm"/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="A4">
            <fo:flow flow-name="xsl-region-body">
              <fo:block space-before="6pt" text-align="center" font-weight="bold">Hello World</fo:block>
              <fo:block font-size="14pt">Bigger <fo:inline font-style="italic">italic</fo:inline> text</fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void ParsesStructure()
    {
        FoRoot root = FoTreeBuilder.ParseString(SampleFo);

        Assert.NotNull(root.LayoutMasterSet);
        var master = root.LayoutMasterSet!.GetSimplePageMaster("A4");
        Assert.NotNull(master);
        Assert.Equal(210, master!.PageWidth.Millipoints / 1000 * 25.4 / 72, 1); // ~210mm in mm
        Assert.NotNull(master.RegionBody);

        var seq = Assert.Single(root.PageSequences);
        Assert.Equal("A4", seq.MasterReference);
        Assert.NotNull(seq.Flow);

        var blocks = seq.Flow!.ChildObjects.OfType<FoBlock>().ToList();
        Assert.Equal(2, blocks.Count);
    }

    [Fact]
    public void ResolvesInheritedAndLocalProperties()
    {
        FoRoot root = FoTreeBuilder.ParseString(SampleFo);
        var blocks = root.PageSequences.First().Flow!.ChildObjects.OfType<FoBlock>().ToList();

        FoBlock first = blocks[0];
        // font-size inherited from root (11pt); font-weight + align local.
        Assert.Equal(11_000, first.FontSizeMpt);
        Assert.Equal("Helvetica", first.FontFamily);
        Assert.Equal(700, first.FontWeight);
        Assert.Equal(TextAlign.Center, first.TextAlign);
        Assert.Equal(6.0, first.SpaceBefore.Points, 3);

        FoBlock second = blocks[1];
        Assert.Equal(14_000, second.FontSizeMpt); // local override

        // The inline inside the second block inherits 14pt and sets italic.
        var inline = second.ChildObjects.OfType<FoInline>().Single();
        Assert.Equal(14_000, inline.FontSizeMpt);
        Assert.Equal(FontStyle.Italic, inline.FontStyle);
    }

    [Fact]
    public void CollapsesWhitespace()
    {
        Assert.Equal("a b c", FoTreeBuilder.CollapseWhitespace("a   b\n\t c"));
        Assert.Equal(" hello ", FoTreeBuilder.CollapseWhitespace("  hello  "));
    }

    [Fact]
    public void BlockRetainsTextContent()
    {
        FoRoot root = FoTreeBuilder.ParseString(SampleFo);
        var first = root.PageSequences.First().Flow!.ChildObjects.OfType<FoBlock>().First();
        var text = first.Children.OfType<FOText>().Single();
        Assert.Equal("Hello World", text.Text);
    }
}
