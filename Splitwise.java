/*
 * Splitwise System - Interview Version
 * 
 * Core Features:
 * - Add users
 * - Create groups
 * - Add expenses (equal, exact, percentage split)
 * - Track who owes whom
 * - Settle up balances
 * - Show simplified debts
 */

/*
 * Key Components:
 * 1) User -> represents a person
 * 2) Expense -> represents money spent
 * 3) Group -> collection of users who share expenses
 * 4) Split -> how expense is divided (equal, exact, percent)
 * 5) SplitwiseService -> manages everything (Singleton)
 * 6) Strategy Pattern -> different split strategies
 */


// ============= USER CLASS =============

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;

    public User(String userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}


// ============= SPLIT STRATEGY (STRATEGY PATTERN) =============

public interface SplitStrategy {
    Map<String, Double> calculateSplit(double totalAmount, List<String> userIds, 
                                       Map<String, Double> splitData);
}

// Equal split among all users
public class EqualSplit implements SplitStrategy {
    @Override
    public Map<String, Double> calculateSplit(double totalAmount, List<String> userIds, 
                                             Map<String, Double> splitData) {
        Map<String, Double> splits = new HashMap<>();
        double amountPerPerson = totalAmount / userIds.size();
        
        for (String userId : userIds) {
            splits.put(userId, amountPerPerson);
        }
        return splits;
    }
}

// Exact amounts specified for each user
public class ExactSplit implements SplitStrategy {
    @Override
    public Map<String, Double> calculateSplit(double totalAmount, List<String> userIds, 
                                             Map<String, Double> splitData) {
        // splitData contains userId -> exact amount
        double sum = splitData.values().stream().mapToDouble(Double::doubleValue).sum();
        
        if (Math.abs(sum - totalAmount) > 0.01) {
            throw new IllegalArgumentException("Split amounts don't match total");
        }
        
        return new HashMap<>(splitData);
    }
}

// Percentage split
public class PercentSplit implements SplitStrategy {
    @Override
    public Map<String, Double> calculateSplit(double totalAmount, List<String> userIds, 
                                             Map<String, Double> splitData) {
        // splitData contains userId -> percentage
        Map<String, Double> splits = new HashMap<>();
        double totalPercent = splitData.values().stream().mapToDouble(Double::doubleValue).sum();
        
        if (Math.abs(totalPercent - 100.0) > 0.01) {
            throw new IllegalArgumentException("Percentages must sum to 100");
        }
        
        for (Map.Entry<String, Double> entry : splitData.entrySet()) {
            double amount = (entry.getValue() / 100.0) * totalAmount;
            splits.put(entry.getKey(), amount);
        }
        return splits;
    }
}


// ============= EXPENSE CLASS =============

public class Expense {
    private String expenseId;
    private String description;
    private double amount;
    private String paidBy;  // userId who paid
    private List<String> participants;  // userIds involved
    private Map<String, Double> splits;  // userId -> amount they owe
    private Date createdAt;

    public Expense(String expenseId, String description, double amount, 
                   String paidBy, List<String> participants, Map<String, Double> splits) {
        this.expenseId = expenseId;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.participants = participants;
        this.splits = splits;
        this.createdAt = new Date();
    }

    public String getExpenseId() { return expenseId; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getPaidBy() { return paidBy; }
    public Map<String, Double> getSplits() { return splits; }
}


// ============= GROUP CLASS =============

public class Group {
    private String groupId;
    private String name;
    private List<String> memberIds;  // userIds in group
    private List<String> expenseIds;  // expenses in this group

    public Group(String groupId, String name, List<String> memberIds) {
        this.groupId = groupId;
        this.name = name;
        this.memberIds = new ArrayList<>(memberIds);
        this.expenseIds = new ArrayList<>();
    }

    public String getGroupId() { return groupId; }
    public String getName() { return name; }
    public List<String> getMemberIds() { return memberIds; }
    public List<String> getExpenseIds() { return expenseIds; }

    public void addMember(String userId) {
        if (!memberIds.contains(userId)) {
            memberIds.add(userId);
        }
    }

    public void addExpense(String expenseId) {
        expenseIds.add(expenseId);
    }
}


// ============= SPLITWISE SERVICE (SINGLETON) =============

public class SplitwiseService {
    private static SplitwiseService instance;
    
