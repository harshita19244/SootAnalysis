from os import link, listdir, path, pipe
from os.path import isfile, join
import re
import csv
from traceback import print_tb
file1 = open("C:/Users/harsh/Desktop/Versions-new/guava-28//output.txt", "r",encoding="utf8")
Lines = file1.readlines()
file2 = open("C:/Users/harsh/Desktop/Versions-new/guava-28/output.txt", "r",encoding="utf8")
data = file2.readlines()
#print(len(data))
#str_data = file2.read()
class_method_map = {}
method_names = []
method_declarations = []
method_classes = []
link_graph = {}
#print(data)
for line in Lines:
    if line[0]== "p" or line[0]==" " and line[1]!="*" and line[1]!=" ":
        index = line.index(')')
        index_path_end  = line.rindex(')')
        index_path_start = line.rindex('(')
        path = line[index_path_start+1:index_path_end]
        path_start_idx = path.rindex('\\')
        # path = path.replace('guava-28\\com\\google\\common\\base\\','')
        path_end_idx = path.index(".java")
        path = path[path_start_idx+1:path_end_idx]
        method_declaration = line[:index+1]
        index_bracket = method_declaration.index('(')
        m_name = method_declaration[:index_bracket]
        #print(type(method_name))
        index_space = m_name.rindex(" ")
        m_name = m_name[index_space+1:]  
        if path in class_method_map.keys():
            class_method_map[path].append(m_name)
        else:
            class_method_map[path] = [m_name]       

        method_names.append(m_name)
        method_classes.append(path)
        method_declarations.append(method_declaration)
        #print(method_declaration)
        
file1.close()
src_paths = []
print(method_classes)
print(method_declarations)


