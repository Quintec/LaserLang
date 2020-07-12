import java.util.*;
import java.io.*;
public class Laser {
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    
    public static final int INSTRUCTION_MODE = 10;
    public static final int STRING_MODE = 11;
    public static final int NUMBER_MODE = 12;
    public static final int RAW_MODE = 13;
    
    public static final Set<Character> MIRRORS = new HashSet<Character>(Arrays.asList('\\', '/', '>', '<', '^', 'v')); 
    public static final Set<Character> BIN_OPS = new HashSet<Character>(Arrays.asList('+', '-', '×', '÷', '*', '&', '|', '%', 'g', 'l', '='));
    public static final Set<Character> UNARY_OPS = new HashSet<Character>(Arrays.asList('(', ')', 'r', 'R', '!','~', 'c'));
    public static final Set<Character> BRANCHES = new HashSet<Character>(Arrays.asList('⌞', '⌜', '⌟', '⌝'));

    public static final Long TRUE = Long.valueOf(1);
    public static final Long FALSE = Long.valueOf(0);
    
    private final int rows;
    private final int cols;
    private final char[][] program;

    private boolean verbose;
    
    private int pRow;
    private int pCol;
    private int dir;
    
    private ArrayList<LinkedList<Object>> memory;
    private int addr;

    private LinkedList<Object> input;
    
    private String str;
    private String num;
    
    private int mode;

    public static void main(String[] args) throws IOException {
        run(args);
    }
    
    public static void run(String[] args) throws IOException {
        if (args[0].indexOf(".lsr") == -1) {
            System.err.println("FileError: Laser program files must end in .lsr");
            System.exit(1);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
        ArrayList<String> lines = new ArrayList<String>();
        
        String line = null;
        int maxLen = 0;
        int rows = 0;
        boolean vb = false;

        boolean implicit = true;

        LinkedList<Object> input = new LinkedList<Object>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-v") || args[i].equals("-verbose")) {
                vb = true;
            } else if (args[i].charAt(0) == '"') {
                input.push(args[i].substring(1, args[i].length() - 1));
            } else if (args[i].matches("[0-9]+")) {
                input.push(Long.parseLong(args[i]));
            } else {
                input.push(args[i]);
            }
        }
        
        while ((line = in.readLine()) != null) {
            rows++;
            maxLen = Math.max(maxLen, line.length());
            if (line.contains("i") || line.contains("I"))
                implicit = false;
            lines.add(line);
        }
        
