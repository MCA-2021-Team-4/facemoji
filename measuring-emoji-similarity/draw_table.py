import os
import plotly.express as px
from sys import argv
import numpy as np
import cv2

def draw_table(img_result, col):
    
    fig = px.imshow(np.array(img_result), 
                    facet_col=0, binary_string=True, 
                    facet_col_wrap=col,
                    facet_row_spacing=0.003)
    for i in range(0, len(img_result)):
        fig.layout.annotations[i]['text'] = ""
    
    fig.update_xaxes(showticklabels=False)
    fig.update_yaxes(showticklabels=False)
    fig.update_layout(
        margin=dict(l=10, r=10, t=20, b=1),
    )
    fig['layout'].update(height=10000)
    fig['layout'].update(width=2000)
    
    fig.show()

def top_k_img_pair(platform, metric, k):
    result_loc = "results/{}/{}.txt".format(platform, metric)
    target_dir = os.path.join("emojis", platform)
    result = open(result_loc, 'r')
    
    emojis = []
    for i, emoji in enumerate(sorted(os.listdir(target_dir))):
        # ex) smiling_face.png
        emojis.append(emoji)
    
    row = 0
    img_result = []
    while True:
        line = result.readline()
        if not line:
            break
        listline = list(line.replace('inf', '100.00').split())
        listline = [(index, float(value)) for index, value in enumerate(listline)]
        if metric == 'rmse':
            sorted_listline = sorted(listline, key=lambda tup: tup[1])
        else:
            sorted_listline = sorted(listline, key=lambda tup: tup[1], reverse=True)
            
        top_k = sorted_listline[0:k]
        row_img = cv2.imread(os.path.join(target_dir, emojis[row]))
        for i in range(0, k):
            col_img = cv2.imread(os.path.join(target_dir, emojis[top_k[i][0]]))
            pair_img = np.hstack((row_img, col_img))
            img_result.append(pair_img)
            
        row += 1
        
    return img_result

if __name__ == "__main__":
    platform = argv[1] #platform
    metric = argv[2] #metric
    k = int(argv[3]) #top k
    img_result = top_k_img_pair(platform, metric, k)
    draw_table(img_result, k)
    

"""
USAGE:
       python3 draw_table.py {platform} {metric} {top_k}
       platform : Apple or Facebook ...
       metric : psnr or rmse ...
       top_k : int, show top_k most similar pairs 
       
"""