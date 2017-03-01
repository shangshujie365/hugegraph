// Copyright 2017 HugeGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.baidu.hugegraph.graphdb.types.typemaker;

import com.baidu.hugegraph.core.*;
import com.baidu.hugegraph.core.schema.DefaultSchemaMaker;
import com.baidu.hugegraph.core.schema.EdgeLabelMaker;
import com.baidu.hugegraph.core.schema.PropertyKeyMaker;
import com.baidu.hugegraph.core.schema.VertexLabelMaker;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */

public class DisableDefaultSchemaMaker implements DefaultSchemaMaker {

    public static final DefaultSchemaMaker INSTANCE = new DisableDefaultSchemaMaker();

    private DisableDefaultSchemaMaker() {
    }

    @Override
    public EdgeLabel makeEdgeLabel(EdgeLabelMaker factory) {
        throw new IllegalArgumentException("Edge Label with given name does not exist: " + factory.getName());
    }

    @Override
    public Cardinality defaultPropertyCardinality(String key) {
        return Cardinality.SINGLE;
    }

    @Override
    public PropertyKey makePropertyKey(PropertyKeyMaker factory) {
        throw new IllegalArgumentException("Property Key with given name does not exist: " + factory.getName());
    }

    @Override
    public VertexLabel makeVertexLabel(VertexLabelMaker factory) {
        throw new IllegalArgumentException("Vertex Label with given name does not exist: " + factory.getName());
    }

    @Override
    public boolean ignoreUndefinedQueryTypes() {
        return false;
    }
}
