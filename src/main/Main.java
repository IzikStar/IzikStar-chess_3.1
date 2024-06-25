package main;

import javax.swing.*;
import java.awt.*;

import GUI.CustomButtonPanel;
import ai.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setMinimumSize(new Dimension(1000, 1000));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(Color.gray);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // יצירת מופע של CustomButtonPanel והוספתו לחלון
        CustomButtonPanel customButtonPanel = new CustomButtonPanel(1);
        frame.add(customButtonPanel);

        // הגדרת GridBagConstraints עבור הכפתור
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 1;  // עמודה שנייה (צד ימין)
        buttonConstraints.gridy = 0;  // שורה ראשונה (למעלה)
        buttonConstraints.anchor = GridBagConstraints.NORTHEAST;  // עיגון בצד ימין למעלה
        buttonConstraints.insets = new Insets(10, 10, 10, 10);  // רווחים מסביב לכפתור

        // יצירת מופע של CustomButtonPanel והוספתו לחלון
        CustomButtonPanel customButtonPanel2 = new CustomButtonPanel(2);
        frame.add(customButtonPanel2);

        // הגדרת GridBagConstraints עבור הכפתור
        GridBagConstraints buttonConstraints2 = new GridBagConstraints();
        buttonConstraints2.gridx = 3;  // עמודה שנייה (צד ימין)
        buttonConstraints2.gridy = 0;  // שורה ראשונה (למעלה)
        buttonConstraints2.anchor = GridBagConstraints.NORTHEAST;  // עיגון בצד ימין למעלה
        buttonConstraints2.insets = new Insets(10, 10, 10, 10);  // רווחים מסביב לכפתור

        // הוספת הכפתורים לחלון
        frame.add(customButtonPanel, buttonConstraints);
        frame.add(customButtonPanel2, buttonConstraints2);


        GridBagConstraints tabConstraints = new GridBagConstraints();
        tabConstraints.gridx = 0;
        tabConstraints.gridy = 3;
        tabConstraints.gridwidth = 2;
//        tabConstraints.gridheight = GridBagConstraints.REMAINDER;
//        tabConstraints.fill = GridBagConstraints.BOTH;
//        tabConstraints.weightx = 1.0;
//        tabConstraints.weighty = 1.0;
        frame.add(tabbedPane, tabConstraints);

        Board board = new Board();
        tabbedPane.addTab("Game", board);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setBackground(Color.white);
        settingsPanel.add(new JLabel("Settings Panel"));
        tabbedPane.addTab("Settings", settingsPanel);

        JPanel savedGamesPanel = new JPanel();
        savedGamesPanel.setBackground(Color.white);
        savedGamesPanel.add(new JLabel("Saved Games Panel"));
        tabbedPane.addTab("Saved Games", savedGamesPanel);

        frame.add(tabbedPane, tabConstraints);
        frame.setVisible(true);

        // הפעלת Stockfish והפעלת המשחק
//        String pathToStockfish = "D:\\Desktop\\סיכומים קורס תכנות\\אורט סינגאלובסקי\\java-projects\\chessGame_3\\src\\res\\stockfish\\stockfish-windows-x86-64.exe";
//        StockfishEngine engine = new StockfishEngine();
//        if (engine.startEngine(pathToStockfish)) {
//            String fen = "startpos"; // מצב הלוח ההתחלתי (או מצב אחר לפי הצורך)
//            String bestMove = engine.getBestMove(fen);
//            System.out.println("Best move: " + bestMove);
//            engine.stopEngine();
//        } else {
//            System.out.println("Failed to start Stockfish engine.");
//        }
    }

    public static void showEndGameMessage(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "סיום המשחק", JOptionPane.INFORMATION_MESSAGE);
    }
}
