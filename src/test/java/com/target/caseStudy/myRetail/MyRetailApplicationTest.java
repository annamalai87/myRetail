package com.target.caseStudy.myRetail;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MyRetailApplicationTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    MyRetailApplication.main(new String[] {});
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
    MyRetailApplication.shutdown();
  }

  @Test
  public void testMyApplication(TestContext context) {
    final Async async = context.async();

    vertx
        .createHttpClient()
        .getNow(
            8443,
            "localhost",
            "/api/products/13860428",
            response ->
                response.handler(
                    body -> {
                      JsonObject jsonObject = body.toJsonObject();
                      context.assertTrue(
                          jsonObject.getString("name").equals("The Big Lebowski (Blu-ray)"));
                      async.complete();
                    }));
  }
}
