package com.target.caseStudy.myRetail.util;

import com.target.caseStudy.myRetail.model.Product;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

/**
 * ProductCodec enables verticles to send Product objects over the event bus for interverticle communication.
 */
public class ProductCodec implements MessageCodec<Product, Product> {
  @Override
  public void encodeToWire(Buffer buffer, Product product) {
    Json.encodeToBuffer(product);
  }

  @Override
  public Product decodeFromWire(int pos, Buffer buffer) {
    return Json.decodeValue(buffer, Product.class);
  }

  @Override
  public Product transform(Product product) {
    return product;
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
