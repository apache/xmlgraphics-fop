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

namespace Fop.Events;

/// <summary>
/// Severity of an <see cref="Event"/>. Replaces the Java singleton enumeration
/// <c>org.apache.fop.events.model.EventSeverity</c> with an idiomatic C# enum.
/// </summary>
public enum EventSeverity
{
    /// <summary>Informational message.</summary>
    Info,

    /// <summary>Warning.</summary>
    Warn,

    /// <summary>Error.</summary>
    Error,

    /// <summary>Fatal error: processing cannot continue.</summary>
    Fatal,
}

/// <summary>Helpers that mirror the Java <c>EventSeverity.valueOf</c> / <c>getName</c> contract.</summary>
public static class EventSeverityExtensions
{
    /// <summary>Returns the canonical upper-case name (e.g. <c>"INFO"</c>) used by the original FOP.</summary>
    public static string GetName(this EventSeverity severity) => severity switch
    {
        EventSeverity.Info => "INFO",
        EventSeverity.Warn => "WARN",
        EventSeverity.Error => "ERROR",
        EventSeverity.Fatal => "FATAL",
        _ => throw new ArgumentOutOfRangeException(nameof(severity)),
    };

    /// <summary>Parses a severity name case-insensitively, matching Java's <c>valueOf</c>.</summary>
    /// <exception cref="ArgumentException">If the name does not match a known severity.</exception>
    public static EventSeverity ParseSeverity(string name)
    {
        ArgumentNullException.ThrowIfNull(name);
        return name.ToUpperInvariant() switch
        {
            "INFO" => EventSeverity.Info,
            "WARN" => EventSeverity.Warn,
            "ERROR" => EventSeverity.Error,
            "FATAL" => EventSeverity.Fatal,
            _ => throw new ArgumentException($"Illegal value for enumeration: {name}"),
        };
    }
}
