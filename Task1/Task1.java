


import java.util.ArrayList;
import java.util.List;

// Do not modify the code below except for replacing the "..."!  Don't
// add anything (including "public" declarations), don't remove
// anything (including "public" declarations). Don't wrap it in a
// package, don't make it an innner class of some other class.  If
// your IDE suggsts to change anything below, ignore your IDE. You are
// welcome to add new classes! Please put them into separate files.

class Task1 {
    public static Codegen create () throws CodegenException {return new CodeGenerator(); } 

public static void main(String[] args) throws CodegenException {
        //While w = new While(new IntLiteral(2), new Equals(), new IntLiteral(2), new IntLiteral(10));
        Declaration d = new Declaration("p13", 0, new RepeatUntil(new Break(),new IntLiteral(1), new Equals(), new IntLiteral(2)));
        List<Declaration> progs = new ArrayList<>();
        progs.add(d);
        Program p = new Program(progs);
        System.out.println(create().codegen(p));
    }}