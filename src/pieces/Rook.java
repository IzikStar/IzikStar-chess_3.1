package pieces;

import ai.BoardState;
import main.Board;
import main.setting.ChoosePlayFormat;

import java.awt.image.BufferedImage;

public class Rook extends Piece{
    public Rook(BoardState board, int col, int row, boolean isWhite) {
        super(board);
        this.col = col;
        this.row = row;
        if (ChoosePlayFormat.isPlayingWhite) {
            this.xPos = col * Board.tileSize;
            this.yPos = row * Board.tileSize;
        }
        else {
            this.xPos = (7 - col) * Board.tileSize;
            this.yPos = (7 - row) * Board.tileSize;
        }

        this.isWhite = isWhite;
        this.name = "Rook";
        this.type = isWhite ? 'R' : 'r';
        this.value = 5;

        this.sprite = sheet.getSubimage(4 * sheetScale, isWhite ? 0 : sheetScale, sheetScale, sheetScale).getScaledInstance(Board.tileSize, Board.tileSize, BufferedImage.SCALE_SMOOTH);
    }

    public boolean isValidMovement(int col, int row) {
        return this.col == col || this.row == row;
    }

    public boolean moveCollidesWithPiece(int col, int row) {

        // left
        if (this.col > col) {
            for (int c = this.col - 1; c > col; c--) {
                if (board.getPiece(c, this.row) != null) {
                    return true;
                }
            }
        }

        // right
        if (this.col < col) {
            for (int c = this.col + 1; c < col ; c++) {
                if (board.getPiece(c, this.row) != null) {
                    return true;
                }
            }
        }

        // up
        if (this.row > row) {
            for (int r = this.row - 1; r > row ; r--) {
                if (board.getPiece(this.col, r) != null) {
                    return true;
                }
            }
        }

        // down
        if (this.row < row) {
            for (int r = this.row + 1; r < row ; r++) {
                if (board.getPiece(this.col, r) != null) {
                    return true;
                }
            }
        }




        return false;
    }


}
