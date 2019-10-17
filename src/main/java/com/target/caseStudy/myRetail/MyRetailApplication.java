package com.target.caseStudy.myRetail;

import com.target.caseStudy.myRetail.model.Product;
import com.target.caseStudy.myRetail.util.ProductCodec;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * MyRetailApplication is a reactive microservice built using vertx. This class deploys <br>
 * below three verticles. The number of instances of each of those verticles are configurable <br>
 * as per the need.
 *
 * <p>1) HttpServerVerticle - Creates HttpServer and exposes product api endpoint.
 *
 * <p>2) ExternalApiVerticle - Communicates to external api to retrieve product name.
 *
 * <p>3) ProductDBVerticle - Creates CassandraClient and Product Service.
 */
public final class MyRetailApplication {

  private static final Logger logger = LoggerFactory.getLogger(MyRetailApplication.class);
  private static Vertx vertx;

  public static void main(String[] args) {

    vertx = Vertx.vertx();

    JsonObject config =
        vertx
            .fileSystem()
            .readFileBlocking(System.getProperty("user.dir") + "/src/main/resources/config.json")
            .toJsonObject();

    // Registers ProductCodec to enable event bus communication between verticles.
    vertx.eventBus().registerDefaultCodec(Product.class, new ProductCodec());

    // Deployment options as per config.json.
    DeploymentOptions options =
        new DeploymentOptions()
            .setInstances(config.getInteger("verticle.instances"))
            .setConfig(config);

    vertx.deployVerticle(
        "com.target.caseStudy.myRetail.database.ProductDBVerticle",
        options,
        result -> {
          if (result.succeeded()) {
            logger.info("All ProductDBVerticle instances are deployed");
          } else {
            logger.error(
                "Deployments of some/all of the ProductDBVerticle instances failed "
                    + result.cause());
            throw new RuntimeException();
          }
        });

    vertx.deployVerticle(
        "com.target.caseStudy.myRetail.http.HttpServerVerticle",
        options,
        result -> {
          if (result.succeeded()) {
            logger.info("All HttpServerVerticle instances are deployed");
          } else {
            logger.error(
                "Deployments of some/all of the HttpServer instances failed " + result.cause());
            throw new RuntimeException();
          }
        });

    vertx.deployVerticle(
        "com.target.caseStudy.myRetail.http.ExternalApiVerticle",
        options,
        result -> {
          if (result.succeeded()) {
            logger.info("All ExternalApiVerticle instances are deployed");
          } else {
            logger.error(
                "Deployments of some/all of the ExternalApiVerticle instances failed "
                    + result.cause());
            throw new RuntimeException();
          }
        });
  }

  public static void shutdown() {
    vertx.close(
        handler -> {
          if (handler.succeeded()) {
            logger.info("Shutdown successful ");
          } else {
            logger.error("Failed to shutdown");
            throw new RuntimeException();
          }
        });
  }
}
