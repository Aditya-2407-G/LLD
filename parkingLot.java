/*
 * Parking Lot System - Interview Version
 * 
 * Core Features:
 * - Multiple floors with parking spots
 * - Different spot types (Compact, Large, Handicapped)
 * - Different vehicle types (Car, Bike, Truck)
 * - Park and unpark vehicles
 * - Find available spots
 * - Calculate parking fee
 * - Display availability status
 */

/*
 * Key Components:
 * 1) Vehicle -> represents vehicles with type and license
 * 2) ParkingSpot -> represents a parking spot
 * 3) ParkingFloor -> represents a floor with multiple spots
 * 4) Ticket -> issued when vehicle parks
 * 5) ParkingLot -> main system (Singleton)
 * 6) Strategy Pattern -> for finding spots and calculating fees
 */


// ============= ENUMS =============

public enum VehicleType {
    BIKE,
    CAR,
    TRUCK
}

public enum SpotType {
    COMPACT,      // For bikes and small cars
    LARGE,        // For cars
    HANDICAPPED,  // For handicapped vehicles
    TRUCK         // For trucks
}

public enum SpotStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED
}


// ============= VEHICLE CLASS =============

public class Vehicle {
    private String licenseNumber;
    private VehicleType type;

    public Vehicle(String licenseNumber, VehicleType type) {
        this.licenseNumber = licenseNumber;
        this.type = type;
    }

    public String getLicenseNumber() { return licenseNumber; }
    public VehicleType getType() { return type; }
}


// ============= PARKING SPOT CLASS =============

public class ParkingSpot {
    private String spotId;
    private SpotType type;
    private SpotStatus status;
    private Vehicle parkedVehicle;

    public ParkingSpot(String spotId, SpotType type) {
        this.spotId = spotId;
        this.type = type;
        this.status = SpotStatus.AVAILABLE;
        this.parkedVehicle = null;
    }

    public String getSpotId() { return spotId; }
    public SpotType getType() { return type; }
    public SpotStatus getStatus() { return status; }
    public Vehicle getParkedVehicle() { return parkedVehicle; }

    // Check if this spot can accommodate the vehicle type
    public boolean canFitVehicle(VehicleType vehicleType) {
        switch (vehicleType) {
            case BIKE:
                // Bikes can park in any spot
                return true;
            case CAR:
                // Cars need LARGE or HANDICAPPED spots
                return type == SpotType.LARGE || type == SpotType.HANDICAPPED;
            case TRUCK:
                // Trucks need TRUCK spots
                return type == SpotType.TRUCK;
            default:
                return false;
        }
    }

    // Park vehicle in this spot
    public boolean parkVehicle(Vehicle vehicle) {
        if (status != SpotStatus.AVAILABLE) {
            return false;
        }
        if (!canFitVehicle(vehicle.getType())) {
            return false;
        }
        
        this.parkedVehicle = vehicle;
        this.status = SpotStatus.OCCUPIED;
        return true;
    }

    // Remove vehicle from spot
    public void unparkVehicle() {
        this.parkedVehicle = null;
        this.status = SpotStatus.AVAILABLE;
    }
}


// ============= PARKING FLOOR CLASS =============

public class ParkingFloor {
    private int floorNumber;
    private List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
    }

    public int getFloorNumber() { return floorNumber; }
    public List<ParkingSpot> getSpots() { return spots; }

    // Add spot to this floor
    public void addSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    // Find available spot for given vehicle type
    public ParkingSpot findAvailableSpot(VehicleType vehicleType) {
        for (ParkingSpot spot : spots) {
            if (spot.getStatus() == SpotStatus.AVAILABLE && 
                spot.canFitVehicle(vehicleType)) {
                return spot;
            }
        }
        return null;
    }

    // Get count of available spots by type
    public int getAvailableCount(SpotType spotType) {
        int count = 0;
        for (ParkingSpot spot : spots) {
            if (spot.getType() == spotType && 
                spot.getStatus() == SpotStatus.AVAILABLE) {
                count++;
            }
        }
        return count;
    }

    // Display floor status
    public void displayStatus() {
        System.out.println("Floor " + floorNumber + ":");
        for (SpotType type : SpotType.values()) {
            int available = getAvailableCount(type);
            System.out.println("  " + type + ": " + available + " available");
        }
    }
}


