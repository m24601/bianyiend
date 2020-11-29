package miniplc0java.Tokenizer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CharReader {
    private Pos pos=new Pos();
    private FileReader fileReader;
    private char thisChar,nextChar;
    public CharReader(File file) throws IOException {
        fileReader=new FileReader(file);
        nextChar=(char)fileReader.read();
        System.out.print(nextChar);
    }
    public char peekChar(){
            return nextChar;
    }
    public char getChar(){
        return thisChar;
    }
    public char nextChar() throws IOException {
        if(thisChar=='\n')
            pos.nextRow();
        else
            pos.nextCol();
        thisChar=nextChar;
        nextChar=(char)fileReader.read();
        System.out.print(nextChar);
        return thisChar;
    }
    public char expectChar(char a) throws IOException {
        if(!ifNextChar(a))
            throw new Error("expect \'"+a+"\'at: row"+pos.getRow()+" col"+pos.getCol());
        return thisChar;
    }
    public char expectChar(char a,char b) throws IOException {
        if(!ifNextChar(a,b))
            throw new Error("expect ["+a+"-"+b+" at: row"+pos.getRow()+" col"+pos.getCol());
        return thisChar;
    }
    public boolean ifNextChar(char a) throws IOException {
        if(nextChar==a){
            nextChar();
            return true;
        }else
            return false;
    }
    public boolean ifNextChar(char a,char b) throws IOException {
        if(a<=nextChar&&nextChar<=b){
            nextChar();
            return true;
        }else
            return false;
    }
    public Pos getPos(){
        return pos;
    }
}
