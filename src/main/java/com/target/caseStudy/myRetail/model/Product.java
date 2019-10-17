package com.target.caseStudy.myRetail.model;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import java.util.Date;

@DataObject(generateConverter = true)
@Table(name = "product", keyspace = "targetcasestudy")
public class Product {

  @PartitionKey private long id;

  @Column(name = "name")
  private String name;

  @Column(name = "price")
  private double price;

  @Column(name = "currency")
  private String currency;

  @Column(name = "last_updated")
  private Date last_updated;

  private Product() {}

  public Product(JsonObject jsonObject) {
    // Requires maven compile to auto generate the class ProductConverter.
    ProductConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Date getLast_updated() {
    return last_updated;
  }

  public void setLast_updated(Date last_updated) {
    this.last_updated = last_updated;
  }

  @Override
  public String toString() {
    return new org.apache.commons.lang3.builder.ToStringBuilder(this)
        .append("id", id)
        .append("name", name)
        .append("price", price)
        .append("currency", currency)
        .append("last_updated", last_updated)
        .toString();
  }

  public static class ProductBuilder {

    private long id;
    private String name;
    private double price;
    private String currency;
    private Date last_updated;

    public ProductBuilder(long id) {
      this.id = id;
    }

    public ProductBuilder name(String name) {
      this.name = name;
      return this;
    }

    public ProductBuilder price(double price) {
      this.price = price;
      return this;
    }

    public ProductBuilder currency(String currency) {
      this.currency = currency;
      return this;
    }

    public ProductBuilder last_updated(Date last_updated) {
      this.last_updated = last_updated;
      return this;
    }

    public Product build() {
      Product product = new Product();
      product.id = this.id;
      product.name = this.name;
      product.currency = this.currency;
      product.price = this.price;
      product.last_updated = this.last_updated;

      return product;
    }
  }
}
