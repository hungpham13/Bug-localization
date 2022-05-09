import os
import javalang
import pathlib as pl
import pandas as pd
import re
from sklearn.feature_extraction.text import CountVectorizer
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from preprocessing.preprocessing import SourceFilesPreprocess, BugReportsPreprocess
from tqdm import tqdm
import traceback


# Feature 1 - Rvsm
def tfidf_similarity(src_strings, report_strings):
    """
    Compute similarity matrix for each src and report
    :param src_strings: list of Source content in string
    :param report_strings: list of Reports content in string
    :return: Numpy tf-idf similarity matrix
    """
    tfidf = TfidfVectorizer(sublinear_tf=True, smooth_idf=False)
    src_tfidf = tfidf.fit_transform(src_strings)
    reports_tfidf = tfidf.transform(report_strings)


# Feature 3 - Class name similarity
# def get_class_name(f):
#     for i in range(len(f)-1,-1,-1):
#         if f[i] == "/":
#             return f[i+1:len(f)-5]

def class_name_similarity(class_name, bug_content):
    return len(class_name) if class_name in bug_content else 0


# Feature 4 - Bug Fixing Frequency
# extract month
# def get_timestamp(bug_id,bug_df):
#     return pd.to_datetime(bug_df['report_time'])[bug_id]

def bug_fixing_frequency(bug_index, src_index, bbr_df):
    if bbr_df.empty:
        return 0
    last_time = get_timestamp(bbr_df['report_timestamp'].idxmax(), bug_df=bbr_df)
    bug_time = get_timestamp(bug_index)
    return (int((bug_time-last_time)/np.timedelta64(1, 'M')) + 1)**-1

def extract(bug):
