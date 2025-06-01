from mpi4py import MPI
from math import sin

def f(x):
    return sin(x * x + x)

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

if rank == 0:
    a = float(input("请输入定积分下限："))
    b = float(input("请输入定积分上限："))
else :
    a = None
    b = None

a = comm.bcast(a, root=0)
b = comm.bcast(b, root=0)

n = 10000000
chunk_size = n // size
length = (b - a) / n

partial_res = 0
for i in range(0, chunk_size):
    l = a + (rank * chunk_size + i) * length
    partial_res += length * (f(l) + f(l + length)) * 0.5

total_res = comm.reduce(partial_res, MPI.SUM, root=0)

if rank == 0:
    print(f"近似值为：{total_res}")
