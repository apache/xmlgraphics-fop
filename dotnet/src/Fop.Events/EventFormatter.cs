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
using System.Text;
using System.Text.RegularExpressions;
using Fop.Util.Text;

namespace Fop.Events;

/// <summary>
/// Converts events into human-readable messages.
/// <para>
/// Port of <c>org.apache.fop.events.EventFormatter</c>. The Java version resolved message templates
/// from XML resource bundles. Those bundle files are not part of this port, so template lookup is
/// delegated to a pluggable <see cref="IEventModelMessageSource"/> (see <see cref="MessageSource"/>).
/// The template is then rendered with <see cref="AdvancedMessageFormat"/>, exactly as in Java.
/// </para>
/// </summary>
public static partial class EventFormatter
{
#if NET7_0_OR_GREATER
    [GeneratedRegex("\\{\\{.+\\}\\}")]
    private static partial Regex IncludesPatternGenerator();

    private static Regex IncludesPattern => IncludesPatternGenerator();
#else
    private static readonly Regex IncludesPattern = new("\\{\\{.+\\}\\}");
#endif

    static EventFormatter()
    {
        // Replaces the Java service-provider registration of the "lookup" PartFactory.
        AdvancedMessageFormat.RegisterPartFactory(new LookupFieldPartFactory());
    }

    /// <summary>
    /// The source of message templates. Defaults to an empty
    /// <see cref="InMemoryEventModelMessageSource"/>; assign a populated source (or a custom
    /// implementation) before formatting events.
    /// </summary>
    public static IEventModelMessageSource MessageSource { get; set; } =
        new InMemoryEventModelMessageSource();

    /// <summary>Formats an event using the event's own culture.</summary>
    /// <param name="event">The event.</param>
    /// <returns>The formatted message.</returns>
    public static string Format(Event @event)
    {
        ArgumentNullException.ThrowIfNull(@event);
        return Format(@event, @event.Culture);
    }

    /// <summary>Formats an event using a given culture.</summary>
    /// <param name="event">The event.</param>
    /// <param name="culture">The culture (may be <c>null</c>).</param>
    /// <returns>The formatted message.</returns>
    public static string Format(Event @event, CultureInfo? culture)
    {
        ArgumentNullException.ThrowIfNull(@event);
        string key = @event.EventKey;
        string? template = MessageSource.GetTemplate(@event.EventGroupId, key, culture);
        if (template == null)
        {
            // Java rendered "Missing bundle..." when no bundle existed for the group; a missing key
            // within an existing bundle threw. Both observable outcomes collapse into this single,
            // clear fallback message here.
            template = "Missing bundle. Can't lookup event key: '" + key + "'.";
            return Format(@event, template);
        }
        return Format(@event, ProcessIncludes(template, @event.EventGroupId, culture));
    }

    private static string ProcessIncludes(string template, string? groupId, CultureInfo? culture)
    {
        string input = template;
        int replacements;
        StringBuilder sb;
        do
        {
            sb = new StringBuilder(Math.Max(16, input.Length));
            replacements = ProcessIncludesInner(input, sb, groupId, culture);
            input = sb.ToString();
        }
        while (replacements > 0);
        return sb.ToString();
    }

    private static int ProcessIncludesInner(string template, StringBuilder sb, string? groupId,
        CultureInfo? culture)
    {
        int replacements = 0;
        int last = 0;
        foreach (Match m in IncludesPattern.Matches(template))
        {
            string include = m.Value;
            include = include.Substring(2, include.Length - 4);
            string? replacement = MessageSource.GetTemplate(groupId, include, culture);
            sb.Append(template, last, m.Index - last);
            sb.Append(replacement ?? string.Empty);
            last = m.Index + m.Length;
            replacements++;
        }
        sb.Append(template, last, template.Length - last);
        return replacements;
    }

    /// <summary>
    /// Formats the event using a given pattern. The pattern needs to be compatible with
    /// <see cref="AdvancedMessageFormat"/>.
    /// </summary>
    /// <param name="event">The event.</param>
    /// <param name="pattern">The pattern (compatible with <see cref="AdvancedMessageFormat"/>).</param>
    /// <returns>The formatted message.</returns>
    public static string Format(Event @event, string pattern)
    {
        ArgumentNullException.ThrowIfNull(@event);
        ArgumentNullException.ThrowIfNull(pattern);
        var format = new AdvancedMessageFormat(pattern);
        var parameters = new Dictionary<string, object?>(@event.Parameters)
        {
            ["source"] = @event.Source,
            ["severity"] = @event.Severity,
            ["groupID"] = @event.EventGroupId,
            ["locale"] = @event.Culture,
        };
        return format.Format(parameters);
    }
}
