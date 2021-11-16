from os import link, listdir, path, pipe
from os.path import isfile, join
import re
import csv
file1 = open("C:/Users/USer/spring-boot/output20.txt", "r")
Lines = file1.readlines()
file2 = open("C:/Users/USer/spring-boot/output20.txt", "r")
data = file2.readlines()
#print(len(data))
#str_data = file2.read()
method_declarations = []
method_names = []
method_classes = []
link_graph = {}
#print(data)
for line in Lines:
    if line[0]== "p" or line[0]==" " and line[1]!="*" and line[1]!=" ":
        index = line.index(')')
        index_path_end  = line.rindex(')')
        index_path_start = line.rindex('(')
        path = line[index_path_start+1:index_path_end]
        path = path[path.index('spring-boot'):]
        method_declaration = line[:index+1]
        index_bracket = method_declaration.index('(')
        method_name = method_declaration[:index_bracket]
        #print(type(method_name))
        index_space = method_name.rindex(" ")
        method_name = method_name[index_space+1:]      
        method_declarations.append(method_declaration)
        method_names.append(method_name)
        method_classes.append(path)
        #print(method_declaration)
        #print(method_name)
file1.close()
file1 = open("C:/Users/USer/spring-boot/output10.txt", "r")
str_data = file1.read()
print(method_classes)
src_paths = []
for method in method_names:
#print(data)
    start = 1
    end = 0
    while start<len(data):
        end = data[start:].index(']\n')
        temp = data[start:start+end]
        comment = ' '.join(map(str, temp[2:]))
        #print(comment)
        method_ref = ''
        method_ref_2 = ''
        count = method_names.count(method)
        method_ref = '@linkplain'+' '+ '#' + method 
        method_ref_2 = '@link'+' ' + '#' + method
        #print(method_ref_2)
        match = re.findall(method_ref ,comment)
        match2 = re.findall(method_ref_2, comment)
        #print(match)
        if len(match)>0 and len(match[0])==len(method_ref) or len(match2)>0 and len(match2[0])==len(method_ref_2):
            end_index = temp[0].index(')')
            signature = temp[0][:end_index+1]
            method_index = method_declarations.index(signature)
            src_signature = method_declarations[method_names.index(method)]
            src_path = method_classes[method_names.index(method)]
            if src_signature in link_graph.keys():
                link_graph[src_signature].append(signature)
            else:
                link_graph[src_signature] = [signature]   
            print(signature)
            print(method)
            print(src_path)
            src_paths.append(src_path)
            print('-------------')

        start +=end+2
    #print(end)
csv_file = "C:/Users/USer/Desktop/splink.csv"
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

