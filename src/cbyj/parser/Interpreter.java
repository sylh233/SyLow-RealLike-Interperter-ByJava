package cbyj.parser;

import static cbyj.lexer.TokenType.*;
import cbyj.cbyj;
import cbyj.lexer.*;
import java.util.List;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static cbyj.parser.Stmt.Write;

public class Interpreter implements Expr.Visitor<Object>,
									Stmt.Visitor<Object>{
	private Environment environment = new Environment();
	
	// public void interpert(List<Stmt> statements){
	// 	try {
	// 		for (Stmt stmt : statements) {
	// 			exec(stmt);
	// 		}
	// 	    // Object value = eval(expr);
	// 		// System.out.println(stringify(value));
	// 	} catch (RuntimeError error) {
	// 		cbyj.runtimeError(error);
	// 	}
	// }
	public Object interpert(Expr result){
		try {
			return eval(result);
		} catch (RuntimeError error) {
			cbyj.runtimeError(error);
			return null;
		}
	}

	public Object interpert_noStruct(List<Expr> exprs){
		Object result = null;
		try {
			for (Expr expr : exprs) {
			    result = eval(expr);
			}
			return result;
		} catch (RuntimeError error) {
			cbyj.runtimeError(error);
			return null;
		}
	}
	
	static public String stringify(Object object){
		if (object == null) return "NULL";
		if (object instanceof Double || object instanceof Integer){
			return object.toString();
		}
		return object.toString();
	}
	private Object eval(Expr expr){
		if(expr != null) return expr.accept(this);
		return null;
	}
	private Object exec(Stmt stmt){
		if(stmt != null)
			return stmt.accept(this);
		else
			return null;
	}
	@Override
	public Object visitBinary(Expr.Binary binary){
		Object left = binary.left.accept(this);
		Object right = binary.right.accept(this);

		switch (binary.operator.type) {
		// case COMMA:
		// 	return right;
		case PLUS:
			if (left instanceof Integer && right instanceof Integer) {
				return (Integer)left + (Integer)right;
			} else if (left instanceof Double || right instanceof Double){
				return ((Number)left).doubleValue() +
					((Number)right).doubleValue();
			}else if (left instanceof String && right instanceof String){
				return (String)left + (String)right;
			}
			return left.toString() + right.toString();
		case MINUS:
			isValidOperand(binary.operator, left, right);
			if (left instanceof Integer && right instanceof Integer) {
				return (Integer)left - (Integer)right;
			} else if (left instanceof Double || right instanceof Double){
				return ((Number)left).doubleValue() -
					((Number)right).doubleValue();
			}
			break;
		case STAR:
			isValidOperand(binary.operator, left, right);
			if (left instanceof Integer && right instanceof Integer) {
				return (Integer)left * (Integer)right;
			} else if (left instanceof Double || right instanceof Double){
				return ((Number)left).doubleValue() *
					((Number)right).doubleValue();
			}
			break;
		case SLASH:
			isValidOperand(binary.operator, left, right);
			if (left instanceof Integer && right instanceof Integer) {
				if((Integer)right == 0 && (Integer)left == 0) return Double.NaN;
				if((Integer)right == 0) return Double.POSITIVE_INFINITY;
				return (Integer)left / (Integer)right;
			} else if (left instanceof Double || right instanceof Double){
				return ((Number)left).doubleValue() /
					((Number)right).doubleValue();
			}
			break;
		case GREATER:
			isValidOperand(binary.operator, left, right);
			return ((Number)left).doubleValue() > ((Number)right).doubleValue();
		case LESS:
			isValidOperand(binary.operator, left, right);
		    return ((Number)left).doubleValue() < ((Number)right).doubleValue();
		case GEQUAL:
			isValidOperand(binary.operator, left, right);
			return ((Number)left).doubleValue() >= ((Number)right).doubleValue();
		case RETURN:
			isValidOperand(binary.operator, left, right);
		    return ((Number)left).doubleValue() <= ((Number)right).doubleValue();
		case DEQUAL:
			return isEqual(left, right);
		case NEQUAL:
			return !isEqual(left, right);
		default:
			break;
		}
		return null;
	}
	@Override
	public Object visitGrouping(Expr.Grouping grouping){
		Object value = null;
		Environment previous = this.environment;
		try {
			this.environment = new Environment(previous);
			for (Expr expr : grouping.exprs) {
				value = eval(expr);
			}
		} catch (ReturnException e) {
		    value = e.value;
		} finally {
			this.environment = previous;
		}
		return value;
	}
	@Override
	public Object visitLiteral(Expr.Literal literal){
		return literal.value;
	}
	@Override
	public Object visitUnary(Expr.Unary unary){
		Object value = unary.expr.accept(this);
		switch(unary.operator.type) {
		case MINUS:
			isValidOperand(unary.operator, value);
			if (value instanceof Integer) {
				return -(Integer)value;
			}else if (value instanceof Double){
				return -(Double)value;
			}
			break;
		case BANG:
			return !isTruthy(value);
		default:
			break;
		}
		return null;
	}
	private boolean isTruthy(Object object){
		if (object == null) return false;
		if (object instanceof Boolean){
			return (boolean)object;
		}
		if (object instanceof Integer){
			if ((Integer)object == 0){
				return false;
			}
		}else if (object instanceof Double){
			if ((Double)object == 0.0){
				return false;
			}
		}else if (object instanceof String){
			if (((String)object).length() == 0){
				return false;
			}
		}
		return true;
	}
	private boolean isEqual(Object a,Object b){
		if (a == null && b == null) return true;
		if (a == null) return false;
		return a.equals(b);
	}
	private void isValidOperand(Token operator,Object operand){
		if (operand instanceof Double || operand instanceof Integer) return;
		throw new RuntimeError(operator, "Operand must be a Number!");
	}
	private void isValidOperand(Token operator,Object left,Object right){
		if ((left instanceof Double || left instanceof Integer)&&
			(right instanceof Double || right instanceof Integer)) return;
		throw new RuntimeError(operator, "Operand must be a Number!");
	}
	@Override
	public Object visitVariable(Expr.Variable variable){
		// return variable.name.literal;
		return environment.get(variable.name);
	}
	@Override
	public Object visitAssign(Expr.Assign assign){
		// return variable.name.literal;
		Object value = eval(assign.value);
		if(assign.index == null){
			environment.assign(assign.name, value);
		}else{
			int index = (Integer)eval(assign.index);
			environment.assign(assign.name, index, value);
		}
		return value;
	}
	@Override
	public Object visitLogical(Expr.Logical logical) {
		Object left = eval(logical.left);
		if(logical.operator.type == OR){
			if(isTruthy(left)) return isTruthy(left);
		}else{
		    if(!isTruthy(left)) return isTruthy(left);
		}
		return isTruthy(eval(logical.right));
	}
	@Override
	public Object visitStatement(Expr.Statement statement) {
		return exec(statement.stmt);
	}
	@Override
	public Object visitAList(Expr.aList aList){
		return Arrays.stream(aList.list).map(x -> eval(x)).toArray();
	}
	// @Override
	// public Object visitListItem(Expr.ListItem listItem){
	// 	int index = (Integer)eval(listItem.index);
	// 	if(listItem.expr instanceof Expr.Variable){
	// 		Expr.Variable variable = (Expr.Variable)listItem.expr;
	// 		Object[] list = (Object[])environment.get(variable.name);
	// 		return list[index];
	// 	}else if(listItem.expr instanceof Expr.Assign){
	// 		Expr.Assign assign = (Expr.Assign)listItem.expr;
	// 		Object value = eval(assign.value);
	// 		environment.assign(assign.name,
	// 						   index,
	// 						   value);
	// 		return value;
	// 	}else if(listItem.expr instanceof Expr.Literal){
	// 		Object value = eval(listItem.expr);
	// 		String s = stringify(value);
	// 		return s.charAt(index);
	// 	}
	// 	return null;
	// }
	@Override
	public Object visitSelect(Expr.Select select) {
		Object condition = eval(select.condition);
		int index = 0;
		if(condition instanceof Boolean){
			Boolean b = (Boolean)condition;
			if(b){
				index = 0;
			}else{
				index = 1;
			}
		}else if(condition instanceof Integer){
			index = (Integer)condition;
			if(index < 0) throw new RuntimeError(select.QorS,
												 "Index must be large than 0!");
		}else{
			throw new RuntimeError(select.QorS,
			"Expect a Boolean or a Integer as a index after Quote or Sharp!");
		}
		
	    if(select.list instanceof Expr.aList){
			Expr.aList list = (Expr.aList)select.list;
			if(index < list.list.length){
				Object result = eval(list.list[index]);
				return result;
			}else{
				throw new RuntimeError(select.QorS,"Array out of bounds!");
			}
		}else if(select.list instanceof Expr.Literal){
			String str = stringify(eval(select.list));
			if(index < str.length()){
				return str.charAt(index);
			}else{
				throw new RuntimeError(select.QorS,"Array(String) out of bounds!");
			}
		}else if(select.list instanceof Expr.Variable){
			Expr.Variable var = (Expr.Variable)select.list;
			Object object = environment.get(var.name);
			if(object instanceof Object[]){
				 Object[] list = (Object[])object;
				 if(index < list.length){
					 return list[index];
				 }else{
					 throw new RuntimeError(select.QorS,"Array out of bounds!");
				 }
			}else
				return object;
		}
		// if(condition instanceof Boolean){
		// 	if((Boolean)condition){
		// 		exec(selectStmt.ifstmt);
		// 	}else if(selectStmt.elsestmt.size() > 0){
		// 		exec(selectStmt.elsestmt.get(0));
		// 	}
		// }else if(condition instanceof Integer){
		// 	Integer index = (Integer)condition;
		// 	if(index < 0) {
		// 		return null;
		// 	}else if(index == 0) {
		// 		exec(selectStmt.ifstmt);
		// 	}else if(index > 0 &&
		// 			 selectStmt.elsestmt.size() >= index){
		// 		exec(selectStmt.elsestmt.get(index - 1));
		// 	}
		// }
		return null;
	}
	
	// Stmt
	@Override
	public String visitWriteStmt(Stmt.Write write){
		Object value = eval(write.expr);
		switch (write.mode) {
		case Write.Mode.LINE:
			System.out.println(stringify(value));
			break;
		case Write.Mode.FILE:
			break;
		case Write.Mode.NOUN:
		default:
			System.out.print(stringify(value));
			break;
		}
	    return stringify(value);
	}
	@Override
	public Void visitExprStmt(Stmt.Expression expression){
		eval(expression.expr);
	    return null;
	}
	@Override
	public String visitDeclareStmt(Stmt.Declare declare){
		// System.out.println(declare.name.literal);
		Object value = null;
		if(declare.length == null){
			value = eval(declare.initializer);
			environment.define(declare.name.lexeme, value);
			return "$ " + declare.name.lexeme + " = " + stringify(value);
		}else{
			int length = (Integer)eval(declare.length);
			Object[] list = new Object[length];
			
			Object init = eval(declare.initializer);
			if(init instanceof Object[]){
				Object[] src = (Object[])init;
				if(src.length <= length){
					System.arraycopy(src, 0,
									 list, 0, src.length);
				}else{
					// throw new RuntimeError("List value's length must be less than list variable's length!");
					System.arraycopy(src, 0,
									 list, 0, length);
				}
			}else{
				list[0] = init;
			}
			environment.define(declare.name.lexeme, (Object)list);
		}
		return null;
	}
	@Override
	public Object visitLiteralStmt(Stmt.LiteralStmt literal){
		String stmts = "";
		Object value = eval(literal.literal);
		if(value instanceof String){
			stmts = stringify(value);
		}else{
		    throw new RuntimeError(literal.grave,"Except 'Statement String' after Grave!");
		}
		Scanner scanner = new Scanner(stmts);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);

		Environment previous = this.environment;
		Object result = null;
		try {
			this.environment = new Environment(previous);
			result = interpert_noStruct(parser.parse_noStruct());
		} finally {
			this.environment = previous;
		}
	    return result;
	}
	@Override
	public Void visitBlock(Stmt.Block block){
		execBlock(block, new Environment(environment));
		return null;
	}
	private void execBlock(Stmt.Block block,Environment environment){
		Environment previous = this.environment;
		try {
		    this.environment = environment;
			for (Stmt stmt : block.stmts) {
				exec(stmt);
			}
		} finally {
			this.environment = previous;
		}
	    return;
	}
	@Override
	public Void visitLoopStmt(Stmt.LoopStmt loopStmt) {
		try{
			while (true) {
			    exec(loopStmt.block);
			}
		}catch(BreakException be){}
		
		return null;
	}
	@Override
	public Void visitBreakStmt(Stmt.BreakStmt breakStmt) {
		throw new BreakException();
	}
	@Override
	public Object visitReturnStmt(Stmt.ReturnStmt returnStmt){
		Object value = eval(returnStmt.expr);
		throw new ReturnException(value);
	}
	@Override
	public Object visitReadStmt(Stmt.ReadStmt readStmt){
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		String inString = "";
		try {
			inString = reader.readLine();
		} catch (IOException e) {}
		
		return inString;
	}
}

class BreakException extends RuntimeException{}
class ReturnException extends RuntimeException{
	final Object value;
	public ReturnException(Object object){
		this.value = object;
	}
}
