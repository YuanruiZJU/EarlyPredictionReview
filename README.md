# Eearly Prediction of Merged Code Changes to Prioritize Reviewing Tasks
This is the code for crawling gerrit code reviews and running experiments in our paper: "Early Prediction of Merged Code Changes to Prioritize Reviewing Tasks"

# How to Run
1. Set up the crawling path in `crawl/config.py`
2. Just run `crawl/crawl.py`
3. Feature extractor: you need to import the code to Pycharm and run extractors in `features`, `baseline_features` and `icse_paper_features`. You need to first run `download_reviewers.py` to get all reviewers of code reviews.
4. Classifier code: you need to import the java code in `code/` to eclipse and run.

# Citation
If you find our code useful, please cite:

```
@article{fan2018early,
  title={Early prediction of merged code changes to prioritize reviewing tasks},
  author={Fan, Yuanrui and Xia, Xin and Lo, David and Li, Shanping},
  journal={Empirical Software Engineering},
  volume={23},
  number={6},
  pages={3346--3393},
  year={2018},
  publisher={Springer}
}

```
