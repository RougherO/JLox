## Getting-started

### Installation

#### Docker image

> (This section is currently unimplemented.)

#### Using JVM

> (temporary solution till a CLI interface is made available)

Download the jar file and run it using JVM:
```bash
$ java -jar JLox-0.0.1.jar [filename]
```
---
If you prefer building from source and clone the [JLox repo](https://github.com/RougherO/JLox).

```bash
$ git clone https://github.com/RougherO/JLox && cd JLox
```
Since `maven` is used as the build tool hence installing maven is required. Then run the package command.
```bash
$ mvn package
```
This should create a `.jar` file in the target directory by default. You can change the package format from pom.xml if you want it packeged in other formats.

Run it using [JVM](#using-jvm)

An optional filename arguement takes any text file which contains valid JLox code. Omitting the filename creates a REPL interface which accepts any valid JLox statements.
```
Welcome to Jlox. For documentation refer to https://github.com/RougherO/JLox/docs
=> 
```

Here's how a sample JLox file looks like
```javascript
for (let i = 0; i < 100; ++i) {
    print("Programming is fun!");
}
```

Here's a REPL sample
```bash
=> print("Hello World!");
Hello World!
``` 
As you probably noticed JLox uses semi-colons to mark end of statements. It has very simple syntax and is very similar to python and JS. See [Syntax](./syntax.md) to know more.

<a href="./intro.md" style="color: white;"> <button style="float: left; background: none; min-height: 30px; border-radius: 5px; border-color: white; padding: 10px 15px"><i>< Intro </i></button></a>

<a href="./syntax.md" style="color: white"><button style="float: right; background: none; min-height: 30px; border-radius: 5px; border-color: white; padding: 10px 15px"><i> Syntax > </i></button></a>