package cn.edu.nju.util;

import java.io.*;

public class FileUtil {

    private static String encoding = "gbk";

    static void writeFile(String content, String path){
        writeFile(content, path, false);
    }

    public static void writeFile(String content, String path, boolean add){
        File file = new File(path);
        try {
            if (!file.exists()){
                if(!file.createNewFile()){
                    return;
                }
            }
            FileWriter writer = new FileWriter(file, add);
            writer.write(content);
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String readFile(File file){
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            String tmp = null;
            while ((tmp = reader.readLine()) != null){
                content.append(tmp).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    public static String readFile(File file,String encoding){
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            String tmp = null;
            while ((tmp = reader.readLine()) != null){
                content.append(tmp).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }
}
