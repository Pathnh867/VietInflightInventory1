package com.vietflight.inventory.utils;

public class Constants {
    // User Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_STAFF = "staff";
    public static final String ROLE_ATTENDANT = "attendant";

    // Handover Status
    public static final String HANDOVER_STATUS_PENDING = "pending";
    public static final String HANDOVER_STATUS_RECEIVED = "received";
    public static final String HANDOVER_STATUS_RETURNED = "returned";
    public static final String HANDOVER_STATUS_LOCKED = "locked";

    // Handover Types
    public static final String HANDOVER_TYPE_COM = "com";
    public static final String HANDOVER_TYPE_TOPUP = "topup";

    // Flight Types
    public static final String FLIGHT_TYPE_DOMESTIC = "domestic";
    public static final String FLIGHT_TYPE_INTERNATIONAL = "international";

    // Product Categories
    public static final String CATEGORY_HOT_MEAL = "hot_meal";
    public static final String CATEGORY_FOOD_BEVERAGE = "food_beverage";
    public static final String CATEGORY_SOUVENIR = "souvenir";

    // Firebase Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_FLIGHTS = "flights";
    public static final String COLLECTION_AIRCRAFT = "aircraft";
    public static final String COLLECTION_PRODUCTS = "products";
    public static final String COLLECTION_CATEGORIES = "product_categories";
    public static final String COLLECTION_HANDOVERS = "handovers";
    public static final String COLLECTION_HANDOVER_ITEMS = "handover_items";

    // SharedPreferences Keys
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
}