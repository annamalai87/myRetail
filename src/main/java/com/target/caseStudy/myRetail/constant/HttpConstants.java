package com.target.caseStudy.myRetail.constant;

public final class HttpConstants {

  private HttpConstants() {}

  public static final String API_GET_PRODUCT_BY_ID = "/api/products/:productId";
  public static final String API_UPDATE_PRODUCT = "/api/products/:productId";
  public static final String API_APPLICATION_HEALTH = "/health";
  public static final String EXTERNAL_API_HOST = "redsky.target.com";
  public static final String EXTERNAL_API_URI = "/v2/pdp/tcin/";
  public static final String EXTERNAL_API_QUERY_PARAM =
      "taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,"
          + "rating_and_review_statistics,question_answer_statistics";
  public static final String HTTP_PORT = "http.port";
  public static final Integer INTERNAL_SERVER_ERROR = 500;
  public static final Integer SUCCESS_CODE = 200;
}
