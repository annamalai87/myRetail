package com.target.caseStudy.myRetail;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
  public void testMyApplication(TestContext context) throws InterruptedException {
    final Async async = context.async();

    WebClient.create(vertx)
        .get(8443, "localhost", "/health")
        .send(
            response -> {
              context.assertTrue(
                  response
                      .result()
                      .body()
                      .toString()
                      .equals("myRetailApplication is up and running"));
              async.complete();
            });
    Thread.sleep(25000);
  }
}
