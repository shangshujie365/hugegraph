// Copyright 2017 hugegraph Authors
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

package com.baidu.hugegraph.hadoop.config;

import com.google.common.base.Preconditions;
import com.baidu.hugegraph.diskstorage.configuration.ConfigElement;
import com.baidu.hugegraph.diskstorage.configuration.ConfigNamespace;
import com.baidu.hugegraph.diskstorage.configuration.ModifiableConfiguration;
import com.baidu.hugegraph.graphdb.configuration.GraphDatabaseConfiguration;
import org.apache.hadoop.conf.Configuration;

public class ModifiableHadoopConfiguration extends ModifiableConfiguration {

    private final Configuration conf;

    private ModifiableHadoopConfiguration(ConfigNamespace root, Configuration c, Restriction restriction) {
        super(root, new HadoopConfiguration(c), restriction);
        this.conf = c;
    }

    public static ModifiableHadoopConfiguration of(ConfigNamespace root, Configuration c) {
        Preconditions.checkNotNull(c);
        return new ModifiableHadoopConfiguration(root, c, Restriction.NONE);
    }

    public static ModifiableConfiguration prefixView(ConfigNamespace newRoot, ConfigNamespace prefixRoot,
                                                     ModifiableHadoopConfiguration mc) {
        HadoopConfiguration prefixConf = new HadoopConfiguration(mc.getHadoopConfiguration(),
                ConfigElement.getPath(prefixRoot, true) + ".");
        return new ModifiableConfiguration(newRoot, prefixConf,  Restriction.NONE);
    }

    public Configuration getHadoopConfiguration() {
        return conf;
    }

    public ModifiableConfiguration getHugeGraphConf() {
        return prefixView(GraphDatabaseConfiguration.ROOT_NS, HugeGraphHadoopConfiguration.GRAPH_CONFIG_KEYS, this);
    }
}
