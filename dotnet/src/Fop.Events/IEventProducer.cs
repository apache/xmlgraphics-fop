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
/// Marker interface that all event-producer interfaces must extend.
/// <para>
/// Each method on a producer interface represents an event. By convention the first
/// parameter must be <c>object source</c>; the remaining parameters become the event's
/// named parameters. A method may be annotated with <see cref="EventAttribute"/> to set
/// its severity and, for fatal events, the exception type to raise.
/// </para>
/// <para>Port of <c>org.apache.fop.events.EventProducer</c>.</para>
/// </summary>
public interface IEventProducer
{
}

/// <summary>
/// Declares the severity (and optional exception type) of an event-producer method.
/// Replaces the generated <c>event-model.xml</c> + javadoc annotations used by the Java code
/// with a compile-time, reflection-friendly attribute.
/// </summary>
[AttributeUsage(AttributeTargets.Method, AllowMultiple = false)]
public sealed class EventAttribute : Attribute
{
    /// <summary>The severity of the event. Defaults to <see cref="EventSeverity.Info"/>.</summary>
    public EventSeverity Severity { get; init; } = EventSeverity.Info;

    /// <summary>
    /// For <see cref="EventSeverity.Fatal"/> events, the exception type to throw. Must have a
    /// constructor accepting a single <see cref="string"/> message. If null, a generic
    /// <see cref="EventException"/> is thrown.
    /// </summary>
    public Type? ExceptionType { get; init; }
}
