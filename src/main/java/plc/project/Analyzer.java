package plc.project;

import java.beans.Expression;
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
        ast.getFields().forEach(this::visit);
        ast.getMethods().forEach(this::visit);

        boolean found = false;
        for (Ast.Method method : ast.getMethods()) {
            if (method.getName().equals("main")) {
                found = true;
                if (!method.getParameters().isEmpty()) {
                    throw new RuntimeException("The main method should not have any parameters.");
                }
                if (method.getReturnTypeName().isEmpty() || !method.getReturnTypeName().get().equals("integer")) {
                    throw new RuntimeException("The main method should return an integer.");
                }
            }
        }
        if (!found) throw new RuntimeException("Main method not found.");

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        if (ast.getValue().isPresent()) {
            visit(ast.getValue().get());
            requireAssignable(Environment.getType(ast.getTypeName()), ast.getValue().get().getType());
        }
        else if (ast.getConstant()) throw new RuntimeException("Constant field must have an initial value assigned to it when the Field is declared.");

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
        visit(ast.getExpression());
        if (!(ast.getExpression() instanceof Ast.Expression.Function)) 
            throw new RuntimeException("Expression is not a function");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        if (ast.getValue().isPresent()) visit(ast.getValue().get());

        Environment.Type type;
        if (ast.getTypeName().isPresent()) type = Environment.getType(ast.getTypeName().get());
        else if (ast.getValue().isPresent()) type = ast.getValue().get().getType();
        else throw new RuntimeException("The variable's type is not present.");

        if (ast.getTypeName().isPresent() && ast.getValue().isPresent())
            requireAssignable(type, ast.getValue().get().getType());

        Environment.Variable variable = this.scope.defineVariable(
                ast.getName(),
                ast.getName(),
                type,
                false,
                Environment.NIL
        );
        ast.setVariable(variable);

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        visit(ast.getReceiver());
        visit(ast.getValue());

        if (!(ast.getReceiver() instanceof Ast.Expression.Access accessExpression))
            throw new RuntimeException("The receiver is not an Access expression");

        if (accessExpression.getVariable().getConstant())
            throw new RuntimeException("Cannot assign to a constant field.");

        requireAssignable(accessExpression.getVariable().getType(), ast.getValue().getType());

        //scope.defineVariable(accessExpression.getVariable().getName(), true, accessExpression.getVariable().getValue());
        
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        visit(ast.getCondition());
        if (ast.getCondition().getType() != Environment.Type.BOOLEAN)
            throw new RuntimeException("The condition is not of type Boolean");

        if (ast.getThenStatements().isEmpty())
            throw new RuntimeException("The thenStatements list is empty.");

        Scope thenScope = new Scope(this.scope);
        Scope elseScope = new Scope(this.scope);

        Scope prev = this.scope;
        this.scope = thenScope;
        for(Ast.Statement stmt : ast.getThenStatements()){
            visit(stmt);
        }
        this.scope = elseScope;
        for(Ast.Statement stmt : ast.getElseStatements()){
            visit(stmt);
        }
        this.scope = prev;
        return null;
    }

    @Override
    public Void visit(Ast.Statement.For ast) {
        if (ast.getInitialization() != null) {
            visit(ast.getInitialization());
            if (ast.getInitialization() instanceof Ast.Statement.Declaration declaration) {
                if (declaration.getTypeName().isPresent()) {
                    Environment.Type type = Environment.getType(declaration.getTypeName().get());
                    if (type != Environment.Type.COMPARABLE)
                        throw new RuntimeException("The identifier is not a Comparable type.");
                } else {
                    throw new RuntimeException("The identifier is not a Comparable type.");
                }
            }
        }

        visit(ast.getCondition());
        if (ast.getCondition().getType() != Environment.Type.BOOLEAN)
            throw new RuntimeException("The condition is not of type Boolean");


        if (ast.getIncrement() != null) {
            visit(ast.getIncrement());
            if (ast.getInitialization() instanceof Ast.Statement.Declaration declaration &&
                ast.getIncrement() instanceof Ast.Statement.Expression expression &&
                expression.getExpression().getType() != declaration.getVariable().getType()) 
                    throw new RuntimeException("The expression in the increment is not the same type as the identifier given at the start of the For signature.");
        }

        if (ast.getStatements().isEmpty())
            throw new RuntimeException("The list of statements is empty.");

        Scope scope = new Scope(this.scope);
        Scope prev = this.scope;
        this.scope = scope;
        ast.getStatements().forEach(this::visit);
        this.scope = prev;
        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        visit(ast.getCondition());
        if (ast.getCondition().getType() != Environment.Type.BOOLEAN)
            throw new RuntimeException("The value is not of type boolean");

        Scope scope = new Scope(this.scope);
        Scope prev = this.scope;
        
        this.scope = scope;

        ast.getStatements().forEach(this::visit);
        
        this.scope = prev;
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        Ast.Expression expression = ast.getValue();
        visit(expression);
        Environment.Type returnType = this.method.getFunction().getReturnType();
        Environment.Type valueType = expression.getType();
        requireAssignable(returnType, valueType);
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        final Object literal = ast.getLiteral();
        if (literal == null) {
            ast.setType(Environment.Type.NIL);
            return null;
        }
        if (literal instanceof Boolean) {
            ast.setType(Environment.Type.BOOLEAN);
            return null;
        }
        if (literal instanceof Character) {
            ast.setType(Environment.Type.CHARACTER);
            return null;
        }
        if (literal instanceof String) {
            ast.setType(Environment.Type.STRING);
            return null;
        }

        if (literal instanceof BigInteger) {
            BigInteger value = (BigInteger) literal;
            if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 || 
                value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) 
                throw new RuntimeException("value is out of range of a Java int (32-bit signed int)");
            ast.setType(Environment.Type.INTEGER);
            return null;
        }

        if (literal instanceof BigDecimal) {
            BigDecimal value = (BigDecimal) literal;
            if (Double.isInfinite(value.doubleValue()) || Double.isNaN(value.doubleValue()))
                throw new RuntimeException("value is out of range of a Java double value (64-bit signed float)");
            ast.setType(Environment.Type.DECIMAL);
            return null;
        }

        throw new RuntimeException("The literal is not of a valid type");
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        visit(ast.getExpression());
        if (!(ast.getExpression() instanceof Ast.Expression.Binary))
            throw new RuntimeException("The contained expression is not a binary expression");
        ast.setType(ast.getExpression().getType());
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        visit(ast.getLeft());
        visit(ast.getRight());
        
        switch (ast.getOperator()) {
            case "AND", "OR" -> {
                if (ast.getLeft().getType() != Environment.Type.BOOLEAN || ast.getRight().getType() != Environment.Type.BOOLEAN)
                    throw new RuntimeException("Both operands must be Boolean.");
                if (ast.getLeft().getType() != ast.getRight().getType())
                    throw new RuntimeException("Result type will be Boolean.");
                ast.setType(Environment.Type.BOOLEAN);
                return null;
            }
            case "<", "<=", ">", ">=", "==", "!=" -> {
                if(!isComparable(ast.getLeft().getType()) || !isComparable(ast.getRight().getType())){
                    throw new RuntimeException("Both operands must be Comparable.");
                }
                if(ast.getLeft().getType() != ast.getRight().getType()){
                    throw new RuntimeException("Both operands must be the same type.");
                }
                ast.setType(Environment.Type.BOOLEAN);
                return null;
            }
            case "+" -> {
                if (ast.getLeft().getType() == Environment.Type.STRING || ast.getRight().getType() == Environment.Type.STRING) {
                    ast.setType(Environment.Type.STRING);
                    return null;
                }

                if (ast.getLeft().getType() != Environment.Type.INTEGER && ast.getLeft().getType() != Environment.Type.DECIMAL)
                    throw new RuntimeException("The LHS must be an Integer/Decimal.");

                if (ast.getRight().getType() != ast.getLeft().getType())
                    throw new RuntimeException("Both the RHS and result types are the same as the LHS.");

                ast.setType(ast.getLeft().getType());
                return null;
            }
            case "-", "*", "/" -> {
                if (ast.getLeft().getType() != Environment.Type.INTEGER && ast.getLeft().getType() != Environment.Type.DECIMAL)
                    throw new RuntimeException("The LHS must be an Integer/Decimal.");

                if (ast.getRight().getType() != ast.getLeft().getType())
                    throw new RuntimeException("Both the RHS and result types are the same as the LHS.");

                ast.setType(ast.getLeft().getType());
                return null;
            }
        }

        throw new RuntimeException("The operator is not valid");
    }

    private boolean isComparable(Environment.Type type){
        return type == Environment.Type.COMPARABLE || type == Environment.Type.INTEGER || type == Environment.Type.DECIMAL || type == Environment.Type.CHARACTER || type == Environment.Type.STRING;
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        Environment.Variable variable;
        if (ast.getReceiver().isPresent()) {
            visit(ast.getReceiver().get());
            Environment.Type type = ast.getReceiver().get().getType();
            variable = type.getField(ast.getName());
        } else variable = this.scope.lookupVariable(ast.getName());

        ast.setVariable(variable);
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        Environment.Function function;
        if (ast.getReceiver().isPresent()) {
            visit(ast.getReceiver().get());
            Environment.Type type = ast.getReceiver().get().getType();
            try {
                function = type.getFunction(ast.getName(), ast.getArguments().size());
            } catch (RuntimeException e) {
                throw new RuntimeException("Function not found in the receiver's type.");
            }
        } else function = scope.lookupFunction(ast.getName(), ast.getArguments().size());

        List<Environment.Type> types = function.getParameterTypes();
        for (int i = 0; i < ast.getArguments().size(); i++) {
            Ast.Expression expression = ast.getArguments().get(i);
            visit(expression);
            Environment.Type argType = expression.getType();
            Environment.Type expectedType = types.get(ast.getReceiver().isPresent() ? i + 1 : i);
            requireAssignable(expectedType, argType);
        }

        ast.setFunction(function);

        return null;
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        if (target == type || target == Environment.Type.ANY) return;

        if (target != Environment.Type.COMPARABLE)
            throw new RuntimeException("Target type is not comparable.");

        if (type == Environment.Type.INTEGER || type == Environment.Type.DECIMAL || 
            type == Environment.Type.CHARACTER || type == Environment.Type.STRING)
            return;

        throw new RuntimeException("The target type is not assignable to the given type.");
    }

}
