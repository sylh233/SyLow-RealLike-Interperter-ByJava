package cbyj.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static cbyj.lexer.TokenType.*;
import cbyj.cbyj;
import cbyj.lexer.*;
import static cbyj.parser.Stmt.Write;

public class Parser {
	private static class ParseError extends RuntimeException{}
	
	private final List<Token> tokens;
	private int current = 0;

	public Parser(List<Token> tokens){
		this.tokens = tokens;
	}

	public Expr parse(){
		while(!match(ENTRY) && !isEnd()){
			advance();
		}
		try{
			return expr();
		}catch(ParseError e){
			return null;
		}
		// while(!isEnd()){
		// 	statements.add(statement());
		// }
		// return statements;
		// try {
		//     return expr();
		// } catch (ParseError error) {
		//     return null;
		// }
	}

	public List<Expr> parse_noStruct(){
		List<Expr> exprs = new ArrayList<>();
		try {
			do {
				exprs.add(expr());
			} while (!isEnd() && match(COMMA,SEMI));
			return exprs;
		} catch (ParseError error) {
		    return null;
		}
	}

	private Expr expr_statement(){
		try{
			Stmt stmt = match_stmt();

			if(stmt != null){
				return new Expr.Statement(stmt);
			}else{
				return assignment();
			}
		}catch(ParseError error){
			synchronize();
			return null;
		}
	}
	
	private Stmt statement(){
		try{
			Stmt stmt = match_stmt();
			if(stmt != null) return stmt;
		    return exprStmt();
		}catch(ParseError error){
			synchronize();
			return null;
		}
	}

	private Stmt match_stmt(){
		if(match(WRITE)) return writeStmt();
		if(match(DOLAR)) return declStmt();
		if(match(GRAVE)) return literalStmt();
		if(match(LEFT_BRACE)) return new Stmt.Block(block());
		if(match(AT)) return loopStmt();
		if(match(LESS)) return breakStmt();
		if(match(RETURN)) return returnStmt();
		if(match(READ)) return readStmt();
		// sugar
		if(match(S_BREAK)) return breakSuger();
		if(match(S_FOR)) return forSuger();
		if(match(S_WHILE)) return whileSuger();
		if(match(S_RET)) return returnStmt();
		
		return null;
	}

	private Stmt writeStmt(){
		Write.Mode mode = Write.Mode.NOUN;
		while(match(COLON)){
			Token token = advance();
			switch (token.lexeme) {
			case "line":
				mode = Write.Mode.LINE;
				break;
			case "file":
				mode = Write.Mode.FILE;
				break;
			case "noun":
			default:
				mode = Write.Mode.NOUN;
				break;
			}
		}
		Expr value = expr();
		// consume(SEMI, "Expect ';' after value!");
		return new Stmt.Write(value,mode);
	}

	private Stmt exprStmt(){
		Expr expr = expr();
		// consume(SEMI, "Expect ';' after expression!");
		// if(!match(SEMI)) {
		// 	return new Stmt.Write(expr);
		// }
		return new Stmt.Expression(expr);
	}

	private Stmt declStmt(){
		Token name = consume(IDENTIFIER, "Expect a variable name!");
		Expr initializer = null;
		Expr length = null;
		if(match(QUES)){
			length = or();		// 防止识别后面的等号
		}
		if(match(EQUAL)){
			initializer = expr();
		}
		// consume(SEMI, "Expect ';' after variable declaration!");
		return new Stmt.Declare(name, initializer, length);
	}

	private Stmt literalStmt(){
		Token grave = previous();
	    Expr literal = expr();
	    // consume(SEMI, "Expect ';' after literal statements!");
		return new Stmt.LiteralStmt(grave,literal);
	}

	private List<Stmt> block(){
		List<Stmt> stmts = new ArrayList<>();
		Stmt first;
		if(match(RIGHT_BRACE)){
			first = null;
		}else{
			first = statement();
		}
		stmts.add(first);
		while (match(SEMI)) {
			if(match(RIGHT_BRACE))
				return stmts;
			else
				stmts.add(statement());
		}
		
		consume(SEMI, "Expect ';' after statement in block!");
		consume(RIGHT_BRACE, "Expect '}' after block!");
		return stmts;
	}

