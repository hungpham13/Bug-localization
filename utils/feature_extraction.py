import os
import javalang
import pathlib as pl
import pandas as pd
import re
from sklearn.feature_extraction.text import CountVectorizer
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from tqdm import tqdm
import traceback




# Feature 3
def get_class_name(f):
    for i in range(len(f)-1,-1,-1):
        if f[i] == "/":
            return f[i+1:len(f)-5]

def compute_feature_3(bug_index, src_index, bug_content,
                      all_src_path = all_src_path):
    # for i,(x,y) in enumerate(zip(bug_indexes, src_indexes)):
    class_name = get_class_name(all_src_path[src_index])
    return len(class_name) if class_name in bug_content[bug_index] else 0


# Feature 4
# extract month
def get_timestamp(bug_id,bug_df=bug_rp_df):
    return pd.to_datetime(bug_df['report_time'])[bug_id]

def compute_feature_4(bug_index, src_index, bbr_df):
    if bbr_df.empty:
        return 0
    last_time = get_timestamp(bbr_df['report_timestamp'].idxmax(), bug_df=bbr_df)
    bug_time = get_timestamp(bug_index)
    return (int((bug_time-last_time)/np.timedelta64(1, 'M')) + 1)**-1

