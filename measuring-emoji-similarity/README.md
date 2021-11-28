# Measuring emoji similarity
Measuring similarity between emojis with image similarity metrics.

# Environment Setup
- 95 face emojis, from 😀 to 🤬 [source: emojipedia](https://emojipedia.org/people/)
- Measured at 5 different platforms (Apple, Facebook, Google, Samsung, Twitter)
- Measured with 5 diffrent metrics ([PSNR](https://en.wikipedia.org/wiki/Peak_signal-to-noise_ratio), [RMSE](https://en.wikipedia.org/wiki/Root-mean-square_deviation), [SAM](https://ntrs.nasa.gov/citations/19940012238), [SRE](https://www.sciencedirect.com/science/article/abs/pii/S0924271618302636), [SSIM](https://en.wikipedia.org/wiki/Structural_similarity))

# Result
- Result is at `results/{platform}/{metric}.txt` for 5 platforms and 5 metrics.
- `results/label.txt` contains unique index given to each emoji.
- Each line is in format of `{emoji_index} {similarity-with-emoji_0} {similarity-with-emoji_1} ...`
- Except RMSE, bigger calculated value = more similar images. (smaller is more similar for RMSE)
