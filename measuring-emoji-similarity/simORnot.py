from sys import argv


def main():
    if(len(argv) != 4):
        print("")
        print(" USAGE:")
        print("       python3 simORnot.py {platform} {metric} {threshold}")
        print("")
        print(" platform: Apple, Facebook, Google, Samsung, Twitter")
        print(" metric: psnr, rmse, sam, sre, ssim")
        quit()
    platform = argv[1]
    metric = argv[2]
    thres = argv[3]

    result_loc = "results/{}/{}.txt".format(platform, metric)
    bitmap_loc = "results/{}/bitmap_{}_{}.txt".format(platform, metric, thres)
    result = open(result_loc, 'r')
    bitmap = open(bitmap_loc, 'w')


    bigger = 1 if(metric != "rmse") else 0
    smaller = 0 if(bigger == 1) else 1
    emoji_index = 0

    while True:
        line = result.readline()
        if not line: break
        listline = list(line.split(' '))
        for val in listline:
            bitmap.write("{} ".format(bigger if val>thres else smaller))
        bitmap.write("\n")
    bitmap.close()


if __name__ == "__main__":
    main()