        char[][] prog = new char[rows][maxLen];
        for (int i = 0; i < lines.size(); i++) {
            char[] cs = lines.get(i).toCharArray();
            for (int j = 0; j < cs.length; j++) {
                prog[i][j] = cs[j];
            }
        }
        new Laser(prog, rows, maxLen, input, vb, implicit).run();
    }
    
    public Laser(char[][] p, int r, int c, LinkedList<Object> inp, boolean v, boolean imp) {
        this.program = p;
        this.rows = r;
        this.cols = c;

        this.verbose = v;
        
        this.pRow = 0;
        this.pCol = 0;
        this.dir = EAST;
        this.mode = INSTRUCTION_MODE;
        
        this.memory = new ArrayList<LinkedList<Object>>();
        memory.add(new LinkedList<Object>());
        this.addr = 0;

        this.input = inp;
        
        this.str = "";
        this.num = "";

        if (imp) {
            while (!input.isEmpty()) {
                memory.get(addr).push(input.pop());
            }
        }
    }

    private long fastPow(long base, long power) {
        long res = 1;
        long sq = base;
        while(power > 0){
            if(power % 2 == 1){
                res *= sq; 
            }
            sq = sq * sq;
            power /= 2;
        }
        return res;
    }

    private long flipBits(long n) {
        if (n == 0)
            return 1;
        long k = (long)(Math.floor(Math.log(n)/Math.log(2))+1);
        long mask = (1 << k) - 1;
        return n ^ mask;
    }
    
    private void run() {
        boolean cont;
        do {
            cont = step();
        } while (cont);
    }
    
    private boolean step() {
        char curr = program[pRow][pCol];
        if (verbose)
            System.out.println("curr: " +curr + "\t" + "addr: " + addr + "\t" + "stack: " + memory.get(addr));
        switch (mode) {
            case INSTRUCTION_MODE:
                if (MIRRORS.contains(curr)) {
                    parseMirror(curr);
                } else if (curr == '"')  {
                    mode = STRING_MODE;
                    str = "";
                } else if (curr == '`') {
                    mode = RAW_MODE;
                    str = "";
                } else if (curr == '\'') {
                    mode = NUMBER_MODE;
                    num = "";
                } else if (BIN_OPS.contains(curr)) {
                    binOp(curr);
                } else if (UNARY_OPS.contains(curr)) {
                    unaryOp(curr);
                } else if (BRANCHES.contains(curr)) {
                    branch(curr);
                } else if (Character.isDigit(curr)) {
                    memory.get(addr).push(Long.parseLong(""+curr));
                } else if (curr == 'L') {
                    laser();
                } else if (curr == 'p') {
                    memory.get(addr).pop();
                } else if (curr == 'P') {
                    memory.remove(addr);
                    if (addr >= memory.size())
                        addr--;
                } else if (curr == 'o') {
                    System.out.println(memory.get(addr).pop());
                } else if (curr == 'O') {
                    if (!memory.get(addr).isEmpty()) {
                        String output = "";
                        while (!memory.get(addr).isEmpty()) {
                            output += memory.get(addr).pop() + " ";
                        }
                        output = output.substring(0, output.length() - 1);
                        System.out.println(output);
                    }
                } else if (curr == 'U') {
                    addr++;
                    if (addr >= memory.size())
                        memory.add(new LinkedList<Object>());
                } else if (curr == 'D') {
                    addr--;
                    if (addr < 0) {
                        System.err.println("IndexError: tried to access stack at position -1");
                        System.exit(1);
                    }
                } else if (curr == 'u') {
                    Object bottom = memory.get(addr).removeLast();
                    memory.get(addr).push(bottom);
                } else if (curr == 'd') {
                    Object top = memory.get(addr).pop();
                    memory.get(addr).add(top);
                } else if (curr == 's') {
                    Object top = memory.get(addr).pop();
                    addr++;
                    if (addr >= memory.size())
                        memory.add(new LinkedList<Object>());
                    memory.get(addr).push(top);
                    addr--;
                } else if (curr == 'w') {
                    Object top = memory.get(addr).pop();
                    addr--;
                    if (addr < 0) {
                        System.err.println("IndexError: tried to access stack at position -1");
                        System.exit(1);
                    }
                    memory.get(addr).push(top);
                    addr++;
                } else if (curr == 'i') {
                    try {
                        memory.get(addr).push(input.pop());
                    } catch (NoSuchElementException nsee) {
                        System.err.println("InputError: tried to pop input off empty stack");
                        System.exit(1);
                    }
                } else if (curr == 'I') {
                    while (!input.isEmpty()) {
                        memory.get(addr).push(input.pop());
                    }
                } else if (curr == '#') {
                    if (memory.get(addr).isEmpty())
                        return false;
                    String output = "";
                    while (!memory.get(addr).isEmpty()) {
                        output += memory.get(addr).pop() + " ";
                    }
                    output = output.substring(0, output.length() - 1);
                    System.out.println(output);
                    return false;
                } 
                break;
            case STRING_MODE:
            case RAW_MODE:
                if (curr == '"' && mode == STRING_MODE
                        || curr == '`' && mode == RAW_MODE) {
                    memory.get(addr).push(str);
                    mode = INSTRUCTION_MODE;
                } else if (MIRRORS.contains(curr) && mode == STRING_MODE) {
                    parseMirror(curr);
                } else {
                    str += curr;
                }
                break;
            case NUMBER_MODE:
                if (curr == '\'') {
                    memory.get(addr).push(Long.parseLong(num));
                    mode = INSTRUCTION_MODE;
                } else if (MIRRORS.contains(curr)) {
                    parseMirror(curr);
                } else {
                    num += curr;
                }
                break;
            default:
                System.err.println("InternalError: unknown parse mode");
                System.exit(1);
        }
        movePtr();
        return true;
    }

    private void laser() {
        movePtr();
        char op = program[pRow][pCol];
        Set<Character> unusable = new HashSet<Character>(Arrays.asList('r', 'R'));
        if (BIN_OPS.contains(op) && !(op == '-')) {
            while (memory.get(addr).size() > 1) {
                binOp(op);
            }
        } else if (op == '-' || op == 'o' || (UNARY_OPS.contains(op) && !unusable.contains(op))) {
            for (int i = 0; i < memory.get(addr).size(); i++) {
                switch (op) {
                    case '(':
                        memory.get(addr).set(i, (Long)(memory.get(addr).get(i)) - 1);
                        break;
                    case ')':
                        memory.get(addr).set(i, (Long)(memory.get(addr).get(i)) + 1);
                        break;
                    case '!':
                        memory.get(addr).set(i, flipBits((Long)(memory.get(addr).get(i))));
                        break;
                    case '~':
                        memory.get(addr).set(i, ~(Long)(memory.get(addr).get(i)));
                        break;
                    case '-':
                        memory.get(addr).set(i, 0 - (Long)(memory.get(addr).get(i)));
                        break;
                    case 'o':
                        while (memory.get(addr).size() > 0)
                            System.out.println(memory.get(addr).pop());
                        break;
                }
            }
        }
    }

    private void unaryOp(char curr) {
        Object a;
        Long la;
        switch(curr) {
            case '(':
                a = memory.get(addr).pop();
                if (a instanceof String) {
                    char[] chars = ((String)a).toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        chars[i] = (char)((int)chars[i] - 1);
                    }
                    memory.get(addr).push(new String(chars));
                } else {
                    memory.get(addr).push((Long)a - 1);
                }
                break;
            case ')':
                a = memory.get(addr).pop();
                if (a instanceof String) {
                    char[] chars = ((String)a).toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        chars[i] = (char)((int)chars[i] + 1);
                    }
                    memory.get(addr).push(new String(chars));
                } else {
                    memory.get(addr).push((Long)a + 1);
                }
                break;
            case 'r':
                a = memory.get(addr).pop();
                Object b;
                if (a instanceof String) {
                    b = new String((String)a);
                } else {
                    b = Long.valueOf((Long)a);
                }
                memory.get(addr).push(a);
                memory.get(addr).push(b);
                break;
            case 'R':
                LinkedList<Object> sack = memory.get(addr);
                LinkedList<Object> dup = new LinkedList<Object>(sack);
                memory.add(addr, dup);
                break;
            case '!':
                a = memory.get(addr).pop();
                la = (Long)a;
                memory.get(addr).push(flipBits(la));
                break;
            case '~':
                a = memory.get(addr).pop();
                la = (Long)a;
                memory.get(addr).push(~la);
                break;
            case 'c':
            	LinkedList<Object> temp = memory.get(addr);
            	temp.push(Long.valueOf(temp.size()));
            	break;
        }
    }

    private void binOp(char curr) {
        Object a, b;
        Long la, lb;
        switch(curr) {
            case '+':
                a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                if (a instanceof String || b instanceof String) {
                    memory.get(addr).push(a.toString() + b.toString());
                } else {
                    la = (Long)a;
                    lb = (Long)b;
                    memory.get(addr).push(la + lb);
                }
                break;
            case '-':
                a = memory.get(addr).pop();
                la = (Long)a;
                if (memory.get(addr).size() > 0) {
                    b = memory.get(addr).pop();
                    lb = (Long)b;
                    memory.get(addr).push(lb - la);
                } else {
                    memory.get(addr).push(0 - la);
                }
                break;
            case '×':
                a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                la = (Long)a;
                lb = (Long)b;
                memory.get(addr).push(la * lb);
                break;
            case '÷':
                a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                la = (Long)a;
                lb = (Long)b;
                memory.get(addr).push(lb / la);
                break;
            case '*':
                a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                la = (Long)a;
                lb = (Long)b;
                memory.get(addr).push(fastPow(lb, la));
                break;
            case 'g':
            	a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                if (a instanceof String || b instanceof String) {
                	int comp = b.toString().compareTo(a.toString());
                	if (comp > 0)
                		memory.get(addr).push(TRUE);
                	else
                		memory.get(addr).push(FALSE);
                } else {
                	la = (Long)a;
                	lb = (Long)b;
                	if (lb > la)
                		memory.get(addr).push(TRUE);
                	else
                		memory.get(addr).push(FALSE);
                }
                break;
            case 'l':
            	a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                if (a instanceof String || b instanceof String) {
                	int comp = b.toString().compareTo(a.toString());
                	if (comp > 0)
                		memory.get(addr).push(FALSE);
                	else
                		memory.get(addr).push(TRUE);
                } else {
                	la = (Long)a;
                	lb = (Long)b;
                	if (lb > la)
                		memory.get(addr).push(FALSE);
                	else
                		memory.get(addr).push(TRUE);
                }
                break;
            case '=':
            	a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                if (a instanceof String || b instanceof String) {
                	int comp = b.toString().compareTo(a.toString());
                	if (comp == 0)
                		memory.get(addr).push(TRUE);
                	else
                		memory.get(addr).push(FALSE);
                } else {
                	la = (Long)a;
                	lb = (Long)b;
                	if (lb.equals(la))
                		memory.get(addr).push(TRUE);
                	else
                		memory.get(addr).push(FALSE);
                }
                break;
            case '&':
                a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                la = (Long)a;
                lb = (Long)b;
                memory.get(addr).push(la & lb);
                break;
            case '|':
                a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                la = (Long)a;
                lb = (Long)b;
                memory.get(addr).push(la | lb);
                break;
            case '%':
                a = memory.get(addr).pop();
                b = memory.get(addr).pop();
                la = (Long)a;
                lb = (Long)b;
                memory.get(addr).push(lb % la);
                break;
        }
    }
    
    private void movePtr() {
        switch (dir) {
            case NORTH:
                pRow--;
                if (pRow < 0) {
                    pRow = rows - 1;
                }
                break;
            case EAST:
                pCol++;
                if (pCol >= cols) {
                    pCol = 0;
                }
                break;
            case SOUTH:
                pRow++;
                if (pRow >= rows) {
                    pRow = 0;
                }
                break;
            case WEST:
                pCol--;
                if (pCol < 0) {
                    pCol = cols - 1;
                }
                break;
        }
    }

    private void branch(char curr) {
        long val = (long)memory.get(addr).peek();
        switch (curr) {
            case '⌞':
                switch (dir) {
                    case NORTH:
                    case SOUTH:
                        if (val == 0)
                            dir = EAST;
                        break;
                    case EAST:
                    case WEST:
                        if (val == 0)
                            dir = NORTH;
                        break;
                }
                break;
            case '⌜':
                switch (dir) {
                    case NORTH:
                    case SOUTH:
                        if (val == 0)
                            dir = EAST;
                        break;
                    case EAST:
                    case WEST:
                        if (val == 0)
                            dir = SOUTH;
                        break;
                }
                break;
            case '⌟':
                switch (dir) {
                    case NORTH:
                    case SOUTH:
                        if (val == 0)
                            dir = WEST;
                        break;
                    case EAST:
                    case WEST:
                        if (val == 0)
                            dir = NORTH;
                        break;
                }
                break;
            case '⌝':
                switch (dir) {
                    case NORTH:
                    case SOUTH:
                        if (val == 0)
                            dir = WEST;
                        break;
                    case EAST:
                    case WEST:
                        if (val == 0)
                            dir = SOUTH;
                        break;
                }
                break;
        }
    }
    
    private void parseMirror(char curr) {
        switch (curr) {
            case '/':
                switch (dir) {
                    case NORTH:
                        dir = EAST;
                        break;
                    case EAST:
                        dir = NORTH;
                        break;
                    case SOUTH:
                        dir = WEST;
                        break;
                    case WEST:
                        dir = SOUTH;
                        break;
                    default:
                        System.err.println("InternalError: unknown pointer direction");
                        System.exit(1);
                }
                break;
            case '\\':
                dir = 3 - dir;
                break;
            case '>':
                switch (dir) {
                    case NORTH:
                    case SOUTH:
                        dir = EAST;
                        break;
                }
                break;
            case '<':
                switch (dir) {
                    case NORTH:
                    case SOUTH:
                        dir = WEST;
                        break;
                }
                break;
            case '^':
                switch (dir) {
                    case EAST:
                    case WEST:
                        dir = NORTH;
                        break;
                }
                break;
            case 'v':
                switch (dir) {
                    case EAST:
                    case WEST:
                        dir = SOUTH;
                        break;
                }
                break;
            default:
                System.err.println("SyntaxError: unknown character " + curr);
                System.exit(1);
        }
    }
}
