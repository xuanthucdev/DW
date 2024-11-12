package org.example;

import java.math.BigDecimal;
import java.sql.Timestamp;


public class Product {
    private int productSk;
    private String productId;
    private String name;
    private BigDecimal price;
    private BigDecimal priceSale;
    private String brand;
    private String color;
    private String size;
    private String status;



    private String descriptionPart1;
    private String descriptionPart2;
    private String descriptionPart3;
    private Timestamp createdAt;
    private Timestamp lastModified;
    public Product(Timestamp lastModified, Timestamp createdAt, String descriptionPart3, String descriptionPart2, String descriptionPart1, String status, String brand, String color, String size, BigDecimal priceSale, BigDecimal price, String name, String productId, int productSk) {
        this.lastModified = lastModified;
        this.createdAt = createdAt;
        this.descriptionPart3 = descriptionPart3;
        this.descriptionPart2 = descriptionPart2;
        this.descriptionPart1 = descriptionPart1;
        this.status = status;
        this.brand = brand;
        this.color = color;
        this.size = size;
        this.priceSale = priceSale;
        this.price = price;
        this.name = name;
        this.productId = productId;
        this.productSk = productSk;
    }
    public Product() {

    }
    public int getProductSk() {
        return productSk;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPriceSale() {
        return priceSale;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getBrand() {
        return brand;
    }

    public String getColor() {
        return color;
    }

    public String getSize() {
        return size;
    }

    public String getStatus() {
        return status;
    }

    public String getDescriptionPart1() {
        return descriptionPart1;
    }

    public String getDescriptionPart2() {
        return descriptionPart2;
    }

    public void setProductSk(int productSk) {
        this.productSk = productSk;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setPriceSale(BigDecimal priceSale) {
        this.priceSale = priceSale;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDescriptionPart1(String descriptionPart1) {
        this.descriptionPart1 = descriptionPart1;
    }

    public void setDescriptionPart2(String descriptionPart2) {
        this.descriptionPart2 = descriptionPart2;
    }

    public void setDescriptionPart3(String descriptionPart3) {
        this.descriptionPart3 = descriptionPart3;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    public String getDescriptionPart3() {
        return descriptionPart3;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public static boolean isDifferent(Product oldProduct, Product newProduct) {
        return !oldProduct.getPrice().equals(newProduct.getPrice()) ||
                !oldProduct.getPriceSale().equals(newProduct.getPriceSale()) ||
                !oldProduct.getName().equals(newProduct.getName()) ||
                !oldProduct.getColor().equals(newProduct.getColor()) ||
                !oldProduct.getSize().equals(newProduct.getSize()) ||
                !oldProduct.getStatus().equals(newProduct.getStatus()) ||
                !oldProduct.getDescriptionPart1().equals(newProduct.getDescriptionPart1()) ||
                !oldProduct.getDescriptionPart2().equals(newProduct.getDescriptionPart2()) ||
                !oldProduct.getDescriptionPart3().equals(newProduct.getDescriptionPart3());
    }
}
