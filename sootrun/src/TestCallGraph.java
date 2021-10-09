import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;

public class TestCallGraph {
    public static void main(String[] args) {
        args = new String[] {"-w", "testers.CallGraphs", "testers.CallGraphs", "testers.A"};
//        List<String> argsList = new ArrayList<>(Arrays.asList(args));
//        argsList.addAll(Arrays.asList("-w",
//                "-keep-line-number",
//                "testers.CallGraphs",//main-class
//                "testers.CallGraphs",//argument classes
//                "testers.A"            //
//        ));

        String javapath = System.getProperty("java.class.path");
        String jredir = "C://Program Files (x86)/Java/jdk1.6.0_13/jre/lib/rt.jar";
        String jcedir = "C://Program Files (x86)/Java/jdk1.6.0_13/jre/lib/jce.jar";
        String path = javapath+ File.pathSeparator+jredir+File.pathSeparator+jcedir;
        Scene.v().setSootClassPath(path);

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {

            @Override
            protected void internalTransform(String phaseName, Map options) {
                CHATransformer.v().transform();
                SootClass a = Scene.v().getSootClass("testers.A");

                SootMethod src = Scene.v().getMainClass().getMethodByName("doStuff");
                CallGraph cg = Scene.v().getCallGraph();

                Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
                while (targets.hasNext()) {
                    SootMethod tgt = (SootMethod)targets.next();
                    System.out.println(src + " may call " + tgt);
                }
            }

        }));

        soot.Main.main(args);
    }
}