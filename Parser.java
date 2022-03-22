//package src;
/*
You parse the information and read data from the bin files
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Parser {
    private RandomAccessFile mergeFile;
    private RandomAccessFile firstMergeFile;
    private ArrayList<MergeInfo> runsInfo;
    private ArrayList<Record> run;
    private ArrayList<ArrayList<Record>> allRuns;
    private boolean next;

    // the constructor of parser and you can add more here if
    // you need to
    public Parser() throws IOException, FileNotFoundException {
        mergeFile = new RandomAccessFile("Merge.bin", "rw");
        firstMergeFile = new RandomAccessFile("mergeFirst.bin", "rw");
        runsInfo = new ArrayList<MergeInfo>();
        allRuns = new ArrayList<>();
        next = true;
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
    }

    public void run(){

    }
}





// merge run gets called runs.size()/8 =numofmerges
//for(i =0; i <numMerges; i__)
//mergeRun(i*8, 8)
//have a variable leftovoermerge that saves %8
//if leftovermerge isnt 0 then call merge run again
//mergerun(numofMerges * 8, leftovermerge) starts where the other loop ended and goes till the leftovermerge is completed
