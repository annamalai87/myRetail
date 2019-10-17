package com.target.caseStudy.myRetail.database;

import com.target.caseStudy.myRetail.model.Product;
import io.vertx.cassandra.CassandraClient;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface ProductService {

  @GenIgnore
  static ProductService create(
      CassandraClient cassandraClient, Handler<AsyncResult<ProductService>> resultHandler) {
    return new ProductServiceImpl(cassandraClient, resultHandler);
  }

  @GenIgnore
  static ProductService createProxy(Vertx vertx, String address) {
    // Requires maven compile to generate the class ProductServiceVertxEBProxy
    return new ProductServiceVertxEBProxy(vertx, address);
  }

  @Fluent
  ProductService getProduct(long productId, Handler<AsyncResult<Product>> handler);

  @Fluent
  ProductService updateProduct(Product product, Handler<AsyncResult<Product>> handler);
}
