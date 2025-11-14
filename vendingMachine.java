/*
 * Vending Machine System - Interview Version
 * 
 * Core Features:
 * - Display available products with prices
 * - Accept money (coins, notes)
 * - Dispense product
 * - Return change
 * - Handle insufficient funds
 * - Track inventory
 */

/*
 * Key Components:
 * 1) Product -> items in machine
 * 2) Inventory -> track product stock
 * 3) VendingMachine -> main machine (Singleton)
 * 4) State Pattern -> machine states (idle, has money, dispense)
 * 5) Payment handling
 */


//  Idle → insertMoney() → HasMoney → selectProduct() → Dispense → back to Idle
//                            ↓
//                     returnChange() → Idle


// ============= PRODUCT CLASS =============

public class Product {
    private String code;  // e.g., "A1", "B2"
    private String name;
    private double price;

    public Product(String code, String name, double price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}


// ============= INVENTORY CLASS =============

public class Inventory {
    private Map<String, Product> products;  // code -> product
    private Map<String, Integer> stock;     // code -> quantity

    public Inventory() {
        products = new HashMap<>();
        stock = new HashMap<>();
    }

    public void addProduct(Product product, int quantity) {
        products.put(product.getCode(), product);
        stock.put(product.getCode(), quantity);
    }

    public Product getProduct(String code) {
        return products.get(code);
    }

    public int getStock(String code) {
        return stock.getOrDefault(code, 0);
    }

    public boolean isAvailable(String code) {
        return getStock(code) > 0;
    }

    public void reduceStock(String code) {
        if (isAvailable(code)) {
            stock.put(code, stock.get(code) - 1);
        }
    }

    public void displayProducts() {
        System.out.println("\n=== Available Products ===");
        for (Product product : products.values()) {
            int quantity = stock.get(product.getCode());
            System.out.println(product.getCode() + ": " + product.getName() + 
                             " - $" + product.getPrice() + 
                             " [Stock: " + quantity + "]");
        }
        System.out.println("========================\n");
    }
}


// ============= STATE PATTERN =============

public interface VendingMachineState {
    void insertMoney(double amount);
    void selectProduct(String code);
    void dispenseProduct();
    void returnChange();
}

// Idle State - waiting for money
public class IdleState implements VendingMachineState {
    private VendingMachine machine;

    public IdleState(VendingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void insertMoney(double amount) {
        machine.addMoney(amount);
        System.out.println("Money inserted: $" + amount);
        System.out.println("Total: $" + machine.getCurrentAmount());
        machine.setState(machine.getHasMoneyState());
    }

    @Override
    public void selectProduct(String code) {
        System.out.println("Please insert money first");
    }

    @Override
    public void dispenseProduct() {
        System.out.println("Please insert money and select product");
    }

    @Override
    public void returnChange() {
        System.out.println("No money to return");
    }
}

// Has Money State - money inserted, waiting for selection
public class HasMoneyState implements VendingMachineState {
    private VendingMachine machine;

    public HasMoneyState(VendingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void insertMoney(double amount) {
        machine.addMoney(amount);
        System.out.println("Money added: $" + amount);
        System.out.println("Total: $" + machine.getCurrentAmount());
    }

    @Override
    public void selectProduct(String code) {
        Product product = machine.getInventory().getProduct(code);
        
        if (product == null) {
            System.out.println("Invalid product code");
            return;
        }

        if (!machine.getInventory().isAvailable(code)) {
            System.out.println("Product out of stock");
            return;
        }

        if (machine.getCurrentAmount() < product.getPrice()) {
            double needed = product.getPrice() - machine.getCurrentAmount();
            System.out.println("Insufficient funds. Need $" + needed + " more");
            return;
        }

        machine.setSelectedProduct(product);
        machine.setState(machine.getDispenseState());
        dispenseProduct();
    }

    @Override
    public void dispenseProduct() {
        System.out.println("Please select a product first");
    }

    @Override
    public void returnChange() {
        double amount = machine.getCurrentAmount();
        System.out.println("Returning $" + amount);
        machine.resetAmount();
        machine.setState(machine.getIdleState());
    }
}

// Dispense State - product selected, ready to dispense
public class DispenseState implements VendingMachineState {
    private VendingMachine machine;

    public DispenseState(VendingMachine machine) {
        this.machine = machine;
    }

    @Override
    public void insertMoney(double amount) {
        System.out.println("Please wait, dispensing product");
    }

    @Override
    public void selectProduct(String code) {
        System.out.println("Already dispensing product");
    }

    @Override
    public void dispenseProduct() {
        Product product = machine.getSelectedProduct();
        System.out.println("Dispensing: " + product.getName());
        
        // Reduce inventory
        machine.getInventory().reduceStock(product.getCode());
        
        // Calculate change
        double change = machine.getCurrentAmount() - product.getPrice();
        if (change > 0) {
            System.out.println("Returning change: $" + change);
        }
        
        // Reset machine
        machine.resetAmount();
        machine.setSelectedProduct(null);
        machine.setState(machine.getIdleState());
        
        System.out.println("Thank you for your purchase!\n");
    }

