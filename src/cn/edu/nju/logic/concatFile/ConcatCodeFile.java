package cn.edu.nju.logic.concatFile;

import cn.edu.nju.util.FileUtil;
import cn.edu.nju.util.PropertiesUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConcatCodeFile {

    public static void concatCodeFile(){
        String concatRootPath = PropertiesUtil.getProperties("concatRoot");
        String concatOutputRootPath = PropertiesUtil.getProperties("concatOutputRoot");

        String[] targetSuffixs = {".h",".cpp"};

        File rootDir = new File(concatRootPath);

        if (!rootDir.exists() ||rootDir.isFile()) {
            System.out.println("根目录不存在 ： " + concatRootPath);
            return;
        }

        for(File userRoot : rootDir.listFiles()){
            if(userRoot.isFile()){
                System.out.println("用户目录异常 ： " + userRoot.getAbsolutePath());
                continue;
            }

            for(File dateTimeRoot : userRoot.listFiles()){
                if(dateTimeRoot.isFile()){
                    System.out.println("用户代码目录异常 ： " + dateTimeRoot.getAbsolutePath());
                    continue;
                }

                //找到代码文件
                List<File> targetFiles = new ArrayList<>();
                for(File possibleFile : dateTimeRoot.listFiles()){
                    if(!possibleFile.isFile()){
                        continue;
                    }
                    boolean isNeed = false;
                    for(String suffix : targetSuffixs){
                        if(possibleFile.getName().endsWith(suffix)){
                            isNeed = true;
                            break;
                        }
                    }
                    if(isNeed){
                        targetFiles.add(possibleFile);
                    }
                }

                if(targetFiles.size() == 0){
                    System.out.println("用户目录无代码 ： " + dateTimeRoot.getAbsolutePath());
                    continue;
                }

                String targetFilePath = concatOutputRootPath + "\\" + userRoot.getName()+"-"+dateTimeRoot.getName() + ".source";

                for(File targetFile : targetFiles){
                    String content = FileUtil.readFile(targetFile);
                    FileUtil.writeFile(content,targetFilePath,true);
                }
            }
        }
    }


    public static void main(String[] args){
        concatCodeFile();
    }
}
