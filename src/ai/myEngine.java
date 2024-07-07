package ai;

import main.Board;
import main.Move;
import main.setting.ChoosePlayFormat;
import main.setting.SettingPanel;
import pieces.Piece;
import java.util.ArrayList;

public class myEngine {
    // board state:
    BoardState board;
    private String fen;

    // for the random engine:
    private ArrayList<Move> randomMovesList;
    public String promotionChoice;
    private final ArrayList<Piece> alreadyChecked = new ArrayList<>();

    // general parameters:
    public static int waitTime = 1000;
    private Thread thread;

    // for making move methods:
    public Piece chosenPiece;
    private static final int DEPTH = 2;

    // constructor:
    public myEngine(BoardState board) {
        this.board = board;
    }

    // making move methods
    public void makeMove(String fen, Board realBoard) {
        // Board.selectedPiece = null;
        thread = new Thread(() -> {
            try {
                Thread.sleep(waitTime);
                if (Thread.currentThread().isInterrupted()) {
                    return; // בדיקה אם ה-Thread הופסק
                }
            } catch (InterruptedException e) {
                return; // יציאה מה-Thread במקרה של הפסקה
            }
            this.fen = fen;
            Move move = chooseMethod();
            while (move == null) {
                move = chooseMethod();
            }
            Move tempMove = move;
            if (board.makeMoveToCheckIt(tempMove)) {
                realBoard.makeMove(move);
                //Board.input.selectedX = -1;
                //Board.input.selectedY = -1;
                alreadyChecked.clear();
            } else {
                System.out.println("retrying...");
                makeMove(fen, realBoard);
            }
        });
        thread.start();
    }

    private Move chooseMethod() {
        int skillLevel = SettingPanel.skillLevel;
        if (skillLevel == 0) {
            return getRandomMove();
        } else {
            return getBestMove();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    // random move methods
    private Move getRandomMove() {
        if (board == null) {
            System.out.println("BoardState is null");
            return null;
        }

        if (!board.checkScanner.isChecking(board)) {
            chosePiece();
        } else {
            for (int i = 0; i < board.getNumOfPieces(board.getIsWhiteToMove()); i++) {
                this.chosenPiece = board.getPieceByNumber(i, board.getIsWhiteToMove());
                if (chosenPiece.isWhite == board.getIsWhiteToMove()) {
                    this.randomMovesList = chosenPiece.getValidMoves(board);
                    if (!randomMovesList.isEmpty()) {
                        break;
                    }
                }
            }
        }
        if (randomMovesList == null || randomMovesList.isEmpty()) {
            System.out.println("No valid moves available");
            return null;
        }
        int randomNumOfMove = (int) (Math.random() * randomMovesList.size());
        Move randomMove = randomMovesList.get(randomNumOfMove);
        if (randomMove.piece.name.equals("Pawn") && board.getIsWhiteToMove() ? randomMove.newRow == 0 : randomMove.newRow == 7) {
            setPromotionChoice();
        } else {
            promotionChoice = null;
        }
        return randomMove;
    }

    private void setPromotionChoice() {
        int randomPromotionChoiceNum = (int) (Math.random() * 4);
        switch (randomPromotionChoiceNum) {
            case 1:
                promotionChoice = "r";
                break;
            case 2:
                promotionChoice = "b";
                break;
            case 3:
                promotionChoice = "n";
                break;
            default:
                promotionChoice = "q";
                break;
        }
    }

    private void chosePiece() {
        // for the random engine:
        int randomNumOfPiece = (int) (Math.random() * board.getNumOfPieces(board.getIsWhiteToMove()));
        this.chosenPiece = board.getPieceByNumber(randomNumOfPiece, board.getIsWhiteToMove());
        if (chosenPiece != null && !alreadyChecked.contains(chosenPiece)) {
            this.randomMovesList = chosenPiece.getValidMoves(board);
            if (randomMovesList.isEmpty()) {
                alreadyChecked.add(chosenPiece);
                chosePiece();
            }
        } else {
            chosePiece();
        }
    }

    // other engine methods
    private Move getBestMove() {
        Move bestMove = Minimax.getBestMove(board, SettingPanel.skillLevel/2);
        return bestMove;
    }

    // board setter
    public void setBoard(BoardState state) {
        this.board = state;
    }
}
