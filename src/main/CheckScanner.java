package main;

import ai.BoardState;
import pieces.Piece;

public class CheckScanner {

    BoardState board;

    public CheckScanner(BoardState board) {
        this.board = board;
    }

    public boolean isMoveCausesCheck(Move move) {

        Piece king = board.findKing(board.getIsWhiteToMove());
        assert king != null;

        int kingCol = king.col;
        int kingRow = king.row;

        if (move.piece.name.equals("King")) {
            kingCol = move.newCol;
            kingRow = move.newRow;
        }

        return  hitByKnight(move.newCol, move.newRow, king, kingCol, kingRow) ||
                hitByPawn  (move.newCol, move.newRow, king, kingCol, kingRow) ||
                hitByKing  (king, kingCol, kingRow) ||

                hitByRook  (move.newCol, move.newRow, king, kingCol, kingRow, 0,1) || // up
                hitByRook  (move.newCol, move.newRow, king, kingCol, kingRow, 1,0) || // right
                hitByRook  (move.newCol, move.newRow, king, kingCol, kingRow, 0, -1) || // down
                hitByRook  (move.newCol, move.newRow, king, kingCol, kingRow, -1, 0) || // left

                hitByBishop(move.newCol, move.newRow, king, kingCol, kingRow, -1,-1) || // up left
                hitByBishop(move.newCol, move.newRow, king, kingCol, kingRow,1,-1) || // up right
                hitByBishop(move.newCol, move.newRow, king, kingCol, kingRow,1,1) || // down right
                hitByBishop(move.newCol, move.newRow, king, kingCol, kingRow,-1,1); // down left
    }