    @Override
    public void returnChange() {
        System.out.println("Cannot return change during dispensing");
    }
}


// ============= VENDING MACHINE (SINGLETON) =============

public class VendingMachine {
    private static VendingMachine instance;
    
    private Inventory inventory;
    private double currentAmount;
    private Product selectedProduct;
    
    // States
    private VendingMachineState idleState;
    private VendingMachineState hasMoneyState;
    private VendingMachineState dispenseState;
    private VendingMachineState currentState;

    private VendingMachine() {
        inventory = new Inventory();
        currentAmount = 0.0;
        
        // Initialize states
        idleState = new IdleState(this);
        hasMoneyState = new HasMoneyState(this);
        dispenseState = new DispenseState(this);
        currentState = idleState;
    }

    public static VendingMachine getInstance() {
        if (instance == null) {
            instance = new VendingMachine();
        }
        return instance;
    }

    // Getters for states
    public VendingMachineState getIdleState() { return idleState; }
    public VendingMachineState getHasMoneyState() { return hasMoneyState; }
    public VendingMachineState getDispenseState() { return dispenseState; }

    public void setState(VendingMachineState state) {
        this.currentState = state;
    }

    public Inventory getInventory() { return inventory; }
    public double getCurrentAmount() { return currentAmount; }
    public Product getSelectedProduct() { return selectedProduct; }

    public void addMoney(double amount) {
        this.currentAmount += amount;
    }

    public void resetAmount() {
        this.currentAmount = 0.0;
    }

    public void setSelectedProduct(Product product) {
        this.selectedProduct = product;
    }

    // Delegate to current state
    public void insertMoney(double amount) {
        currentState.insertMoney(amount);
    }

    public void selectProduct(String code) {
        currentState.selectProduct(code);
    }

    public void dispenseProduct() {
        currentState.dispenseProduct();
    }

    public void returnChange() {
        currentState.returnChange();
    }

    public void displayProducts() {
        inventory.displayProducts();
    }
}


// ============= DEMO =============

public class Main {
    public static void main(String[] args) {
        VendingMachine machine = VendingMachine.getInstance();
        
        // Add products to inventory
        machine.getInventory().addProduct(new Product("A1", "Coke", 1.50), 10);
        machine.getInventory().addProduct(new Product("A2", "Pepsi", 1.50), 5);
        machine.getInventory().addProduct(new Product("B1", "Chips", 2.00), 8);
        machine.getInventory().addProduct(new Product("B2", "Candy", 1.00), 15);
        
        // Display available products
        machine.displayProducts();
        
        System.out.println("=== Scenario 1: Successful Purchase ===");
        machine.insertMoney(2.00);
        machine.selectProduct("A1");  // Buy Coke for $1.50, get $0.50 change
        
        System.out.println("\n=== Scenario 2: Insufficient Funds ===");
        machine.insertMoney(1.00);
        machine.selectProduct("B1");  // Try to buy Chips for $2.00
        machine.insertMoney(1.50);    // Add more money
        machine.selectProduct("B1");  // Now buy Chips for $2.00, get $0.50 change
        
        System.out.println("\n=== Scenario 3: Cancel Transaction ===");
        machine.insertMoney(5.00);
        machine.returnChange();       // Get money back
        
        System.out.println("\n=== Scenario 4: Out of Stock ===");
        // First, buy all Pepsi
        for (int i = 0; i < 5; i++) {
            machine.insertMoney(2.00);
            machine.selectProduct("A2");
        }
        // Try to buy when out of stock
        machine.insertMoney(2.00);
        machine.selectProduct("A2");  // Out of stock
        machine.returnChange();
        
        // Display final inventory
        machine.displayProducts();
    }
}


/*
 * Interview Discussion Points:
 * 
 * 1) Classes & Responsibilities:
 *    - Product: item with code, name, price
 *    - Inventory: manages stock levels
 *    - VendingMachine: coordinates everything
 * 
 * 2) Design Patterns:
 *    - Singleton: One vending machine instance
 *    - State Pattern: Different behaviors in different states
 *      * IdleState: waiting for money
 *      * HasMoneyState: money inserted
 *      * DispenseState: dispensing product
 * 
 * 3) Key Operations:
 *    - insertMoney(): add money
 *    - selectProduct(): choose item
 *    - dispenseProduct(): give item + change
 *    - returnChange(): cancel transaction
 * 
 * 4) Can extend with:
 *    - Different payment methods (card, digital wallet)
 *    - Admin functions (restock, collect money)
 *    - Refund handling
 *    - Transaction logging
 */