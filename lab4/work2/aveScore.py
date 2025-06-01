from mrjob.job import MRJob
from mrjob.protocol import RawProtocol

class MRAveScore(MRJob):
    OUTPUT_PROTOCOL = RawProtocol
    # Mapper函数：把每一行信息分解出来，然后为每一行信息生成一个键值对{(班级, 姓名), (必修课分数, 1)}
    def mapper(self, _, line):
        cls, name, subject, subject_type, score = line.strip().split(',')
        score = float(score)
        if subject_type == "必修":
            yield (cls, name), (score, 1)

    # Reducer函数：对同一个人的成绩进行聚合，计算每个人的必修课平均成绩。
    def reducer(self, key, values):
        total_score = 0
        total_num = 0
        for score, num in values:
            total_score += score
            total_num += num
        average_score = total_score / total_num if total_num else 0
        yield f"{key[0]} {key[1]}", f"{average_score:.2f}"

if __name__ == '__main__':
    MRAveScore().run()