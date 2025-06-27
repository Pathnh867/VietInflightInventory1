package com.vietflight.inventory.models;

public class Product {
    private String id;
    private String code;
    private String name;
    private String category;
    private String unit;
    private double price;
    private String imageName;
    private String imageUrl;
    private boolean isActive;
    private int quantity = 0; // dùng khi tạo bàn giao

    public Product() {}

    public Product(String id, String code, String name, String category, String unit, double price, String imageName, String imageUrl) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.price = price;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.isActive = true;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); }
}
