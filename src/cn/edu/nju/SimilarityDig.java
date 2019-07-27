package cn.edu.nju;

import cn.edu.nju.logic.AptedMatcher;
import cn.edu.nju.logic.GumTreeMatcher;
import cn.edu.nju.logic.Helper.CalculateSimilarityJob;
import cn.edu.nju.logic.Helper.Notifier;
import cn.edu.nju.logic.RtedMatcher;
import cn.edu.nju.logic.clean.FileChineseCharsetDetector;
import cn.edu.nju.logic.clean.RemoveComment;
import cn.edu.nju.model.UserCodeInfo;
import cn.edu.nju.util.DBUtil;
import cn.edu.nju.util.FileUtil;
import cn.edu.nju.util.PropertiesUtil;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.gen.srcml.SrcmlCppTreeGenerator;
import com.github.gumtreediff.matchers.optimal.rted.RtedAlgorithm;
import com.github.gumtreediff.tree.TreeContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    public SimilarityDig(String fileLocation, boolean isFinal){
        this.fileLocation = fileLocation;
        init(isFinal);
    }

    private void init(){
        File dir = new File(fileLocation);
        FileChineseCharsetDetector fileChineseCharsetDetector = new FileChineseCharsetDetector();
        SrcmlCppTreeGenerator srcmlCppTreeGenerator = new SrcmlCppTreeGenerator();
        for (File file: dir.listFiles()){
            if (!file.getName().contains(".cpp") && !file.getName().contains(".source")){
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
            boolean isGet = false;
            int count = 0;
            while (!isGet){
                if (count > 10){
                    System.out.println("ITREE_INIT_FAILED USER_ID: " + user_id);
                    isGet = true;
                }
                try {
                    TreeContext srcContext = srcmlCppTreeGenerator.generateFromString(content);
                    RemoveComment.purifyTree(srcContext.getRoot(),srcContext);
                    gum.setTree(srcContext);
                    isGet = true;
                    count = 0;
                } catch (Exception e) {
//                e.printStackTrace();
                    count++;
                }
            }

            contents.add(gum);
        }
    }

    private void init(boolean finalCode){
        if (!finalCode){
            init();
            return;
        }

        Map<Integer, String> info = getFinalCode(Integer.parseInt(PropertiesUtil.getProperties("examId")));
        SrcmlCppTreeGenerator srcmlCppTreeGenerator = new SrcmlCppTreeGenerator();
        for (int userId: info.keySet()){
            UserCodeInfo userCodeInfo = new UserCodeInfo("testCpp" + userId, info.get(userId), "");
            try {
                TreeContext srcContext = srcmlCppTreeGenerator.generateFromString(info.get(userId));
                RemoveComment.purifyTree(srcContext.getRoot(),srcContext);
                userCodeInfo.setTree(srcContext);
            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println("ITREE_INIT_FAILED USER_ID: " + userId);
                continue;
            }

            contents.add(userCodeInfo);
        }
    }
    private Map<Integer, String> getFinalCode(int examId){
        Map<Integer, String> location = getFinalCodeLocation(examId);
        FileChineseCharsetDetector fileChineseCharsetDetector = new FileChineseCharsetDetector();
        Map<Integer, String> result = new HashMap<>();
        for (int userId: location.keySet()){
            String loc = location.get(userId);
            File finalBuildDir = new File(loc);
            StringBuilder codeContent = new StringBuilder("");
            for (File question: finalBuildDir.listFiles()){
                if (question.isFile()){
                    continue;
                }

                if (!question.getName().contains("Q")){
                    continue;
                }

                for (File finaCodeFile: question.listFiles()){
                    if (finaCodeFile.isFile()){
                        continue;
                    }

                    for (File code: finaCodeFile.listFiles()){
                        if (code.isDirectory()){
                            continue;
                        }


                        String encoding = "UTF-8";
                        try {
                            encoding = fileChineseCharsetDetector.guessFileEncoding(code);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        codeContent.append(FileUtil.readFile(code, encoding)).append("\r\n");
                    }
                }
            }
            result.put(userId, codeContent.toString());
        }
        return result;
    }
    private Map<Integer, String> getFinalCodeLocation(int examId){
        String path = fileLocation;
        File root = new File(path);
        Map<Integer, String> result = new HashMap<>();
        for (File exam: root.listFiles()){
            if (exam.isFile()){
                continue;
            }

            if (!exam.getName().equals("testCpp" + examId)){
                continue;
            }
            File results = new File(exam.getAbsolutePath() + "\\Results");
            for (File user: results.listFiles()){
                if (user.isFile()){
                    continue;
                }
                String prefix = "\\File\\build_files";
                String buildPath = user.getAbsolutePath() + prefix;

                File build = new File(buildPath);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                try {
                    Date maxDate = df.parse("1970-01-01-00-00-00");
                    String date = null;
                    if (!build.exists() || build.listFiles() == null){
                        continue;
                    }
                    for (File buildDir: build.listFiles()){
                        Date current = df.parse(buildDir.getName());
                        if (current.after(maxDate)){
                            maxDate = current;
                            date = buildDir.getAbsolutePath();
                        }
                    }
                    result.put(Integer.parseInt(user.getName().substring(7)), date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    private void getMatrix(String outputLocation, String dbLocation){
        StringBuilder info = new StringBuilder("user_id");
        double result[][] = new double[contents.size()][contents.size()];
        Notifier notifier = new Notifier(contents.size());

//        Connection connection = DBUtil.getSqliteDBConnection(dbLocation);
//        Statement statement = null;
//        ResultSet resultSet = null;
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
            info.append(",").append(content.getUser_id());
        }
        info.append("\n");
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);

//        try {
            for (int i = 0; i < contents.size(); i++){
                info.append(contents.get(i).getUser_id());
                for (int j = i + 1; j < contents.size(); j++){
                    info.append(",").append(String.valueOf(result[i][j]));
//                        statement = connection.createStatement();
//                        String sql = "insert into similarity(user_id, compare_user_id, exam_id, action, similarity) " +
//                                "values (" +Integer.valueOf(contents.get(i).getUser_id().substring(7)) +
//                                ", " + Integer.valueOf(contents.get(j).getUser_id().substring(7)) +
//                                ", " + Integer.valueOf(PropertiesUtil.getProperties("examId")) +
//                                ", 'build', '" + nf.format(result[i][j]) + "')";
//                        System.out.println("affect " + statement.executeUpdate(sql) + " row");
                }
                info.append("\n");
            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            DBUtil.elegantlyClose(connection, s tatement, resultSet);
//        }
        FileUtil.writeFile(info .toString(), outputLocation, false);
    }

    private void  getMatrixWithMultiThread(String outputLocation){
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
        String fileLocation = "D:\\test";
//        for (int i = 3; i < 4; i++){
            SimilarityDig sd = new SimilarityDig(fileLocation);
            String outputFile = fileLocation + "\\gumtree.csv";
            sd.getMatrixWithMultiThread(outputFile);
//        }
    }
}
