## Syntax

This section outlines the syntax of JLox.

### Comments

Comments in JLox are single lined with `//` as the starting token. Anything written after `//` and the next newline is part of a comment and is totally ignored by JLox.

Eg.
```javascript
// This is a sample comment in JLox
```

Multi-line comments are still on the feature list and will be implemented once the base language is completed and if time permits.

### Statements

In JLox each statement is comprises of either variable declaration, block statements or simply expressions ending with a semi-colon.

Here's how statements in JLox look like
```javascript
// This is a single statement which defines a new variable myVar
let myVar = 1;

// This is a Block statement / Block -> braces enclosing multiple statements
{
    let myNewVar = 2;
    myNewVar = myVar + 1;
    print(myNewVar);
    // ... so on
}
```

Every statement is marked by a `';'` at the end (except block statement). Missing `';'` raises SyntaxError.

Block statements are special as each block introduces a new scope in the current context. The `myNewVar` variable in the above example was created in a new scope inside the block, thus, as soon as the program exits the current block/scope `myNewVar` is no longer available. However variables outside of current scope ( outer scope ) are available in the inner scopes. Like `myVar` in the above example.

P.S.- empty statements are not allowed ,i.e., a `;` without any preceding expression is considered wrong syntax. A valid expression is expected always.

### Variables

Variables are used to store data values. They must be declared before they are used and follow a specific syntax.

#### Variable Declaration

Variables are declared using the `let` keyword followed by the variable name and an optional initial value.

```javascript
// Initialising myVar with 1
let myVar = 1;

// Default initialised with "nil"
let myNewVar;
```
Note that we do not provide any type information while defining a new variable. Type is determined at runtime as JLox is dynamically typed. However, internally JLox does use types:
- Number (no distinction between ints and floats)
- String
- Boolean ( true / false )
- `nil` (same as `None` in python)

Uninitalised variables are default initialised with `nil` value, however, they cannot be used in an expression if not initialised. If uninitialised variables are used in an expression it raises runtime error.

```javascript
=> let myVar; print(myVar); // Produces error
```
```
[line 1] Error at 'myVar' : Unassigned variable 'myVar' is used.
```
### Expressions

Expressions are combinations of literals, variables, operators, and function calls that produce a value.

```python
// Arithmetic expression
result = 10 * (5 + 3);

// Boolean expression
isTrue = (value1 == value2) and (value3 != value4);
```

### Operators

JLox has quite some operators with new ones on the way. Currently supported operators are:

| Operators    | Name                            |
|:------------:|:--------------------------------|
|    =         | Assignment                      |
|    ?  :      | Conditional                     | 
|    or        | Logical OR                      |
|    and       | Logical AND                     |
|    == , !=   | Equality                        |
|    -, +      | Binary plus and Binary minus    |
| >, <, >=, <= | Comparison                      |
|    *, /      | mulitply, divide                |
|    -, !      | Unary minus and Unary not       |
|    ++, --    | Postfix increment and decrement |
|    ++, --    | Prefix increment and decrement  |
> All operators listed here are in decreasing precedence order, from lowest to highest.

All operators follow standard operator operations:

#### Assignment 
Assignment operator is used to assign values to newly defined variables or to assign values to old variables.

```javascript
let myVar = 1;

myVar = 2;
```
Assigning values to previously defined variables is an expression so they return values which can be used as a part of another expression.

```javascript
let myVar = 1;
let myNewVar = myVar = 2; // myVar = 2 returns 2 which is assigned to myNewVar

print(myVar);             // prints 2
print(myNewVar);          // prints 2
```

#### Conditional
Conditional operator is used to evaluate expressions depending on truth value of an expression.

Syntax:
```
<expression> ? <expression if true> : <expression if false>
```
Eg.
```javascript
print(1 + 2 == 3 ? "That's easy!" : "It's quite hard!");
```
_**All non-zero values and strings and non-nil values are true**_

```javascript
print(nil ? "Its not nil" : "Its nil");
```
#### Logical operators
Logical OR and Logical AND works just like other languages `or` returns when either one is truthy value, `and` returns when both are true. The important point is what's returned. `or` returns the first truthy value it finds while `and` returns the first falsy value it finds. Something to observe is that as whenever `or` and `and` find their respective truthy or falsy value they short circuit and ignore any trailing values.

```javascript
print(1 and 2 and 0 and 3 and 4);  // prints 0
print(0 or nil or "yes" or false)  // prints "yes"
```

In both cases if no truthy or falsy value is found it returns the last value in the expression
```javascript
print(1 and 2 and 3);      // prints 3
print(0 or false or nil)   // prints nil
```

#### Equality operators
Equality operators return `true` or `false` depending on whether the comparison returns true or false.

```javascript
print("Happy" == "Happy"); // prints true
```
P.S.- The type of values being compared should be same, i.e., a string should be compared with string and a number type should be compared with number

#### Binary operators
Binary operators work like usual binary operator operations

```javascript
let myVar = 1;

print(myVar * 2);   // prints 2
print(myVar / 2);   // prints 0.5
print(myVar + 2);   // prints 3
print(myVar - 2);   // prints -1
```

The `+` operator, however, is overloaded to allow string concatenation.
```javascript
print("Hello" + "World"); // prints HelloWorld
```
Note that for string concatenation either operand needs to be string. For the rest binary operators both the operands should be of number type.

#### Unary operators
- Unary negate `!` - negates the truthy value of the expression
```javascript
print(!nil);  // prints true
```
- Unary minus `-` - return the negative value of the expression. The expression needs to be of number type otherwise it raises runtime error
```javascript
print(-nil);    // Error

let myVar = 1;
print(-myVar); // prints -1
```

### Postfix operators
Postfix operators include `++` and `--`. When a statement contains one of the postfix operators the corresponding variable's original value is used throughout the statement execution. Only after the statement is terminated that the increment/decrement is performed.

```javascript
let myVar = 1;
print(myVar++ + 28 + myVar);  // uses myVar = 1 for both times; prints 30
                              // Once semi - colon is reached end of 
                              // statement is reached and the postfix increment occurs
print(myVar);                 // prints 2
```
### Prefix operators
Prefix operators also include `++` and `--`. Their only difference from their prefix alternative is that any prefix expression causes the corresponding variable to have immediate effects: increment/decrement.
```javascript
let myVar = 1;
print(myVar);     // prints 2
print(++myVar);   // prints 2
print(myVar);     // prints 2
```

> Note: Postfix and Prefix operators can only be applied on lvalues(identifiers) and chaining of postfix and prefix operators is not allowed, eg. `myVar++++` or `(++myVar++)++`

<a href="./getting-started.md" style="color: white"><button style="float: left; background: none; min-height: 30px; border-radius: 5px; border-color: white; padding: 10px 15px"><i> < Getting-Started </i></button></a>

<a href="./control-flow.md" style="color: white"><button style="float: right; background: none; min-height: 30px; border-radius: 5px; border-color: white; padding: 10px 15px"><i> Control flow > </i></button></a>