start = 1
end = 0
count = 0
while start<len(data):
    #extracting the comment
    end = data[start:].index(']\n')
    temp = data[start:start+end]
    #print(temp[2:])
    comment = ' '.join(map(str, temp[2:]))
    comment = comment.replace("*","")
    comment = comment.replace("\n ","")
    comment = re.sub(' +', ' ', comment)
    print(comment)

    #Getting the current class and method
    current_class = method_classes[count]
    current_sig = method_declarations[count]
    # src_path2 = method_classes[method_names.index(method)]
    # src_path2 = src_path2.split('\\')[-1]
    # end = src_path2.index('.java')
    # src_path2 = src_path2[:end]

    #Finding all curly braces references in the comment
    method_ref = r"\{(.*?)\}"
    match = re.findall(method_ref ,comment)
    #print(match)

    #Removing @code references
    matches = [elements for elements in match if '@code' not in elements]
    matches = [s.lstrip() for s in matches] 
    matches = list(dict.fromkeys(matches))

    #Getting a series of @links
    print(matches)

    #Iterating for every match
    for match in matches:
        match = match.replace("@link ","")
        match = match.replace("@linkplain","")
        m_name = match.split("#")
        m_name[0]
        print("This is called")
        print(m_name)
        print(current_sig)

        #If there is no class nesting
        if len(m_name)>=2 and (m_name[0]=="" or m_name[0]==" "):
            # if len(m_name[1])>1:
            #     method_name_idx = m_name[1].index(")")
            #     m_name[1] = m_name[1][:method_name_idx+1]
            print("Now this is called : "+ m_name[1])
            #If it is a raw_sig_reference
            if '(' in m_name[1]:
                end_idx = m_name[1].index(')')
                start_idx = m_name[1].index('(')
                if end_idx-start_idx == 1:
                    method_name = m_name[1][:m_name[1].index('(')]
                    if method_name in method_names:
                        sink_signature = method_declarations[method_names.index(method_name)]

                        # if the reference is present in the current class
                        if method_name in class_method_map[current_class]:
                            if current_sig in link_graph.keys():
                                link_graph[current_sig].append(sink_signature)
                            else:
                                link_graph[current_sig] = [sink_signature]
                            src_paths.append(current_class)
                        
                        #check in other classes
                        else:
                            for class_name in class_method_map.keys():
                                if method_name in class_method_map[class_name]:
                                    if current_sig in link_graph.keys():
                                        link_graph[current_sig].append(sink_signature)
                                    else:
                                        link_graph[current_sig] = [sink_signature]
                                    src_paths.append(current_class)
                                    break
                
                else:
                    print(m_name[1].index('('))
                    method_args = m_name[1][start_idx+1:end_idx].split(",")
                    print(m_name[1])
                    ref_regex = [s + ".*" for s in method_args]
                    method_name_regex = m_name[1][:m_name[1].index('(')] 
                    print(method_args)
                    method_name = m_name[1][:m_name[1].index('(')]
                    
                    if method_name in class_method_map[current_class]:
                        sink_signature = method_declarations[method_names.index(method_name)]
                        tweaked_sig = sink_signature[sink_signature.index("(")+1:len(sink_signature)-1].split(",")
                        for i in range(len(tweaked_sig)):
                            param = tweaked_sig[i].split(" ")
                            if i<len(method_args):
                                if param[0]!= method_args[i]:
                                    break
                                elif i == len(tweaked_sig)-1 and param[0] == method_args[i]:
                                    if current_sig in link_graph.keys():
                                        link_graph[current_sig].append(sink_signature)
                                    else:
                                        link_graph[current_sig] = [sink_signature]
                                    src_paths.append(current_class)
                                else:
                                    continue
                                
                    else:
                        for class_name in class_method_map.keys():
                            if method_name in class_method_map[class_name]:
                                sink_signature = method_declarations[method_names.index(method_name)]
                                tweaked_sig = sink_signature[sink_signature.index("(")+1:len(sink_signature)-1].split(",")
                                print("this here :")
                                print(tweaked_sig)
                                for i in range(len(tweaked_sig)):
                                    param = tweaked_sig[i].split(" ")
                                    if i<len(method_args):
                                        if param[0]!= method_args[i]:
                                            break
                                        elif i == len(tweaked_sig)-1 and param[0] == method_args[i]:
                                            if current_sig in link_graph.keys():
                                                link_graph[current_sig].append(sink_signature)
                                            else:
                                                link_graph[current_sig] = [sink_signature]
                                            src_paths.append(current_class)
                                        else:
                                            continue
                                
            #not raw sig
            else:
                method_name = m_name[1]
                if method_name in method_names:
                    sink_signature = method_declarations[method_names.index(method_name)]

                    # if the reference is present in the current class
                    if method_name in class_method_map[current_class]:
                        if current_sig in link_graph.keys():
                            link_graph[current_sig].append(sink_signature)
                        else:
                            link_graph[current_sig] = [sink_signature]
                        src_paths.append(current_class)
                    
                    #check in other classes
                    else:
                        for class_name in class_method_map.keys():
                            if method_name in class_method_map[class_name]:
                                if current_sig in link_graph.keys():
                                    link_graph[current_sig].append(sink_signature)
                                else:
                                    link_graph[current_sig] = [sink_signature]
                                src_paths.append(current_class)
                                break
        # else:
        #     print(len(m_name))
        #     print(m_name[1])
        elif len(m_name)>=2:
            if len(m_name[1])>1:
                m_name[1] = m_name[1].split(" ")[0] 
                print(m_name[1])
            class_name = m_name[0]
            method_name = m_name[1]
            sink_signature = method_declarations[method_names.index(method_name)]

            if class_name in class_method_map.keys():
                if method_name in class_method_map[class_name]:
                    if current_sig in link_graph.keys():
                                link_graph[current_sig].append(sink_signature)
                    else:
                        link_graph[current_sig] = [sink_signature]
                    src_paths.append(current_class)


    start +=end+2
    count = count + 1

print(link_graph)
csv_file = "C:/Users/harsh/Desktop/guava_28_new.csv"
keys = list(link_graph.keys())
zd = zip(*link_graph.values())
i = 0
with open(csv_file, 'a') as csvfile:
    writer = csv.writer(csvfile,delimiter = ',')
    for k in keys:
        vals = link_graph[k]
        writer.writerow([k,src_paths[i],vals])
        i = i+1
print(link_graph)