from pyspark import SparkContext

# 分数映射到区间函数
def score_to_range(score):
    if score < 60:
        return "0~59"
    elif score < 70:
        return "60~69"
    elif score < 80:
        return "70~79"
    elif score < 90:
        return "80~89"
    else:
        return "90~100"

def score_statistics(input_file):
    sc = SparkContext("local", "Score Statistics")

    data_rdd = sc.textFile(input_file)

    class_score = data_rdd.map(lambda line: line.split()).map(lambda fields: (fields[0], float(fields[2])))

    # 分数映射到区间
    class_score_ranges = class_score.mapValues(lambda score: (score_to_range(score), 1))

    # 将键变为 {班级, 分数区间}，方便统计数量
    class_score_counts = class_score_ranges.map(lambda x: ((x[0], x[1][0]), x[1][1]))

    class_score_result = class_score_counts.reduceByKey(lambda x, y: x + y)

    result = class_score_result.sortByKey(ascending=True, numPartitions=1).map(lambda x: (x[0][0], x[0][1], x[1]))

    result.saveAsTextFile("result")

    sc.stop()

if __name__ == "__main__":
    input_file = "input.txt"
    score_statistics(input_file)
