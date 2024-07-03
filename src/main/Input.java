package main;

import GUI.AudioPlayer;
import ai.Level2Engine;
import ai.RandomMoveEngine;
import main.setting.ChoosePlayFormat;
import main.setting.SettingPanel;
import pieces.Piece;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import ai.StockfishEngine;

public class Input extends MouseAdapter {

    Board board;
    private boolean isDragged = false;
    public int selectedX = -1, selectedY = -1;
    public boolean isStatusChanged = false, isCheckMate = false, isStaleMate = false, isWhiteTurn;
    public int col, row;
    AudioPlayer audioPlayer = new AudioPlayer();

    String pathToStockfish = "src/res/stockfish/stockfish-windows-x86-64.exe";
    StockfishEngine engine;
    RandomMoveEngine randomMoveEngine;
    Level2Engine level2Engine;

    public Input(Board board) {
        this.board = board;
        engine = new StockfishEngine();
        randomMoveEngine = new RandomMoveEngine(board);
        level2Engine = new Level2Engine(board);
        if (!ChoosePlayFormat.isPlayingWhite) {
            makeEngineMove();
        }
    }

    public void makeEngineMove() {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            engine.setSkillLevel(SettingPanel.skillLevel - 1);
            boolean moveFound = false;
            long endTime = System.currentTimeMillis();
            while (!(moveFound) && endTime - startTime < 1500) {
                engine.setSkillLevel(ChoosePlayFormat.setSkillLevel);
                String fen = board.convertPiecesToFEN();
                // System.out.println("Current FEN: " + fen);
                String bestMove = engine.getBestMove(fen);
                // System.out.println("Best move: " + bestMove);

                if (bestMove != null && !bestMove.equals("unknown")) {
                    int fromCol = bestMove.charAt(0) - 'a';
                    int fromRow = 8 - (bestMove.charAt(1) - '0');
                    int toCol = bestMove.charAt(2) - 'a';
                    int toRow = 8 - (bestMove.charAt(3) - '0');
                    if (bestMove.length() > 4) {
                        engine.promotionChoice = String.valueOf(bestMove.charAt(4));
                    }
                    // System.out.println("From: " + fromCol + "," + fromRow + " To: " + toCol + "," + toRow);

                    Move move = new Move(board, board.getPiece(fromCol, fromRow), toCol, toRow);

                    if (board.isValidMove(move, true)) {
                        board.makeMove(move, true);
                        moveFound = true; // מהלך חוקי נמצא, לצאת מהלולאה
                        // System.out.println("Move found and made: " + bestMove);
                    } else {
                        // System.out.println("Move is invalid, retrying...");
                    }
                } else {
                    // System.out.println("No valid move found, retrying...");
                }
                endTime = System.currentTimeMillis();
            }
            // System.out.println(endTime - startTime);
            if (!moveFound) {
                System.out.println("taking to long, making a random move");
                RandomMoveEngine.waitTime = 0;
                randomMoveEngine.makeMove(board.convertPiecesToFEN());
                RandomMoveEngine.waitTime = 1000;
            }
            SwingUtilities.invokeLater(() -> {
                board.repaint();
                if (isStatusChanged) {
                    board.selectedPiece = null;
                    JFrame frame = new JFrame("Game Over");
                    board.updateGameState(true);
                    Main.showEndGameMessage(frame, (isCheckMate ? (isWhiteTurn ? "שחמט!!! שחור ניצח" : "שחמט!!! לבן ניצח") : "תיקו"));
                }
            });
        }).start();
    }

    public void takeEngineHint() {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            boolean moveFound = false;
            long endTime = System.currentTimeMillis();
            while ((!moveFound) && endTime - startTime < 1500) {
                engine.setSkillLevel(20);
                String fen = board.convertPiecesToFEN();
                // System.out.println("Current FEN: " + fen);
                String bestMove = engine.getBestMove(fen);
                // System.out.println("Best move: " + bestMove);

                if (bestMove != null && !bestMove.equals("unknown")) {
                    int fromCol = bestMove.charAt(0) - 'a';
                    int fromRow = 8 - (bestMove.charAt(1) - '0');
                    int toCol = bestMove.charAt(2) - 'a';
                    int toRow = 8 - (bestMove.charAt(3) - '0');
                    if (bestMove.length() > 4) {
                        engine.promotionChoice = String.valueOf(bestMove.charAt(4));
                    }
                    // System.out.println("From: " + fromCol + "," + fromRow + " To: " + toCol + "," + toRow);

                    Move move = new Move(board, board.getPiece(fromCol, fromRow), toCol, toRow);

                    if (board.isValidMove(move, true)) {
                        board.hintFromC = fromCol;
                        board.hintFromR = fromRow;
                        board.hintToC = toCol;
                        board.hintToR = toRow;
                        audioPlayer.playHintSound();
                        board.repaint();
                        engine.setSkillLevel(SettingPanel.skillLevel);
                        moveFound = true; // מהלך חוקי נמצא, לצאת מהלולאה
                        // System.out.println("Move found and made: " + bestMove);
                    } else {
                        // System.out.println("Move is invalid, retrying...");
                    }
                } else {
                    // System.out.println("No valid move found, retrying...");
                    endTime = System.currentTimeMillis();
                }
            }
                // System.out.println(endTime - startTime);
                if (!moveFound) {
                    System.out.println("taking to long, doing nothing");
                }
            SwingUtilities.invokeLater(() -> {
                board.repaint();
                if (isStatusChanged) {
                    board.selectedPiece = null;
                    JFrame frame = new JFrame("Game Over");
                    board.updateGameState(true);
                    Main.showEndGameMessage(frame, (isCheckMate ? (isWhiteTurn ? "שחמט!!! שחור ניצח" : "שחמט!!! לבן ניצח") : "תיקו"));
                }
            });
        }).start();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // deleting the hint mark
        board.hintFromC = -1;
        board.hintFromR = -1;
        board.hintToC = -1;
        board.hintToR = -1;
        if (selectedX == -1 && selectedY == -1) {
            // בחירת כלי
            if (ChoosePlayFormat.isPlayingWhite) {
                col = e.getX() / Board.tileSize;
                row = e.getY() / Board.tileSize;
            } else {
                col = (board.cols - 1) - (e.getX() / Board.tileSize);
                row = (board.rows - 1) - (e.getY() / Board.tileSize);
            }

            Piece pieceXY = board.getPiece(col, row);
            if (pieceXY != null && pieceXY.isWhite == board.getIsWhiteToMove() && (!ChoosePlayFormat.isOnePlayer || ChoosePlayFormat.isPlayingWhite == board.getIsWhiteToMove())) {
                audioPlayer.playSelectPieceSound();
                board.selectedPiece = pieceXY;
                if (ChoosePlayFormat.isPlayingWhite) {
                    selectedX = e.getX() - Board.tileSize / 2;
                    selectedY = e.getY() - Board.tileSize / 2;
                } else {
                    selectedX = (board.cols - 1) - (e.getX() - Board.tileSize) / 2;
                    selectedY = (board.rows - 1) - (e.getY() - Board.tileSize) / 2;
                }
            } else {
                selectedX = -1;
                selectedY = -1;
            }
            board.repaint();
        } else {
            // הזזת כלי
            if (ChoosePlayFormat.isPlayingWhite) {
                col = e.getX() / Board.tileSize;
                row = e.getY() / Board.tileSize;
            } else {
                col = (board.cols - 1) - (e.getX() / Board.tileSize);
                row = (board.rows - 1) - (e.getY() / Board.tileSize);
            }

            if (board.selectedPiece != null) {
                Move move = new Move(board, board.selectedPiece, col, row);
                if (board.isValidMove(move, true)) {
                    board.makeMove(move, true);
                    selectedX = -1;
                    selectedY = -1;
                    board.selectedPiece = null;
                    board.repaint();
                    if (isStatusChanged) {
                        board.selectedPiece = null;
                        board.repaint();
                        SwingUtilities.invokeLater(() -> {
                            JFrame frame = new JFrame("Game Over");
                            board.updateGameState(true);
                            Main.showEndGameMessage(frame, (isCheckMate ? (isWhiteTurn ? "שחמט!!! שחור ניצח" : "שחמט!!! לבן ניצח!") : (isStaleMate ? "פת. ליריב אין מהלכים חוקיים. המשחק נגמר בתיקו" : "אין חומר מספיק. המשחק נגמר בתיקו.")));
                        });
                    } else {
                        if ((ChoosePlayFormat.isOnePlayer && ChoosePlayFormat.isPlayingWhite != board.getIsWhiteToMove())) {
                            if (SettingPanel.skillLevel > 1) {
                                makeEngineMove();
                            } /*else if (SettingPanel.skillLevel > 0){
                                level2Engine.makeMove(board);
                            }*/ else {
                                randomMoveEngine.makeMove(board.convertPiecesToFEN());
                            }
                        }
                    }
                } else {
                    if (ChoosePlayFormat.isPlayingWhite) {
                        board.selectedPiece.xPos = board.selectedPiece.col * Board.tileSize;
                        board.selectedPiece.yPos = board.selectedPiece.row * Board.tileSize;
                    } else {
                        board.selectedPiece.xPos = (board.cols - 1 - board.selectedPiece.col) * Board.tileSize;
                        board.selectedPiece.yPos = (board.rows - 1 - board.selectedPiece.row) * Board.tileSize;
                    }
                    col = e.getX() / Board.tileSize;
                    row = e.getY() / Board.tileSize;
                    if (!ChoosePlayFormat.isPlayingWhite) {
                        col = (board.cols - 1) - col;
                        row = (board.rows - 1) - row;
                    }

                    Piece pieceXY = board.getPiece(col, row);
                    if (pieceXY != null && pieceXY.isWhite == board.getIsWhiteToMove()) {
                        audioPlayer.playSelectPieceSound();
                        board.selectedPiece = pieceXY;
                        if (ChoosePlayFormat.isPlayingWhite) {
                            selectedX = e.getX() - Board.tileSize / 2;
                            selectedY = e.getY() - Board.tileSize / 2;
                        }
                        else {
                            selectedX = (8 * Board.tileSize) - e.getX() - Board.tileSize / 2;
                            selectedY = (8 * Board.tileSize) - e.getY() - Board.tileSize / 2;
                        }
                    } else {
                        audioPlayer.playInvalidMoveSound();
                        selectedX = -1;
                        selectedY = -1;
                        board.selectedPiece = null;
                        board.repaint();
                    }
                    board.repaint();
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (board.selectedPiece != null) {
            board.selectedPiece.xPos = e.getX() - Board.tileSize / 2;
            board.selectedPiece.yPos = e.getY() - Board.tileSize / 2;
            board.repaint();
            isDragged = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isDragged) {
            if (Math.abs(selectedX - board.selectedPiece.xPos) > Board.tileSize / 2 || Math.abs(selectedY - board.selectedPiece.yPos) > Board.tileSize / 2) {
                int col = board.getColFromX(e.getX());
                int row = board.getRowFromY(e.getY());
                Move move = new Move(board, board.selectedPiece, col, row);
                if (board.isValidMove(move, true)) {
                    board.makeMove(move, false);
                    if (isStatusChanged) {
                        board.selectedPiece = null;
                        board.repaint();
                        SwingUtilities.invokeLater(() -> {
                            JFrame frame = new JFrame("Game Over");
                            board.updateGameState(true);
                            Main.showEndGameMessage(frame, (isCheckMate ? (isWhiteTurn ? "שחמט!!! שחור ניצח" : "שחמט!!! לבן ניצח!") : (isStaleMate ? "פת. ליריב אין מהלכים חוקיים. המשחק נגמר בתיקו" : "אין חומר מספיק. המשחק נגמר בתיקו.")));
                        });
                    } else {
                        board.repaint();
                        if (ChoosePlayFormat.isOnePlayer && ChoosePlayFormat.isPlayingWhite != board.getIsWhiteToMove()) {
                            if (SettingPanel.skillLevel > 1) {
                                makeEngineMove();
                            } /*else if (SettingPanel.skillLevel > 0){
                                level2Engine.makeMove(board);
                            } */else {
                                randomMoveEngine.makeMove(board.convertPiecesToFEN());
                            }
                        }
                    }
                } else {
                    board.selectedPiece.xPos = board.getXFromCol(board.selectedPiece.col);
                    board.selectedPiece.yPos = board.getYFromRow(board.selectedPiece.row);
                    audioPlayer.playInvalidMoveSound();
                }
                board.selectedPiece = null;
                board.repaint();
                selectedX = -1;
                selectedY = -1;
            } else {
                board.selectedPiece.xPos = board.getXFromCol(board.selectedPiece.col);
                board.selectedPiece.yPos = board.getYFromRow(board.selectedPiece.row);
                board.repaint();
            }
        }
        isDragged = false;
    }

}
