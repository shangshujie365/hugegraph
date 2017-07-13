package com.baidu.hugegraph.backend.store.cassandra;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.hugegraph.backend.BackendException;
import com.baidu.hugegraph.util.E;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;

public class CassandraSessionPool {

    private static final Logger logger =
            LoggerFactory.getLogger(CassandraStore.class);

    private Cluster cluster;
    private String keyspace;

    private ThreadLocal<Session> threadLocalSession;
    private AtomicInteger sessionCount;

    public CassandraSessionPool(String keyspace) {
        this.keyspace = keyspace;

        this.threadLocalSession = new ThreadLocal<>();
        this.sessionCount = new AtomicInteger(0);
    }

    public void open(String hosts, int port) {
        if (opened()) {
            throw new BackendException("Please close the old SessionPool " +
                      "before opening a new one");
        }
        this.cluster = Cluster.builder()
                       .addContactPoints(hosts.split(","))
                       .withPort(port)
                       .build();
    }

    public final boolean opened() {
        return (this.cluster != null && !this.cluster.isClosed());
    }

    public final Cluster cluster() {
        return this.cluster;
    }

    public synchronized Session session() {
        Session session = this.threadLocalSession.get();
        if (session == null) {
            session = new Session(this.cluster.connect(this.keyspace));
            this.threadLocalSession.set(session);
            this.sessionCount.incrementAndGet();
            logger.debug("Now(after connect()) session count is: {}",
                         this.sessionCount.get());
        }
        return session;
    }

    public void closeSession() {
        Session session = this.threadLocalSession.get();
        if (session == null) {
            return;
        }
        session.close();
        this.threadLocalSession.remove();
        this.sessionCount.decrementAndGet();
    }

    public void close() {
        try {
            this.closeSession();
        } finally {
            if (this.sessionCount.get() == 0 && !this.cluster.isClosed()) {
                this.cluster.close();
            }
        }
        logger.debug("Now(after close()) session count is: {}",
                     this.sessionCount.get());
    }

    public final void checkClusterConneted() {
        E.checkState(this.cluster != null,
                     "Cassandra cluster has not been initialized");
        E.checkState(!this.cluster.isClosed(),
                     "Cassandra cluster has been closed");
    }

    public final void checkSessionConneted() {
        this.checkClusterConneted();

        E.checkState(this.session() != null,
                     "Cassandra session has not been initialized");
        E.checkState(!this.session().closed(),
                     "Cassandra session has been closed");
    }

    /**
     * The Session class is a wrapper of driver Session
     * Expect every thread hold a Session wrapper
     */
    protected final class Session {

        private com.datastax.driver.core.Session session;
        private BatchStatement batch;

        public Session(com.datastax.driver.core.Session session) {
            this.session = session;
            this.batch = new BatchStatement();
        }

        public BatchStatement add(Statement statement) {
            return this.batch.add(statement);
        }

        public void clear() {
            this.batch.clear();
        }

        public ResultSet commit() {
            return this.session.execute(this.batch);
        }

        public ResultSet execute(Statement statement) {
            return this.session.execute(statement);
        }

        public ResultSet execute(String statement) {
            return this.session.execute(statement);
        }

        public ResultSet execute(String statement, Object... args) {
            return this.session.execute(statement, args);
        }

        public boolean closed() {
            return this.session.isClosed();
        }

        private void close() {
            this.session.close();
        }

        public boolean hasChanged() {
            return this.batch.size() > 0;
        }

        public Collection<Statement> statements() {
            return this.batch.getStatements();
        }

        public String keyspace() {
            return CassandraSessionPool.this.keyspace;
        }

        public Metadata metadata() {
            return CassandraSessionPool.this.cluster.getMetadata();
        }
    }
}
