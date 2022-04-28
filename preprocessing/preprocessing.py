import pandas as pd
from utils.utils import get_all_source, get_content, extract_details
import swifter
from utils.utils import get_token, clean_java
import numpy as np
import re


class BugReportsPreprocess(object):
    def __init__(self, table_path):
        self.data = pd.read_csv(table_path, sep='\t', header=0)

    def transform(self):
        self.data['summary'] = self.data['summary'].str.extract(
            r'Bug[ \d]+(.+)',
            expand=False)
        self.data['description'] = self.data['description'].fillna("")
        self.data['content'] = self.data['summary'] + ". " + self.data[
            'description']
        self.data['report_time'] = pd.to_datetime(self.data['report_time'])
        self.data['fixed_files'] = self.data['files'].str.split()
        self.data.drop(
            ['id', 'bug_id', 'report_timestamp', 'status', 'commit', 'files',
             'commit_timestamp', 'Unnamed: 10'], axis=1, inplace=True)


class SourceFilesPreprocess(object):
    def __init__(self, root_path):
        self.ROOT_PATH = root_path
        self.data = pd.DataFrame(columns=['all_content', 'comments',
                                          'class_names', 'attributes',
                                          'method_names', 'variables',
                                          'relative_path', 'package_name'])

    def get_root(self):
        return self.ROOT_PATH

    def transform(self):
        self.data['relative_path'] = get_all_source(self.get_root())
        self.data['all_content'] = self.data['relative_path'].swifter.apply(
            get_content)
        self.data[['comments', 'class_names', 'attributes', 'method_names',
                   'variables', 'package_name'
                   ]] = self.data['all_content'].swifter.apply(extract_details)
        tokens = np.unique(get_token(self.data['all_content']))
        self.data['cleaned_content'] = clean_java('  '.join(tokens))

    # def vocab_construct(self):
    #     "Construct vocab from all source files"
    #     tokens = []
    #     for raw_content in self.data['all_content']:
    #         token = np.unique(get_token(raw_content))
    #         tokens.extend(token)
    #     tokens = np.unique(tokens)
    #     # enrich vocab by split into their components based on capital letters
    #     new = []
    #     for t in tokens:
    #         new.extend(re.sub( r"([A-Z\d]*[a-z\d]*)", r" \1", t).split())
    #     # clean token
    #     tokens = clean_java('  '.join(np.append(tokens)).split()
    #     return np.unique([i.lower() for i in np.append(tokens, new)])


if __name__ == '__main__':
    s = BugReportsPreprocess("data/bug reports/Birt.txt")
    s.transform()
    print(s.data.head())