    private boolean hitByRook(int col, int row, Piece king, int kingCol, int kingRow, int colVal, int rowVal) {
        for (int i = 1; i <= 8; i++) {
            if (kingCol + (i * colVal) == col && kingRow + (i * rowVal) == row) {
                break;
            }
            Piece piece = board.getPiece(kingCol + (i * colVal), kingRow + (i *rowVal));
            if (piece != null && piece != Board.selectedPiece) {
                if ((!board.sameTeam(piece, king)) && (piece.name.equals("Rook") || piece.name.equals("Queen"))) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private boolean hitByBishop(int col, int row, Piece king, int kingCol, int kingRow, int colVal, int rowVal) {
        for (int i = 1; i <= 8; i++) {
            if (kingCol - (i * colVal) == col && kingRow - (i *rowVal) == row) {
                break;
            }
            Piece piece = board.getPiece(kingCol - (i * colVal), kingRow - (i *rowVal));
            if (piece != null && piece != Board.selectedPiece) {
                if ((piece.name.equals("Bishop") || piece.name.equals("Queen")) && !(board.sameTeam(piece, king))) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private boolean hitByKnight(int col, int row, Piece king, int kingCol, int kingRow) {
        return  checkKnight(board.getPiece(kingCol - 1, kingRow - 2), king, col, row) ||
                checkKnight(board.getPiece(kingCol + 1, kingRow - 2), king, col, row) ||
                checkKnight(board.getPiece(kingCol + 2, kingRow - 1), king, col, row) ||
                checkKnight(board.getPiece(kingCol + 2, kingRow + 1), king, col, row) ||
                checkKnight(board.getPiece(kingCol + 1, kingRow + 2), king, col, row) ||
                checkKnight(board.getPiece(kingCol - 1, kingRow + 2), king, col, row) ||
                checkKnight(board.getPiece(kingCol - 2, kingRow + 1), king, col, row) ||
                checkKnight(board.getPiece(kingCol - 2, kingRow - 1), king, col, row);
    }

    private boolean checkKnight(Piece p, Piece k, int col, int row) {
        return p != null && !board.sameTeam(p, k) && p.name.equals("Knight") && !(p.col == col && p.row == row);
    }

    private boolean hitByKing (Piece king, int kingCol, int kingRow) {
        return  checkKing(board.getPiece(kingCol - 1, kingRow - 1), king) ||
                checkKing(board.getPiece(kingCol + 1, kingRow - 1), king) ||
                checkKing(board.getPiece(kingCol, kingRow - 1), king) ||
                checkKing(board.getPiece(kingCol - 1, kingRow), king) ||
                checkKing(board.getPiece(kingCol + 1, kingRow), king) ||
                checkKing(board.getPiece(kingCol - 1, kingRow + 1), king) ||
                checkKing(board.getPiece(kingCol + 1, kingRow + 1), king) ||
                checkKing(board.getPiece(kingCol, kingRow + 1), king);
    }

    private boolean checkKing(Piece p, Piece k) {
        return p != null && !board.sameTeam(p, k) && p.name.equals("King");
    }

    private boolean hitByPawn(int col, int row, Piece king, int kingCol, int kingRow) {
        int colorVal = king.isWhite ? -1 : 1;
        return checkPawn(board.getPiece(kingCol + 1, kingRow + colorVal), king, col, row) || checkPawn(board.getPiece(kingCol - 1, kingRow + colorVal), king, col, row);
    }

    private boolean checkPawn(Piece p, Piece k, int col, int row) {
        return p != null && !board.sameTeam(p, k) && p.name.equals("Pawn")  && !(p.col == col && p.row == row);
    }

    public boolean isGameOver(Piece king) {
        if (king == null) return false;
        for (Piece piece : board.getAllPieces()) {
            if (board.sameTeam(piece, king)) {
                Board.selectedPiece = piece == king ? king : null;
                for (int row = 0; row < Board.rows; row++) {
                    for (int col = 0; col < Board.cols; col++) {
                        Move move = new Move(board, piece, col, row);
                        if (board.isValidMove(move)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isChecking(BoardState board) {
        Piece king = board.findKing(board.getIsWhiteToMove());
        int kingCol = king.col;
        int kingRow = king.row;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board.getPiece(i, j) != null) {
                    Piece piece = board.getPiece(i, j);
                    if (piece.isWhite != board.getIsWhiteToMove()) {
                        if (!board.sameTeam(piece, king)) {
                            if (piece.isValidMovement(kingCol, kingRow)) {
                                if (!piece.moveCollidesWithPiece(kingCol, kingRow)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isCheckingForClone(BoardState board) {
        Piece king = board.findKing(!board.getIsWhiteToMove());
        int kingCol = king.col;
        int kingRow = king.row;
        //System.out.println(king + " " + kingCol + ", " + kingRow);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                //System.out.print("1");
                if (board.getPiece(i, j) != null) {
                    //System.out.print("2");
                    Piece piece = board.getPiece(i, j);
                    if (piece.isWhite == board.getIsWhiteToMove()) {
                        //System.out.print("3" + new Move(board, piece, kingCol, kingRow));
                        if (piece.isValidMovement(kingCol, kingRow)) {
                            //System.out.print("4");
                            if (!piece.moveCollidesWithPiece(kingCol, kingRow)) {
                                //System.out.println("5");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        //System.out.println(" ");
        return false;
    }

    public boolean isCheckingForEvaluation(BoardState board) {
        Piece piece;
        Piece king = board.findKing(board.getIsWhiteToMove());
        if (king == null) {
            System.out.println("null!!!!!!!!!!!!!!"); return false; }
        int kingCol = king.col;
        int kingRow = king.row;
        //System.out.println(king + " " + kingCol + ", " + kingRow);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                //System.out.print("1");
                if ((piece = board.getPiece(i, j)) != null) {
                    //System.out.print("2");
                    if (piece.isWhite != board.getIsWhiteToMove()) {
                        // System.out.print("3" + new Move(board, piece, kingCol, kingRow));
                        if (piece.isValidMovement(kingCol, kingRow)) {
                            // System.out.print("4");
                            if (!piece.moveCollidesWithPiece(kingCol, kingRow)) {
                                // System.out.println("5");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        // System.out.println(" ");
        return false;
    }


}
