package org.monkey.parser;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.monkey.ast.*;
import org.monkey.lexer.Lexer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testLetStatements() {
        String input = """
                       let x = 5;
                       let y = 10;
                       let foobar = 838383;
                       """;

        Parser parser = new Parser(new Lexer(input));
        Program program = parser.parseProgram();

        checkParserErrors(parser);
        assertNotNull(program);
        assertEquals(3, program.getStatements().size(), "program.Statements does not contain 3 statements");

        String[] expectedIdentifiers = { "x", "y", "foobar" };
        for (int i = 0; i < expectedIdentifiers.length; i++) {
            Statement stmt = program.getStatements().get(i);
            assertInstanceOf(LetStatement.class, stmt, "stmt not LetStatement");
            testLetStatement((LetStatement) stmt, expectedIdentifiers[i]);
        }
    }

    @Test
    void testReturnStatements() {
        String input = """
                       return 5;
                       return 10;
                       return 993322;
                       """;

        Parser parser = new Parser(new Lexer(input));
        Program program = parser.parseProgram();

        checkParserErrors(parser);
        assertEquals(3, program.getStatements().size());

        for (Statement stmt : program.getStatements()) {
            assertInstanceOf(ReturnStatement.class, stmt, "stmt not ReturnStatement");
            assertEquals("return", stmt.tokenLiteral());
        }
    }

    @Test
    void testIdentifierExpression() {
        String input = "foobar;";

        Parser parser = new Parser(new Lexer(input));
        Program program = parser.parseProgram();

        checkParserErrors(parser);
        assertEquals(1, program.getStatements().size());

        Statement stmt = program.getStatements().getFirst();
        assertInstanceOf(ExpressionStatement.class, stmt);
        ExpressionStatement expStmt = (ExpressionStatement) stmt;

        assertInstanceOf(Identifier.class, expStmt.expression());
        Identifier ident = (Identifier) expStmt.expression();
        assertEquals("foobar", ident.value());
        assertEquals("foobar", ident.tokenLiteral());
    }

    @Nested
    class ParseBaseObjectTests {
        @Test
        void testIntegerLiteralExpression() {
            String input = "5;";

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();

            checkParserErrors(parser);
            assertEquals(1, program.getStatements().size());

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            ExpressionStatement expStmt = (ExpressionStatement) stmt;

            assertInstanceOf(IntegerLiteral.class, expStmt.expression());
            IntegerLiteral literal = (IntegerLiteral) expStmt.expression();
            assertEquals(5, literal.value());
            assertEquals("5", literal.tokenLiteral());
        }

        @Test
        void testBooleanExpression() {
            record BooleanTest(String input, boolean expectedBoolean) { }

            BooleanTest[] tests = {
                    new BooleanTest("true;", true),
                    new BooleanTest("false;", false)
            };

            for (BooleanTest tt : tests) {
                Parser parser = new Parser(new Lexer(tt.input));
                Program program = parser.parseProgram();
                checkParserErrors(parser);

                assertEquals(1, program.getStatements().size());
                Statement stmt = program.getStatements().getFirst();
                assertInstanceOf(ExpressionStatement.class, stmt);
                ExpressionStatement expStmt = (ExpressionStatement) stmt;

                assertInstanceOf(BooleanLiteral.class, expStmt.expression());
                BooleanLiteral bool = (BooleanLiteral) expStmt.expression();
                assertEquals(tt.expectedBoolean, bool.value());
                assertEquals(String.valueOf(tt.expectedBoolean), bool.tokenLiteral());
            }
        }

        @Test
        void testStringLiteralExpression() {
            String input = """
                           "hello world";
                           """.trim();

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            var literal = ((ExpressionStatement) stmt).expression();

            assertEquals("hello world", literal.string());
        }


    }



    @Test
    void testIfExpression() {
        String input = "if (x < y) { x }";

        Parser parser = new Parser(new Lexer(input));
        Program program = parser.parseProgram();
        checkParserErrors(parser);

        assertEquals(1, program.getStatements().size());
        Statement stmt = program.getStatements().getFirst();
        assertInstanceOf(ExpressionStatement.class, stmt);
        ExpressionStatement expStmt = (ExpressionStatement) stmt;

        assertInstanceOf(IfExpression.class, expStmt.expression());
        IfExpression exp = (IfExpression) expStmt.expression();

        testInfixExpression(exp.condition(), "x", "<", "y");
        assertEquals(1, exp.consequence().statements().size());

        Statement consequence = exp.consequence().statements().getFirst();
        assertInstanceOf(ExpressionStatement.class, consequence);
        ExpressionStatement consequenceExp = (ExpressionStatement) consequence;

        testIdentifier(consequenceExp.expression(), "x");
        assertNull(exp.alternative());
    }

    @Test
    void testIfElseExpression() {
        String input = "if (x < y) { x } else { y }";

        Parser parser = new Parser(new Lexer(input));
        Program program = parser.parseProgram();
        checkParserErrors(parser);

        assertEquals(1, program.getStatements().size());
        Statement stmt = program.getStatements().getFirst();
        assertInstanceOf(ExpressionStatement.class, stmt);
        ExpressionStatement expStmt = (ExpressionStatement) stmt;

        assertInstanceOf(IfExpression.class, expStmt.expression());
        IfExpression exp = (IfExpression) expStmt.expression();

        testInfixExpression(exp.condition(), "x", "<", "y");
        assertEquals(1, exp.consequence().statements().size());

        Statement consequence = exp.consequence().statements().getFirst();
        assertInstanceOf(ExpressionStatement.class, consequence);
        ExpressionStatement consequenceExp = (ExpressionStatement) consequence;
        testIdentifier(consequenceExp.expression(), "x");

        assertNotNull(exp.alternative());
        assertEquals(1, exp.alternative().statements().size());
        Statement alternative = exp.alternative().statements().getFirst();
        assertInstanceOf(ExpressionStatement.class, alternative);
        ExpressionStatement alternativeExp = (ExpressionStatement) alternative;
        testIdentifier(alternativeExp.expression(), "y");
    }


    @Nested
    class ParseFunctionLiteralTests {
        @Test
        void testFunctionLiteralParsing() {
            String input = "fn(x, y) { x + y; }";

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            assertEquals(1, program.getStatements().size());
            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            ExpressionStatement expStmt = (ExpressionStatement) stmt;

            assertInstanceOf(FunctionLiteral.class, expStmt.expression());
            FunctionLiteral function = (FunctionLiteral) expStmt.expression();

            assertEquals(2, function.parameters().size());
            testLiteralExpression(function.parameters().get(0), "x");
            testLiteralExpression(function.parameters().get(1), "y");

            assertEquals(1, function.body().statements().size());
            Statement bodyStmt = function.body().statements().getFirst();
            assertInstanceOf(ExpressionStatement.class, bodyStmt);
            testInfixExpression(((ExpressionStatement) bodyStmt).expression(), "x", "+", "y");
        }

        @Test
        void testFunctionParameterParsing() {
            record ParameterTest(String input, String[] expectedParams) { }

            ParameterTest[] tests = {
                    new ParameterTest("fn() {};", new String[]{ }),
                    new ParameterTest("fn(x) {};", new String[]{ "x" }),
                    new ParameterTest("fn(x, y, z) {};", new String[]{ "x", "y", "z" })
            };

            for (ParameterTest tt : tests) {
                Parser parser = new Parser(new Lexer(tt.input));
                Program program = parser.parseProgram();
                checkParserErrors(parser);

                Statement stmt = program.getStatements().getFirst();
                assertInstanceOf(ExpressionStatement.class, stmt);
                var function = (FunctionLiteral) ((ExpressionStatement) stmt).expression();

                assertEquals(tt.expectedParams.length, function.parameters().size());
                for (int i = 0; i < tt.expectedParams.length; i++) {
                    testLiteralExpression(function.parameters().get(i), tt.expectedParams[i]);
                }
            }
        }

        @Test
        void testCallExpressionParsing() {
            String input = "add(1, 2 * 3, 4 + 5);";

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            assertEquals(1, program.getStatements().size());
            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            ExpressionStatement expStmt = (ExpressionStatement) stmt;

            assertInstanceOf(CallExpression.class, expStmt.expression());
            CallExpression exp = (CallExpression) expStmt.expression();

            testIdentifier(exp.function(), "add");
            assertEquals(3, exp.arguments().size());

            testLiteralExpression(exp.arguments().get(0), 1);
            testInfixExpression(exp.arguments().get(1), 2, "*", 3);
            testInfixExpression(exp.arguments().get(2), 4, "+", 5);
        }

        @Test
        void testCallExpressionParameterParsing() {
            record CallTest(String input, String expectedIdent, String[] expectedArgs) { }

            CallTest[] tests = {
                    new CallTest("add();", "add", new String[]{ }),
                    new CallTest("add(1);", "add", new String[]{ "1" }),
                    new CallTest("add(1, 2 * 3, 4 + 5);", "add", new String[]{ "1", "(2 * 3)", "(4 + 5)" })
            };

            for (CallTest tt : tests) {
                Parser parser = new Parser(new Lexer(tt.input));
                Program program = parser.parseProgram();
                checkParserErrors(parser);

                Statement stmt = program.getStatements().getFirst();
                assertInstanceOf(ExpressionStatement.class, stmt);
                CallExpression exp = (CallExpression) ((ExpressionStatement) stmt).expression();

                testIdentifier(exp.function(), tt.expectedIdent);

                assertEquals(tt.expectedArgs.length, exp.arguments().size());

                for (int i = 0; i < tt.expectedArgs.length; i++) {
                    assertEquals(tt.expectedArgs[i], exp.arguments().get(i).string());
                }
            }
        }

    }

    @Nested
    class ParseArrayLiteralTests {
        @Test
        void testParsingIndexExpressions() {
            String input = "myArray[1 + 1]";

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            var indexExp = (IndexExpression) ((ExpressionStatement) stmt).expression();

            testIdentifier(indexExp.left(), "myArray");
            testInfixExpression(indexExp.index(), 1, "+", 1);
        }

        @Test
        void testParsingEmptyArrayLiterals() {
            String input = "[]";

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            ArrayLiteral array = (ArrayLiteral) ((ExpressionStatement) stmt).expression();

            assertEquals(0, array.elements().size());
        }

        @Test
        void testParsingHashLiteralsStringKeys() {
            String input = """
                           {"one": 1, "two": 2, "three": 3}
                           """.trim();

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            HashLiteral hash = (HashLiteral) ((ExpressionStatement) stmt).expression();

            assertEquals(3, hash.pairs().size());

            Map<String, Integer> expected = new HashMap<>();
            expected.put("one", 1);
            expected.put("two", 2);
            expected.put("three", 3);

            for (Map.Entry<Expression, Expression> pair : hash.pairs().entrySet()) {
                assertInstanceOf(StringLiteral.class, pair.getKey());
                var key = ((StringLiteral) pair.getKey()).value();

                Integer expectedValue = expected.get(key);
                assertNotNull(expectedValue);
                testIntegerLiteral(pair.getValue(), expectedValue);
            }
        }

        @Test
        void testParsingHashLiteralsWithExpressions() {
            String input = """
                           {"one": 0 + 1, "two": 10 - 8, "three": 15 / 5}
                           """.trim();

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            HashLiteral hash = (HashLiteral) ((ExpressionStatement) stmt).expression();

            assertEquals(3, hash.pairs().size());

            Map<String, Consumer<Expression>> tests = new HashMap<>();
            tests.put("one", exp -> testInfixExpression(exp, 0, "+", 1));
            tests.put("two", exp -> testInfixExpression(exp, 10, "-", 8));
            tests.put("three", exp -> testInfixExpression(exp, 15, "/", 5));

            for (Map.Entry<Expression, Expression> pair : hash.pairs().entrySet()) {
                assertInstanceOf(StringLiteral.class, pair.getKey());
                String key = ((StringLiteral) pair.getKey()).value();

                Consumer<Expression> testFunc = tests.get(key);
                assertNotNull(testFunc);
                testFunc.accept(pair.getValue());
            }
        }

        @Test
        void testParsingArrayLiterals() {
            String input = "[1, 2 * 2, 3 + 3]";

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            ArrayLiteral array = (ArrayLiteral) ((ExpressionStatement) stmt).expression();

            assertEquals(3, array.elements().size());
            testIntegerLiteral(array.elements().get(0), 1);
            testInfixExpression(array.elements().get(1), 2, "*", 2);
            testInfixExpression(array.elements().get(2), 3, "+", 3);
        }
    }

    @Nested
    class ParseHashLiteralTests {
        @Test
        void testParsingHashLiteralsBooleanKeys() {
            String input = "{true: 1, false: 2}";

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            HashLiteral hash = (HashLiteral) ((ExpressionStatement) stmt).expression();

            assertEquals(2, hash.pairs().size());

            Map<String, Integer> expected = new HashMap<>();
            expected.put("true", 1);
            expected.put("false", 2);

            for (Map.Entry<Expression, Expression> pair : hash.pairs().entrySet()) {
                assertInstanceOf(BooleanLiteral.class, pair.getKey());
                var key = ((BooleanLiteral) pair.getKey()).value();

                Integer expectedValue = expected.get(String.valueOf(key));
                assertNotNull(expectedValue);
                testIntegerLiteral(pair.getValue(), expectedValue);
            }
        }

        @Test
        void testParsingHashLiteralsIntegerKeys() {
            String input = "{1: 1, 2: 2, 3: 3}";

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            HashLiteral hash = (HashLiteral) ((ExpressionStatement) stmt).expression();

            assertEquals(3, hash.pairs().size());

            Map<String, Integer> expected = new HashMap<>();
            expected.put("1", 1);
            expected.put("2", 2);
            expected.put("3", 3);

            for (Map.Entry<Expression, Expression> pair : hash.pairs().entrySet()) {
                assertInstanceOf(IntegerLiteral.class, pair.getKey());
                var key = ((IntegerLiteral) pair.getKey()).value();

                Integer expectedValue = expected.get(String.valueOf(key));
                assertNotNull(expectedValue);
                testIntegerLiteral(pair.getValue(), expectedValue);
            }
        }

        @Test
        void testParsingEmptyHashLiteral() {
            String input = "{}";

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            HashLiteral hash = (HashLiteral) ((ExpressionStatement) stmt).expression();

            assertEquals(0, hash.pairs().size());
        }

        @Test
        void testParsingHashLiteralsStringKeys() {
            String input = """
                           {"one": 1, "two": 2, "three": 3}
                           """.trim();

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            HashLiteral hash = (HashLiteral) ((ExpressionStatement) stmt).expression();

            assertEquals(3, hash.pairs().size());

            Map<String, Integer> expected = new HashMap<>();
            expected.put("one", 1);
            expected.put("two", 2);
            expected.put("three", 3);

            for (Map.Entry<Expression, Expression> pair : hash.pairs().entrySet()) {
                assertInstanceOf(StringLiteral.class, pair.getKey());
                var key = ((StringLiteral) pair.getKey()).value();

                Integer expectedValue = expected.get(key);
                assertNotNull(expectedValue);
                testIntegerLiteral(pair.getValue(), expectedValue);
            }
        }

        @Test
        void testParsingHashLiteralsWithExpressions() {
            String input = """
                           {"one": 0 + 1, "two": 10 - 8, "three": 15 / 5}
                           """.trim();

            Parser parser = new Parser(new Lexer(input));
            Program program = parser.parseProgram();
            checkParserErrors(parser);

            Statement stmt = program.getStatements().getFirst();
            assertInstanceOf(ExpressionStatement.class, stmt);
            HashLiteral hash = (HashLiteral) ((ExpressionStatement) stmt).expression();

            assertEquals(3, hash.pairs().size());

            Map<String, Consumer<Expression>> tests = new HashMap<>();
            tests.put("one", exp -> testInfixExpression(exp, 0, "+", 1));
            tests.put("two", exp -> testInfixExpression(exp, 10, "-", 8));
            tests.put("three", exp -> testInfixExpression(exp, 15, "/", 5));

            for (Map.Entry<Expression, Expression> pair : hash.pairs().entrySet()) {
                assertInstanceOf(StringLiteral.class, pair.getKey());
                String key = ((StringLiteral) pair.getKey()).value();

                Consumer<Expression> testFunc = tests.get(key);
                assertNotNull(testFunc);
                testFunc.accept(pair.getValue());
            }
        }

    }

    @Nested
    class ParseOperatorTests {
        @Test
        void testParsingPrefixExpressions() {
            record PrefixTest(String input, String operator, Object value) { }

            PrefixTest[] prefixTests = {
                    new PrefixTest("!5;", "!", 5),
                    new PrefixTest("-15;", "-", 15),
                    new PrefixTest("!true;", "!", true),
                    new PrefixTest("!false;", "!", false)
            };

            for (PrefixTest tt : prefixTests) {
                Parser parser = new Parser(new Lexer(tt.input));
                Program program = parser.parseProgram();

                checkParserErrors(parser);
                assertEquals(1, program.getStatements().size());

                Statement stmt = program.getStatements().getFirst();
                assertInstanceOf(ExpressionStatement.class, stmt);
                ExpressionStatement expStmt = (ExpressionStatement) stmt;

                assertInstanceOf(PrefixExpression.class, expStmt.expression());
                PrefixExpression exp = (PrefixExpression) expStmt.expression();
                assertEquals(tt.operator, exp.operator());
                testLiteralExpression(exp.right(), tt.value);
            }
        }

        @Test
        void testParsingInfixExpressions() {
            record InfixTest(String input, Object leftValue, String operator, Object rightValue) { }

            InfixTest[] infixTests = {
                    new InfixTest("5 + 5;", 5, "+", 5),
                    new InfixTest("5 - 5;", 5, "-", 5),
                    new InfixTest("5 * 5;", 5, "*", 5),
                    new InfixTest("5 / 5;", 5, "/", 5),
                    new InfixTest("5 > 5;", 5, ">", 5),
                    new InfixTest("5 < 5;", 5, "<", 5),
                    new InfixTest("5 == 5;", 5, "==", 5),
                    new InfixTest("5 != 5;", 5, "!=", 5),
                    new InfixTest("true == true", true, "==", true),
                    new InfixTest("true != false", true, "!=", false),
                    new InfixTest("false == false", false, "==", false)
            };

            for (InfixTest tt : infixTests) {
                Parser parser = new Parser(new Lexer(tt.input));
                Program program = parser.parseProgram();
                checkParserErrors(parser);

                assertEquals(1, program.getStatements().size());
                Statement stmt = program.getStatements().getFirst();
                assertInstanceOf(ExpressionStatement.class, stmt);
                ExpressionStatement expStmt = (ExpressionStatement) stmt;

                assertInstanceOf(InfixExpression.class, expStmt.expression());
                InfixExpression exp = (InfixExpression) expStmt.expression();

                testLiteralExpression(exp.left(), tt.leftValue);
                assertEquals(tt.operator, exp.operator());
                testLiteralExpression(exp.right(), tt.rightValue);
            }
        }

        @Test
        void testOperatorPrecedenceParsing() {
            record PrecedenceTest(String input, String expected) { }

            PrecedenceTest[] tests = {
                    new PrecedenceTest("-a * b", "((-a) * b)"),
                    new PrecedenceTest("!-a", "(!(-a))"),
                    new PrecedenceTest("a + b + c", "((a + b) + c)"),
                    new PrecedenceTest("a + b - c", "((a + b) - c)"),
                    new PrecedenceTest("a * b * c", "((a * b) * c)"),
                    new PrecedenceTest("a * b / c", "((a * b) / c)"),
                    new PrecedenceTest("a + b / c", "(a + (b / c))"),
                    new PrecedenceTest("a + b * c + d / e - f", "(((a + (b * c)) + (d / e)) - f)"),
                    new PrecedenceTest("3 + 4; -5 * 5", "(3 + 4)((-5) * 5)"),
                    new PrecedenceTest("5 > 4 == 3 < 4", "((5 > 4) == (3 < 4))"),
                    new PrecedenceTest("5 < 4 != 3 > 4", "((5 < 4) != (3 > 4))"),
                    new PrecedenceTest("3 + 4 * 5 == 3 * 1 + 4 * 5", "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))"),
                    new PrecedenceTest("true", "true"),
                    new PrecedenceTest("false", "false"),
                    new PrecedenceTest("3 > 5 == false", "((3 > 5) == false)"),
                    new PrecedenceTest("3 < 5 == true", "((3 < 5) == true)"),
                    new PrecedenceTest("1 + (2 + 3) + 4", "((1 + (2 + 3)) + 4)"),
                    new PrecedenceTest("(5 + 5) * 2", "((5 + 5) * 2)"),
                    new PrecedenceTest("2 / (5 + 5)", "(2 / (5 + 5))"),
                    new PrecedenceTest("-(5 + 5)", "(-(5 + 5))"),
                    new PrecedenceTest("!(true == true)", "(!(true == true))"),
                    new PrecedenceTest("a + add(b * c) + d", "((a + add((b * c))) + d)"),
                    new PrecedenceTest("add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))",
                                       "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))"),
                    new PrecedenceTest("add(a + b + c * d / f + g)", "add((((a + b) + ((c * d) / f)) + g))")
            };

            for (PrecedenceTest tt : tests) {
                Parser parser = new Parser(new Lexer(tt.input));
                Program program = parser.parseProgram();
                checkParserErrors(parser);

                String actual = program.string();
                assertEquals(tt.expected, actual);
            }
        }
    }

    private void checkParserErrors(Parser parser) {
        var errors = parser.errors();
        if (errors.isEmpty()) {
            return;
        }

        System.err.println("parser has " + errors.size() + " errors");
        for (String error : errors) {
            System.err.println("parser error: " + error);
        }
        fail("parser has errors");
    }

    private void testLetStatement(LetStatement stmt, String name) {
        assertEquals("let", stmt.tokenLiteral());
        assertEquals(name, stmt.name().value());
        assertEquals(name, stmt.name().tokenLiteral());
    }

    private void testLiteralExpression(Expression exp, Object expected) {
        switch (expected) {
            case Integer i -> testIntegerLiteral(exp, i);
            case String s -> testIdentifier(exp, s);
            case Boolean b -> testBooleanLiteral(exp, b);
            case null, default -> fail("type of exp not handled. got=" + exp);
        }
    }

    private void testIntegerLiteral(Expression exp, int value) {
        assertInstanceOf(IntegerLiteral.class, exp);
        IntegerLiteral integ = (IntegerLiteral) exp;
        assertEquals(value, integ.value());
        assertEquals(String.valueOf(value), integ.tokenLiteral());
    }

    private void testIdentifier(Expression exp, String value) {
        assertInstanceOf(Identifier.class, exp);
        Identifier ident = (Identifier) exp;
        assertEquals(value, ident.value());
        assertEquals(value, ident.tokenLiteral());
    }

    private void testBooleanLiteral(Expression exp, boolean value) {
        assertInstanceOf(BooleanLiteral.class, exp);
        BooleanLiteral bool = (BooleanLiteral) exp;
        assertEquals(value, bool.value());
        assertEquals(String.valueOf(value), bool.tokenLiteral());
    }

    private void testInfixExpression(Expression exp, Object left, String operator, Object right) {
        assertInstanceOf(InfixExpression.class, exp);
        InfixExpression infixExp = (InfixExpression) exp;

        testLiteralExpression(infixExp.left(), left);
        assertEquals(operator, infixExp.operator());
        testLiteralExpression(infixExp.right(), right);
    }
}