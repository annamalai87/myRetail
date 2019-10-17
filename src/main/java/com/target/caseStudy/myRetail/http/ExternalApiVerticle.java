package com.target.caseStudy.myRetail.http;

import com.target.caseStudy.myRetail.model.Product;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.ext.web.client.HttpRequest;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;

import java.time.Duration;
import java.time.Instant;

import static com.target.caseStudy.myRetail.constant.HttpConstants.*;
import static com.target.caseStudy.myRetail.constant.VertxConstants.PRODUCT_INFO_VERTICLE;

/** Retrives Product Information from an external api for the given productId */
public class ExternalApiVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(ExternalApiVerticle.class);

  private WebClient webClient;
  protected io.vertx.rxjava.core.Vertx vertx;

  @Override
  public void init(Vertx vertx, Context context) {

    this.vertx = new io.vertx.rxjava.core.Vertx(vertx);
    this.webClient = WebClient.create(this.vertx);
  }

  @Override
  public void start(Promise<Void> promise) {

    vertx
        .eventBus()
        .consumer(PRODUCT_INFO_VERTICLE, this::externalApiProcessHandler)
        .completionHandler(
            result -> {
              if (result.succeeded()) {
                logger.info("ExternalApiVerticle instance deployed");
                promise.complete();
              } else {
                logger.error("Failed to deploy ExternalApiVerticle instance " + result.cause());
                promise.fail(
                    "Failed to deploy ExternalApiVerticle instance " + result.cause().getMessage());
              }
            });
  }

  /**
   * Sends HttpRequest to an external Api end point using RxJava webclient and subscribes to its
   * response
   *
   * @param productIdMessage Message contains ProductId
   */
  private void externalApiProcessHandler(Message<Long> productIdMessage) {

    Instant start = Instant.now();
    long productId = productIdMessage.body();

    logger.info("Calling external API for the productId " + productId + " at " + start);

    HttpRequest<Buffer> httpRequest =
        webClient.get(EXTERNAL_API_HOST, EXTERNAL_API_URI + productId);
    httpRequest.addQueryParam("excludes", EXTERNAL_API_QUERY_PARAM);

    httpRequest
        .rxSend()
        .subscribe(
            successHandler ->
                this.apiCallSuccessfulAttempt(productIdMessage, productId, successHandler, start),
            failureHandler -> {
              logger.error(failureHandler.getCause());
              productIdMessage.fail(INTERNAL_SERVER_ERROR, failureHandler.getCause().getMessage());
            });
  }

  /**
   * Handler for successful response from the external api HttpRequest.
   *
   * @param productIdMessage
   * @param productId
   * @param httpResponse
   * @param start
   */
  private void apiCallSuccessfulAttempt(
      Message<Long> productIdMessage, long productId, HttpResponse httpResponse, Instant start) {

    if (httpResponse.statusCode() == SUCCESS_CODE) {
      JsonObject jsonObject = httpResponse.bodyAsJsonObject();
      String productName =
          jsonObject
              .getJsonObject("product")
              .getJsonObject("item")
              .getJsonObject("product_description")
              .getString("title");

      logger.info("The retrieved product name from external api is " + productName);

      long duration = Duration.between(start, Instant.now()).toMillis();

      logger.info(
          "Total time taken to retrieve Product info from external api is "
              + duration
              + " milli-seconds");

      productIdMessage.reply(new Product.ProductBuilder(productId).name(productName).build());
    } else {
      logger.error("External API call failed due to " + httpResponse.statusMessage());
      productIdMessage.fail(httpResponse.statusCode(), httpResponse.statusMessage());
    }
  }
}
