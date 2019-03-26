package cn.edu.nju.logic.clean;

import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.gen.srcml.SrcmlCppTreeGenerator;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;

import java.io.IOException;
import java.util.List;

/**
 * Created by yuegu on 2019/3/24.
 */
public class RemoveComment {
    public static void purifyTree(ITree root,TreeContext treeContext){
        Tree theTree = (Tree) root;
        List<ITree> children = root.getChildren();
        for(int i = 0 ; i < children.size() ; i++ ){
            ITree child = children.get(i);
            if(treeContext.getTypeLabel(child).equals("comment")){
                children.remove(i);
                i--;
            }else{
                purifyTree(child,treeContext);
            }

        }
    }
    public static void main(String[] args) throws IOException {

        String file = "C:\\Users\\yuegu\\Desktop\\test\\a.cpp";
        TreeContext treeContext = new SrcmlCppTreeGenerator().generateFromFile(file);
        System.out.println(treeContext.toString());
        ITree tree = treeContext.getRoot();
        purifyTree(tree,treeContext);
        System.out.println(treeContext.toString());
    }
}
