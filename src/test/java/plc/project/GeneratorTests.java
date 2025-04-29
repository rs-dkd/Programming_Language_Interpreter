package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GeneratorTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testSource(String test, Ast.Source ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of("Hello, World!",
                        // FUN main(): Integer DO
                        //     print("Hello, World!");
                        //     RETURN 0;
                        // END
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(init(new Ast.Method("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                        new Ast.Statement.Expression(init(new Ast.Expression.Function(Optional.empty(), "print", Arrays.asList(
                                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                        new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))))
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "}"
                        )
                )
        );
    }

    /*
    LET x: Integer;
    LET y: Decimal;
    LET z: String;
    DEF f(): Integer DO RETURN x; END
    DEF g(): Decimal DO RETURN y; END
    DEF h(): String DO RETURN z; END
    DEF main(): Integer DO END
     */

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testSourceMultipleFieldsAndMethods(String test, Ast.Source ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testSourceMultipleFieldsAndMethods() {
        return Stream.of(
            Arguments.of(
                "Multiple Fields and Methods",
                new Ast.Source(
                    Arrays.asList(
                        init(new Ast.Field("x", "Integer", true, Optional.empty()), ast -> ast.setVariable(new Environment.Variable("x", "x", Environment.Type.INTEGER, true, Environment.NIL))),
                        init(new Ast.Field("y", "Decimal", true, Optional.empty()), ast -> ast.setVariable(new Environment.Variable("y", "y", Environment.Type.DECIMAL, true, Environment.NIL))),
                        init(new Ast.Field("z", "String", true, Optional.empty()), ast -> ast.setVariable(new Environment.Variable("z", "z", Environment.Type.STRING, true, Environment.NIL)))
                    ),
                    Arrays.asList(
                        init(new Ast.Method(
                                "f",
                                Arrays.asList(),
                                Arrays.asList(),
                                Optional.of("Integer"),
                                Arrays.asList(
                                    new Ast.Statement.Return(
                                        init(new Ast.Expression.Access(Optional.empty(), "x"),
                                             ast -> ast.setVariable(new Environment.Variable("x", "x", Environment.Type.INTEGER, true, Environment.NIL)))
                                    )
                                )
                            ),
                            ast -> ast.setFunction(new Environment.Function("f", "f", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))
                        ),
                        init(new Ast.Method(
                                "g",
                                Arrays.asList(),
                                Arrays.asList(),
                                Optional.of("Decimal"),
                                Arrays.asList(
                                    new Ast.Statement.Return(
                                        init(new Ast.Expression.Access(Optional.empty(), "y"),
                                             ast -> ast.setVariable(new Environment.Variable("y", "y", Environment.Type.DECIMAL, true, Environment.NIL)))
                                    )
                                )
                            ),
                            ast -> ast.setFunction(new Environment.Function("g", "g", Arrays.asList(), Environment.Type.DECIMAL, args -> Environment.NIL))
                        ),
                        init(new Ast.Method(
                                "h",
                                Arrays.asList(),
                                Arrays.asList(),
                                Optional.of("String"),
                                Arrays.asList(
                                    new Ast.Statement.Return(
                                        init(new Ast.Expression.Access(Optional.empty(), "z"),
                                             ast -> ast.setVariable(new Environment.Variable("z", "z", Environment.Type.STRING, true, Environment.NIL)))
                                    )
                                )
                            ),
                            ast -> ast.setFunction(new Environment.Function("h", "h", Arrays.asList(), Environment.Type.STRING, args -> Environment.NIL))
                        ),
                        init(new Ast.Method(
                                "main",
                                Arrays.asList(),
                                Arrays.asList(),
                                Optional.of("Integer"),
                                Arrays.asList()
                            ),
                            ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))
                        )
                    )
                ),
                String.join(System.lineSeparator(),
                    "public class Main {",
                    "",
                    "    final int x;",
                    "    final double y;",
                    "    final String z;",
                    "",
                    "    int f() {",
                    "        return x;",
                    "    }",
                    "",
                    "    double g() {",
                    "        return y;",
                    "    }",
                    "",
                    "    String h() {",
                    "        return z;",
                    "    }",
                    "",
                    "    public static void main(String[] args) {",
                    "        System.exit(new Main().main());",
                    "    }",
                    "",
                    "    int main() {}",
                    "",
                    "}"
                )
            )
        );
    }

    /*
     * 
Method (2)

Square:
DEF square(num: Decimal): Decimal DO
    RETURN num * num;
END
Multiple Statements:
DEF func(x: Integer, y: Decimal, z: String) DO
    print(x);
    print(y);
    print(z);
END
     */

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testMethodSquare(String test, Ast.Method ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testMethodSquare() {
        return Stream.of(
            Arguments.of(
                "Square",
                init(new Ast.Method(
                        "square",
                        Arrays.asList("num"),
                        Arrays.asList("Decimal"),
                        Optional.of("Decimal"),
                        Arrays.asList(
                            new Ast.Statement.Return(
                                init(new Ast.Expression.Binary("*",
                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.DECIMAL, true, Environment.NIL))),
                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.DECIMAL, true, Environment.NIL)))
                                ), ast -> ast.setType(Environment.Type.DECIMAL))
                            )
                        )
                    ),
                    ast -> ast.setFunction(new Environment.Function("square", "square", Arrays.asList(Environment.Type.DECIMAL), Environment.Type.DECIMAL, args -> Environment.NIL))
                ),
                String.join(System.lineSeparator(),
                    "double square(double num) {",
                    "    return num * num;",
                    "}"
                )

            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testMethodMultipleStatements(String test, Ast.Method ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testMethodMultipleStatements() {
        return Stream.of(
            Arguments.of(
                "Multiple Statements",
                init(new Ast.Method(
                        "func",
                        Arrays.asList("x", "y", "z"),
                        Arrays.asList("Integer", "Decimal", "String"),
                        Optional.empty(),
                        Arrays.asList(
                            new Ast.Statement.Expression(
                                init(new Ast.Expression.Function(Optional.empty(),"print", Arrays.asList(
                                        init(new Ast.Expression.Access(Optional.empty(), "x"),
                                                ast -> ast.setVariable(new Environment.Variable("x", "x", Environment.Type.INTEGER, true, Environment.NIL))))
                                ), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))
                            ),
                            new Ast.Statement.Expression(
                                init(new Ast.Expression.Function(Optional.empty(),"print", Arrays.asList(
                                        init(new Ast.Expression.Access(Optional.empty(), "y"),
                                                ast -> ast.setVariable(new Environment.Variable("y", "y", Environment.Type.DECIMAL, true, Environment.NIL))))
                                ), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))
                            ),
                            new Ast.Statement.Expression(
                                init(new Ast.Expression.Function(Optional.empty(),"print", Arrays.asList(
                                        init(new Ast.Expression.Access(Optional.empty(), "z"),
                                                ast -> ast.setVariable(new Environment.Variable("z", "z", Environment.Type.STRING, true, Environment.NIL))))
                                ), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))
                            )
                        )
                    ),
                    ast -> ast.setFunction(new Environment.Function("func", "func", Arrays.asList(Environment.Type.INTEGER, Environment.Type.DECIMAL, Environment.Type.STRING), Environment.Type.NIL, args -> Environment.NIL))
                ),
                String.join(System.lineSeparator(),
                    "Void func(int x, double y, String z) {",
                    "    System.out.println(x);",
                    "    System.out.println(y);",
                    "    System.out.println(z);",
                    "}"
                )
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testWhileMultipleStatements(String test, Ast.Statement.While ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testWhileMultipleStatements() {
        return Stream.of(
            Arguments.of(
                "Multiple Statements",
                new Ast.Statement.While(
                    init(
                        new Ast.Expression.Binary("<",
                            init(
                                new Ast.Expression.Access(Optional.empty(), "num"),
                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))
                            ),
                            init(
                                new Ast.Expression.Literal(BigInteger.valueOf(10)),
                                ast -> ast.setType(Environment.Type.INTEGER)
                            )
                        ),
                        ast -> ast.setType(Environment.Type.BOOLEAN)
                    ),
                    Arrays.asList(
                        new Ast.Statement.Expression(
                            init(
                                new Ast.Expression.Function(Optional.empty(), "print",
                                    Arrays.asList(
                                        init(
                                            new Ast.Expression.Binary("+",
                                                init(
                                                    new Ast.Expression.Access(Optional.empty(), "num"),
                                                    ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))
                                                ),
                                                init(
                                                    new Ast.Expression.Literal("\n"),
                                                    ast -> ast.setType(Environment.Type.STRING)
                                                )
                                            ),
                                            ast -> ast.setType(Environment.Type.STRING)
                                        )
                                    )
                                ),
                                ast -> ast.setFunction(new Environment.Function("print", "System.out.println",
                                    Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))
                            )
                        ),
                        new Ast.Statement.Assignment(
                            init(
                                new Ast.Expression.Access(Optional.empty(), "num"),
                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))
                            ),
                            init(
                                new Ast.Expression.Binary("+",
                                    init(
                                        new Ast.Expression.Access(Optional.empty(), "num"),
                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))
                                    ),
                                    init(
                                        new Ast.Expression.Literal(BigInteger.ONE),
                                        ast -> ast.setType(Environment.Type.INTEGER)
                                    )
                                ),
                                ast -> ast.setType(Environment.Type.INTEGER)
                            )
                        )
                    )
                ),
                String.join(System.lineSeparator(),
                    "while (num < 10) {",
                    "    System.out.println(num + \"\\n\");",
                    "    num = num + 1;",
                    "}"
                )
            )
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testDeclarationStatement(String test, Ast.Statement.Declaration ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testDeclarationStatement() {
        return Stream.of(
                Arguments.of("Declaration",
                        // LET name: Integer;
                        init(new Ast.Statement.Declaration("name", Optional.of("Integer"), Optional.empty()), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.INTEGER, true, Environment.NIL))),
                        "int name;"
                ),
                Arguments.of("Initialization",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.empty(), Optional.of(
                                init(new Ast.Expression.Literal(new BigDecimal("1.0")),ast -> ast.setType(Environment.Type.DECIMAL))
                        )), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.DECIMAL, true, Environment.NIL))),
                        "double name = 1.0;"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testIfStatement(String test, Ast.Statement.If ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testIfStatement() {
        return Stream.of(
                Arguments.of("If",
                        // IF expr DO
                        //     stmt;
                        // END
                        new Ast.Statement.If(
                                init(new Ast.Expression.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, true, Environment.NIL))),
                                Arrays.asList(new Ast.Statement.Expression(init(new Ast.Expression.Access(Optional.empty(), "stmt"), ast -> ast.setVariable(new Environment.Variable("stmt", "stmt", Environment.Type.NIL, true, Environment.NIL))))),
                                Arrays.asList()
                        ),
                        String.join(System.lineSeparator(),
                                "if (expr) {",
                                "    stmt;",
                                "}"
                        )
                ),
                Arguments.of("Else",
                        // IF expr DO
                        //     stmt1;
                        // ELSE
                        //     stmt2;
                        // END
                        new Ast.Statement.If(
                                init(new Ast.Expression.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, true, Environment.NIL))),
                                Arrays.asList(new Ast.Statement.Expression(init(new Ast.Expression.Access(Optional.empty(), "stmt1"), ast -> ast.setVariable(new Environment.Variable("stmt1", "stmt1", Environment.Type.NIL, true, Environment.NIL))))),
                                Arrays.asList(new Ast.Statement.Expression(init(new Ast.Expression.Access(Optional.empty(), "stmt2"), ast -> ast.setVariable(new Environment.Variable("stmt2", "stmt2", Environment.Type.NIL, true, Environment.NIL)))))
                        ),
                        String.join(System.lineSeparator(),
                                "if (expr) {",
                                "    stmt1;",
                                "} else {",
                                "    stmt2;",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testForStatement(String test, Ast.Statement.For ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testForStatement() {
        return Stream.of(
                Arguments.of("For",
                            // for (num = 0; num < 5; num = num + 1)
                            //     print(num);
                            // END
                        new Ast.Statement.For(
                                new Ast.Statement.Assignment(
                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                        init(new Ast.Expression.Literal(BigInteger.valueOf(0)),
                                                ast -> ast.setType(Environment.Type.INTEGER))),
                                init(new Ast.Expression.Binary("<",
                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                init(new Ast.Expression.Literal(BigInteger.valueOf(5)),
                                                        ast -> ast.setType(Environment.Type.INTEGER))),
                                        ast -> ast.setType(Environment.Type.BOOLEAN)),
                                new Ast.Statement.Assignment(
                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                        init(new Ast.Expression.Binary("+",
                                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                        init(new Ast.Expression.Literal(BigInteger.valueOf(1)),
                                                                ast -> ast.setType(Environment.Type.INTEGER))),
                                                ast -> ast.setType(Environment.Type.INTEGER))),
                                Arrays.asList(
                                        new Ast.Statement.Expression(
                                                init(new Ast.Expression.Function(Optional.empty(),"print", Arrays.asList(
                                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))))),
                                                        ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))))
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "for ( num = 0; num < 5; num = num + 1 ) {",
                                "    System.out.println(num);",
                                "}"
                        )
                ),
                Arguments.of("Missing Signature",
                        // for (; num < 5;)
                        //     print(num);
                        // END
                        new Ast.Statement.For(
                                null,
                                init(new Ast.Expression.Binary("<",
                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                init(new Ast.Expression.Literal(BigInteger.valueOf(5)),
                                                        ast -> ast.setType(Environment.Type.INTEGER))),
                                        ast -> ast.setType(Environment.Type.BOOLEAN)),
                                null,
                                Arrays.asList(
                                        new Ast.Statement.Expression(
                                                init(new Ast.Expression.Function(Optional.empty(),"print", Arrays.asList(
                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))))),
                                                        ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                        new Ast.Statement.Assignment(
                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                init(new Ast.Expression.Binary("+",
                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                init(new Ast.Expression.Literal(BigInteger.valueOf(1)),
                                                                        ast -> ast.setType(Environment.Type.INTEGER))),
                                                        ast -> ast.setType(Environment.Type.INTEGER)
                                                )
                                        )
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "for ( ; num < 5; ) {",
                                "    System.out.println(num);",
                                "    num = num + 1;",
                                "}"
                        )
                )
        );
    }

    // for (; num < 5;)
    //     print(num);
    //     num = num + 1;
    // END

