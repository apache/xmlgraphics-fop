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

using System.Drawing;
using System.Xml.Linq;
using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

public class XMLUtilTests
{
    private static XElement Element(params (string Name, string Value)[] attrs)
    {
        var e = new XElement("x");
        foreach (var (name, value) in attrs)
        {
            e.SetAttributeValue(name, value);
        }

        return e;
    }

    [Fact]
    public void GetAttributeAsBoolean_MatchesJavaBooleanValueOf()
    {
        Assert.True(XMLUtil.GetAttributeAsBoolean(Element(("a", "true")), "a", false));
        Assert.True(XMLUtil.GetAttributeAsBoolean(Element(("a", "TRUE")), "a", false));
        Assert.False(XMLUtil.GetAttributeAsBoolean(Element(("a", "yes")), "a", true));
        Assert.True(XMLUtil.GetAttributeAsBoolean(Element(), "a", true)); // missing -> default
    }

    [Fact]
    public void GetAttributeAsInt_WithDefault()
    {
        Assert.Equal(5, XMLUtil.GetAttributeAsInt(Element(("a", "5")), "a", -1));
        Assert.Equal(-1, XMLUtil.GetAttributeAsInt(Element(), "a", -1));
    }

    [Fact]
    public void GetAttributeAsInt_MissingThrows()
        => Assert.Throws<FormatException>(() => XMLUtil.GetAttributeAsInt(Element(), "a"));

    [Fact]
    public void GetAttributeAsInteger_NullWhenMissing()
    {
        Assert.Equal(7, XMLUtil.GetAttributeAsInteger(Element(("a", "7")), "a"));
        Assert.Null(XMLUtil.GetAttributeAsInteger(Element(), "a"));
    }

    [Fact]
    public void GetAttributeAsRectangle_ParsesFourInts()
    {
        Rectangle? r = XMLUtil.GetAttributeAsRectangle(Element(("a", " 1 2 3 4 ")), "a");
        Assert.Equal(new Rectangle(1, 2, 3, 4), r);
        Assert.Null(XMLUtil.GetAttributeAsRectangle(Element(), "a"));
    }

    [Fact]
    public void GetAttributeAsRectangle2D_ParsesFourDoubles()
    {
        RectangleF r = XMLUtil.GetAttributeAsRectangle2D(Element(("a", "1.5 2 3 4")), "a");
        Assert.Equal(new RectangleF(1.5f, 2f, 3f, 4f), r);
    }

    [Fact]
    public void GetAttributeAsRectangle_WrongCount_Throws()
        => Assert.Throws<ArgumentException>(
            () => XMLUtil.GetAttributeAsRectangle(Element(("a", "1 2 3")), "a"));

    [Fact]
    public void GetAttributeAsIntArray_SplitsOnWhitespace()
    {
        int[]? actual = XMLUtil.GetAttributeAsIntArray(Element(("a", "1 2 3")), "a");
        Assert.NotNull(actual);
        Assert.Equal([1, 2, 3], actual);
    }

    [Fact]
    public void Escape_ReplacesMarkupCharsWithLowerCaseNcr()
    {
        Assert.Equal("a&#x3c;b&#x3e;c&#x26;d", XMLUtil.Escape("a<b>c&d"));
        Assert.Equal("plain", XMLUtil.Escape("plain"));
    }

    [Fact]
    public void PositionAdjustments_EncodeDecodeRoundTrip()
    {
        int[][] dp =
        [
            [0, 0, 0, 0],
            [10, 0, 0, -5],
            [0, 0, 0, 0],
        ];
        string encoded = XMLUtil.EncodePositionAdjustments(dp);
        int[][]? decoded = XMLUtil.DecodePositionAdjustments(encoded);
        Assert.NotNull(decoded);
        Assert.Equal(dp.Length, decoded!.Length);
        for (int i = 0; i < dp.Length; i++)
        {
            Assert.Equal(dp[i], decoded[i]);
        }
    }

    [Fact]
    public void EncodePositionAdjustments_UsesZRunForRepeatedZeroes()
    {
        // First entry is all zeroes (4 leading zeroes) then a single non-zero.
        int[][] dp = [[0, 0, 0, 0], [0, 0, 0, 9]];
        string encoded = XMLUtil.EncodePositionAdjustments(dp);
        Assert.Equal("2 Z7 9", encoded);
    }

    [Fact]
    public void DecodePositionAdjustments_NullReturnsNull()
        => Assert.Null(XMLUtil.DecodePositionAdjustments(null));

    [Fact]
    public void Constants_AreExposed()
    {
        Assert.Equal("CDATA", XMLUtil.Cdata);
        Assert.Equal("space", XMLUtil.XmlSpace.LocalName);
        Assert.Equal(XMLUtil.XmlNamespace, XMLUtil.XmlSpace.NamespaceUri);
        Assert.Equal("href", XMLUtil.XlinkHref.LocalName);
    }
}
