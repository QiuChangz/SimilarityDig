package cn.edu.nju.model;

import com.github.gumtreediff.tree.ITree;

public class UserCodeInfo {


    private String user_id;
    private String time;
    private String content;
    private ITree tree;

    public UserCodeInfo(String user_id, String content, String time){
        this.user_id = user_id;
        this.content = content;
        this.time = time;
    }

    public String getUser_id() {
        return user_id;
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
        return tree;
    }

    public void setTree(ITree tree) {
        this.tree = tree;
    }
}