// ============= TICKET CLASS =============

public class Ticket {
    private String ticketId;
    private String vehicleNumber;
    private String spotId;
    private int floorNumber;
    private Date entryTime;

    public Ticket(String ticketId, String vehicleNumber, String spotId, 
                  int floorNumber, Date entryTime) {
        this.ticketId = ticketId;
        this.vehicleNumber = vehicleNumber;
        this.spotId = spotId;
        this.floorNumber = floorNumber;
        this.entryTime = entryTime;
    }

    public String getTicketId() { return ticketId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getSpotId() { return spotId; }
    public int getFloorNumber() { return floorNumber; }
    public Date getEntryTime() { return entryTime; }
}


// ============= STRATEGY PATTERN - PARKING FEE CALCULATION =============

public interface FeeCalculationStrategy {
    double calculateFee(Date entryTime, Date exitTime, VehicleType vehicleType);
}

// Hourly rate strategy
public class HourlyRateStrategy implements FeeCalculationStrategy {
    private static final double BIKE_RATE = 10.0;
    private static final double CAR_RATE = 20.0;
    private static final double TRUCK_RATE = 50.0;

    @Override
    public double calculateFee(Date entryTime, Date exitTime, VehicleType vehicleType) {
        // Calculate hours (minimum 1 hour)
        long diffInMillis = exitTime.getTime() - entryTime.getTime();
        long hours = (diffInMillis / (1000 * 60 * 60)) + 1;
        
        double rate;
        switch (vehicleType) {
            case BIKE:
                rate = BIKE_RATE;
                break;
            case CAR:
                rate = CAR_RATE;
                break;
            case TRUCK:
                rate = TRUCK_RATE;
                break;
            default:
                rate = CAR_RATE;
        }
        
        return hours * rate;
    }
}

// Flat rate strategy
public class FlatRateStrategy implements FeeCalculationStrategy {
    private static final double FLAT_RATE = 100.0;

    @Override
    public double calculateFee(Date entryTime, Date exitTime, VehicleType vehicleType) {
        // Same rate for all vehicles regardless of time
        return FLAT_RATE;
    }
}


// ============= STRATEGY PATTERN - SPOT FINDING =============

public interface SpotFindingStrategy {
    ParkingSpot findSpot(List<ParkingFloor> floors, VehicleType vehicleType);
}

// Find nearest available spot (first available on lowest floor)
public class NearestSpotStrategy implements SpotFindingStrategy {
    @Override
    public ParkingSpot findSpot(List<ParkingFloor> floors, VehicleType vehicleType) {
        // Search from ground floor upwards
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicleType);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }
}

// Find spot that best fits the vehicle (optimize space usage)
public class OptimalFitStrategy implements SpotFindingStrategy {
    @Override
    public ParkingSpot findSpot(List<ParkingFloor> floors, VehicleType vehicleType) {
        // For bikes, prefer COMPACT spots
        // For cars, prefer LARGE spots
        // For trucks, TRUCK spots only
        
        SpotType preferredType = getPreferredSpotType(vehicleType);
        
        // First try to find preferred spot type
        for (ParkingFloor floor : floors) {
            for (ParkingSpot spot : floor.getSpots()) {
                if (spot.getStatus() == SpotStatus.AVAILABLE && 
                    spot.getType() == preferredType && 
                    spot.canFitVehicle(vehicleType)) {
                    return spot;
                }
            }
        }
        
        // If no preferred spot, find any available spot
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicleType);
            if (spot != null) {
                return spot;
            }
        }
        
        return null;
    }
    
    private SpotType getPreferredSpotType(VehicleType vehicleType) {
        switch (vehicleType) {
            case BIKE:
                return SpotType.COMPACT;
            case CAR:
                return SpotType.LARGE;
            case TRUCK:
                return SpotType.TRUCK;
            default:
                return SpotType.LARGE;
        }
    }
}


// ============= PARKING LOT (SINGLETON) =============

public class ParkingLot {
    private static ParkingLot instance;
    
