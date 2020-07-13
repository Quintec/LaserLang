# LaserLang
Laser is a 2D language based on lasers.

# Usage
Requires java.
Download/clone the repository, then compile the interpreter file:

`javac Laser.java`

To execute a program, save it in a file with a `.lsr` extension and then run

`java Laser [filename] [space separated arguments]`

# Documentation

Laser is a 2-D language designed to be relatively simple to read code in, even if you have never seen the language before. 

# Basic Function

Like many 2-D langauges, Laser has an instruction pointer that executes the one character instructions it encounters. The instruction pointer starts at the top left and initially points right. The pointer can wrap around the program and termination only occurs on error or the termination character `#`. The memory structure is a *list of stacks*. There are only two types in Laser: `String` and `Number`. `Number`s are java `Long`s.

Laser will push any one digit integer it encounters onto the current stack. To input a multi-digit integer, surround the integer with *single quotes* (`''`). To input a string, surround the string with *double quotes* (`""`). Note that the instruction pointer will still parse mirrors (next section) as normal when reading strings. To force the interpreter to ignore mirrors, use *raw mode* and surround your string with backticks (``` `` ```).

## Examples

In these examples and all examples following, the top of the stack is the beginning of the "array".

| Code | Stack |
| ---- | ----------------- |
| `123` | `[3, 2, 1]` |
| ``` '123' ``` | `[123]` |
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

Unary operations operate on the element on top of the stack, while binary operations operate on the top two elements of the stack. Stack operations modify the whole stack in some way not related to only one element, or move between stacks.

## Unary Operations

| Op | Description | Stack (before) | Stack (after) | Notes |
| -- | ----------- | -------------- | ------------- | ----- |
| `(` | Decrement | `[a, b, c]` | `[(a - 1), b, c]` | Also works on strings via ascii codes. |
| `)` | Increment | `[a, b, c]` | `[(a + 1), b, c]` | Also works on strings via ascii codes. |
| `c` | Cardinality | `[a, b, c]` | `[3, a, b, c]` | Also known as length, size, etc. (of the current stack) |
| `r` | Replicate | `[a, b, c]` | `[a, a, b, c]` | |
| `R` | Stack Replicate | `[[a, b, c]]` | `[[a, b, c], [a, b, c]]` | |
| `!` | Not (bit flip) | `[1, 0]` | `[0, 0]` | |
| `~` | Bitwise not | `[1, 0]` | `[-2, 0]` | |
| `p` | Pop | `[a, b, c]` | `[b, c]` | |
| `P` | Stack Pop | `[[a, b, c], [a, b, c]]` | `[[a, b, c]]` | |
| `o` | Output | `[a, b, c]` | `[b, c]` | Pops and outputs the top element of the stack. |
| `O` | Stack Output | `[a, b, c]` | `[]` | Pops and outputs the whole stack, space separated. |

## Binary Operations

| Op | Description | Stack (before) | Stack (after) | Notes |
| -- | ----------- | -------------- | ------------- | ----- |
| `+` | Add | `[a, b, c]` | `[a + b, c]` | Also concatenates strings. |
| `-` | Subtract | `[a, b, c]` | `[b - a, c]` | |
| `×` | Multiply | `[a, b, c]` | `[a × b, c]`| |
| `÷` | Divide | `[a, b, c]` | `[b ÷ a, c]` | Does integer division only. (floors result) |
| `*` | Exponentiate | `[a, b, c]` | `[b ^ a, c]` | |
| `g` | Greater Than | `[1, 3, 5]` | `[1, 5]` | Can compare strings also. |
| `l` | Less Than | `[1, 3, 5]` | `[0, 5]` | Can compare strings also. |
| `=` | Equals | `[3, 3, 5]` | `[1, 5]` | Can compare strings also. |
| `&` | Bitwise And | `[1, 0]` | `[0]` | |
| `\|` | Bitwise Or | `[1, 0]` | `[1]` | |
| `%` | Modulo | `[a, b, c]` | `[b % a, c]` | |

## Stack Operations

| Op | Description | Stack (before) | Stack (after) | Notes |
| -- | ----------- | -------------- | ------------- | ----- |
| `U` | Stack Up | | | Moves up a stack in the list of stacks. If the stack above doesn't exist, automatically creates one. |
| `D` | Stack Down | | | Moves down a stack in the list of stacks. |
| `u` | Rotate Up | `[a, b, c]` | `[c, a, b]` | |
| `d` | Rotate Down | `[a, b, c]` | `[b, c, a]` | |
| `s` | Swap Up | (Pointing to `[a, b, c]`) <br> `[[a, b, c], [d, e, f]]` | (Pointing to `[b, c]`) <br> `[[b, c], [a, d, e, f]]` | If the stack above doesn't exist, automatically creates one. |
| `w` | Swap Down | (Pointing to `[d, e, f]`) <br> `[[a, b, c], [d, e, f]]` | (Pointing to `[e, f]`) <br> `[[d, a, b, c], [e, f]]` | |
| `i` | Input | `[]` | `[(first input)]` | Pops one argument from the input stack and pushes it onto the current stack. |
| `I` | Input Stack | `[]` | `[input1, input2, input3, ...]` | Pops all arguments from the input stack onto the current stack. |
| `#` | Terminate | | | Terminates execution of program. The current stack is outputted (space-separated) as well. |

Note: if no `i` or `I` commands are in the program, Laser will automatically implicitly push the entire input stack onto the first stack at the beginning of the program. 

## Laser Command

Documentation <br>
Does not exist for this yet<br>
This is a haiku
