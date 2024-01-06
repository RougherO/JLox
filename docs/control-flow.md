## Control Flow

Control flow structures in JLox determines the order in which statements are executed based on certain conditions. This section covers various control flow mechanisms available in the language.

### Conditional Statements (if-else)

Conditional statements are used to execute code blocks conditionally based on boolean expressions.

#### if Statement

The `if` statement executes a block of code if the specified condition is true.

```javascript
if (condition) {
    // code to execute if the condition is true
}
```
Blocks can be omitted in which case the statement immediatly after if will be executed.
```javascript
if (1 + 2 == 3)
    print(true);  // prints true
```
#### if - else-if - else statement

`if` statements can have optional `else` and `else if` clauses to execute when `if` conditions evaluate to falsy values.

```javascript
if (nil) {
    print("Its not nil");
} else if (false) {
    print("Its not false");
} else if (0) {
    print("Its not zero");
} else {
    print("Something truthy!");
}

// prints Something truthy!
```
---
### Loops
Loops are used to execute a block of code repeatedly until a specified condition is met. JLox has two kinds of loops, (for now at least):

#### while loops
The `while` loop executes a block of code repeatedly as long as the specified condition is true/truthy.
```
while (condition) {
    // code to execute
}
```
The `for` loop is also similar to `while` loop that it evaluates till a condition remains true. Additionally, it provides syntactic sugar to provide initialisation before loop starts and any increment expression after each loop iteration
```
for (initialiser ; condition ; increment) {

}
```

Like `if - else` for loops and while loops also allow omitting of block statements.

Example `for` and `while` loops:
```javascript
for (let counter = 0; counter < 100; counter = counter + 1) {
    print(counter);               // prints all values from 0 to 99 
}

while (true) {
    print("I am always true");    // prints "I am always true" infinite times till killed
}
```

---

### Control-Flow Keywords

#### Break

#### Continue

#### Labels

<a href="./syntax.md" style="color: white;"> <button style="float: left; background: none; min-height: 30px; border-radius: 5px; border-color: white; padding: 10px 15px"><i>< Syntax </i></button></a>

<!-- <a href="./blank.md" style="color: white"><button style="float: right; background: none; min-height: 30px; border-radius: 5px; border-color: white; padding: 10px 15px"><i> Blank > </i></button></a> -->