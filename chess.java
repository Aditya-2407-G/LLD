/*
 * Chess game that -
 * Supports a standard 8x8 chessboard.
 * allows 2 players to play against each other in alternate turns.
 * provides basic move validation for each piece.
 * detects check and checkmate conditions.
 */

 /*Key components of the chess game */
/* 
 Piece -> Represent the different chess pieces (King, Queen, Rook, Bishop, Knight, Pawn)
 Board -> Represent the 8x8 on which the game is played
 Player -> Represent the players in the game (White and Black) control their pieces
*/

/*
 1) WE have to manage the game states -> reflect the current state of the game, player and board status
 2) Implement move validation -> follow the logic of the game 
 3) Tracking the turns -> alternate between players after each valid move
 4) Check and Checkmate detection -> determine if a player's king is in check or checkmate
 */

 /*
 1) Strategy for piece movement 
 2)Single instane of Board 
 3) Piece Creation 
 4) Game state management
  */

 public class Player {

    private String name; 
    private boolean isWhite; 

    public Player(String name, boolean isWhite) {
        this.name = name; 
        this.isWhite = isWhite;
    }

    public String getName() {
        return name;
    }
    public boolean isWhite() {
        return isWhite;
    }

 }


 public enum Status {
    ACTIVE,
    IN_CHECK,
    CHECKMATE,
    STALEMATE,
    WHITE_WINS,
    BLACK_WINS,
    DRAW
 }


 // Abstract Piece class - abstract because different pieces have different movement rules

 public abstract class Piece {

    private boolean isWhite;
    private boolean killed = false; // if piece is captured

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public boolean isKilled() {
        return killed;
    }

    public void setKilled(boolean killed) {
        this.killed = killed;
    }

 }

 // Specific piece classes

 public class King extends Piece {
    public King(boolean isWhite) {
        super(isWhite);
    }
 }
    public class Queen extends Piece {
        public Queen(boolean isWhite) {
            super(isWhite);
        }
    }
    public class Rook extends Piece {
        public Rook(boolean isWhite) {
            super(isWhite);
        }
    }

    // Piece factory for creating pieces

    public class PieceFactory {

        public static Piece createPiece(String type, boolean isWhite) {
            switch (type.toLower()) {
                case "king" :
                    return new King(isWhite);
                case "queen" :
                    return new Queen(isWhite);

                    /// Other pieces like Rook, Bishop, Knight, Pawn can be added here
                default:
                    throw new IllegalArgumentException("Invalid piece type");
            }
        }
    }

    // For creating the board, we will first divide the board into cells
    // each board is represented by instance of cell having the positon and the piece on it


    public class Cell {
        private int x: 
        private int y;
        private Piece piece;

        public Cell(int x, int y, Piece piece) {
            this.x = x;
            this.y = y;
            this.piece = piece;
        }
        // getter for piece
        public Piece getPiece() {
            return piece;
        }
        // setter for piece
        public void setPiece(Piece piece) {
            this.piece = piece;
        }
    }


    // Move -> has all the details for move made in the game

    public class Move {
        private Player player;
        private Cell from;
        private Cell to;
        private Piece pieceMoved;
        private Piece pieceKilled;

        public Move(Player player, Cell from, Cell to, Piece pieceMoved, Piece pieceKilled) {
            this.player = player;
            this.from = from;
            this.to = to;
            this.pieceMoved = pieceMoved;
            this.pieceKilled = pieceKilled;
        }
    }

    // Board class -> singleton class representing the chessboard

    public class Board {
        private static Board instance = null;
        private Cell[][] cells;

        private Board() {
            cells = new Cell[8][8];
            // Initialize the board with pieces at starting positions
        }

        public static Board getInstance() {
            if (instance == null) {
                instance = new Board();
            }
            return instance;
        }

        public Cell getCell(int x, int y) {
            return cells[x][y];
        }
    }

    // Game class -> manages the overall game state

    public class Game {
        private Player whitePlayer;
        private Player blackPlayer;
        private Board board;
        private Player currentTurn;
        private Status status;

        public Game(Player whitePlayer, Player blackPlayer) {
            this.whitePlayer = whitePlayer;
            this.blackPlayer = blackPlayer;
            this.board = Board.getInstance();
            this.currentTurn = whitePlayer; // White starts first
            this.status = Status.ACTIVE;
        }

        public boolean makeMove(Move move) {
            // Validate move
            // Update board state
            // Check for check/checkmate
            // Switch turns
            return true; // return true if move is successful
        }
    }

    // check and checkmate detection logic would be implemented in the Game class
    private boolean isInCheck(Player player) {
        // Logic to determine if the player's king is in check
    }

    private boolean isCheckmate(Player player) {
        // Logic to determine if the player is in checkmate
    }
