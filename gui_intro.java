import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class gui_intro extends JFrame implements ActionListener {
    private JTextField display;
    private JPanel panel;
    private String[] buttonLabels = {
            "cos", "tan", "asin", "sin",
            "acos", "atan", "ln", "log",
            "^", "√",
            "7", "8", "9", "(", ")", "4", "5", "6", "*", "/",
            "1", "2", "3", "+", "-", "=", "0", ".", "C",
    };
    private JButton[] buttons = new JButton[buttonLabels.length];

    public gui_intro() {
        setTitle("Scientific Calculator");
        setBounds(500, 100, 400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        ImageIcon image = new ImageIcon("C:\\Users\\Bilal Arif\\Desktop\\IdeaProjects\\calculatorApp\\src\\calculator.png");
        setIconImage(image.getImage());

        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Helvetica", Font.PLAIN, 28));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setBackground(new Color(50, 50, 50, 255));
        display.setForeground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(display, BorderLayout.NORTH);

        panel = new JPanel();
        panel.setLayout(new GridLayout(7, 4, 10, 10));
        panel.setBackground(new Color(240, 240, 240, 255));

        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = new JButton(buttonLabels[i]);
            buttons[i].setFont(new Font("Helvetica", Font.BOLD, 20));
            buttons[i].setFocusPainted(false);
            buttons[i].setOpaque(true);
            buttons[i].setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            if (i < 10) { // The first column of special function buttons
                buttons[i].setBackground(new Color(255, 165, 0));
            } else {
                buttons[i].setBackground(new Color(220, 220, 220, 255));
                buttons[i].setForeground(new Color(0, 0, 0));
            }

            buttons[i].addActionListener(this);
            buttons[i].setPreferredSize(new Dimension(70, 70));
            buttons[i].setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180, 255), 2));
            buttons[i].setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Setting buttons to be circular
            buttons[i].setContentAreaFilled(false);
            buttons[i].setOpaque(true);
            buttons[i].setFocusPainted(false);
            buttons[i].setBorderPainted(false);
            buttons[i].setPreferredSize(new Dimension(70, 70));
            buttons[i].setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int diameter = Math.min(c.getWidth(), c.getHeight());
                    int x = (c.getWidth() - diameter) / 2;
                    int y = (c.getHeight() - diameter) / 2;
                    g2.setColor(c.getBackground());
                    g2.fillOval(x, y, diameter, diameter);
                    g2.setColor(c.getForeground());
                    FontMetrics fm = g2.getFontMetrics();
                    String text = ((AbstractButton) c).getText();
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getAscent();
                    g2.drawString(text, (c.getWidth() - textWidth) / 2, (c.getHeight() + textHeight) / 2 - fm.getDescent());
                    g2.dispose();
                }
            });

            panel.add(buttons[i]);
        }
        add(panel, BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.charAt(0) == 'C') {
            display.setText("");
        } else if (command.charAt(0) == '=') {
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
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
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
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else if (func.equals("asin")) x = Math.toDegrees(Math.asin(x));
                    else if (func.equals("acos")) x = Math.toDegrees(Math.acos(x));
                    else if (func.equals("atan")) x = Math.toDegrees(Math.atan(x));
                    else if (func.equals("log")) x = Math.log10(x);
                    else if (func.equals("ln")) x = Math.log(x);
                    else throw new RuntimeException("Unknown function: " + func);
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
        SwingUtilities.invokeLater(() -> new gui_intro());
    }
}
