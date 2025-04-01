package lox;

public class TestAstPrinter {
    public static void main(String[] args) {
        Expr expr = new Expr.ReversePolish(
                new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(15)),
                new Token(TokenType.SLASH, "/", null, 1),
                new Expr.Grouping(new Expr.Literal(3))
        );
        System.out.println(new AstPrinter().print(expr));
    }
}
