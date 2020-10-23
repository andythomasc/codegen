


import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class CodeGenerator implements Codegen{
 public static int label=0;
 public static int loop_label=0;
 private List<String> codeList ;
 private Stack<String> loops;
 
 
 
    @Override
    public String codegen(Program p) throws CodegenException {
       codeList = new ArrayList();
       loops = new Stack();
       
       StringBuilder builder = new StringBuilder();
       codeList.add("\n.data");
       codeList.add("\n.text");
       
       for (Declaration declaration: p.decls)
            {decl(declaration);}
       
       codeList.remove(codeList.size()-1);
       
       for(String code: codeList)
       {builder.append(code);} 
       
       return builder.toString();
    }
    
    
    public void decl(Declaration d){
    
        int sizeAr = (2 + d.numOfArgs)*4;
        codeList.add("\n"+d.id+"_"+label+":"+"\n\tmove $fp $sp"+"\n\tsw $ra 0($sp)"+"\n\taddiu $sp $sp -4");
        genExp(d.body);
        codeList.add("\n\tlw $ra 4($sp)"+"\n\taddiu $sp $sp "+sizeAr+"\n\tlw $fp 0($sp)"+"\n\tjr $ra");
    
    
    }
    
    public void vardec(int n)
        {if(n!=0)
            { codeList.add("\n\tvar"+label+": .space "+n );}
}

    
    
    public void genExp(Exp exp)
    {
        if(exp instanceof IntLiteral) {
            generateInt(exp);
        }
        else if(exp instanceof Variable)
        {   generateVariable(exp);
        }
        else if(exp instanceof If)
        {
            generateIf(exp);
        }
        else if(exp instanceof Binexp)
        {
            generateBinexp(exp);
        }
        else if(exp instanceof Invoke){
            generateInvoke(exp);
        }
        else if(exp instanceof Seq){
            genExp(((Seq) exp).l);
            genExp(((Seq) exp).r);
        }
        else if(exp instanceof Skip){
            codeList.add("\n\tnop");
        }
        else if(exp instanceof While){
            generateWhile(exp);
        }
        else if(exp instanceof Assign){
            genExp(((Assign)exp).e);
            codeList.add("\n\tsw $a0 "+4*((Assign) exp).x+"($fp)");
        
        }
        else if(exp instanceof RepeatUntil){
            generateRepeatUntil(exp);
        }
        else if(exp instanceof Break){
            generateBreak(exp);
        }
        else if(exp instanceof Continue){
            generateContinue(exp);
        }
        
    
    
    }
    
    public void generateInt(Exp exp){
        codeList.add("\n\tli $a0 "+((IntLiteral) exp).n);
    }
    public void generateVariable(Exp exp){
        codeList.add("\n\tlw $a0 "+((Variable) exp).x*4+"($fp)");
    }
    public void generateIf(Exp exp){
    label++;
    int thenBranch = label;
    int elseBranch = label;
    int exitLabel = label;
    genExp(((If) exp).l);
    codeList.add("\n\t"+"sw $a0 0($sp) \n\taddiu $sp $sp -4");
    genExp(((If)exp).r);
    codeList.add("\n\t"+"lw $t1 4($sp)" +"\n\taddiu $sp $sp 4");
    comp(((If)exp).comp);
    codeList.add("thenBranch_"+thenBranch+"\nelseBranch_"+elseBranch+":"+"\n\t");
    genExp(((If) exp).elseBody);
    codeList.add("\n\tb "+"exitLabel_"+exitLabel+"\nthenBranch_"+thenBranch+":"+"\n\t");
    genExp(((If) exp).thenBody);
    codeList.add("\nexitLabel_"+exitLabel+":");
    }
    
    public void generateBinexp(Exp exp){
        genExp(((Binexp) exp).l);
        codeList.add("\n\tsw $a0 0($sp)"+"\n\taddiu $sp $sp -4"+"\n\t");
        genExp(((Binexp )exp).r);
        codeList.add("\n\tlw $t1 4($sp)");
        binOp(((Binexp)exp).binop);
        codeList.add("\n\taddiu $sp $sp 4");
        
    }
    
    public void generateInvoke(Exp exp){
        codeList.add("\n\tsw $fp 0($sp)"+"\n\taddiu $sp $sp -4");
        if(((Invoke) exp).args.size()==0)
                     {
                         codeList.add("\n\tjal "+((Invoke) exp).name+"_"+label);
                     }
                     else
                     {for(int i=((Invoke) exp).args.size()-1;i>=0;i--)
                     {
                         genExp(((Invoke)exp).args.get(i));
                         codeList.add("\n\tsw $a0 0($sp)"+"\n\taddiu $sp $sp -4");
                     }
                     codeList.add("\n\tjal "+((Invoke) exp).name+"_"+label);
                     }
    
    }
    
    public void generateWhile(Exp exp){
        loop_label++;
        codeList.add("\nloop_" + loop_label + ":");
        
        loops.push("loop_" + loop_label);
        loops.push("loop_exit_"+loop_label);
        
        genExp(((While)exp).l);
        codeList.add("\n\t" + "sw $a0 0($sp)"
                                  + "\n\t" + "addiu $sp $sp -4");
        genExp(((While)exp).r);
        codeList.add("\n\t" + "lw $t1 4($sp)"
                            +"\n\t" + "addiu $sp $sp 4");
        comp(((While)exp).comp);
        codeList.add("loop_body_"+loop_label+"\n\t"+"j "+"loop_exit_"+loop_label+"\nloop_body_"+loop_label+":");
        genExp(((While)exp).body);
        codeList.add("\n\t"+"j "+"loop_"+loop_label+"\n"+"loop_exit_"+loop_label+":");
    }
    
    
    public void generateRepeatUntil(Exp exp){
        loop_label++;
        codeList.add("\nloop_" + loop_label + ":");
        
        loops.push("loop_" + loop_label);
        loops.push("loop_exit_"+loop_label);
        genExp(((RepeatUntil)exp).body);
        genExp(((RepeatUntil)exp).l);
        codeList.add("\n\tsw $a0 0($sp)"+"\n\taddiu $sp $sp -4");
        genExp(((RepeatUntil)exp).r);
        codeList.add("\n\tsw $a0 0($sp)"+"\n\taddiu $sp $sp -4");
        comp(((RepeatUntil)exp).comp);
        codeList.add("loop_exit_"+loop_label +"\n\tj "+"loop_"+loop_label+ "\n\tlabel_exit_"+loop_label+":" );
        
        
    }
    public void generateBreak(Exp exp) {
        codeList.add("\n\t"+"j "+loops.pop());
        //loops.pop();
        }

    public void generateContinue(Exp exp) {
        loops.pop();
        codeList.add("\n\t"+"j "+loops.pop());
       }

    
    
   public void comp(Comp c)
             {
                 if(c instanceof Less)
                 {
                    codeList.add("\n\tblt $t1 $a0 ");
                 }
                 else if(c instanceof LessEq)
                 {
                     codeList.add("\n\tble $t1 $a0 ");
                 }
                 
                 else if(c instanceof Equals)
                 {
                     codeList.add("\n\tbeq $t1 $a0 ");
                 }
                 else if(c instanceof Greater)
                 {
                     codeList.add("\n\tbgt $t1 $a0 ");
                 }
                 else if(c instanceof GreaterEq)
                 {
                     codeList.add("\n\tbge $t1 $a0 ");
                 }  
}

public void binOp(Binop a)
             {
                 if(a instanceof Plus)
                 {
                     codeList.add("\n\tadd $a0 $t1 $a0");
                 }
                 else if(a instanceof Minus)
                 {
                     codeList.add("\n\tsub $a0 $t1 $a0");
                 }
                 else if(a instanceof Div)
                 {
                     codeList.add("\n\tdiv $t1 $a0"+"\n\tmflo $a0");
                 }
                 else if(a instanceof Times)
                 {
                     codeList.add("\n\tmult $t1 $a0"+"\n\tmflo $a0");
                 }
             }

    





}