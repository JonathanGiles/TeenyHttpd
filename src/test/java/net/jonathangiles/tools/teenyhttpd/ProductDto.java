package net.jonathangiles.tools.teenyhttpd;

public class ProductDto {

    private int id;
    private String name;
    private int price;

    public ProductDto() {
    }

    public ProductDto(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public ProductDto setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ProductDto setName(String name) {
        this.name = name;
        return this;
    }

    public int getPrice() {
        return price;
    }

    public ProductDto setPrice(int price) {
        this.price = price;
        return this;
    }
}