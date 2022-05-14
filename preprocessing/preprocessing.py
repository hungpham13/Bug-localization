import pandas as pd
from utils.utils import get_all_source, get_content, to_relative_path
import swifter
from nltk.stem.porter import *
from utils.utils import get_token
import numpy as np
import re
import javalang
import pygments
from pygments.lexers import JavaLexer
from pygments.token import Token


def clean_java(string):
    # https://en.wikipedia.org/wiki/Java_syntax
    stop_word = """abstract	continue	for	new	switch
    assert default	goto	package	synchronized
    boolean	do	if	private	this 
    break	double	implements	protected	throw
    byte	else	import	public	throws
    case	enum instanceof	return	transient
    catch	extends	int	short	try
    char	final	interface	static	void
    class	finally	long	strictfp	volatile
    const	float	native	super	while"""
    black_list = [i + "[\s.:;\n\t]" for i in stop_word.split()]
    black_list.append('\/\/.+\n')  # comment // in java
    black_list.append('0x[\w\d]+')  # hex 0x00000
    black_list.append('<[\w/!\-.]+>')  # html tag <\>
    black_list.append('\".+\"')  # string "..."
    black_list.append(
        '["#(\[{\s.:;,\n\t]-?[\dabcdef]+["dDfFLleE\s.,:;\n\t})\]]|[\s.,:;\n\t]\d+$|^\d+[\s.,:;\n\t]')  # number
    black_list.append(';[\d;]+')  # number sequence

    punc = """
    ( )	 [ ]	 ++ -- + - ! ~ * / % << >> >>>	 < <= > >= instanceof	 == !=	 & ^ |	 &&
    || ? : =	 += -=	 *= /= %=	 <<= >>= >>>=	 &= ^= |= ; { }  ,  . " \\ # ` @
    """
    punc_list = punc.split()

    for k in black_list:
        string = re.sub(k, ";", string)
    for p in punc_list:
        string = string.replace(p, " ")
    string = string.replace("'", "")
    return string


def camelsplit(tokens):
    new = []
    for t in tokens:
        new.extend(re.sub(r"([A-Z\d]*[a-z\d]*)", r" \1", t).split())
    return np.unique(np.append(tokens, new))


def clean_source(string):
    # tokenize
    tokens = get_token(string)
    # camelCase split to enrich the vocab
    tokens = camelsplit(tokens)
    # lowercase
    tokens = [s.lower().strip() for s in tokens]
    # potter stemer
    stemmer = PorterStemmer()
    tokens_stemed = [stemmer.stem(t) for t in tokens]
    # clean string
    return pd.Series({'cleaned_stemed': clean_java(' '.join(tokens_stemed)),
                      'cleaned': clean_java(' '.join(tokens))})


def extract_details(src):
    """ Extract comments, class, attributes, methods, variables and package
    name from a source
    :param src: String content of a source file
    :return: Pandas Series with: comments: String,
                                 class_names, attributes, method_names, variables: List,
                                 package_name: String
    """

    # Placeholder for different parts of a source file
    comments = ''
    class_names = []
    attributes = []
    method_names = []
    variables = []

    # Source parsing
    parse_tree = None
    try:
        parse_tree = javalang.parse.parse(src)
        for path, node in parse_tree.filter(javalang.tree.VariableDeclarator):
            if isinstance(path[-2], javalang.tree.FieldDeclaration):
                attributes.append(node.name)
            elif isinstance(path[-2], javalang.tree.VariableDeclaration):
                variables.append(node.name)
    except:
        pass

    # Triming the source file
    ind = False
    if parse_tree:
        if parse_tree.imports:
            last_imp_path = parse_tree.imports[-1].path
            src = src[src.index(last_imp_path) + len(last_imp_path) + 1:]
        elif parse_tree.package:
            package_name = parse_tree.package.name
            src = src[src.index(package_name) + len(package_name) + 1:]
        else:  # no import and no package declaration
            ind = True
    # javalang can't parse the source file
    else:
        ind = True

    # Lexically tokenize the source file
    lexed_src = pygments.lex(src, JavaLexer())

    for i, token in enumerate(lexed_src):
        if token[0] is Token.Comment.Multiline:
            if ind and i == 0:
                src = src[src.index(token[1]) + len(token[1]):]
                continue
            comments = comments + token[1]
        elif token[0] is Token.Name.Class:
            class_names.append(token[1])
        elif token[0] is Token.Name.Function:
            method_names.append(token[1])

    # get the package declaration if exists
    if parse_tree and parse_tree.package:
        package_name = parse_tree.package.name
    else:
        package_name = None
    return pd.Series({'comments': comments,
                      'class_names': class_names,
                      'attributes': attributes,
                      'method_names': method_names,
                      'variables': variables,
                      'package_name': package_name})


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
                                          'relative_path', 'package_name',
                                          'stemed_content', 'cleaned_content'])
        self.data['full_path'] = get_all_source(self.ROOT_PATH)
        self.data['relative_path'] = [to_relative_path(f, self.ROOT_PATH) \
                                      for f in self.data['full_path']]
        self.data['all_content'] = self.data['full_path'].apply(get_content)

    def get_root(self):
        return self.ROOT_PATH

    def transform(self):
        # extract details
        self.data[['comments', 'class_names', 'attributes', 'method_names',
                   'variables', 'package_name'
                   ]] = self.data['all_content'].swifter.apply(extract_details)
        # clean string
        self.data[['stemed_content', 'cleaned_content']] = self.data[
            'all_content'].swifter.apply(clean_source)

    def get_vocab(self, stem=True):
        """Generate vocab from all source files"""
        content = self.data.stemed_content if stem else self.data.cleaned_content
        if content.empty:
            raise ReferenceError("Not transformed yet")
        return np.unique([i for t in content for i in t.split()])


if __name__ == '__main__':
    s = SourceFilesPreprocess("data/source files/birt-20140211-1400/")
    i = 0
    print(s.data.relative_path)
    # print(s.data.all_content.loc[i])
    # print(clean_source(s.data.all_content.loc[i])['cleaned'])
    # s.transform()
    #
    # print("No stem:", len(s.get_vocab(stem=False)))
    # print("Stem:", len(s.get_vocab(stem=True)))
