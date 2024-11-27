package org.monkey.repl;

import org.monkey.ast.Program;
import org.monkey.evaluator.Evaluator;
import org.monkey.lexer.Lexer;
import org.monkey.object.Environment;
import org.monkey.object.Object;
import org.monkey.parser.Parser;

import java.util.List;
import java.util.Scanner;

public class Repl {

    private static final String PROMPT = ">> ";

    private static final String MONKEY_FACE = """
                                                          __,__
                                                 .--.  .-"     "-.  .--.
                                                / .. \\/  .-. .-.  \\/ .. \\
                                               | |  '|  /   Y   \\  |'  | |
                                               | \\   \\  \\ 0 | 0 /  /   / |
                                                \\ '- ,\\.-""\"""\""-./, -' /
                                                 ''-' /_   ^ ^   _\\ '-''
                                                     |  \\._   _./  |
                                                     \\   \\ '~' /   /
                                                      '._ '-=-' _.'
                                                         '-----'
                                              """;

    public void start() {
        var scanner = new Scanner(System.in);
        var env = new Environment();

        while (true) {
            System.out.printf(PROMPT);
            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                return;
            }
            var lexer = new Lexer(line);
            var parser = new Parser(lexer);

            var program = parser.parseProgram();
            if (!parser.errors().isEmpty()) {
                printParseErrors(parser.errors());
                continue;
            }

            var evaluator = new Evaluator();
            var evaluated = evaluator.eval(program, env);
            if (evaluated != null) {
                System.out.println(evaluated.inspect());
            }
        }
    }

    private void printParseErrors(List<String> errors) {
        System.out.println(MONKEY_FACE);
        System.out.println("Woops! We ran into some monkey business here!");
        System.out.println(" parser errors:");
        for (String errorMsg : errors) {
            System.out.print("\t" + errorMsg + "\n");
        }
    }


}
