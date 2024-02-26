package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Request;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static net.jonathangiles.tools.teenyhttpd.model.StatusCode.CREATED;
import static net.jonathangiles.tools.teenyhttpd.model.TypedResponse.ok;

@SuppressWarnings("unused")
@Path("/store")
public class StoreController {

    private final Map<Integer, Product> productMap;

    public StoreController() {
        productMap = new HashMap<>();
        productMap.put(1, new Product(1, "Apple", 100));
        productMap.put(2, new Product(2, "Banana", 200));
    }

    @Get
    public String sayHello() {
        return "Hello, World!";
    }

    @Get("/products")
    public Collection<Product> get() {
        return productMap.values();
    }

    @Get("/pet")
    public Pet getPet() {
        return new Pet("Bodoque", 21, "Dog");
    }

    @Get("/product/:id")
    public TypedResponse<Product> getProduct(@PathParam("id") int id) {

        Product product = productMap.get(id);

        if (product != null) return ok(product);

        return TypedResponse.notFound();
    }

    @Put("/product")
    public TypedResponse<String> createProduct(@RequestBody Product product) {
        Product replaced = productMap.put(product.getId(), product);

        if (replaced != null) return ok("Product updated");

        return TypedResponse.create(CREATED, "Product created");
    }

    @Delete("/product/:id")
    public TypedResponse<String> deleteProduct(@PathParam("id") int id) {

        Product removed = productMap.remove(id);

        if (removed != null) return ok();

        return TypedResponse.status(StatusCode.NOT_FOUND);
    }

    @Get("/empty")
    private void empty() {
        System.out.println("Hey!");
    }

    @Get("/header")
    public TypedResponse<String> header(@RequestHeader("Authorization") String auth) {
        return ok(auth);
    }

    @Get("/header2")
    public String header2(@RequestHeader("Authorization") Header header) {
        return header.getValues().get(0);
    }

    @Get("/request")
    public String request(Request request) {
        return request.getClass().getSimpleName();
    }

    @Get("/complex/:name")
    public TypedResponse<Pet> complexEndpoint(@PathParam("name") String name,
                                              @RequestHeader("Authorization") String auth,
                                              @QueryParam(value = "age", defaultValue = "21") int age,
                                              @QueryParam(value = "type", defaultValue = "Dog") String type) {

        return ok(new Pet(name, age, type))
                .setHeader("Authorization", auth);
    }

    @Get("/requiredQueryParam")
    public String requiredQueryParam(@QueryParam(value = "name", required = true) String name) {
        return name;
    }

    @Get(value = "/contentType", produces = "application/x-protobuf")
    public String contentType() {
        return "test";
    }

    @Get("/statusCode")
    public StatusCode statusCode() {
        return StatusCode.FORBIDDEN;
    }

    @Get("/statusCode2")
    public TypedResponse<String> statusCode2() {
        return StatusCode.FORBIDDEN.asTypedResponse();
    }

    @Post("/requestBody")
    public String requestBody(@RequestBody String body) {
        return body + " Handled!";
    }

}