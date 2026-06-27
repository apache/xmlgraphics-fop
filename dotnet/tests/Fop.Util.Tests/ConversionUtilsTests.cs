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

using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

public class ConversionUtilsTests
{
    [Fact]
    public void ToIntArray_NullOrEmpty_ReturnsNull()
    {
        Assert.Null(ConversionUtils.ToIntArray(null, "\\s"));
        Assert.Null(ConversionUtils.ToIntArray("", "\\s"));
    }

    [Fact]
    public void ToIntArray_NullSeparator_ParsesSingleValue()
    {
        int[]? actual = ConversionUtils.ToIntArray("42", null);
        Assert.NotNull(actual);
        Assert.Equal([42], actual);
    }

    [Fact]
    public void ToIntArray_SplitsAndParsesSignedValues()
    {
        int[]? actual = ConversionUtils.ToIntArray("1 -2 3", "\\s");
        Assert.NotNull(actual);
        Assert.Equal([1, -2, 3], actual);
    }

    [Fact]
    public void ToIntArray_InvalidValue_Throws()
        => Assert.Throws<FormatException>(() => ConversionUtils.ToIntArray("abc", null));

    [Fact]
    public void ToDoubleArray_NullOrEmpty_ReturnsNull()
    {
        Assert.Null(ConversionUtils.ToDoubleArray(null, "\\s"));
        Assert.Null(ConversionUtils.ToDoubleArray("", "\\s"));
    }

    [Fact]
    public void ToDoubleArray_NullSeparator_ParsesSingleValue()
    {
        double[]? actual = ConversionUtils.ToDoubleArray("1.5", null);
        Assert.NotNull(actual);
        Assert.Equal([1.5], actual);
    }

    [Fact]
    public void ToDoubleArray_SplitsAndParsesValues()
    {
        double[]? actual = ConversionUtils.ToDoubleArray("1 2.5 -3.25", "\\s");
        Assert.NotNull(actual);
        Assert.Equal([1.0, 2.5, -3.25], actual);
    }

    [Fact]
    public void ToDoubleArray_UsesInvariantDecimalPoint()
    {
        double[]? actual = ConversionUtils.ToDoubleArray("3.14", null);
        Assert.NotNull(actual);
        Assert.Equal([3.14], actual);
    }
}
