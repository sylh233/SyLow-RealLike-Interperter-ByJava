package cbyj.parser;

import java.util.List;
import cbyj.lexer.*;
import java.util.Map;
import java.util.HashMap;

abstract public class Expr {
	abstract <R> R accept(Visitor<R> visitor);
	static class Binary extends Expr{
		final Expr left,right;
		final Token operator;
		Binary(Expr left,Token operator,Expr right){
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinary(this);
		}
	}

	static class Grouping extends Expr{
		final List<Expr> exprs;
		Grouping(List<Expr> exprs){
			this.exprs = exprs;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGrouping(this);
		}
	}

	static class Literal extends Expr{
		final Object value;
		Literal(Object value){
			this.value = value;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteral(this);
		}
	}

	static class Unary extends Expr{
		final Token operator;
		final Expr expr;
		Unary(Token operator,Expr expr){
		    this.operator = operator;
			this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnary(this);
		}
	}
	static class Variable extends Expr{
		final Token name;
		Variable(Token name){
			this.name = name;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariable(this);
		}
	}
	static class Assign extends Expr {
	    final Token name;
		final Expr value;
		final Expr index;
		final Boolean b_refer;
		Assign(Token name,Expr expr){
			this.name = name;
			this.value = expr;
			this.index = null;
			this.b_refer = false;
		}
		Assign(Token name,Expr expr,Expr index){
			this.name = name;
			this.value = expr;
			this.index = index;
			this.b_refer = false;
		}
		Assign(Token name,Expr expr,Expr index,Boolean b_refer){
			this.name = name;
			this.value = expr;
			this.index = index;
			this.b_refer = b_refer;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssign(this);
		}
	}
	static class Logical extends Expr{
		final Expr left,right;
		final Token operator;
		Logical(Expr left,Token operator,Expr right){
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLogical(this);
		}
	}
	static class Statement extends Expr{
		final Stmt stmt;
		Statement(Stmt stmt){
			this.stmt = stmt;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitStatement(this);
		}
	}
	static class ArrayExpr extends Expr{
	    final Expr[] array;
		final Map<Expr,Integer> index_map;
		ArrayExpr(Expr[] array,Map<Expr,Integer> index_map){
			this.array = array;
			this.index_map = index_map;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitArray(this);
		}
	}
	// static class ListItem extends Expr{
	//     final Expr expr;
	// 	final Expr index;
	// 	ListItem(Expr expr,Expr index){
	// 		this.expr = expr;
	// 		this.index = index;
	// 	}
	// 	@Override
	// 	<R> R accept(Visitor<R> visitor){
	// 		return visitor.visitListItem(this);
	// 	}
	// }
	static class Select extends Expr{
		final Token ques;
		final Expr condition;
		final Expr list;
		public Select(Token ques,Expr condition,Expr list){
			this.ques = ques;
			this.condition = condition;
			this.list = list;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitSelect(this);
		}
	}
	static class CallableExpr extends Expr{
		final Token sharp;
		final Expr callee;
		final Expr argu;
		public CallableExpr(Token sharp,Expr callee,Expr argu){
			this.sharp = sharp;
			this.callee = callee;
			this.argu = argu;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCallableExpr(this);
		}
	}
	public static class FunctionDecl extends Expr{
		public final List<Token> parameters;
		public final List<Token> closure;
		public final Expr body;
		public FunctionDecl(List<Token> para,Expr body){
			this.parameters = para;
			this.body = body;
			this.closure = null;
		}
		public FunctionDecl(List<Token> para,Expr body,List<Token> clos){
			this.parameters = para;
			this.body = body;
			this.closure = clos;
		}
	    @Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFuncDecl(this);
		}
	}
	public static class ReferDecl extends Expr{
		final Token name;
		final Token star;
		public ReferDecl(Token name,Token star){
			this.name = name;
			this.star = star;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReferDecl(this);
		}
	}
	public static class Reference extends Expr{
	    final Token name;
		final Token percn;
		public Reference(Token name,Token percn){
		    this.name = name;
			this.percn = percn;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReference(this);
		}
	}
	
	public interface Visitor<R> {
		R visitBinary(Expr.Binary binary);
		R visitGrouping(Expr.Grouping grouping);
		R visitLiteral(Expr.Literal literal);
		R visitUnary(Expr.Unary unary);
		R visitVariable(Expr.Variable variable);
		R visitAssign(Expr.Assign assign);
		R visitLogical(Expr.Logical logical);
		R visitStatement(Expr.Statement statement);
		R visitArray(Expr.ArrayExpr arrayExpr);
		// R visitListItem(Expr.ListItem listItem);
		R visitSelect(Expr.Select select);
		R visitCallableExpr(Expr.CallableExpr callableExpr);
		R visitFuncDecl(Expr.FunctionDecl functionDecl);
		R visitReferDecl(Expr.ReferDecl referDecl);
		R visitReference(Expr.Reference reference);
	}
}

