/*
 * Inventory Management System - Interview Version
 * 
 * Core Features:
 * - Manage products with SKU, name, price
 * - Track stock levels across warehouses
 * - Purchase and sell operations
 * - Low stock alerts
 */

/*
 * Key Components:
 * 1) Product -> represents items in inventory
 * 2) Warehouse -> storage location with stock
 * 3) InventoryManager -> manages operations (Singleton)
 * 4) Observer Pattern -> for low stock alerts
 */


// ============= PRODUCT CLASS =============

public class Product {
    private String productId;
    private String name;
    private double price;
    private int reorderPoint;  // alert when stock below this

    public Product(String productId, String name, double price, int reorderPoint) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.reorderPoint = reorderPoint;
    }

    // getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getReorderPoint() { return reorderPoint; }
}


// ============= WAREHOUSE CLASS =============

public class Warehouse {
    private String warehouseId;
    private String location;
    private Map<String, Integer> inventory;  // productId -> quantity

    public Warehouse(String warehouseId, String location) {
        this.warehouseId = warehouseId;
        this.location = location;
        this.inventory = new HashMap<>();
    }

    public String getWarehouseId() { return warehouseId; }

    public int getStock(String productId) {
        return inventory.getOrDefault(productId, 0);
    }

    public boolean addStock(String productId, int quantity) {
        inventory.put(productId, getStock(productId) + quantity);
        return true;
    }

    public boolean removeStock(String productId, int quantity) {
        if (getStock(productId) < quantity) {
            return false;  // not enough stock
        }
        inventory.put(productId, getStock(productId) - quantity);
        return true;
    }
}


// ============= OBSERVER PATTERN FOR ALERTS =============

public interface StockObserver {
    void onLowStock(String productId, String warehouseId, int currentStock);
}

public class EmailAlert implements StockObserver {
    @Override
    public void onLowStock(String productId, String warehouseId, int currentStock) {
        System.out.println("EMAIL ALERT: Product " + productId + 
                          " is low in warehouse " + warehouseId + 
                          ". Current stock: " + currentStock);
    }
}


// ============= INVENTORY MANAGER (SINGLETON) =============

public class InventoryManager {
    private static InventoryManager instance;
    private Map<String, Product> products;
    private Map<String, Warehouse> warehouses;
    private List<StockObserver> observers;

    private InventoryManager() {
        products = new HashMap<>();
        warehouses = new HashMap<>();
        observers = new ArrayList<>();
    }

    public static InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    // ========== PRODUCT OPERATIONS ==========

    public void addProduct(Product product) {
        products.put(product.getProductId(), product);
    }

    public Product getProduct(String productId) {
        return products.get(productId);
    }

    // ========== WAREHOUSE OPERATIONS ==========

    public void addWarehouse(Warehouse warehouse) {
        warehouses.put(warehouse.getWarehouseId(), warehouse);
    }

    public Warehouse getWarehouse(String warehouseId) {
        return warehouses.get(warehouseId);
    }

    // ========== STOCK OPERATIONS ==========

    public boolean purchaseStock(String productId, String warehouseId, int quantity) {
        Warehouse warehouse = warehouses.get(warehouseId);
        if (warehouse == null) {
            return false;
        }
        return warehouse.addStock(productId, quantity);
    }

    public boolean sellStock(String productId, String warehouseId, int quantity) {
        Warehouse warehouse = warehouses.get(warehouseId);
        Product product = products.get(productId);
        
        if (warehouse == null || product == null) {
            return false;
        }

        if (!warehouse.removeStock(productId, quantity)) {
            return false;
        }

        // Check if stock is low and notify observers
        int currentStock = warehouse.getStock(productId);
        if (currentStock <= product.getReorderPoint()) {
            notifyObservers(productId, warehouseId, currentStock);
        }

        return true;
    }

    public int getStockLevel(String productId, String warehouseId) {
        Warehouse warehouse = warehouses.get(warehouseId);
        return warehouse != null ? warehouse.getStock(productId) : 0;
    }

    // ========== OBSERVER METHODS ==========

    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String productId, String warehouseId, int currentStock) {
        for (StockObserver observer : observers) {
            observer.onLowStock(productId, warehouseId, currentStock);
        }
    }
}


// ============= DEMO =============

public class Main {
    public static void main(String[] args) {
        InventoryManager manager = InventoryManager.getInstance();

        // Add observer
        manager.addObserver(new EmailAlert());

        // Create warehouse
        Warehouse wh1 = new Warehouse("WH001", "New York");
        manager.addWarehouse(wh1);

        // Create product with reorder point of 10
        Product laptop = new Product("P001", "Dell Laptop", 999.99, 10);
        manager.addProduct(laptop);

        // Purchase 50 units
        manager.purchaseStock("P001", "WH001", 50);
        System.out.println("Stock: " + manager.getStockLevel("P001", "WH001"));  // 50

        // Sell 41 units (will trigger alert as stock goes to 9, below reorder point 10)
        manager.sellStock("P001", "WH001", 41);
        System.out.println("Stock: " + manager.getStockLevel("P001", "WH001"));  // 9
    }
}