package cbyj.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cbyj.lexer.TokenType.*;
import cbyj.cbyj;

public class Scanner{
	final private String source;
	final private List<Token> tokens = new ArrayList<>();

	private int start = 0,current = 0,cLine = 1;

	public Scanner(String source){
		this.source = source;
	}

	public List<Token> scanTokens(){
		while (!isEnd()) {
			start = current;
			scanToken();
		}
		tokens.add(new Token(EOF,"",null,cLine));
		return tokens;
	}

	private boolean isEnd(){
		return current >= source.length();
	}

	private void scanToken(){
		char c = advance();
		switch (c) {
		case '{':
			addToken(LEFT_BRACE);
			break;
		case '}':
			addToken(RIGHT_BRACE);
			break;
		case '(':
			addToken(LEFT_PAREN);
			break;
		case ')':
			addToken(RIGHT_PAREN);
			break;
		case '[':
			addToken(EXPR_BEGIN);
			break;
		case ']':
			addToken(EXPR_END);
			break;
		case ':':
			addToken(match('=')?CEQUAL:COLON);
			break;
		case ';':
			addToken(SEMI);
			break;
		case '+':
			addToken(PLUS);
			break;
		case '-':
			addToken(MINUS);
			break;
		case '=':
			addToken(match('>')?ENTRY:
					 (match('=')?DEQUAL:EQUAL));
			break;
		case '!':
			addToken(match('=')?NEQUAL:BANG);
			break;
		case '<':
			addToken(match('=')?RETURN:LESS);
			break;
		case '>':
			addToken(match('=')?GEQUAL:GREATER);
			break;
		case '#':
			addToken(SHARP);
			break;
		case '@':
			addToken(AT);
			break;
		case '/':
			if (match('/')) {
				while (peek()!='\n'&&!isEnd()) {
					advance();
				}
			}else if(match('*')){
				while (!isEnd()) {
					if(match('*')){
						if(match('/')) break;
					}else{
						advance();
					}
				}
			}else{
				addToken(SLASH);
			}
			break;
		case '?':
			addToken(QUES);
			break;
		case ',':
			addToken(COMMA);
			break;
		case '.':
			addToken(DOT);
			break;
		case '\'':
			// addToken(QUOTE);
			a_char();
			break;
		case '*':
			addToken(STAR);
			break;
		case '~':
			addToken(TILDE);
			break;
		case '^':
			addToken(HAT);
			break;
		case '&':
			addToken(AND);
			break;
		case '|':
			addToken(OR);
			break;
		case '$':
			addToken(DOLAR);
			break;
		case '%':
			addToken(PERCN);
			break;
		case ' ':
		case '\r':
		case '\t':
			break;
		case '\n':
			cLine++;
			break;
		case '"':
			string();
			break;
		case '`':
			// stmt();
			addToken(GRAVE);
			break;
		default:
			if (isDigit(c)) {
				number();
			}else if(isLetter(c)){
				identifier();
			}else{
				cbyj.throwError(cLine, "Unexpected char: "+c);
			}
			break;
		}
	}

	private char advance(){
		current++;
		return source.charAt(current - 1);
	}

	private void addToken(TokenType type){
		addToken(type, null);
	}

	private void addToken(TokenType type,Object literal){
		String text = source.substring(start, current);
		tokens.add(new Token(type,text,literal,cLine));
	}

	private boolean match(char expected){
		if (isEnd()) {
			return false;
		}
		if (source.charAt(current) != expected) {
		    return false;
		}
		current++;
		return true;
	}

	private char peek(){
		if(isEnd()) return '\0';
		return source.charAt(current);
	}

	private void string(){
		String literal = "";
		while(peek() != '"' && !isEnd()){
			if (peek() == '\n') {
			    cLine++;
			}
			literal += format_char();
		}

		if (isEnd()) {
			cbyj.throwError(cLine, "Unterminated string!");
			return;
		}

		advance();
		// String literal = source.substring(start + 1,current - 1);
		addToken(STRING, literal);
	}

	private boolean isDigit(char c){
		return c >= '0' && c <= '9';
	}

	private void number(){
		boolean isFloat = false;
		while (isDigit(peek())) {
			advance();
		}
		if (peek() == '.' && isDigit(peekNext())) {
			isFloat = true;
		    advance();
			while (isDigit(peek())) {
				advance();
			}
		}
		if (isFloat) {
			addToken(F64, Double.parseDouble(source.substring(start,current)));
		}else{
			addToken(I32, Integer.parseInt(source.substring(start,current)));
		}
	    
	}

	private boolean isLetter(char c){
		return (c >= 'a' && c <= 'z') ||
			(c >= 'A' && c <= 'Z') ||
			c == '_';
	}

	private void identifier(){
		while (isLetterNum(peek())) {
		    advance();
		}

		String text = source.substring(start, current);
		TokenType type = keywords.get(text);
		if (type == null) {
		    type = IDENTIFIER;
		}
		
		addToken(type);
	}

	private boolean isLetterNum(char c){
		return isDigit(c) || isLetter(c);
	}

	private char peekNext(){
		if(current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}

	private static final Map<String,TokenType> keywords;

	static{
		keywords = new HashMap<>();
		keywords.put("Super", SUPER);
		keywords.put("Origin", ORG);
		
		keywords.put("true", TRUE);
		keywords.put("false", FALSE);
		keywords.put("True", TRUE);
		keywords.put("False", FALSE);
		keywords.put("null", NULL);
		keywords.put("NULL", NULL);
		
		keywords.put("Write", WRITE);
		keywords.put("Read", READ);
		keywords.put("Load", LOAD);
		keywords.put("Exit", EXIT);

		keywords.put("while", S_WHILE);
		keywords.put("for", S_FOR);
		keywords.put("break", S_BREAK);
		keywords.put("return", S_RET);
	}

	// private void stmt(){
	// 	while(peek() != '`' && !isEnd()){
	// 		if (peek() == '\n') {
	// 		    cLine++;
	// 		}
	// 		advance();
	// 	}

	// 	if (isEnd()) {
	// 		cbyj.throwError(cLine, "Unterminated literal statement!");
	// 		return;
	// 	}

	// 	advance();
	// 	String literal = source.substring(start + 1,current - 1);
	// 	addToken(STMT, literal);
	// }
	private void a_char(){
		addToken(C16, (char)format_char());
		if(peek() == '\''){
			advance();
		}else{
			cbyj.throwError(cLine, "Expect a \"'\" after Char");
			return;
		}
	}

	private char format_char(){
		if(peek() == '\\'){
				advance();
				char c;
				switch (peek()) {
				case 'n':
					c = '\n';
					break;
				case 't':
					c = '\t';
					break;
				case '"':
					c = '"';
					break;
				case '\\':
					c = '\\';
					break;
				case '0':
					c = '\0';
					break;
				default:
					c = peek();
					break;
				}
				advance();
				return c;
			}else{
				return advance();
			}
	}
}

