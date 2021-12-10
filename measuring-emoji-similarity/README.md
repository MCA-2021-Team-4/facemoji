# Measuring emoji similarity
Measuring similarity between emojis with image similarity metrics.

### Environment Setup
- 95 face emojis, from 😀 to 🤬 [source: emojipedia](https://emojipedia.org/people/)
- Measured at 5 different platforms (Apple, Facebook, Google, Samsung, Twitter)
- Measured with 5 diffrent metrics ([PSNR](https://en.wikipedia.org/wiki/Peak_signal-to-noise_ratio), [RMSE](https://en.wikipedia.org/wiki/Root-mean-square_deviation), [SAM](https://ntrs.nasa.gov/citations/19940012238), [SRE](https://www.sciencedirect.com/science/article/abs/pii/S0924271618302636), [SSIM](https://en.wikipedia.org/wiki/Structural_similarity))

### Raw measurement result (measure.py)
- `python3 measure.py` makes result at `results/{platform}/{metric}.txt` for 5 platforms and 5 metrics.
- `results/label.txt` contains unique index given to each emoji.
- i'th line is in format of `{similarity between emoji_i and emoji_0} {similarity between emoji_i and emoji_1} ...`
- Except RMSE, bigger calculated value = more similar images. (smaller is more similar for RMSE)

### Threhold based similarity judgement (similar_or_not.py)
- `python3 similar_or_not.py {platform} {metric} {threshold}` for printing statistics(average & std. for number of similar emojis for each emoji) for given threhold.
- `python3 similar_or_not.py {platform} {metric} {threshold} 1` for printing statistics and generating bitmap table of similar or not similar emojis at `results/{platform}/bitmap_{metirc}_{threshold}`
- i'th line is in format of `{1 if emoji_i and emoji_0 is similar, else 0} {1 if emoji_i and emoji_1 is similar, else 0} ...`

### Visualization based on similarity result (draw_table.py)
- `python3 draw_table.py {platform} {metric} {top_k}` for see visualization
If top_k is 3, the table has 3 columns and 95 rows. 
Each i-th row contains the k emojis which is most similar to the i-th emoji.
The preferred value of top_k is 8 or less.

### Merging
- `python3 merge_platforms.py {desired number of similar emojis per emoji, say N} {output directory}`
- Merge results of platforms for each metric.
- Algorithm : 
1. For specific metric, fetch ith line from results of every platform.
2. Select top k similar emojis from each platform for `k = N`, and intersect those results.
3. If size of intersection is smaller than N, repeat step 2 with `k = k + 1`. Else, select top N results with largest sum of measured value.
4. Repeat step 1..3 for i = 0..94
5. Write merged result at "{output_dir}/{metric}.txt" with each ith line in format of `{index of most similar emoji with emoji_i} {index of next most similar emoji with emoji_i} ...`
- Output shows statistic info. of value of k required to satisfy get intersection of size N for each metrics. (avg, min, max, std, histogram)
