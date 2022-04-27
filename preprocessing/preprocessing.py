import pandas as pd
from utils.utils import get_all_source, get_content, extract_details
import swifter


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


if __name__ == '__main__':
    s = BugReportsPreprocess(
        "/home/hung/Bug-localization/data/bug reports/Birt.txt")
    s.transform()
    print(s.data.head())
