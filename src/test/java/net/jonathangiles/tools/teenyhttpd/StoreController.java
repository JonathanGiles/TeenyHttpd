package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Request;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("unused")
@Path("/store")
public class StoreController {

    private final List<Product> productList;

    public StoreController() {
        productList = new ArrayList<>();
        productList.add(new Product(1, "Apple", 100));
        productList.add(new Product(2, "Banana", 200));
    }

    @Get
    public String sayHello() {
        return "Hello, World!";
    }

    @Get("/products")
    public List<Product> get() {
        return productList;
    }

    @Get("/pet")
    public Pet getPet() {
        return new Pet("Bodoque", 21, "Dog");
    }

    @Get("/product/:id")
    public TypedResponse<Product> getProduct(@PathParam("id") int id) {

        for (Product product : productList) {
            if (product.getId() == id) {
                return TypedResponse.ok(product);
            }
        }

        return TypedResponse.notFound();
    }

    @Put("/product")
    public TypedResponse<String> createProduct(@RequestBody Product product) {
        productList.add(product);
        return TypedResponse.ok("Product created");
    }

    @Get("/empty")
    private void empty() {
        System.out.println("Hey!");
    }

    @Get("/header")
    public TypedResponse<String> header(@RequestHeader("Authorization") String auth) {
        return TypedResponse.ok(auth);
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

        return TypedResponse.ok(new Pet(name, age, type))
                .header("Authorization", auth);
    }

    @Get("/requiredQueryParam")
    public String requiredQueryParam(@QueryParam(value = "name", required = true) String name) {
        return name;
    }

    @Get(value = "/contentType", produces = "application/x-protobuf")
    public String contentType() {
        return "test";
    }

}