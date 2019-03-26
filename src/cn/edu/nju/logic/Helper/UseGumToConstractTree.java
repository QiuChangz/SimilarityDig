package cn.edu.nju.logic.Helper;

import com.github.gumtreediff.gen.srcml.SrcmlCppTreeGenerator;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;

import java.io.IOException;
import java.io.StringWriter;

public class UseGumToConstractTree {
    public static void writeTree(final BracketFormatter formatter, ITree root) throws Exception {

        TreeUtils.visitTree(root, new TreeUtils.TreeVisitor() {
            public void startTree(ITree tree) {
                try {
                    formatter.startTree(tree);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            public void endTree(ITree tree) {
                try {
                    formatter.endTree(tree);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static String produceBracketTreeStringFromFile(String filePath){
        TreeContext treeContextA = null;
        StringWriter stringWriter = null;
        try {
            treeContextA = new SrcmlCppTreeGenerator().generateFromFile(filePath);
            stringWriter = new StringWriter();

            BracketFormatter bracketFormatter = new BracketFormatter(stringWriter,treeContextA);

            writeTree(bracketFormatter, treeContextA.getRoot());

            stringWriter.close();

            return stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(stringWriter != null){
                try {
                    stringWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    public static String produceBracketTreeStringFromTC(TreeContext treeContext){
        StringWriter stringWriter = null;
        try {
            stringWriter = new StringWriter();
            BracketFormatter bracketFormatter = new BracketFormatter(stringWriter,treeContext);
            writeTree(bracketFormatter, treeContext.getRoot());
            stringWriter.close();
            return stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(stringWriter != null){
                try {
                    stringWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    public static void main(String[] args) throws Exception {
        String filePathA = "C:\\Users\\yuegu\\Desktop\\testGUM\\g.cpp";

        TreeContext treeContextA = new SrcmlCppTreeGenerator().generateFromFile(filePathA);

        StringWriter stringWriter = new StringWriter();
        BracketFormatter bracketFormatter = new BracketFormatter(stringWriter,treeContextA);

        writeTree(bracketFormatter, treeContextA.getRoot());

        stringWriter.close();

        System.out.println(stringWriter.toString());

        System.out.println(treeContextA.toString());

    }

}
