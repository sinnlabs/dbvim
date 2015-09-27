/**
 * 
 */
package org.sinnlabs.dbvim.evaluator.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.sinnlabs.dbvim.evaluator.ConditionTokenizer;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;

/**
 * @author peter.liverovsky
 *
 */
public class ConditionTokenizerTest {

	/**
	 * Test method for {@link org.sinnlabs.dbvim.evaluator.ConditionTokenizer#tokenize(java.lang.String)}.
	 * @throws ParseException 
	 */
	@Test
	public void testTokenize() throws ParseException {
		String condition = "('field1'=-158 AND 'field2'=\" 3 dsds$(90 \") OR 'field3'<$TIMESTAMP$ and 'field 4' > 12.3";
		List<String> testTokens = new ArrayList<String>();
		fillTokens(testTokens);
		ConditionTokenizer tokenizer = new ConditionTokenizer('.', null);
		Iterator<String> tokens = tokenizer.tokenize(condition);
		Iterator<String> test = testTokens.iterator();
		while(tokens.hasNext() && test.hasNext()) {
			String token = tokens.next();
			String t = test.next();
			System.out.println(token);
			if (!token.equals(t))
				fail("Tokens does not mutch.");
		}
	}
	
	private void fillTokens(List<String> tokens) {
		tokens.add("(");
		tokens.add("'field1'");
		tokens.add("=");
		tokens.add("-");
		tokens.add("158");
		tokens.add("AND");
		tokens.add("'field2'");
		tokens.add("=");
		tokens.add("\" 3 dsds$(90 \"");
		tokens.add(")");
		tokens.add("OR");
		tokens.add("'field3'");
		tokens.add("<");
		tokens.add("$TIMESTAMP$");
		tokens.add("and");
		tokens.add("'field 4'");
		tokens.add(">");
		tokens.add("12.3");
	}

}
