package cn.edu.nju;

import cn.edu.nju.logic.AptedMatcher;
import cn.edu.nju.logic.GumTreeMatcher;
import cn.edu.nju.logic.Helper.CalculateSimilarityJob;
import cn.edu.nju.logic.Helper.Notifier;
import cn.edu.nju.logic.RtedMatcher;
import cn.edu.nju.logic.clean.FileChineseCharsetDetector;
import cn.edu.nju.logic.clean.RemoveComment;
import cn.edu.nju.model.UserCodeInfo;
import cn.edu.nju.util.FileUtil;
import cn.edu.nju.util.PropertiesUtil;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.gen.srcml.SrcmlCppTreeGenerator;
import com.github.gumtreediff.matchers.optimal.rted.RtedAlgorithm;
import com.github.gumtreediff.tree.TreeContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

public class SimilarityDig {
    private List<UserCodeInfo> contents = new ArrayList<>();
    private String fileLocation;

    public SimilarityDig(String fileLocation){
        this.fileLocation = fileLocation;
        init();
    }

    private void init(){
        File dir = new File(fileLocation);
        FileChineseCharsetDetector fileChineseCharsetDetector = new FileChineseCharsetDetector();
        SrcmlCppTreeGenerator srcmlCppTreeGenerator = new SrcmlCppTreeGenerator();
        for (File file: dir.listFiles()){
            if (!file.getName().contains(".cpp")){
                continue;
            }

            String encoding = "UTF-8";
            try {
                encoding = fileChineseCharsetDetector.guessFileEncoding(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String content = FileUtil.readFile(file ,encoding);
            String user_id = file.getName().substring(0, file.getName().indexOf("_"));
            String time = file.getName().substring(file.getName().indexOf("_") + 1);

            UserCodeInfo gum = new UserCodeInfo(user_id, content, time);
            try {
                TreeContext srcContext = srcmlCppTreeGenerator.generateFromString(content);
                RemoveComment.purifyTree(srcContext.getRoot(),srcContext);
                gum.setTree(srcContext);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("ITREE_INIT_FAILED USER_ID: " + user_id);
                continue;
            }

            contents.add(gum);
        }
    }

    private void getMatrix(String outputLocation){
        StringBuilder info = new StringBuilder("user_id,");
        double result[][] = new double[contents.size()][contents.size()];
        Notifier notifier = new Notifier(contents.size());
        for (int i = 0; i < contents.size(); i++){
            for (int j = i; j < contents.size(); j++){
                if (i == j){
                    result[i][j] = 1.0;
                } else {
//                    result[i][j] = result[j][i] = GumTreeMatcher.getSimilarity(contents.get(i).getTree(), contents.get(j).getTree());
//                    result[i][j] = result[j][i] = RtedMatcher.getSimilarity(contents.get(i).getTree(), contents.get(j).getTree());
                    result[i][j] = result[j][i] = AptedMatcher.getSimilarity(contents.get(i).getTreeContext(), contents.get(j).getTreeContext());
                }
            }
            notifier.notifyFinish();
        }

        for (UserCodeInfo content : contents) {
            info.append(content.getUser_id()).append(",");
        }
        info.append("\n");
        for (int i = 0; i < contents.size(); i++){
            info.append(contents.get(i).getUser_id()).append(",");
            for (int j = 0; j < contents.size(); j++){
                info.append(String.valueOf(result[i][j])).append(",");
            }
            info.append("\n");
        }
        FileUtil.writeFile(info.toString(), outputLocation, false);
    }

    private void getMatrixWithMultiThread(String outputLocation){
        StringBuilder info = new StringBuilder("user_id,");
        double result[][] = new double[contents.size()][contents.size()];

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(6);
        Notifier notifier = new Notifier(contents.size());


        for (int i = 0; i < contents.size(); i++){
            for (int j = i; j < contents.size(); j++){
                if (i == j){
                    result[i][j] = 1.0;
                } else {
                   fixedThreadPool.execute(new CalculateSimilarityJob(notifier,i,j,contents.get(i).getTreeContext(),contents.get(j).getTreeContext(),result));
                }
            }
        }

        fixedThreadPool.shutdown();

        try {
            fixedThreadPool.awaitTermination(Long.MAX_VALUE , SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (UserCodeInfo content : contents) {
            info.append(content.getUser_id()).append(",");
        }
        info.append("\n");
        for (int i = 0; i < contents.size(); i++){
            info.append(contents.get(i).getUser_id()).append(",");
            for (int j = 0; j < contents.size(); j++){
                info.append(String.valueOf(result[i][j])).append(",");
            }
            info.append("\n");
        }
        FileUtil.writeFile(info.toString(), outputLocation, false);
    }

    public static void main(String[] args) {
        String fileLocation = PropertiesUtil.getProperties("cppFileLocation");
        String outputFile = PropertiesUtil.getProperties("outputFile");
        SimilarityDig sd = new SimilarityDig(fileLocation);
        sd.getMatrixWithMultiThread(outputFile);
    }
}
