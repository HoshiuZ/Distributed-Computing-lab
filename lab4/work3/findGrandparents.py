from mrjob.job import MRJob

class MRFindGrandparents(MRJob):
    # Mapper函数：输出两个键值对 `{A, ("child", B)}, {B, ("parent", A)}`，分别代表 A 的孩子是 B，B 的父母是 A。
    def mapper(self, _, line):
        parent, child = line.strip().split(',')
        yield parent, ("child", child)
        yield child, ("parent", parent)

    # Reducer函数：对同一个人的信息进行聚合，生成 grandchild-grandparent 关系
    def reducer(self, key, values):
        parents = []
        children = []
        for relation, name in values:
            if relation == "child":
                children.append(name)
            else:
                parents.append(name)
        for child in children:
            for parent in parents:
                yield child, parent

if __name__ == '__main__':
    MRFindGrandparents.run()