package cn.edu.nju.logic;

import com.github.gumtreediff.matchers.optimal.rted.RtedAlgorithm;
import com.github.gumtreediff.tree.ITree;

public class RtedMatcher {
    public static double getSimilarity(ITree src, ITree dst){
        RtedAlgorithm rtedAlgorithm = new RtedAlgorithm(1D,1D,1D);
        double diff = rtedAlgorithm.nonNormalizedTreeDist(src,dst);

        int total = src.getDescendants().size() + dst.getDescendants().size();
        return 1-(double)diff/ (double)total;
    }
}
