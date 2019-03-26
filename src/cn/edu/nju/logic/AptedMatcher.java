package cn.edu.nju.logic;

import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.costmodel.PerEditOperationStringNodeDataCostModel;
import at.unisalzburg.dbresearch.apted.distance.APTED;
import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;
import at.unisalzburg.dbresearch.apted.parser.BracketStringInputParser;
import cn.edu.nju.logic.Helper.UseGumToConstractTree;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

public class AptedMatcher {

    static BracketStringInputParser bracketStringInputParser = null;
    static APTED<CostModel,StringNodeData> aptedModel = null;

    public static double getSimilarity(TreeContext src, TreeContext dst){
        String treeAStr = UseGumToConstractTree.produceBracketTreeStringFromTC(src);
        String treeBStr = UseGumToConstractTree.produceBracketTreeStringFromTC(dst);

        if(bracketStringInputParser == null){
            bracketStringInputParser = new BracketStringInputParser();
        }
        Node<StringNodeData> treeA = bracketStringInputParser.fromString(treeAStr);
        Node<StringNodeData> treeB = bracketStringInputParser.fromString(treeBStr);

        if(aptedModel == null){
            aptedModel = new APTED<>(new PerEditOperationStringNodeDataCostModel(1f,1f,1f));
        }

        float editDistance = aptedModel.computeEditDistance(treeA,treeB);
        return 1 - (double)editDistance/(treeA.getNodeCount()+treeB.getNodeCount());
    }

}
