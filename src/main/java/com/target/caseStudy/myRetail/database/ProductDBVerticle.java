package com.target.caseStudy.myRetail.database;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.TypeCodec;
import io.vertx.cassandra.CassandraClient;
import io.vertx.cassandra.CassandraClientOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ServiceBinder;

import static com.target.caseStudy.myRetail.constant.CassandraConstants.*;
import static com.target.caseStudy.myRetail.constant.VertxConstants.PRODUCT_SERVICE;

/**
 * ProductDBVerticle creates CassandraClient and ProductService for HttpServerHandler to use
 * database retrieval
 */
public class ProductDBVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(ProductDBVerticle.class);

  protected Vertx vertx;
  private CassandraClient cassandraClient;
  private JsonObject config;

  @Override
  public void init(Vertx vertx, Context context) {
    this.vertx = vertx;
    config = context.config();
  }

  @Override
  public void start(Promise<Void> promise) {

    CodecRegistry codecRegistry = new CodecRegistry();
    codecRegistry.register(TypeCodec.timestamp());

    Cluster.Builder clusterBuilder =
        Cluster.builder()
            .addContactPoint(config.getString(CASSANDRA_HOST))
            .withPort(config.getInteger(CASSANDRA_PORT))
            .withCodecRegistry(codecRegistry);

    CassandraClientOptions options =
        new CassandraClientOptions(clusterBuilder)
            .setKeyspace(config.getString(CASSANDRA_KEYSPACE));

    cassandraClient = CassandraClient.createNonShared(vertx, options);

    ProductService.create(
        cassandraClient,
        ready -> {
          if (ready.succeeded()) {
            ServiceBinder binder = new ServiceBinder(vertx);
            binder.setAddress(PRODUCT_SERVICE).register(ProductService.class, ready.result());
            promise.complete();
          } else {
            promise.fail(ready.cause());
          }
        });
  }
}
