package miniplc0java.Tokenizer;

public class Pos {
    private int row=0,col=0;
    public Pos(){}
    public Pos(int row,int col){
        this.row=row;
        this.col=col;
    }
    public void nextCol(){
        col=col+1;
    }
    public void nextRow(){
        row=row+1;
        col=0;
    }
    public int getRow(){
        return row;
    }
    public int getCol(){
        return col;
    }
}
