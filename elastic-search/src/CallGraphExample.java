import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import java.util.*;

class Pair{
    private String method_name;
    private String declaring_class;

    public Pair(String method_name,String declaring_class){
        this.method_name = method_name;
        this.declaring_class = declaring_class;
    }

    public String getDeclaring_class() {
        return declaring_class;
    }

    public String getMethod_name() {
        return method_name;
    }
}
public class CallGraphExample
{
    static HashMap<String,ArrayList<String>> linkmap = new HashMap<>();
    static HashMap<SootMethod,Pair> targetmethods = new HashMap<>();

    public static String clean_string(String str)
    {
        String new_str = "";
        if (str.contains(","))
        {
            String[] parts = str.split(",");
            for (int i=0; i<parts.length; i++)
            {
                if (parts[i].contains("."))
                {
                    int index1 = parts[i].lastIndexOf(".");
                    parts[i] = parts[i].substring(index1+1);
                    if (i==parts.length-1)
                        new_str = new_str + parts[i];
                    else
                        new_str = new_str + parts[i] + ",";

                }
            }
            new_str = new_str.replace("]", "");
        }
        else
        {
            new_str = str;
        }
        if (new_str.contains("."))
        {
            int index1 = str.lastIndexOf(".");
            new_str = str.substring(index1+1);
        }

        return new_str;
    }


    public static SootMethod getMethodMatch(String name, List<SootMethod> methodList, String params, String ret) {
        ArrayList<SootMethod> newarr = new ArrayList<>();
        SootMethod foundMethod = null;
        if (methodList == null) {
            throw new RuntimeException("couldn't find method " + name + "("  + ") in ");

        }
        int max_score = 0;
        System.out.println(methodList);
        for (SootMethod method : methodList) {
            int score = 0;


            if (method.getName().equals(name)){ // && parameterTypes.equals(method.getParameterTypes().toString())) {
                score +=10;
                if (method.getReturnType().toString().equals(ret))
                {
                    score += 5;
                }

                String temp = method.getParameterTypes().toString();
                temp = clean_string(temp);
                String[] pp = temp.split(",");
                String[] rr = params.split(",");
                if (pp.length == rr.length)
                {
                    score += 5;
                    for (int j=0; j<pp.length; j++)
                    {
                        String qw = pp[j];
                        if (qw.equals(rr[j]))
                        {
                            score += 2;
                        }
                    }
                }

                if (score > max_score)
                {
                    foundMethod = method;
                    max_score = score;
                    System.out.println(score);
                }
            }
        }

        return foundMethod;
    }

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        Options.v().set_verbose(false);
        Options.v().set_keep_line_number(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_no_bodies_for_excluded(true);
        System.out.println("Classpath: " + Scene.v().getSootClassPath());
        Scene.v().extendSootClassPath("C:\\Users\\USer\\Desktop\\SootAnalysis\\elastic-search\\src");
        System.out.println("Classpath: " + Scene.v().getSootClassPath());
        SootClass c = Scene.v().forceResolve("org.elasticsearch.index.fielddata.ordinals.OrdinalsBuilder", SootClass.BODIES);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        List<SootMethod> sm = c.getMethods();
        SootMethod src = c.getMethodByName("getFirstOrdinals");
       //SootMethod src = getMethodMatch("type",sm,"String type","IndexRequest");
        Scene.v().getEntryPoints().add(src);
        List entryPoints = new ArrayList();
        entryPoints.add(src);
        Scene.v().setEntryPoints(entryPoints);
        PackManager.v().getPack("cg").apply();
        CallGraph cg = Scene.v().getCallGraph();
        recurse(cg,src,6,0);
        System.out.println("Linkage graph given source method");
        for (Map.Entry<String,ArrayList<String>> pair : linkmap.entrySet()) {
            System.out.println(pair.getKey() + " -> ");
            for (String m: pair.getValue()){
                System.out.print(m + ", ");
            }
        }
        System.out.println();
        System.out.println();
        System.out.println("Target methods with dependency");
        for (Map.Entry<SootMethod,Pair> s: targetmethods.entrySet()){
            System.out.println("Method name: " + s.getValue().getMethod_name().replace("java.lang.",""));
            System.out.println("Declaring class: "+ s.getValue().getDeclaring_class());
        }

        System.out.println();
        args = argsList.toArray(new String[0]);
        soot.Main.main(args);
    }

    public static void recurse(CallGraph cg, SootMethod src, int k,int temp){
        Iterator targets = new Targets(cg.edgesOutOf(src));
        List<String> method_names = new ArrayList<>();
        while (targets.hasNext() && temp<=k) {
            SootMethod tgt = (SootMethod)targets.next();
            //System.out.println(tgt.getName());
            //System.out.println(src.getDeclaration() + " may call " + tgt.getDeclaration());
            if(!linkmap.containsKey(src.getDeclaration())){
                linkmap.put(src.getDeclaration(),new ArrayList<>());
            }
            if(tgt.getDeclaringClass().getName().contains("org.elasticsearch")) {
                targetmethods.put(tgt, new Pair(tgt.getDeclaration(),tgt.getDeclaringClass().getName()));
            }
            if(!method_names.contains(tgt.getName())){
                method_names.add(tgt.getName());
                linkmap.get(src.getDeclaration()).add(tgt.getDeclaration());
                temp++;
            }
        }
    }

    public static void recurseback(CallGraph cg, SootMethod src, int k,int temp){
        Iterator targets = new Targets(cg.edgesInto(src));
        List<String> method_names = new ArrayList<>();
        while (targets.hasNext() && temp<=k) {
            SootMethod tgt = (SootMethod)targets.next();
            //System.out.println(tgt.getName());
            //System.out.println(src.getDeclaration() + " may call " + tgt.getDeclaringClass() + " " + tgt.getReturnType() );
            if(!linkmap.containsKey(src.getDeclaration())){
                linkmap.put(src.getDeclaration(),new ArrayList<>());
            }
            if(tgt.getDeclaringClass().getName().contains("org.elasticsearch")) {
                targetmethods.put(tgt, new Pair(tgt.getDeclaration(),tgt.getDeclaringClass().getName()));
            }
            if(!method_names.contains(tgt.getName())){
                method_names.add(tgt.getName());
                linkmap.get(src.getDeclaration()).add(tgt.getDeclaration());
                temp++;
            }
        }
    }
}
