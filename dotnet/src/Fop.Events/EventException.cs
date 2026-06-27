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
/// Thrown when a fatal event is broadcast and no specific exception type was declared.
/// Mirrors the role of <c>org.apache.fop.events.EventExceptionManager</c>.
/// </summary>
public class EventException : Exception
{
    /// <summary>Creates a new <see cref="EventException"/>.</summary>
    public EventException(string message)
        : base(message)
    {
    }

    /// <summary>Creates a new <see cref="EventException"/> with an inner cause.</summary>
    public EventException(string message, Exception innerException)
        : base(message, innerException)
    {
    }
}
