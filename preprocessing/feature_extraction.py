import pandas as pd
from sklearn.metrics.pairwise import linear_kernel
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from preprocessing.preprocessing import SourceFilesPreprocess, \
    BugReportsPreprocess
from utils.utils import joins
from tqdm import tqdm
import multiprocessing as mp
import swifter
import traceback


# Feature 2 - Colaborative Filtering
def colab_filtering(bug_tfidf, prev_content, tfidf):
    """
    :param bug_tfidf: 1-D tf-idf matrix of current bug
    :param prev_content: String - Joined of all previous bug content
    :param tfidf: Fitted TFIDFVectorizer of the project
    :return:
    """
    if not prev_content:
        return 0
    wbr = tfidf.transform([prev_content])
    sim = linear_kernel(wbr, bug_tfidf.reshape(1,
                                               -1))  # change bug_tfidf shape to 2D
    return float(sim.squeeze())


# Feature 3 - Class name similarity
def class_name_similarity(bug_content, class_names):
    """Take the maximum length all class names that contains in bug content"""
    if len(class_names) == 0:
        return 0
    return max([len(c) if c in bug_content else 0 for c in class_names])


# Feature 4 - Bug Fixing Frequency
def bug_fixing_recency(bug_time, last_time):
    if last_time is None:
        return 0
    else:
        return (int((bug_time - last_time) / np.timedelta64(1, 'M')) + 1) ** -1


# Feature 6 - 13
def sim(content_1, content_2, tfidf):
    w1 = tfidf.transform([content_1])
    w2 = tfidf.transform([content_2])
    return float(linear_kernel(w1, w2).squeeze())


class ExtractFeature(object):
    def __init__(self, **kwargs):
        self.name = kwargs['name']
        self.sources = SourceFilesPreprocess(kwargs['source_root'])
        self.bugs = BugReportsPreprocess(kwargs['bugs_tsv_path'])
        print("Start preprocessing data...")
        self.sources.transform()
        self.bugs.transform()
        print("Done preprocessing data.")

        self.tfidf = TfidfVectorizer(sublinear_tf=True, smooth_idf=False)
        src_strings = self.sources.data['cleaned_content'].tolist()
        report_strings = self.bugs.data['content'].tolist()

        self.src_tfidf = self.tfidf.fit_transform(src_strings)
        self.bug_tfidf = self.tfidf.transform(report_strings)

        self.result = None

    def get_prev_details(self, bug_index, src_index):
        """Get before bug content
        input:  bug_index taken from bug report dataframe,
                src path is relative path
        return: Pandas dataframe that contains all previous bug report, with the same template as Bug Preprocess class data
        """

        report_time = self.bugs.data['report_time'].loc[bug_index]
        src_path = self.sources.data['relative_path'].loc[
            src_index]  # relative path
        df = self.bugs.data
        prev_bug_df = df.loc[(df['fixed_files'].str.contains(src_path)) &
                             (df.index != bug_index) &
                             (df['report_time'] < report_time)]
        if prev_bug_df.empty:
            return '', None, 0
        else:
            content = " ".join(prev_bug_df['content'])
            last_time = prev_bug_df['report_time'].max()
            length = len(prev_bug_df)
            return content, last_time, length

    def compute_features(self, x, y):
        prev_content, last_time, length = self.get_prev_details(x, y)

        bug = self.bugs.data.loc[x]
        src = self.sources.data.loc[y]

        try:
            feature_2 = colab_filtering(self.bug_tfidf[x], prev_content,
                                        self.tfidf)
            feature_3 = class_name_similarity(bug['content'],
                                              src['class_names'])
            feature_4 = bug_fixing_recency(bug['report_time'], last_time)
            feature_5 = length
            feature_6 = sim(bug['summary'], joins(src['class_names']),
                            self.tfidf)
            feature_7 = sim(bug['summary'], joins(src['method_names']),
                            self.tfidf)
            feature_8 = sim(bug['summary'], joins(src['variables']), self.tfidf)
            feature_9 = sim(bug['summary'], src['comments'], self.tfidf)
            feature_10 = sim(bug['description'], joins(src['class_names']),
                             self.tfidf)
            feature_11 = sim(bug['description'], joins(src['method_names']),
                             self.tfidf)
            feature_12 = sim(bug['description'], joins(src['variables']),
                             self.tfidf)
            feature_13 = sim(bug['description'], src['comments'], self.tfidf)

        except Exception as e:
            print(f"Error on bug_id {x}, source_id {y}")
            print(e)
            traceback.print_exc()
        else:
            return pd.Series({'f2_colab_filtering': feature_2,
                              'f3_class_similarity': feature_3,
                              'f4_bug_fixing_recency': feature_4,
                              'f5_bug_fixing_frequency': feature_5,
                              'f6_summary_classes_similarity': feature_6,
                              'f7_summary_methods_similarity': feature_7,
                              'f8_summary_variables_similarity': feature_8,
                              'f9_summary_comments_similarity': feature_9,
                              'f10_description_classes_similarity': feature_10,
                              'f11_description_methods': feature_11,
                              'f12_description_variables_similarity': feature_12,
                              'f13_description_comments_similarity': feature_13,
                              })

    def extract_feature(self):
        sources = self.sources.data
        bugs = self.bugs.data
        print("Start extracting features...")
        feature_1 = linear_kernel(self.bug_tfidf, self.src_tfidf,
                                  dense_output=True)

        assert len(bugs) == feature_1.shape[0]
        assert len(sources) == feature_1.shape[1]

        print("Creating feature table using rvsm values...")

        feature_df = pd.DataFrame(columns=['bug', 'src', 'f1_rvsm', 'label'])
        for i in tqdm(range(len(bugs))):
            contaminated_src = bugs['fixed_files'].loc[i]  # relative path
            contaminated_index = [
                sources.index[sources.relative_path == f].tolist()[0]
                for f in contaminated_src
            ]

            not_contaminated_index = [k for k in range(len(sources))
                                      if k not in contaminated_index]
            negative_300 = sorted(not_contaminated_index,
                                  key=lambda y: feature_1[i][y])[:300]
            new_src_index = contaminated_index + negative_300
            labels = [1] * len(contaminated_index) + [0] * len(negative_300)

            # bug_src_indexes.extend([(i,s) for s in new_src_index])
            # bug_indexes, src_indexes = list(zip(*bug_src_indexes))
            feature_df = feature_df.append(
                pd.DataFrame({'bug': [i] * len(new_src_index),
                              'src': new_src_index,
                              'f1_rvsm': [feature_1[i][y] for y in
                                          new_src_index],
                              'label': labels,
                              }), ignore_index=True)
        feature_df.head()
        # Extract other features
        print("Start extracting other features...")
        other_df = feature_df.swifter.apply(
            lambda x: self.compute_features(x.bug, x.src),
            axis=1, result_type='expand', )

        self.result = pd.concat([feature_df, other_df], axis=1)

    def save_to_csv(self):
        if not self.result:
            raise ReferenceError("Not extracted yet")
        else:
            self.result.to_csv(f"{self.name}.csv")


if __name__ == '__main__':
    config = {
        'name': "Birt",
        'source_root': "data/source files/birt-20140211-1400/",
        'bugs_tsv_path': "data/bug reports/Birt.txt"
    }

