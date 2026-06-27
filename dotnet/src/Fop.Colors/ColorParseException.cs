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

namespace Fop.Colors;

/// <summary>
/// Thrown when a colour string cannot be parsed or does not follow any of the
/// supported formats.
/// <para>
/// Stands in for the Java <c>org.apache.fop.fo.expr.PropertyException</c> that
/// <c>ColorUtil.parseColorString</c> throws. The full <c>PropertyException</c>
/// belongs to the FO expression layer (not part of this standalone slice), so a
/// dedicated, dependency-free exception is used here.
/// </para>
/// </summary>
public sealed class ColorParseException : Exception
{
    /// <summary>Creates a new exception with the given message.</summary>
    public ColorParseException(string message)
        : base(message)
    {
    }

    /// <summary>Creates a new exception wrapping an inner cause.</summary>
    public ColorParseException(string message, Exception innerException)
        : base(message, innerException)
    {
    }
}
