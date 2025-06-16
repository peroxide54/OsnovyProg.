import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Main {
    private static final String HISTORY_FILE = "history.txt";

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Калькулятор. Введите выражение или 'history' для просмотра истории.");

        while (true) {
            System.out.print(">>> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) break;

            if (input.equalsIgnoreCase("history")) {
                printHistory();
                continue;
            }

            try {
                double result = evaluate(input);
                System.out.println("= " + result);
                saveHistory(input + " = " + result);
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    static void saveHistory(String entry) throws IOException {
        try (FileWriter fw = new FileWriter(HISTORY_FILE, true)) {
            fw.write(entry + "\n");
        }
    }

    static void printHistory() throws IOException {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            System.out.println("История пуста.");
            return;
        }

        System.out.println("--- История ---");
        Scanner fileScanner = new Scanner(file);
        while (fileScanner.hasNextLine()) {
            System.out.println(fileScanner.nextLine());
        }
        System.out.println("---------------");
    }

    static double evaluate(String expr) {
        expr = expr.replaceAll("\\s+", "");
        expr = expr.replaceAll("\\|([^|]+)\\|", "abs($1)");
        expr = expr.replaceAll("//", "idiv");
        return parse(expr);
    }

    static double parse(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else if (match("idiv")) x = (int)(x / parseFactor());
                    else if (eat('%')) x %= parseFactor();
                    else return x;
                }
            }

            boolean match(String token) {
                if (expr.startsWith(token, pos)) {
                    pos += token.length();
                    nextChar();
                    return true;
                }
                return false;
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if (match("abs(")) {
                    x = Math.abs(parseExpression());
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Неожиданный символ: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor());

                return x;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Лишние символы: " + expr.substring(pos));
                return x;
            }
        }.parse();
    }
}

