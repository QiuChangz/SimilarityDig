package cn.edu.nju.model;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

public class UserCodeInfo {


    private String user_id;
    private String time;
    private String content;
    private TreeContext treeContext;

    public UserCodeInfo(String user_id, String content, String time){
        this.user_id = user_id;
        this.content = content;
        this.time = time;
    }

    public String getUser_id() {
        return user_id+"_"+time.substring(0,time.lastIndexOf("."));
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ITree getTree() {
        return treeContext.getRoot();
    }

    public TreeContext getTreeContext(){ return  treeContext; }

    public void setTree(TreeContext treeContext) {
        this.treeContext = treeContext;
    }
}
