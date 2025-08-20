import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.text.*;

public class SudokuSolver extends JFrame {

    private static final int GRID_SIZE = 9; // Makes the Sudoku board be 9x9.
    private JTextField[][] cells = new JTextField[GRID_SIZE][GRID_SIZE]; // UI input cells.
    private int[][] board = new int[GRID_SIZE][GRID_SIZE]; // Internal representation of Sudoku grid.

    public SudokuSolver() {
        setTitle("Sudoku Solver");
        setSize(500, 550);
        setLayout(new BorderLayout());

        // Create the 9x9 Sudoku grid panel.
        JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setFont(new Font("Arial", Font.PLAIN, 20));
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);

                // Apply the input filter (new code)
                setupInputFilter(cells[row][col]);

                // Adding thicker borders for separating 3x3 boxes visually.
                int top = (row % 3 == 0) ? 3 : 1;
                int left = (col % 3 == 0) ? 3 : 1;
                int bottom = (row == GRID_SIZE - 1) ? 3 : 1;
                int right = (col == GRID_SIZE - 1) ? 3 : 1;

                cells[row][col].setBorder(new MatteBorder(top, left, bottom, right, Color.BLACK));

                gridPanel.add(cells[row][col]);
            }
        }

        // Panel for buttons (Solve, Reset, Save, Load).
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));

        // Solve button - triggers the backtracking algorithm.
        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isBoardValid()) { // Validate entries before solving.
                    JOptionPane.showMessageDialog(SudokuSolver.this, "Invalid entries detected.");
                    return;
                }

                updateBoardFromUI(); // Copy numbers from UI into the board array.
                if (solveBoard()) {  // Attempt to solve using backtracking.
                    updateUIFromBoard(); // Update UI with the solved numbers.
                } else {
                    JOptionPane.showMessageDialog(SudokuSolver.this, "Unsolvable board :(");
                }
            }
        });
        buttonPanel.add(solveButton);

        // Reset button - clears the board and UI.
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetBoard();
            }
        });
        buttonPanel.add(resetButton);

        // Save button - writes puzzle to a text file.
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePuzzle();
            }
        });
        buttonPanel.add(saveButton);

        // Load button - reads puzzle from a text file.
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPuzzle();
            }
        });
        buttonPanel.add(loadButton);

        // Add UI panels to the window.
        add(gridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Sets up input filter for text fields to allow only single digits (1-9)
    private void setupInputFilter(JTextField textField) {
        ((PlainDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                if ((fb.getDocument().getLength() + text.length() - length) <= 1 && (newText.matches("^[1-9]?$"))) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (fb.getDocument().getLength() + string.length() <= 1 && string.matches("^[1-9]$")) {
                    super.insertString(fb, offset, string, attr);
                }
            }
        });
    }

// Checks whether the current board contains valid numbers  (1-9 only, no conflicts in rows, columns, or boxes).
private boolean isBoardValid() {
    updateBoardFromUI();

    for (int row = 0; row < GRID_SIZE; row++) {
        for (int col = 0; col < GRID_SIZE; col++) {
            String text = cells[row][col].getText();

            // Allow empty cells (0) or digits 1-9
            if (!text.isEmpty() && !text.matches("[0-9]")) {
                cells[row][col].setBackground(Color.PINK);
                return false;
            }

            int number = text.isEmpty() ? 0 : Integer.parseInt(text);

            // Check if placement follows Sudoku rules (skip for 0)
            if (number != 0) {
                board[row][col] = 0; // Temporarily clear to check placement
                if (!isValidPlacement(row, col, number)) {
                    cells[row][col].setBackground(Color.PINK);
                    board[row][col] = number;
                    return false;
                }
                board[row][col] = number;
            }
        }
    }
    return true;
}

    //Solves the Sudoku board using recursive backtracking.
    private boolean solveBoard() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                if (board[row][column] == 0) { // Empty cell found.
                    for (int numberToTry = 1; numberToTry <= GRID_SIZE; numberToTry++) {
                        if (isValidPlacement(row, column, numberToTry)) {
                            board[row][column] = numberToTry;

                            if (solveBoard()) { // Recursively solve remaining board.
                                return true;
                            } else {
                                board[row][column] = 0; // Backtrack.
                            }
                        }
                    }
                    return false; // If no valid number is found, then it triggers backtracking.
                }
            }
        }
        return true; // No empty cells left, means it has been solved.
    }

    // Checks if placing a number at (row, col) is valid by checking row, column, and 3x3 subgrid.
    private boolean isValidPlacement(int row, int column, int number) {
        // Checks the row and column.
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[row][i] == number || board[i][column] == number) {
                return false;
            }
        }

        // Checks the 3x3 subgrid.
        int localBoxRow = row - row % 3;
        int localBoxColumn = column - column % 3;

        for (int i = localBoxRow; i < localBoxRow + 3; i++) {
            for (int j = localBoxColumn; j < localBoxColumn + 3; j++) {
                if (board[i][j] == number) {
                    return false;
                }
            }
        }

        return true;
    }

    // Resets both the UI and internal board to empty.
    private void resetBoard() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].setText("");
                cells[row][col].setBackground(Color.WHITE);
                board[row][col] = 0;
            }
        }
    }

    // Copies the values from the UI text fields into the internal board array.
    private void updateBoardFromUI() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                String text = cells[row][col].getText();
                board[row][col] = text.isEmpty() ? 0 : Integer.parseInt(text);
            }
        }
    }

    // Updates the UI text fields to reflect the current state of the board array.
    private void updateUIFromBoard() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                String value = board[row][col] == 0 ? "" : String.valueOf(board[row][col]);
                cells[row][col].setText(value);
            }
        }
    }

    // Saves the current puzzle state to a text file. The save will use "0" to represent empty cells.
    private void savePuzzle() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sudoku_puzzle.txt"))) {
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    writer.write(cells[row][col].getText().isEmpty() ? "0" : cells[row][col].getText());
                    if (col < GRID_SIZE - 1) {
                        writer.write(","); // Separate values with commas.
                    }
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Puzzle Saved!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving puzzle: " + e.getMessage());
        }
    }

    // Loads a puzzle state from a text file into the board and UI.
    private void loadPuzzle() {
        try (BufferedReader reader = new BufferedReader(new FileReader("sudoku_puzzle.txt"))) {
            String line;
            int row = 0;

            while ((line = reader.readLine()) != null && row < GRID_SIZE) {
                String[] values = line.split(",");

                for (int col = 0; col < GRID_SIZE; col++) {
                    cells[row][col].setText(values[col].equals("0") ? "" : values[col]);
                }
                row++;
            }
            updateBoardFromUI(); // Sync board with loaded values and inform the user if it was successful or a fail.
            JOptionPane.showMessageDialog(this, "Puzzle Loaded!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading puzzle: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Start the application on the Swing event dispatch thread.
        SwingUtilities.invokeLater(SudokuSolver::new);
    }
}