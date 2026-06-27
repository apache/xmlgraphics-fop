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

/// <summary>
/// Mirrors <c>org.apache.fop.util.LanguageTagsTestCase</c>.
/// </summary>
public class LanguageTagsTests
{
    [Fact]
    public void ToLanguageTag_RejectsNull()
        => Assert.Throws<ArgumentNullException>(() => LanguageTags.ToLanguageTag(null!));

    [Fact]
    public void ToLanguageTag_FormatsLanguageAndCountry()
    {
        Assert.Equal("", LanguageTags.ToLanguageTag(new Locale("")));
        Assert.Equal("en", LanguageTags.ToLanguageTag(new Locale("en")));
        Assert.Equal("en-US", LanguageTags.ToLanguageTag(new Locale("en", "US")));
        Assert.Equal("en-US", LanguageTags.ToLanguageTag(new Locale("EN", "us")));
    }

    [Fact]
    public void ToLocale_RejectsNull()
        => Assert.Throws<ArgumentNullException>(() => LanguageTags.ToLocale(null!));

    [Fact]
    public void ToLocale_ParsesLanguageAndCountry()
    {
        Assert.Equal(new Locale(""), LanguageTags.ToLocale(""));
        Assert.Equal(new Locale("en"), LanguageTags.ToLocale("en"));
        Assert.Equal(new Locale("en", "US"), LanguageTags.ToLocale("en-US"));
        Assert.Equal(new Locale("en", "US"), LanguageTags.ToLocale("EN-us"));
    }
}
