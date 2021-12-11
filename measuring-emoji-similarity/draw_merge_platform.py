from sys import argv
import os
import numpy as np
import cv2
import plotly.express as px
from draw_table import draw_table


platforms = ["Apple", "Facebook", "Google", "Samsung", "Twitter"]

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

def get_image_pair(selected_platform, metric):
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
        
    return img_result

if __name__ == "__main__":
    # if len(argv) < 2 :
    #     usage_msg = """
    #     USAGE:
    #            python draw_merge_platform.py {metric} {mark directory}
    #     """
    #     print(usage_msg)
    #     quit()
    
    metric = argv[1]
    # mark_dir = argv[2]
    
    strange_f = open(f"strange/{metric}.txt", "r")
    strange_line = strange_f.readline()
    strange_line = list(strange_line.split())
    strange_line = [int(value) for value in strange_line]
    
    minRank = 80000
    for plat in platforms:
        rank = get_platform_rank(metric, plat)
        if rank < minRank:
            minRank = rank
            selected_platform = plat
            
    print(f"finish getting platfrom. selected platform is {selected_platform}")
            
    img_result = get_image_pair(selected_platform, metric)
    draw_table(img_result, 8, strange_line)
    
    
    
    