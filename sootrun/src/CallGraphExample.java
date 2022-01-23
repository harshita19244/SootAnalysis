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
        Scene.v().extendSootClassPath("C:\\Users\\USer\\Desktop\\SootAnalysis\\sootrun\\out\\production\\sootrun");
        System.out.println("Classpath: " + Scene.v().getSootClassPath());
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\USer\\Desktop\\guava\\guava\\src\\com\\outnew2.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                HashMap<String, ArrayList<Pair>> linkmap = new HashMap<>();
                int idx = line.indexOf("{");
                int idx2 = line.indexOf("[");
                int idx3 = line.indexOf("}");
                int idx4 = line.indexOf("]");
                int pathidx = line.indexOf("src");
                int endidx = line.indexOf(".java");
                String method = line.substring(0, idx - 1);
                System.out.println(method);
                System.out.println(method.length());
                String ret = line.substring(idx + 1, idx3);
                System.out.println(ret);
                String parameters = line.substring(idx2 + 1, idx4);
                System.out.println(parameters);
                String cp = line.substring(pathidx + 4, endidx);
                cp = cp.replace('\\', '.');
                System.out.println(cp);
                //System.out.println(cp.length());
                SootClass c = Scene.v().forceResolve(cp, SootClass.BODIES);
                c.setApplicationClass();
                Scene.v().loadNecessaryClasses();
                List<SootMethod> cm = c.getMethods();
                SootMethod src = getMethodMatch(method, cm, parameters, ret);
                if(src == null){
                    continue;
                }
                Scene.v().getEntryPoints().add(src);
                List entryPoints = new ArrayList();
                entryPoints.add(src);
                Scene.v().setEntryPoints(entryPoints);
                PackManager.v().getPack("cg").apply();
                CallGraph cg = Scene.v().getCallGraph();
                recurse(cg, src, 4, 0, linkmap);
                System.out.println("Linkage graph given source method");
                for (Map.Entry<String, ArrayList<Pair>> element : linkmap.entrySet()) {
                    linkage_graph.put(element.getKey(), element.getValue());
//                    System.out.println(element.getKey() + " -> ");
//                    for (Pair s: element.getValue()){
//                        System.out.println("Method name: " + s.getMethod_name().replace("java.lang.",""));
//                        System.out.println("Declaring class: "+ s.getDeclaring_class());
//
//                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            FileOutputStream myFileOutStream = new FileOutputStream("C:\\Users\\USer\\Desktop\\guava\\link.txt");
            ObjectOutputStream myObjectOutStream = new ObjectOutputStream(myFileOutStream);
            myObjectOutStream.writeObject(linkage_graph);
            for (Map.Entry<String,ArrayList<Pair>> element : linkage_graph.entrySet()) {
                System.out.println("Source name: " + element.getKey());
                for (Pair s: element.getValue()){
                    System.out.println("Method name: " + s.getMethod_name().replace("java.lang.",""));
                    System.out.println("Declaring class: "+ s.getDeclaring_class());

                }
            }
            // closing FileOutputStream and
            // ObjectOutputStream
            myObjectOutStream.close();
            myFileOutStream.close();
        }
        catch (IOException e) {
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

            if(!method_names.contains(tgt.getName()) && tgt.getDeclaringClass().getName().contains("com.google")){
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