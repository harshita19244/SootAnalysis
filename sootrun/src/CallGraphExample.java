import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import java.util.*;

public class CallGraphExample
{
    static HashMap<String,ArrayList<String>> linkmap = new HashMap<>();
    static HashMap<String,Type> return_types = new HashMap<>();
    static ArrayList<SootMethod> targetmethods = new ArrayList<>();


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
//        String path = sc.next();
//        String method = sc.next();
//        int depth = sc.nextInt();
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        Options.v().set_verbose(false);
        Options.v().set_keep_line_number(true);
        Options.v().set_whole_program(true);
        String classesDir = "C:\\Users\\USer\\sootrun\\out\\production\\sootrun";
        String newdir = "C:\\Users\\USer\\sootrun\\src";
        System.out.println("Classpath: " + Scene.v().getSootClassPath());
        Scene.v().extendSootClassPath(classesDir);
        Scene.v().extendSootClassPath(newdir);

        System.out.println("Classpath: " + Scene.v().getSootClassPath());
        SootClass c = Scene.v().forceResolve("com.google.common.math.PairedStats", SootClass.BODIES);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        SootMethod src = c.getMethodByName("populationCovariance");
        Scene.v().getEntryPoints().add(src);
        List entryPoints = new ArrayList();
        entryPoints.add(src);
        Scene.v().setEntryPoints(entryPoints);
        PackManager.v().getPack("cg").apply();
        CallGraph cg = Scene.v().getCallGraph();
        recurse(cg,src,2,0);
        for (Map.Entry<String,ArrayList<String>> pair : linkmap.entrySet()) {
            for (String m: pair.getValue()){
                System.out.println(pair.getKey() + " -> " + m);
            }
        }
        for (Map.Entry<String,Type> pair: return_types.entrySet()){
            System.out.println(pair.getKey() + " -> " + pair.getValue());
        }
        args = argsList.toArray(new String[0]);
        soot.Main.main(args);
     }

    public static void recurse(CallGraph cg, SootMethod src, int k,int temp){
        Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
        while (targets.hasNext() && temp<=k) {
            SootMethod tgt = (SootMethod)targets.next();
            //System.out.println(src.getDeclaration() + " may call " + tgt.getDeclaration());
            if(!targetmethods.contains(tgt.getName())) {
                targetmethods.add(tgt);
            }
            if(!linkmap.containsKey(src.getDeclaration())){
                linkmap.put(src.getDeclaration(),new ArrayList<>());
            }
            linkmap.get(src.getDeclaration()).add(tgt.getDeclaration());
            return_types.put(tgt.getDeclaration(), tgt.getReturnType());

            temp++;
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