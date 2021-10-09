import soot.*;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import java.util.*;

public class CallGraphExample
{
    static HashMap<SootMethod,SootMethod> linkmap = new HashMap<>();
    static ArrayList<SootMethod> targetmethods = new ArrayList<>();

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        Options.v().set_verbose(false);
        Options.v().set_keep_line_number(true);
        String classesDir = "C:\\Users\\USer\\sootrun\\out\\production\\sootrun";
        String depdir = "C:\\Users\\USer\\sootrun\\src\\testers";
        String newdir = "C:\\Users\\USer\\sootrun\\src";
        System.out.println("Classpath: " + Scene.v().getSootClassPath());
        Scene.v().extendSootClassPath(classesDir);
        Scene.v().extendSootClassPath(depdir);
        Scene.v().extendSootClassPath(newdir);
        System.out.println("Classpath: " + Scene.v().getSootClassPath());

        argsList.addAll(Arrays.asList("-w",
 //            "-main-class",
              "testers.CallGraphs",
              "testers.CallGraphs",
                "testers.A"

        ));
         PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {

            @Override
            protected void internalTransform(String phaseName, Map options) {
                CHATransformer.v().transform();
                //SootClass c = Scene.v().getSootClass("testers.A");
                SootClass c = Scene.v().forceResolve("com.google.common.base.Splitter", SootClass.BODIES);
                c.setApplicationClass();
                Scene.v().loadNecessaryClasses();
                SootMethod src = c.getMethodByName("bar");
                System.out.println(Scene.v().getEntryPoints());
                Scene.v().getEntryPoints().add(src);
                  List entryPoints = new ArrayList();
                entryPoints.add(src);
                Scene.v().setEntryPoints(entryPoints);
                System.out.println(Scene.v().getEntryPoints());
                PackManager.v().runPacks();
//                SootClass c = Scene.v().getSootClass("testers.CallGraphs");
//                SootMethod src = c.getMethodByName("main");
                CallGraph cg = Scene.v().getCallGraph();
                //System.out.println(cg);
                recurse(cg,src,2,0);
                for (SootMethod method : targetmethods) {
                    System.out.println(method);
                }

                for (Map.Entry<SootMethod, SootMethod> pair : linkmap.entrySet()) {
                    System.out.println(pair.getKey().getName() + " -> " + pair.getValue().getName());
                }
            }

        }));
        args = argsList.toArray(new String[0]);
        soot.Main.main(args);
    }

    public static void recurse(CallGraph cg, SootMethod src, int k,int temp){


        Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
        System.out.println(targets.hasNext());
        while (targets.hasNext() && temp<=k) {
            System.out.println(111);
            SootMethod tgt = (SootMethod)targets.next();
            System.out.println(src + " may call " + tgt);
            if(!targetmethods.contains(tgt.getName())){
                targetmethods.add(tgt);
                linkmap.put(src,tgt);
                //System.out.println(tgt.getDeclaration());
                //System.out.println("tYPE "+ tgt.getParameterTypes())
            }

            temp++;
           // recurse(cg,tgt,k,temp);
        }
    }

    public static void recurseback(CallGraph cg, SootMethod src, int k,int temp){

        if(temp==k){
            return;
        }

        Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesInto(src));

        while (targets.hasNext()) {
            SootMethod tgt = (SootMethod)targets.next();
            System.out.println(tgt + " may be called by" + src);
//            if(!targetmethods.contains(tgt.getName())){
//                targetmethods.add(tgt.getName());
//            }

            temp++;
            recurseback(cg,tgt,k,temp);
        }

    }
}