	private Expr select(){
		// ? [condition] (list);
		Token qors = previous();
		Expr condition = or();
		Expr list;
		if(match(STRING)){
			list = new Expr.Literal(previous().literal);
		}else if(match(IDENTIFIER)){
			list = new Expr.Variable(previous());
		}else if(match(LEFT_PAREN)){
			list = alist();
		}else{
			list = expr();
		}
		// consume(LEFT_PAREN, "Expect '(' after Condition!");
		
		// if(check(COMMA)){
		// 	ifstmt = null;
		// }else{
		// 	ifstmt = statement();
		// }
		
		// List<Stmt> elsestmt = new ArrayList<>();
		// while(match(COMMA)){
		// 	elsestmt.add(statement());
		// }
		// consume(RIGHT_PAREN, "Expect ')' after Statement Branch!");
		// consume(SEMI, "Expect ';' after Condition statements!");
		
		return new Expr.Select(qors, condition, list);
	}

	private Stmt loopStmt(){
		// @{}
		consume(LEFT_BRACE, "Expect a Block('{','}') after Loop('@')!");
		return new Stmt.LoopStmt(new Stmt.Block(block()));
	}

	private Stmt breakStmt(){
		Token token = previous();
		// consume(SEMI, "Expect ';' after Break('<')!");
		return new Stmt.BreakStmt(token);
	}

	private Stmt returnStmt(){
		return new Stmt.ReturnStmt(expr());
	}

	private Stmt readStmt(){
		return new Stmt.ReadStmt();
	}

	private Expr expr(){
		// return equality();
	    
		// Expr expr = assignment();
		Expr expr = expr_statement();
		
		// while (match(COMMA)) {
		//     Token ope = previous();
		// 	Expr right = equality();
		// 	expr = new Expr.Binary(expr, ope, right);
		// 	// expr = equality();
		// }

		return expr;
	}

	private Expr assignment(){
		Expr expr = or();
		if (match(EQUAL)) {
			Token equal = previous();
			Expr value = expr();
			if(expr instanceof Expr.Variable){
				return new Expr.Assign(((Expr.Variable)expr).name, value);
			}else if(expr instanceof Expr.Select){
				Expr.Select select = (Expr.Select)expr;
				if(select.list instanceof Expr.Variable){
					Expr.Variable var = (Expr.Variable)select.list;
					return new Expr.Assign(var.name, value, select.condition);
				}// else if(select.list instanceof Expr.aList){
				// }
			}
			
			// if(expr instanceof Expr.ListItem){
			// 	return new Expr.Assign(((Expr.ListItem)expr).name,
			// 						   ((Expr.ListItem)expr).index, value);
			// }

			error(equal, "Invalid Assignment Target!");
		}

		return expr;
	}

	private Expr or(){
		Expr left = and();

		while(match(OR)){
			Token operator = previous();
			Expr right = and();
			left = new Expr.Logical(left, operator, right);
		}

		return left;
	}

	private Expr and(){
		Expr left = equality();

		while(match(AND)){
			Token operator = previous();
			Expr right = equality();
			left = new Expr.Logical(left, operator, right);
		}

		return left;
	}

	private Expr equality(){
		Expr expr = comparison();
		
		while (match(DEQUAL,NEQUAL)) {
		    Token ope = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, ope, right);
		}

