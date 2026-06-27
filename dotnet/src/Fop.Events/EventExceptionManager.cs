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
/// Converts events into exceptions.
/// <para>
/// Port of <c>org.apache.fop.events.EventExceptionManager</c>. Java discovered exception factories
/// through the JAR service-provider mechanism; this port replaces that with an explicit registry
/// (see <see cref="RegisterExceptionFactory"/>), keyed by the exception type's full name.
/// </para>
/// </summary>
public static class EventExceptionManager
{
    private static readonly Dictionary<string, IExceptionFactory> ExceptionFactories = new();
    private static readonly object RegistryLock = new();

    /// <summary>
    /// Registers an exception factory under its <see cref="IExceptionFactory.ExceptionType"/>
    /// full name. Replaces the Java service-provider registration.
    /// </summary>
    public static void RegisterExceptionFactory(IExceptionFactory factory)
    {
        ArgumentNullException.ThrowIfNull(factory);
        string name = factory.ExceptionType.FullName
            ?? factory.ExceptionType.Name;
        lock (RegistryLock)
        {
            ExceptionFactories[name] = factory;
        }
    }

    /// <summary>
    /// Converts an event into an exception and throws it. If <paramref name="exceptionClass"/> is
    /// <c>null</c>, an <see cref="EventException"/> is thrown.
    /// </summary>
    /// <param name="event">The event to be converted.</param>
    /// <param name="exceptionClass">
    /// The full name of the exception class to be thrown, or <c>null</c>.
    /// </param>
    /// <exception cref="Exception">This method always throws.</exception>
    public static void ThrowException(Event @event, string? exceptionClass)
    {
        ArgumentNullException.ThrowIfNull(@event);
        if (exceptionClass != null)
        {
            IExceptionFactory? factory;
            lock (RegistryLock)
            {
                ExceptionFactories.TryGetValue(exceptionClass, out factory);
            }
            if (factory != null)
            {
                throw factory.CreateException(@event);
            }
            throw new ArgumentException("No such ExceptionFactory available: " + exceptionClass);
        }

        string msg = EventFormatter.Format(@event);
        // Get the original exception as the cause if one is given as a parameter.
        Exception? cause = null;
        foreach (var value in @event.Parameters.Values)
        {
            if (value is Exception ex)
            {
                cause = ex;
                break;
            }
        }
        throw cause != null
            ? new EventException(msg, cause)
            : new EventException(msg);
    }
}
