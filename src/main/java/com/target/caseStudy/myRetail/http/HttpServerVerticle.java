package com.target.caseStudy.myRetail.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.target.caseStudy.myRetail.database.ProductService;
import com.target.caseStudy.myRetail.constant.DBAction;
import com.target.caseStudy.myRetail.model.Product;
import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.time.Duration;
import java.time.Instant;

import static com.target.caseStudy.myRetail.constant.HttpConstants.*;
import static com.target.caseStudy.myRetail.constant.VertxConstants.*;

/**
 * HttpServerVerticle creates HttpServer and exposes below endpoints.
 *
 * <p>$Host/api/products/:productId - GET to retrieve aggregated product information from <br>
 * External API and from Cassandra database.
 *
 * <p>$Host/api/products/:productId - PUT to update product info in Cassandra database
 */
public final class HttpServerVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

  private HttpServer httpServer;
  private ProductService productService;
  private JsonObject config;
  protected Vertx vertx;

  @Override
  public void init(Vertx vertx, Context context) {
    this.vertx = vertx;
    config = context.config();
  }

  @Override
  public void start(Promise<Void> startPromise) {

    httpServer = vertx.createHttpServer();

    productService = ProductService.createProxy(vertx, PRODUCT_SERVICE);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    // Route for get end point
    router
        .route(HttpMethod.GET, API_GET_PRODUCT_BY_ID)
        .produces(APPLICATION_JSON)
        .handler(this::getProductHandler);

    // Route for update end point
    router
        .route(HttpMethod.PUT, API_UPDATE_PRODUCT)
        .produces(APPLICATION_JSON)
        .handler(this::updateProductHandler);

    // Route for Application Health heart beat end point.
    router
        .route(HttpMethod.GET, API_APPLICATION_HEALTH)
        .produces(TEXT_PLAIN)
        .handler(this::applicationHealthHandler);

    // Listen to Http Port
    httpServer
        .requestHandler(router)
        .listen(
            config.getInteger(HTTP_PORT),
            result -> {
              if (result.succeeded()) {
                logger.info("HttpServerVerticle instance deployed");
                startPromise.complete();
              } else {
                logger.error("HttpServerVerticle instance deployment failed " + result.cause());
                startPromise.fail(
                    "HttpServerVerticle instance deployment failed " + result.cause().getMessage());
              }
            });
  }

  /**
   * Handler for get end point
   *
   * @param routingContext Represents the context for the handling of a request in Vert.x-Web.
   */
  private void getProductHandler(RoutingContext routingContext) {

    HttpServerResponse httpServerResponse = routingContext.response();

    try {

      Long productId = Long.parseLong(routingContext.request().getParam("productId"));
      Instant start = Instant.now();

      logger.info("Received the get request for the productid " + productId + " at " + start);
      Product inputProduct = new Product.ProductBuilder(productId).build();

      // Create future for external api call
      Future<Product> externalApiPromise =
          Future.future(promise -> messagingExternalApiVerticle(promise, productId));

      // Create future for DB call
      Future<Product> productDBPromise =
          Future.future(promise -> callDBService(promise, inputProduct, DBAction.GET));

      // Concatenate result from external api and database future
      CompositeFuture.all(externalApiPromise, productDBPromise)
          .setHandler(
              result -> {
                if (result.succeeded()) {
                  Product productInfo = externalApiPromise.result();
                  Product productDBInfo = productDBPromise.result();

                  productInfo.setPrice(productDBInfo.getPrice());
                  productInfo.setCurrency(productDBInfo.getCurrency());
                  productInfo.setLast_updated(productDBInfo.getLast_updated());

                  logger.info("The retrieved product is " + productInfo.toString());

                  Gson gson = new GsonBuilder().create();
                  String json = gson.toJson(productInfo);

                  Long duration = Duration.between(start, Instant.now()).toMillis();
                  logger.info(
                      "Total time taken to process the request " + duration + " milli-seconds");

                  httpServerResponse.setStatusCode(SUCCESS_CODE);
                  httpServerResponse.end(new JsonObject(json).encodePrettily());

                } else {
                  logger.error(result.cause());
                  httpServerResponse.setStatusCode(INTERNAL_SERVER_ERROR);
                  httpServerResponse.end(
                      "Error due to "
                          + result.cause().getMessage()
                          + ". Please try again after sometime.");
                }
              });

    } catch (Exception e) {
      logger.error(e);
      httpServerResponse.setStatusCode(INTERNAL_SERVER_ERROR);
      httpServerResponse.end(
          "Error due to " + e.getMessage() + ". Please try again after sometime.");
    }
  }

  /**
   * Handler for update end point
   *
   * @param routingContext Represents the context for the handling of a request in Vert.x-Web.
   */
  private void updateProductHandler(RoutingContext routingContext) {

    HttpServerResponse httpServerResponse = routingContext.response();

    try {
      Long productId = Long.parseLong(routingContext.request().getParam("productId"));
      Instant start = Instant.now();

      logger.info("Received the update request for the productid " + productId + " at " + start);

      JsonObject productJson = routingContext.getBodyAsJson();
      Product inputProduct = new Product(productJson);
      inputProduct.setId(productId);

      logger.info("The input product " + inputProduct.toString());

      // Create future for DB call
      Future<Product> productDBPromise =
          Future.future(promise -> callDBService(promise, inputProduct, DBAction.UPDATE));

      productDBPromise.setHandler(
          dbResponse -> {
            if (dbResponse.succeeded()) {

              Product updatedProduct = dbResponse.result();
              logger.info("The updated product is " + updatedProduct.toString());
              Gson gson = new GsonBuilder().create();
              String json = gson.toJson(updatedProduct);

              Long duration = Duration.between(start, Instant.now()).toMillis();
              logger.info("Total time taken to process the request " + duration + " milli-seconds");

              httpServerResponse.setStatusCode(SUCCESS_CODE);
              httpServerResponse.end(new JsonObject(json).encodePrettily());
            } else {
              logger.error(dbResponse.cause());
              httpServerResponse.setStatusCode(INTERNAL_SERVER_ERROR);
              httpServerResponse.end(
                  "Error due to "
                      + dbResponse.cause().getMessage()
                      + ". Please try again after sometime.");
            }
          });

    } catch (Exception e) {

      logger.error(e);
      httpServerResponse.setStatusCode(INTERNAL_SERVER_ERROR);
      httpServerResponse.end(
          "Error due to " + e.getMessage() + ". Please try again after sometime.");
    }
  }

  /**
   * Handler for health end point
   *
   * @param routingContext Represents the context for the handling of a request in Vert.x-Web.
   */
  private void applicationHealthHandler(RoutingContext routingContext) {
    HttpServerResponse httpServerResponse = routingContext.response();
    httpServerResponse.setStatusCode(SUCCESS_CODE);
    httpServerResponse.end("myRetailApplication is up and running");
  }

  /**
   * Get product info from ExternalApiVerticle for the given productId. It uses EventBus for
   * communication.
   *
   * @param promise Promise from ExternalApi Verticle
   * @param productId Product Identifier
   */
  private void messagingExternalApiVerticle(Promise<Product> promise, Long productId) {

    vertx
        .eventBus()
        .request(
            PRODUCT_INFO_VERTICLE,
            productId,
            response -> {
              if (response.succeeded()) {
                Product product = (Product) response.result().body();
                promise.complete(product);
              } else {
                promise.fail(response.cause());
              }
            });
  }

  /**
   * Calls ProductService to retrieve/update the product from/in the database.
   *
   * @param promise Promise from ProductService
   * @param product Product
   * @param dbAction DBAction to denote get or update
   */
  private void callDBService(Promise<Product> promise, Product product, DBAction dbAction) {

    Instant start = Instant.now();
    logger.info("Calling ProductDBService for the productid " + product.getId() + " at " + start);

    if (dbAction == DBAction.GET) {

      productService.getProduct(
          product.getId(),
          handler -> {
            if (handler.succeeded()) {

              logger.info("The response from the service " + handler.result().toString());

              Long duration = Duration.between(start, Instant.now()).toMillis();
              logger.info(
                  "Total time taken to process the request by ProductDBVerticle "
                      + duration
                      + " milli-seconds");
              promise.complete(handler.result());
            } else {
              handler.cause().printStackTrace();
              promise.fail(handler.cause());
            }
          });

    } else if (dbAction == DBAction.UPDATE) {

      productService.updateProduct(
          product,
          handler -> {
            if (handler.succeeded()) {

              logger.info("The response from the service " + handler.result().toString());

              Long duration = Duration.between(start, Instant.now()).toMillis();
              logger.info(
                  "Total time taken to process the request by ProductDBVerticle "
                      + duration
                      + " milli-seconds");
              promise.complete(handler.result());
            } else {
              handler.cause().printStackTrace();
              promise.fail(handler.cause());
            }
          });
    }
  }
}
