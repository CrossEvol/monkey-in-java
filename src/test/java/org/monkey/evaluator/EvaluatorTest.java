package org.monkey.evaluator;

import org.junit.jupiter.api.Test;
import org.monkey.lexer.Lexer;
import org.monkey.object.*;
import org.monkey.object.Boolean;
import org.monkey.object.Error;
import org.monkey.object.Integer;
import org.monkey.object.String;
import org.monkey.parser.Parser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorTest {

    private static final Boolean TRUE = new Boolean(true);
    private static final Boolean FALSE = new Boolean(false);

    @Test
    void testEvalIntegerExpression() {
        record TestCase(java.lang.String input, int expected) { }

        var tests = List.of(
                new TestCase("5", 5),
                new TestCase("10", 10),
                new TestCase("-5", -5),
                new TestCase("-10", -10),
                new TestCase("5 + 5 + 5 + 5 - 10", 10),
                new TestCase("2 * 2 * 2 * 2 * 2", 32),
                new TestCase("-50 + 100 + -50", 0),
                new TestCase("5 * 2 + 10", 20),
                new TestCase("5 + 2 * 10", 25),
                new TestCase("20 + 2 * -10", 0),
                new TestCase("50 / 2 * 2 + 10", 60),
                new TestCase("2 * (5 + 10)", 30),
                new TestCase("3 * 3 * 3 + 10", 37),
                new TestCase("3 * (3 * 3) + 10", 37),
                new TestCase("(5 + 10 * 2 + 15 / 3) * 2 + -10", 50)
        );

        for (TestCase tt : tests) {
            var evaluated = testEval(tt.input);
            testIntegerObject(evaluated, tt.expected);
        }
    }

    @Test
    void testEvalBooleanExpression() {
        record TestCase(java.lang.String input, boolean expected) { }

        var tests = List.of(
                new TestCase("true", true),
                new TestCase("false", false),
                new TestCase("1 < 2", true),
                new TestCase("1 > 2", false),
                new TestCase("1 < 1", false),
                new TestCase("1 > 1", false),
                new TestCase("1 == 1", true),
                new TestCase("1 != 1", false),
                new TestCase("1 == 2", false),
                new TestCase("1 != 2", true),
                new TestCase("true == true", true),
                new TestCase("false == false", true),
                new TestCase("true == false", false),
                new TestCase("true != false", true),
                new TestCase("false != true", true),
                new TestCase("(1 < 2) == true", true),
                new TestCase("(1 < 2) == false", false),
                new TestCase("(1 > 2) == true", false),
                new TestCase("(1 > 2) == false", true)
        );

        for (TestCase tt : tests) {
            var evaluated = testEval(tt.input);
            testBooleanObject(evaluated, tt.expected);
        }
    }

    @Test
    void testBangOperator() {
        record TestCase(java.lang.String input, boolean expected) { }

        var tests = List.of(
                new TestCase("!true", false),
                new TestCase("!false", true),
                new TestCase("!5", false),
                new TestCase("!!true", true),
                new TestCase("!!false", false),
                new TestCase("!!5", true)
        );

        for (TestCase tt : tests) {
            var evaluated = testEval(tt.input);
            testBooleanObject(evaluated, tt.expected);
        }
    }

    @Test
    void testIfElseExpressions() {
        record TestCase(java.lang.String input, java.lang.Object expected) { }

        var tests = List.of(
                new TestCase("if (true) { 10 }", 10),
                new TestCase("if (false) { 10 }", null),
                new TestCase("if (1) { 10 }", 10),
                new TestCase("if (1 < 2) { 10 }", 10),
                new TestCase("if (1 > 2) { 10 }", null),
                new TestCase("if (1 > 2) { 10 } else { 20 }", 20),
                new TestCase("if (1 < 2) { 10 } else { 20 }", 10)
        );

        for (TestCase tt : tests) {
            var evaluated = testEval(tt.input);
            if (tt.expected instanceof java.lang.Integer) {
                testIntegerObject(evaluated, (java.lang.Integer) tt.expected);
            } else {
                testNullObject(evaluated);
            }
        }
    }

    @Test
    void testReturnStatements() {
        record TestCase(java.lang.String input, int expected) { }

        var tests = List.of(
                new TestCase("return 10;", 10),
                new TestCase("return 10; 9;", 10),
                new TestCase("return 2 * 5; 9;", 10),
                new TestCase("9; return 2 * 5; 9;", 10),
                new TestCase("if (10 > 1) { return 10; }", 10),
                new TestCase("""
                             if (10 > 1) {
                                 if (10 > 1) {
                                     return 10;
                                 }
                                 return 1;
                             }
                             """, 10),
                new TestCase("""
                             let f = fn(x) {
                                 return x;
                                 x + 10;
                             };
                             f(10);
                             """, 10),
                new TestCase("""
                             let f = fn(x) {
                                 let result = x + 10;
                                 return result;
                                 return 10;
                             };
                             f(10);
                             """, 20)
        );

        for (TestCase tt : tests) {
            var evaluated = testEval(tt.input);
            testIntegerObject(evaluated, tt.expected);
        }
    }

    @Test
    void testErrorHandling() {
        record TestCase(java.lang.String input, java.lang.String expectedMessage) { }

        var tests = List.of(
                new TestCase("5 + true;", "type mismatch: INTEGER + BOOLEAN"),
                new TestCase("5 + true; 5;", "type mismatch: INTEGER + BOOLEAN"),
                new TestCase("-true", "unknown operator: -BOOLEAN"),
                new TestCase("true + false;", "unknown operator: BOOLEAN + BOOLEAN"),
                new TestCase("5; true + false; 5", "unknown operator: BOOLEAN + BOOLEAN"),
                new TestCase("if (10 > 1) { true + false; }", "unknown operator: BOOLEAN + BOOLEAN"),
                new TestCase("""
                             if (10 > 1) {
                                 if (10 > 1) {
                                     return true + false;
                                 }
                                 return 1;
                             }
                             """, "unknown operator: BOOLEAN + BOOLEAN"),
                new TestCase("foobar", "identifier not found: foobar")
        );

        for (TestCase tt : tests) {
            var evaluated = testEval(tt.input);
            assertInstanceOf(Error.class, evaluated,
                             "no error object returned. got=" + evaluated.getClass());
            var error = (Error) evaluated;
            assertEquals(tt.expectedMessage, error.message().value(),
                         "wrong error message");
        }
    }

    @Test
    void testLetStatements() {
        record TestCase(java.lang.String input, int expected) { }

        var tests = List.of(
                new TestCase("let a = 5; a;", 5),
                new TestCase("let a = 5 * 5; a;", 25),
                new TestCase("let a = 5; let b = a; b;", 5),
                new TestCase("let a = 5; let b = a; let c = a + b + 5; c;", 15)
        );

        for (TestCase tt : tests) {
            testIntegerObject(testEval(tt.input), tt.expected);
        }
    }

    @Test
    void testFunctionObject() {
        var input = "fn(x) { x + 2; };";

        var evaluated = testEval(input);
        assertInstanceOf(Function.class, evaluated,
                         "object is not Function. got=" + evaluated.getClass());
        var fn = (Function) evaluated;

        assertEquals(1, fn.parameters().size(),
                     "function has wrong parameters");
        assertEquals("x", fn.parameters().getFirst().string(),
                     "parameter is not 'x'");
        assertEquals("(x + 2)", fn.body().string(),
                     "body is not (x + 2)");
    }

    @Test
    void testFunctionApplication() {
        record TestCase(java.lang.String input, int expected) { }

        var tests = List.of(
                new TestCase("let identity = fn(x) { x; }; identity(5);", 5),
                new TestCase("let identity = fn(x) { return x; }; identity(5);", 5),
                new TestCase("let double = fn(x) { x * 2; }; double(5);", 10),
                new TestCase("let add = fn(x, y) { x + y; }; add(5, 5);", 10),
                new TestCase("let add = fn(x, y) { x + y; }; add(5 + 5, add(5, 5));", 20),
                new TestCase("fn(x) { x; }(5)", 5)
        );

        for (TestCase tt : tests) {
            testIntegerObject(testEval(tt.input), tt.expected);
        }
    }

    @Test
    void testClosures() {
        var input = """
                    let newAdder = fn(x) {
                        fn(y) { x + y };
                    };
                    let addTwo = newAdder(2);
                    addTwo(2);
                    """;

        testIntegerObject(testEval(input), 4);
    }

    @Test
    void testStringLiteral() {
        var input = "\"Hello World!\"";

        var evaluated = testEval(input);
        assertInstanceOf(String.class, evaluated,
                         "object is not String. got=" + evaluated.getClass());
        var str = (String) evaluated;
        assertEquals("Hello World!", str.value(),
                     "String has wrong value");
    }

    @Test
    void testStringConcatenation() {
        var input = "\"Hello\" + \" \" + \"World!\"";

        var evaluated = testEval(input);
        assertInstanceOf(String.class, evaluated,
                         "object is not String. got=" + evaluated.getClass());
        var str = (String) evaluated;
        assertEquals("Hello World!", str.value(),
                     "String has wrong value");
    }

    @Test
    void testBuiltinFunctions() {
        record TestCase(java.lang.String input, java.lang.Object expected) { }

        var tests = List.of(
                // String tests
                new TestCase("""
                             len("")
                             """.trim(), 0),
                new TestCase("""
                             len("four")
                             """.trim(), 4),
                new TestCase("""
                             len("hello world")
                             """, 11),
                new TestCase("len(1)", "argument to `len` not supported, got INTEGER"),
                new TestCase("""
                             len("one", "two")
                             """, "wrong number of arguments. got=2, want=1"),

                // Array tests
                new TestCase("len([])", 0),
                new TestCase("len([1, 2, 3])", 3),
                new TestCase("first([1, 2, 3])", 1),
                new TestCase("first([])", null),
                new TestCase("first(1)", "argument to `first` must be ARRAY, got INTEGER"),
                new TestCase("last([1, 2, 3])", 3),
                new TestCase("last([])", null),
                new TestCase("last(1)", "argument to `last` must be ARRAY, got INTEGER"),
                new TestCase("rest([1, 2, 3])", List.of(2, 3)),
                new TestCase("rest([])", null),
                new TestCase("rest(1)", "argument to `rest` must be ARRAY, got INTEGER"),
                new TestCase("push([], 1)", List.of(1)),
                new TestCase("push(1, 1)", "argument to `push` must be ARRAY, got INTEGER")
        );

        for (TestCase tt : tests) {
            var evaluated = testEval(tt.input);

            switch (tt.expected) {
                case java.lang.Integer i -> testIntegerObject(evaluated, i);
                case java.lang.String s -> {
                    assertInstanceOf(Error.class, evaluated,
                                     "object is not Error. got=" + evaluated.getClass());
                    var error = (Error) evaluated;
                    assertEquals(s, error.message().value(),
                                 "wrong error message");
                }
                case List<?> l -> {
                    assertInstanceOf(Array.class, evaluated,
                                     "obj not Array. got=" + evaluated.getClass());
                    var array = (Array) evaluated;
                    assertEquals(l.size(), array.elements().size(),
                                 "wrong num of elements");
                    for (int i = 0; i < l.size(); i++) {
                        testIntegerObject(array.elements().get(i), (java.lang.Integer) l.get(i));
                    }
                }
                case null, default -> testNullObject(evaluated);
            }
        }
    }

    @Test
    void testArrayLiterals() {
        var input = "[1, 2 * 2, 3 + 3]";

        var evaluated = testEval(input);
        assertInstanceOf(Array.class, evaluated,
                         "object is not Array. got=" + evaluated.getClass());
        var result = (Array) evaluated;

        assertEquals(3, result.elements().size(),
                     "array has wrong num of elements");

        testIntegerObject(result.elements().get(0), 1);
        testIntegerObject(result.elements().get(1), 4);
        testIntegerObject(result.elements().get(2), 6);
    }

    @Test
    void testArrayIndexExpressions() {
        record TestCase(java.lang.String input, java.lang.Object expected) { }

        var tests = List.of(
                new TestCase("[1, 2, 3][0]", 1),
                new TestCase("[1, 2, 3][1]", 2),
                new TestCase("[1, 2, 3][2]", 3),
                new TestCase("let i = 0; [1][i];", 1),
                new TestCase("[1, 2, 3][1 + 1];", 3),
                new TestCase("let myArray = [1, 2, 3]; myArray[2];", 3),
                new TestCase("[1, 2, 3][3]", null),
                new TestCase("[1, 2, 3][-1]", null)
        );

        for (TestCase tt : tests) {
            var evaluated = testEval(tt.input);
            if (tt.expected instanceof java.lang.Integer i) {
                testIntegerObject(evaluated, i);
            } else {
                testNullObject(evaluated);
            }
        }
    }

    @Test
    void testHashLiterals() {
        var input = """
                    let two = "two";
                    {
                        "one": 10 - 9,
                        two: 1 + 1,
                        "thr" + "ee": 6 / 2,
                        4: 4,
                        true: 5,
                        false: 6
                    }""";

        var evaluated = testEval(input);
        assertInstanceOf(Hash.class, evaluated,
                         "Eval didn't return Hash. got=" + evaluated.getClass());
        var result = (Hash) evaluated;

        var expected = new java.util.HashMap<HashKey, java.lang.Integer>();
        expected.put(new String("one").hashKey(), 1);
        expected.put(new String("two").hashKey(), 2);
        expected.put(new String("three").hashKey(), 3);
        expected.put(new Integer(4).hashKey(), 4);
        expected.put(TRUE.hashKey(), 5);
        expected.put(FALSE.hashKey(), 6);

        assertEquals(expected.size(), result.pairs().size(),
                     "Hash has wrong num of pairs");

        for (var entry : expected.entrySet()) {
            var pair = result.pairs().get(entry.getKey());
            assertNotNull(pair, "no pair for given key in Pairs");
            testIntegerObject(pair.value(), entry.getValue());
        }
    }

    @Test
    void testEnclosingEnvironments() {
        var input = """
                    let first = 10;
                    let second = 10;
                    let third = 10;
                                
                    let ourFunction = fn(first) {
                        let second = 20;
                        first + second + third;
                    };
                                
                    ourFunction(20) + first + second;""";

        testIntegerObject(testEval(input), 70);
    }

    @Test
    void testHashIndexExpressions() {
        record TestCase(java.lang.String input, java.lang.Object expected) { }

        var tests = List.of(
                new TestCase("""
                             {"foo": 5}["foo"]
                             """, 5),
                new TestCase("""
                             {"foo": 5}["bar"]
                             """, null),
                new TestCase("""
                             let key = "foo";
                             {"foo": 5}[key]
                             """, 5),
                new TestCase("""
                             {}["foo"]
                             """, null),
                new TestCase("""
                             {5: 5}[5]
                             """, 5),
                new TestCase("""
                             {true: 5}[true]
                             """, 5),
                new TestCase("""
                             {false: 5}[false]
                             """, 5)
        );

        for (TestCase tt : tests) {
            var evaluated = testEval(tt.input);
            if (tt.expected instanceof java.lang.Integer i) {
                testIntegerObject(evaluated, i);
            } else {
                testNullObject(evaluated);
            }
        }
    }

    private org.monkey.object.Object testEval(java.lang.String input) {
        var l = new Lexer(input);
        var p = new Parser(l);
        var program = p.parseProgram();
        var env = new Environment();

        return new Evaluator().eval(program, env);
    }

    private void testIntegerObject(org.monkey.object.Object obj, int expected) {
        assertInstanceOf(Integer.class, obj, "object is not Integer. got=" + obj.getClass());
        var result = (Integer) obj;
        assertEquals(expected, result.value(), "object has wrong value");
    }

    private void testBooleanObject(org.monkey.object.Object obj, boolean expected) {
        assertInstanceOf(Boolean.class, obj, "object is not Boolean. got=" + obj.getClass());
        var result = (Boolean) obj;
        assertEquals(expected, result.value(), "object has wrong value");
    }

    private void testNullObject(org.monkey.object.Object obj) {
        assertEquals(obj, new Null(), "object is not NULL");
    }
}