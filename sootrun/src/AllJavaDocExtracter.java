import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Iterate over the classes and print their Javadoc comments corresponding to each method.
 */
public class AllJavaDocExtracter {

    public static void main(String[] args) throws IOException {
        String pathname = "C:\\Users\\USer\\Desktop\\guava\\android\\guava";
        File output = new File("C:\\Users\\USer\\Desktop\\guava\\android\\guava\\outnew2.txt");
        File projectDir = new File(pathname);
        PrintStream o = new PrintStream(output);
        // Store current System.out before assigning a new value
        PrintStream console = System.out;
        // Assign o to output stream
        System.setOut(o);
        //HashSet <String> m = new HashSet<>();
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            try {
                CompilationUnit cu = StaticJavaParser.parse(file);
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodDeclaration n, Object arg) {
                        super.visit(n, arg);
                        try {
                            output.createNewFile();
                            String name = n.getNameAsString();
                            String type = String.valueOf(n.getType());
                            String parameters = String.valueOf(n.getParameters());
                            if (n.isMethodDeclaration()) {
                                String title = String.format("%s {%s} %s (%s)", name, type, parameters, file);
                                System.out.println(title);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }.visit(cu, null);
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);
    }

}

class DirExplorer {
    public interface FileHandler {
        void handle(int level, String path, File file) throws IOException;
    }

    public interface Filter {
        boolean interested(int level, String path, File file);
    }

    private FileHandler fileHandler;
    private Filter filter;

    public DirExplorer(Filter filter, FileHandler fileHandler) {
        this.filter = filter;
        this.fileHandler = fileHandler;
    }

    public void explore(File root) throws IOException {
        explore(0, "", root);
    }

    private void explore(int level, String path, File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                explore(level + 1, path + "/" + child.getName(), child);
            }
        } else {
            if (filter.interested(level, path, file)) {
                fileHandler.handle(level, path, file);
            }
        }
    }
}