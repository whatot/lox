package com.github.whatot.lox;

import com.github.whatot.lox.api.enums.TokenType;
import com.github.whatot.lox.api.model.Token;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;

import static com.github.whatot.lox.api.enums.TokenType.AND;
import static com.github.whatot.lox.api.enums.TokenType.BANG;
import static com.github.whatot.lox.api.enums.TokenType.BANG_EQUAL;
import static com.github.whatot.lox.api.enums.TokenType.CLASS;
import static com.github.whatot.lox.api.enums.TokenType.COMMA;
import static com.github.whatot.lox.api.enums.TokenType.DOT;
import static com.github.whatot.lox.api.enums.TokenType.ELSE;
import static com.github.whatot.lox.api.enums.TokenType.EOF;
import static com.github.whatot.lox.api.enums.TokenType.EQUAL;
import static com.github.whatot.lox.api.enums.TokenType.EQUAL_EQUAL;
import static com.github.whatot.lox.api.enums.TokenType.FALSE;
import static com.github.whatot.lox.api.enums.TokenType.FOR;
import static com.github.whatot.lox.api.enums.TokenType.FUN;
import static com.github.whatot.lox.api.enums.TokenType.GREATER;
import static com.github.whatot.lox.api.enums.TokenType.GREATER_EQUAL;
import static com.github.whatot.lox.api.enums.TokenType.IDENTIFIER;
import static com.github.whatot.lox.api.enums.TokenType.IF;
import static com.github.whatot.lox.api.enums.TokenType.LEFT_BRACE;
import static com.github.whatot.lox.api.enums.TokenType.LEFT_PAREN;
import static com.github.whatot.lox.api.enums.TokenType.LESS;
import static com.github.whatot.lox.api.enums.TokenType.LESS_EQUAL;
import static com.github.whatot.lox.api.enums.TokenType.MINUS;
import static com.github.whatot.lox.api.enums.TokenType.NIL;
import static com.github.whatot.lox.api.enums.TokenType.NUMBER;
import static com.github.whatot.lox.api.enums.TokenType.OR;
import static com.github.whatot.lox.api.enums.TokenType.PLUS;
import static com.github.whatot.lox.api.enums.TokenType.PRINT;
import static com.github.whatot.lox.api.enums.TokenType.RETURN;
import static com.github.whatot.lox.api.enums.TokenType.RIGHT_BRACE;
import static com.github.whatot.lox.api.enums.TokenType.RIGHT_PAREN;
import static com.github.whatot.lox.api.enums.TokenType.SEMICOLON;
import static com.github.whatot.lox.api.enums.TokenType.SLASH;
import static com.github.whatot.lox.api.enums.TokenType.STAR;
import static com.github.whatot.lox.api.enums.TokenType.STRING;
import static com.github.whatot.lox.api.enums.TokenType.SUPER;
import static com.github.whatot.lox.api.enums.TokenType.THIS;
import static com.github.whatot.lox.api.enums.TokenType.TRUE;
import static com.github.whatot.lox.api.enums.TokenType.VAR;
import static com.github.whatot.lox.api.enums.TokenType.WHILE;

/**
 * @author shenlou.huang
 * @date 2019/12/27
 */
@SuppressWarnings({"PMD.MethodTooLongRule", "PMD.UndefineMagicConstantRule"})
public class Scanner {
    private final String source;
    private final List<Token> tokens = Lists.newArrayList();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final ImmutableMap<String, TokenType> KEYWORDS =
            ImmutableMap.<String, TokenType>builder()
                    .put("and", AND)
                    .put("class", CLASS)
                    .put("else", ELSE)
                    .put("false", FALSE)
                    .put("for", FOR)
                    .put("fun", FUN)
                    .put("if", IF)
                    .put("nil", NIL)
                    .put("or", OR)
                    .put("print", PRINT)
                    .put("return", RETURN)
                    .put("super", SUPER)
                    .put("this", THIS)
                    .put("true", TRUE)
                    .put("var", VAR)
                    .put("while", WHILE)
                    .build();

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            case 'o':
                if (peek() == 'r') {
                    addToken(OR);
                }
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        // See if the identifier is a reserved word.
        String text = source.substring(start, current);

        TokenType type = KEYWORDS.get(text);
        if (type == null) {
            type = IDENTIFIER;
        }
        addToken(type);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        // Unterminated string.
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
