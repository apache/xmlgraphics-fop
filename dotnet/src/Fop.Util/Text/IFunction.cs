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

namespace Fop.Util.Text;

/// <summary>
/// Implementations of this interface do some computation based on the message parameters given to
/// it. Note: at the moment, this has to be done in a locale-independent way since there is no
/// locale information.
/// <para>Port of the nested <c>AdvancedMessageFormat.Function</c> interface.</para>
/// </summary>
public interface IFunction
{
    /// <summary>Executes the function.</summary>
    /// <param name="parameters">The message parameters.</param>
    /// <returns>The function result.</returns>
    object? Evaluate(IReadOnlyDictionary<string, object?> parameters);

    /// <summary>The name of the function.</summary>
    object Name { get; }
}
