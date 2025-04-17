from http.client import responses

import grpc
from pkg_resources import empty_provider

import HospitalAppointment_pb2
import HospitalAppointment_pb2_grpc

def run():
    # 连接到 gRPC 服务器
    channel = grpc.insecure_channel('localhost:50051')
    stub = HospitalAppointment_pb2_grpc.AppointmentServiceStub(channel)

    Appointment = getattr(HospitalAppointment_pb2, 'Appointment')
    AppointmentId = getattr(HospitalAppointment_pb2, 'AppointmentId')
    PatientName = getattr(HospitalAppointment_pb2, 'PatientName')
    Empty = getattr(HospitalAppointment_pb2, 'Empty')

    while True:
        print("请选择功能（输入对应的数字）：")
        print("1.预约挂号")
        print("2.通过ID查询预约信息")
        print("3.通过病人查询预约信息")
        print("4.取消预约")
        print("5.退出")
        opt = int(input())
        # 预约挂号
        if opt == 1:
            empty = Empty()
            size = stub.getStorageSize(empty)
            appointmentid = size.size + 1
            patientname = input("请输入病人姓名：")
            doctorname = input("请输入医生姓名：")
            department = input("请输入科室：")
            date = input("请输入预约日期：")
            timeSlot = input("请输入预约时间段：")
            appointment = Appointment(
                appointmentId = appointmentid,
                patientName = patientname,
                doctorName=doctorname,
                department=department,
                date=date,
                timeSlot=timeSlot
            )
            response = stub.bookAppointment(appointment)
            if response.res:
                print(f"预约成功，预约ID为： {appointmentid}" )
            else:
                print("预约失败")
            print("------------------------------")
        # 通过ID查询
        elif opt == 2:
            appointmentid = int(input("请输入查询的预约ID："))
            query_by_id = AppointmentId(appointmentId=appointmentid)
            response = stub.queryById(query_by_id)
            if not response.ListFields():
                print("没有查询到该ID对应的信息")
            else:
                print("查询结果为：")
                print(response)
            print("------------------------------")
        # 通过病人姓名查询
        elif opt == 3:
            patientname = input("请输入要查询的病人姓名：")
            query_by_name = PatientName(patientName=patientname)
            response = stub.queryByPatient(query_by_name)
            if len(response.appointments) == 0:
                print("没有该病人的挂号记录")
            else:
                print("查询结果为：")
                print(response)
            print("------------------------------")
        elif opt == 4:
            appointmentid = int(input("请输入需要取消预约的ID："))
            cancel_appointment = AppointmentId(appointmentId=appointmentid)
            response = stub.cancelAppointment(cancel_appointment)
            if response.res:
                print("取消成功")
            else:
                print("取消失败")
        else:
            break

if __name__ == '__main__':
    run()
