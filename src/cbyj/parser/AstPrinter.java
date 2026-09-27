// package cbyj.parser;

// public class AstPrinter implements Expr.Visitor<String>{
// 	public String print(Expr expr){
// 		return expr.accept(this);
// 	}
// 	@Override
// 	public String visitBinary(Expr.Binary binary){
// 		return parentTheSize(binary.operator.lexeme,
// 							 binary.left,
// 							 binary.right);
// 	}
// 	@Override
// 	public String visitGrouping(Expr.Grouping grouping){
// 		return parentTheSize("group", grouping.expr);
// 	}
// 	@Override
// 	public String visitLiteral(Expr.Literal literal){
// 		if(literal.value == null) return "nil";
// 		return literal.value.toString();
// 	}
// 	@Override
// 	public String visitUnary(Expr.Unary unary){
// 		return parentTheSize(unary.operator.lexeme,unary.expr);
// 	}
// 	private String parentTheSize(String name,Expr... exprs){
// 		StringBuilder builder = new StringBuilder();

// 		builder.append("(").append(name);
// 		for (Expr expr : exprs) {
// 			builder.append(" ");
// 			builder.append(expr.accept(this));
// 		}
// 		builder.append(")");
		
// 		// builder.append("[ ")
// 		// 	.append(name)
// 		// 	.append(" ]---\n");
// 		// for (Expr expr : exprs) {
// 		// 	builder.append("       |-")
// 		// 		.append(expr.accept(this))
// 		// 		.append("\n");
// 		// }
// 		return builder.toString();
// 	}
// }
