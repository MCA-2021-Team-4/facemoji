from collections import deque
from itertools import permutations
from copy import deepcopy

ssim = []

anger = [[32, 63],
         [31],
         [0]]

disgust = [[18, 22, 56],
           [5, 6, 10, 12, 13, 30, 41, 47, 61, 84],
           [85, 87]]

fear = [[21],
        [2, 37],
        []]

happiness = [[4, 33, 43, 46, 59, 65, 73, 81, 82],
             [14, 17, 20, 23, 35, 42, 44, 45, 48, 50, 51, 55, 72, 74, 75, 78, 88],
             [8, 52, 57, 64, 71, 76, 77, 79, 86, 89]]

neutral = [[16, 36, 58],
           [19, 25, 26, 28, 29, 94],
           [11, 24, 54, 67, 68, 69, 80, 83, 90, 92]]

sadness = [[53],
           [9, 62, 66],
           [1, 7, 34, 39, 40, 60, 70, 91]]

surprise = [[15],
            [3, 27, 38, 49],
            [93]]


anger_num = [2, 1, 1]
disgust_num = [3, 10, 2]
fear_num = [1, 2, 0]
happiness_num = [9, 17, 10]
neutral_num = [3, 6, 10]
sadness_num = [1, 3, 8]
surprise_num = [1, 4, 1]
emojis_num = [anger_num, disgust_num, fear_num, happiness_num, neutral_num, sadness_num, surprise_num]

emojis_list = [anger, disgust, fear, happiness, neutral, sadness, surprise]
emojis_list_empty = deepcopy(emojis_list)

result_graph = []
result_graph_flat = []
result_top_num = []
result_outlier = []
statistics = [list(0 for i in range(3)) for j in range(95)]

