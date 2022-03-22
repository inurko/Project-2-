//package src;

public class MergeInfo {
    private int start;
    private int runLength;

    // merge info object constructor
    public MergeInfo(int start, int runLength){
        this.start = start;
        this.runLength = runLength;
    }

    // getters
    public int getStart(){ return start; }
    public int getRunLength() { return runLength; }

    //setters
    public void setStart(int start){ this.start = start; }
    public void setRunLength(int runLength) { this.runLength = runLength; }


}
