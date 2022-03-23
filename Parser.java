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
        int k = 0;
        while(k < 6596){
            System.out.println(k);
            getBlock(0,k);
            k += 512;}


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
                System.out.println("runlength " + runsInfo.get(i).getRunLength());
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
        int k = 0;
        while(k < 6596){
            System.out.println(k);
            getBlock(0,k);
            k += 512;}


    }

    public ArrayList<Record> getBlock(int runNumber, int index){
        ArrayList<Record> Block = new ArrayList<>();
        allRuns.get(runNumber);
        int length = runsInfo.get(runNumber).getRunLength();
        if ( 8192 > (length - (index * 16))){
            System.out.println("less than 512");
            //go to end of the run
            for (int i = 0; i < ((length - (index* 16)) / 16); i++){
                Block.add(allRuns.get(runNumber).get(index + i));
            }
        } else {
            for (int i = 0; i < 512; i++){
                Block.add(allRuns.get(runNumber).get(index + i));
            }
        }
        return Block;
    }


    public void merge(int StartRunNum,int EndRunNum) {
        ArrayList<ArrayList<Record>> blocks = new ArrayList<>();
        ArrayList<Integer> position = new ArrayList<>();
        int totalRecords = 0;
        for (int i = StartRunNum; i < EndRunNum; i++) {
            totalRecords = totalRecords + runsInfo.get(i).getRunLength() / 16;
            blocks.add(getBlock(runsInfo.get(i).getStart(), runsInfo.get(i).getRunLength()));
        }
        ArrayList<Record> curBlock = new ArrayList<>();
        for (int j = 0; j < totalRecords; j++) {
            if (j % 512 == 0) { //512 might b wrong bc j = 0
                printToFile(curBlock);
                curBlock.clear();
            }
            int holdNum = position.get(0);
            if (position.get(0) + runsInfo.get(0).getStart() == runsInfo.get(0).getRunLength()) {
                blocks.remove(0);
            } else if (position.get(0) == blocks.get(0).size()) {
                getBlock(runsInfo.get(0).getStart() + j, runsInfo.get(0).getRunLength());
                int holdStart = runsInfo.get(0).getStart();
                runsInfo.get(0).setStart(position.get(0) + holdStart);
                position.set(0, 0);
            } else {
                int qHold = 0;
                double min = blocks.get(0).get(position.get(0)).getKey();

                for (int q = 1; q < blocks.size(); q++) {
                    if (position.get(q) + runsInfo.get(q).getStart() == runsInfo.get(q).getRunLength()) {
                        blocks.remove(q);
                    } else if (position.get(q) == blocks.get(q).size()) {
                        getBlock(runsInfo.get(q).getStart() + j, runsInfo.get(q).getRunLength());
                        int holdStart = runsInfo.get(q).getStart();
                        runsInfo.get(q).setStart(position.get(q) + holdStart);
                        position.set(q, 0);
                    } else if (blocks.get(q).get(position.get(q)).getKey() < min) {
                        min = blocks.get(q).get(position.get(q)).getKey();
                        qHold = q;
                    }
                }
                Record holdRec = new Record(blocks.get(qHold).get(position.get(qHold)).getWholeRecord());
                curBlock.add(holdRec);
            }
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
        ArrayList<MergeInfo> NewRunInfo=new ArrayList<MergeInfo>();
        int runPointer  = 0;
        int numRuns=runsInfo.size();
        int i=0;
        while (numRuns%8!=0){
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
            merge(0,numRuns);
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