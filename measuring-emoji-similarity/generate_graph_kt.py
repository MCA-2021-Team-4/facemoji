import os
from sys import argv

def main():
    if len(argv) < 2 :
        print("specify location of graph.txt")
        quit()

    graph_dir = argv[1]
    graph_file = open(graph_dir, 'r')
    graph = graph_file.readlines()
    kotlins = []

    for i, line in enumerate(graph):
        oneline = line[:-1].split(",")
        adjP = list(map(int,[val for val in oneline[1].split(" ") if val != '']))
        adjS = list(map(int,[val for val in oneline[0].split(" ") if val != '']))
        adjC = list(map(int,[val for val in oneline[2].split(" ") if val != '']))

        kt = "graph.add(intArrayOf("
        if adjP:
            for p in adjP:
                kt = kt + "{},".format(p)
        if adjS:
            for s in adjS:
                kt = kt + "{},".format(s)
        if adjC:
            for c in adjC:
                kt = kt + "{},".format(c)
        kt = kt[:-1] + "))"
        kotlins.append(kt)

    ktcode = open("add_graph.kt", 'w')
    for line in kotlins:
        ktcode.write("{}\n".format(line))
    ktcode.close()


if __name__ == "__main__":
    main()
