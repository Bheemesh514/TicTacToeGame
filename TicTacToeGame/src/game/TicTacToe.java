package game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class TicTacToe extends JFrame implements ActionListener {
    private JButton[][] buttons = new JButton[3][3];
    private JLabel statusLabel, scoreLabel;
    private JButton restartButton;
    private JComboBox<String> difficultyCombo, modeCombo;

    private int xWins = 0, oWins = 0, draws = 0;
    private boolean isPlayerXTurn = true;
    private String currentMode = "Single Player"; // Default

    public TicTacToe() {
        setTitle("Tic Tac Toe - Player vs Player / Computer");
        setSize(480, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new GridLayout(4, 1));
        statusLabel = new JLabel("Your Turn (X)", SwingConstants.CENTER);
        scoreLabel = new JLabel("X Wins: 0 | O Wins: 0 | Draws: 0", SwingConstants.CENTER);

        modeCombo = new JComboBox<>(new String[]{"Single Player", "Multiplayer"});
        modeCombo.addActionListener(e -> {
            currentMode = (String) modeCombo.getSelectedItem();
            resetGame(false);
            difficultyCombo.setEnabled(currentMode.equals("Single Player"));
        });

        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        topPanel.add(statusLabel);
        topPanel.add(scoreLabel);
        topPanel.add(modeCombo);
        topPanel.add(difficultyCombo);
        add(topPanel, BorderLayout.NORTH);

        // Grid Panel
        JPanel gridPanel = new JPanel(new GridLayout(3, 3));
        Font font = new Font("Arial", Font.BOLD, 60);
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++) {
                buttons[r][c] = new JButton("");
                buttons[r][c].setFont(font);
                buttons[r][c].addActionListener(this);
                gridPanel.add(buttons[r][c]);
            }
        add(gridPanel, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();
        restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> resetGame(false));
        bottomPanel.add(restartButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton clicked = (JButton) e.getSource();
        if (!clicked.getText().equals("")) return;

        if (currentMode.equals("Multiplayer")) {
            clicked.setText(isPlayerXTurn ? "X" : "O");
            clicked.setForeground(isPlayerXTurn ? Color.BLUE : Color.RED);
            if (checkForWin(isPlayerXTurn ? "X" : "O")) {
                if (isPlayerXTurn) xWins++; else oWins++;
                statusLabel.setText((isPlayerXTurn ? "X" : "O") + " Wins!");
                updateScore();
                disableBoard();
                return;
            } else if (isBoardFull()) {
                draws++;
                statusLabel.setText("It's a Draw!");
                updateScore();
                return;
            }
            isPlayerXTurn = !isPlayerXTurn;
            statusLabel.setText("Turn: " + (isPlayerXTurn ? "X" : "O"));
        } else {
            // Single Player
            clicked.setText("X");
            clicked.setForeground(Color.BLUE);

            if (checkForWin("X")) {
                xWins++;
                statusLabel.setText("You Win!");
                updateScore();
                disableBoard();
                return;
            } else if (isBoardFull()) {
                draws++;
                statusLabel.setText("It's a Draw!");
                updateScore();
                return;
            }

            statusLabel.setText("Computer's Turn...");
            SwingUtilities.invokeLater(() -> {
                computerMove();

                if (checkForWin("O")) {
                    oWins++;
                    statusLabel.setText("Computer Wins!");
                    updateScore();
                    disableBoard();
                } else if (isBoardFull()) {
                    draws++;
                    statusLabel.setText("It's a Draw!");
                    updateScore();
                } else {
                    statusLabel.setText("Your Turn (X)");
                }
            });
        }
    }

    private void computerMove() {
        String level = (String) difficultyCombo.getSelectedItem();
        switch (level) {
            case "Easy": playEasy(); break;
            case "Medium": playMedium(); break;
            case "Hard": playHard(); break;
        }
    }

    private void playEasy() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (buttons[r][c].getText().equals("")) {
                    buttons[r][c].setText("O");
                    buttons[r][c].setForeground(Color.RED);
                    return;
                }
    }

    private void playMedium() {
        if (tryWinOrBlock("O")) return;
        if (tryWinOrBlock("X")) return;
        Random rand = new Random();
        int r, c;
        do {
            r = rand.nextInt(3);
            c = rand.nextInt(3);
        } while (!buttons[r][c].getText().equals(""));
        buttons[r][c].setText("O");
        buttons[r][c].setForeground(Color.RED);
    }

    private boolean tryWinOrBlock(String player) {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (buttons[r][c].getText().equals("")) {
                    buttons[r][c].setText(player);
                    if (checkForWin(player)) {
                        if (player.equals("O")) buttons[r][c].setForeground(Color.RED);
                        else {
                            buttons[r][c].setText("O");
                            buttons[r][c].setForeground(Color.RED);
                        }
                        return true;
                    }
                    buttons[r][c].setText("");
                }
        return false;
    }

    private void playHard() {
        int[] bestMove = minimax(0, true);
        int r = bestMove[1], c = bestMove[2];
        buttons[r][c].setText("O");
        buttons[r][c].setForeground(Color.RED);
    }

    private int[] minimax(int depth, boolean isMax) {
        if (checkForWin("O")) return new int[] {10 - depth, -1, -1};
        if (checkForWin("X")) return new int[] {-10 + depth, -1, -1};
        if (isBoardFull()) return new int[] {0, -1, -1};

        int bestScore = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int bestRow = -1, bestCol = -1;

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (buttons[r][c].getText().equals("")) {
                    buttons[r][c].setText(isMax ? "O" : "X");
                    int score = minimax(depth + 1, !isMax)[0];
                    buttons[r][c].setText("");

                    if (isMax && score > bestScore) {
                        bestScore = score;
                        bestRow = r;
                        bestCol = c;
                    }
                    if (!isMax && score < bestScore) {
                        bestScore = score;
                        bestRow = r;
                        bestCol = c;
                    }
                }

        return new int[] {bestScore, bestRow, bestCol};
    }

    private boolean checkForWin(String player) {
        for (int i = 0; i < 3; i++) {
            if (checkLine(player, buttons[i][0], buttons[i][1], buttons[i][2])) return true;
            if (checkLine(player, buttons[0][i], buttons[1][i], buttons[2][i])) return true;
        }
        return checkLine(player, buttons[0][0], buttons[1][1], buttons[2][2]) ||
               checkLine(player, buttons[0][2], buttons[1][1], buttons[2][0]);
    }

    private boolean checkLine(String player, JButton b1, JButton b2, JButton b3) {
        return b1.getText().equals(player) &&
               b2.getText().equals(player) &&
               b3.getText().equals(player);
    }

    private boolean isBoardFull() {
        for (JButton[] row : buttons)
            for (JButton b : row)
                if (b.getText().equals("")) return false;
        return true;
    }

    private void disableBoard() {
        for (JButton[] row : buttons)
            for (JButton b : row)
                b.setEnabled(false);
    }

    private void enableBoard() {
        for (JButton[] row : buttons)
            for (JButton b : row) {
                b.setEnabled(true);
                b.setText("");
            }
    }

    private void resetGame(boolean fullReset) {
        enableBoard();
        isPlayerXTurn = true;
        statusLabel.setText("Your Turn (X)");
        if (fullReset) {
            xWins = oWins = draws = 0;
            updateScore();
        }
    }

    private void updateScore() {
        scoreLabel.setText("X Wins: " + xWins + " | O Wins: " + oWins + " | Draws: " + draws);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicTacToe());
    }
}