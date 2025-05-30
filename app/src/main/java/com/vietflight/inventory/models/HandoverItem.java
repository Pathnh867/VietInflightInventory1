package com.vietflight.inventory.models;

public class HandoverItem {
    private String id;
    private String handoverId;
    private String productId;
    private String productName;
    private String productCategory;
    private String productUnit;
    private double productPrice;
    private int initialQuantity;     // Số lượng ban đầu
    private int soldQuantity;        // Số lượng đã bán
    private int canceledQuantity;    // Số lượng hủy
    private int remainingQuantity;   // Số lượng còn lại
    private String notes;
    private long updatedAt;

    public HandoverItem() {}

    public HandoverItem(String handoverId, String productId, String productName,
                        String productCategory, String productUnit, double productPrice,
                        int initialQuantity) {
        this.handoverId = handoverId;
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.productUnit = productUnit;
        this.productPrice = productPrice;
        this.initialQuantity = initialQuantity;
        this.soldQuantity = 0;
        this.canceledQuantity = 0;
        this.remainingQuantity = initialQuantity;
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHandoverId() { return handoverId; }
    public void setHandoverId(String handoverId) { this.handoverId = handoverId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getProductUnit() { return productUnit; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public int getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(int initialQuantity) { this.initialQuantity = initialQuantity; }

    public int getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(int soldQuantity) { this.soldQuantity = soldQuantity; }

    public int getCanceledQuantity() { return canceledQuantity; }
    public void setCanceledQuantity(int canceledQuantity) { this.canceledQuantity = canceledQuantity; }

    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public void calculateRemaining() {
        this.remainingQuantity = this.initialQuantity - this.soldQuantity - this.canceledQuantity;
    }

    public int getTotalUsed() {
        return this.soldQuantity + this.canceledQuantity;
    }
}