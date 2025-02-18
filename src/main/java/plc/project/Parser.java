package plc.project;

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
    public Ast.Stmt parseStatement() throws ParseException {
        Ast.Expr leftSide = parseExpression();
        if(match("=")){
            Ast.Expr rightSide = parseExpression();
            if(match(";")){
                return new Ast.Stmt.Assignment(leftSide, rightSide);
            }
        }else if(match(";")){
                return new Ast.Stmt.Expression(leftSide);
        }
        throw new ParseException("Invalid Expression Case", tokens.get(0).getIndex());
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        Ast.Expr leftSide = parseEqualityExpression();
        while(match("AND") || match("OR")){
            String operator = tokens.get(0).getLiteral();
            Ast.Expr rightSide = parseEqualityExpression();
            leftSide = new Ast.Expr.Binary(operator, leftSide, rightSide);
        }
        return leftSide;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        Ast.Expr leftSide = parseAdditiveExpression();
        while(match("<") || match(">") || match("<=") || match(">=") || match("==") || match("!=")){
            String operator = tokens.get(0).getLiteral();
            Ast.Expr rightSide = parseAdditiveExpression();
            leftSide = new Ast.Expr.Binary(operator, leftSide, rightSide);
        }
        return leftSide;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        Ast.Expr leftSide = parseMultiplicativeExpression();
        while(match("+") || match("-")){
            String operator = tokens.get(0).getLiteral();
            Ast.Expr rightSide = parseMultiplicativeExpression();
            leftSide = new Ast.Expr.Binary(operator, leftSide, rightSide);
        }
        return leftSide;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        Ast.Expr leftSide = parseSecondaryExpression();
        while(match("*") || match("/")){
            String operator = tokens.get(0).getLiteral();
            Ast.Expr rightSide = parseSecondaryExpression();
            leftSide = new Ast.Expr.Binary(operator, leftSide, rightSide);
        }
        return leftSide;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        Ast.Expr leftSide = parsePrimaryExpression();
        while(match(".")){
            if(!match(Token.Type.IDENTIFIER)){
                throw new ParseException("Invalid Primary Expr", tokens.get(0).getIndex());
            }
            String id = tokens.get(0).getLiteral();
            if(match("(")){
                List<Ast.Expr> args = new ArrayList<>();
                if(!match(")")){
                    args.add(parseExpression());
                    if(match(",")){
                        args.add(parseExpression());
                    }
                    throw new ParseException("No End Parenthesis", tokens.get(0).getIndex());
                }
                leftSide = new Ast.Expr.Function(Optional.of(leftSide), id, args);
            }else{
                leftSide = new Ast.Expr.Access(Optional.of(leftSide), id);
            }
        }
        return leftSide;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
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
            if(!tokens.has(1)){
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
