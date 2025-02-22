package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Statement parseStatement() throws ParseException {
        Ast.Expression leftSide = parseExpression();
        if(match("=")){
            Ast.Expression rightSide = parseExpression();
            if(match(";")){
                return new Ast.Statement.Assignment(leftSide, rightSide);
            }
        }else if(match(";")){
                return new Ast.Statement.Expression(leftSide);
        }
        throw new ParseException("Invalid Expression Case", tokens.get(0).getIndex());
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Statement.For parseForStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expression parseLogicalExpression() throws ParseException {
        Ast.Expression leftSide = parseEqualityExpression();
        while(peek("AND") || peek("OR")){
            String operator = tokens.get(0).getLiteral();
            match("AND");
            match("OR");
            Ast.Expression rightSide = parseEqualityExpression();
            leftSide = new Ast.Expression.Binary(operator, leftSide, rightSide);
        }
        return leftSide;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseEqualityExpression() throws ParseException {
        Ast.Expression leftSide = parseAdditiveExpression();
        while(peek("<") || peek(">") || peek("<=") || peek(">=") || peek("==") || peek("!=")){
            String operator = tokens.get(0).getLiteral();
            match("<");
            match(">");
            match("<=");
            match(">=");
            match("==");
            match("!=");
            Ast.Expression rightSide = parseAdditiveExpression();
            leftSide = new Ast.Expression.Binary(operator, leftSide, rightSide);
        }
        return leftSide;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {
        Ast.Expression leftSide = parseMultiplicativeExpression();
        while(peek("+") || peek("-")){
            String operator = tokens.get(0).getLiteral();
            match("+");
            match("-");
            Ast.Expression rightSide = parseMultiplicativeExpression();
            leftSide = new Ast.Expression.Binary(operator, leftSide, rightSide);
        }
        return leftSide;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {
        Ast.Expression leftSide = parseSecondaryExpression();
        while(peek("*") || peek("/")){
            String operator = tokens.get(0).getLiteral();
            match("*");
            match("/");
            Ast.Expression rightSide = parseSecondaryExpression();
            leftSide = new Ast.Expression.Binary(operator, leftSide, rightSide);
        }
        return leftSide;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expression parseSecondaryExpression() throws ParseException {
        Ast.Expression leftSide = parsePrimaryExpression();
        while(peek(".")){
            match(".");
            if(!peek(Token.Type.IDENTIFIER)){
                throw new ParseException("Invalid Primary Expr", tokens.get(0).getIndex());
            }
            String id = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);

            if(match("(")){
                List<Ast.Expression> args = new ArrayList<>();
                if(!match(")")){
                    args.add(parseExpression());
                    if(match(",")){
                        args.add(parseExpression());
                    }
                    throw new ParseException("No End Parenthesis", tokens.get(0).getIndex());
                }
                leftSide = new Ast.Expression.Function(Optional.of(leftSide), id, args);
            }else{
                leftSide = new Ast.Expression.Access(Optional.of(leftSide), id);
            }
        }
        return leftSide;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     *
     * primary_expression
     *          ::= 'NIL'
     *            | 'TRUE'
     *            | 'FALSE'
     *            | integer
     *            | decimal
     *            | character
     *            | string
     *            | '(' expression ')'
     *            | identifier ( '(' ( expression ( ',' expression )* )? ')' )?
     */
    public Ast.Expression parsePrimaryExpression() throws ParseException {
        if (match("TRUE")) return new Ast.Expression.Literal(true);
        if (match("FALSE")) return new Ast.Expression.Literal(false);
        if (match("NIL")) return new Ast.Expression.Literal(null);

        if (peek(Token.Type.STRING)) {
            String value = tokens.get(0).getLiteral().replace("\"", "").replace("\\n", "\n");
            match(Token.Type.STRING);
            return new Ast.Expression.Literal(value);
        }

        if (peek(Token.Type.INTEGER)) {
            String value = tokens.get(0).getLiteral();
            match(Token.Type.INTEGER);
            return new Ast.Expression.Literal(new BigInteger(value));
        }

        if (peek(Token.Type.DECIMAL)) {
            String value = tokens.get(0).getLiteral();
            match(Token.Type.DECIMAL);
            return new Ast.Expression.Literal(new BigDecimal(value));
        }

        if (peek(Token.Type.CHARACTER)) {
            String value = tokens.get(0).getLiteral().replace("'", "");
            match(Token.Type.CHARACTER);
            return new Ast.Expression.Literal(value.charAt(0));
        }

        if (match("(")) {
            Ast.Expression expr = parseExpression();
            if (!match(")")) {
                throw new ParseException("Expected ')' after expression.", 0);
            }
            return new Ast.Expression.Group(expr);
        }

        if (peek(Token.Type.IDENTIFIER)) {
            Token previous = tokens.get(0);
            match(Token.Type.IDENTIFIER);
            if (match("(")) {
                List<Ast.Expression> args = new ArrayList<>();
                if (!match(")")) {
                    do {
                        args.add(parseExpression());
                    } while (match(","));
                    if (!match(")")) {
                        throw new ParseException("Expected ')' after arguments.", 0);
                    }
                }
                return new Ast.Expression.Function(Optional.empty(), previous.getLiteral(), args);
            }
            return new Ast.Expression.Access(Optional.empty(), previous.getLiteral());
        }
        throw new ParseException("Invalid Primary Expression", 0);
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for(int i = 0; i < patterns.length; i++){
            if(!tokens.has(i)){
                return false;
            }else if(patterns[i] instanceof Token.Type){
                if(patterns[i] != tokens.get(i).getType()){
                    return false;
                }
            }else if(patterns[i] instanceof String){
                if(!patterns[i].equals(tokens.get(i).getLiteral())){
                    return false;
                }
            }else{
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if(peek){
            for(int i = 0; i < patterns.length; i++){
                tokens.advance();
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
