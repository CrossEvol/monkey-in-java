package org.monkey.evaluator;

import org.monkey.ast.*;
import org.monkey.object.Array;
import org.monkey.object.Boolean;
import org.monkey.object.BuiltIn;
import org.monkey.object.Environment;
import org.monkey.object.Error;
import org.monkey.object.Function;
import org.monkey.object.Hash;
import org.monkey.object.HashKey;
import org.monkey.object.HashPair;
import org.monkey.object.Hashable;
import org.monkey.object.Integer;
import org.monkey.object.Null;
import org.monkey.object.Object;
import org.monkey.object.ObjectType;
import org.monkey.object.ReturnValue;
import org.monkey.object.String;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Evaluator {

    private static final Object NULL = new Null();
    private static final Boolean TRUE = new Boolean(true);
    private static final Boolean FALSE = new Boolean(false);

    private static final Map<java.lang.String, BuiltIn> builtIns = Map.ofEntries(
            Map.entry("len", new BuiltIn(args -> {
                if (args.length != 1) {
                    return newError("wrong number of arguments. got=%d, want=1", args.length);
                }
                var arg = args[0];
                if (arg instanceof Array arr) {
                    return new Integer(arr.elements().size());
                } else if (arg instanceof String str) {
                    return new Integer(str.value().length());
                } else {
                    return newError("argument to `len` not supported, got %s", arg.type().value());
                }
            })),
            Map.entry("puts", new BuiltIn(args -> {
                for (Object arg : args) {
                    System.out.println(arg.inspect());
                }
                return NULL;
            })),
            Map.entry("first", new BuiltIn(args -> {
                if (args.length != 1) {
                    return newError("wrong number of arguments. got=%d, want=1", args.length);
                }
                var arg = args[0];
                if (arg.type() != ObjectType.ARRAY_OBJ) {
                    return newError("argument to `first` must be ARRAY, got %s", arg.type().value());
                }
                if (arg instanceof Array arr) {
                    if (arr.elements().isEmpty()) { return NULL; }
                    return arr.elements().getFirst();
                }

                return NULL;
            })),
            Map.entry("last", new BuiltIn(args -> {
                if (args.length != 1) {
                    return newError("wrong number of arguments. got=%d, want=1", args.length);
                }
                var arg = args[0];
                if (arg.type() != ObjectType.ARRAY_OBJ) {
                    return newError("argument to `last` must be ARRAY, got %s", arg.type().value());
                }
                if (arg instanceof Array arr) {
                    if (arr.elements().isEmpty()) { return NULL; }
                    return arr.elements().getLast();
                }

                return NULL;
            })),
            Map.entry("rest", new BuiltIn(args -> {
                if (args.length != 1) {
                    return newError("wrong number of arguments. got=%d, want=1", args.length);
                }
                var arg = args[0];
                if (arg.type() != ObjectType.ARRAY_OBJ) {
                    return newError("argument to `rest` must be ARRAY, got %s", arg.type().value());
                }
                if (arg instanceof Array arr) {
                    if (arr.elements().isEmpty()) { return NULL; }
                    return new Array(arr.elements().subList(1, arr.elements().size()));
                }

                return NULL;
            })),
            Map.entry("push", new BuiltIn(args -> {
                if (args.length != 2) {
                    return newError("wrong number of arguments. got=%d, want=2", args.length);
                }
                var arg = args[0];
                if (arg.type() != ObjectType.ARRAY_OBJ) {
                    return newError("argument to `push` must be ARRAY, got %s", arg.type().value());
                }
                if (arg instanceof Array arr) {
                    var elements = new ArrayList<>(arr.elements());
                    elements.add(args[1]);
                    return new Array(elements);
                }

                return arg;
            }))
    );

    public Object eval(Node n, Environment env) {
        return switch (n) {
            case Program node -> evalProgram(node, env);
            case BlockStatement node -> evalBlockStatement(node, env);
            case ExpressionStatement node -> this.eval(node.expression(), env);
            case ReturnStatement node -> {
                var value = this.eval(node.returnValue(), env);
                if (isError(value)) {
                    yield value;
                }
                yield new ReturnValue(value);
            }
            case LetStatement node -> {
                var value = this.eval(node.value(), env);
                if (isError(value)) {
                    yield value;
                }
                env.set(node.name().value(), value);
                yield null;
            }
            case IntegerLiteral node -> new Integer(node.value());
            case StringLiteral node -> new String(node.value());
            case BooleanLiteral node -> nativeBoolToBooleanObject(node.value());
            case PrefixExpression node -> {
                var right = this.eval(node.right(), env);
                if (isError(right)) {
                    yield right;
                }
                yield evalPrefixExpression(node.operator(), right);
            }
            case InfixExpression node -> {
                var left = this.eval(node.left(), env);
                if (isError(left)) {
                    yield left;
                }
                var right = this.eval(node.right(), env);
                if (isError(right)) {
                    yield right;
                }
                yield evalInfixExpression(node.operator(), left, right);
            }
            case IfExpression node -> evalIfExpression(node, env);
            case Identifier node -> evalIdentifier(node, env);
            case FunctionLiteral node -> new Function(node.parameters(), node.body(), env);
            case CallExpression node -> {
                var function = this.eval(node.function(), env);
                if (isError(function)) {
                    yield function;
                }

                var args = this.evalExpressions(node.arguments(), env);
                if (args.size() == 1 && isError(args.getFirst())) {
                    yield args.getFirst();
                }

                yield applyFunction(function, args);
            }
            case ArrayLiteral node -> {
                var elements = this.evalExpressions(node.elements(), env);
                if (elements.size() == 1 && isError(elements.getFirst())) {
                    yield elements.getFirst();
                }
                yield new Array(elements);
            }
            case IndexExpression node -> {
                var left = this.eval(node.left(), env);
                if (isError(left)) {
                    yield left;
                }
                var index = this.eval(node.index(), env);
                if (isError(index)) {
                    yield index;
                }
                yield evalIndexExpression(left, index);
            }
            case HashLiteral node -> this.evalHashLiteral(node, env);
            default -> throw new IllegalStateException("Unexpected value: " + n.getClass());
        };
    }

    private static Error newError(java.lang.String format, java.lang.Object... args) {
        return new Error(new String(java.lang.String.format(format, args)));
    }

    private Object evalProgram(Program program, Environment env) {
        Object result = null;

        for (Statement statement : program.statements) {
            result = this.eval(statement, env);
            if (result instanceof ReturnValue returnValue) {
                return returnValue.value();
            } else if (result instanceof Error error) {
                return error;
            }
        }

        return result;
    }

    private Object evalBlockStatement(BlockStatement block, Environment env) {
        Object result = null;

        for (Statement statement : block.statements()) {
            result = this.eval(statement, env);

            if (result != null) {
                if (result.type() == ObjectType.RETURN_VALUE_OBJ || result.type() == ObjectType.ERROR_OBJ) {
                    return result;
                }
            }
        }

        return result;
    }

    private Boolean nativeBoolToBooleanObject(boolean input) {
        return input
                ? TRUE
                : FALSE;
    }

    private Object evalPrefixExpression(java.lang.String operator, Object right) {
        return switch (operator) {
            case "!" -> this.evalBangOperatorExpression(right);
            case "-" -> this.evalMinusPrefixOperatorExpression(right);
            default -> newError("unknown operator: %s%s", operator, right.type());
        };
    }

    private Object evalInfixExpression(java.lang.String operator, Object left, Object right) {
        if (left.type() == ObjectType.INTEGER_OBJ && right.type() == ObjectType.INTEGER_OBJ) {
            return this.evalIntegerInfixExpression(operator, left, right);
        }
        if (left.type() == ObjectType.STRING_OBJ && right.type() == ObjectType.STRING_OBJ) {
            return this.evalStringInfixExpression(operator, left, right);
        }
        if (operator.equals("==")) {
            return nativeBoolToBooleanObject(left.equals(right));
        }
        if (operator.equals("!=")) {
            return nativeBoolToBooleanObject(!left.equals(right));
        }
        if (left.type() != right.type()) {
            return newError("type mismatch: %s %s %s", left.type().value(), operator, right.type().value());
        }
        return newError("unknown operator: %s %s %s", left.type().value(), operator, right.type().value());
    }

    private Object evalBangOperatorExpression(Object right) {
        if (right == TRUE) {
            return FALSE;
        }
        if (right == FALSE) {
            return TRUE;
        }
        if (right == NULL) {
            return TRUE;
        }
        return FALSE;
    }

    private Object evalMinusPrefixOperatorExpression(Object right) {
        if (right.type() != ObjectType.INTEGER_OBJ) {
            return newError("unknown operator: -%s", right.type().value());
        }
        return new Integer(-((Integer) right).value());
    }

    private Object evalIntegerInfixExpression(java.lang.String operator, Object left, Object right) {
        var leftValue = ((Integer) left).value();
        var rightValue = ((Integer) right).value();

        return switch (operator) {
            case "+" -> new Integer(leftValue + rightValue);
            case "-" -> new Integer(leftValue - rightValue);
            case "*" -> new Integer(leftValue * rightValue);
            case "/" -> new Integer(leftValue / rightValue);
            case "<" -> nativeBoolToBooleanObject(leftValue < rightValue);
            case ">" -> nativeBoolToBooleanObject(leftValue > rightValue);
            case "==" -> nativeBoolToBooleanObject(Objects.equals(leftValue, rightValue));
            case "!=" -> nativeBoolToBooleanObject(!Objects.equals(leftValue, rightValue));
            default -> newError("unknown operator: %s %s %s",
                                left.type(), operator, right.type());
        };
    }

    private Object evalStringInfixExpression(java.lang.String operator, Object left, Object right) {
        if (!Objects.equals(operator, "+")) {
            return newError("unknown operator: %s %s %s",
                            left.type().value(), operator, right.type().value());
        }
        var leftValue = ((String) left).value();
        var rightValue = ((String) right).value();
        return new String(leftValue + rightValue);
    }

    private Object evalIfExpression(IfExpression ie, Environment env) {
        var condition = this.eval(ie.condition(), env);
        if (isError(condition)) {
            return condition;
        }

        if (isTruthy(condition)) {
            return this.eval(ie.consequence(), env);
        } else if (ie.alternative() != null) {
            return this.eval(ie.alternative(), env);
        } else {
            return NULL;
        }
    }

    private Object evalIdentifier(Identifier node, Environment env) {
        var tuple = env.get(node.value());
        if (tuple.ok()) {
            return tuple.value();
        }

        var builtIn = builtIns.get(node.value());
        if (builtIn != null) {
            return builtIn;
        }

        return newError("identifier not found: " + node.value());
    }

    private boolean isTruthy(Object obj) {
        if (obj.equals(NULL)) {
            return false;
        } else if (obj.equals(TRUE)) {
            return true;
        } else if (obj.equals(FALSE)) {
            return false;
        } else {
            return true;
        }

    }

    private boolean isError(Object obj) {
        if (obj != null) {
            return obj.type() == ObjectType.ERROR_OBJ;
        }
        return false;
    }

    private List<Object> evalExpressions(List<Expression> exps, Environment env) {
        var result = new ArrayList<Object>();

        for (Expression exp : exps) {
            var evaluated = this.eval(exp, env);
            if (isError(evaluated)) {
                return List.of(evaluated);
            }
            result.add(evaluated);
        }

        return result;
    }

    private Object applyFunction(Object fn, List<Object> args) {
        return switch (fn) {
            case Function function -> {
                var extendedEnv = extendFunctionEnv(function, args);
                var evaluated = this.eval(function.body(), extendedEnv);
                yield unwrapReturnValue(evaluated);

            }
            case BuiltIn builtIn -> builtIn.fn().call(args.toArray(new Object[0]));
            default -> newError("not a function: %s", fn.type());
        };
    }

    private Environment extendFunctionEnv(Function fn, List<Object> args) {
        var enclosedEnv = new Environment(fn.env());
        var parameters = fn.parameters();
        for (int i = 0; i < parameters.size(); i++) {
            enclosedEnv.set(parameters.get(i).value(), args.get(i));
        }
        return enclosedEnv;
    }

    private Object unwrapReturnValue(Object obj) {
        if (obj instanceof ReturnValue returnValue) {
            return returnValue.value();
        }
        return obj;
    }

    private Object evalIndexExpression(Object left, Object index) {
        if (left.type() == ObjectType.ARRAY_OBJ && index.type() == ObjectType.INTEGER_OBJ) {
            return this.evalArrayIndexExpression(left, index);
        }
        if (left.type() == ObjectType.HASH_OBJ) {
            return this.evalHashIndexExpression(left, index);
        }
        return newError("index operator not supported: %s", left.type().value());
    }

    private Object evalArrayIndexExpression(Object array, Object index) {
        var arr = (Array) array;
        var idx = ((Integer) index).value();
        if (idx < 0 || idx > arr.elements().size() - 1) {
            return NULL;
        }
        return arr.elements().get(idx);
    }

    private Object evalHashLiteral(HashLiteral node, Environment env) {
        var pairs = new HashMap<HashKey, HashPair>();

        for (Map.Entry<Expression, Expression> entry : node.pairs().entrySet()) {
            var key = this.eval(entry.getKey(), env);
            if (isError(key)) {
                return key;
            }

            if (!(key instanceof Hashable hashKey)) {
                return newError("unusable as hash key: %s", key.type().value());
            }

            var value = this.eval(entry.getValue(), env);
            if (isError(value)) {
                return value;
            }

            pairs.put(hashKey.hashKey(), new HashPair(key, value));

        }

        return new Hash(pairs);
    }

    private Object evalHashIndexExpression(Object hash, Object index) {
        var hashObject = (Hash) hash;
        if (!(index instanceof Hashable key)) { return newError("unusable as hash key: %s", index.type().value()); }
        var pair = hashObject.pairs().get(key.hashKey());
        if (pair == null) {
            return NULL;
        }
        return pair.value();

    }
}