    private String name;
    private List<ParkingFloor> floors;
    private Map<String, Ticket> activeTickets;  // ticketId -> Ticket
    private SpotFindingStrategy spotFindingStrategy;
    private FeeCalculationStrategy feeCalculationStrategy;
    private int ticketCounter = 0;

    private ParkingLot(String name) {
        this.name = name;
        this.floors = new ArrayList<>();
        this.activeTickets = new HashMap<>();
        this.spotFindingStrategy = new NearestSpotStrategy();
        this.feeCalculationStrategy = new HourlyRateStrategy();
    }

    public static ParkingLot getInstance(String name) {
        if (instance == null) {
            instance = new ParkingLot(name);
        }
        return instance;
    }

    // ========== INITIALIZATION ==========

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public void setSpotFindingStrategy(SpotFindingStrategy strategy) {
        this.spotFindingStrategy = strategy;
    }

    public void setFeeCalculationStrategy(FeeCalculationStrategy strategy) {
        this.feeCalculationStrategy = strategy;
    }

    // ========== PARKING OPERATIONS ==========

    // Park a vehicle and return ticket
    public Ticket parkVehicle(Vehicle vehicle) {
        // Find available spot using strategy
        ParkingSpot spot = spotFindingStrategy.findSpot(floors, vehicle.getType());
        
        if (spot == null) {
            System.out.println("No available spot for " + vehicle.getType());
            return null;
        }
        
        // Park the vehicle
        if (!spot.parkVehicle(vehicle)) {
            System.out.println("Failed to park vehicle");
            return null;
        }
        
        // Generate ticket
        String ticketId = "T" + (++ticketCounter);
        int floorNumber = getFloorForSpot(spot);
        Ticket ticket = new Ticket(ticketId, vehicle.getLicenseNumber(), 
                                   spot.getSpotId(), floorNumber, new Date());
        
        activeTickets.put(ticketId, ticket);
        
        System.out.println("Vehicle parked successfully!");
        System.out.println("Ticket ID: " + ticketId);
        System.out.println("Spot: " + spot.getSpotId() + " on Floor " + floorNumber);
        
        return ticket;
    }

    // Unpark vehicle using ticket and calculate fee
    public double unparkVehicle(String ticketId) {
        Ticket ticket = activeTickets.get(ticketId);
        
        if (ticket == null) {
            System.out.println("Invalid ticket");
            return -1;
        }
        
        // Find the spot and vehicle
        ParkingSpot spot = findSpotById(ticket.getSpotId());
        if (spot == null || spot.getParkedVehicle() == null) {
            System.out.println("No vehicle found at the spot");
            return -1;
        }
        
        Vehicle vehicle = spot.getParkedVehicle();
        
        // Calculate fee
        Date exitTime = new Date();
        double fee = feeCalculationStrategy.calculateFee(
            ticket.getEntryTime(), exitTime, vehicle.getType());
        
        // Unpark vehicle
        spot.unparkVehicle();
        activeTickets.remove(ticketId);
        
        System.out.println("Vehicle unparked successfully!");
        System.out.println("License: " + vehicle.getLicenseNumber());
        System.out.println("Parking Fee: $" + String.format("%.2f", fee));
        
        return fee;
    }

    // ========== HELPER METHODS ==========

    // Find which floor contains the spot
    private int getFloorForSpot(ParkingSpot spot) {
        for (ParkingFloor floor : floors) {
            if (floor.getSpots().contains(spot)) {
                return floor.getFloorNumber();
            }
        }
        return -1;
    }

    // Find spot by ID across all floors
    private ParkingSpot findSpotById(String spotId) {
        for (ParkingFloor floor : floors) {
            for (ParkingSpot spot : floor.getSpots()) {
                if (spot.getSpotId().equals(spotId)) {
                    return spot;
                }
            }
        }
        return null;
    }

    // ========== DISPLAY METHODS ==========

    public void displayAvailability() {
        System.out.println("\n=== " + name + " Availability ===");
        for (ParkingFloor floor : floors) {
            floor.displayStatus();
        }
        System.out.println("================================\n");
    }

    public int getTotalAvailableSpots() {
        int total = 0;
        for (ParkingFloor floor : floors) {
            for (ParkingSpot spot : floor.getSpots()) {
                if (spot.getStatus() == SpotStatus.AVAILABLE) {
                    total++;
                }
            }
        }
        return total;
    }
}


