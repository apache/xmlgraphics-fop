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

namespace Fop.Fo.Expr;

/// <summary>
/// Exception thrown during property value evaluation.
/// <para>
/// TODO: replace when Fop.Fo.Expr is ported. This is a minimal forward declaration
/// of <c>org.apache.fop.fo.expr.PropertyException</c> so that the datatype layer can
/// compile and be tested standalone, before the real fo.expr layer exists.
/// </para>
/// </summary>
public class PropertyException : Exception
{
    /// <summary>Creates a new property exception with the given message.</summary>
    public PropertyException(string message)
        : base(message)
    {
    }

    /// <summary>Creates a new property exception wrapping the given cause.</summary>
    public PropertyException(string message, Exception innerException)
        : base(message, innerException)
    {
    }
}
