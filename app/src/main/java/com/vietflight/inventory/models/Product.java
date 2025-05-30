package com.vietflight.inventory.models;

public class Product {
    private String id;
    private String code;
    private String name;
    private String description;
    private String categoryId;
    private String categoryName;
    private String unit;
    private double price;
    private String imageUrl;
    private boolean isActive;
    private int displayOrder;
    private long createdAt;
    private long updatedAt;

    // Temporary field for handover quantity (not stored in database)
    private int quantity = 0;

    public Product() {}

    public Product(String code, String name, String categoryId, String unit, double price) {
        this.code = code;
        this.name = name;
        this.categoryId = categoryId;
        this.unit = unit;
        this.price = price;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();


        // Set default image URL
        this.imageUrl = getDefaultImageUrl(code);
    }

    // Constructor with image URL
    public Product(String code, String name, String categoryId, String unit, double price, String imageUrl) {
        this(code, name, categoryId, unit, price);
        this.imageUrl = imageUrl;
    }

    private String getDefaultImageUrl(String code) {
        // You can use URLs from internet or local drawable resources
        switch (code) {
            // Hot Meals
            case "HM001": return "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300"; // Chicken Teriyaki
            case "HM002": return "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=300"; // Beef Rice
            case "HM003": return "https://images.unsplash.com/photo-1551892374-ecf8754cf8b0?w=300"; // Pasta
            case "HM004": return "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=300"; // Fried Rice

            // F&B
            case "FB001": return "https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=300"; // Water
            case "FB002": return "https://images.unsplash.com/photo-1629203851122-3726ecdf080e?w=300"; // Coca Cola
            case "FB003": return "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=300"; // Coffee
            case "FB004": return "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=300"; // Tea
            case "FB005": return "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=300"; // Cookies

            // Souvenirs
            case "SV001": return "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=300"; // Keychain
            case "SV002": return "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=300"; // T-shirt
            case "SV003": return "https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=300"; // Cap

            // Business
            case "BS001": return "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300"; // Business Meal
            case "BS002": return "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=300"; // Wine

            // Other
            case "OT001": return "https://images.unsplash.com/photo-1584464491033-06628f3a6b7b?w=300"; // Wet Tissue
            case "OT002": return "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=300"; // Paper Bag

            default: return "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300";
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Quantity for handover (temporary field)
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); }
}