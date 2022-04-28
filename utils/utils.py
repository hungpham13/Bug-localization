import javalang
import pathlib as pl
import os
import re
import pygments
from pygments.lexers import JavaLexer
from pygments.token import Token
import pandas as pd


def get_all_source(root):
    all_source_path = []
    for path, subdirs, files in os.walk(root):
        for name in files:
            # just source file
            if name.endswith('.java'):
                all_source_path.append(str(pl.PurePath(path, name)))
    return sorted(all_source_path)


def print_long_list(tokens, limit='all'):
    """
    Print a very long list with specific range
    :param tokens: list
    :param limit: 2-elements tuple (start index, end index) or 'all'
    :return: None
    """
    if limit == 'all':
        start, end = 0, len(tokens)
    else:
        assert len(limit) == 2
        start, end = limit
    for i in range(start, end, 10):
        for k in range(i, i + 10):
            if k == end:
                break
            print(tokens[k], end="\t")
        print("\n")


def get_content(file_path):
    """
    Get full content from an individual file path
    :param file_path: relative path, string
    :return: String
    """
    with open(file_path, 'rb') as f:
        text = f.read().decode(errors='replace')
        return text.replace('\ufffd', "")


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


def get_ast(file_path, print_result=False):
    with open(file_path) as f:
        tree = javalang.parse.parse(f.read())
    if print_result:
        for path, node in tree:
            # print(path)
            print(node)
    return tree


def get_token(string, print_result=False, ignore_error=True):
    tokens = javalang.tokenizer.tokenize(string, ignore_errors=ignore_error)
    tokens = [t.value for t in tokens]
    if print_result:
        print_long_list(tokens)
    return tokens


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


def to_relative_path(full_path, src_dir):
    assert full_path[:len(src_dir)] == src_dir
    return full_path[len(src_dir):]


def to_full_path(relative_path, src_dir):
    return src_dir + relative_path
