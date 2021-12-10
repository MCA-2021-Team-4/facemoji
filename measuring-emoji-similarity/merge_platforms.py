import os
import numpy
import plotille
from sys import argv

def find_topk(line: list, k, is_rmse):
    topk_ind = []
    topk_val = []
    local_line = line[:]
    for _ in range(k):
        if(is_rmse):
            min_val = min(local_line)
            min_ind = local_line.index(min_val)
            local_line[min_ind] = max(local_line)
            topk_ind.append(min_ind)
            topk_val.append(min_val)
        else:
            max_val = max(local_line)
            max_ind = local_line.index(max_val)
            local_line[max_ind] = min(local_line)
            topk_ind.append(max_ind)
            topk_val.append(max_val)
    return (topk_ind, topk_val)


def merge(metric, n):
    platform_results = []
    for platform in ["Apple", "Facebook", "Google", "Samsung", "Twitter"]:
        platform_results.append(open("results/{}/{}.txt".format(platform, metric)))

    done = False
    topn_inds_lines = []
    ks = []
    linenum = 0
    while True: # looping through lines
        topn_inds_line = []

        platform_lines = []
        for i in range(5):
            pr = platform_results[i]
            line = pr.readline()
            if not line:
                done = True
                break
            listline = list(line.split(' '))[:-1]
            platform_lines.append(listline)

        if done: break

        k = n
        while True: # looping through k = n, n+1, ...
            inds = []
            vals = []
            for i in range(5):
                if(metric == "rmse"):
                    ind, val = find_topk(platform_lines[i], k, True)
                else:
                    ind, val = find_topk(platform_lines[i], k, False)

                inds.append(ind)
                vals.append(val)

            # intersection of top k from 5 platform
            sets = list(map(set, inds))
            inter = list(sets[0] & sets[1] & sets[2] & sets[3] & sets[4])
            # remove itself
            inter.remove(linenum)

            if len(inter) >= n:
                sum_of_vals = {}
                for ind in inter:
                    sum_of_vals[ind] = 0
                    for p in range(0,5):
                        sum_of_vals[ind] = sum_of_vals[ind] + float(vals[p][inds[p].index(ind)])
                sorted_sum_of_vals = sorted(sum_of_vals.items(), key = lambda item: item[1], reverse = True)
                topn_inds_line = [i[0] for i in sorted_sum_of_vals[:n]]
                ks.append(k)
                break
            else:
                k = k + 1
        topn_inds_lines.append(topn_inds_line)
        linenum = linenum + 1

    nparr = numpy.array(ks)
    print("avg: {}".format(sum(ks)/len(ks)))
    print("min: {}".format(min(ks)))
    print("max: {}".format(max(ks)))
    print("std: {}".format(numpy.std(nparr)))
    print("hist: ")
    print(plotille.histogram(nparr, X_label="k"))
    print("")
    return topn_inds_lines # list of list of indices


def write_file(lines, output_dir):
    output = open(output_dir, 'w')
    for line in lines:
        for ind in line:
            output.write("{} ".format(ind))
        output.write("\n")
    output.close()


def main():
    if len(argv) < 3 :
        usage_msg = """
        USAGE:
               python3 merge_platform.py {number of similar emojis per line} {output directory}
        """
        print(usage_msg)
        quit()

    n = int(argv[1])
    output_dir =  argv[2]
    if not os.path.isdir(output_dir):
        print("invalid directory")
        quit()

    for metric in ["psnr", "rmse", "sre", "ssim"]: # removed sam...
        print("# Merging results for {}".format(metric))
        lines = merge(metric, n)
        write_file(lines, "{}/{}.txt".format(output_dir, metric))

if __name__ == "__main__":
    main()
