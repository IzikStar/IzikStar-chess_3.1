package pieces;

import ai.BoardState;
import main.Move;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class Piece {

    public int col, row;
    public int xPos, yPos;

    public boolean isWhite;
    public String name;
    public char type;
    public  double value;
    public boolean isFirstMove = true;

    BufferedImage sheet;
    {
        try {
            sheet = ImageIO.read(ClassLoader.getSystemResourceAsStream("pieces.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected int sheetScale = sheet.getWidth()/6;

    Image sprite;

    BoardState board;

    public Piece(BoardState board) {
        this.board = board;
    }

    public void paint(Graphics2D g2d) {
        g2d.drawImage(sprite, xPos, yPos, null);
    }

    public boolean isValidMovement(int col, int row) {return true;}

    public boolean moveCollidesWithPiece(int col, int row) {return false;}

    public ArrayList<Move> getValidMoves(BoardState board) {
        ArrayList<Move> validMoves = new ArrayList<>();
        //System.out.println(chosenPiece);
        for (int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                //System.out.println("c: " + c + " r: " + r);
                Move move = new Move(board, this, c, r);
                if (board.isValidMove(move)) {
                    validMoves.add(move);
                }
            }
        }
        return validMoves;
    }

    public char getRepresentation() {
        return type;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

}
