import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame implements ActionListener {
    private JTextField display;
    private JPanel panel;
    private JButton[] buttons;

    private String[] buttonLabels = {
            "cos", "tan", "asin", "sin",
            "acos", "atan", "ln", "log",
            "^", "√",
            "7", "8", "9", "(", ")", "4", "5", "6", "*", "/",
            "1", "2", "3", "+", "-", "=", "0", ".", "C",
    };

    public Main() {
        setTitle("Scientific Calculator");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(50, 50, 50));

        initDisplay();
        initButtons();
        initPanel();

        setVisible(true);
    }

    private void initDisplay() {
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.BOLD, 28));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setBackground(Color.BLACK);
        display.setForeground(Color.GREEN);
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(display, BorderLayout.NORTH);
    }

    private void initButtons() {
        buttons = new JButton[buttonLabels.length];
        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = createButton(buttonLabels[i]);
        }
    }

    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.addActionListener(this);
        return button;
    }

    private void initPanel() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(7, 4, 5, 5));
        panel.setBackground(new Color(50, 50, 50));
        for (JButton button : buttons) {
            panel.add(button);
        }
        add(panel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("C")) {
            display.setText("");
        } else if (command.equals("=")) {
            try {
                display.setText(evaluate(display.getText()));
            } catch (Exception ex) {
                display.setText("Error");
            }
        } else {
            display.setText(display.getText() + command);
        }
    }

    private String evaluate(String expression) throws Exception {
        try {
            return Double.toString(eval(expression));
        } catch (Exception e) {
            throw new Exception("Invalid Expression");
        }
    }

    private double eval(final String str) {
        class Parser {
            int pos = -1, c;

            void nextChar() {
                c = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (c == ' ') nextChar();
                if (c == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) c);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((c >= '0' && c <= '9') || c == '.') { // numbers
                    while ((c >= '0' && c <= '9') || c == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (c >= 'a' && c <= 'z') { // functions
                    while (c >= 'a' && c <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    switch (func) {
                        case "sqrt" -> x = Math.sqrt(x);
                        case "sin" -> x = Math.sin(Math.toRadians(x));
                        case "cos" -> x = Math.cos(Math.toRadians(x));
                        case "tan" -> x = Math.tan(Math.toRadians(x));
                        case "asin" -> x = Math.toDegrees(Math.asin(x));
                        case "acos" -> x = Math.toDegrees(Math.acos(x));
                        case "atan" -> x = Math.toDegrees(Math.atan(x));
                        case "log" -> x = Math.log10(x);
                        case "ln" -> x = Math.log(x);
                        default -> throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) c);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }
        return new Parser().parse();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
