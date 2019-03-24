package cn.edu.nju.logic;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import java.io.IOException;
import java.util.List;

public class GumTreeMatcher extends Matcher {


    public GumTreeMatcher(ITree src, ITree dst, MappingStore mappings) {
        super(src, dst, mappings);
    }

    private GumTreeMatcher(Matcher matcher){
        super(matcher.getSrc(), matcher.getDst(), matcher.getMappings());
    }

    private double getSimilarity(){
        return diceSimilarity(src, dst);
    }

    public static double getSimilarity(ITree src, ITree dst){
        Matcher matcher = Matchers.getInstance().getMatcher(src, dst);
        matcher.match();
        GumTreeMatcher run = new GumTreeMatcher(matcher);
        return run.getSimilarity();
    }

    public static void main(String[] args) {

        Run.initGenerators();
        String file1 = "E:\\jar\\gumtree-2.1.2\\bin\\exam1-60.cpp";
        String file2 = "E:\\jar\\gumtree-2.1.2\\bin\\exam1-567.cpp";
        try {
            TreeContext srcContext = Generators.getInstance().getTree(file1);
            TreeContext dstContext = Generators.getInstance().getTree(file2);
            ITree src = srcContext.getRoot();
            ITree dst = dstContext.getRoot();
            Matcher matcher = Matchers.getInstance().getMatcher(src, dst);
            matcher.match();
            GumTreeMatcher run = new GumTreeMatcher(matcher);
            System.out.println(run.getSimilarity());

            MappingStore store = matcher.getMappings();
            ActionGenerator actionGenerator = new ActionGenerator(src, dst, matcher.getMappings());
            actionGenerator.generate();
            List<Action> actionList = actionGenerator.getActions();
            TreeIoUtils.TreeSerializer treeSerializer = TreeIoUtils.toLisp(srcContext);
            treeSerializer.writeTo(System.out);
            System.out.println(TreeIoUtils.toLisp(srcContext).toString());
//
//            double insert, delete, update, move, match;
//            insert = delete = update = move = match = 0;
//            for (Action action: actionList){
////                System.out.println("action: " + action.getName());
//                if (action.getName().contains("INS")) insert++;
//                else if (action.getName().contains("DEL")) delete++;
//                else if (action.getName().contains("MOV")) move++;
//                else if (action.getName().contains("UPD")) update++;
//                else System.out.println("UNKNOWN_ACTION");
//            }
//
//            Iterator<Mapping> mappingIterator = store.iterator();
//            while (mappingIterator.hasNext()){
//                mappingIterator.next();
//                match++;
//            }
//
//            System.out.println((update + delete + insert)/ (update + delete + insert + move + match*2));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void match() {

    }
}
