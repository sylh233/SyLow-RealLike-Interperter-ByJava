package cbyj.parser;

import java.util.List;
import cbyj.lexer.*;

abstract public class Stmt {
	abstract <R> R accept(Visitor<R> visitor);
	static class Expression extends Stmt{
		final Expr expr;
		public Expression(Expr expr){
			this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitExprStmt(this);
		}
	}
	static class Write extends Stmt{
		public static enum Mode{
			NOUN,
			LINE,
			FILE
		}
		final Mode mode;
		final Expr expr;
		public Write(Expr expr,Mode mode){
			this.mode = mode;
			this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitWriteStmt(this);
		}
	}
	static class Declare extends Stmt{
		final Token name;
		final Expr initializer;
		final Expr length;
		public Declare(Token name,Expr initializer,Expr length){
			this.name = name;
			this.initializer = initializer;
			this.length = length;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitDeclareStmt(this);
		}
	}
	static class LiteralStmt extends Stmt {
		final Expr literal;
		final Token grave;
		public LiteralStmt(Token grave,Expr literal){
			this.grave = grave;
			this.literal = literal;
		}
		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitLiteralStmt(this);
		}
	}
	static class Block extends Stmt{
		final List<Stmt> stmts;
		public Block(List<Stmt> stmts){
			this.stmts = stmts;
		}
	    @Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlock(this);
		}
	}
	static class LoopStmt extends Stmt{
		final Block block;
		public LoopStmt(Block block){
			this.block = block;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLoopStmt(this);
		}
	}
	static class BreakStmt extends Stmt{
		final Token token;
		public BreakStmt(Token token){
			this.token = token;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStmt(this);
		}
	}
	static class ReturnStmt extends Stmt{
		final Expr expr;
		public ReturnStmt(Expr expr){
			this.expr = expr;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}
	}
	// static class ContinueStmt extends Stmt{
	// 	final Token token;
	// 	public ContinueStmt(Token token){
	// 		this.token = token;
	// 	}
	// 	@Override
	// 	<R> R accept(Visitor<R> visitor) {
	// 		return null;
	// 	}
	// }
	static class ReadStmt extends Stmt{
	    @Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReadStmt(this);
		}
	}
	
	public interface Visitor<R> {
		R visitWriteStmt(Stmt.Write write);
		R visitExprStmt(Stmt.Expression expression);
		R visitDeclareStmt(Stmt.Declare declare);
		R visitLiteralStmt(Stmt.LiteralStmt literal);
		R visitBlock(Stmt.Block block);
		R visitLoopStmt(Stmt.LoopStmt loopStmt);
		R visitBreakStmt(Stmt.BreakStmt breakStmt);
		// R visitContinueStmt(Stmt.ContinueStmt continueStmt);
		R visitReturnStmt(Stmt.ReturnStmt returnStmt);
		R visitReadStmt(Stmt.ReadStmt readStmt);
	}
}
