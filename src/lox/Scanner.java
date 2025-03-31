package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords= new HashMap<>();
    static {
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch(c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;

            case '!': addToken(matchNext('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=': addToken(matchNext('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<': addToken(matchNext('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>': addToken(matchNext('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '/':
                if(matchNext('/')) {
                    // Goes until the end of the line since it is a comment. Detecting \n with lookahead/peek here
                    // rather than matchNext allows the loop to continue and properly increase the line variable
                    while (peekOne() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else if(matchNext('*')) {
                    multilineComment();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            // Ignore whitespace stuff
            case ' ':
            case '\r':
            case '\t':
            break;

            case '\n':
                line++;
                break;

            case '"':
                string();
                break;

            case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                number();
                break;

            default:
                if(isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character '" + c + "'");
                }
                break;
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void multilineComment() {
        var done = false;
        while (!done) {
            if(peekOne() != '*') {
                advance();
                continue;
            }
            if(peekTwo() == '/') {
                break;
            }
            advance();
        }
        // Skip past the */
        advance();
        advance();
    }

    private void identifier() {
        while(isAlphaNumeric(peekOne())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) {
            type = TokenType.IDENTIFIER;
        }

        addToken(type);
    }

    private void number() {
        while(isDigit(peekOne())) {
            advance();
        }

        // Decimal handling
        if(peekOne() == '.' && isDigit(peekTwo())) {
            // Move past the .
            advance();

            while(isDigit(peekOne())) {
                advance();
            }
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while(peekOne() != '"' && !isAtEnd()) {
            if(peekOne() == '\n') {
                line++;
            }
            advance();
        }

        if(isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // This takes us to the closing double quote.
        advance();

        // Remove enclosing double quotes
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private char peekOne() {
        if(isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekTwo() {
        if(current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private boolean matchNext(char expected) {
        if(isAtEnd()) {
            return false;
        }
        if(source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current == source.length();
    }
}
