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
/// Minimal logging abstraction standing in for Apache Commons Logging's <c>org.apache.commons.logging.Log</c>.
/// <para>
/// FOP threads a commons-logging <c>Log</c> through many of its helpers (see <see cref="LogUtil"/>).
/// Rather than take a dependency on <c>Microsoft.Extensions.Logging.Abstractions</c> for the single
/// <c>error(message)</c> call that <see cref="LogUtil"/> needs, <c>Fop.Util</c> keeps itself lean with
/// this tiny interface. Only the members actually exercised by ported code are declared; the surface
/// can grow (or be replaced with an adapter onto <c>ILogger</c>) as further FOP code is ported.
/// </para>
/// <para>
/// TODO: when the renderer/parser layers land, provide an adapter that maps this abstraction onto
/// <c>Microsoft.Extensions.Logging.ILogger</c> so logging integrates with the host application.
/// </para>
/// </summary>
public interface ILog
{
    /// <summary>Gets a value indicating whether error logging is enabled.</summary>
    bool IsErrorEnabled => true;

    /// <summary>Logs a message at the "error" level.</summary>
    /// <param name="message">the message to log (may be <c>null</c>).</param>
    void Error(object? message);

    /// <summary>Logs a message and an associated exception at the "error" level.</summary>
    /// <param name="message">the message to log (may be <c>null</c>).</param>
    /// <param name="exception">the exception to log.</param>
    void Error(object? message, Exception exception) => Error(message);
}

/// <summary>
/// An <see cref="ILog"/> implementation that discards everything written to it.
/// <para>Equivalent to commons-logging's <c>NoOpLog</c>; handy as a default when no logger is supplied.</para>
/// </summary>
public sealed class NoOpLog : ILog
{
    /// <summary>A shared, reusable instance.</summary>
    public static readonly NoOpLog Instance = new();

    /// <inheritdoc/>
    public bool IsErrorEnabled => false;

    /// <inheritdoc/>
    public void Error(object? message)
    {
        // Intentionally a no-op.
    }

    /// <inheritdoc/>
    public void Error(object? message, Exception exception)
    {
        // Intentionally a no-op.
    }
}
