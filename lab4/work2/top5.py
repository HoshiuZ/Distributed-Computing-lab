from mrjob.job import MRJob
from mrjob.protocol import RawProtocol

class MRTop5(MRJob):
    OUTPUT_PROTOCOL = RawProtocol

    # Mapper函数：把每一行的信息分解出来，生成键值对 {班级, (姓名, 分数, 1)}
    def mapper(self, _, line):
        cls, name, subject, subject_type, score = line.strip().split(',')
        score = float(score)
        yield cls, (name, score, 1)

    # Reducer函数：对同一个班级的学生信息进行聚合，开一个 scores 字典用来统计该班级内的学生总分与科目数，用于计算平均成绩
    def reducer(self, key, values):
        scores = {}

        for name, score, num in values:
            if name not in scores:
                scores[name] = [0.0, 0]
            scores[name][0] += score
            scores[name][1] += num

        avg_scores = [(name, score / num) for name, (score, num) in scores.items()]

        top5 = sorted(avg_scores, key=lambda x: x[1], reverse=True)[:5]

        for name, score in top5:
            yield key, f"{name}\t{score:.2f}"

if __name__ == '__main__':
    MRTop5.run()