		return expr;
	}

	private boolean match(TokenType... types){
		for (TokenType type : types) {
			if(check(type)){
				advance();
				return true;
			}
		}
		return false;
	}

	private boolean check(TokenType type){
		if(isEnd()) return false;
		return type == peek().type;
	}

	private Token advance(){
		if(!isEnd()) current++;
		return previous();
	}

	private boolean isEnd(){
		return peek().type == EOF;
	}

	private Token peek(){
		return tokens.get(current);
	}

	private Token previous(){
		return tokens.get(current - 1);
	}

	private Expr comparison(){
		Expr expr = term();
		while (match(GREATER,LESS,GEQUAL,RETURN)) {
		    Token ope = previous();
			Expr right = term();
			expr = new Expr.Binary(expr, ope, right);
		}

		return expr;
	}

	private Expr term(){
		Expr expr = factor();
		while (match(PLUS,MINUS)) {
		    Token ope = previous();
			Expr right = factor();
			expr = new Expr.Binary(expr, ope, right);
		}

		return expr;
	}

	private Expr factor(){
	    Expr expr = unary();
		while (match(STAR,SLASH)) {
		    Token ope = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, ope, right);
		}

		return expr;
	}

	private Expr unary(){
		if (match(BANG,MINUS)) {
			Token ope = previous();
			Expr expr = unary();
			return new Expr.Unary(ope, expr);
		}

		return primary();
	}

	private final List<TokenType> BinaryType = List.of(DEQUAL,NEQUAL,
													   GREATER,LESS,
													   GEQUAL,RETURN,
													   PLUS,MINUS,STAR,SLASH);

	private Expr primary(){
		if(match(FALSE)) return new Expr.Literal(false);
		if(match(TRUE)) return new Expr.Literal(true);
		if(match(I32,F64,STRING,C8)){
			return new Expr.Literal(previous().literal);
		}
		if(match(NULL)){
			return new Expr.Literal(null);
		}
		if(match(EXPR_BEGIN)){
		    List<Expr> exprs = new ArrayList<>();
			Expr first;
			if(match(EXPR_END)){
				first = null;
			}else{
				first = expr();
			}
			exprs.add(first);
			while(match(COMMA)){
				exprs.add(expr());
			}
			consume(EXPR_END,"");
			return new Expr.Grouping(exprs);
		}
		if(match(LEFT_PAREN)) return alist();
		if(match(IDENTIFIER)){
			return new Expr.Variable(previous());
		}
		if(match(QUES)) return select();
		if(match(SHARP)) return callable();

		for (TokenType tokenType : BinaryType) {
			if(match(tokenType)){
				throw error(previous(),
								"The Operator is for Binary!");
			}
		}
		
		throw error(peek(), "Unexpected expression!");
	}

	private Expr alist(){
		List<Expr> array = new ArrayList<>();
		while(match(COMMA)){
			array.add(expr());	// 现在支持长度为0以适配函数参数列表
		}
		consume(RIGHT_PAREN, "Expect ')' for the end of list!");
		return new Expr.aList(array.toArray(new Expr[array.size()]));
	}

	// private Expr listItem(){
	// 	Expr index = expr();
	// 	Expr expr = expr();
	// 	return new Expr.ListItem(expr, index);
	// }

	private Expr callable(){
		Token sharp = previous();
		Expr callee = expr();
		Expr argu = expr();

		return new Expr.CallableExpr(sharp, callee, argu);
	}

	private Token consume(TokenType type,String msg){
		if (check(type)) return advance();
		throw error(peek(),msg);
	}

	private ParseError error(Token token,String msg){
		cbyj.throwError(token,msg);
		return new ParseError();
	}

    private void synchronize(){
		advance();
		while(!isEnd()){
			if(previous().type == SEMI) break;
			if(previous().type == RIGHT_BRACE) break;
			if(previous().type == COMMA) break;
			if(previous().type == RIGHT_PAREN) break;
			if(previous().type == EXPR_END) break;
		}
		advance();
	}

	// Suger
	private Stmt breakSuger(){
		Token token = previous();
		// consume(SEMI, "Expect ';' after break!");
		return new Stmt.BreakStmt(token);
	}
	private Stmt forSuger(){
		// for($ i = 0;i < 10;i = i + 1){}
		consume(LEFT_PAREN, "Expect '(' after for!");
		Stmt decl;
		if(match(SEMI)){
			decl = null;
		}else{
			if(match(DOLAR)){
				decl = declStmt();
			}else{
				decl = exprStmt();
			}
			consume(SEMI, "Expect ';' after for-loop initializer!");
		}

		Expr condition = null;
		if(!check(SEMI)){
			condition = assignment();
		}
		consume(SEMI, "Expect ';' after for-loop condition!");

		Expr increment = null;
		if(!check(RIGHT_PAREN)){
			increment = assignment();
		}
		consume(RIGHT_PAREN, "Expect ')' after for-loop increment!");

		consume(LEFT_BRACE, "Expect a Block('{','}') after for-loop!");
		List<Stmt> stmts = new ArrayList<>();
		if(decl != null){
			stmts.add(decl);
		}
		
		if(condition == null){
			condition = new Expr.Literal(true);
		}
		List<Stmt> block = new ArrayList<>();
		Expr[] list = {null,new Expr.Statement(new Stmt.BreakStmt(null))};
		Expr alist = new Expr.aList(list);
		block.add(new Stmt.Expression(new Expr.Select(null, condition, alist)));
		block.addAll(block());
		if(increment != null){
			block.add(new Stmt.Expression(increment));
		}
		stmts.add(new Stmt.LoopStmt(new Stmt.Block(block)));
		return new Stmt.Block(stmts);
	}
	private Stmt whileSuger(){
		consume(LEFT_PAREN, "Expect '(' after while!");
		Expr condition = expr();
	    consume(RIGHT_PAREN, "Expect ')' after condition!");
		consume(LEFT_BRACE, "Expect a Block('{','}') after while-loop!");
		List<Stmt> block = new ArrayList<>();
		Expr[] list = {null,new Expr.Statement(new Stmt.BreakStmt(null))};
		Expr alist = new Expr.aList(list);
		// block.add(new Stmt.IfStmt(condition,null,
		// 						  Arrays.asList(new Stmt.BreakStmt(null))
		// 						  ));
		block.add(new Stmt.Expression(new Expr.Select(null, condition, alist)));
		block.addAll(block());
		return new Stmt.LoopStmt(new Stmt.Block(block));
	}
}
