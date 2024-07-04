package main;

// מחלקות שלי
import GUI.AudioPlayer;
import GUI.ChessAnimation;
import ai.BoardState;
import main.setting.ChoosePlayFormat;
import main.setting.SettingPanel;
import pieces.*;
// מחלקות של ג'אווה
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class Board extends JPanel {

    // משתנים לציור הלוח
    JFrame parentFrame;
    public static String fenStartingPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static int tileSize = 85;
    public static int cols = 8;
    public static int rows = 8;


    BoardState state;
    // משתנים של מצב הלוח
    public static Piece selectedPiece;

    public boolean isGameOver = false;
    // כללי יותר
    public Stack<String> savedStates = new Stack<>();

    // משתנים לטיפול באינפוט וGUI
    public Input input = new Input(this);
    AudioPlayer audioPlayer = new AudioPlayer();
    private ChessAnimation animation;
    private final java.util.List<ChessAnimation> animations = new ArrayList<>();
    ShowScore showScore = new ShowScore(this);

    // הצגת רמזים
    int hintFromC = -1, hintFromR = -1, hintToC = -1, hintToR = -1;

    // constructor
    public Board() {
        this.setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));
        this.addMouseListener(input);
        this.addMouseMotionListener(input);

        this.savedStates.push(fenStartingPosition);

        this.state = new BoardState(fenStartingPosition, null);
        loadPiecesFromFen(state.fenCurrentPosition);

        Timer animationTimer = new Timer(5, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (animation != null && animation.isFinished()) {
                    animation = null;
                }
                repaint();
            }
        });
        animationTimer.start();
    }

    // פונקציות צביעה
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // paint board
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                g2d.setColor((c + r) % 2 != 0 ? new Color(152, 97, 42) : new Color(208, 182, 164));
                g2d.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
            }
        }

        // paint last move
        if (state.getLastMove() != null) {
            g.setColor(new Color(72, 255, 0, 158));
            g.fillRect(getXFromCol(state.getLastMove().piece.col), getYFromRow(state.getLastMove().piece.row), tileSize, tileSize);
            g.setColor(new Color(55, 255, 0, 186));
            g.fillRect(getXFromCol(state.getLastMove().newCol), getYFromRow(state.getLastMove().newRow), tileSize, tileSize);
        }

        // paint engine hint
        g.setColor(new Color(0, 255, 215, 158));
        g.fillRect(getXFromCol(hintFromC), getYFromRow(hintFromR), tileSize, tileSize);
        g.setColor(new Color(47, 206, 255, 237));
        g.fillRect(getXFromCol(hintToC), getYFromRow(hintToR), tileSize, tileSize);

        // paint the border of the king red if it's under attack
        if (state.findKing(state.getIsWhiteToMove()) != null && state.checkScanner.isChecking(state)) {
            Piece king = state.findKing(state.getIsWhiteToMove());
            drawSquareWithCircle(g ,getXFromCol(king.col) / tileSize, getYFromRow(king.row) / tileSize);
        }

        // paint highLights
        if (selectedPiece != null) {
            if (selectedPiece.isWhite == state.getIsWhiteToMove()) {
                g2d.setColor(new Color(0, 0, 255, 128)); // כחול חצי שקוף
                if (ChoosePlayFormat.isPlayingWhite) {
                    g2d.fillRect(selectedPiece.col * tileSize, selectedPiece.row * tileSize, tileSize, tileSize);
                }
                else {
                    g2d.fillRect((cols - 1 - selectedPiece.col) * tileSize, (rows - 1 - selectedPiece.row) * tileSize, tileSize, tileSize);
                }
                if (state.checkScanner.isChecking(state)) {
                    Piece king = state.findKing(state.getIsWhiteToMove());
                    //g2d.setColor(new Color(255, 0, 0, 237)); // אדום חצי שקוף
                    //g2d.fillRect(king.col * tileSize, king.row * tileSize, tileSize, tileSize);
                    drawSquareWithCircle(g ,getXFromCol(king.col) / tileSize, getYFromRow(king.row) / tileSize);
                }
            }

            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    if (state.isValidMove(new Move(state, selectedPiece, c, r))) {
                        if (state.getPiece(c, r) == null) {
                            // ציור עיגול במרכז הריבוע
                            g.setColor(new Color(72, 255, 0, 158));
                            int diameter = tileSize / 3;
                            int circleX;
                            int circleY;
                            if (ChoosePlayFormat.isPlayingWhite) {
                                circleX = c * tileSize + (tileSize - diameter) / 2;
                                circleY = r * tileSize + (tileSize - diameter) / 2;
                            }
                            else {
                                circleX = (cols - 1 - c) * tileSize + (tileSize - diameter) / 2;
                                circleY = (rows - 1 - r) * tileSize + (tileSize - diameter) / 2;
                            }
                            g.fillOval(circleX, circleY, diameter, diameter);
                        } else {
                            drawSquareWithCircle(g, getXFromCol(c) / tileSize, getYFromRow(r) / tileSize);
                        }
                    }
                }
            }
        }

        Iterator<ChessAnimation> it = animations.iterator();
        while (it.hasNext()) {
            ChessAnimation animation = it.next();
            if (!animation.isFinished()) {
                animation.paint(g2d);
            } else {
                it.remove();
            }
        }

        // paint pieces
        for (Piece piece : state.getAllPieces()) {
            piece.paint(g2d);
        }
    }

    private void drawSquareWithCircle(Graphics g, int col, int row) {
        int x = col * tileSize;
        int y = row * tileSize;
        int borderThickness = 5;

        g.setColor(Color.RED);
        g.fillRect(x, y, tileSize, borderThickness); // Top
        g.fillRect(x, y, borderThickness, tileSize); // Left
        g.fillRect(x + tileSize - borderThickness, y, borderThickness, tileSize); // Right
        g.fillRect(x, y + tileSize - borderThickness, tileSize, borderThickness); // Bottom
    }
    // טיפול בהפיכת הלוח
    public int getXFromCol(int col){
        if (ChoosePlayFormat.isPlayingWhite) {
            return col * tileSize;
        }
        else {
            return (cols - 1 - col) * tileSize;
        }
    }
    public int getYFromRow(int row){
        if (ChoosePlayFormat.isPlayingWhite) {
            return row * tileSize;
        }
        else {
            return (rows - 1 - row) * tileSize;
        }
    }
    public int getColFromX(int x){
        if (ChoosePlayFormat.isPlayingWhite) {
            return x / tileSize;
        }
        else {
            return (cols - 1) - x / tileSize;
        }
    }
    public int getRowFromY(int y){
        if (ChoosePlayFormat.isPlayingWhite) {
            return y / tileSize;
        }
        else {
            return (rows - 1) - y / tileSize;
        }
    }


    // פונקציות לשינוי מצב הלוח וביצוע מהלכים
    public void loadPiecesFromFen(String fenCurrentPosition) {
        state.loadPiecesFromFen(fenCurrentPosition);
        repaint();
    }


    public void makeMove(Move move, String promotionChoice) {
        state.makeMove(move, promotionChoice);

        Piece piece = state.getPiece(move.piece.col, move.piece.row);

        if (move.captured != null && state.getPiece(move.captured.col, move.captured.row) != null) {
            audioPlayer.playCaptureSound();
        } else {
            audioPlayer.playMovingPieceSound();
        }

        ChessAnimation moveAnimation;
        if (ChoosePlayFormat.isPlayingWhite) {
            moveAnimation = new ChessAnimation(piece, piece.col * tileSize, piece.row * tileSize,
                    move.newCol * tileSize, move.newRow * tileSize, 500);
        } else {
            moveAnimation = new ChessAnimation(piece, (cols - 1 - piece.col) * tileSize, (rows - 1 - piece.row) * tileSize,
                    (cols - 1 - move.newCol) * tileSize, (rows - 1 - move.newRow) * tileSize, 500);
        }
        animations.add(moveAnimation);

        piece.xPos = getXFromCol(move.newCol);
        piece.yPos = getYFromRow(move.newRow);
        updateGameState(true);

        if (ChoosePlayFormat.isPlayingWhite == state.getIsWhiteToMove() || !ChoosePlayFormat.isOnePlayer) {
            savedStates.push(state.fenCurrentPosition);
            state.fenCurrentPosition = state.convertPiecesToFEN();
        }
        showScore.calculateScore();
    }

    public void moveKing(Move move) {
        if (Math.abs(move.piece.col - move.newCol) == 2) {
            Piece rook;
            int rookEndCol;
            if (move.piece.col < move.newCol) {
                rook = state.getPiece(7, move.piece.row);
                rookEndCol = 5;
            } else {
                rook = state.getPiece(0, move.piece.row);
                rookEndCol = 3;
            }

            // הוספת אנימציה להצרחה גם למלך וגם לצריח
            int kingStartX = move.piece.xPos;
            int kingStartY = move.piece.yPos;
            int kingEndX = getXFromCol(move.newCol);
            int kingEndY = getYFromRow(move.newRow);

            int rookStartX = rook.xPos;
            int rookStartY = rook.yPos;
            int rookEndX = getXFromCol(rookEndCol);
            int rookEndY = getYFromRow(move.newRow);

            animations.add(new ChessAnimation(move.piece, kingStartX, kingStartY, kingEndX, kingEndY, 500));
            animations.add(new ChessAnimation(rook, rookStartX, rookStartY, rookEndX, rookEndY, 500));

            move.piece.col = move.newCol;
            move.piece.row = move.newRow;
            rook.col = rookEndCol;
            rook.row = move.newRow;

            move.piece.xPos = kingEndX;
            move.piece.yPos = kingEndY;
            rook.xPos = rookEndX;
            rook.yPos = rookEndY;

            audioPlayer.playCastlingSound();
        }
    }

    public boolean movePawn(Move move) {

        // promotions:
        int colorIndex = move.piece.isWhite ? 0 : 7;
        if (move.newRow == colorIndex) {
            if (!ChoosePlayFormat.isOnePlayer || ChoosePlayFormat.isPlayingWhite == state.getIsWhiteToMove()) { // כאן אמור להיות אם זה שני שחקנים
                if(!promotePawn(move)) {
                    return false;
                }
            }
            else {
                if (SettingPanel.skillLevel != 0) {
                    state.promotePawnTo(move, input.engine.promotionChoice);
                }
                else {
                    state.promotePawnTo(move, input.randomMoveEngine.promotionChoice);
                }
                state.capture(move.piece);
            }
        }
        state.numOfTurnWithoutCaptureOrPawnMove = -1;
        return true;
    }

    private boolean promotePawn(Move move) {
        PromotionDialog promotionDialog = new PromotionDialog(parentFrame, move.piece.isWhite);
        String choice = promotionDialog.getSelection();
        if (choice != null) {
            state.promotePawnTo(move, choice);
            state.capture(move.piece);

            // הוספת אנימציה להכתרת רגלי
            animation = new ChessAnimation(move.piece, move.piece.xPos, move.piece.yPos, move.piece.xPos, move.piece.yPos, 500);
        } else {
            System.out.println("No selection made");
            selectedPiece.xPos = getXFromCol(selectedPiece.col);
            selectedPiece.yPos = getYFromRow(selectedPiece.row);
            selectedPiece = null;
            repaint();
            input.selectedX = -1;
            input.selectedY = -1;
            return false;
        }
        return true;
    }


    // שינוי מצב הלוח שלא באמצעות ביצוע מהלך
    public void goBack() {
        if(!ChoosePlayFormat.isOnePlayer || ChoosePlayFormat.isPlayingWhite == state.getIsWhiteToMove()) {
            //System.out.println("current position: " + fenCurrentPosition);
            state.fenCurrentPosition = savedStates.pop();
            //System.out.println("changing position to: " + fenCurrentPosition);
            input.engine.stopEngine();
            state.setLastMove(null);
            input.selectedX = input.selectedY = -1;
            selectedPiece = null;
            audioPlayer.playGoBackSound();
            loadPiecesFromFen(state.fenCurrentPosition);
            if (ChoosePlayFormat.isOnePlayer && ChoosePlayFormat.isPlayingWhite != state.getIsWhiteToMove()) {
                input.makeEngineMove();
            }
        }
        else {
            System.out.println("doing nothing");
        }
    }

    public void restart() {
        input.randomMoveEngine.stop();
        isGameOver = false;
        input.isStatusChanged = false;
        state.setLastMove(null);
        hintFromC = -1; hintFromR = -1; hintToC = -1; hintToR = -1;
        Main.updateScores(0, 0);
        audioPlayer.playHintSound();
        if (ChoosePlayFormat.isOnePlayer) {
            SettingPanel.changeIsPlayingWhiteText();
            loadPiecesFromFen(fenStartingPosition);
            if (ChoosePlayFormat.isPlayingWhite != state.getIsWhiteToMove()) {
                input.makeEngineMove();
            }
        }
        else {
            ChoosePlayFormat.isPlayingWhite = true;
            SettingPanel.changeIsPlayingWhiteText();
            loadPiecesFromFen(fenStartingPosition);
        }
    }

    public void refresh() {
        savedStates.push(state.fenCurrentPosition);
        state.fenCurrentPosition = state.convertPiecesToFEN();
        loadPiecesFromFen(state.fenCurrentPosition);
        if (ChoosePlayFormat.isOnePlayer && ChoosePlayFormat.isPlayingWhite != state.getIsWhiteToMove()) {
            input.makeEngineMove();
        }
    }

    public void updateGameState(boolean isRealBoard) {
        Piece king = state.findKing(state.getIsWhiteToMove());
        if (state.checkScanner.isGameOver(king)) {
            if (state.checkScanner.isChecking(state)) {
                // System.out.println(isWhiteToMove ? "black wins!" : "white wins!");
                if (isRealBoard){
                    input.isStatusChanged = true;
                    input.isCheckMate = true;
                    input.isStaleMate = false;
                    input.isWhiteTurn = state.getIsWhiteToMove();
                    if (ChoosePlayFormat.isOnePlayer && ChoosePlayFormat.isPlayingWhite == state.getIsWhiteToMove()) {
                        audioPlayer.playLosingSound();
                    }
                    else {
                        audioPlayer.playCheckMateSound();
                    }
                    animation = new ChessAnimation(king, king.xPos, king.yPos, king.xPos, king.yPos, 500);
                }
            } else {
                // System.out.println("stale mate! draw!");
                if (isRealBoard){
                    input.isStatusChanged = true;
                    input.isCheckMate = false;
                    input.isStaleMate = true;
                    input.isWhiteTurn = state.getIsWhiteToMove();
                    audioPlayer.playDrawSound();
                }
            }
//            isGameOver = true;
        } else if (state.insufficientMaterial(true) && state.insufficientMaterial(false)) {
            // System.out.println("insufficientMaterial! draw!");
//            isGameOver = true;
            if (isRealBoard){
                input.isStatusChanged = true;
                input.isCheckMate = false;
                input.isStaleMate = false;
                input.isWhiteTurn = state.getIsWhiteToMove();
                audioPlayer.playDrawSound();
            }
        }
        else if (state.checkScanner.isChecking(state)) {
            audioPlayer.playCheckSound();
            repaint();
        }
    }


    // גטרים למצב הלוח
    public String squareToLetters(int col, int row) {
        String namesOfRows = "87654321";
        String namesOfCols = "abcdefgh";
        // System.out.println(col + " " + row + " " + squareName);
        return String.valueOf(namesOfCols.charAt(col)) +
                namesOfRows.charAt(row);
    }

}

