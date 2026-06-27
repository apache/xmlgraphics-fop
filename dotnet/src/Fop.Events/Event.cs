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

using System.Globalization;

namespace Fop.Events;

/// <summary>
/// The default event class. Each event carries a unique identifier, a severity,
/// a culture (for message formatting) and a map of name/value parameters.
/// <para>Port of <c>org.apache.fop.events.Event</c>.</para>
/// </summary>
public class Event
{
    private readonly IReadOnlyDictionary<string, object?> parameters;

    /// <summary>Creates a new event using the current culture.</summary>
    public Event(object source, string eventId, EventSeverity severity, IReadOnlyDictionary<string, object?>? parameters)
        : this(source, eventId, severity, CultureInfo.CurrentCulture, parameters)
    {
    }

    /// <summary>Creates a new event.</summary>
    /// <param name="source">The object that created the event.</param>
    /// <param name="eventId">The unique identifier of the event.</param>
    /// <param name="severity">The severity level.</param>
    /// <param name="culture">The culture to use when formatting the event (or null for the current culture).</param>
    /// <param name="parameters">The event parameters (a map of name/value pairs).</param>
    public Event(object source, string eventId, EventSeverity severity, CultureInfo? culture,
        IReadOnlyDictionary<string, object?>? parameters)
    {
        ArgumentNullException.ThrowIfNull(source);
        ArgumentNullException.ThrowIfNull(eventId);

        Source = source;
        int pos = eventId.LastIndexOf('.');
        if (pos < 0 || pos == eventId.Length - 1)
        {
            EventKey = eventId;
        }
        else
        {
            EventGroupId = eventId[..pos];
            EventKey = eventId[(pos + 1)..];
        }

        Severity = severity;
        Culture = culture;
        this.parameters = parameters ?? new Dictionary<string, object?>();
    }

    /// <summary>The object that produced the event.</summary>
    public object Source { get; }

    /// <summary>The event group identifier, or <c>null</c> if there is no group.</summary>
    public string? EventGroupId { get; }

    /// <summary>The event key (the portion after the last dot of the event id).</summary>
    public string EventKey { get; }

    /// <summary>The full event identifier.</summary>
    public string EventId => EventGroupId is null ? EventKey : $"{EventGroupId}.{EventKey}";

    /// <summary>
    /// The severity level. May be changed by a listener to escalate or de-escalate handling.
    /// </summary>
    public EventSeverity Severity { get; set; }

    /// <summary>The culture used when formatting the event, or <c>null</c> for the current culture.</summary>
    public CultureInfo? Culture { get; }

    /// <summary>An unmodifiable view of all event parameters.</summary>
    public IReadOnlyDictionary<string, object?> Parameters => parameters;

    /// <summary>Returns a parameter value, or <c>null</c> if no value with this key is found.</summary>
    public object? GetParam(string key) => parameters.TryGetValue(key, out var value) ? value : null;

    /// <summary>Creates a fluent builder for the parameter map.</summary>
    public static ParamsBuilder Params() => new();

    /// <summary>Fluent builder for an event parameter map.</summary>
    public sealed class ParamsBuilder
    {
        private Dictionary<string, object?>? parameters;

        /// <summary>Adds a name/value parameter.</summary>
        public ParamsBuilder Param(string name, object? value)
        {
            (parameters ??= new Dictionary<string, object?>())[name] = value;
            return this;
        }

        /// <summary>Returns the accumulated parameter map.</summary>
        public IReadOnlyDictionary<string, object?> Build() =>
            parameters ?? new Dictionary<string, object?>();
    }
}
