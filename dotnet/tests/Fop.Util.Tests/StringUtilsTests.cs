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

public class StringUtilsTests
{
    private sealed class Painter(bool supports) : ISoftHyphenSupport
    {
        public bool SupportsSoftHyphen { get; } = supports;
    }

    private const string WithSoftHyphen = "ab\u00ADcd";

    [Fact]
    public void ProcessSoftHyphen_WhenUnsupported_ReplacesWithHyphenMinus()
        => Assert.Equal("ab-cd", StringUtils.ProcessSoftHyphen(WithSoftHyphen, new Painter(false)));

    [Fact]
    public void ProcessSoftHyphen_WhenSupported_LeavesTextUnchanged()
        => Assert.Equal(WithSoftHyphen, StringUtils.ProcessSoftHyphen(WithSoftHyphen, new Painter(true)));

    [Fact]
    public void ProcessSoftHyphen_NoSoftHyphen_IsUnchanged()
        => Assert.Equal("abcd", StringUtils.ProcessSoftHyphen("abcd", new Painter(false)));
}
