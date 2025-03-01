package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from Homework 1
 * or the LexerTests file from the last project part for more information.
 */
final class ParserExpressionTests {

    @ParameterizedTest
    @MethodSource
    void testExpressionStatement(String test, List<Token> tokens, Ast.Statement.Expression expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testExpressionStatement() {
        return Stream.of(
                Arguments.of("Function Expression",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5),
                                new Token(Token.Type.OPERATOR, ";", 6)
                        ),
                        new Ast.Statement.Expression(new Ast.Expression.Function(Optional.empty(), "name", Arrays.asList()))
                ),
                Arguments.of("Variable Expression",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr", 0),
                                new Token(Token.Type.OPERATOR, ";", 4)
                        ),
                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "expr"))
                ),
                Arguments.of("Method Call Expression",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "method", 4),
                                new Token(Token.Type.OPERATOR, "(", 10),
                                new Token(Token.Type.OPERATOR, ")", 11),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        new Ast.Statement.Expression(new Ast.Expression.Function(Optional.of(
                                new Ast.Expression.Access(Optional.empty(), "obj")
                        ), "method", Arrays.asList()))
                ),
                Arguments.of("Missing Semicolon",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "x", 0)
                        ),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAssignmentStatement(String test, List<Token> tokens, Ast.Statement.Assignment expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testAssignmentStatement() {
        return Stream.of(
                Arguments.of("Assignment",
                        Arrays.asList(
                                // name = value;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.IDENTIFIER, "value", 7),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        new Ast.Statement.Assignment(
                                new Ast.Expression.Access(Optional.empty(), "name"),
                                new Ast.Expression.Access(Optional.empty(), "value")
                        )
                ),
                Arguments.of("Assignment Variable",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.IDENTIFIER, "value", 7),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        new Ast.Statement.Assignment(
                                new Ast.Expression.Access(Optional.empty(), "name"),
                                new Ast.Expression.Access(Optional.empty(), "value")
                        )
                ),
                Arguments.of("Assignment Field",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "field", 4),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.IDENTIFIER, "value", 12),
                                new Token(Token.Type.OPERATOR, ";", 17)
                        ),
                        new Ast.Statement.Assignment(
                                new Ast.Expression.Access(Optional.of(new Ast.Expression.Access(Optional.empty(), "obj")), "field"),
                                new Ast.Expression.Access(Optional.empty(), "value")
                        )
                ),
                Arguments.of("Assignment Missing Value",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.OPERATOR, ";", 6)
                        ),
                        null
                ),
                Arguments.of("Assignment Missing Semicolon",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.IDENTIFIER, "expr", 7)
                        ),
                        null
                )

        );
    }

    @ParameterizedTest
    @MethodSource
    void testLiteralExpression(String test, List<Token> tokens, Ast.Expression.Literal expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                Arguments.of("Nil Literal",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "NIL", 0)),
                        new Ast.Expression.Literal(null)
                ),
                Arguments.of("Boolean Literal",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "TRUE", 0)),
                        new Ast.Expression.Literal(Boolean.TRUE)
                ),
                Arguments.of("Boolean False Literal",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "FALSE", 0)),
                        new Ast.Expression.Literal(Boolean.FALSE)
                ),
                Arguments.of("Integer Literal",
                        Arrays.asList(new Token(Token.Type.INTEGER, "1", 0)),
                        new Ast.Expression.Literal(new BigInteger("1"))
                ),
                Arguments.of("Big Integer Literal",
                        Arrays.asList(new Token(Token.Type.INTEGER, "123456789123456789123456789", 0)),
                        new Ast.Expression.Literal(new BigInteger("123456789123456789123456789"))
                ),
                Arguments.of("Decimal Literal",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "2.0", 0)),
                        new Ast.Expression.Literal(new BigDecimal("2.0"))
                ),
                Arguments.of("Big Decimal Literal",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "123456789123456789123456789.9999999", 0)),
                        new Ast.Expression.Literal(new BigDecimal("123456789123456789123456789.9999999"))
                ),
                Arguments.of("Character Literal",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'c'", 0)),
                        new Ast.Expression.Literal('c')
                ),
                Arguments.of("String Literal",
                        Arrays.asList(new Token(Token.Type.STRING, "\"This is a string\"", 0)),
                        new Ast.Expression.Literal("This is a string")
                ),
                Arguments.of("Escape Character",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\nWorld!\"", 0)),
                        new Ast.Expression.Literal("Hello,\nWorld!")
                ),
                Arguments.of("Char Escape '\\b'",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\b'", 0)),
                        new Ast.Expression.Literal('\b')
                ),
                Arguments.of("Char Escape '\\n'",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\n'", 0)),
                        new Ast.Expression.Literal('\n')
                ),
                Arguments.of("Char Escape '\\r'",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\r'", 0)),
                        new Ast.Expression.Literal('\r')
                ),
                Arguments.of("Char Escape '\\t'",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\t'", 0)),
                        new Ast.Expression.Literal('\t')
                ),
                Arguments.of("Char Escape '\\''",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\''", 0)),
                        new Ast.Expression.Literal('\'')
                ),
                Arguments.of("Char Escape '\"'",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\"'", 0)),
                        new Ast.Expression.Literal('"')
                ),
                Arguments.of("Char Escape '\\\\'",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\\\'", 0)),
                        new Ast.Expression.Literal('\\')
                ),
                Arguments.of("String Escape \"\\b\"",
                        Arrays.asList(new Token(Token.Type.STRING, "\"\\b\"", 0)),
                        new Ast.Expression.Literal("\b")
                ),
                Arguments.of("String Escape \"\\n\"",
                        Arrays.asList(new Token(Token.Type.STRING, "\"\\n\"", 0)),
                        new Ast.Expression.Literal("\n")
                ),
                Arguments.of("String Escape \"\\r\"",
                        Arrays.asList(new Token(Token.Type.STRING, "\"\\r\"", 0)),
                        new Ast.Expression.Literal("\r")
                ),
                Arguments.of("String Escape \"\\t\"",
                        Arrays.asList(new Token(Token.Type.STRING, "\"\\t\"", 0)),
                        new Ast.Expression.Literal("\t")
                ),
                Arguments.of("String Escape \"\\'\"",
                        Arrays.asList(new Token(Token.Type.STRING, "\"\\'\"", 0)),
                        new Ast.Expression.Literal("\'")
                ),
                Arguments.of("String Escape \"\\\"\"",
                        Arrays.asList(new Token(Token.Type.STRING, "\"\\\"\"", 0)),
                        new Ast.Expression.Literal("\"")
                ),
                Arguments.of("String Escape \"\\\\\"",
                        Arrays.asList(new Token(Token.Type.STRING, "\"\\\\\"", 0)),
                        new Ast.Expression.Literal("\\")
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGroupExpression(String test, List<Token> tokens, Ast.Expression.Group expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testGroupExpression() {
        return Stream.of(
                Arguments.of("Grouped Variable",
                        Arrays.asList(
                                // (expr)
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        new Ast.Expression.Group(new Ast.Expression.Access(Optional.empty(), "expr"))
                ),
                Arguments.of("Grouped Binary",
                        Arrays.asList(
                                // (expr1 + expr2)
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr1", 1),
                                new Token(Token.Type.OPERATOR, "+", 7),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, ")", 14)
                        ),
                        new Ast.Expression.Group(new Ast.Expression.Binary("+",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        ))
                ),
                Arguments.of("Missing Closing Parenthesis",
                        Arrays.asList(
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1)
                        ),
                        null
                ),
                Arguments.of("Missing Expression",
                        Arrays.asList(
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.OPERATOR, ")", 1)
                        ),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testBinaryExpression(String test, List<Token> tokens, Ast.Expression.Binary expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("Binary And",
                        Arrays.asList(
                                // expr1 && expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 10)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Or",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "||", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 10)
                        ),
                        new Ast.Expression.Binary("||",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Less Than",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "<", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expression.Binary("<",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Less Than or Equal",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "<=", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expression.Binary("<=",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Greater Than",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, ">", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expression.Binary(">",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Greater Than or Equal",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, ">=", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expression.Binary(">=",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Equality",
                        Arrays.asList(
                                // expr1 == expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "==", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Not Equal",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "!=", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Addition",
                        Arrays.asList(
                                // expr1 + expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Subtraction",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "-", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expression.Binary("-",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Multiplication",
                        Arrays.asList(
                                // expr1 * expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expression.Binary("*",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Division",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "/", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Multiple Logical Operators (&&)",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 11),
                                new Token(Token.Type.OPERATOR, "&&", 16),
                                new Token(Token.Type.IDENTIFIER, "expr3", 21)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Binary("&&",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                ),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Multiple Comparison Operators",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "<", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "<", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 16)
                        ),
                        new Ast.Expression.Binary("<",
                                new Ast.Expression.Binary("<",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                ),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Multiple Additive Operators",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "+", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 16)
                        ),
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Binary("+",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                ),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Multiple Multiplicative Operators",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "*", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 16)
                        ),
                        new Ast.Expression.Binary("*",
                                new Ast.Expression.Binary("*",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                ),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Missing Operand in Logical &&",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6)
                        ),
                        null
                ),
                Arguments.of("Missing Operand in Comparison <",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "<", 6)
                        ),
                        null
                ),
                Arguments.of("Missing Operand in Additive +",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6)
                        ),
                        null
                ),
                Arguments.of("Missing Operand in Multiplicative *",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6)
                        ),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAccessExpression(String test, List<Token> tokens, Ast.Expression.Access expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testAccessExpression() {
        return Stream.of(
                Arguments.of("Variable",
                        // name
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "name", 0)),
                        new Ast.Expression.Access(Optional.empty(), "name")
                ),

                Arguments.of("Field Access",
                        Arrays.asList(
                                // obj.field
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "field", 4)
                        ),
                        new Ast.Expression.Access(Optional.of(new
                                Ast.Expression.Access(Optional.empty(), "obj")), "field")
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testFunctionExpression(String test, List<Token> tokens, Ast.Expression.Function expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Zero Arguments",
                        Arrays.asList(
                                // name()
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        new Ast.Expression.Function(Optional.empty(), "name", Arrays.asList())
                ),
                Arguments.of("One Argument",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "x", 5),
                                new Token(Token.Type.OPERATOR, ")", 6)
                        ),
                        new Ast.Expression.Function(Optional.empty(), "name", Arrays.asList(
                                new Ast.Expression.Access(Optional.empty(), "x")
                        ))
                ),
                Arguments.of("Multiple Arguments",
                        Arrays.asList(
                                // name(expr1, expr2, expr3)
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "expr1", 5),
                                new Token(Token.Type.OPERATOR, ",", 10),
                                new Token(Token.Type.IDENTIFIER, "expr2", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr3", 19),
                                new Token(Token.Type.OPERATOR, ")", 24)
                        ),
                        new Ast.Expression.Function(Optional.empty(), "name", Arrays.asList(
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2"),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        ))
                ),
                Arguments.of("Complex Argument",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "expr1", 5),
                                new Token(Token.Type.OPERATOR, "/", 11),
                                new Token(Token.Type.IDENTIFIER, "expr2", 13),
                                new Token(Token.Type.OPERATOR, ")", 18)
                        ),
                        new Ast.Expression.Function(Optional.empty(), "name", Arrays.asList(
                                new Ast.Expression.Binary("/",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                )
                        ))
                ),
                Arguments.of("Trailing Comma",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "expr", 5),
                                new Token(Token.Type.OPERATOR, ",", 9),
                                new Token(Token.Type.OPERATOR, ")", 10)
                        ),
                        null
                )
        );
    }
    @ParameterizedTest
    @MethodSource
    void testObjectAccess(String test, List<Token> tokens, Ast.Expression expected){
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testObjectAccess(){
        return Stream.of(
                Arguments.of("Field Access",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "field", 4)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Access(Optional.empty(), "obj")), "field")
                ),
                Arguments.of("Nested Field Access",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "x", 0),
                                new Token(Token.Type.OPERATOR, ".", 1),
                                new Token(Token.Type.IDENTIFIER, "y", 2),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "z", 4)
                        ),
                        new Ast.Expression.Access(
                                Optional.of(new Ast.Expression.Access(
                                        Optional.of(new Ast.Expression.Access(Optional.empty(), "x")), "y"
                                )),
                                "z"
                        )
                ),
                Arguments.of("Method Call",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "method", 4),
                                new Token(Token.Type.OPERATOR, "(", 10),
                                new Token(Token.Type.OPERATOR, ")", 11)
                        ),
                        new Ast.Expression.Function(
                                Optional.of(new Ast.Expression.Access(Optional.empty(), "obj")),
                                "method",
                                Arrays.asList()
                        )
                ),
                Arguments.of("Method One Argument",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "method", 4),
                                new Token(Token.Type.OPERATOR, "(", 10),
                                new Token(Token.Type.IDENTIFIER, "x", 11),
                                new Token(Token.Type.OPERATOR, ")", 12)
                        ),
                        new Ast.Expression.Function(
                                Optional.of(new Ast.Expression.Access(Optional.empty(), "obj")),
                                "method",
                                Arrays.asList(new Ast.Expression.Access(Optional.empty(), "x"))
                        )
                ),
                Arguments.of("Method Multiple Arguments",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "method", 4),
                                new Token(Token.Type.OPERATOR, "(", 10),
                                new Token(Token.Type.IDENTIFIER, "expr1", 11),
                                new Token(Token.Type.OPERATOR, ",", 16),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, ",", 23),
                                new Token(Token.Type.IDENTIFIER, "expr3", 25),
                                new Token(Token.Type.OPERATOR, ")", 30)
                        ),
                        new Ast.Expression.Function(
                                Optional.of(new Ast.Expression.Access(Optional.empty(), "obj")),
                                "method",
                                Arrays.asList(
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")
                                )
                        )
                ),
                Arguments.of("Nested Method Access",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "method", 4),
                                new Token(Token.Type.OPERATOR, "(", 10),
                                new Token(Token.Type.OPERATOR, ")", 11),
                                new Token(Token.Type.OPERATOR, ".", 12),
                                new Token(Token.Type.IDENTIFIER, "method", 13),
                                new Token(Token.Type.OPERATOR, "(", 19),
                                new Token(Token.Type.OPERATOR, ")", 20),
                                new Token(Token.Type.OPERATOR, ".", 21),
                                new Token(Token.Type.IDENTIFIER, "method", 22),
                                new Token(Token.Type.OPERATOR, "(", 28),
                                new Token(Token.Type.OPERATOR, ")", 29)
                        ),
                        new Ast.Expression.Function(
                                Optional.of(new Ast.Expression.Function(
                                        Optional.of(new Ast.Expression.Function(
                                                Optional.of(new Ast.Expression.Access(Optional.empty(), "obj")),
                                                "method",
                                                Arrays.asList()
                                        )),
                                        "method",
                                        Arrays.asList()
                                )),
                                "method",
                                Arrays.asList()
                        )
                ),
                Arguments.of("Missing Primary Expression",
                        Arrays.asList(
                                new Token(Token.Type.OPERATOR, ".", 0),
                                new Token(Token.Type.IDENTIFIER, "method", 1),
                                new Token(Token.Type.OPERATOR, "(", 7),
                                new Token(Token.Type.OPERATOR, ")", 8)
                        ),
                        null
                ),
                Arguments.of("Alternating Fields/Methods",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "obj1", 0),
                                new Token(Token.Type.OPERATOR, ".", 4),
                                new Token(Token.Type.IDENTIFIER, "method1", 5),
                                new Token(Token.Type.OPERATOR, "(", 12),
                                new Token(Token.Type.OPERATOR, ")", 13),
                                new Token(Token.Type.OPERATOR, ".", 14),
                                new Token(Token.Type.IDENTIFIER, "obj2", 15),
                                new Token(Token.Type.OPERATOR, ".", 19),
                                new Token(Token.Type.IDENTIFIER, "method2", 20),
                                new Token(Token.Type.OPERATOR, "(", 27),
                                new Token(Token.Type.OPERATOR, ")", 28),
                                new Token(Token.Type.OPERATOR, ".", 29),
                                new Token(Token.Type.IDENTIFIER, "obj3", 30)
                        ),
                        new Ast.Expression.Access(
                                Optional.of(new Ast.Expression.Function(
                                        Optional.of(new Ast.Expression.Access(
                                                Optional.of(new Ast.Expression.Function(
                                                        Optional.of(new Ast.Expression.Access(Optional.empty(), "obj1")),
                                                        "method1",
                                                        Arrays.asList()
                                                )),
                                                "obj2"
                                        )),
                                        "method2",
                                        Arrays.asList()
                                )),
                                "obj3"
                        )
                )
        );
    }
    @ParameterizedTest
    @MethodSource
    void testOperatorPrecedence(String test, List<Token> tokens, Ast.Expression expected){
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testOperatorPrecedence(){
        return Stream.of(
                Arguments.of("Logical AND with Comparison",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, "==", 15),
                                new Token(Token.Type.IDENTIFIER, "expr3", 18)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Binary("==",
                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")
                                )
                        )
                ),
                Arguments.of("Logical AND with Addition",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, "+", 15),
                                new Token(Token.Type.IDENTIFIER, "expr3", 17)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Binary("+",
                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")
                                )
                        )
                ),
                Arguments.of("Logical AND with Multiplication",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, "*", 15),
                                new Token(Token.Type.IDENTIFIER, "expr3", 17)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Binary("*",
                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")
                                )
                        )
                ),
                Arguments.of("Comparison with Logical OR",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "==", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, "&&", 15),
                                new Token(Token.Type.IDENTIFIER, "expr3", 18)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Binary("==",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                ),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Comparison with Addition",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "==", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, "+", 15),
                                new Token(Token.Type.IDENTIFIER, "expr3", 17)
                        ),
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Binary("+",
                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")
                                )
                        )
                ),
                Arguments.of("Comparison with Multiplication",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "==", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, "*", 15),
                                new Token(Token.Type.IDENTIFIER, "expr3", 17)
                        ),
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Binary("*",
                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")
                                )
                        )
                ),
                Arguments.of("Addition with Logical AND",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "&&", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 17)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Binary("+",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                ),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Addition with Comparison",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "==", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 17)
                        ),
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Binary("+",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                ),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Addition with Multiplication",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "*", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 16)
                        ),
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Binary("*",
                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")
                                )
                        )
                ),
                Arguments.of("Multiplication with Logical AND",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "&&", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 17)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Binary("*",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                ),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Multiplication with Comparison",
                        Arrays.asList(
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6),
                                        new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                        new Token(Token.Type.OPERATOR, "==", 14),
                                        new Token(Token.Type.IDENTIFIER, "expr3", 17)
                                ),
                                new Ast.Expression.Binary("==",
                                        new Ast.Expression.Binary("*",
                                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                                new Ast.Expression.Access(Optional.empty(), "expr2")
                                        ),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")
                                )
                        ),
                        Arguments.of("Multiplication with Addition",
                                Arrays.asList(
                                        new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                        new Token(Token.Type.OPERATOR, "*", 6),
                                        new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                        new Token(Token.Type.OPERATOR, "+", 14),
                                        new Token(Token.Type.IDENTIFIER, "expr3", 16)
                                ),
                                new Ast.Expression.Binary("+",
                                        new Ast.Expression.Binary("*",
                                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                                new Ast.Expression.Access(Optional.empty(), "expr2")
                                        ),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")
                                )
                        )
                );
    }
    @ParameterizedTest
    @MethodSource
    void testErrorCases(String test, List<Token> tokens, Integer expectedIndex){
        testError(tokens, expectedIndex, Parser::parseExpression);
    }

    private static Stream<Arguments> testErrorCases(){
        return Stream.of(
                // Invalid Expression: !
                Arguments.of("Invalid Expression",
                        Arrays.asList(
                                new Token(Token.Type.OPERATOR, "!", 0)
                        ),
                        0
                ),
                Arguments.of("Missing Closing Parenthesis",
                        Arrays.asList(
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1)
                        ),
                        2
                )
        );
    }
    private static <T> void testError(List<Token> tokens, Integer expectedIndex, Function<Parser, T> function){
        Parser parser = new Parser(tokens);
        ParseException exception = Assertions.assertThrows(ParseException.class, () -> function.apply(parser));
        Assertions.assertEquals(expectedIndex, exception.getIndex());
    }
    /**
     * Standard test function. If expected is null, a ParseException is expected
     * to be thrown (not used in the provided tests).
     */
    private static <T extends Ast> void test(List<Token> tokens, T expected, Function<Parser, T> function) {
        Parser parser = new Parser(tokens);
        if (expected != null) {
            Assertions.assertEquals(expected, function.apply(parser));
        } else {
            Assertions.assertThrows(ParseException.class, () -> function.apply(parser));
        }
    }

}
