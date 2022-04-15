import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;

import java.io.*;
import java.util.*;

class Pair implements Serializable{
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
    static HashMap<String,ArrayList<Pair>> linkage_graph = new HashMap();
    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        Options.v().set_verbose(false);
        Options.v().set_keep_line_number(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_no_bodies_for_excluded(true);
        System.out.println("Classpath: " + Scene.v().getSootClassPath());
        Scene.v().extendSootClassPath("C:\\Users\\USer\\glide\\src");
        System.out.println("Classpath: " + Scene.v().getSootClassPath());

        int count= 0;
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\USer\\Desktop\\glide\\out.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                HashMap<String,ArrayList<Pair>> linkmap = new HashMap<>();
                int idx = line.indexOf("java");
                int endidx = line.indexOf(".java");
                int midx = line.indexOf("(");
                String cp = line.substring(idx+5,endidx);
                String m = line.substring(0,midx-1);
                cp = cp.replace('\\','.');
                System.out.println(cp);
                SootClass c = Scene.v().forceResolve(cp, SootClass.BODIES);
                c.setApplicationClass();
                Scene.v().loadNecessaryClasses();
                Scene.v().loadNecessaryClasses();
                System.out.println(m);
                try{
                    SootMethod src = c.getMethodByName(m);
                    Scene.v().getEntryPoints().add(src);
                    List entryPoints = new ArrayList();
                    entryPoints.add(src);
                    Scene.v().setEntryPoints(entryPoints);
                    PackManager.v().getPack("cg").apply();
                    CallGraph cg = Scene.v().getCallGraph();
                    recurse(cg,src,4,0,linkmap);
                    System.out.println("Linkage graph given source method");
                    for (Map.Entry<String,ArrayList<Pair>> element : linkmap.entrySet()) {
                        linkage_graph.put(element.getKey(), element.getValue());
//                    System.out.println(element.getKey() + " -> ");
//                    for (Pair s: element.getValue()){
//                        System.out.println("Method name: " + s.getMethod_name().replace("java.lang.",""));
//                        System.out.println("Declaring class: "+ s.getDeclaring_class());
//
//                    }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


//                System.out.println();
//                System.out.println();
//                System.out.println("Target methods with dependency");
//
//                System.out.println();


            }
//            try {
//                FileOutputStream myFileOutStream = new FileOutputStream("C:\\Users\\USer\\glide\\link.txt");
//                ObjectOutputStream myObjectOutStream = new ObjectOutputStream(myFileOutStream);
//                myObjectOutStream.writeObject(linkage_graph);
//                for (Map.Entry<String,ArrayList<Pair>> element : linkage_graph.entrySet()) {
//                    System.out.println("Source name: " + element.getKey());
//                    for (Pair s: element.getValue()){
//                        System.out.println("Method name: " + s.getMethod_name().replace("java.lang.",""));
//                        System.out.println("Declaring class: "+ s.getDeclaring_class());
//
//                    }
//                }
//                // closing FileOutputStream and
//                // ObjectOutputStream
//                myObjectOutStream.close();
//                myFileOutStream.close();
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        args = argsList.toArray(new String[0]);
        soot.Main.main(args);
    }

    public static void recurse(CallGraph cg, SootMethod src, int k,int temp,HashMap<String,ArrayList<Pair>> linkmap){
        Iterator targets = new Targets(cg.edgesOutOf(src));
        List<String> method_names = new ArrayList<>();
        while (targets.hasNext() && temp<=k) {
            SootMethod tgt = (SootMethod)targets.next();
            //System.out.println(tgt.getName());
            //System.out.println(src.getDeclaration() + " may call " + tgt.getDeclaration());
            if(!linkmap.containsKey(src.getDeclaration())){
                linkmap.put(src.getDeclaration(),new ArrayList<>());
            }

            if(!method_names.contains(tgt.getName()) && tgt.getDeclaringClass().getName().contains("com.bumptech")){
                method_names.add(tgt.getName());
                linkmap.get(src.getDeclaration()).add(new Pair(tgt.getDeclaration(),tgt.getDeclaringClass().getName()));
                temp++;
            }
        }
    }

//    public static void recurseback(CallGraph cg, SootMethod src, int k,int temp){
//        Iterator targets = new Targets(cg.edgesInto(src));
//        List<String> method_names = new ArrayList<>();
//        while (targets.hasNext() && temp<=k) {
//            SootMethod tgt = (SootMethod)targets.next();
//            //System.out.println(tgt.getName());
//            //System.out.println(src.getDeclaration() + " may call " + tgt.getDeclaringClass() + " " + tgt.getReturnType() );
//            if(!linkmap.containsKey(src.getDeclaration())){
//                linkmap.put(src.getDeclaration(),new ArrayList<>());
//            }
//            if(tgt.getDeclaringClass().getName().contains("org.elasticsearch")) {
//                targetmethods.put(tgt, new Pair(tgt.getDeclaration(),tgt.getDeclaringClass().getName()));
//            }
//            if(!method_names.contains(tgt.getName())){
//                method_names.add(tgt.getName());
//                linkmap.get(src.getDeclaration()).add(tgt.getDeclaration());
//                temp++;
//            }
//        }
//    }
}

//link graph s.t src calls sink and each of target calls src.