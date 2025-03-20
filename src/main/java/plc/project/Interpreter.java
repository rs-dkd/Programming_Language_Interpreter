package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {
        for(Ast.Field f : ast.getFields()){
            visit(f);
        }
        for(Ast.Method m : ast.getMethods()){
            visit(m);
        }
        return scope.lookupFunction("main", 0).invoke(new ArrayList<>());
    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast) {
        Environment.PlcObject val = ast.getValue().isPresent() ? visit(ast.getValue().get()) : Environment.NIL;
        scope.defineVariable(ast.getName(), false, val);
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Method ast) {
        scope.defineFunction(ast.getName(), ast.getParameters().size(), args -> {
            Scope curr = scope;
            try{
                for(int i = 0; i < args.size(); i++){
                    scope.defineVariable(ast.getParameters().get(i), false, args.get(i));
                }
                for(Ast.Statement statement : ast.getStatements()){
                    visit(statement);
                }
                return Environment.NIL;
            }catch(Return returnVal){
                return returnVal.value;
            }finally{
                scope = curr;
            }
        });
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Expression ast) {
        visit(ast.getExpression());
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Declaration ast) {
        Environment.PlcObject val =  ast.getValue().isPresent() ? visit(ast.getValue().get()) : Environment.NIL;
        scope.defineVariable(ast.getName(), false, val);
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Assignment ast) {
        if(!(ast.getReceiver() instanceof Ast.Expression.Access access)){
            throw new RuntimeException("Non-access expression");
        }
        Environment.PlcObject val = visit(ast.getValue());
        if(access.getReceiver().isPresent()){
            Environment.PlcObject receiver = visit(access.getReceiver().get());
            receiver.setField(access.getName(), val);
        }else{
            Environment.Variable var = scope.lookupVariable(access.getName());
            if(var.getConstant()){
                throw new RuntimeException("Cannot access constant variable" + access.getName());
            }
            var.setValue(val);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.If ast) {
        Environment.PlcObject cond = visit(ast.getCondition());
        requireType(Boolean.class, cond);
        Scope curr = scope;
        scope = new Scope(curr);
        try{
            if((Boolean) cond.getValue()){
                for(Ast.Statement statement : ast.getThenStatements()){
                    visit(statement);
                }
            }else{
                for(Ast.Statement statement : ast.getElseStatements()){
                    visit(statement);
                }
            }
            return Environment.NIL;
        }finally{
            scope = curr;
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.For ast) {
        Scope curr = scope;
        scope = new Scope(curr);
        try{
            if(ast.getInitialization() != null){
                visit(ast.getInitialization());
            }
            while(true){
                if(ast.getCondition() != null){
                    Environment.PlcObject cond = visit(ast.getCondition());
                    requireType(Boolean.class, cond);
                    if(!(Boolean) cond.getValue()){
                        break;
                    }
                }
                for(Ast.Statement statement : ast.getStatements()){
                    visit(statement);
                }
                if(ast.getIncrement() != null){
                    visit(ast.getIncrement());
                }
            }
            return Environment.NIL;
        }finally{
            scope = curr;
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.While ast) {
        Scope curr = scope;
        scope = new Scope(curr);
        try{
            while(true){
                Environment.PlcObject cond = visit(ast.getCondition());
                requireType(Boolean.class, cond);
                if(!(Boolean) cond.getValue()){
                    break;
                }
                for(Ast.Statement statement : ast.getStatements()){
                    visit(statement);
                }
            }
            return Environment.NIL;
        }finally{
            scope = curr;
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Return ast) {
        Environment.PlcObject val = visit(ast.getValue());
        throw new Return(val);
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Literal ast) {
        if(ast.getLiteral() == null){
            return Environment.NIL;
        }
        return Environment.create(ast.getLiteral());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Group ast) {
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Binary ast) {
        Environment.PlcObject left = visit(ast.getLeft());
        switch(ast.getOperator()){
            case "&&":{
                requireType(Boolean.class, left);
                if(!(Boolean) left.getValue()){
                    return Environment.create(false);
                }
                Environment.PlcObject right = visit(ast.getRight());
                requireType(Boolean.class, right);
                return Environment.create((Boolean) right.getValue());
            }
            case "||":{
                requireType(Boolean.class, left);
                if((Boolean) left.getValue()){
                    return Environment.create(true);
                }
                Environment.PlcObject right = visit(ast.getRight());
                requireType(Boolean.class, right);
                return Environment.create((Boolean) right.getValue());
            }
            case "<":{
                Environment.PlcObject right = visit(ast.getRight());
                if(!(left.getValue() instanceof Comparable) || left.getValue().getClass() != right.getValue().getClass()){
                    throw new RuntimeException("Operands not Comparable or same type");
                }
                Comparable<Object> leftComparable = (Comparable<Object>) left.getValue();
                int comparison = leftComparable.compareTo(right.getValue());
                return Environment.create(comparison < 0);
            }
            case "<=":{
                Environment.PlcObject right = visit(ast.getRight());
                if(!(left.getValue() instanceof Comparable) || left.getValue().getClass() != right.getValue().getClass()){
                    throw new RuntimeException("Operands not Comparable or same type");
                }
                Comparable<Object> leftComparable = (Comparable<Object>) left.getValue();
                int comparison = leftComparable.compareTo(right.getValue());
                return Environment.create(comparison <= 0);
            }
            case ">":{
                Environment.PlcObject right = visit(ast.getRight());
                if(!(left.getValue() instanceof Comparable) || left.getValue().getClass() != right.getValue().getClass()){
                    throw new RuntimeException("Operands not Comparable or same type");
                }
                Comparable<Object> leftComparable = (Comparable<Object>) left.getValue();
                int comparison = leftComparable.compareTo(right.getValue());
                return Environment.create(comparison > 0);
            }
            case ">=":{
                Environment.PlcObject right = visit(ast.getRight());
                if(!(left.getValue() instanceof Comparable) || left.getValue().getClass() != right.getValue().getClass()){
                    throw new RuntimeException("Operands not Comparable or same type");
                }
                Comparable<Object> leftComparable = (Comparable<Object>) left.getValue();
                int comparison = leftComparable.compareTo(right.getValue());
                return Environment.create(comparison >= 0);
            }
            case "==":{
                Environment.PlcObject right = visit(ast.getRight());
                return Environment.create(Objects.equals(left.getValue(), right.getValue()));
            }
            case "!=":{
                Environment.PlcObject right = visit(ast.getRight());
                return Environment.create(!Objects.equals(left.getValue(), right.getValue()));
            }
            case "+":{
                Environment.PlcObject right = visit(ast.getRight());
                if(left.getValue() instanceof String || right.getValue() instanceof String){
                    return Environment.create(left.getValue().toString() + right.getValue().toString());
                }
                if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger){
                    return Environment.create(((BigInteger) left.getValue()).add((BigInteger) right.getValue()));
                }
                if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal){
                    return Environment.create(((BigDecimal) left.getValue()).add((BigDecimal) right.getValue()));
                }
                throw new RuntimeException("Invalid operands");
            }
            case "-":{
                Environment.PlcObject right = visit(ast.getRight());
                if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger){
                    return Environment.create(((BigInteger) left.getValue()).subtract((BigInteger) right.getValue()));
                }
                if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal){
                    return Environment.create(((BigDecimal) left.getValue()).subtract((BigDecimal) right.getValue()));
                }
                throw new RuntimeException("Invalid operands");
            }
            case "*":{
                Environment.PlcObject right = visit(ast.getRight());
                if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger){
                    return Environment.create(((BigInteger) left.getValue()).multiply((BigInteger) right.getValue()));
                }
                if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal){
                    return Environment.create(((BigDecimal) left.getValue()).multiply((BigDecimal) right.getValue()));
                }
                throw new RuntimeException("Invalid operands");
            }
            case "/":{
                Environment.PlcObject right = visit(ast.getRight());
                if(left.getValue() instanceof BigInteger && right.getValue() instanceof BigInteger divisor){
                    if(divisor.equals(BigInteger.ZERO)){
                        throw new RuntimeException("Division by zero");
                    }
                    return Environment.create(((BigInteger) left.getValue()).divide(divisor));
                }
                if(left.getValue() instanceof BigDecimal && right.getValue() instanceof BigDecimal divisor){
                    if(divisor.equals(BigDecimal.ZERO)){
                        throw new RuntimeException("Division by zero");
                    }
                    return Environment.create(((BigDecimal) left.getValue()).divide(divisor, RoundingMode.HALF_EVEN));
                }
                throw new RuntimeException("Invalid operands");
            }
            default:
                throw new RuntimeException("Invalid binary operator" + ast.getOperator());
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Access ast) {
        if(ast.getReceiver().isPresent()){
            Environment.PlcObject receiver = visit(ast.getReceiver().get());
            return receiver.getField(ast.getName()).getValue();
        }else{
            return scope.lookupVariable(ast.getName()).getValue();
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Function ast) {
        List<Environment.PlcObject> args = new ArrayList<>();
        for(Ast.Expression arg : ast.getArguments()){
            args.add(visit(arg));
        }
        if(ast.getReceiver().isPresent()){
            Environment.PlcObject receiver = visit(ast.getReceiver().get());
            return receiver.callMethod(ast.getName(), args);
        }else{
            return scope.lookupFunction(ast.getName(), args.size()).invoke(args);
        }
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
