package miniplc0java.Analyser;

public class BreakPoint {
    int instructionID;
    boolean isBreak;
    public BreakPoint(int instructionID,boolean isBreak){
        this.instructionID=instructionID;
        this.isBreak=isBreak;
    }
}
