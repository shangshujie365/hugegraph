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

package com.baidu.hugegraph.graphdb.database.management;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.baidu.hugegraph.core.PropertyKey;
import com.baidu.hugegraph.core.HugeGraph;
import com.baidu.hugegraph.core.schema.SchemaStatus;
import com.baidu.hugegraph.core.schema.HugeGraphIndex;
import com.baidu.hugegraph.core.schema.HugeGraphManagement;
import com.baidu.hugegraph.diskstorage.util.time.Timer;
import com.baidu.hugegraph.diskstorage.util.time.TimestampProviders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GraphIndexStatusWatcher
        extends AbstractIndexStatusWatcher<GraphIndexStatusReport, GraphIndexStatusWatcher> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphIndexStatusWatcher.class);

    private String graphIndexName;

    public GraphIndexStatusWatcher(HugeGraph g, String graphIndexName) {
        super(g);
        this.graphIndexName = graphIndexName;
    }

    @Override
    protected GraphIndexStatusWatcher self() {
        return this;
    }

    @Override
    public GraphIndexStatusReport call() throws InterruptedException {
        Preconditions.checkNotNull(g, "Graph instance must not be null");
        Preconditions.checkNotNull(graphIndexName, "Index name must not be null");
        Preconditions.checkNotNull(status, "Target status must not be null");

        Map<String, SchemaStatus> notConverged = new HashMap<>();
        Map<String, SchemaStatus> converged = new HashMap<>();
        HugeGraphIndex idx;

        Timer t = new Timer(TimestampProviders.MILLI).start();
        boolean timedOut;
        while (true) {
            HugeGraphManagement mgmt = null;
            try {
                mgmt = g.openManagement();
                idx = mgmt.getGraphIndex(graphIndexName);
                for (PropertyKey pk : idx.getFieldKeys()) {
                    SchemaStatus s = idx.getIndexStatus(pk);
                    LOGGER.debug("Key {} has status {}", pk, s);
                    if (!status.equals(s))
                        notConverged.put(pk.toString(), s);
                    else
                        converged.put(pk.toString(), s);
                }
            } finally {
                if (null != mgmt)
                    mgmt.rollback(); // Let an exception here propagate up the stack
            }

            String waitingOn = Joiner.on(",").withKeyValueSeparator("=").join(notConverged);
            if (!notConverged.isEmpty()) {
                LOGGER.info("Some key(s) on index {} do not currently have status {}: {}", graphIndexName, status, waitingOn);
            } else {
                LOGGER.info("All {} key(s) on index {} have status {}", converged.size(), graphIndexName, status);
                return new GraphIndexStatusReport(true, graphIndexName, status, notConverged, converged, t.elapsed());
            }

            timedOut = null != timeout && 0 < t.elapsed().compareTo(timeout);

            if (timedOut) {
                LOGGER.info("Timed out ({}) while waiting for index {} to converge on status {}",
                        timeout, graphIndexName, status);
                return new GraphIndexStatusReport(false, graphIndexName, status, notConverged, converged, t.elapsed());
            }
            notConverged.clear();
            converged.clear();

            Thread.sleep(poll.toMillis());
        }
    }
}
