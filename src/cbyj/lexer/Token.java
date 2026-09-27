package cbyj.lexer;

public class Token{
	public final TokenType type;
	public final String lexeme;
	public final Object literal;
	public final int cLine;

	Token(TokenType t,String le,Object li,int c){
		this.type = t;
		this.lexeme = le;
		this.literal = li;
		this.cLine = c;
	}

	public String toString(){
		return type + " " + lexeme +  " " + literal;
	}
}
