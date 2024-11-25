# syntax

Expressed as a list of features, Monkey has the following:

- C-like syntax
- variable bindings
- integers and booleans
- arithmetic expressions
- built-in functions
- first-class and higher-order functions
- closures
- a string data structure
- an array data structure
- a hash data structure
  We’re going to take a detailed look at and implement each of these features in the rest of this book. But for now,
  let’s see what Monkey looks like.

Here is how we bind values to names in Monkey:

```text
let age = 1;
let name = "Monkey";
let result = 10 * (20 / 2);
```

Besides integers, booleans and strings, the Monkey interpreter we’re going to build will also support arrays and hashes.
Here’s what binding an array of integers to a name looks like:

```text
let myArray = [1, 2, 3, 4, 5];
```

And here is a hash, where values are associated with keys:

```text
let thorsten = {"name": "Thorsten", "age": 28};
```

Accessing the elements in arrays and hashes is done with index expressions:

```text
myArray[0]       // => 1
thorsten["name"] // => "Thorsten"
```

The let statements can also be used to bind functions to names. Here’s a small function that adds two numbers:

```text
let add = fn(a, b) { return a + b; };
```

But Monkey not only supports return statements. Implicit return values are also possible, which means we can leave out
the return if we want to:

```text
let add = fn(a, b) { a + b; };
```

And calling a function is as easy as you’d expect:

```text
add(1, 2);
```

A more complex function, such as a fibonacci function that returns the Nth Fibonacci number, might look like this:

```text
let fibonacci = fn(x) {
    if (x == 0) {
        0
    } else {
        if (x == 1) {
            1
        } else {
            fibonacci(x - 1) + fibonacci(x - 2);
        }
    }
};
```

Note the recursive calls to fibonacci itself!

Monkey also supports a special type of functions, called higher order functions. These are functions that take other
functions as arguments. Here is an example:

```text
let twice = fn(f, x) {
    return f(f(x));
};

let addTwo = fn(x) {
    return x + 2;
};

twice(addTwo, 2); // => 6
```

Here twice takes two arguments: another function called addTwo and the integer 2. It calls addTwo two times with first 2
as argument and then with the return value of the first call. The last line produces 6.

Yes, we can use functions as arguments in function calls. Functions in Monkey are just values, like integers or strings.
That feature is called “first class functions”.

The interpreter we’re going to build in this book will implement all these features. It will tokenize and parse Monkey
source code in a REPL, building up an internal representation of the code called abstract syntax tree and then evaluate
this tree. It will have a few major parts:

- the lexer
- the parser
- the Abstract Syntax Tree (AST)
- the internal object system
- the evaluator

We’re going to build these parts in exactly this order, from the bottom up. Or better put: starting with the source
code and ending with the output. The drawback of this approach is that it won’t produce a simple “Hello World” after
the first chapter. The advantage is that it’s easier to understand how all the pieces fit together and how the data
flows through the program.