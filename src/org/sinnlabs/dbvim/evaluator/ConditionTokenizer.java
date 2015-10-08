/**
 * 
 */
package org.sinnlabs.dbvim.evaluator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;

/**
 * A String tokenizer
 * @author peter.liverovsky
 *
 */
public class ConditionTokenizer {
	
	protected static final int FIELD_STATE = 0;
	protected static final int DELIMETER_STATE = 1;
	protected static final int VALUE_STATE = 2;
	protected static final int LITERAL_STATE = 3;
	protected static final int NUMBER_STATE = 4;
	protected static final int OPERATOR_STATE = 5;
	protected static final int JOIN_FIELD_STATE = 6;
	
	private char decimalSeparator = '.';
	private List<String> delimeters;
	
	public ConditionTokenizer(char decimalSeparator, List<String> delimeters) {
		this.decimalSeparator = decimalSeparator;
		this.delimeters = delimeters;
	}
	
	/** Converts a string into tokens.
	 * @param string The string to be split into tokens
	 * @return The tokens
	 * @throws ParseException 
	 */
	public Iterator<String> tokenize(String string) throws ParseException {
		List<String> tokens = new ArrayList<String>();
		int state = DELIMETER_STATE;
		String token = "";
		for(int i=0; i<string.length(); i++) {
			char c = string.charAt(i);
			switch(state) {
			case DELIMETER_STATE:
				if (c == '(' || c == ')') {
					tokens.add(String.valueOf(c));
					continue;
				} else if (c=='\'') { // field name start
					state = FIELD_STATE;
					token = "'";
					continue;
				} else if (c=='`') { // join field name start
					state = JOIN_FIELD_STATE;
					token = "`";
					continue;
				} else if (c == '"') {
					state = VALUE_STATE;
					token = "\"";
					continue;
				} else if (Character.isWhitespace(c)) {
					continue;
				} else if (Character.isJavaIdentifierStart(c)) {
					token = String.valueOf(c);
					state = LITERAL_STATE;
					continue;
				} else if (Character.isDigit(c)) {
					token = String.valueOf(c);
					state = NUMBER_STATE;
					continue;
				} else { // all other characters
					token = String.valueOf(c);
					state = OPERATOR_STATE;
				}
				break;
			case FIELD_STATE:
				if (c == '\'') {
					token += "'";
					tokens.add(token);
					token = "";
					state = DELIMETER_STATE;
				} else if (c == '\\') {
					if (i<string.length()-1) {
						i++;
						token += string.charAt(i);
						continue;
					} else {
						throw new ParseException("Expected: \'.");
					}
				} else {
					token += c;
				}
				break;
			case JOIN_FIELD_STATE:
				if (c == '`') {
					token += "`";
					tokens.add(token);
					token = "";
					state = DELIMETER_STATE;
				} else if (c == '\\') {
					if (i<string.length()-1) {
						i++;
						token += string.charAt(i);
						continue;
					} else {
						throw new ParseException("Expected: \\`.");
					}
				} else {
					token += c;
				}
				break;
			case VALUE_STATE:
				if (c == '"') {
					token += "\"";
					tokens.add(token);
					token = "";
					state = DELIMETER_STATE;
				} else if (c == '\\') {
					if (i<string.length()-1) {
						i++;
						token += string.charAt(i);
						continue;
					} else {
						throw new ParseException("Expected: \".");
					}
				} else {
					token += c;
				}
				break;
			case LITERAL_STATE:
				if (!Character.isJavaIdentifierPart(c)) {
					tokens.add(token);
					token = "";
					state = DELIMETER_STATE;
					if (!Character.isWhitespace(c)) // if character not a white space
						i--;
					continue;
				} else {
					token += c;
				}
				break;
			case NUMBER_STATE:
				if (!Character.isDigit(c) && c!=decimalSeparator) {
					tokens.add(token);
					token = "";
					state = DELIMETER_STATE;
					if (!Character.isWhitespace(c))
						i--;
				} else {
					token += c;
				}
				break;
			case OPERATOR_STATE:
				boolean hasCandidate = false;
				for(String d : delimeters) {
					if (d.startsWith(token) && !d.equals(token)) {
						hasCandidate = true;
						break;
					}
				}
				if (!hasCandidate || Character.isWhitespace(c)) {
					tokens.add(token);
					i--;
					state = DELIMETER_STATE;
				}
				token += c;
			}
		}
		if (state == LITERAL_STATE || state == NUMBER_STATE)
			tokens.add(token);
		if (state == FIELD_STATE)
			throw new ParseException("Unexpected end of statement. Expected: \'");
		if (state == VALUE_STATE)
			throw new ParseException("Unexpected end of statement. Expected: \"");
		return tokens.iterator();
	}
}
