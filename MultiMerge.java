//package src;

import java.util.ArrayList;

public class MultiMerge {
    private ArrayList<ArrayList<Record>> blocks;
    public MultiMerge(ArrayList<Record> blockhold)
    {
        blocks.add(blockhold);
    }
    public void addBlock(ArrayList<Record> blockhold)
    {
        blocks.add(blockhold);
    }
    public void merge()
    {
        int[] position= new int[blocks.size()];
        ArrayList<ArrayList<Record>> holdBlocks=new ArrayList<>();
        ArrayList<Record> curBlock=new ArrayList<>();
        for(int i=0; i<512*blocks.size();i++) {
            if(i%512==0) {
                holdBlocks.add(curBlock);
                curBlock.clear();
            }
            double max=blocks.get(0).get(position[0]).getKey();
            int qHold=0;
            for(int q=1; q<blocks.size();q++)
            {
                if (blocks.get(q).get(position[q]).getKey()>max)
                {
                    max=blocks.get(q).get(position[q]).getKey();
                    qHold=q;
                }
            }
            curBlock.add(blocks.get(qHold).get(position[qHold]));
        }
        blocks=holdBlocks;
    }
}