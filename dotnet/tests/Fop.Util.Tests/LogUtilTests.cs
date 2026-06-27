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

using System;
using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

public class LogUtilTests
{
    private sealed class CapturingLog : ILog
    {
        public string? LastError { get; private set; }

        public void Error(object? message) => LastError = message?.ToString();
    }

    [Fact]
    public void HandleErrorThrowsWhenStrict()
    {
        FOPException ex = Assert.Throws<FOPException>(
            () => LogUtil.HandleError(new CapturingLog(), "boom", strict: true));
        Assert.Equal("boom", ex.Message);
    }

    [Fact]
    public void HandleErrorLogsWhenNotStrict()
    {
        CapturingLog log = new();

        LogUtil.HandleError(log, "boom", strict: false);

        Assert.Equal("boom", log.LastError);
    }

    [Fact]
    public void HandleExceptionRethrowsFopExceptionUnwrapped()
    {
        FOPException original = new("original");

        FOPException thrown = Assert.Throws<FOPException>(
            () => LogUtil.HandleException(new CapturingLog(), original, strict: true));

        Assert.Same(original, thrown);
    }

    [Fact]
    public void HandleExceptionWrapsOtherExceptionsWhenStrict()
    {
        InvalidOperationException cause = new("cause");

        FOPException thrown = Assert.Throws<FOPException>(
            () => LogUtil.HandleException(new CapturingLog(), cause, strict: true));

        Assert.Same(cause, thrown.InnerException);
    }

    [Fact]
    public void HandleExceptionLogsMessageWhenNotStrict()
    {
        CapturingLog log = new();

        LogUtil.HandleException(log, new InvalidOperationException("nope"), strict: false);

        Assert.Equal("nope", log.LastError);
    }

    [Fact]
    public void NoOpLogSwallowsEverything()
    {
        // Should not throw.
        NoOpLog.Instance.Error("ignored");
        NoOpLog.Instance.Error("ignored", new Exception());
        Assert.False(NoOpLog.Instance.IsErrorEnabled);
    }
}
