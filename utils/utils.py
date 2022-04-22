import javalang


def print_long_list(tokens, limit = 'all'):
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
        for k in range(i, i+10):
            if k == end:
                break
            print(tokens[k],end="\t")
        print("\n")


def get_content(file_path):
    """
    Get full content from an individual file path
    :param file_path: relative path, string
    :return: String
    """
    with open(file_path, 'rb') as f:
        text = f.read().decode(errors='replace')
        return text.replace('\ufffd',"")

def get_ast(file_path, print_result = False):
    with open(file_path) as f:
        tree = javalang.parse.parse(f.read())
    if print_result:
        for path, node in tree:
            # print(path)
            print(node)
    return tree

def get_token(string, print_result = False, ignore_error= True):
    tokens = javalang.tokenizer.tokenize(string, ignore_errors=ignore_error)
    tokens = [t.value for t in tokens]
    if print_result:
        print_long_list(tokens)
    return tokens

def to_relative_path(full_path, src_dir=src_dir):
    assert full_path[:len(src_dir)] == src_dir
    return full_path[len(src_dir):]

def to_full_path(relative_path, src_dir=src_dir):
    return src_dir + relative_path
