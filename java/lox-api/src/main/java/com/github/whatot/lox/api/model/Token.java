package com.github.whatot.lox.api.model;

import com.github.whatot.lox.api.enums.TokenType;
import lombok.Value;

/**
 * @author shenlou.huang
 * @date 2019/12/27
 */
@Value
public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;
}
