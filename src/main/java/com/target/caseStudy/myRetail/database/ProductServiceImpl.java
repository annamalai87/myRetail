package com.target.caseStudy.myRetail.database;

import com.target.caseStudy.myRetail.model.Product;
import io.vertx.cassandra.CassandraClient;
import io.vertx.cassandra.Mapper;
import io.vertx.cassandra.MappingManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;

/** ProductService Implementation for Get Product and Update Product in Cassandra */
public class ProductServiceImpl implements ProductService {

  private CassandraClient cassandraClient;
  private MappingManager mappingManager;
  private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

  public ProductServiceImpl(
      CassandraClient cassandraClient, Handler<AsyncResult<ProductService>> resultHandler) {
    this.cassandraClient = cassandraClient;
    mappingManager = MappingManager.create(cassandraClient);
    resultHandler.handle(Future.succeededFuture(this));
  }

  @Override
  public ProductService getProduct(long productId, Handler<AsyncResult<Product>> handler) {

    Mapper<Product> mapper = mappingManager.mapper(Product.class);
    mapper.get(Collections.singletonList(productId), handler);

    return this;
  }

  @Override
  public ProductService updateProduct(
      Product inputProduct, Handler<AsyncResult<Product>> resultHandler) {

    Mapper<Product> mapper = mappingManager.mapper(Product.class);
    mapper.get(
        Collections.singletonList(inputProduct.getId()),
        getHandler -> {
          if (getHandler.succeeded()) {

            Product updatedProduct = getHandler.result();
            updatedProduct.setPrice(inputProduct.getPrice());
            updatedProduct.setLast_updated(Date.from(Instant.now()));

            logger.info("The product to be updated " + updatedProduct);

            mapper.save(
                updatedProduct,
                updateHandler -> {
                  if (updateHandler.succeeded()) {
                    resultHandler.handle(Future.succeededFuture(updatedProduct));
                  } else {
                    resultHandler.handle(Future.failedFuture(updateHandler.cause()));
                  }
                });

          } else {
            resultHandler.handle(Future.failedFuture(getHandler.cause()));
          }
        });
    return this;
  }
}
