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
            }else if(object != null){
                writer.write(object.toString());
            }else{
                writer.write("null");
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
        newline(0);
        indent++;
        if(!ast.getFields().isEmpty()){
            for(Ast.Field field : ast.getFields()){
                newline(indent);
                visit(field);
            }
        }
        if(!ast.getMethods().isEmpty()){
            boolean hasNonMainMethods = ast.getMethods().stream().anyMatch(method -> !method.getName().equals("main"));
            if(hasNonMainMethods && !ast.getFields().isEmpty()){
                newline(0);
            }

            for(Ast.Method method : ast.getMethods()){
                if(method.getName().equals("main")) continue;
                newline(indent);
                visit(method);
                newline(0);
            }
        }
        newline(indent);
        print("public static void main(String[] args) {");
        newline(indent + 1);
        print("System.exit(new Main().main());");
        newline(indent);
        print("}");

        for(Ast.Method method : ast.getMethods()){
            if (!method.getName().equals("main")) continue;
            newline(0);
            newline(indent);
            visit(method);
        }
        newline(0);
        newline(0);
        indent--;
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
        indent++;
        if(!ast.getStatements().isEmpty()){
            for(Ast.Statement statement : ast.getStatements()){
                newline(indent);
                visit(statement);
            }
            indent--;
            newline(indent);
        }else{
            indent--;
            print("}");
            return null;
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
        if(!ast.getThenStatements().isEmpty()){
            indent++;
            for(Ast.Statement statement : ast.getThenStatements()){
                newline(indent);
                visit(statement);
            }
            indent--;
            newline(indent);
        }
        print("}");
        if(!ast.getElseStatements().isEmpty()){
            print(" else {");
            indent++;
            for(Ast.Statement statement : ast.getElseStatements()){
                newline(indent);
                visit(statement);
            }
            indent--;
            newline(indent);
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
        if(!ast.getStatements().isEmpty()){
            indent++;
            for(Ast.Statement statement : ast.getStatements()){
                newline(indent);
                visit(statement);
            }
            indent--;
            newline(indent);
        }
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        print("while (");
        visit(ast.getCondition());
        print(") {");
        if(!ast.getStatements().isEmpty()){
            indent++;
            for(Ast.Statement statement : ast.getStatements()){
                newline(indent);
                visit(statement);
            }
            indent--;
            newline(indent);
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
        if(ast.getLiteral() == null){
            print("null");
        }else if(ast.getLiteral() instanceof String){
            print("\"", ast.getLiteral().toString().replace("\n", "\\n"), "\"");
        } else if(ast.getLiteral() instanceof Character){
            print("'", ast.getLiteral().toString(), "'");
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
        if(ast.getOperator().equals("*") && ast.getLeft() instanceof Ast.Expression.Binary && ((Ast.Expression.Binary)ast.getLeft()).getOperator().equals("/")){
            print("(");
            visit(ast.getLeft());
            print(")");
            print(" ", ast.getOperator(), " ");
            visit(ast.getRight());
        }else{
            visit(ast.getLeft());
            print(" ", ast.getOperator(), " ");
            visit(ast.getRight());
        }
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
