package miniplc0java.Analyser;

import miniplc0java.Tokenizer.*;

import java.io.*;
import java.util.ArrayList;

public class Analyser {
    static Tokenizer t;
    DataOutputStream output;


    FunctionList functionList=new FunctionList();
    Scope scope=new Scope();
    boolean isCreatingFunction=false;


    Stack stack=new Stack();


    ArrayList<Boolean>br=new ArrayList<>();
    ArrayList<BreakPoint>breakPoints=new ArrayList<>();
    int loopLevel=0;

    ReturnCheck returnCheck=new ReturnCheck();

    static String[]stdio={"getint","getdouble","getchar","putint","putdouble","putchar","putstr","putln"};

    public Analyser(File file,File file2) throws IOException {
        t=new Tokenizer(file);
        output=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file2.getAbsoluteFile())));
        functionList.add(new Function("_start"));
        scope.addScope();
        program();
        output();
    }
    private void expectNonTerminal(String s,boolean b){
        if(!b)
            throw new Error("expect NonTerminal "+s+" "+getPos());
    }
    private void program() throws IOException {
        while(decl_stmt());
        while(t.peekToken()!=null)
            expectNonTerminal("function",function());
        Function _start=functionList.functions.get(0);
        Integer mainID=null;
        for(int i=0;i<functionList.functions.size();i++){
            if(functionList.functions.get(i).name.equals("main")){
                mainID=i;
                break;
            }
        }
        if(mainID==null)
            throw new Error("expect function main in this program");
        if(functionList.functions.get(mainID).variableType!=VariableType.VOID){
            functionList.addInstruction("stackalloc "+1,toByte(0x1a,1,4));
        }
        functionList.addInitialInstruction("call "+mainID,toByte(0x48,mainID,4));
    }
    private boolean decl_stmt() {
        boolean isConst;
        if(t.ifNextToken(TokenType.LET_KW))
            isConst=false;
        else if(t.ifNextToken(TokenType.CONST_KW))
            isConst=true;
        else
            return false;
        String name=t.expectToken(TokenType.IDENT).getValue().toString();
        VariableType variableType=getVariableType();
        Variable variable=new Variable(name,isConst,variableType,functionList.functions.size()==1,false,functionList.functions.size()!=1,null);
        functionList.addVariable(variable);
        scope.addVariable(variable);
        if(t.ifNextToken(TokenType.ASSIGN)){
            pushVariableAddress(name);
            expectNonTerminal("expr",expr(false));
//            variable.isInitialized=true;
            functionList.addInstruction("store.64", toByte(0x17, 0, 0));
            stack.pop(variableType);
            stack.pop(StackEnum.ADDR);
        }else if(isConst){
            throw new Error();
        }
        t.expectToken(TokenType.SEMICOLON);

        return true;
    }
    private boolean function() throws IOException {
        if(t.ifNextToken(TokenType.FN_KW)){
            String name=t.expectToken(TokenType.IDENT).getValue().toString();
            Function function=new Function(name);
            functionList.add(function);
//            scope.addVariable(new Variable(name,true,true,VariableType.FUNCTION,name));
            scope.addScope();
            t.expectToken(TokenType.L_PAREN);
            function_param_list();
            t.expectToken(TokenType.R_PAREN);
            t.expectToken(TokenType.ARROW);
            Token type=t.expectToken(TokenType.TYPE_KW);
            if(type.getValue().equals("int")){
                functionList.setVariableType(VariableType.INT);
                functionList.topFunction.params.add(0,new Variable(null,false,VariableType.INT,false,true,false,null));
                for(int i=1;i<functionList.topFunction.params.size();i++)
                    functionList.topFunction.params.get(i).offset++;
                functionList.setReturnSlot();
            }else if(type.getValue().equals("double")){
                functionList.setVariableType(VariableType.DOUBLE);
                functionList.topFunction.params.add(0,new Variable(null,false,VariableType.DOUBLE,false,true,false,null));
                for(int i=1;i<functionList.topFunction.params.size();i++)
                    functionList.topFunction.params.get(i).offset++;
                functionList.setReturnSlot();
            }else if(type.getValue().equals("void")){
                functionList.setVariableType(VariableType.VOID);
//                functions.setReturnSlot(0);
            }else
                throw new Error("cannot identify Variable type "+getPos());
            isCreatingFunction=true;
            returnCheck.returnPoints.add(new ReturnPoint(ReturnEnum.FUNCTION,false));
            expectNonTerminal("block_stmt",block_stmt());
            returnCheck.getResult();
            if(returnCheck.returnPoints.size()!=1)
                throw new Error("123");
            else if(function.variableType!=VariableType.VOID&&!returnCheck.returnPoints.get(0).ifReturn)
                throw new Error("function ["+functionList.topFunction.name+"] need to return "+getPos());
            returnCheck.returnPoints.remove(0);
            if(function.variableType==VariableType.VOID){
                functionList.addInstruction("ret",toByte(0x49,0,0));
            }
        }else
            return false;
        return true;
    }
    private boolean function_param_list() {
        if(t.peekToken().getTokenType()!=TokenType.R_PAREN){
            expectNonTerminal("function_param",function_param());
            while(t.ifNextToken(TokenType.COMMA)){
                expectNonTerminal("function_param",function_param());
            }
        }else
            return false;
        return true;
    }
    private boolean function_param() {
        boolean isConst=false;
        if(t.ifNextToken(TokenType.CONST_KW))
            isConst=true;
        String name=t.expectToken(TokenType.IDENT).getValue().toString();
        VariableType variableType=getVariableType();
        Variable variable=new Variable(name,isConst,variableType,false,true,false,null);
        scope.addVariable(variable);
        functionList.addVariable(variable);
        return true;
    }
    private boolean block_stmt() throws IOException {
        if(t.ifNextToken(TokenType.L_BRACE)){
            if(!isCreatingFunction)
                scope.addScope();
            isCreatingFunction=false;
            while(!t.ifNextToken(TokenType.R_BRACE)){
                expectNonTerminal("stmt",stmt());
            }
            scope.removeScope();
        }else
            return false;
        return true;
    }
    private boolean stmt() throws IOException {
        if(decl_stmt()){
        }else if(t.peekToken().getTokenType()==TokenType.IF_KW){
            if(loopLevel==0)
                returnCheck.returnPoints.add(new ReturnPoint(ReturnEnum.IF,false));
            if_stmt(true);
        }else if(while_stmt()){
        }else if(return_stmt()){
        }else if(block_stmt()){
        }else if(t.ifNextToken(TokenType.SEMICOLON)){
        }else if(break_continue_stmt()){
        }else if(expr(false)){
            t.expectToken(TokenType.SEMICOLON);
        }else
            return false;
        return true;
    }
    private boolean if_stmt(boolean isFirst) throws IOException {
        if(t.ifNextToken(TokenType.IF_KW)){
            expectNonTerminal("expr",expr(true));
            stack.pop(StackEnum.BOOL);
            int brStart=functionList.getInstructionNum();
            functionList.addInstruction("null",(byte)0);
            expectNonTerminal("block_stmt",block_stmt());
            int brNum=functionList.getInstructionNum()-brStart-1;
            if(t.ifNextToken(TokenType.ELSE_KW)){
                int skipStart=functionList.getInstructionNum();
                brNum++;
                functionList.addInstruction("null",(byte)0);
                if(t.peekToken().getTokenType()==TokenType.L_BRACE){
                    if(loopLevel==0)
                        returnCheck.returnPoints.add(new ReturnPoint(ReturnEnum.ELSE,false));
                    block_stmt();
                }else if(t.peekToken().getTokenType()==TokenType.IF_KW){
                    if(loopLevel==0)
                        returnCheck.returnPoints.add(new ReturnPoint(ReturnEnum.ELSE_IF,false));
                    if_stmt(false);
                }else
                    throw new Error("expect NonTerminal else "+getPos());
                int skipNum=functionList.getInstructionNum()-skipStart-1;
//                replaceBrInstruction(skipStart,skipNum);
                functionList.replaceInstruction(skipStart,"br "+skipNum,toByte(0x41,skipNum,4));
            }
            if(isFirst&&loopLevel==0)
                returnCheck.getResult();
            replaceBrInstruction(brStart,brNum);
        }else
            return false;
        return true;
    }
    private boolean while_stmt()throws IOException {
        if(t.ifNextToken(TokenType.WHILE_KW)){
            int returnPos=functionList.getInstructionNum();
            expectNonTerminal("expr",expr(true));
            stack.pop(StackEnum.BOOL);
            int brStart=functionList.getInstructionNum();
            functionList.addInstruction("null",(byte)0);
            loopLevel++;
            expectNonTerminal("block_stmt",block_stmt());
            loopLevel--;
            for(int i=0;i<breakPoints.size();i++){
                BreakPoint breakPoint=breakPoints.get(i);
                if(breakPoint.isBreak){
                    int brNum=functionList.getInstructionNum()-breakPoint.instructionID;
                    functionList.replaceInstruction(breakPoint.instructionID,"br "+brNum,toByte(0x41,brNum,4));
                }else{
                    int returnNum=returnPos-breakPoint.instructionID;
                    functionList.replaceInstruction(breakPoint.instructionID,"br "+returnNum,toByte(0x41,returnNum,4));
                }
            }
            breakPoints.clear();
            int brNum=functionList.getInstructionNum()-brStart;
            replaceBrInstruction(brStart,brNum);
            int returnNum=returnPos-functionList.getInstructionNum()-1;
            functionList.addInstruction("br "+returnNum,toByte(0x41,returnNum,4));
            return true;
        }
        return false;
    }
    private boolean break_continue_stmt(){
        if(t.ifNextToken(TokenType.BREAK_KW))
            if(loopLevel==0)
                throw new Error("token "+t.getThisToken().getValue().toString()+"cannot be here "+getPos());
            else{
                breakPoints.add(new BreakPoint(functionList.getInstructionNum(),true));
                functionList.addInstruction("null",(byte)0);
            }
        else if(t.ifNextToken(TokenType.CONTINUE_KW))
            if(loopLevel==0)
                throw new Error("token "+t.getThisToken().getValue().toString()+"cannot be here "+getPos());
            else{
                breakPoints.add(new BreakPoint(functionList.getInstructionNum(),false));
                functionList.addInstruction("null",(byte)0);
            }
        else
            return false;
        t.expectToken(TokenType.SEMICOLON);
        return true;
    }
    private boolean return_stmt() {
        if(t.ifNextToken(TokenType.RETURN_KW)){
            if(functionList.topFunction.variableType!=VariableType.VOID){
                functionList.addInstruction("arga 0",toByte(0x0b,0,4));
                stack.push(StackEnum.ADDR);
                expectNonTerminal("expr",expr(false));
                functionList.addInstruction("store.64",toByte(0x17,0,4));
                if(functionList.topFunction.variableType==VariableType.INT)
                    stack.pop(StackEnum.INT);
                else
                    stack.pop(StackEnum.DOUBLE);
                stack.pop(StackEnum.ADDR);
            }
            t.expectToken(TokenType.SEMICOLON);
            if(loopLevel==0)
                returnCheck.top().ifReturn=true;
            functionList.addInstruction("ret",toByte(0x49,0,0));
        }else
            return false;
        return true;
    }
    private boolean expr(boolean isBool){
        expectNonTerminal("assign_expr",assign_expr(isBool));
        t.savePoint();
        if(t.ifNextToken(TokenType.IDENT)&&t.ifNextToken(TokenType.ASSIGN)){
            t.loadPoint();
            t.removePoint();
            String name=t.expectToken(TokenType.IDENT).getValue().toString();
            Variable variable=pushVariableAddress(name);
            if(variable.isConst){
                throw new Error("variable "+name+" cannot be assigned "+getPos());
            }
            t.expectToken(TokenType.ASSIGN);
//            isAssigned=true;
            expectNonTerminal("assign_expr",assign_expr(isBool));
//            isAssigned=false;
            functionList.addInstruction("store.64", toByte(0x17, 0, 0));
            stack.pop(variable.variableType);
        }else{
            t.loadPoint();
            t.removePoint();
            assign_expr(isBool);
        }
        return true;
    }
    private boolean assign_expr(boolean isBool){
        if(compare_expr()){
            while(true){
                String s;
                if(t.ifNextToken(TokenType.GE)){
                    expectNonTerminal("compare_expr",compare_expr());
                    s="GE";
                }else if(t.ifNextToken(TokenType.LE)){
                    expectNonTerminal("compare_expr",compare_expr());
                    s="LE";
                }else if(t.ifNextToken(TokenType.GT)){
                    expectNonTerminal("compare_expr",compare_expr());
                    s="GT";
                }else if(t.ifNextToken(TokenType.LT)){
                    expectNonTerminal("compare_expr",compare_expr());
                    s="LT";
                }else if(t.ifNextToken(TokenType.EQ)){
                    expectNonTerminal("compare_expr",compare_expr());
                    s="EQ";
                }else if(t.ifNextToken(TokenType.NEQ)){
                    expectNonTerminal("compare_expr",compare_expr());
                    s="NEQ";
                }else{
                    if(isBool&&stack.top()==StackEnum.INT){
                        br.add(false);
                        stack.pop(StackEnum.INT);
                        stack.push(StackEnum.BOOL);
                    }else if(isBool&&stack.top()==StackEnum.DOUBLE){
                        br.add(false);
                        stack.pop(StackEnum.DOUBLE);
                        stack.push(StackEnum.BOOL);
                    }
                    break;
                }
                compare();
                if(s.equals("GE")){
                    functionList.addInstruction("set.lt",toByte(0x39,0,0));
                    br.add(true);
                }else if(s.equals("LE")){
                    functionList.addInstruction("set.gt",toByte(0x3a,0,0));
                    br.add(true);
                }else if(s.equals("GT")){
                    functionList.addInstruction("set.gt",toByte(0x3a,0,0));
                    br.add(false);
                }else if(s.equals("LT")){
                    functionList.addInstruction("set.lt",toByte(0x39,0,0));
                    br.add(false);
                }else if(s.equals("EQ")){
                    br.add(true);
                }else{
                    br.add(false);
                }
            }
        }else
            return false;
        return true;
    }
    private boolean compare_expr(){
        if(plus_minus_expr()){
            while(true){
                if(t.ifNextToken(TokenType.PlUS)){
                    expectNonTerminal("plus_minus_expr",plus_minus_expr());
                    if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.INT){
                        functionList.addInstruction("add.i",toByte(0x20,0,0));
                        stack.pop(StackEnum.INT);
                    }else if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.DOUBLE) {
                        functionList.addInstruction("add.f", toByte(0x24, 0, 0));
                        stack.pop(StackEnum.DOUBLE);
                    }else
                        throw new Error(stack.top().toString()+" and "+stack.preTop().toString()+" cannot calculate "+getPos());
                }else if(t.ifNextToken(TokenType.MINUS)){
                    expectNonTerminal("plus_minus_expr",plus_minus_expr());
                    if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.INT){
                        functionList.addInstruction("sub.i",toByte(0x21,0,0));
                        stack.pop(StackEnum.INT);
                    }else if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.DOUBLE){
                        functionList.addInstruction("sub.f",toByte(0x25,0,0));
                        stack.pop(StackEnum.DOUBLE);
                    }else
                        throw new Error(stack.top().toString()+" and "+stack.preTop().toString()+" cannot calculate "+getPos());
                }else
                    break;
            }
        }else
            return false;
        return true;
    }
    private boolean plus_minus_expr(){
        if(mul_div_expr()){
            while(true){
                if(t.ifNextToken(TokenType.MUL)){
                    expectNonTerminal("mul_div_expr",mul_div_expr());
                    if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.INT){
                        functionList.addInstruction("mul.i",toByte(0x22,0,0));
                        stack.pop(StackEnum.INT);
                    }else if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.DOUBLE){
                        functionList.addInstruction("mul.f",toByte(0x26,0,0));
                        stack.pop(StackEnum.DOUBLE);
                    }else
                        throw new Error(stack.top().toString()+" and "+stack.preTop().toString()+" cannot calculate "+getPos());
                }else if(t.ifNextToken(TokenType.DIV)){
                    expectNonTerminal("mul_div_expr",mul_div_expr());
                    if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.INT){
                        functionList.addInstruction("div.i",toByte(0x23,0,0));
                        stack.pop(StackEnum.INT);
                    }else if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.DOUBLE){
                        functionList.addInstruction("div.f",toByte(0x27,0,0));
                        stack.pop(StackEnum.DOUBLE);
                    }else
                        throw new Error(stack.top().toString()+" and "+stack.preTop().toString()+" cannot calculate "+getPos());
                }else
                    break;
            }
        }else
            return false;
        return true;
    }
    private boolean mul_div_expr(){
        if(factor()){
            if(stack.stackEnums.size()==0)
                return true;
            String name=stack.top().toString();
            while(t.ifNextToken(TokenType.AS_KW)){
                name=t.expectToken(TokenType.TYPE_KW).getValue().toString();
                if(!name.equals("int")&&!name.equals("double"))
                    throw new Error("cannot identify type ["+name+"] "+getPos());
            }
            if(stack.top().toString().equals(name)){
            }else if(stack.top().toString().equals("int")&&name.equals("double")){
                functionList.addInstruction("int to float",toByte(0x36,0,0));
                stack.pop(StackEnum.INT);
                stack.push(StackEnum.DOUBLE);
            }else if(stack.top().toString().equals("double")&&name.equals("int")){
                functionList.addInstruction("float to int",toByte(0x37,0,0));
                stack.pop(StackEnum.DOUBLE);
                stack.push(StackEnum.INT);
            }
        }else
            return false;
        return true;
    }
    private boolean factor(){
        boolean neg=negate_factor();
        t.savePoint();
        if(group_expr()){

        }else if(call_expr()){

        }else if(ident_expr()){

        }else if(t.ifNextToken(TokenType.INT)){
            functionList.addInstruction("push "+(long)t.getThisToken().getValue(),toByte(0x01,(long)t.getThisToken().getValue(),8));
            stack.push(StackEnum.INT);
        }else if(t.ifNextToken(TokenType.DOUBLE)){
            functionList.addInstruction("push "+(double)t.getThisToken().getValue(),toByte(0x01,(double)t.getThisToken().getValue(),8));
            stack.push(StackEnum.DOUBLE);
        }else if(t.ifNextToken(TokenType.STRING)){
            String s=t.getThisToken().getValue().toString();
            functionList.addVariable(new Variable(null,true,VariableType.STRING,true,false,false,s));
            scope.addGlobalVariable(new Variable(null,true,VariableType.STRING,true,false,false,s));
            int ID=functionList.functions.get(0).params.size();
            functionList.addInstruction("push "+ID,toByte(0x01,ID,8));
            stack.push(StackEnum.INT);
        }else if(t.ifNextToken(TokenType.CHAR)){
            functionList.addInstruction("push "+ (long)(char)t.getThisToken().getValue(),toByte(0x01, (long)(char)t.getThisToken().getValue(),8));
            stack.push(StackEnum.INT);
        }else
            return false;
        t.removePoint();
        if(!neg&&(stack.top()==StackEnum.INT))
            functionList.addInstruction("neg.i",toByte(0x34,0,0));
        else if(!neg&&(stack.top()==StackEnum.DOUBLE))
            functionList.addInstruction("neg.f",toByte(0x35,0,0));
        else if(!neg)
            throw new Error("stackEnum cannot be negated "+getPos());
        return true;
    }
    private boolean negate_factor(){
        boolean b=true;
        while(t.ifNextToken(TokenType.MINUS))b=!b;
        return b;
    }
    private boolean group_expr(){
        if(t.ifNextToken(TokenType.L_PAREN)){
            expectNonTerminal("expr",expr(false));
            t.expectToken(TokenType.R_PAREN);
        }else
            return false;
        return true;
    }
    private boolean call_expr(){
        if(t.ifNextToken(TokenType.IDENT)&&t.ifNextToken(TokenType.L_PAREN)){
            t.loadPoint();
            String name=t.expectToken(TokenType.IDENT).getValue().toString();
            Integer functionID=functionList.searchFunction(name);
            Function function;
            if(functionID!=null)
                function=functionList.functions.get(functionID);
            else
                function=null;
            int expectedParamNum=0,actualParamNum=0;
            if(function==null){
                if(name.equals(stdio[0])||name.equals(stdio[1])||name.equals(stdio[2]))
                    functionList.addInstruction("stackalloc "+1,toByte(0x1a,1,4));
            }else{
                expectedParamNum=function.paramSlot;
                if(function.variableType==VariableType.INT){
                    functionList.addInstruction("stackalloc "+1,toByte(0x1a,1,4));
                    stack.push(StackEnum.INT);
                }else if(function.variableType==VariableType.DOUBLE){
                    functionList.addInstruction("stackalloc "+1,toByte(0x1a,1,4));
                    stack.push(StackEnum.DOUBLE);
                }
            }
            t.expectToken(TokenType.L_PAREN);
            if(!t.ifNextToken(TokenType.R_PAREN)){
                expectNonTerminal("expr",expr(false));
                actualParamNum++;
                while (!t.ifNextToken(TokenType.R_PAREN)){
                    t.expectToken(TokenType.COMMA);
                    expectNonTerminal("expr",expr(false));
                    actualParamNum++;
                }
            }
            if(function==null){
                if((name.equals(stdio[3])||name.equals(stdio[4])||name.equals(stdio[5])||name.equals(stdio[6]))&&actualParamNum!=1)
                    throw new Error("the param num of ["+name+"] is not correct "+getPos());
                else if((name.equals(stdio[0])||name.equals(stdio[1])||name.equals(stdio[2])||name.equals(stdio[7]))&&actualParamNum!=0)
                    throw new Error("the param num of ["+name+"] is not correct "+getPos());
                addStdioFunctionInstruction(name);
            }else{
                if(expectedParamNum!=actualParamNum)
                    throw new Error("the param num of ["+name+"] is not correct "+getPos());
                for(int i=function.params.size()-1;i>0;i--){
                    if(function.params.get(i).variableType.toString().equals(stack.top().toString())){
                        stack.pop(stack.top());
                    }else
                        throw new Error("type of param ["+function.params.get(i).name+"] of function ["+function.name+"] is not correct "+getPos());
                }
                functionList.addInstruction("call "+functionID,toByte(0x48,functionID,4));
            }
        }else{
            t.loadPoint();
            return false;
        }
        return true;
    }
    private boolean ident_expr(){
        if(t.ifNextToken(TokenType.IDENT)){
            String name=t.getThisToken().getValue().toString();
            Variable variable=pushVariableAddress(name);
//                if(!variable.isInitialized)
//                    throw new Error("variable ["+variable.name+"] hasn't been initialized at: row"+Analyser.t.getThisToken().getStartPos().getRow()+" col"+Analyser.t.getThisToken().getStartPos().getCol());
            functionList.addInstruction("load.64",toByte(0x13,0,0));
            stack.pop(StackEnum.ADDR);
            stack.push(variable.variableType);
        }else
            return false;
        return true;
    }
    private void output() throws IOException {
        output.writeInt(0x72303b3e);
        output.writeInt(0x00000001);
        System.out.println("magic: 72303b3e");
        System.out.println("version: 00000001");
        int globalVariableNum=functionList.functions.get(0).params.size();
        output.writeInt(globalVariableNum);
        System.out.println("\nglobalVariableNum: "+globalVariableNum+"\n");
        for(int i=0;i<globalVariableNum;i++){
            Variable variable=functionList.functions.get(0).params.get(i);
            output.writeByte(variable.isConst?1:0);
            System.out.println("isConst: "+(variable.isConst?1+" ":0+" "));
            if(variable.variableType!=VariableType.STRING){
                output.writeInt(4);
                output.writeInt(0);
                System.out.println("VariableLength: "+4);
                System.out.println("VariableValue: "+0);
            }
            else{
                output.writeInt(variable.value.length());
                System.out.println("VariableLength: "+variable.value.length());
                System.out.print("VariableValue:");
                for(int j=0;j<variable.value.length();j++){
                    output.writeByte(variable.value.toCharArray()[j]);
                    System.out.print(" "+variable.value.toCharArray()[j]);
                }
                System.out.println();
            }
        }
        output.writeInt(functionList.functions.size());
        System.out.println("\nFunctionNum: "+functionList.functions.size()+"\n");
        for(int i=0;i<functionList.functions.size();i++){
            Function function=functionList.functions.get(i);
            output.writeInt(i+1);
            output.writeInt(function.returnSlot);
            output.writeInt(function.paramSlot);
            output.writeInt(function.locSlot);
            output.writeInt(function.instructionNum);
            System.out.println("FunctionID: "+i);
            System.out.println("FunctionReturnSlot: "+function.returnSlot);
            System.out.println("FunctionParamSlot: "+function.paramSlot);
            System.out.println("FunctionLocSlot: "+function.locSlot);
            System.out.println("FunctionInstructionNum: "+function.instructionNum);
            System.out.println(function.getInstructionsString());
            ArrayList<Byte>instructions=function.getInstructions();
            for (Byte instruction : instructions) {
                output.write(instruction);
            }
        }
        output.flush();
        output.close();
    }
    private byte[] toByte(int instruction,long num, int length){
        byte[]bytes=new byte[length+1];
        bytes[0]=(byte)instruction;
        for(int i=length-1;i>=0;i--){
            bytes[length-i]=(byte)(num>>(i*8));
        }
        return bytes;
    }
    private byte[] toByte(int instruction,double num, int length){
        return toByte(instruction,Double.doubleToLongBits(num),length);
    }
    private Variable pushVariableAddress(String name){
        Variable variable=scope.getVariable(name);
        if(variable.isGlobal) {
            functionList.addInstruction("global "+variable.offset, toByte(0x0c, variable.offset, 4));
            stack.push(StackEnum.ADDR);
        }else if(variable.isParam){
            functionList.addInstruction("arga "+variable.offset, toByte(0x0b, variable.offset, 4));
            stack.push(StackEnum.ADDR);
        }else if(variable.isLocal){
            functionList.addInstruction("local "+variable.offset, toByte(0x0a, variable.offset, 4));
            stack.push(StackEnum.ADDR);
        }
        return variable;
    }
    private VariableType getVariableType(){
        VariableType variableType;
        t.expectToken(TokenType.COLON);
        Token type=t.expectToken(TokenType.TYPE_KW);
        if(type.getValue().equals("void"))
            throw new Error("Type cannot be void "+getPos());
        else if(type.getValue().equals("int"))
            variableType=VariableType.INT;
        else if(type.getValue().equals("double"))
            variableType=VariableType.DOUBLE;
        else
            throw new Error("cannot identify Variable type "+getPos());
        return variableType;
    }
    private void compare(){
        if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.INT){
            functionList.addInstruction("cmp.i",toByte(0x30,0,0));
            stack.pop(StackEnum.INT,StackEnum.INT);
            stack.push(StackEnum.BOOL);
        }else if(stack.top()==stack.preTop()&&stack.preTop()==StackEnum.DOUBLE){
            functionList.addInstruction("cmp.f",toByte(0x32,0,0));
            stack.pop(StackEnum.DOUBLE,StackEnum.DOUBLE);
            stack.push(StackEnum.BOOL);
        }else
            throw new Error(stack.top().toString()+" and "+stack.preTop().toString()+" cannot calculate "+getPos());
    }
    private void replaceBrInstruction(int start,int num){
        boolean b=br.get(br.size()-1);
        br.remove(br.size()-1);
        if(b)
            functionList.replaceInstruction(start,"notZero "+num,toByte(0x43,num,4));
        else
            functionList.replaceInstruction(start,"isZero "+num,toByte(0x42,num,4));
    }
    public void addStdioFunctionInstruction(String name){
        if(name.equals(stdio[0])){
            functionList.addInstruction("scan.i",toByte(0x50,0,0));
            stack.push(StackEnum.INT);

        }else if(name.equals(stdio[1])){
            functionList.addInstruction("scan.c",toByte(0x51,0,0));
            stack.push(StackEnum.INT);

        }else if(name.equals(stdio[2])){
            functionList.addInstruction("scan.f",toByte(0x52,0,0));
            stack.push(StackEnum.DOUBLE);

        }else if(name.equals(stdio[3])){
            functionList.addInstruction("print.i",toByte(0x54,0,0));
            stack.pop(StackEnum.INT);

        }else if(name.equals(stdio[5])){
            functionList.addInstruction("print.c",toByte(0x55,0,0));
            stack.pop(StackEnum.INT);

        }else if(name.equals(stdio[4])){
            functionList.addInstruction("print.f",toByte(0x56,0,0));
            stack.pop(StackEnum.DOUBLE);

        }else if(name.equals(stdio[6])){
            functionList.addInstruction("print.s",toByte(0x57,0,0));
            stack.pop(StackEnum.INT);

        }else if(name.equals(stdio[7])){
            functionList.addInstruction("println",toByte(0x58,0,0));
        }
    }
    public static String getPos(){
        return "at: row"+t.getThisToken().getStartPos().getRow()+" col"+t.getThisToken().getStartPos().getCol();
    }
}
