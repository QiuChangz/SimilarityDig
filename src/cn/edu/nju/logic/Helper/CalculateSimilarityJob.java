package cn.edu.nju.logic.Helper;

import cn.edu.nju.logic.GumTreeMatcher;
import com.github.gumtreediff.tree.TreeContext;
import com.helger.commons.callback.IThrowingRunnable;

public class CalculateSimilarityJob implements Runnable{
    private Notifier notifier;

    private int i,j;
    private TreeContext treeContextA;
    private TreeContext treeContextB;
    private double[][] metrix;

    public CalculateSimilarityJob(Notifier notifier, int i, int j, TreeContext treeContextA, TreeContext treeContextB, double[][] metrix) {
        this.notifier = notifier;
        this.i = i;
        this.j = j;
        this.treeContextA = treeContextA;
        this.treeContextB = treeContextB;
        this.metrix = metrix;
    }

    @Override
    public void run() {
        metrix[i][j] = metrix[j][i] = GumTreeMatcher.getSimilarity(treeContextA.getRoot(), treeContextB.getRoot());

        notifier.notifyFinish();
    }
}
