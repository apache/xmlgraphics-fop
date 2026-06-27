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

namespace Fop.Util;

/// <summary>
/// Convenience logging utility methods used in FOP.
/// <para>Port of <c>org.apache.fop.util.LogUtil</c>.</para>
/// <para>
/// The Java original takes a commons-logging <c>Log</c>; here it takes the lean <see cref="ILog"/>
/// abstraction (see that type for the rationale). The method shapes are preserved.
/// </para>
/// </summary>
public static class LogUtil
{
    /// <summary>
    /// Convenience method that handles any error appropriately.
    /// </summary>
    /// <param name="log">the log to write to when not validating strictly.</param>
    /// <param name="errorStr">the error string.</param>
    /// <param name="strict">whether to validate strictly (throw) rather than log.</param>
    /// <exception cref="FOPException">thrown when <paramref name="strict"/> is <c>true</c>.</exception>
    public static void HandleError(ILog log, string errorStr, bool strict) =>
        HandleException(log, new FOPException(errorStr), strict);

    /// <summary>
    /// Convenience method that handles any exception appropriately.
    /// </summary>
    /// <param name="log">the log to write to when not validating strictly.</param>
    /// <param name="e">the exception.</param>
    /// <param name="strict">whether to validate strictly (re-throw) rather than log.</param>
    /// <exception cref="FOPException">thrown when <paramref name="strict"/> is <c>true</c>.</exception>
    public static void HandleException(ILog log, Exception e, bool strict)
    {
        ArgumentNullException.ThrowIfNull(log);
        ArgumentNullException.ThrowIfNull(e);

        if (strict)
        {
            // Preserve the Java behaviour: an existing FOPException is re-thrown as-is,
            // any other exception is wrapped.
            throw e is FOPException fop ? fop : new FOPException(e);
        }

        log.Error(e.Message);
    }
}
