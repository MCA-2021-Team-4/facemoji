from sys import argv

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
    
    minRank = 80000
    for plat in platforms:
        rank = get_platform_rank(metric, plat)
        if rank < minRank:
            minRank = rank
            selected_platform = plat
    
    print(selected_platform)
    
    