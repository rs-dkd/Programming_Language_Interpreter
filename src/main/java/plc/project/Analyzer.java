package plc.project;

import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {
    public Scope scope;
    private Ast.Method method;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) {
        for (Ast.Method method : ast.getMethods()) {
            if (method.getName().equals("main")) {
                if (!method.getParameters().isEmpty()) {
                    throw new RuntimeException("The main method should not have any parameters.");
                }
                if (method.getReturnTypeName().isEmpty() || !method.getReturnTypeName().get().equals("integer")) {
                    throw new RuntimeException("The main method should return an integer.");
                }
                return null;
            }
        }
        throw new RuntimeException("Main method not found.");
    }

    /* TODO: Additionally, throws a RuntimeException if: The value, if present, is not assignable to the Field. For a value to be assignable, its type must be a subtype of the Field's type, as defined above. */
    @Override
    public Void visit(Ast.Field ast) {
        if (ast.getValue().isPresent()) visit(ast.getValue().get());

        this.scope.defineVariable(ast.getName(), ast.getName(), Environment.getType(ast.getTypeName()), ast.getConstant(), Environment.NIL);
        ast.setVariable(scope.lookupVariable(ast.getName()));
        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        List<Environment.Type> paramTypes = ast.getParameters().stream()
                .map(Environment::getType)
                .toList();
        this.scope.defineFunction(
                ast.getName(),
                ast.getName(),
                paramTypes,
                ast.getReturnTypeName().isPresent() ?
                        Environment.getType(ast.getReturnTypeName().get()) : Environment.Type.NIL,
                args -> Environment.NIL
        );
        Environment.Function function = scope.lookupFunction(ast.getName(), paramTypes.size());

        ast.setFunction(function);

        this.method = ast;
        Scope methodScope = new Scope(this.scope);

        for (int i = 0; i < this.method.getParameters().size(); i++) {
            /* Not sure if Environment.NIL was correct here? */
            methodScope.defineVariable(this.method.getParameters().get(i), false, Environment.NIL);
        }

        Scope prev = this.scope;
        this.scope = methodScope;
        for (int i = 0; i < this.method.getStatements().size(); i++) {
            visit(this.method.getStatements().get(i));
        }
        this.scope = prev;
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        if (!(ast.getExpression() instanceof Ast.Expression.Function)) throw new RuntimeException("Expression is not a function");
        return null;
    }

    /* TODO: Additionally, throws a RuntimeException if: The value, if present, is not assignable to the variable (see Ast.Field for info).*/
    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        if (ast.getValue().isPresent()) visit(ast.getValue().get());

        this.scope.defineVariable(
                ast.getName(),
                ast.getName(),
                ast.getTypeName().isPresent() ? Environment.getType(ast.getTypeName().get()) : ast.getValue().orElseThrow().getType(),
                ast.getVariable().getConstant(),
                Environment.NIL);

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        if (!(ast.getReceiver() instanceof Ast.Expression.Access accessExpression))
            throw new RuntimeException("The receiver is not an Access expression");


        /* TODO: The value is not assignable to the receiver (see Ast.Field for info). */

        scope.defineVariable(accessExpression.getVariable().getName(), true, accessExpression.getVariable().getValue());
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        if (ast.getCondition().getType() != Environment.Type.BOOLEAN)
            throw new RuntimeException("The condition is not of type Boolean");

        if (ast.getThenStatements().isEmpty())
            throw new RuntimeException("The thenStatements list is empty.");

        Scope scope = new Scope(this.scope);

        Scope prev = this.scope;
        this.scope = scope;

        ast.getThenStatements().forEach(this::visit);
        ast.getElseStatements().forEach(this::visit);

        this.scope = prev;
        return null;
    }

    @Override
    public Void visit(Ast.Statement.For ast) {
        //Environment.getType(ast.)
        //if (((Ast.Statement.For)ast.getInitialization())..getType() != Environment.Type.COMPARABLE)

        if (ast.getCondition().getType() != Environment.Type.BOOLEAN)
            throw new RuntimeException("The condition is not of type Boolean");

        if (ast.getIncrement() != ast.get);

        if (ast.getStatements().isEmpty())
            throw new RuntimeException("The list of statements is empty.");

        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        if (ast.getCondition().getType() != Environment.Type.BOOLEAN)
            throw new RuntimeException("The value is not of type boolean");

        ast.getStatements().forEach(this::visit);
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        final Object literal = ast.getLiteral();
        if (literal)
        if (type == Environment.Type.NIL ||
            type == Environment.Type.BOOLEAN ||
            type == Environment.Type.CHARACTER ||
            type == Environment.Type.STRING
        ) return null;

        if (type == Environment.Type.INTEGER)
            try {
                BigInteger.valueOf((long) ast.getLiteral());
            } catch(Exception e) {
                throw new RuntimeException(e);
            }

        if (type == Environment.Type.DECIMAL)
            try {
                new BigDecimal(ast.getLiteral().toString()).doubleValue();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }

        ast.setType();


        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        throw new UnsupportedOperationException();  // TODO
    }

}
