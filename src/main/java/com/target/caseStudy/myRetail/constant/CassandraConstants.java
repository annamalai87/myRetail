package com.target.caseStudy.myRetail.constant;

import io.vertx.cassandra.CassandraClient;

public final class CassandraConstants {
    private CassandraConstants(){}

    public static final String CASSANDRA_HOST = "cassandra.host";
    public static final String CASSANDRA_PORT = "cassandra.port";
    public static final String CASSANDRA_KEYSPACE = "cassandra.keyspace";
}
