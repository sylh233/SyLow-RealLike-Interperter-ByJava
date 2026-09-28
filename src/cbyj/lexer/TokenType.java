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

	// expression
	EXPR_BEGIN,EXPR_END,		// [,]
	PLUS,MINUS,					// +,-
	GREATER,LESS,				// >,<
	QUES,						// ?
	BANG,						// !
	DEQUAL,						// ==
	NEQUAL,						// !=
	AND,OR,						// &,|
	GEQUAL,						// >=
	
	// literal
	IDENTIFIER,
	STRING,
	// STMT,						// `...`
	F32,F64,
	I32,U32,
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

	EOF,
}