// ============= DEMO =============

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Initialize parking lot
        ParkingLot parkingLot = ParkingLot.getInstance("City Mall Parking");
        
        // Create floors and add spots
        // Floor 0 (Ground floor)
        ParkingFloor floor0 = new ParkingFloor(0);
        floor0.addSpot(new ParkingSpot("G-C1", SpotType.COMPACT));
        floor0.addSpot(new ParkingSpot("G-C2", SpotType.COMPACT));
        floor0.addSpot(new ParkingSpot("G-L1", SpotType.LARGE));
        floor0.addSpot(new ParkingSpot("G-L2", SpotType.LARGE));
        floor0.addSpot(new ParkingSpot("G-H1", SpotType.HANDICAPPED));
        parkingLot.addFloor(floor0);
        
        // Floor 1
        ParkingFloor floor1 = new ParkingFloor(1);
        floor1.addSpot(new ParkingSpot("F1-C1", SpotType.COMPACT));
        floor1.addSpot(new ParkingSpot("F1-L1", SpotType.LARGE));
        floor1.addSpot(new ParkingSpot("F1-L2", SpotType.LARGE));
        floor1.addSpot(new ParkingSpot("F1-T1", SpotType.TRUCK));
        parkingLot.addFloor(floor1);
        
        // Display initial availability
        parkingLot.displayAvailability();
        
        System.out.println("=== Scenario 1: Park vehicles ===");
        
        // Park a bike
        Vehicle bike = new Vehicle("KA-01-1234", VehicleType.BIKE);
        Ticket ticket1 = parkingLot.parkVehicle(bike);
        
        // Park a car
        Vehicle car = new Vehicle("KA-01-5678", VehicleType.CAR);
        Ticket ticket2 = parkingLot.parkVehicle(car);
        
        // Park a truck
        Vehicle truck = new Vehicle("KA-01-9999", VehicleType.TRUCK);
        Ticket ticket3 = parkingLot.parkVehicle(truck);
        
        // Display updated availability
        parkingLot.displayAvailability();
        
        System.out.println("\n=== Scenario 2: Unpark vehicles ===");
        
        // Simulate some time passing
        Thread.sleep(2000);
        
        // Unpark bike
        if (ticket1 != null) {
            parkingLot.unparkVehicle(ticket1.getTicketId());
        }
        
        // Unpark car
        if (ticket2 != null) {
            parkingLot.unparkVehicle(ticket2.getTicketId());
        }
        
        // Display final availability
        parkingLot.displayAvailability();
        
        System.out.println("Total available spots: " + parkingLot.getTotalAvailableSpots());
    }
}


/*
 * Interview Discussion Points:
 * 
 * 1) Classes & Responsibilities:
 *    - Vehicle: represents vehicle with type and license
 *    - ParkingSpot: individual parking spot with type
 *    - ParkingFloor: contains multiple spots
 *    - Ticket: issued when vehicle parks
 *    - ParkingLot: manages entire system
 * 
 * 2) Design Patterns:
 *    - Singleton: One parking lot instance
 *    - Strategy: 
 *      a) SpotFindingStrategy: different algorithms to find spots
 *      b) FeeCalculationStrategy: different pricing models
 * 
 * 3) Key Design Decisions:
 *    - Vehicle-Spot compatibility: bikes can park anywhere,
 *      cars need large spots, trucks need truck spots
 *    - Multi-floor support with floor-wise search
 *    - Ticket-based entry/exit system
 * 
 * 4) Core Operations:
 *    - parkVehicle(): find spot, park, issue ticket
 *    - unparkVehicle(): remove vehicle, calculate fee
 *    - displayAvailability(): show spots per floor
 * 
 * 5) Can extend with:
 *    - Reservation system
 *    - Payment gateway integration
 *    - Multiple entry/exit gates
 *    - Monthly pass holders
 *    - Electric vehicle charging spots
 *    - Real-time monitoring dashboard
 * 
 * 6) Time Complexity:
 *    - Park vehicle: O(F * S) where F = floors, S = spots per floor
 *    - Unpark vehicle: O(1) using ticket map
 *    - Can optimize with separate available spots list
 */