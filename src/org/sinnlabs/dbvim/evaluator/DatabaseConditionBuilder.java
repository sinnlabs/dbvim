/**
 * 
 */
package org.sinnlabs.dbvim.evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.ui.IField;

/**
 *  An evaluator that is able to evaluate boolean expressions on field values.
 * @author peter.liverovsky
 *
 */
public class DatabaseConditionBuilder {
	
	private final ConditionTokenizer tokenizer;
	private final Map<String, Function> functions;
	private final Map<String, List<Operator>> operators;
	private final Map<String, Constant> constants;
	private final String functionArgumentSeparator;
	private final Map<String, BracketPair> functionBrackets;
	private final Map<String, BracketPair> expressionBrackets;
	
	/** A constant that represents NULL value */
	public static final Constant NULL = new Constant("$NULL$");

	/** The negate unary operator in the standard operator precedence.*/
	public static final Operator NEGATE = new Operator("-", 1, Operator.Associativity.RIGHT, 5, "-");
	/** The negate unary operator in the Excel like operator precedence.*/
	public static final Operator NEGATE_HIGH = new Operator("-", 1, Operator.Associativity.RIGHT, 7, "-");
	/** The substraction operator.*/
	public static final Operator MINUS = new Operator("-", 2, Operator.Associativity.LEFT, 3, "-");
	/** The addition operator.*/
	public static final Operator PLUS = new Operator("+", 2, Operator.Associativity.LEFT, 3, "+");
	/** The multiplication operator.*/
	public static final Operator MULTIPLY = new Operator("*", 2, Operator.Associativity.LEFT, 4, "*");
	/** The division operator.*/
	public static final Operator DIVIDE = new Operator("/", 2, Operator.Associativity.LEFT, 4, "/");
	/** The <a href="http://en.wikipedia.org/wiki/Modulo_operation">modulo operator</a>.*/
	public static final Operator MODULO = new Operator("%", 2, Operator.Associativity.LEFT, 4, "%");
	/** The equality operator */
	public static final Operator EQ = new Operator("=", 2, Operator.Associativity.LEFT, 2, "=");
	/** The not equality operator */
	public static final Operator NOT_EQ = new Operator("!=", 2, Operator.Associativity.LEFT, 2, "<>");
	/** The grate then operator */
	public static final Operator GT = new Operator(">", 2, Operator.Associativity.LEFT, 2, ">");
	/** The grate or equal operator */
	public static final Operator GE = new Operator(">=", 2, Operator.Associativity.LEFT, 2, ">=");
	/** The less then operator */
	public static final Operator LT = new Operator("<", 2, Operator.Associativity.LEFT, 2, "<");
	/** The less or equal operator */
	public static final Operator LE = new Operator("<=", 2, Operator.Associativity.LEFT, 2, "<=");
	/** The LIKE operator */
	public static final Operator LIKE = new Operator("LIKE", 2, Operator.Associativity.LEFT, 2, "LIKE");
	/** The logical AND operator */
	public static final Operator AND = new Operator("AND", 2, Operator.Associativity.LEFT, 1, "AND");
	/** The logical OR operator */
	public static final Operator OR = new Operator("OR", 2, Operator.Associativity.LEFT, 1, "OR");
	/** The logical NOT operator */
	public static final Operator NOT = new Operator("NOT", 1, Operator.Associativity.RIGHT, 2, "NOT");
	
	/** The standard whole set of predefined operators */
	private static final Operator[] OPERATORS = new Operator[]{NEGATE, MINUS, PLUS, MULTIPLY,
		DIVIDE, MODULO, EQ, NOT_EQ, GT, GE, LT, LE, LIKE, AND, OR, NOT};
	
	/** The whole set of predefined constants */
	private static final Constant[] CONSTANTS = new Constant[]{NULL};

	
	private static Parameters DEFAULT_PARAMETERS;
	
	private static Parameters getParameters() {
		if (DEFAULT_PARAMETERS == null) {
			DEFAULT_PARAMETERS = getDefaultParameters();
		}
		return DEFAULT_PARAMETERS;
	}
	
