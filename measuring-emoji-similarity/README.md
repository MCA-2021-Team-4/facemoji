# Measuring emoji similarity
Measuring similarity between emojis with image similarity metrics.

### Environment Setup
- 95 face emojis, from 😀 to 🤬 [source: emojipedia](https://emojipedia.org/people/)
- Measured at 5 different platforms (Apple, Facebook, Google, Samsung, Twitter)
- Measured with 5 diffrent metrics ([PSNR](https://en.wikipedia.org/wiki/Peak_signal-to-noise_ratio), [RMSE](https://en.wikipedia.org/wiki/Root-mean-square_deviation), [SAM](https://ntrs.nasa.gov/citations/19940012238), [SRE](https://www.sciencedirect.com/science/article/abs/pii/S0924271618302636), [SSIM](https://en.wikipedia.org/wiki/Structural_similarity))

### Result
- Result is at `results/{platform}/{metric}.txt` for 5 platforms and 5 metrics.
- `results/label.txt` contains unique index given to each emoji.
- i'th line is in format of `{similarity between emoji_i and emoji_0} {similarity between emoji_i and emoji_1} ...`
- Except RMSE, bigger calculated value = more similar images. (smaller is more similar for RMSE)

### Threhold based similarity judgement
- `python3 simORnot.py {platform} {metric} {threshold}` for printing statistics(average & std. for number of similar emojis for each emoji) for given threhold.
- `python3 simORnot.py {platform} {metric} {threshold} 1` for printing statistics and generating bitmap table of similar or not similar emojis at `results/{platform}/bitmap_{metirc}_{threshold}`
- i'th line is in format of `{1 if emoji_i and emoji_0 is similar, else 0} {1 if emoji_i and emoji_1 is similar, else 0} ...`
