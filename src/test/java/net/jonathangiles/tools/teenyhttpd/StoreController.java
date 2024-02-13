package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.winter.*;
import net.jonathangiles.tools.teenyhttpd.winter.annot.*;

import java.util.List;
import java.util.ArrayList;

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

    @Get("/product/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") int id) {

        for (Product product : productList) {
            if (product.getId() == id) {
                return ResponseEntity.ok(product);
            }
        }

        return ResponseEntity.notFound();
    }

    @Put("/product")
    public ResponseEntity<String> createProduct(@RequestBody Product product) {
        productList.add(product);
        return ResponseEntity.ok("Product created");
    }


    @Get("/empty")
    public void emptyController() {
        System.out.println("Hey!");
    }

    @Get("/complex/{name}")
    public ResponseEntity<Pet> complexEndpoint(@PathVariable("name") String name,
                                               @QueryParam("age") int age,
                                               @QueryParam(value = "type", defaultValue = "Dog") String type) {
        return ResponseEntity.ok(new Pet(name, age, type));
    }

}