	/** Gets a copy of DoubleEvaluator default parameters.
	 * <br>The returned parameters contains all the predefined operators, functions and constants.
	 * <br>Each call to this method create a new instance of Parameters. 
	 * @return a Paramaters instance
	 */
	private static Parameters getDefaultParameters() {
		Parameters result = new Parameters();
		result.addOperators(Arrays.asList(OPERATORS));
		//result.addFunctions(Arrays.asList(FUNCTIONS));
		result.addConstants(Arrays.asList(CONSTANTS));
		result.addFunctionBracket(BracketPair.PARENTHESES);
		result.addExpressionBracket(BracketPair.PARENTHESES);
		return result;
	}
	
	public DatabaseConditionBuilder() {
		Parameters parameters = getParameters();
		final ArrayList<String> tokenDelimitersBuilder = new ArrayList<String>();
		this.functions = new HashMap<String, Function>();
		this.operators = new HashMap<String, List<Operator>>();
		this.constants = new HashMap<String, Constant>();
		this.functionBrackets = new HashMap<String, BracketPair>();
		for (final BracketPair pair : parameters.getFunctionBrackets()) {
			functionBrackets.put(pair.getOpen(), pair);
			functionBrackets.put(pair.getClose(), pair);
			tokenDelimitersBuilder.add(pair.getOpen());
			tokenDelimitersBuilder.add(pair.getClose());
		}
		this.expressionBrackets = new HashMap<String, BracketPair>();
		for (final BracketPair pair : parameters.getExpressionBrackets()) {
			expressionBrackets.put(pair.getOpen(), pair);
			expressionBrackets.put(pair.getClose(), pair);
			tokenDelimitersBuilder.add(pair.getOpen());
			tokenDelimitersBuilder.add(pair.getClose());
		}
		if (operators!=null) {
			for (Operator ope : parameters.getOperators()) {
				tokenDelimitersBuilder.add(ope.getSymbol());
				List<Operator> known = this.operators.get(ope.getSymbol());
				if (known==null) {
					known = new ArrayList<Operator>();
					this.operators.put(ope.getSymbol(), known);
				}
				known.add(ope);
				if (known.size()>1) {
					validateHomonyms(known);
				}
			}
		}
		boolean needFunctionSeparator = false;
		if (parameters.getFunctions()!=null) {
			for (Function function : parameters.getFunctions()) {
				this.functions.put(parameters.getTranslation(function.getName()), function);
				if (function.getMaximumArgumentCount()>1) {
					needFunctionSeparator = true;
				}
			}			
		}
		if (parameters.getConstants()!=null) {
			for (Constant constant : parameters.getConstants()) {
				this.constants.put(parameters.getTranslation(constant.getName()), constant);
			}
		}
		functionArgumentSeparator = parameters.getFunctionArgumentSeparator();
		if (needFunctionSeparator) {
			tokenDelimitersBuilder.add(functionArgumentSeparator);
		}
		tokenizer = new ConditionTokenizer('.', tokenDelimitersBuilder);
	}
	