def main():
    f = open("ssim.txt", 'r')
    while True:
        line = f.readline()
        if not line: break
        ssim.append(list(map(int, line.split())))
    # make a graph
    for id, top_list in enumerate(ssim):
        result = [[], [], []]       #sibling, parent, child
        result_flat = []
        for emotion_idx, emotion in enumerate(emojis_list):
            for level, stage in enumerate(emotion):
                if id in stage:
                    if level == 0:
                        sibling = min(emojis_num[emotion_idx][level] - 1, 3)
                        parent = 0
                        child = min(emojis_num[emotion_idx][level + 1], 2)
                    elif level == 1:
                        sibling = min(emojis_num[emotion_idx][level] - 1, 3)
                        parent = min(emojis_num[emotion_idx][level - 1], 1)
                        child = min(emojis_num[emotion_idx][level + 1], 1)
                    else:
                        sibling = min(emojis_num[emotion_idx][level] - 1, 3)
                        parent = min(emojis_num[emotion_idx][level - 1], 2)
                        child = 0
                    num = sibling + parent + child
                    for top_idx, top in enumerate(top_list):
                        if sibling != 0 and emojis_list_empty[emotion_idx][level] and top in stage:
                            if len(emojis_list_empty[emotion_idx][level]) == 1 and id in emojis_list_empty[emotion_idx][level]:
                                num -= 1
                                sibling -= 1
                                result[0].append(top)
                                result_flat.append(top)
                                emojis_list_empty[emotion_idx][level].remove(top)
                                continue
                            if top in emojis_list_empty[emotion_idx][level]:
                                num -= 1
                                sibling -= 1
                                result[0].append(top)
                                result_flat.append(top)
                                emojis_list_empty[emotion_idx][level].remove(top)
                            else: continue
                        elif sibling != 0 and top in stage and top not in result[0]:
                            num -= 1
                            sibling -= 1
                            result[0].append(top)
                            result_flat.append(top)
                        elif child != 0 and emojis_list_empty[emotion_idx][level + 1] and top in emotion[level + 1]:
                            if top in emojis_list_empty[emotion_idx][level + 1]:
                                num -= 1
                                child -= 1
                                result[2].append(top)
                                result_flat.append(top)
                            else: continue
                        elif child != 0 and top in emotion[level + 1] and top not in result[2]:
                            num -= 1
                            child -= 1
                            result[2].append(top)
                            result_flat.append(top)
                        elif parent != 0 and emojis_list_empty[emotion_idx][level - 1] and top in emotion[level - 1]:
                            if top in emojis_list_empty[emotion_idx][level - 1]:
                                num -= 1
                                parent -= 1
                                result[1].append(top)
                                result_flat.append(top)
                            else: continue
                        elif parent != 0 and top in emotion[level - 1] and top not in result[1]:
                            num -= 1
                            parent -= 1
                            result[1].append(top)
                            result_flat.append(top)
                        if num == 0:
                            result_graph.append(result)
                            result_graph_flat.append(result_flat)
                            result_top_num.append(top_idx)
                            break
                    else:
                        for top_idx, top in enumerate(top_list):
                            if sibling != 0 and top in stage and top not in result[0]:
                                num -= 1
                                sibling -= 1
                                result[0].append(top)
                                result_flat.append(top)
                            elif child != 0 and top in emotion[level + 1] and top not in result[2]:
                                num -= 1
                                child -= 1
                                result[2].append(top)
                                result_flat.append(top)
                            elif parent != 0 and top in emotion[level - 1] and top not in result[1]:
                                num -= 1
                                parent -= 1
                                result[1].append(top)
                                result_flat.append(top)
                            if num == 0:
                                result_graph.append(result)
                                result_graph_flat.append(result_flat)
                                result_top_num.append(top_idx)
                                break
                        else:
                            result_graph.append(result)
                            result_graph_flat.append(result_flat)
                            result_top_num.append(-1)
                    break
            else: continue
            break

    # find outliers
    for idx, node in enumerate(result_graph):
        for child in node[2]:
            if idx not in result_graph[child][1]:
                result_outlier.append(idx)
                break

    # make statistics
    for node in result_graph:
        for idx, lst in enumerate(node):
            for emoji in lst:
                statistics[emoji][idx] += 1

    fgraph = open('graph.txt', 'w')
    fnum = open('required_top_num.txt', 'w')
    foutlier = open('outliers.txt', 'w')
    fstatistics = open('statistics.txt', 'w')
    for lists in result_graph:
        for idx, lst in enumerate(lists):
            for i in lst:
                fgraph.write("{} ".format(i))
            if idx != 2:
                fgraph.write(", ")
            else:
                fgraph.write("\n")
    for num in result_top_num:
        fnum.write("{}\n".format(num))
    for idx in result_outlier:
        foutlier.write("{}\n".format(idx))
    for lst in statistics:
        for id in lst:
            fstatistics.write("{} ".format(id))
        fstatistics.write("\n")

    fgraph.close()
    fnum.close()
    foutlier.close()
    fstatistics.close()
    check_connected()

is_connected = []
connected_statistics = []
longest_paths = []

def check_connected():
    for emotion in emojis_list:
        num_true = 0
        num_false = 0
        emojis_flat = []
        longest = -1
        for emojis in emotion:
            for i in emojis:
                emojis_flat.append(i)
        perms = permutations(emojis_flat, 2)
        connected = True
        for s, e in perms:
            visited = [False for i in range(95)]
            q = deque([])
            q.append((s, 1))
            visited[s] = True
            found = False
            while q:
                node, d = q.popleft()
                for adj in result_graph_flat[node]:
                    if adj == e:
                        found = True
                        num_true += 1
                        longest = max(longest, d)
                        break
                    if not visited[adj]:
                        visited[adj] = True
                        q.append((adj, d+1))
                else: continue
                break
            if not found:
                connected = False
                num_false += 1
        is_connected.append(connected)
        connected_statistics.append((num_true, num_false))
        longest_paths.append(longest)
    print(is_connected)
    print(connected_statistics)
    print(longest_paths)

if __name__ == "__main__":
    main()