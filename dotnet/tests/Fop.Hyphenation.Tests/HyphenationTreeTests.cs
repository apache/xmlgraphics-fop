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

using System.IO;
using System.Xml;

using Fop.Hyphenation;
using Xunit;

namespace Fop.Hyphenation.Tests;

/// <summary>
/// Tests the Liang hyphenation over the bundled <c>en</c> pattern set loaded by
/// <see cref="Hyphenator"/>, plus the <see cref="PatternParser"/> XML loading of a small custom tree.
/// </summary>
public class HyphenationTreeTests
{
    private static HyphenationTree En => Hyphenator.Default.GetHyphenationTree("en")!;

    [Theory]
    [InlineData("hyphenation", "hy-phen-ation")]
    [InlineData("typography", "ty-pog-raphy")]
    [InlineData("formatting", "for-mat-ting")]
    [InlineData("represents", "rep-re-sents")]
    public void KnownWordsHyphenateAtExpectedPositions(string word, string expected)
    {
        Hyphenation? hyp = En.Hyphenate(word, 2, 2);
        Assert.NotNull(hyp);
        Assert.Equal(expected, hyp!.ToString());
    }

    [Fact]
    public void HyphenationPointsForHyphenationAreTwoAndSix()
    {
        Hyphenation hyp = Assert.IsType<Hyphenation>(En.Hyphenate("hyphenation", 2, 2));
        Assert.Equal([2, 6], hyp.GetHyphenationPoints());
        Assert.Equal("hy", hyp.GetPreHyphenText(0));
        Assert.Equal("phenation", hyp.GetPostHyphenText(0));
    }

    [Fact]
    public void ExceptionWordUsesTheExceptionList()
    {
        // "table" is pinned via the exceptions list to ta-ble.
        Hyphenation hyp = Assert.IsType<Hyphenation>(En.Hyphenate("table", 1, 1));
        Assert.Equal("ta-ble", hyp.ToString());
    }

    [Fact]
    public void RemainCharCountDropsEarlyBreaks()
    {
        // remain=4 forbids the break at offset 2 (hy|phenation); only offset 6 survives.
        Hyphenation hyp = Assert.IsType<Hyphenation>(En.Hyphenate("hyphenation", 4, 2));
        Assert.Equal([6], hyp.GetHyphenationPoints());
    }

    [Fact]
    public void PushCharCountDropsLateBreaks()
    {
        // push=6 forbids breaks within 6 chars of the end; only the early offset 2 survives.
        Hyphenation hyp = Assert.IsType<Hyphenation>(En.Hyphenate("hyphenation", 2, 6));
        Assert.Equal([2], hyp.GetHyphenationPoints());
    }

    [Fact]
    public void WordTooShortForCountsIsNotHyphenated()
    {
        // remain + push (7 + 7) exceeds the word length, so there is no valid break.
        Assert.Null(En.Hyphenate("hyphenation", 7, 7));
    }

    [Fact]
    public void WordWithNoPatternMatchesIsNotHyphenated()
    {
        Assert.Null(En.Hyphenate("xyzqwm", 1, 1));
    }

    [Fact]
    public void UnknownLanguageHasNoTree()
    {
        Assert.Null(Hyphenator.Default.GetHyphenationTree("zz"));
    }

    [Fact]
    public void UnknownLanguageHyphenateReturnsNull()
    {
        Assert.Null(Hyphenator.Default.Hyphenate("zz", "hyphenation", 2, 2));
    }

    [Fact]
    public void CountrySpecificFallsBackToBareLanguage()
    {
        // No en_us.xml is bundled, so en_US resolves to the bare "en" tree.
        HyphenationTree? tree = Hyphenator.Default.GetHyphenationTree("en", "US");
        Assert.NotNull(tree);
        Assert.Equal("hy-phen-ation", tree!.Hyphenate("hyphenation", 2, 2)!.ToString());
    }

    [Fact]
    public void GetHyphenationTreeIsCachedPerLanguage()
    {
        HyphenationTree? a = Hyphenator.Default.GetHyphenationTree("en");
        HyphenationTree? b = Hyphenator.Default.GetHyphenationTree("en");
        Assert.Same(a, b);
    }

    [Fact]
    public void PatternParserLoadsAClassesPatternsAndExceptions()
    {
        const string xml = """
            <?xml version="1.0" encoding="US-ASCII"?>
            <hyphenation-info>
              <classes>aA bB cC eE hH iI lL mN nN oO pP rR sS tT yY</classes>
              <exceptions>as-so-ciate</exceptions>
              <patterns>
                hy3ph he2n hena4 hen5at 1tion o2n 2io
              </patterns>
            </hyphenation-info>
            """;

        var tree = new HyphenationTree();
        using var reader = XmlReader.Create(new StringReader(xml),
            new XmlReaderSettings { DtdProcessing = DtdProcessing.Ignore });
        tree.LoadPatterns(reader);

        Assert.Equal("hy-phen-ation", tree.Hyphenate("hyphenation", 2, 2)!.ToString());

        // The exception list pins associate -> as-so-ci-ate.
        Hyphenation assoc = Assert.IsType<Hyphenation>(tree.Hyphenate("associate", 1, 1));
        Assert.Equal("as-so-ciate", assoc.ToString());
    }

    [Fact]
    public void FindPatternReturnsPackedInterletterValues()
    {
        var tree = new HyphenationTree();
        tree.AddPattern("hyph", "03000");   // value 3 between y and p
        Assert.Equal("03000", tree.FindPattern("hyph"));
        Assert.Equal(string.Empty, tree.FindPattern("zzz"));
    }
}
