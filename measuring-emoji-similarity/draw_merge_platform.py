from sys import argv
import os
import numpy as np
import cv2
import plotly.express as px
from draw_table import draw_table


platforms = ["Apple", "Facebook", "Google", "Samsung", "Twitter"]
emotion = {"0" : "Happiness", "1" : "Neutral", "2" : "Surprise", "3" : "Fear", "4" : "Sadness", "5" : "Disgust", "6" : "Anger"}

def draw_emoji_emotion(plat):
    target_dir = os.path.join("emojis", plat)
    emoji_emotion_f = open(f"results/label-with-emotion.txt", "r")
    
    img_results= []
    for emoji in sorted(os.listdir(target_dir)):
        img = cv2.imread(os.path.join(target_dir, emoji))
        img_results.append(img)
        
    labels = []
    while True:
        line = emoji_emotion_f.readline()
        if not line:
            break
        line = list(line.split())
        labels.append(emotion[line[2]])
        
    draw_table(img_results, 1, labels)
        
   
        
    
def get_platform_rank(metric, plat):
    merged_f = open(f"merged/{metric}.txt", "r")
    plat_f = open(f"results/{plat}/{metric}.txt", "r")
    rank = 0
    while True:
        merge_line = merged_f.readline()
        plat_line = plat_f.readline()
        if not merge_line:
            break
        
        merge_line = list(merge_line.split())
        merge_line = [int(value) for value in merge_line]
        
        plat_line = list(plat_line.replace('inf', '100.00').split())
        plat_line = [(index, float(value)) for index, value in enumerate(plat_line)]
        if metric == 'rmse':
            sorted_plat_line = sorted(plat_line, key=lambda tup: tup[1])
        else:
            sorted_plat_line = sorted(plat_line, key=lambda tup: tup[1], reverse=True)
            
        for merge_val in merge_line:
            for index, plat_val in enumerate(sorted_plat_line):
                if merge_val == plat_val[0]:
                    rank += index
    return rank

def draw_image_pair(selected_platform, metric):
    target_dir = os.path.join("emojis", selected_platform)
    merged_f = open(f"merged/{metric}.txt", "r")
    
    emojis = []
    for i, emoji in enumerate(sorted(os.listdir(target_dir))):
        # ex) smiling_face.png
        emojis.append(emoji)
    
    row = 0
    img_result = []    
    while True:
        line = merged_f.readline()
        if not line:
            break
        
        listline = list(line.split())
        listline = [int(index) for index in listline]
        
        row_img = cv2.imread(os.path.join(target_dir, emojis[row]))
        for i in listline:
            col_img = cv2.imread(os.path.join(target_dir, emojis[i]))
            pair_img = np.hstack((row_img, col_img))
            img_result.append(pair_img)
            
        row += 1
        
    labels = [i for i in range(0, len(img_result))]
    print("draw start")
    draw_table(img_result, 8, labels)

def get_same_emotion_num(metric):
    result = [] #행별 대표이모지와 같은 감정을 가지고 있는 이모지의 수를 나타낸다.
    emoji_emotion_f = open(f"results/label-with-emotion.txt", "r")
    merged_f = open(f"merged/{metric}.txt", "r")
    
    emoji_emotion = []
    while True:
        line = emoji_emotion_f.readline()
        if not line:
            break
        emoji_emotion.append(list(line.split()))
        
    row = 0
    while True:
        line = merged_f.readline()
        same_emotion_num = 0
        if not line:
            break
        
        line = list(line.split())
        line = [int(value) for value in line] #merged file의 결과를 int로 변환
        represent_emoji_emotion = emoji_emotion[row][2] #행의 대표 이모지의 감정 번호
        for merged_result in line:
            if emoji_emotion[merged_result][2] == represent_emoji_emotion: #행에 속하는 이모지 감정이 행의 대표 이모지 감정과 같을 경우
                same_emotion_num += 1
        
        result.append(same_emotion_num)
        row += 1
        
    return result

if __name__ == "__main__":
    if len(argv) < 2 :
        usage_msg = """
        USAGE:
               python draw_merge_platform.py {mode} {metric or platform}
        """
        print(usage_msg)
        quit()
    
    mode = int(argv[1])
    
    
    if(mode == 0):
        platform = argv[2]
        print("Show imoji-emotion mapping")
        draw_emoji_emotion(platform)
        
    elif(mode == 1):    
        metric = argv[2]
        print(f"merged된 {metric}을 바탕으로 통계 결과")
        minRank = 80000
        for plat in platforms:
            rank = get_platform_rank(metric, plat)
            if rank < minRank:
                minRank = rank
                selected_platform = plat

        print(f"finish getting platfrom. selected platform is {selected_platform}")


        result = get_same_emotion_num(metric)
        mean_val = np.mean(np.array(result))
        min_val = min(result)
        max_val = max(result)
        min_appear_num = result.count(min_val)
        max_appear_num = result.count(max_val)
        print(f"평균 : {mean_val}")
        print(f"최소값 : {min_val}, 최소값 등장 횟수 : {min_appear_num}")
        print(f"최대값 : {max_val}, 최대값 등장 횟수 : {max_appear_num}")
        draw_image_pair(selected_platform, metric)

    
    
    