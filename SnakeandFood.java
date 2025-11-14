/*
 game played on a width x height grid 
 snake has a inital position and length 
 player can control the snake to move in 4 directions (up, down, left, right)

 food appears randomly on the grid 
 when snake eats food, it grows in length and new food appears
 scoring system based on number of food items eaten
    game ends if snake collides with walls or itself
 */

 /* 
  main components: 
  game board -> represent the grid where the game is played
  snake -> represent the snake with its position and length
  food -> represent the food item on the grid
    player -> represent the player controlling the snake
    game state management -> manage the current state of the game (active, game over)
    scoring system -> track the player's score based on food eaten
 */


 public class GameBoard {
    private int width;
    private int height;

    public GameBoard(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
 }

 public class Snake {

    private Deque<Pair<Integer, Integer>> body; // represents the snake's body 
    private Map<Pair<Integer, Integer>, Boolean> positionMap; // for quick collision detection

    public Snake(int initialX, int initialY) {
        body = new LinkedList<>();
        positionMap = new HashMap<>();
        body.addFirst(new Pair<>(initialX, initialY));
        positionMap.put(new Pair<>(initialX, initialY), true);
    }

    // Methods to move the snake, grow the snake, and check for collisions would go here
 }

 public class Food {
    private int x;
    private int y;

    public Food(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
 }


 public class Player {
    private String name;
    private int score;

    public Player(String name) {
        this.name = name;
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void incrementScore() {
        score++;
    }
 }

    public enum GameStatus {
        ACTIVE,
        GAME_OVER
    }


    public class SnakeGame {
        private gameBoard board;
        private Snake snake;
        private Food food;
        private Player player;
        private GameStatus status;
        private int score;
    }


// strategy pattern for smake movement
// factory pattern for food placement
// singleton pattern for board
// observer pattern for game state changes(score updates, game over notifications)

//1st Strategy Pattern for movement 

interface MovementStrategy {
    Pair<Integer, Integer> getNextPosition(Snake snake);

}

class HumanMovementStrategy implements MovementStrategy {
    private String direction;

    public HumanMovementStrategy(String direction) {
        this.direction = direction;
    }

    @Override
    public Pair<Integer, Integer> getNextPosition(Snake snake) {
        
        int row = snake.getHead().getKey();
        int col = snake.getHead().getValue();

        switch(direction) {
            case "UP":
                return new Pair<>(row - 1, col);
            case "DOWN":
                return new Pair<>(row + 1, col);
            case "LEFT":
                return new Pair<>(row, col - 1);
            case "RIGHT":
                return new Pair<>(row, col + 1);
            default:
                throw new IllegalArgumentException("Invalid direction");
        }
    }

    // class food factory for food placement

    abstract class foodItem {
        int row, col;
        int points;


        public foodItem(int row, int col, int points) {
            this.row = row;
            this.col = col;
            this.points = points;
        }

        // getters

    }

    class FoodFactory {
        public static FoodItem createFood(String type, int row, int col) {
            switch(type.toLowerCase()) {
                case "normal":
                    return new NormalFood(row, col);
                case "special":
                    return new SpecialFood(row, col);   
                default:
                    throw new IllegalArgumentException("Invalid food type");
            }
        }
    }

    public class NormalFood extends FoodItem {
        public NormalFood(int row, int col) {
            super(row, col, 1);
        }
    }
    public class SpecialFood extends FoodItem {
        public SpecialFood(int row, int col) {
            super(row, col, 5);
        }
    }


    public class GameBoard {
        private static GameBoard instance;
        private int width;
        private int height;

        private GameBoard(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public static GameBoard getInstance(int width, int height) {
            if (instance == null) {
                instance = new GameBoard(width, height);
            }
            return instance;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }



    // finally the snake game - 

    class SnakeGame {
        private GameBoard board;
        private Deque<Pair<Integer, Integer>> snake;
        private Map<Pair<Integer, Integer>, Boolean> positionMap;
        private FoodItem food;
        private Player player;
        private GameStatus status;
        private MovementStrategy movementStrategy;
        private int score;


        public snakeGame(int width, int height, Player player, MovementStrategy movementStrategy) {
            this.board = GameBoard.getInstance(width, height);
            this.snake = new LinkedList<>();
            this.positionMap = new HashMap<>();
            this.player = player;
            this.movementStrategy = movementStrategy;
            this.status = GameStatus.ACTIVE;
            this.score = 0;

            // Initialize snake position
            Pair<Integer, Integer> initialPosition = new Pair<>(height / 2, width / 2);
            snake.addFirst(initialPosition);
            positionMap.put(initialPosition, true);

            // Place initial food
            placeFood();
        }

        public int moveSnake() {
            if (status == GameStatus.GAME_OVER) {
                return score; // Game over, no more moves
            }

            Pair<Integer, Integer> nextPosition = movementStrategy.getNextPosition(snake);

            // Check for collisions with walls
            if (nextPosition.getKey() < 0 || nextPosition.getKey() >= board.getHeight() ||
                nextPosition.getValue() < 0 || nextPosition.getValue() >= board.getWidth()) {
                status = GameStatus.GAME_OVER;
                return score;
            }

            // Check for collisions with itself
            if (positionMap.containsKey(nextPosition)) {
                status = GameStatus.GAME_OVER;
                return score;
            }

            // Move snake
            snake.addFirst(nextPosition);
            positionMap.put(nextPosition, true);

            // Check if food is eaten
            if (nextPosition.getKey() == food.row && nextPosition.getValue() == food.col) {
                score += food.points;
                player.incrementScore();
                placeFood(); // Place new food
            } else {
                // Remove tail
                Pair<Integer, Integer> tail = snake.removeLast();
                positionMap.remove(tail);
            }

            return score;
        }
        private void placeFood() {
            Random rand = new Random();
            int foodRow, foodCol;
            do {
                foodRow = rand.nextInt(board.getHeight());
                foodCol = rand.nextInt(board.getWidth());
            } while (positionMap.containsKey(new Pair<>(foodRow, foodCol)));

            // Randomly decide food type
            String foodType = rand.nextInt(10) < 8 ? "normal" : "special"; // 80% normal, 20% special
            food = FoodFactory.createFood(foodType, foodRow, foodCol);
        }
        
    }


    public enum GameStatus {
        ACTIVE,
        GAME_OVER
}   

public class SnakeGame {

        public static void main(String[] args) {
            // define game config - width, height 
            }
            System.out.println("Game Over! Final Score: " + player.getScore());
        }
}

