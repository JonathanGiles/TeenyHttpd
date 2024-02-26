package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.json.JsonIncludeNonNull;

import java.util.Map;

@JsonIncludeNonNull
public class ProductDto {

    public int id;
    public String name;
    public int price;
    public Map<String, Object> perks;

    public ProductDto() {
    }

    public ProductDto(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Map<String, Object> getPerks() {
        return perks;
    }

    public ProductDto put(String key, Object value) {

        if (perks == null) {
            perks = new java.util.HashMap<>();
        }

        perks.put(key, value);

        return this;
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

    @Override
    public String toString() {
        return "ProductDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", perks=" + perks +
                '}';
    }
}