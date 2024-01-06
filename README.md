# JLox

Welcome to JLox, a language crafted for learning and educational exploration. This is an attempt at implementing Robert Nystrom's Lox in Java, how its done in the first part of the book [craftinginterpreters](https://www.craftinginterpreters.com/) by Robert Nystrom, combined with some of my own features.

## Overview

JLox is a passionate project created primarily for educational purposes, designed primarily for my own sake of learning more about interpreters and compilers. It's important to note:

1. **Designed for Learning**: This language is intentionally built for learning and educational purposes. So it's not optimized for performance given that the implementation relies on JVM's own GC and also moderate usage of instanceof which is not **cheap**, it serves as a valuable tool for understanding language concepts. It can be used as a reference while going through the book.

2. **Early Stage Prototype**: Currently, JLox exists in its early prototype stage. It's a work in progress and I am investing almost 14 - 16 hours weekly on it since the last month. Judging by the current pace, it would take another month to complete the java version of this language.

3. **Inspired by "Crafting Interpreters"**: The language is heavily influenced by Robert Nystrom's book "Crafting Interpreters". While following the book's guidance, it incorporates original elements and some of my own adaptations to add a unique flavor to the language.

4. **Future Development Roadmap**: The language is undergoing active development. The immediate roadmap involves completing the initial implementation in Java, based on the book's first part. Following this phase, I plan to reimplement the language in C, based on the book's second part but that's another beast which would probably take another 4 - 5 months to tame. This reimplementation aims to enhance performance, making the language more suitable for real-world applications.

## Features

- Jlox is dynamically typed language, no type definition is required. New variables are declared using the `let` keyword.
```javascript
let myVar = 2;
```
- C-like syntax: Here's an example `for` loop
```javascript
for (let i = 0; i < 10; i++) {
    print("Programming languages are cool!" + "Programmers are even cooler!");
}
```
- Garbage collected (JVM)
- Need semi-colon to end statements. (Yes I am that guy who favours semi-colons)
- ..._more on the way._(from Chapter 10 `functions`)

## Installation

For now, until a docker image or a CLI app is made available, to test and poke around the language run it by downloading the jar file and running it using JVM.

```bash
$ java -jar JLox-0.0.1.jar [filename]
```
The filename is any text file which contains valid JLox code that can be interpreted.

If no filename is provided then a standard REPL interface opens up which accepts any valid jlox statements.

## Documentation
[Docs](./docs/intro.md) are available here and are currently incomplete and undergoing development. Feel free to contribute towards improving the documentation.

## Upcoming
- Functions
- Closures
- Classes
- Inheritance
- Multi-line comments
- break and continue statements with loop labels
- Testing / unit-tests
- _feel free to suggest_
## Contributing

At the moment contributions are not accepted, since I feel the source code documentation could still use some work, although I have tried to provide as much relavant comments as possible to make the source code more readable. However once the language is completely implemented with better code documentation it will be open for all kinds of feedbacks, contributions, suggestions etc.

Reporting of issues is always open.

Once testing is implemented PR's for additional test cases are also welcome.

## Acknowledgements
1. [craftinginterpreters book](https://www.craftinginterpreters.com/)
2. Robert Nystrom - <a href="https://twitter.com/intent/user?screen_name=munificentbob"><svg xmlns="http://www.w3.org/2000/svg" height="16" width="16" viewBox="0 0 512 512"><!--!Font Awesome Free 6.5.1 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2024 Fonticons, Inc.--><path d="M389.2 48h70.6L305.6 224.2 487 464H345L233.7 318.6 106.5 464H35.8L200.7 275.5 26.8 48H172.4L272.9 180.9 389.2 48zM364.4 421.8h39.1L151.1 88h-42L364.4 421.8z"/></svg></a> <a href = "https://github.com/munificent/"><svg xmlns="http://www.w3.org/2000/svg" height="16" width="15.5" viewBox="0 0 496 512"><!--!Font Awesome Free 6.5.1 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2024 Fonticons, Inc.--><path d="M165.9 397.4c0 2-2.3 3.6-5.2 3.6-3.3 .3-5.6-1.3-5.6-3.6 0-2 2.3-3.6 5.2-3.6 3-.3 5.6 1.3 5.6 3.6zm-31.1-4.5c-.7 2 1.3 4.3 4.3 4.9 2.6 1 5.6 0 6.2-2s-1.3-4.3-4.3-5.2c-2.6-.7-5.5 .3-6.2 2.3zm44.2-1.7c-2.9 .7-4.9 2.6-4.6 4.9 .3 2 2.9 3.3 5.9 2.6 2.9-.7 4.9-2.6 4.6-4.6-.3-1.9-3-3.2-5.9-2.9zM244.8 8C106.1 8 0 113.3 0 252c0 110.9 69.8 205.8 169.5 239.2 12.8 2.3 17.3-5.6 17.3-12.1 0-6.2-.3-40.4-.3-61.4 0 0-70 15-84.7-29.8 0 0-11.4-29.1-27.8-36.6 0 0-22.9-15.7 1.6-15.4 0 0 24.9 2 38.6 25.8 21.9 38.6 58.6 27.5 72.9 20.9 2.3-16 8.8-27.1 16-33.7-55.9-6.2-112.3-14.3-112.3-110.5 0-27.5 7.6-41.3 23.6-58.9-2.6-6.5-11.1-33.3 2.6-67.9 20.9-6.5 69 27 69 27 20-5.6 41.5-8.5 62.8-8.5s42.8 2.9 62.8 8.5c0 0 48.1-33.6 69-27 13.7 34.7 5.2 61.4 2.6 67.9 16 17.7 25.8 31.5 25.8 58.9 0 96.5-58.9 104.2-114.8 110.5 9.2 7.9 17 22.9 17 46.4 0 33.7-.3 75.4-.3 83.6 0 6.5 4.6 14.4 17.3 12.1C428.2 457.8 496 362.9 496 252 496 113.3 383.5 8 244.8 8zM97.2 352.9c-1.3 1-1 3.3 .7 5.2 1.6 1.6 3.9 2.3 5.2 1 1.3-1 1-3.3-.7-5.2-1.6-1.6-3.9-2.3-5.2-1zm-10.8-8.1c-.7 1.3 .3 2.9 2.3 3.9 1.6 1 3.6 .7 4.3-.7 .7-1.3-.3-2.9-2.3-3.9-2-.6-3.6-.3-4.3 .7zm32.4 35.6c-1.6 1.3-1 4.3 1.3 6.2 2.3 2.3 5.2 2.6 6.5 1 1.3-1.3 .7-4.3-1.3-6.2-2.2-2.3-5.2-2.6-6.5-1zm-11.4-14.7c-1.6 1-1.6 3.6 0 5.9 1.6 2.3 4.3 3.3 5.6 2.3 1.6-1.3 1.6-3.9 0-6.2-1.4-2.3-4-3.3-5.6-2z"/></svg></a>
3. [Answers to the book's challenges](https://github.com/munificent/craftinginterpreters/blob/master/note/)
4. [@OpenAI ](https://openai.com) - Documentation and README has been generated with the help of ChatGPT.

To stay tuned for further updates and progress consider following the project and might as well star it too :D.