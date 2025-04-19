package plc.project;

import java.io.PrintWriter;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        print("public class Main {");
        if(!ast.getFields().isEmpty()){
            newline(0);
            for(Ast.Field field : ast.getFields()){
                newline(1);
                visit(field);
            }
        }
        for(Ast.Method method : ast.getMethods()){
            if(method.getName().equals("main")) continue;
            newline(0);
            newline(1);
            visit(method);
        }
        newline(0);
        newline(1);
        print("public static void main(String[] args) {");
        newline(2);
        print("System.exit(new Main().main());");
        newline(1);
        print("}");
        for(Ast.Method method : ast.getMethods()){
            if (!method.getName().equals("main")) continue;
            newline(0);
            newline(1);
            visit(method);
        }
        newline(0);
        newline(0);
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        if (ast.getConstant()) print("final ");
        print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName());
        if(ast.getValue().isPresent()){
            print(" = ", ast.getValue().get());
        }
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        print(ast.getFunction().getReturnType() == Environment.Type.NIL ? "void" : ast.getFunction().getReturnType().getJvmName(), " ", ast.getFunction().getJvmName(), "(");
        for(int i = 0; i < ast.getParameters().size(); i++){
            if(i > 0) print(", ");
            print(ast.getFunction().getParameterTypes().get(i).getJvmName(), " ", ast.getParameters().get(i));
        }
        print(") {");
        if(!ast.getStatements().isEmpty()){
            for(Ast.Statement statement : ast.getStatements()){
                newline(1);
                visit(statement);
            }
            newline(0);
        }
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        print(ast.getExpression(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName());
        if(ast.getValue().isPresent()){
            print(" = ", ast.getValue().get());
        }

        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        visit(ast.getReceiver());
        print(" = ");
        visit(ast.getValue());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        print("if (");
        visit(ast.getCondition());
        print(") {");
        for(Ast.Statement statement : ast.getThenStatements()){
            newline(1);
            visit(statement);
        }
        newline(0);
        print("}");
        if(!ast.getElseStatements().isEmpty()){
            print(" else {");
            for(Ast.Statement statement : ast.getElseStatements()){
                newline(1);
                visit(statement);
            }
            newline(0);
            print("}");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.For ast) {
        print("for ( ");
        if(ast.getInitialization() != null){
            switch(ast.getInitialization()){
                case Ast.Statement.Declaration decl ->{
                    print(decl.getVariable().getType().getJvmName(), " ", decl.getVariable().getJvmName());
                    if(decl.getValue().isPresent()){
                        print(" = ", decl.getValue().get());
                    }
                }
                case Ast.Statement.Assignment assign ->{
                    visit(assign.getReceiver());
                    print(" = ");
                    visit(assign.getValue());
                }
                case Ast.Statement.Expression expr -> visit(expr.getExpression());
                default -> {
                }
            }
        }
        print("; ");
        visit(ast.getCondition());
        print(";");
        if(ast.getIncrement() != null){
            print(" ");
            if(ast.getIncrement() instanceof Ast.Statement.Expression expr){
                visit(expr.getExpression());
            }else if(ast.getIncrement() instanceof Ast.Statement.Assignment assign){
                visit(assign.getReceiver());
                print(" = ");
                visit(assign.getValue());
            }
        }
        print(" ) {");
        for(Ast.Statement statement : ast.getStatements()){
            newline(1);
            visit(statement);
        }
        newline(0);
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        print("while (", ast.getCondition(), ") {");
        if(!ast.getStatements().isEmpty()){
            for(Ast.Statement statement : ast.getStatements()){
                newline(1);
                visit(statement);
            }
            newline(0);
        }
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        print("return ", ast.getValue(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if(ast.getLiteral() instanceof String){
            print("\"", ast.getLiteral().toString().replace("\n", "\\n"), "\"");
        }else{
            print(ast.getLiteral());
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        print("(", ast.getExpression(), ")");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        print(ast.getLeft(), " ", ast.getOperator(), " ", ast.getRight());
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        if(ast.getReceiver().isPresent()){
            print(ast.getReceiver().get(), ".");
        }
        print(ast.getVariable().getJvmName());
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        if(ast.getReceiver().isPresent()){
            print(ast.getReceiver().get(), ".");
        }
        print(ast.getFunction().getJvmName(), "(");
        for(int i = 0; i < ast.getArguments().size(); i++){
            if(i > 0){
                print(", ");
            }
            print(ast.getArguments().get(i));
        }
        print(")");
        return null;
    }
}
