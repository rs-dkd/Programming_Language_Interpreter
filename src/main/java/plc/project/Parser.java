package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;

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

    private void skipNewline() {
        while (peek("\\n")) {
            match("\\n");
        }
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        List<Ast.Field> fields = new ArrayList<>();
        List<Ast.Method> methods = new ArrayList<>();
        while(peek("LET")){
            fields.add(parseField());
            skipNewline();
        }
        while(peek("DEF")){
            methods.add(parseMethod());
            skipNewline();
        }

        if (tokens.has(0)) {
            throw new ParseException("Unexpected token: ", tokens.get(0).getIndex());
        }

        return new Ast.Source(fields, methods);
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        match("LET");
        boolean constTracker = match("CONST");
        if(!peek(Token.Type.IDENTIFIER)){
            throw new ParseException("No identifier after LET", tokens.get(0).getIndex());
        }
        String name = tokens.get(0).getLiteral();
        match(Token.Type.IDENTIFIER);
        Optional<Ast.Expression> val = Optional.empty();
        if(match("=")){
            val = Optional.of(parseExpression());
        } else {
            throw new ParseException("No assignment after LET", tokens.get(0).getIndex());
        }
        if(!match(";")){
            throw new ParseException("No semicolon", tokens.get(0).getIndex());
        }
        return new Ast.Field(name, constTracker, val);
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        match("DEF");
        if(!peek(Token.Type.IDENTIFIER)){
            throw new ParseException("No identifier after DEF", tokens.get(0).getIndex());
        }
        String name = tokens.get(0).getLiteral();
        match(Token.Type.IDENTIFIER);
        match("(");
        List<String> params = new ArrayList<>();
        while(!match(")")){
            if(!peek(Token.Type.IDENTIFIER)){
                throw new ParseException("No param name", tokens.get(0).getIndex());
            }
            params.add(tokens.get(0).getLiteral());
            match(Token.Type.IDENTIFIER);
            if(!peek(")")){
                match(",");
            }
        }
        match("DO");
        List<Ast.Statement> body = new ArrayList<>();
        while(!peek("END")){
            body.add(parseStatement());
        }
        match("END");
        return new Ast.Method(name, params, body);
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Statement parseStatement() throws ParseException {
        if(peek("LET")){
            return parseDeclarationStatement();
        }else if(peek("IF")){
            return parseIfStatement();
        }else if(peek("FOR")){
            return parseForStatement();
        }else if(peek("WHILE")){
            return parseWhileStatement();
        }else if(peek("RETURN")){
            return parseReturnStatement();
        }
        Ast.Expression leftSide = parseExpression();
        if(match("=")){
            Ast.Expression rightSide = parseExpression();
            if(!match(";")){
                throw new ParseException("Missing semicolon after assignment.", tokens.has(0) ? tokens.get(0).getIndex() : -1);
            }
            return new Ast.Statement.Assignment(leftSide, rightSide);
        }else if(match(";")){
                return new Ast.Statement.Expression(leftSide);
        }
        throw new ParseException("Invalid Expression Case", tokens.has(0) ? tokens.get(0).getIndex() : -1);
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        match("LET");
        if(!peek(Token.Type.IDENTIFIER)){
            throw new ParseException("No identifier after LET", tokens.get(0).getIndex());
        }
        String name = tokens.get(0).getLiteral();
        match(Token.Type.IDENTIFIER);
        Optional<Ast.Expression> val = Optional.empty();
        if(match("=")){
            val = Optional.of(parseExpression());
        } else if(!match(";")) {
            throw new ParseException("Missing semicolon", tokens.get(0).getIndex());
        }
        return new Ast.Statement.Declaration(name, val);
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        match("IF");
        Ast.Expression condition = parseExpression();
        match("DO");
        List<Ast.Statement> thenBlock = new ArrayList<>();
        while(!peek("ELSE") && !peek("END")){
            thenBlock.add(parseStatement());
        }
        List<Ast.Statement> elseBlock = new ArrayList<>();
        if(match("ELSE")){
            while(!match("END")){
                elseBlock.add(parseStatement());
            }
        }else{
            match("END");
        }
        return new Ast.Statement.If(condition, thenBlock, elseBlock);
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Statement.For parseForStatement() throws ParseException {
        match("FOR");
        match("(");
        Ast.Statement intializer = null;
        if(peek("LET")){
            intializer = parseDeclarationStatement();
        }else if(peek(Token.Type.IDENTIFIER)){
            intializer = parseDeclarationStatement();
        }
        match(";");
        Ast.Expression condition = parseExpression();
        match(";");
        Ast.Statement inc = null;
        if(peek(Token.Type.IDENTIFIER)){
            inc = parseStatement();
        }
        match(")");
        List<Ast.Statement> body = new ArrayList<>();
        while(!match("END")){
            body.add(parseStatement());
        }
        return new Ast.Statement.For(intializer, condition, inc, body);
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        match("WHILE");
        Ast.Expression condition = parseExpression();
        match("DO");
        List<Ast.Statement> body = new ArrayList<>();
        while(!match("END")){
            body.add(parseStatement());
        }
        return new Ast.Statement.While(condition, body);
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {
        match("RETURN");
        Ast.Expression val = parseExpression();
        if(!match(";")){
            throw new ParseException("Missing semicolon", tokens.get(0).getIndex());
        }
        return new Ast.Statement.Return(val);
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
        while(peekAny("&&", "||")){
            String operator = tokens.get(0).getLiteral();
            consume(operator, "&&", "||");
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
        while (peekAny("<", ">", "<=", ">=", "==", "!=")) {
            String operator = tokens.get(0).getLiteral();
            consume(operator, "<", ">", "<=", ">=", "==", "!=");
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
        while (peekAny("+", "-")) {
            String operator = tokens.get(0).getLiteral();
            consume(operator, "+", "-");
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
        while (peekAny("*", "/")) {
            String operator = tokens.get(0).getLiteral();
            consume(operator, "*", "/");
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
                while(!match(")")){
                    args.add(parseExpression());
                    if(match(",")){
                        continue;
                    }else if(!peek(")")){
                        throw new ParseException("Missing , or )", tokens.get(0).getIndex());
                    }
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
            String value = tokens.get(0).getLiteral();
            if(value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")){
                value = value.substring(1, value.length()-1);
            }
            value = value.replace("\\\\", "\\")
                    .replace("\\b", "\b")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t")
                    .replace("\\'", "'")
                    .replace("\\\"", "\"");
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
            String value = tokens.get(0).getLiteral();
            if(value.length() >= 2 && value.startsWith("'") && value.endsWith("'")){
                value = value.substring(1, value.length()-1);
            }
            char character;
            if(value.length() == 1){
                character = value.charAt(0);
            }else if(value.equals("\\b")){
                character = '\b';
            }else if(value.equals("\\n")){
                character = '\n';
            }else if(value.equals("\\r")){
                character = '\r';
            }else if(value.equals("\\t")){
                character = '\t';
            }else if(value.equals("\\'")){
                character = '\'';
            }else if(value.equals("\\\"")){
                character = '\"';
            }else if(value.equals("\\\\")){
                character = '\\';
            }else{
                throw new ParseException("Invalid Char Expr", tokens.get(0).getIndex());
            }
            match(Token.Type.CHARACTER);
            return new Ast.Expression.Literal(character);
        }

        if (match("(")) {
            Ast.Expression expr = parseExpression();
            if (!match(")")) {
                throw new ParseException("Expected ')' after expression.", tokens.has(0) ? tokens.get(0).getIndex() : tokens.get(-1).getIndex() + 1);
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

    private boolean peekAny(Object...patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (peek(patterns[i])) return true;
        }
        return false;
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

    private void consume(String tokenLiteral, Object... patterns) {
        Object[] filteredPatterns = Arrays
                .stream(patterns)
                .filter(pattern -> pattern instanceof String && tokenLiteral.equals(pattern))
                .toArray();
        if (peekAny(filteredPatterns)) {
            match(filteredPatterns);
        } else {
            throw new ParseException("Expected " + tokenLiteral, tokens.get(0).getIndex());
        }
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
