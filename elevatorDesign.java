/*
 * Elevator System - Interview Version
 * 
 * Core Features:
 * - Multiple elevators in a building
 * - Handle floor requests (up/down buttons)
 * - Handle internal elevator requests (floor selection inside)
 * - Optimal elevator selection algorithm
 * - Track elevator state (moving, idle, direction)
 */

/*
 * Key Components:
 * 1) Elevator -> represents single elevator with state
 * 2) Request -> represents floor request with direction
 * 3) ElevatorController -> manages single elevator
 * 4) ElevatorSystem -> manages multiple elevators (Singleton)
 * 5) Strategy Pattern -> for elevator selection algorithm
 */


// ============= ENUMS =============

public enum Direction {
    UP,
    DOWN,
    IDLE
}

public enum ElevatorState {
    MOVING,
    IDLE,
    MAINTENANCE
}


// ============= REQUEST CLASS =============

public class Request {
    private int floor;
    private Direction direction;

    public Request(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }

    public int getFloor() { return floor; }
    public Direction getDirection() { return direction; }
}


// ============= ELEVATOR CLASS =============

public class Elevator {
    private int elevatorId;
    private int currentFloor;
    private Direction currentDirection;
    private ElevatorState state;
    private Set<Integer> requestedFloors;  // internal button presses

    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;  // ground floor
        this.currentDirection = Direction.IDLE;
        this.state = ElevatorState.IDLE;
        this.requestedFloors = new TreeSet<>();
    }

    public int getElevatorId() { return elevatorId; }
    public int getCurrentFloor() { return currentFloor; }
    public Direction getCurrentDirection() { return currentDirection; }
    public ElevatorState getState() { return state; }

    public void addRequest(int floor) {
        requestedFloors.add(floor);
    }

    public void move() {
        if (requestedFloors.isEmpty()) {
            currentDirection = Direction.IDLE;
            state = ElevatorState.IDLE;
            return;
        }

        state = ElevatorState.MOVING;

        // Determine direction based on next target floor
        int nextFloor = getNextFloor();
        
        if (nextFloor > currentFloor) {
            currentDirection = Direction.UP;
            currentFloor++;
        } else if (nextFloor < currentFloor) {
            currentDirection = Direction.DOWN;
            currentFloor--;
        }

        // Check if reached a requested floor
        if (requestedFloors.contains(currentFloor)) {
            requestedFloors.remove(currentFloor);
            System.out.println("Elevator " + elevatorId + " reached floor " + currentFloor);
        }
    }

    private int getNextFloor() {
        // Simple logic: go to nearest floor in current direction
        if (currentDirection == Direction.UP || currentDirection == Direction.IDLE) {
            for (int floor : requestedFloors) {
                if (floor >= currentFloor) {
                    return floor;
                }
            }
        }
        // If no floors above, go to nearest floor below
        int nearest = Integer.MAX_VALUE;
        for (int floor : requestedFloors) {
            if (Math.abs(floor - currentFloor) < Math.abs(nearest - currentFloor)) {
                nearest = floor;
            }
        }
        return nearest;
    }

    public boolean hasRequests() {
        return !requestedFloors.isEmpty();
    }
}


// ============= STRATEGY PATTERN - ELEVATOR SELECTION =============

public interface ElevatorSelectionStrategy {
    Elevator selectElevator(List<Elevator> elevators, Request request);
}

// Select nearest available elevator
public class NearestElevatorStrategy implements ElevatorSelectionStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        Elevator nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            if (elevator.getState() == ElevatorState.MAINTENANCE) {
                continue;
            }

            int distance = Math.abs(elevator.getCurrentFloor() - request.getFloor());
            
            // Prefer elevators moving in same direction or idle
            if (elevator.getCurrentDirection() == request.getDirection() || 
                elevator.getCurrentDirection() == Direction.IDLE) {
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = elevator;
                }
            }
        }

        // If no elevator in same direction, pick any nearest
        if (nearest == null) {
            for (Elevator elevator : elevators) {
                if (elevator.getState() == ElevatorState.MAINTENANCE) {
                    continue;
                }
                int distance = Math.abs(elevator.getCurrentFloor() - request.getFloor());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = elevator;
                }
            }
        }

        return nearest;
    }
}


// ============= ELEVATOR CONTROLLER =============

public class ElevatorController {
    private Elevator elevator;
    private Queue<Request> pendingRequests;

    public ElevatorController(Elevator elevator) {
        this.elevator = elevator;
        this.pendingRequests = new LinkedList<>();
    }

    public void addRequest(Request request) {
        pendingRequests.offer(request);
        elevator.addRequest(request.getFloor());
    }

