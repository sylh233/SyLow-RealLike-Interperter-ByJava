package cbyj.lexer;

public enum TokenType{
	ENTRY,						// =>
	LEFT_BRACE,RIGHT_BRACE,		// {,}
	LEFT_PAREN,RIGHT_PAREN,		// (,)
	EQUAL,						// =
	RETURN,						// <=
	COLON,						// :
	SEMI,						// ;
	SLASH,						// /
	COMMA,						// ,
	SHARP,						// #
	AT,							// @
	CEQUAL,						// :=
	DOT,						// .
	QUOTE,						// '
	STAR,						// *
	TILDE,						// ~
	GRAVE,						// `
	HAT,						// ^
	DOLAR,						// $
	QUES,						// ?
	PERCN,						// %

	// expression
	EXPR_BEGIN,EXPR_END,		// [,]
	PLUS,MINUS,					// +,-
	GREATER,LESS,				// >,<
	BANG,						// !
	DEQUAL,						// ==
	NEQUAL,						// !=
	AND,OR,						// &,|
	GEQUAL,						// >=
	
	// literal
	IDENTIFIER,
	STRING,
	// STMT,						// `...`
	F64,// F32,
	I32,// U32,
	C8,

	// keywords
	SUPER,ORG,					// Super,Origin
	WRITE,						// Write
	READ,						// Read
	TRUE,FALSE,					// true,false
	NULL,						// NULL

	// Suger
	S_WHILE,S_FOR,					// while,for
	S_BREAK,						// break
	S_RET,							// return

	EOF,
}
