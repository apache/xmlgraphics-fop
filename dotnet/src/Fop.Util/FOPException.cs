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
/// Exception thrown when FOP has a problem.
/// <para>
/// TODO: In Java this type lives in <c>org.apache.fop.apps.FOPException</c> (which also carries
/// SAX-locator / line-and-column information). It is reproduced here as a minimal stand-in so that
/// <see cref="LogUtil"/> can be ported within <c>Fop.Util</c>; it should migrate to a future
/// <c>Fop.Apps</c> project (mirroring the precedent of <c>Fop.DataTypes.PropertyException</c>) once
/// that layer is ported, at which point the locator support can be added.
/// </para>
/// </summary>
public class FOPException : Exception
{
    /// <summary>Initializes a new instance with the given message.</summary>
    /// <param name="message">the detail message.</param>
    public FOPException(string message)
        : base(message)
    {
    }

    /// <summary>Initializes a new instance wrapping the given cause.</summary>
    /// <param name="innerException">the wrapped exception.</param>
    public FOPException(Exception innerException)
        : base(innerException.Message, innerException)
    {
    }

    /// <summary>Initializes a new instance with the given message and cause.</summary>
    /// <param name="message">the detail message.</param>
    /// <param name="innerException">the wrapped exception.</param>
    public FOPException(string message, Exception innerException)
        : base(message, innerException)
    {
    }
}
