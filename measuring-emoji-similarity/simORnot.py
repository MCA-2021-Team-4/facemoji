import numpy
from sys import argv


def main():
    if(len(argv) < 4):
        usage_msg = """
        USAGE:
               python3 simORnot.py {platform} {metric} {threshold}
               -> only print mean & std for that threshold

               python3 simORnot.py {platform} {metric} {threshold} 1
               -> Also generates bitmap.txt at results/{platform}/bitmap_{metric}_{threshold}.txt

               platform: Apple, Facebook, Google, Samsung, Twitter
               metric: psnr, rmse, sam, sre, ssim

        """
        print(usage_msg)
        quit()
    platform = argv[1]
    metric = argv[2]
    thres = argv[3]
    bitgen = 0 if len(argv) == 4 else argv[4]

    result_loc = "results/{}/{}.txt".format(platform, metric)
    result = open(result_loc, 'r')
    
    bitmap_loc = "results/{}/bitmap_{}_{}.txt".format(platform, metric, thres)
    if(bitgen):
        bitmap = open(bitmap_loc, 'w')


    bigger = 1 if(metric != "rmse") else 0
    smaller = 0 if(bigger == 1) else 1
    count = [0] * 95 # num of emojis = 95
    emoji_index = 0

    while True:
        line = result.readline()
        if not line: break
        listline = list(line.split(' '))
        for val in listline:
            if(bitgen):
                bitmap.write("{} ".format(bigger if val > thres else smaller))
            count[emoji_index] = count[emoji_index] + (val > thres)
        if(bitgen):
            bitmap.write("\n")
    if(bitgen):
        bitmap.close()

    print("mean:{}".format(numpy.mean(count)))
    print("std:{}".format(numpy.std(count)))
    if(bitgen):
        print("bitmap generated at {}".format(bitmap_loc))

    


if __name__ == "__main__":
    main()