	/** Validates that homonym operators are valid.
	 * <br>Homonym operators are operators with the same name (like the unary - and the binary - operators)
	 * <br>This method is called when homonyms are passed to the constructor.
	 * <br>This default implementation only allows the case where there's two operators, one binary and one unary.
	 * Subclasses can override this method in order to accept others configurations. 
	 * @param operators The operators to validate.
	 * @throws IllegalArgumentException if the homonyms are not compatibles.
	 * @see #guessOperator(Token, List)
	 */
	protected void validateHomonyms(List<Operator> operators) {
		if (operators.size()>2) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Returns all fields that contains in the condition
	 * @param expression Condition expression
	 * @param resolver FormFieldResolver for the form
	 * @param isJoinClause indicates that condition is a join qualification
	 * @return List of IField
	 * @throws ParseException
	 */
	public List<IField<?>> getConditionFields(String expression, FormFieldResolver resolver,
			boolean isJoinClause) throws ParseException {
		
		List<IField<?>> fields = new ArrayList<IField<?>>();
		
		final Iterator<String> tokens = tokenize(expression);
		Token previous = null;
		while (tokens.hasNext()) {
			// read one token from the input stream
			String strToken = tokens.next();
			final Token token = toToken(previous, strToken, resolver, isJoinClause);
			
			if (token.isField())
				fields.add(token.getField());
		}
		return fields;
	}
	
	public String buildCondition(String expression, AbstractVariableSet<Value<?>> environment, 
			FormFieldResolver resolver, List<Value<?>> sorted) throws ParseException {
		return buildCondition(expression, environment, 
				resolver, sorted, null, null, null, null, false);
	}

	/**
	 * Builds condition expression for prepared statement
	 * @param expression - Expression to be processed
	 * @param environment - Environment variables
	 * @param resolver - FormFieldResolver for the form
	 * @param sorted - [Out] Prepared sorted list of values for PreparedStatement
	 * @param leftAlias - left form alias
	 * @param rightAlias - right form alias
	 * @param leftFieldAlias - map contains left form field aliases
	 * @param rightFieldAlias - map contains right form field aliases
	 * @param isJoinClause - true if expression is join form condition, otherwise false
	 * @return
	 * @throws ParseException
	 */
	public String buildCondition(String expression, AbstractVariableSet<Value<?>> environment, 
			FormFieldResolver resolver, List<Value<?>> sorted, String leftAlias, String rightAlias, 
			Map<DBField, String> leftFieldAlias, Map<DBField, String> rightFieldAlias, 
			boolean isJoinClause) throws ParseException {
		
		String condition = "";
		final Iterator<String> tokens = tokenize(expression);
		Token previous = null;
		Token leftOperand = null;
		int brackets = 0;
		
		while (tokens.hasNext()) {
			// read one token from the input stream
			String strToken = tokens.next();
			final Token token = toToken(previous, strToken, resolver, isJoinClause);
			if (token.isOpenBracket()) {
				// If the token is a left parenthesis, then push it onto the stack.
				condition += "(";
				brackets++;
				if (previous!=null && previous.isFunction()) {
					if (!functionBrackets.containsKey(token.getBrackets().getOpen())) {
						throw new IllegalArgumentException("Invalid bracket after function: "+strToken);
					}
				} else {
					if (!expressionBrackets.containsKey(token.getBrackets().getOpen())) {
						throw new IllegalArgumentException("Invalid bracket in expression: "+strToken);
					}
				}
			} else if (token.isCloseBracket()) {
				if (previous==null || brackets==0) {
					throw new IllegalArgumentException("expression can't start with a close bracket");
				}
				if (previous.isFunctionArgumentSeparator()) {
					throw new IllegalArgumentException("argument is missing");
				}
				condition += ")";
				brackets--;
			} else if (token.isFunctionArgumentSeparator()) {
				if (previous==null) {
					throw new IllegalArgumentException("expression can't start with a function argument separator");
				}
				// Verify that there was an argument before this separator
				if (previous.isOpenBracket() || previous.isFunctionArgumentSeparator()) {
					// The cases were operator miss an operand are detected elsewhere.
					throw new IllegalArgumentException("argument is missing");
				}
				// If the token is a function argument separator
				condition+=",";
			} else if (token.isFunction()) {
				// If the token is a function token, then push it onto the stack.
				condition+=token.getFunction().getName();
			} else if (token.isOperator()) {
				// If the token is an operator, op1, then:
				condition+=" " + token.getOperator().getDbSymbol();
				if (previous.isField())
					leftOperand = previous;
			} else if (token.isField()) {
				// If token is a field
				condition += convertFieldToSqlStatement(token, leftAlias, rightAlias, 
						leftFieldAlias, rightFieldAlias, isJoinClause, resolver);
			} else {
				// If the token is a number (identifier), a constant or a variable, then add its value to the output queue.
				if ((previous!=null) && previous.isLiteral()) {
					throw new IllegalArgumentException("A literal can't follow another literal");
				}
				Value<?> v = toValue(token, environment, previous, leftOperand);
				if (v == null)
					throw new IllegalArgumentException("Syntax error. Can't read value for: " + token.getLiteral());
				if (v.getValue() == null) {
					if (previous.getOperator().equals(EQ)) {
						condition = condition.substring(0, condition.lastIndexOf(EQ.getDbSymbol()));
						condition += " IS NULL";
					} else if (previous.getOperator().equals(NOT_EQ)) {
						condition = condition.substring(0, condition.lastIndexOf(NOT_EQ.getDbSymbol()));
						condition += " IS NOT NULL";
					} else {
						condition += " ?";
						sorted.add(v);
					}
				} else {
					condition += " ?";
					sorted.add(v);
				}
				//output(values, token, evaluationContext);
			}
			previous = token;
		}
		if (brackets != 0) {
			throw new IllegalArgumentException("Invalid bracket in expression.");
		}
		return condition;
	}
	
	/**
	 * 
	 * @param token
	 * @param leftAlias
	 * @param rightAlias
	 * @param leftFieldAlias
	 * @param rightFieldAlias
	 * @param rightFields
	 * @return
	 */
	private String convertFieldToSqlStatement(Token token, String leftAlias, String rightAlias, 
			Map<DBField, String> leftFieldAlias, Map<DBField, String> rightFieldAlias, 
			boolean isJoinClause, FormFieldResolver resolver) {
		String res = null;
		/*
		 *  У нас могут быть три варианта развития событий:
		 *  1. Это обычная форма
		 *  2. Это джоин форма и where условие к ней
		 *  3. Это джоин форма и join условие к ней
		 */
		// not a join condition
		if (!isJoinClause) {
			// basic form
			if (!resolver.getForm().isJoin())
				res=" " + token.getField().getDBField().getName();
			else { // join form
				// if field mapped to the left form
				if (resolver.getLeftResolver().getForm().getName().equals(
						token.getField().getForm())) {
					res = " " + leftAlias + ".";
					// if left form is join, it can have field alias
					if (leftFieldAlias != null) {
						String alias = leftFieldAlias.get(token.getField().getDBField());
						if (alias != null)
							res += alias;
						else // if alias not found
							res += token.getField().getDBField().getName();
					} else // if alias map not specified, use table column name
						res += token.getField().getDBField().getName();
				} else {
					res = " " + rightAlias + ".";
					if (rightFieldAlias != null) {
						String alias = rightFieldAlias.get(token.getField().getDBField());
						if (alias != null)
							res += alias;
						else // if alias not found
							res += token.getField().getDBField().getName();
					} else // if alias map not specified, use table column name
						res += token.getField().getDBField().getName();
				}
			}
		}
		else { // if form is a join form
			if (token.isJoinField()) { // if field from the right form
				res=" " + rightAlias + ".";
				String alias = rightFieldAlias.get(token.getField().getDBField());
				if (alias == null)
					res += token.getField().getDBField().getName();
				else
					res += alias;
			} else { // if field from the left form
				res=" " + leftAlias + ".";
				String alias = leftFieldAlias.get(token.getField().getDBField());
				if (alias == null)
					res += token.getField().getDBField().getName();
				else
					res += alias;
			}
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	protected Value<?> toValue(Token token, Object evaluationContext,
			Token previous, Token lastField) {
		
		if (token.isLiteral()) { // If the token is a literal, a constant, or a variable name
			String literal = token.getLiteral();
			Constant ct = this.constants.get(literal);
			Value<?> value = convert(ct, previous, lastField);
			if (value==null && evaluationContext!=null && (evaluationContext instanceof AbstractVariableSet)) {
				value = ((AbstractVariableSet<Value<?>>)evaluationContext).get(literal);
			}
			if (value != null) {
				return value;
			}
			if (lastField.isField()) {
				Value<?> v = convert(literal, lastField.getField());
				if (v == null) {
					throw new IllegalArgumentException("Incorrect value for field " + 
							lastField.getLiteral() + " value: " + literal);
				}
				return v;
			}
		} else {
			throw new IllegalArgumentException();
		}
		return null;
	}
	
	protected Value<?> convert(String s, IField<?> field) {
		return field.fromString(s);
	}
	
	protected Value<?> convert(Constant c, Token previous, Token lastField) {
		if (NULL.equals(c) && lastField.isField()) {
			return convert((String)null, lastField.getField());
		}
		return null;
	}

	protected Token toToken(Token previous, String token, FormFieldResolver resolver, 
			boolean isJoinClause) {
		
		if (token.equals(functionArgumentSeparator)) {
			return Token.FUNCTION_ARG_SEPARATOR;
		} else if (functions.containsKey(token)) {
			return Token.buildFunction(functions.get(token));
		} else if (operators.containsKey(token)) {
			List<Operator> list = operators.get(token);
			return (list.size()==1) ? Token.buildOperator(list.get(0)) : Token.buildOperator(guessOperator(previous, list));
		} else {
			final BracketPair brackets = getBracketPair(token);
			if (brackets!=null) {
				if (brackets.getOpen().equals(token)) {
					return Token.buildOpenToken(brackets);
				} else {
					return Token.buildCloseToken(brackets);
				}
			} else if (token.startsWith("'") && token.endsWith("'")) {
				// it can be basic form field or left join form field
				String fname = token.substring(1, token.length()-1);
				Collection<IField<?>> fields = null;
				// if it is join condition, we need resolve fields separately for each form
				if (isJoinClause)
					fields = resolver.getLeftResolver().getFields().values();
				else
					fields = resolver.getFields().values();
				
				for (IField<?> f : fields) {
					if (f.getId().equals(fname))
						return Token.buildFieldToken(f);
				}
				throw new IllegalArgumentException("Field not found: " + fname);
			} else if (token.startsWith("`") && token.endsWith("`")) {
				String fname = token.substring(1, token.length()-1);
				for (IField<?> f : resolver.getRightResolver().getFields().values()) {
					if (f.getId().equals(fname))
						return Token.buildJoinFieldToken(f);
				}
				throw new IllegalArgumentException("Field not found: " + fname);
			} else if (token.startsWith("\"") && token.endsWith("\"")) {
				String lit = token.substring(1, token.length()-1);
				return Token.buildLiteral(lit);
			} else {
				return Token.buildLiteral(token);
			}
		}
	}
	
	/** When a token can be more than one operator (homonym operators), this method guesses the right operator.
	 * <br>A very common case is the - sign in arithmetic computation which can be an unary or a binary operator, depending
	 * on what was the previous token. 
	 * <br><b>Warning:</b> maybe the arguments of this function are not enough to deal with all the cases.
	 * So, this part of the evaluation is in alpha state (method may change in the future).
	 * @param previous The last parsed tokens (the previous token in the infix expression we are evaluating). 
	 * @param candidates The candidate tokens.
	 * @return A token
	 * @see #validateHomonyms(List)
	 */
	protected Operator guessOperator(Token previous, List<Operator> candidates) {
		final int argCount = ((previous!=null) && (previous.isCloseBracket() || previous.isLiteral())) ? 2 : 1;
		for (Operator operator : candidates) {
			if (operator.getOperandCount()==argCount) {
				return operator;
			}
		}
		return null;
	}
	
	private BracketPair getBracketPair(String token) {
		BracketPair result = expressionBrackets.get(token);
		return result==null ? functionBrackets.get(token) : result;
	}

	/** Converts the evaluated expression into tokens.
	 * <br>Example: The result for the expression "<i>-1+min(10,3)</i>" is an iterator on "-", "1", "+", "min", "(", "10", ",", "3", ")".
	 * <br>By default, the operators symbols, the brackets and the function argument separator are used as delimiter in the string.
	 * @param expression The expression that is evaluated
	 * @return A string iterator.
	 * @throws ParseException 
	 */
	protected Iterator<String> tokenize(String expression) throws ParseException {
		return tokenizer.tokenize(expression);
	}
}
