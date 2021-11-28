import cv2
import os
import time
import image_similarity_measures
from sys import argv
from image_similarity_measures.quality_metrics import *

platforms = ["Apple", "Facebook", "Google", "Samsung", "Twitter"]
   
def write_result(metric, dict, platform):
    f = open("results/{}/{}.txt".format(platform, metric), 'w')
    for i, res in enumerate(dict.values()):
        f.write("{} ".format(i))
        for value in res.values():
            f.write("{:.3f} ".format(value))
        f.write("\n")
    f.close()

def measure(platform, debug):
    print("=====")
    if(debug):
        print("[{}]".format(platform))
    else:
        print("warming up... divide by zero errors will appear")
    target_dir = os.path.join("emojis", platform)
    label = open("label.txt", 'w')
    emojis = [] 
    for i, emoji in enumerate(sorted(os.listdir(target_dir))):
        # ex) smiling_face.png
        emojis.append(emoji)
        label.write("{} {}\n".format(i, emoji))
    label.close()
    emojinum = len(emojis)

    metrics = []
    #
    # BIGGER value = MORE SIMILAR image,
    # except for rmse(smaller = more similar)
    #
    metrics.append(("rmse",rmse))
    metrics.append(("psnr",psnr)) 
    metrics.append(("ssim",ssim)) 
    #metrics.append(("fsim",fsim)) 
    #metrics.append(("issm",issm))  
    metrics.append(("sre",sre))   
    metrics.append(("sam",sam))   
    #metrics.append(("uiq",uiq))

    total_result = {}
    for metric in metrics:
        t0 = time.time()
        result = {}
        bitmap = [[0 for x in range(emojinum)] for y in range(emojinum)]
        for i in range(emojinum):
            i_img = cv2.imread(os.path.join(target_dir, emojis[i]))
            res = {}
            for j in range(emojinum):
                j_img = cv2.imread(os.path.join(target_dir, emojis[j]))
                if(bitmap[j][i] == 0):
                    res[emojis[j]] = metric[1](i_img, j_img)
                    bitmap[i][j] = 1
                else:
                    res[emojis[j]] = result[emojis[j]][emojis[i]]
            result[emojis[i]] = res
        total_result[metric[0]] = result
        if(debug):
            print("{} done in {:.2f} s".format(metric[0], time.time() - t0))
        write_result(metric[0], result, platform)
    
def main():
    measure("Apple", 0) # warming up
    for platform in platforms:
        measure(platform, 1)

if __name__ == "__main__":
    main()
