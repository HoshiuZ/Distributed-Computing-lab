from mpi4py import MPI
from math import log, sin, cos, tan, asin, acos, atan

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

if rank == 0:
    print("1.幂函数   f(x) = x^n")
    print("2.指数函数  f(x) = a^x")
    print("3.对数函数  f(x) = log_a(x)")
    print("4.正弦函数  f(x) = sin(x)")
    print("5.余弦函数  f(x) = cos(x)")
    print("6.正切函数  f(x) = tan(x)")
    print("7.反正弦函数 f(x) = arcsin(x)")
    print("8.反余弦函数 f(x) = arccos(x)")
    print("9.反正切函数 f(x) = arctan(x)")
    opt = int(input("请选择函数类型："))
    lower = float(input("请输入定积分的下限："))
    upper = float(input("请输入定积分的上限："))
    if opt == 1:
        n = float(input("请输入幂函数的幂次："))
    elif opt == 2:
        a = float(input("请输入指数函数的底数："))
    elif opt == 3:
        a = float(input("请输入对数函数的底数："))
else:
    opt = None
    lower = None
    upper = None
    n = None
    a = None

opt = comm.bcast(opt, root=0)
lower = comm.bcast(lower, root=0)
upper = comm.bcast(upper, root=0)
if opt == 1:
    n = comm.bcast(n, root=0)
elif opt == 2:
    a = comm.bcast(a, root=0)
elif opt == 3:
    a = comm.bcast(a, root=0)

length = (upper - lower) / size
lower = lower + length * rank
upper = lower + length

local_res = 0

if opt == 1:
    local_res += length * (lower ** n + upper ** n) / 2
elif opt == 2:
    local_res += length * (a ** lower + a ** upper) / 2
elif opt == 3:
    local_res += length * (log(lower, a) + log(upper, a)) / 2
elif opt == 4:
    local_res += length * (sin(lower) + sin(upper)) / 2
elif opt == 5:
    local_res += length * (cos(lower) + cos(upper)) / 2
elif opt == 6:
    local_res += length * (tan(lower) + tan(upper)) / 2
elif opt == 7:
    local_res += length * (asin(lower) + asin(upper)) / 2
elif opt == 8:
    local_res += length * (acos(lower) + acos(upper)) / 2
elif opt == 9:
    local_res += length * (atan(lower) + atan(upper)) / 2

total_res = comm.reduce(local_res, MPI.SUM, root=0)
if rank == 0:
    print(f"近似值为：{total_res}")

# print(f"rank 为 {rank}, lower : {lower}, upper : {upper}, local_res : {local_res}")