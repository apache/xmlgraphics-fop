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

namespace Fop.Hyphenation;

/// <summary>
/// A hyphenation exception.
/// <para>
/// Faithful port of <c>org.apache.fop.hyphenation.HyphenationException</c>. The
/// Java original derives from <c>java.lang.Exception</c> (a checked exception);
/// here it derives from <see cref="Exception"/>.
/// </para>
/// </summary>
public class HyphenationException : Exception
{
    /// <summary>Construct a hyphenation exception.</summary>
    /// <param name="message">a message string.</param>
    public HyphenationException(string message)
        : base(message)
    {
    }

    /// <summary>Construct a hyphenation exception with an inner cause.</summary>
    /// <param name="message">a message string.</param>
    /// <param name="innerException">the underlying cause.</param>
    public HyphenationException(string message, Exception innerException)
        : base(message, innerException)
    {
    }
}
