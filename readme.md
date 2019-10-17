```Pre-requisite:```

Cassandra host and port can be configured in the resource file config.json.
The application assumes cassandra cluster available with below keyspace and table

```
CREATE KEYSPACE targetcasestudy WITH 
   replication = {'class': 'SimpleStrategy', 'replication_factor': '3'}  
   AND durable_writes = true;
```

```
CREATE TABLE targetcasestudy.product (
    id bigint PRIMARY KEY,
    currency text,
    last_updated timestamp,
    name text,
    price double
)
```

```How application works:```

A) HttpServerVerticle creates HttpServer and exposes below end-points

    1) http://localhost:8443/api/products/{productId} - GET

    2) http://locahost:8443/api/products/{productId} - PUT

        Sample Request Body for PUT

        {
            "price" : 14.99
        }

    3) http://locahost:8443/health - GET (Application heart beat)


B) For GET request, HttpServerVerticle sends product id to ExternalAPIVerticle via vertx Event Bus. At the same time, it also calls
    database service to retrieve product pricing.


C) Both ExternalAPI call and database call happens concurrently for the GET request. For PUT request, External API call will not occur.


D) ExternalAPIVerticle issues RxJava HttpRequest to the External API to retrieve Product. Once the response comes from the API, Verticle
   request handler sends the product info back to the EventBus.
  
   
E) ProductService retrieves the product info from database or updates the product info based on the type of the Http Request.


F) Once both ExternalAPI future and database future are complete, HttpServerVerticle sends the aggregated response back to the client

