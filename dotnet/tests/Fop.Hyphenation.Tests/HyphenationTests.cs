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

using Fop.Hyphenation;
using Xunit;

namespace Fop.Hyphenation.Tests;

public class HyphenTests
{
    [Fact]
    public void ThreeArgConstructorSetsAllParts()
    {
        var h = new Hyphen("pre", "no", "post");
        Assert.Equal("pre", h.PreBreak);
        Assert.Equal("no", h.NoBreak);
        Assert.Equal("post", h.PostBreak);
    }

    [Fact]
    public void SingleArgConstructorLeavesOthersNull()
    {
        var h = new Hyphen("x");
        Assert.Equal("x", h.PreBreak);
        Assert.Null(h.NoBreak);
        Assert.Null(h.PostBreak);
    }

    [Fact]
    public void ToStringReturnsDashForPlainHyphen()
    {
        var h = new Hyphen("-");
        Assert.Equal("-", h.ToString());
    }

    [Fact]
    public void ToStringRendersBracesInPrePostNoOrder()
    {
        var h = new Hyphen("k", "n", "k");
        // Order in Java: {preBreak}{postBreak}{noBreak}
        Assert.Equal("{k}{k}{n}", h.ToString());
    }

    [Fact]
    public void ToStringWithOnlyPreBreakNonDashRendersBraces()
    {
        var h = new Hyphen("=");
        Assert.Equal("{=}{}{}", h.ToString());
    }
}

public class HyphenationTests
{
    [Fact]
    public void LengthEqualsNumberOfPoints()
    {
        var hyp = new Hyphenation("hyphenation", [2, 6, 8]);
        Assert.Equal(3, hyp.Length);
    }

    [Fact]
    public void PreAndPostHyphenTextSplitAtPoints()
    {
        var hyp = new Hyphenation("hyphenation", [2, 6, 8]);
        Assert.Equal("hy", hyp.GetPreHyphenText(0));
        Assert.Equal("phenation", hyp.GetPostHyphenText(0));
        Assert.Equal("hyphen", hyp.GetPreHyphenText(1));
        Assert.Equal("ation", hyp.GetPostHyphenText(1));
    }

    [Fact]
    public void GetHyphenationPointsReturnsThePoints()
    {
        int[] points = [2, 6, 8];
        var hyp = new Hyphenation("hyphenation", points);
        Assert.Equal(points, hyp.GetHyphenationPoints());
    }

    [Fact]
    public void ToStringInsertsDashesAtPoints()
    {
        var hyp = new Hyphenation("hyphenation", [2, 6, 8]);
        Assert.Equal("hy-phen-at-ion", hyp.ToString());
    }

    [Fact]
    public void NoPointsToStringIsTheWholeWord()
    {
        var hyp = new Hyphenation("word", []);
        Assert.Equal(0, hyp.Length);
        Assert.Equal("word", hyp.ToString());
    }

    [Fact]
    public void NullWordThrows()
    {
        Assert.Throws<ArgumentNullException>(() => new Hyphenation(null!, [1]));
    }
}

public class HyphenationExceptionTests
{
    [Fact]
    public void MessageIsPreserved()
    {
        var ex = new HyphenationException("boom");
        Assert.Equal("boom", ex.Message);
    }

    [Fact]
    public void InnerExceptionIsPreserved()
    {
        var inner = new InvalidOperationException("cause");
        var ex = new HyphenationException("boom", inner);
        Assert.Same(inner, ex.InnerException);
    }
}
