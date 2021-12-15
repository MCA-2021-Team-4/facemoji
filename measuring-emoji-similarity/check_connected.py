from itertools import permutations
from collections import deque

graph = []

def main():
    f = open("graph/graph.txt", 'r')
    while True:
        line = f.readline()
        if not line: break
        graph.append(list(map(int, line.replace(',', ' ').split())))

    check_connected()


is_connected = []
connected_statistics = []
longest_paths = []


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

emojis_list = [anger, disgust, fear, happiness, neutral, sadness, surprise]


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
                for adj in graph[node]:
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