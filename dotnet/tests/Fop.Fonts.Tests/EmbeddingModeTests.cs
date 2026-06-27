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

using Fop.Fonts;
using Xunit;

namespace Fop.Fonts.Tests;

public class EmbeddingModeTests
{
    [Theory]
    [InlineData(EmbeddingMode.Auto, "auto")]
    [InlineData(EmbeddingMode.Full, "full")]
    [InlineData(EmbeddingMode.Subset, "subset")]
    public void GetName_IsLowerCase(EmbeddingMode mode, string expected)
    {
        Assert.Equal(expected, mode.GetName());
    }

    [Theory]
    [InlineData("auto", EmbeddingMode.Auto)]
    [InlineData("AUTO", EmbeddingMode.Auto)]
    [InlineData("Full", EmbeddingMode.Full)]
    [InlineData("subset", EmbeddingMode.Subset)]
    public void GetValue_IsCaseInsensitive(string value, EmbeddingMode expected)
    {
        Assert.Equal(expected, EmbeddingModeExtensions.GetValue(value));
    }

    [Fact]
    public void GetValue_UnknownThrows()
    {
        Assert.Throws<ArgumentException>(() => EmbeddingModeExtensions.GetValue("bogus"));
    }
}
