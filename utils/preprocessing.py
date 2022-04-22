import pandas as pd


class BugReportsPreprocess(object):
    def __init__(self, xlsx_path):
        self.df = pd.read_excel(xlsx_path)
        self.content = None
        self.timestamp = None

    def get_content_bug_rp(self):
        """
        return: a series that contains summary concat with description of each bug report"""
        # remove bug id from summary
        cleaned_summary = self.df['summary'].str.extract(r'Bug[ \d]+(.+)',
                                                         expand=False)
        s = cleaned_summary + ". " + self.df['description'].fillna("")
        return s.str.lower().tolist()

    def transform(self):
        self.content = self.get_content_bug_rp()
        self.timestamp = pd.to_datetime(self.df['report_time'])


class SourceFilesPreprocess(object):
    def __init__(self, prj_root):
        self.project_root = prj_root

if __name__ == '__main__':
    s = SourceFilesPreprocess("")