//                "for ( num = 0; num < 5; num = num + 1 ) {",
//        "    System.out.println(num);",
//                "END"


    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testBinaryExpression(String test, Ast.Expression.Binary ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("And",
                        // TRUE && FALSE
                        init(new Ast.Expression.Binary("&&",
                                init(new Ast.Expression.Literal(true), ast -> ast.setType(Environment.Type.BOOLEAN)),
                                init(new Ast.Expression.Literal(false), ast -> ast.setType(Environment.Type.BOOLEAN))
                        ), ast -> ast.setType(Environment.Type.BOOLEAN)),
                        "true && false"
                ),
                Arguments.of("Concatenation",
                        // "Ben" + 10
                        init(new Ast.Expression.Binary("+",
                                init(new Ast.Expression.Literal("Ben"), ast -> ast.setType(Environment.Type.STRING)),
                                init(new Ast.Expression.Literal(BigInteger.TEN), ast -> ast.setType(Environment.Type.INTEGER))
                        ), ast -> ast.setType(Environment.Type.STRING)),
                        "\"Ben\" + 10"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testFunctionExpression(String test, Ast.Expression.Function ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Print",
                        // print("Hello, World!")
                        init(new Ast.Expression.Function(Optional.empty(),"print", Arrays.asList(
                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))),
                        "System.out.println(\"Hello, World!\")"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testMultipleMethods(String test, Ast.Source ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testMultipleMethods() {
        return Stream.of(
                Arguments.of("Multiple Methods",
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(
                                        init(new Ast.Method("f", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ONE), ast -> ast.setType(Environment.Type.INTEGER)))
                                        )), ast -> ast.setFunction(new Environment.Function("f", "f", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))),
                                        init(new Ast.Method("g", Arrays.asList(), Arrays.asList(), Optional.of("Decimal"), Arrays.asList(
                                                new Ast.Statement.Return(init(new Ast.Expression.Literal(new BigDecimal("1.0")), ast -> ast.setType(Environment.Type.DECIMAL)))
                                        )), ast -> ast.setFunction(new Environment.Function("g", "g", Arrays.asList(), Environment.Type.DECIMAL, args -> Environment.NIL))),
                                        init(new Ast.Method("h", Arrays.asList(), Arrays.asList(), Optional.of("String"), Arrays.asList(
                                                new Ast.Statement.Return(init(new Ast.Expression.Literal("str"), ast -> ast.setType(Environment.Type.STRING)))
                                        )), ast -> ast.setFunction(new Environment.Function("h", "h", Arrays.asList(), Environment.Type.STRING, args -> Environment.NIL))),
                                        init(new Ast.Method("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.valueOf(-1)), ast -> ast.setType(Environment.Type.INTEGER)))
                                        )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL)))
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    int f() {",
                                "        return 1;",
                                "    }",
                                "",
                                "    double g() {",
                                "        return 1.0;",
                                "    }",
                                "",
                                "    String h() {",
                                "        return \"str\";",
                                "    }",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        return -1;",
                                "    }",
                                "",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testNestedStatements(String test, Ast.Method ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testNestedStatements() {
        return Stream.of(
                Arguments.of("Nested Statements",
                        init(new Ast.Method(
                                        "sumOdds",
                                        Arrays.asList("start", "end"),
                                        Arrays.asList("Integer", "Integer"),
                                        Optional.of("Integer"),
                                        Arrays.asList(
                                                init(new Ast.Statement.Declaration(
                                                        "sum",
                                                        Optional.of("Integer"),
                                                        Optional.of(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                                ), ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),

                                                init(new Ast.Statement.Declaration(
                                                        "num",
                                                        Optional.of("Integer"),
                                                        Optional.empty()
                                                ), ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),

                                                new Ast.Statement.For(
                                                        new Ast.Statement.Assignment(
                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                init(new Ast.Expression.Access(Optional.empty(), "start"),
                                                                        ast -> ast.setVariable(new Environment.Variable("start", "start", Environment.Type.INTEGER, false, Environment.NIL)))
                                                        ),
                                                        init(new Ast.Expression.Binary("<=",
                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                init(new Ast.Expression.Access(Optional.empty(), "end"),
                                                                        ast -> ast.setVariable(new Environment.Variable("end", "end", Environment.Type.INTEGER, false, Environment.NIL)))
                                                        ), ast -> ast.setType(Environment.Type.BOOLEAN)),
                                                        new Ast.Statement.Assignment(
                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                init(new Ast.Expression.Binary("+",
                                                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                        init(new Ast.Expression.Literal(BigInteger.ONE),
                                                                                ast -> ast.setType(Environment.Type.INTEGER))
                                                                ), ast -> ast.setType(Environment.Type.INTEGER))
                                                        ),
                                                        Arrays.asList(
                                                                new Ast.Statement.If(
                                                                        init(new Ast.Expression.Binary("!=",
                                                                                init(new Ast.Expression.Binary("*",
                                                                                        init(new Ast.Expression.Binary("/",
                                                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                                                init(new Ast.Expression.Literal(BigInteger.valueOf(2)),
                                                                                                        ast -> ast.setType(Environment.Type.INTEGER))
                                                                                        ), ast -> ast.setType(Environment.Type.INTEGER)),
                                                                                        init(new Ast.Expression.Literal(BigInteger.valueOf(2)),
                                                                                                ast -> ast.setType(Environment.Type.INTEGER))
                                                                                ), ast -> ast.setType(Environment.Type.INTEGER)),
                                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL)))
                                                                        ), ast -> ast.setType(Environment.Type.BOOLEAN)),
                                                                        Arrays.asList(
                                                                                new Ast.Statement.Assignment(
                                                                                        init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                                                                ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                                        init(new Ast.Expression.Binary("+",
                                                                                                init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                                                                        ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL)))
                                                                                        ), ast -> ast.setType(Environment.Type.INTEGER))
                                                                                )
                                                                        ),
                                                                        Arrays.asList()
                                                                )
                                                        )
                                                ),

                                                new Ast.Statement.Return(
                                                        init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                                ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL)))
                                                )
                                        )
                                ), ast -> ast.setFunction(new Environment.Function("sumOdds", "sumOdds",
                                        Arrays.asList(Environment.Type.INTEGER, Environment.Type.INTEGER),
                                        Environment.Type.INTEGER,
                                        args -> Environment.NIL))
                        ),
                        String.join(System.lineSeparator(),
                                "int sumOdds(int start, int end) {",
                                "    int sum = 0;",
                                "    int num;",
                                "    for ( num = start; num <= end; num = num + 1 ) {",
                                "        if ((num / 2) * 2 != num) {",
                                "            sum = sum + num;",
                                "        }",
                                "    }",
                                "    return sum;",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testNestedIf(String test, Ast.Statement.If ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testNestedIf() {
        return Stream.of(
                Arguments.of("Nested If",
                        new Ast.Statement.If(
                                init(new Ast.Expression.Access(Optional.empty(), "cond1"),
                                        ast -> ast.setVariable(new Environment.Variable("cond1", "cond1", Environment.Type.BOOLEAN, false, Environment.NIL))),
                                Arrays.asList(
                                        new Ast.Statement.If(
                                                init(new Ast.Expression.Access(Optional.empty(), "cond2"),
                                                        ast -> ast.setVariable(new Environment.Variable("cond2", "cond2", Environment.Type.BOOLEAN, false, Environment.NIL))),
                                                Arrays.asList(
                                                        new Ast.Statement.Expression(
                                                                init(new Ast.Expression.Function(Optional.empty(), "function", Arrays.asList(
                                                                        init(new Ast.Expression.Literal(BigInteger.ONE), ast -> ast.setType(Environment.Type.INTEGER))
                                                                )), ast -> ast.setFunction(new Environment.Function("function", "function", Arrays.asList(Environment.Type.INTEGER), Environment.Type.NIL, args -> Environment.NIL)))
                                                        )
                                                ),
                                                Arrays.asList()
                                        )
                                ),
                                Arrays.asList()
                        ),
                        String.join(System.lineSeparator(),
                                "if (cond1) {",
                                "    if (cond2) {",
                                "        function(1);",
                                "    }",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testNestedFor(String test, Ast.Statement.For ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testNestedFor() {
        return Stream.of(
                Arguments.of("Nested For",
                        new Ast.Statement.For(
                                new Ast.Statement.Assignment(
                                        init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),
                                        init(new Ast.Expression.Literal(BigInteger.ZERO),
                                                ast -> ast.setType(Environment.Type.INTEGER))
                                ),
                                init(new Ast.Expression.Binary("<",
                                        init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),
                                        init(new Ast.Expression.Literal(BigInteger.valueOf(5)),
                                                ast -> ast.setType(Environment.Type.INTEGER))
                                ), ast -> ast.setType(Environment.Type.BOOLEAN)),
                                new Ast.Statement.Assignment(
                                        init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),
                                        init(new Ast.Expression.Binary("+",
                                                init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                        ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),
                                                init(new Ast.Expression.Literal(BigInteger.ONE),
                                                        ast -> ast.setType(Environment.Type.INTEGER))
                                        ), ast -> ast.setType(Environment.Type.INTEGER))
                                ),
                                Arrays.asList(
                                        new Ast.Statement.For(
                                                new Ast.Statement.Assignment(
                                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                        init(new Ast.Expression.Literal(BigInteger.ZERO),
                                                                ast -> ast.setType(Environment.Type.INTEGER))
                                                ),
                                                init(new Ast.Expression.Binary("<",
                                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                        init(new Ast.Expression.Literal(BigInteger.valueOf(10)),
                                                                ast -> ast.setType(Environment.Type.INTEGER))
                                                ), ast -> ast.setType(Environment.Type.BOOLEAN)),
                                                new Ast.Statement.Assignment(
                                                        init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                        init(new Ast.Expression.Binary("+",
                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                init(new Ast.Expression.Literal(BigInteger.ONE),
                                                                        ast -> ast.setType(Environment.Type.INTEGER))
                                                        ), ast -> ast.setType(Environment.Type.INTEGER))
                                                ),
                                                Arrays.asList(
                                                        new Ast.Statement.Expression(
                                                                init(new Ast.Expression.Function(Optional.empty(), "print", Arrays.asList(
                                                                        init(new Ast.Expression.Binary("*",
                                                                                init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                                                        ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                                init(new Ast.Expression.Access(Optional.empty(), "num"),
                                                                                        ast -> ast.setVariable(new Environment.Variable("num", "num", Environment.Type.INTEGER, false, Environment.NIL)))
                                                                        ), ast -> ast.setType(Environment.Type.INTEGER))
                                                                )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))
                                                        )
                                                )
                                        )
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "for ( sum = 0; sum < 5; sum = sum + 1 ) {",
                                "    for ( num = 0; num < 10; num = num + 1 ) {",
                                "        System.out.println(sum * num);",
                                "    }",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testNestedWhile(String test, Ast.Statement.While ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testNestedWhile() {
        return Stream.of(
                Arguments.of("Nested While",
                        new Ast.Statement.While(
                                init(new Ast.Expression.Access(Optional.empty(), "cond1"),
                                        ast -> ast.setVariable(new Environment.Variable("cond1", "cond1", Environment.Type.BOOLEAN, false, Environment.NIL))),
                                Arrays.asList(
                                        new Ast.Statement.While(
                                                init(new Ast.Expression.Access(Optional.empty(), "cond2"),
                                                        ast -> ast.setVariable(new Environment.Variable("cond2", "cond2", Environment.Type.BOOLEAN, false, Environment.NIL))),
                                                Arrays.asList(
                                                        new Ast.Statement.Expression(
                                                                init(new Ast.Expression.Function(Optional.empty(), "function", Arrays.asList(
                                                                        init(new Ast.Expression.Literal(BigInteger.ONE), ast -> ast.setType(Environment.Type.INTEGER))
                                                                )), ast -> ast.setFunction(new Environment.Function("function", "function", Arrays.asList(Environment.Type.INTEGER), Environment.Type.NIL, args -> Environment.NIL)))
                                                        )
                                                )
                                        )
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "while (cond1) {",
                                "    while (cond2) {",
                                "        function(1);",
                                "    }",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testLiteral(String test, Ast.Expression.Literal ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testLiteral() {
        return Stream.of(
                Arguments.of("Nil",
                        init(new Ast.Expression.Literal(null), ast -> ast.setType(Environment.Type.NIL)),
                        "null"
                ),
                Arguments.of("Character",
                        init(new Ast.Expression.Literal('a'), ast -> ast.setType(Environment.Type.CHARACTER)),
                        "'a'"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testSum(String test, Ast.Source ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testSum() {
        return Stream.of(
                Arguments.of("Sum",
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(
                                        init(new Ast.Method("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                // Declaration: int i = 1;
                                                init(new Ast.Statement.Declaration("i", Optional.of("Integer"),
                                                                Optional.of(init(new Ast.Expression.Literal(BigInteger.ONE), ast -> ast.setType(Environment.Type.INTEGER)))),
                                                        ast -> ast.setVariable(new Environment.Variable("i", "i", Environment.Type.INTEGER, false, Environment.NIL))),

                                                // Declaration: int sum = 0;
                                                init(new Ast.Statement.Declaration("sum", Optional.empty(),
                                                                Optional.of(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))),
                                                        ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),

                                                // While loop: while (i < 50) { ... }
                                                new Ast.Statement.While(
                                                        init(new Ast.Expression.Binary("<",
                                                                init(new Ast.Expression.Access(Optional.empty(), "i"),
                                                                        ast -> ast.setVariable(new Environment.Variable("i", "i", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                init(new Ast.Expression.Literal(BigInteger.valueOf(50)),
                                                                        ast -> ast.setType(Environment.Type.INTEGER))
                                                        ), ast -> ast.setType(Environment.Type.BOOLEAN)),
                                                        Arrays.asList(
                                                                // Assignment: sum = sum + i;
                                                                new Ast.Statement.Assignment(
                                                                        init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                                                ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                        init(new Ast.Expression.Binary("+",
                                                                                init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                                                        ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                                init(new Ast.Expression.Access(Optional.empty(), "i"),
                                                                                        ast -> ast.setVariable(new Environment.Variable("i", "i", Environment.Type.INTEGER, false, Environment.NIL)))
                                                                        ), ast -> ast.setType(Environment.Type.INTEGER))
                                                                ),
                                                                // Assignment: i = i + 1;
                                                                new Ast.Statement.Assignment(
                                                                        init(new Ast.Expression.Access(Optional.empty(), "i"),
                                                                                ast -> ast.setVariable(new Environment.Variable("i", "i", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                        init(new Ast.Expression.Binary("+",
                                                                                init(new Ast.Expression.Access(Optional.empty(), "i"),
                                                                                        ast -> ast.setVariable(new Environment.Variable("i", "i", Environment.Type.INTEGER, false, Environment.NIL))),
                                                                                init(new Ast.Expression.Literal(BigInteger.ONE),
                                                                                        ast -> ast.setType(Environment.Type.INTEGER))
                                                                        ), ast -> ast.setType(Environment.Type.INTEGER))
                                                                )
                                                        )
                                                ),

                                                // Print statement: print(sum);
                                                new Ast.Statement.Expression(
                                                        init(new Ast.Expression.Function(Optional.empty(), "print", Arrays.asList(
                                                                init(new Ast.Expression.Access(Optional.empty(), "sum"),
                                                                        ast -> ast.setVariable(new Environment.Variable("sum", "sum", Environment.Type.INTEGER, false, Environment.NIL)))
                                                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))
                                                ),

                                                // Return statement: return 0;
                                                new Ast.Statement.Return(
                                                        init(new Ast.Expression.Literal(BigInteger.ZERO),
                                                                ast -> ast.setType(Environment.Type.INTEGER))
                                                )
                                        )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL)))
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        int i = 1;",
                                "        int sum = 0;",
                                "        while (i < 50) {",
                                "            sum = sum + i;",
                                "            i = i + 1;",
                                "        }",
                                "        System.out.println(sum);",
                                "        return 0;",
                                "    }",
                                "",
                                "}"
                        )
                )
        );
    }

    /**
     * Helper function for tests, using a StringWriter as the output stream.
     */
    private static void test(Ast ast, String expected) {
        StringWriter writer = new StringWriter();
        new Generator(new PrintWriter(writer)).visit(ast);
        Assertions.assertEquals(expected, writer.toString());
    }

    /**
     * Runs a callback on the given value, used for inline initialization.
     */
    private static <T> T init(T value, Consumer<T> initializer) {
        initializer.accept(value);
        return value;
    }

}
