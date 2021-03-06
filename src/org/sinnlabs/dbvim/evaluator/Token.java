package org.sinnlabs.dbvim.evaluator;

import org.sinnlabs.dbvim.ui.IField;

/** A token.
 * <br>When evaluating an expression, it is first split into tokens.
 * These tokens can be operators, constants, etc ...
 * @author Jean-Marc Astesana
 */
public class Token {
	private enum Kind {
		OPEN_BRACKET,
		CLOSE_BRACKET,
		FUNCTION_SEPARATOR,
		FUNCTION,
		OPERATOR,
		LITERAL,
		FIELD,
		JOIN_FIELD
	}
	static final Token FUNCTION_ARG_SEPARATOR = new Token(Kind.FUNCTION_SEPARATOR, null);
	
	private Kind kind;
	private Object content;
	
	static Token buildLiteral(String literal) {
		return new Token(Kind.LITERAL, literal);
	}

	static Token buildOperator(Operator ope) {
		return new Token(Kind.OPERATOR, ope);
	}

	static Token buildFunction(Function function) {
		return new Token(Kind.FUNCTION, function);
	}
	
	static Token buildOpenToken(BracketPair pair) {
		return new Token(Kind.OPEN_BRACKET, pair);
	}

	static Token buildCloseToken(BracketPair pair) {
		return new Token(Kind.CLOSE_BRACKET, pair);
	}
	
	static Token buildFieldToken(IField<?> field) {
		return new Token(Kind.FIELD, field);
	}
	
	static Token buildJoinFieldToken(IField<?> field) {
		return new Token(Kind.JOIN_FIELD, field);
	}

	private Token(Kind kind, Object content) {
		super();
		if ((kind.equals(Kind.OPERATOR) && !(content instanceof Operator)) ||
				(kind.equals(Kind.FUNCTION) && !(content instanceof Function)) ||
				(kind.equals(Kind.LITERAL) && !(content instanceof String))) {
			throw new IllegalArgumentException();
		}
		this.kind = kind;
		this.content = content;
	}
	
	BracketPair getBrackets() {
		return (BracketPair) this.content;
	}

	Operator getOperator() {
		return (Operator) this.content;
	}

	Function getFunction() {
		return (Function) this.content;
	}
	
	IField<?> getField() {
		return (IField<?>) this.content;
	}

	Kind getKind() {
		return kind;
	}

	/** Tests whether the token is an operator.
	 * @return true if the token is an operator
	 */
	public boolean isOperator() {
		return kind.equals(Kind.OPERATOR);
	}

	/** Tests whether the token is a function.
	 * @return true if the token is a function
	 */
	public boolean isFunction() {
		return kind.equals(Kind.FUNCTION);
	}

	/** Tests whether the token is an open bracket.
	 * @return true if the token is an open bracket
	 */
	public boolean isOpenBracket() {
		return kind.equals(Kind.OPEN_BRACKET);
	}

	/** Tests whether the token is a close bracket.
	 * @return true if the token is a close bracket
	 */
	public boolean isCloseBracket() {
		return kind.equals(Kind.CLOSE_BRACKET);
	}

	/** Tests whether the token is a function argument separator.
	 * @return true if the token is a function argument separator
	 */
	public boolean isFunctionArgumentSeparator() {
		return kind.equals(Kind.FUNCTION_SEPARATOR);
	}
	
	/** Tests whether the token is a literal or a constant or a variable name.
	 * @return true if the token is a literal, a constant or a variable name
	 */
	public boolean isLiteral() {
		return kind.equals(Kind.LITERAL);
	}
	
	/**
	 * Tests whether the token is a field
	 * @return true if token is a field
	 */
	public boolean isField() {
		return kind.equals(Kind.FIELD) || kind.equals(Kind.JOIN_FIELD);
	}
	
	/**
	 * Tests whether the token is a join field (field from right form)
	 * @return true if token is a join field
	 */
	public boolean isJoinField() {
		return kind.equals(Kind.JOIN_FIELD);
	}

	Operator.Associativity getAssociativity() {
		return getOperator().getAssociativity();
	}

	int getPrecedence() {
		return getOperator().getPrecedence();
	}

	String getLiteral() {
		if (!this.kind.equals(Kind.LITERAL)) {
			throw new IllegalArgumentException();
		}
		return (String)this.content;
	}
}