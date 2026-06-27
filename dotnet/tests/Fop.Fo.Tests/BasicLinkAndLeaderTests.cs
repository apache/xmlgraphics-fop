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

using System.Linq;

using Fop.Fo;

using Xunit;

namespace Fop.Fo.Tests;

/// <summary>
/// Parsing + property tests for <c>fo:basic-link</c> (internal/external destinations, url() unwrap)
/// and <c>fo:leader</c> (pattern, length and rule-thickness parsing).
/// </summary>
public sealed class BasicLinkAndLeaderTests
{
    private static FoBlock FirstBlock(string blockMarkup)
    {
        string fo = $"""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="400pt" page-height="400pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  {blockMarkup}
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        FoRoot root = FoTreeBuilder.ParseString(fo);
        return root.PageSequences.First().Flow!.ChildObjects.OfType<FoBlock>().First();
    }

    // ----- fo:basic-link ----------------------------------------------------------------

    [Fact]
    public void ParsesBasicLinkWithInternalDestination()
    {
        FoBlock block = FirstBlock(
            "<fo:block>See <fo:basic-link internal-destination=\"chap1\">Chapter 1</fo:basic-link></fo:block>");
        FoBasicLink link = block.ChildObjects.OfType<FoBasicLink>().Single();

        Assert.Equal("basic-link", link.LocalName);
        Assert.Equal("chap1", link.InternalDestination);
        Assert.Equal(string.Empty, link.ExternalDestination);
    }

    [Fact]
    public void ParsesBasicLinkChildrenAsInlineContent()
    {
        FoBlock block = FirstBlock(
            "<fo:block><fo:basic-link internal-destination=\"x\">click here</fo:basic-link></fo:block>");
        FoBasicLink link = block.ChildObjects.OfType<FoBasicLink>().Single();

        // The link's text children are collected (it is a content container).
        FOText text = link.Children.OfType<FOText>().Single();
        Assert.Equal("click here", text.Text);
    }

    [Fact]
    public void ParsesBasicLinkWithExternalDestination()
    {
        FoBlock block = FirstBlock(
            "<fo:block><fo:basic-link external-destination=\"https://example.com/\">site</fo:basic-link></fo:block>");
        FoBasicLink link = block.ChildObjects.OfType<FoBasicLink>().Single();

        Assert.Equal("https://example.com/", link.ExternalDestination);
        Assert.Equal(string.Empty, link.InternalDestination);
    }

    [Theory]
    [InlineData("url(https://example.com/)", "https://example.com/")]
    [InlineData("url('https://example.com/')", "https://example.com/")]
    [InlineData("https://example.com/", "https://example.com/")]
    public void UnwrapsUrlWrapperFromExternalDestination(string raw, string expected)
    {
        FoBlock block = FirstBlock(
            $"<fo:block><fo:basic-link external-destination=\"{raw}\">x</fo:basic-link></fo:block>");
        FoBasicLink link = block.ChildObjects.OfType<FoBasicLink>().Single();

        Assert.Equal(expected, link.ExternalDestination);
    }

    [Fact]
    public void UnwrapsSingleQuotedExternalDestinationWithoutUrlWrapper()
    {
        // A bare single-quoted value (no url(...) wrapper) still has its quotes stripped.
        FoBlock block = FirstBlock(
            "<fo:block><fo:basic-link external-destination=\"'https://example.com/'\">x</fo:basic-link></fo:block>");
        FoBasicLink link = block.ChildObjects.OfType<FoBasicLink>().Single();

        Assert.Equal("https://example.com/", link.ExternalDestination);
    }

    // ----- fo:leader --------------------------------------------------------------------

    [Fact]
    public void LeaderDefaultsToSpacePattern()
    {
        FoBlock block = FirstBlock("<fo:block><fo:leader/></fo:block>");
        FoLeader leader = block.ChildObjects.OfType<FoLeader>().Single();

        Assert.Equal("leader", leader.LocalName);
        Assert.Equal(LeaderPattern.Space, leader.Pattern);
        Assert.Null(leader.LeaderLength);
    }

    [Theory]
    [InlineData("dots", LeaderPattern.Dots)]
    [InlineData("rule", LeaderPattern.Rule)]
    [InlineData("space", LeaderPattern.Space)]
    [InlineData("use-content", LeaderPattern.UseContent)]
    public void ParsesLeaderPattern(string raw, LeaderPattern expected)
    {
        FoBlock block = FirstBlock($"<fo:block><fo:leader leader-pattern=\"{raw}\"/></fo:block>");
        FoLeader leader = block.ChildObjects.OfType<FoLeader>().Single();

        Assert.Equal(expected, leader.Pattern);
    }

    [Fact]
    public void ParsesFixedLeaderLength()
    {
        FoBlock block = FirstBlock("<fo:block><fo:leader leader-length=\"50pt\"/></fo:block>");
        FoLeader leader = block.ChildObjects.OfType<FoLeader>().Single();

        Assert.NotNull(leader.LeaderLength);
        Assert.Equal(50_000, leader.LeaderLength!.Value.Millipoints, 3);
    }

    [Fact]
    public void ParsesLeaderLengthOptimumComponent()
    {
        FoBlock block = FirstBlock("<fo:block><fo:leader leader-length.optimum=\"30pt\"/></fo:block>");
        FoLeader leader = block.ChildObjects.OfType<FoLeader>().Single();

        Assert.Equal(30_000, leader.LeaderLength!.Value.Millipoints, 3);
    }

    [Fact]
    public void PercentageLeaderLengthIsTreatedAsExpanding()
    {
        FoBlock block = FirstBlock("<fo:block><fo:leader leader-length=\"100%\"/></fo:block>");
        FoLeader leader = block.ChildObjects.OfType<FoLeader>().Single();

        Assert.Null(leader.LeaderLength);
    }

    [Fact]
    public void ParsesRuleThicknessWithDefault()
    {
        FoBlock plain = FirstBlock("<fo:block><fo:leader leader-pattern=\"rule\"/></fo:block>");
        Assert.Equal(1_000, plain.ChildObjects.OfType<FoLeader>().Single().RuleThickness.Millipoints, 3);

        FoBlock thick = FirstBlock(
            "<fo:block><fo:leader leader-pattern=\"rule\" rule-thickness=\"3pt\"/></fo:block>");
        Assert.Equal(3_000, thick.ChildObjects.OfType<FoLeader>().Single().RuleThickness.Millipoints, 3);
    }
}
