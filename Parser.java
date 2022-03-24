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

    // CHECK INDEX BEFORE PASSED
    public ArrayList<Record> getBlock(int runNumber, int index){
        ArrayList<Record> Block = new ArrayList<>();

        // gets length of entire run
        int length = runsInfo.get(runNumber).getRunLength();

        // check if in last block
        if ( index * 16 + 8192 > length){
            // if yes: get records to end of run and add to block array list
            for (int i = 0; i < ((length/16) - index) ; i++){ Block.add(allRuns.get(runNumber).get(index + i)); }
        }

        // if not in last block, get 512 records and add to array list
        else {
            for (int i = 0; i < 512; i++) { Block.add(allRuns.get(runNumber).get(index + i)); }
        }
        return Block;
    }


    public void merge(int StartRunNum,int EndRunNum) {
        // makes array lists
        ArrayList<ArrayList<Record>> blocks = new ArrayList<>();
        // position in the blocks array
        ArrayList<Integer> position = new ArrayList<>();
        int qHold=0;
        // fills array list with 0 as many times as EndRunNum-StartRunNum
        for(int l=0;l< EndRunNum-StartRunNum;l++)
        {
            position.add(runsInfo.get(l).getStart());
        }
        int totalRecords = 0;
        // gets one block from each run and adds to block arraylist
        for (int i = StartRunNum; i < EndRunNum; i++) {
            totalRecords = totalRecords + runsInfo.get(i).getRunLength() / 16;
            blocks.add(getBlock(i, runsInfo.get(i).getStart()));
        }
        System.out.println("total Records " + totalRecords);
        ArrayList<Record> curBlock = new ArrayList<>();
        for (int j = 0; j < totalRecords; j++) {
            if (j % 512 == 0) { //512 might b wrong bc j = 0
                printToFile(curBlock);
                curBlock.clear();
            }
            boolean hasMin=false;
            Record min=null;
            for(int q= 0; q<blocks.size();q++)
            {
                if(position.get(q)==runsInfo.get(q+StartRunNum).getRunLength()){
                    blocks.set(q,null);
                }
                else if(blocks.get(q)==null||blocks.get(q).size()==0)
                {
                    q++;
                }
                else if(position.get(q)%512==0){
                   blocks.set(q,getBlock(StartRunNum+q,position.get(q)));
                }
                else if(hasMin==false)
                {

                    min=new Record(blocks.get(q).get(position.get(q)%512-1).getWholeRecord());
                    qHold=q;
                    q++;
                }
                else if(hasMin==true&&blocks.get(q).get(position.get(q)%512-1).compareTo(min)==-1)
                {
                    min=new Record(blocks.get(q).get(position.get(q)%512-1).getWholeRecord());
                    qHold=q;
                }
            }
            if(min!=null) {
                curBlock.add(min);
                int HoldPos = position.get(qHold);
                position.set(qHold, HoldPos + 1);
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
        // decides which file to write to

        // runs as long as runsInfo has more than one run
        while(runsInfo.size() > 1){
            ArrayList<MergeInfo> runsInfoTwo = new ArrayList<MergeInfo>();
            // checks for groups of 8 runs
            int numOfMerges = runsInfo.size()/8;
            // checks for left over runs
            int leftOverMerge = runsInfo.size()%8;

            // calls merge for as many times as groups of 8 runs
            for(int i = 0; i < numOfMerges; i++){
                merge(i*8, (i*8)+8);
                int newLen = 0;
                for(int j = i*8; j < (i*8)+8; j++){
                    newLen = newLen + runsInfo.get(j).getRunLength();
                }
                runsInfoTwo.add(new MergeInfo(runsInfo.get(i*8).getStart(),newLen));
            }

            // calls merge for left over runs
            if(leftOverMerge != 0){
                merge(numOfMerges*8, (numOfMerges*8) + leftOverMerge);
                int newLen = 0;
                for(int i = numOfMerges*8; i < (numOfMerges*8) + leftOverMerge; i++){
                    newLen = newLen + runsInfo.get(i).getRunLength();
                }
                runsInfoTwo.add(new MergeInfo(runsInfo.get(numOfMerges*8).getStart(),newLen));
                numOfMerges++;
            }

            runsInfo = runsInfoTwo;
        }
    }
}