    private Map<String, User> users;
    private Map<String, Group> groups;
    private Map<String, Expense> expenses;
    private Map<String, Map<String, Double>> balances;  // userId -> (friendId -> amount)
    
    private int expenseCounter = 0;

    private SplitwiseService() {
        users = new HashMap<>();
        groups = new HashMap<>();
        expenses = new HashMap<>();
        balances = new HashMap<>();
    }

    public static SplitwiseService getInstance() {
        if (instance == null) {
            instance = new SplitwiseService();
        }
        return instance;
    }

    // ========== USER OPERATIONS ==========

    public void addUser(User user) {
        users.put(user.getUserId(), user);
        balances.put(user.getUserId(), new HashMap<>());
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    // ========== GROUP OPERATIONS ==========

    public void createGroup(String groupId, String name, List<String> memberIds) {
        Group group = new Group(groupId, name, memberIds);
        groups.put(groupId, group);
    }

    public void addMemberToGroup(String groupId, String userId) {
        Group group = groups.get(groupId);
        if (group != null) {
            group.addMember(userId);
        }
    }

    // ========== EXPENSE OPERATIONS ==========

    public void addExpense(String description, double amount, String paidBy,
                          List<String> participants, SplitStrategy strategy,
                          Map<String, Double> splitData) {
        
        // Calculate splits using strategy
        Map<String, Double> splits = strategy.calculateSplit(amount, participants, splitData);
        
        // Create expense
        String expenseId = "EXP" + (++expenseCounter);
        Expense expense = new Expense(expenseId, description, amount, paidBy, 
                                     participants, splits);
        expenses.put(expenseId, expense);
        
        // Update balances
        updateBalances(paidBy, splits);
        
        System.out.println("Expense added: " + description + " for $" + amount);
    }

    private void updateBalances(String paidBy, Map<String, Double> splits) {
        for (Map.Entry<String, Double> entry : splits.entrySet()) {
            String userId = entry.getKey();
            double amount = entry.getValue();
            
            if (userId.equals(paidBy)) {
                continue;  // Person who paid doesn't owe themselves
            }
            
            // userId owes paidBy
            addBalance(userId, paidBy, amount);
        }
    }

    private void addBalance(String user1, String user2, double amount) {
        // user1 owes user2
        Map<String, Double> user1Balances = balances.get(user1);
        user1Balances.put(user2, user1Balances.getOrDefault(user2, 0.0) + amount);
        
        // user2 is owed by user1 (negative balance)
        Map<String, Double> user2Balances = balances.get(user2);
        user2Balances.put(user1, user2Balances.getOrDefault(user1, 0.0) - amount);
    }

    // ========== BALANCE QUERIES ==========

    public void showBalance(String userId) {
        User user = users.get(userId);
        if (user == null) {
            System.out.println("User not found");
            return;
        }

        System.out.println("\n=== Balance for " + user.getName() + " ===");
        Map<String, Double> userBalances = balances.get(userId);
        
        boolean hasBalance = false;
        for (Map.Entry<String, Double> entry : userBalances.entrySet()) {
            String friendId = entry.getKey();
            double amount = entry.getValue();
            
            if (Math.abs(amount) < 0.01) {
                continue;  // Skip negligible amounts
            }
            
            hasBalance = true;
            User friend = users.get(friendId);
            
            if (amount > 0) {
                System.out.println(user.getName() + " owes " + friend.getName() + 
                                 ": $" + String.format("%.2f", amount));
            } else {
                System.out.println(friend.getName() + " owes " + user.getName() + 
                                 ": $" + String.format("%.2f", -amount));
            }
        }
        
        if (!hasBalance) {
            System.out.println("No pending balances");
        }
        System.out.println("=======================\n");
    }

    public void showAllBalances() {
        System.out.println("\n=== All Balances ===");
        for (String userId : users.keySet()) {
            Map<String, Double> userBalances = balances.get(userId);
            
            for (Map.Entry<String, Double> entry : userBalances.entrySet()) {
                double amount = entry.getValue();
                if (amount > 0.01) {  // Only show positive balances to avoid duplicates
                    User user = users.get(userId);
                    User friend = users.get(entry.getKey());
                    System.out.println(user.getName() + " owes " + friend.getName() + 
                                     ": $" + String.format("%.2f", amount));
                }
            }
        }
        System.out.println("===================\n");
    }

    // ========== SETTLE UP ==========

    public void settleUp(String userId1, String userId2) {
        Map<String, Double> user1Balances = balances.get(userId1);
        Map<String, Double> user2Balances = balances.get(userId2);
        
        if (user1Balances == null || user2Balances == null) {
            System.out.println("Invalid users");
            return;
        }
        
        double amount = user1Balances.getOrDefault(userId2, 0.0);
        
        if (Math.abs(amount) < 0.01) {
            System.out.println("No balance to settle");
            return;
        }
        
        User user1 = users.get(userId1);
        User user2 = users.get(userId2);
        
        if (amount > 0) {
            System.out.println(user1.getName() + " paid " + user2.getName() + 
                             " $" + String.format("%.2f", amount));
        } else {
            System.out.println(user2.getName() + " paid " + user1.getName() + 
                             " $" + String.format("%.2f", -amount));
        }
        
        // Clear balances
        user1Balances.put(userId2, 0.0);
        user2Balances.put(userId1, 0.0);
    }
}


// ============= DEMO =============

public class Main {
    public static void main(String[] args) {
        SplitwiseService service = SplitwiseService.getInstance();
        
        // Add users
        service.addUser(new User("U1", "Alice", "alice@email.com", "1234567890"));
        service.addUser(new User("U2", "Bob", "bob@email.com", "9876543210"));
        service.addUser(new User("U3", "Charlie", "charlie@email.com", "5555555555"));
        
        System.out.println("=== Scenario 1: Equal Split ===");
        // Alice pays $90 for dinner, split equally among 3 people
        List<String> participants = Arrays.asList("U1", "U2", "U3");
        service.addExpense("Dinner", 90.0, "U1", participants, 
                          new EqualSplit(), null);
        service.showBalance("U1");
        service.showBalance("U2");
        
        System.out.println("\n=== Scenario 2: Exact Split ===");
        // Bob pays $100, Alice owes $30, Charlie owes $70
        Map<String, Double> exactSplits = new HashMap<>();
        exactSplits.put("U1", 30.0);
        exactSplits.put("U3", 70.0);
        service.addExpense("Shopping", 100.0, "U2", 
                          Arrays.asList("U1", "U3"), 
                          new ExactSplit(), exactSplits);
        service.showBalance("U1");
        service.showBalance("U2");
        
        System.out.println("\n=== Scenario 3: Percentage Split ===");
        // Charlie pays $150, split 50%-30%-20%
        Map<String, Double> percentSplits = new HashMap<>();
        percentSplits.put("U1", 50.0);
        percentSplits.put("U2", 30.0);
        percentSplits.put("U3", 20.0);
        service.addExpense("Trip", 150.0, "U3", participants, 
                          new PercentSplit(), percentSplits);
        
        System.out.println("\n=== All Balances ===");
        service.showAllBalances();
        
        System.out.println("\n=== Scenario 4: Settle Up ===");
        service.settleUp("U1", "U2");
        service.showBalance("U1");
        service.showBalance("U2");
    }
}


/*
 * Interview Discussion Points:
 * 
 * 1) Classes & Responsibilities:
 *    - User: person using the app
 *    - Expense: money spent with split details
 *    - Group: collection of users (optional feature)
 *    - SplitwiseService: manages all operations
 * 
 * 2) Design Patterns:
 *    - Singleton: One service instance
 *    - Strategy: Different split strategies (Equal, Exact, Percent)
 * 
 * 3) Key Data Structure:
 *    - balances: Map<userId, Map<friendId, amount>>
 *    - Positive amount = user owes friend
 *    - Negative amount = friend owes user
 * 
 * 4) Core Operations:
 *    - addExpense(): create expense and update balances
 *    - showBalance(): display who owes whom
 *    - settleUp(): clear balance between two users
 * 
 * 5) Can extend with:
 *    - Simplify debts (minimize transactions)
 *    - Activity feed
 *    - Expense categories
 *    - Currency conversion
 *    - Reminders for payments
 */