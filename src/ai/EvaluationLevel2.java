package ai;

import main.Move;
import main.setting.ChoosePlayFormat;
import pieces.Piece;

import java.util.ArrayList;

public class EvaluationLevel2 {

    public static double evaluate(BoardState board) {
        double score = 0;
        if (board.getStatus() == 0) {
            if (board.getIsCheck()) {
                score = board.getIsWhiteToMove() != ChoosePlayFormat.isPlayingWhite ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }
            // staleMate. score = 0;
        }
        else {
            for (Piece piece : board.getAllPieces()) {
                double pieceValue = getPieceValue(piece);
                boolean isPlayerPiece = piece.isWhite == ChoosePlayFormat.isPlayingWhite;
                ArrayList<Move> validMoves;
                validMoves = piece.getValidMoves(board);

                // חישוב ערך בסיסי של הכלי
                score += isPlayerPiece ? -pieceValue : pieceValue;

                // בטיחות המלך
                if (piece.name.equals("King") && piece.row == (piece.isWhite ? 7 : 0)) {
                    double kingSafetyBonus = 1.25;
                    if (piece.col == 6 || piece.col == 1) {
                        kingSafetyBonus += 1.25;
                    } else if (piece.col == 2) {
                        kingSafetyBonus += 0.75;
                    }
                    score += isPlayerPiece ? -kingSafetyBonus : kingSafetyBonus;
                }

                // חישוב פיתוח ראשוני
                if ((piece.name.equals("Knight") || piece.name.equals("Bishop")) && piece.row == (piece.isWhite ? 7 : 0)) {
                    score += isPlayerPiece ? 1.05 : -1.05;
                }

                // חישוב פיתוח מתקדם
                double moveScoreFactor = 0.02 * (piece.name.equals("King") ? 0 : pieceValue);
                score += isPlayerPiece ? -validMoves.size() * moveScoreFactor : validMoves.size() * moveScoreFactor;

                // חישוב תוצאות מהלכים אפשריים
                for (Move move : validMoves) {
                    double moveScore = calculateMoveScore(board, move, pieceValue);
                    score += isPlayerPiece ? -moveScore : moveScore;
                }

                // הערכת רגלי
                if (piece.name.equals("Pawn")) {
                    double pawnEvaluation = evaluatePawn(board, piece);
                    score += isPlayerPiece ? -pawnEvaluation : pawnEvaluation;
                }
            }
        }
        return score;
    }

    private static double calculateMoveScore(BoardState board, Move move, double pieceValue) {
        if (move.captured == null) {
            return 0;
        }

        double moveScore = 0;
        double capturedValue = getPieceValue(move.captured);
        boolean sameTeam = board.sameTeam(move.piece, move.captured);

        if (!sameTeam && !move.captured.name.equals("King")) {
            // אם הכלים הם מאותו סוג האיום הוא דו צדדי ולכן המשמעות היא שבמצב הנוכחי הכלי הנבדק כנראה יאכל בתור הבא
            if (move.piece.name.equals(move.captured.name)) {
                moveScore += pieceValue * 0.8;
            }
            else {
                double threatValue = capturedValue * 0.1 - pieceValue * 0.05;
                if (threatValue > 0) {
                    moveScore += threatValue;
                }
            }
        } else if (!sameTeam) {
            moveScore += 1.5;
        } else {
            double defendingValue = capturedValue * 0.1 - pieceValue * 0.05;
            if (defendingValue > 0) {
                moveScore += defendingValue;
            }
        }

        return moveScore;
    }

    private static double evaluatePawn(BoardState board, Piece pawn) {
        double evaluation = 0;
        int promotionRow = pawn.isWhite ? 0 : 7;
        int direction = pawn.isWhite ? -1 : 1;

        // קידום רגלי
        evaluation += (7 - Math.abs(pawn.row - promotionRow)) * 0.3;

        // נתיב פתוח להכתרה
        boolean pathOpen = true;
        for (int i = pawn.row + direction; i != promotionRow; i += direction) {
            if (board.getPiece(pawn.col, i) != null) {
                pathOpen = false;
                break;
            }
        }
        if (pathOpen) {
            evaluation += 0.6;
        }

        // מיקום במרכז
        if (pawn.col > 2 && pawn.col < 5) {
            int centering = 7 - Math.abs(pawn.row - promotionRow);
            evaluation += 0.1 * centering;
        }

        // רגלי מוגן
        if ((pawn.col > 0 && board.getPiece(pawn.col - 1, pawn.row) != null && board.getPiece(pawn.col - 1, pawn.row).name.equals("Pawn") && board.sameTeam(pawn, board.getPiece(pawn.col - 1, pawn.row))) ||
                (pawn.col < 7 && board.getPiece(pawn.col + 1, pawn.row) != null && board.getPiece(pawn.col + 1, pawn.row).name.equals("Pawn") && board.sameTeam(pawn, board.getPiece(pawn.col + 1, pawn.row)))) {
            evaluation += 0.2;
        }

        return evaluation;
    }


    private static int getPieceValue(Piece piece) {
        return (int) piece.value;
    }

}
