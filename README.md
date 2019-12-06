# LaserLang
Laser is a 2D language based on lasers.

# Usage
Requires java.
Download/clone the repository, then compile the interpreter file:

`javac Laser.java`

To execute a program, save it in a file with a `.lsr` extension and then run

`java Laser [filename] [space separated arguments]`

# Documentation
(WIP)

Laser is a 2-D language designed to be relatively simple to read code in, even if you have never seen the language before. 

# Basic Function

Like many 2-D langauges, Laser has an instruction pointer that executes the one character instructions it encounters. The instruction pointer starts at the top left and initially points right. The pointer can wrap around the program and termination only occurs on error or the termination character `#`. The memory structure is a *list of stacks*. There are only two types in Laser: `String` and `Number`. `Number`s are java `Long`s.

Laser will push any one digit integer it encounters onto the current stack. To input a multi-digit integer, surround the integer with *single quotes* (`''`). To input a string, surround the string with *double quotes* (`""`). Note that the instruction pointer will still parse mirrors (next section) as normal when reading strings. To force the interpreter to ignore mirrors, use *raw mode* and surround your string with backticks (``` `` ```).

## Examples

| Code | Stack (top first) |
| ---- | ----------------- |
| `123` | `[3, 2, 1]` |
| ``` `123` ``` | `[123]` |
| `"foo"` | `["foo"]` |
| `"fo""o"` | `["o", "fo"]` |
| <pre>"fo\\<br>&nbsp;&nbsp;&nbsp;o<br>&nbsp;&nbsp;&nbsp;"</pre> | `["foo"]` |
| ``` `i can slash/\ yay` ``` | `["i can slash/\ yay"]` |

# Mirrors

Control flow is managed with a series of **mirrors** to change the direction of the instruction pointer. The mirrors work as you would expect them to - imagine a bouncy ball/actual laser hitting them and where it would go next.

## Basic mirrors

| Mirror | Direction changes (from -> to) |
| ------ | ------------------------------ |
| `\` | up -> left, left -> up, down -> right, right -> down |
| `/` | up -> right, left -> down, down -> left, right -> up |

A good example of the use of mirrors can be found in the [Hello World program](helloworld.lsr). (As an exercise to get comfortable with the mirrors, try tracing the program!)

## Double-sided mirrors

Laser also has unique **double-sided mirrors** - think of these as two mirrors in one character with a small gap separating them. Double-sided mirrors will force the instruction pointer into a row/column.

| Mirror | Direction changes (from -> to) |
| ------ | ------------------------------ |
| `>` | up -> right, left -> left, down -> right, right -> right |
| `v` | up -> up, left -> down, down -> down, right -> down |
| `<` | up -> left, left -> left, down -> left, right -> right |
| `^` | up -> up, left -> up, down -> down, right -> up |

# Branching

The equivalent of `if` statements in Laser is *branching*. **Branches** are essentially forks in the road with two directions. Whenever the instruction pointer encounters a branch, it will only switch direction if the *current memory cell* is 0. Otherwise, it will keep going in the original direction.

| Branch | Direction change (if applied) |
| ------ | ----------------------------- |
| `⌞` | up -> right, left -> up, down -> right, right -> up|
| `⌜` | up -> right, left -> down, down -> right, right -> down |
| `⌟` | up -> left, left -> up, down -> left, right -> up |
| `⌝` | up -> left, left -> down, down -> left, right -> down |

# Operations

## Unary Operations

## Binary Operations



