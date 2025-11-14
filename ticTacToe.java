/*
    3x3 matrix
    2 players take turns marking X or O
    game conditions: 
    - win: 3 in a row (horizontally, vertically, diagonally)
    - draw: all cells filled without a winner
    validation of moves -> tracking turns and cell occupancy
*/

/*
 * Key Components - 
 * 
 * Symbols -> Represent X and O
 * Board -> Represent the 3x3 grid
 * Player -> Represent the two players
 * Game -> Manage the game state, turns, and win/draw conditions
 * Move -> Represent a player's move
 */


 /*Player interaction 
  * Player interface with make move method
  */

// State -> manage the game state -> inprogress, won, draw

    public enum Symbol {
        X,
        O,
        EMPTY
    }
    
public class Player {   
    private String name;
    private Symbol symbol;

    public Player(String name, Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public char getSymbol() {
        return symbol;
    }
}


    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhite() {
        return isWhite;
    }


    // game context 

    public class GameContext {
         private GameState state;

         public GameContext() {
            currentState = new XTrunState();
         }

         public void setState(GameState state) {
            this.state = state;
         }

         public void nextState() {
            state.next(this);
         }

         public boolean isGameOver() {
            return state.isGameOver();
         }
    }

    // we will have game state 

    public interface GameState {
        void next(GameContext context);
        boolean isGameOver();
    }

    // state for x turn 

    public class XTrunState implements GameState {

        @Override
        public void next(GameContext context) {
            context.setState(new OTurnState());
        }

        @Override
        public boolean isGameOver() {
            return false;
        }
    }

    // state for o turn

    public class OTurnState implements GameState {

        @Override
        public void next(GameContext context) {
            context.setState(new XTrunState());
        }

        @Override
        public boolean isGameOver() {
            return false;
        }
    }


    // x won state
    public class XWonState implements GameState {

        @Override
        public void next(GameContext context) {
            // game over, no next state
        }

        @Override
        public boolean isGameOver() {
            return true;
        }
    }

    // o won state

    public class OWonState implements GameState {

        @Override
        public void next(GameContext context) {
            // game over, no next state
        }

        @Override
        public boolean isGameOver() {
            return true;
        }
    }



    // Board class to represent the 3x3 grid 


    public class Board {
        private final int rows;
        private final int cols;
        private Symbol[][] grid;


        public Board() {
            this.rows = 3;
            this.cols = 3;
            grid = new Symbol[rows][cols];

            for(int i=0; i<rows; i++) {
                for(int j=0; j<cols; j++) {
                    grid[i][j] = Symbol.EMPTY;
                }
            }
        }

        public boolean isValidMove(int x, int y) {
            return x >= 0 && x < rows && y >= 0 && y < cols && grid[x][y] == Symbol.EMPTY;
        }

        public void makeMove(int x, int y, Symbol symbol) {
            if(isValidMove(x, y)) {
                grid[x][y] = symbol;
            }
        }

        public Symbol getCell(int x, int y) {
            return grid[x][y];
        }

        publi void checkGameStatus(GameContext context) {
         // Check rows, columns, diagonals for a win
         // If win detected, set context state to XWonState or OWonState
         // If board is full without a win, set context state to DrawState
         // Otherwise, continue the game
        }
    }


    // tic tac toe game class 

    public class TicTacToe {
        private Player playerX;
        private Player playerO;
        private Board board;
        private GameContext gameContext;

        public TicTacToe(Player playerX, Player playerO) {
            this.playerX = playerX;
            this.playerO = playerO;
            this.board = new Board();
            this.gameContext = new GameContext();
        }

        public boolean makeMove(Player player, int x, int y) {
            // Validate turn by checking game state
            if((gameContext.getState() instanceof XTrunState && player.getSymbol() != Symbol.X) ||
               (gameContext.getState() instanceof OTurnState && player.getSymbol() != Symbol.O)) {
                return false; // Not this player's turn
            }

            // Validate and make move
            if(board.isValidMove(x, y)) {
                board.makeMove(x, y, player.getSymbol());
                board.checkGameStatus(gameContext);
                gameContext.nextState();
                return true;
            }
            return false; // Invalid move
        }

        // Get the current game state
        public GameState getState() {
            return gameState;
        }

        public void SwitchTurn() {
            if (currentTurn == playerX) {
                currentTurn = playerO;
            } else {
                currentTurn = playerX;
            }
        }

        public void announceResult() {
            if (gameContext.getState() instanceof XWonState) {
                System.out.println("Player X wins!");
            } else if (gameContext.getState() instanceof OWonState) {
                System.out.println("Player O wins!");
            } else {
                System.out.println("It's a draw!");
            }
        }
    }

    // main class to run the game

    public class Main {
        public static void main(String[] args) {
            Player playerX = new Player("Alice", Symbol.X);
            Player playerO = new Player("Bob", Symbol.O);
            TicTacToe game = new TicTacToe(playerX, playerO);

            // Simulate some moves
            game.makeMove(playerX, 0, 0); // X
            game.makeMove(playerO, 1, 1); // O
            game.makeMove(playerX, 0, 1); // X
            game.makeMove(playerO, 1, 0); // O
            game.makeMove(playerX, 0, 2); // X wins

            game.announceResult();
        }
    }