    public void step() {
        if (elevator.hasRequests()) {
            elevator.move();
        }
    }

    public Elevator getElevator() {
        return elevator;
    }
}


// ============= ELEVATOR SYSTEM (SINGLETON) =============

public class ElevatorSystem {
    private static ElevatorSystem instance;
    private List<ElevatorController> controllers;
    private ElevatorSelectionStrategy selectionStrategy;
    private int numFloors;

    private ElevatorSystem(int numElevators, int numFloors) {
        this.controllers = new ArrayList<>();
        this.numFloors = numFloors;
        this.selectionStrategy = new NearestElevatorStrategy();

        // Create elevators
        for (int i = 0; i < numElevators; i++) {
            Elevator elevator = new Elevator(i + 1);
            controllers.add(new ElevatorController(elevator));
        }
    }

    public static ElevatorSystem getInstance(int numElevators, int numFloors) {
        if (instance == null) {
            instance = new ElevatorSystem(numElevators, numFloors);
        }
        return instance;
    }

    // External request (floor button pressed)
    public void requestElevator(int floor, Direction direction) {
        if (floor < 0 || floor >= numFloors) {
            System.out.println("Invalid floor");
            return;
        }

        Request request = new Request(floor, direction);
        
        // Select best elevator using strategy
        List<Elevator> elevators = controllers.stream()
                                              .map(ElevatorController::getElevator)
                                              .collect(Collectors.toList());
        
        Elevator selectedElevator = selectionStrategy.selectElevator(elevators, request);
        
        if (selectedElevator != null) {
            // Find controller for selected elevator
            for (ElevatorController controller : controllers) {
                if (controller.getElevator() == selectedElevator) {
                    controller.addRequest(request);
                    System.out.println("Assigned elevator " + selectedElevator.getElevatorId() + 
                                     " to floor " + floor);
                    break;
                }
            }
        }
    }

    // Internal request (button inside elevator pressed)
    public void selectFloor(int elevatorId, int targetFloor) {
        if (targetFloor < 0 || targetFloor >= numFloors) {
            System.out.println("Invalid floor");
            return;
        }

        for (ElevatorController controller : controllers) {
            if (controller.getElevator().getElevatorId() == elevatorId) {
                controller.getElevator().addRequest(targetFloor);
                System.out.println("Floor " + targetFloor + " added to elevator " + elevatorId);
                break;
            }
        }
    }

    // Simulate one step for all elevators
    public void step() {
        for (ElevatorController controller : controllers) {
            controller.step();
        }
    }

    public void displayStatus() {
        System.out.println("\n=== Elevator Status ===");
        for (ElevatorController controller : controllers) {
            Elevator e = controller.getElevator();
            System.out.println("Elevator " + e.getElevatorId() + 
                             ": Floor " + e.getCurrentFloor() + 
                             ", Direction: " + e.getCurrentDirection() + 
                             ", State: " + e.getState());
        }
        System.out.println("=====================\n");
    }
}


// ============= DEMO =============

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Create system with 3 elevators, 10 floors
        ElevatorSystem system = ElevatorSystem.getInstance(3, 10);

        // Initial status
        system.displayStatus();

        // Person at floor 5 wants to go UP
        system.requestElevator(5, Direction.UP);
        
        // Person at floor 2 wants to go DOWN
        system.requestElevator(2, Direction.DOWN);

        // Simulate 10 steps
        for (int i = 0; i < 10; i++) {
            Thread.sleep(500);  // simulate time
            system.step();
            system.displayStatus();
        }

        // Person inside elevator 1 selects floor 8
        system.selectFloor(1, 8);

        // Continue simulation
        for (int i = 0; i < 10; i++) {
            Thread.sleep(500);
            system.step();
            system.displayStatus();
        }
    }
}


/*
 * Interview Discussion Points:
 * 
 * 1) Classes & Responsibilities:
 *    - Elevator: maintains state and floor requests
 *    - ElevatorController: controls single elevator
 *    - ElevatorSystem: coordinates all elevators
 * 
 * 2) Design Patterns:
 *    - Singleton: ElevatorSystem (one system per building)
 *    - Strategy: Different algorithms for selecting elevator
 * 
 * 3) Key Algorithms:
 *    - Elevator selection (nearest, least busy, etc.)
 *    - Floor scheduling (SCAN, LOOK algorithms)
 * 
 * 4) Can extend with:
 *    - Priority requests (emergency)
 *    - Energy optimization
 *    - Load balancing
 *    - Different scheduling algorithms (FCFS, SCAN, LOOK)
 */