import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class Parser {
    private RandomAccessFile mergeFile;
    private RandomAccessFile HoldFIle;
    private RandomAccessFile firstMergeFile;
    private ArrayList<MergeInfo> runsInfo;
    private ArrayList<Record> run;
    private ArrayList<ArrayList<Record>> allRuns;
    private boolean next;
    private int numMerges;

    // the constructor of parser and you can add more here if
    // you need to
    public Parser() throws IOException, FileNotFoundException {
        mergeFile = new RandomAccessFile("Merge.bin", "rw");
        firstMergeFile = new RandomAccessFile("mergeFirst.bin", "rw");
        HoldFIle= new RandomAccessFile("HoldFile.bin","rw");
        runsInfo = new ArrayList<MergeInfo>();
        allRuns = new ArrayList<>();
        next = true;
        numMerges=runsInfo.size();
    }

    public void parseFile(String fileToParse, String infoToParse)
            throws IOException,
            FileNotFoundException {
        RandomAccessFile ifrd = new RandomAccessFile(infoToParse, "r");

        for (int i = 0; i < ifrd.length()/8; i++){
            int start = ifrd.readInt();
            int runLength = ifrd.readInt();
            runsInfo.add(new MergeInfo(start, runLength));
        }

        // executes while there is a next run (at least always 2)
        RandomAccessFile raf = new RandomAccessFile(fileToParse, "r");
        int i = 0;
        while(next){
            //raf.read();

            if( i == runsInfo.size()){
                next = false;
            }

            else {
                int numRuns = runsInfo.size();
                run = new ArrayList<Record>();
                int j;
                System.out.println("runlength " + runsInfo.get(i).getRunLength());
                System.out.println("i " + i);
                for(j = 0; j <= runsInfo.get(i).getRunLength(); j += 16){

                    byte[] bytesToRead = new byte[16];
                    raf.read(bytesToRead);
                    Record newRecord = new Record(bytesToRead);
                    run.add(newRecord);
                    //   System.out.print(newRecord.toString());
                    //
                    //if (i == runs.get(i).getRunLength()){ //end of the run
                    //break;
                    // }
                }
                allRuns.add(run);
                System.out.println("j " + j);
                System.out.println("run size " + run.size());
                // MultiMerge merger = new MultiMerge(runArray);
            }

            i++;
        }
        System.out.println("All Run Size " + allRuns.size());
        /*
        Since start point and length are both integers, you will
        use readInt here. Remember 1 Integer = 4bytes,
        which enlarge the size of file
         */
        /**int k = 0;
        while(k < 6596){
            System.out.println(k);
            getBlock(0,k);
            k += 512;
        }**/

        run();
    }

    public void parseFileUS(RandomAccessFile fileToParse, ArrayList<MergeInfo> mergeInfoArrayList,int mergeNum) {
        runsInfo=mergeInfoArrayList;
        numMerges=mergeNum;

        // executes while there is a next run (at least always 2)
        RandomAccessFile raf = fileToParse;
        int i = 0;
        while(next){
            //raf.read();

            if( i == runsInfo.size()){
                next = false;
            }

            else {
                int numRuns = runsInfo.size();
                run = new ArrayList<Record>();
                int j;
      //          System.out.println("runlength " + runsInfo.get(i).getRunLength());
                System.out.println("i " + i);
                for(j = 0; j <= runsInfo.get(i).getRunLength(); j += 16){

                    byte[] bytesToRead = new byte[16];
                    try {
                        raf.read(bytesToRead);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Record newRecord = new Record(bytesToRead);
                    run.add(newRecord);
                    //   System.out.print(newRecord.toString());
                    //
                    //if (i == runs.get(i).getRunLength()){ //end of the run
                    //break;
                    // }
                }
                allRuns.add(run);
       //         System.out.println("j " + j);
       //         System.out.println("run size " + run.size());
                // MultiMerge merger = new MultiMerge(runArray);
            }

            i++;
        }
   //     System.out.println("All Run Size " + allRuns.size());
        /*
        Since start point and length are both integers, you will
        use readInt here. Remember 1 Integer = 4bytes,
        which enlarge the size of file
         */
       /** int k = 0;
        while(k < 6596){
            System.out.println(k);
            getBlock(0,k);
            k += 512;}**/

            run();
    }

    public ArrayList<Record> getBlock(int runNumber, int index){
        ArrayList<Record> Block = new ArrayList<>();
      //  System.out.println("Run Number " + runNumber);
        allRuns.get(runNumber);
        int length = runsInfo.get(runNumber).getRunLength();
        if ( index > runsInfo.get(runNumber).getStart() + runsInfo.get(runNumber).getRunLength()){
         //   System.out.println("length " + length);
         //   System.out.println("index " + index);
         //   System.out.println("length-index " + (length - (index)));
         //   System.out.println("less than 512");
            //go to end of the run
            for (int i = 0; i < ((length - (index* 16)) / 16); i++){
                Block.add(allRuns.get(runNumber).get(index + i));
            }
        } else {
            for (int i = 0; i < 512; i++){
                //System.out.println("check i :" + i);
                Block.add(allRuns.get(runNumber).get(index - runsInfo.get(runNumber).getStart() + i));
            }
        }
        return Block;
    }


    public void merge(int StartRunNum,int EndRunNum) {
        // makes array lists
        ArrayList<ArrayList<Record>> blocks = new ArrayList<>();
        // position in the blocks array
        ArrayList<Integer> position = new ArrayList<>();
        int qHold = 0;
        int curRun = 0;

        // fills array list with 0 as many times as EndRunNum-StartRunNum
        for(int l=0;l<=EndRunNum-StartRunNum;l++)
        {
            position.add(0);
        }

        int totalRecords = 0;

   //     System.out.println("startrunnum "+ StartRunNum);
   //     System.out.println("Endrun " + EndRunNum);

        // gets one block from each run and adds to block arraylist
        for (int i = StartRunNum; i <= EndRunNum; i++) {
            System.out.println("i " + i);

            // gets the number of total records in all runs passed to merge
            totalRecords = totalRecords + runsInfo.get(i).getRunLength() / 16;
      //      System.out.println("total records : " + totalRecords);
        //    System.out.println("getBlock : " + runsInfo.get(i).getStart());

            // adds the first block from runNumber i
            blocks.add(getBlock(i, runsInfo.get(i).getStart()));
        }
        System.out.println("total Records " + totalRecords);

        ArrayList<Record> curBlock = new ArrayList<>();

        for (int j = 1; j <= totalRecords; j++) {

            if (j % 512 == 0) { //512 might b wrong bc j = 0
                printToFile(curBlock);
                curBlock.clear();
            }

            // if the pointer for the current run is outside of the length of the run set
            if (position.get(curRun)*16 == runsInfo.get(StartRunNum).getRunLength()) {
                blocks.set(curRun, null);
            }


            // check for if at end of block array
            else if (position.get(curRun) == 512) {
                System.out.println("call 1");
                System.out.println("GetBlock 2 : " + (position.get(StartRunNum) + runsInfo.get(StartRunNum).getStart()));
                blocks.set(curRun,getBlock(StartRunNum+curRun,position.get(curRun)));
                int holdStart = runsInfo.get(StartRunNum+curRun).getStart();
                runsInfo.get(StartRunNum+curRun).setStart(position.get(curRun) + holdStart);
                position.set(curRun, 0);
            } else {

                double min = blocks.get(curRun).get(position.get(curRun)).getKey();

                for (int q = curRun; q < (blocks.size()-curRun); q++) {
                    if (position.get(q) + runsInfo.get(q+StartRunNum).getStart() == runsInfo.get(StartRunNum+q).getRunLength()) {
                        blocks.set(q,null);
                    } else if (position.get(q) == 512) {
                        System.out.println("call 2");
                        System.out.println("GetBlock 3 : " + position.get(q));
                        blocks.set(curRun,getBlock(StartRunNum+curRun,position.get(curRun)));
                        int holdStart = runsInfo.get(StartRunNum+q).getStart();
                        runsInfo.get(StartRunNum+q).setStart(position.get(q) + holdStart);
                        position.set(q, 0);
                    } else if (blocks.get(q).get(position.get(q)).getKey() < min) {
                        min = blocks.get(q).get(position.get(q)).getKey();
                        qHold = q;
                    }
                }
                Record holdRec = new Record(blocks.get(qHold).get(position.get(qHold)).getWholeRecord());
                int holdNum =position.get(qHold)+1;
          //      System.out.println("holdNum " + holdNum);
        //        System.out.println("qHold " + qHold);
                position.set(qHold, holdNum);
                curBlock.add(holdRec);
            }
        }
        if(!curBlock.isEmpty()){
            printToFile(curBlock);
            curBlock.clear();
        }
    }


    public void printToFile(ArrayList<Record> FileToPrint){
        //How to print the first or just know when it is
        if (numMerges%2==0){
            try {
                mergeFile = new RandomAccessFile("Merge.bin", "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            for(int i=0;i<FileToPrint.size();i++)
            {
                try {
                    mergeFile.write(FileToPrint.get(i).getWholeRecord());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //Write to the mergeFile
        }
        else {
            try {
                HoldFIle= new RandomAccessFile("HoldFile.bin","rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            for(int i=0;i<FileToPrint.size();i++)
            {
                try {
                    HoldFIle.write(FileToPrint.get(i).getWholeRecord());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //write to holdFile
        }
    }


    public void run(){
/*
        ArrayList<MergeInfo> NewRunInfo=new ArrayList<MergeInfo>();
        int runPointer  = 0;
        int numRuns=runsInfo.size();
        int i=0;
        while (numRuns%8==0){
            int holdRunPointer=runPointer;
            for(int p =0;p<9&&p<runsInfo.size();p++){
                runPointer=runPointer+runsInfo.get(p).getRunLength();
            }
            NewRunInfo.add(new MergeInfo(holdRunPointer,runPointer-holdRunPointer));
            merge(i,i+8);
            i=i+8;
            numRuns=numRuns-8;
        }
        if(numRuns!=0)
        {
            merge(i,numRuns - 1);
        }
    */
        ArrayList<MergeInfo> NewRunInfo=new ArrayList<MergeInfo>();
        int runPointer  = 0;
        int numRuns=runsInfo.size();
        int beg =0;

        if(allRuns.size() > 8){
        for (beg = 0; beg < allRuns.size(); beg += 8){
            merge(beg, (beg + 7));
        }
        } else {
            int end = allRuns.size() % 8;
            System.out.println("end " + end);
            if (end != 0) {
                merge(beg, (beg + end-1));
            }
        }

        if(numMerges!=0){
            if(numMerges%2==0){
                parseFileUS(mergeFile,NewRunInfo,numMerges%8+1);
            }
            else {
                parseFileUS(HoldFIle,NewRunInfo,numMerges%8+1);
            }
        }
    }
}