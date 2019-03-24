package cn.edu.nju.logic;

import cn.edu.nju.model.UserCodeInfo;
import cn.edu.nju.util.FileUtil;
import cn.edu.nju.util.PropertiesUtil;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.tree.TreeContext;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class SimilarityDig {
    private List<UserCodeInfo> contents = new ArrayList<>();
    private String fileLocation;

    public SimilarityDig(String fileLocation){
        this.fileLocation = fileLocation;
        init();
    }

    private void init(){
        Run.initGenerators();
        File dir = new File(fileLocation);
        for (File file: dir.listFiles()){
            if (!file.getName().contains(".cpp")){
                continue;
            }

            String content = FileUtil.readFile(file);
            String user_id = file.getName().substring(0, file.getName().indexOf("_"));
            String time = file.getName().substring(file.getName().indexOf("_") + 1);

            UserCodeInfo gum = new UserCodeInfo(user_id, content, time);
            try {
                TreeContext srcContext = Generators.getInstance().getTree(file.getAbsolutePath());
                gum.setTree(srcContext.getRoot());
            } catch (Exception e) {
                System.out.println("ITREE_INIT_FAILED USER_ID: " + user_id);
                continue;
            }

            contents.add(gum);
        }
    }

    private void getMatrix(String outputLocation){
        StringBuilder info = new StringBuilder("user_id,");
        double result[][] = new double[contents.size()][contents.size()];
//        NumberFormat nf = NumberFormat.getPercentInstance();
//        nf.setMinimumFractionDigits(2);
        for (UserCodeInfo content : contents) {
            info.append(content.getUser_id()).append(",");
        }
        info.append("\n");

        for (int i = 0; i < contents.size(); i++){
            for (int j = i; j < contents.size(); j++){
                if (i == j){
                    result[i][j] = 1.0;
                } else {
                    result[i][j] = result[j][i] = GumTreeMatcher.getSimilarity(contents.get(i).getTree(), contents.get(j).getTree());
                }
            }
        }

        for (int i = 0; i < contents.size(); i++){
            info.append(contents.get(i).getUser_id()).append(",");
            for (int j = 0; j < contents.size(); j++){
                info.append(String.valueOf(result[i][j])).append(",");
            }
            info.append("\n");
        }
        FileUtil.writeFile(info.toString(), outputLocation, true);
    }

//    public void recover(){
//        File file = new File(fileLocation + "\\similarity.csv");
//        BufferedReader
//
//    }
    public static void main(String[] args) {
        String fileLocation = PropertiesUtil.getProperties("cppFileLocation");
        String outputFile = PropertiesUtil.getProperties("outputFile");
        SimilarityDig sd = new SimilarityDig(fileLocation);
        sd.getMatrix(outputFile);
    }
}
