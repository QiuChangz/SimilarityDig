package cn.edu.nju.logic.Helper;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

public class BracketFormatter {

    protected final Writer writer;
    final TreeContext context;
    protected final Pattern replacer = Pattern.compile("[\\\\\"]");
    int level = 0;

    protected BracketFormatter(Writer w, TreeContext ctx) {
        context = ctx;
        this.writer = w;
    }

    public void startTree(ITree tree) throws IOException {
        this.writer.write("{");
        this.writer.write(this.protect(this.context.getTypeLabel(tree)));
    }

    protected String protect(String val) {
        return this.replacer.matcher(val).replaceAll("\\\\$0");
    }

    public void endTree(ITree tree) throws IOException {
        this.writer.write("}");
        --this.level;
    }
}
