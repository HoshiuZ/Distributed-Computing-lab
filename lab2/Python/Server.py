from concurrent import futures
import grpc
import HospitalAppointment_pb2
import HospitalAppointment_pb2_grpc

appointment_storage = {}

class AppointmentServiceServicer(HospitalAppointment_pb2_grpc.AppointmentServiceServicer):
    # 预约挂号
    def bookAppointment(self, request, context):
        print("预约挂号的信息为：")
        print(request)
        print("------------------------------")
        BoolResult = getattr(HospitalAppointment_pb2, 'BoolResult')
        appointment_storage[request.appointmentId] = request
        return BoolResult(res=True)
    # 通过ID查询
    def queryById(self, request, context):
        print("查询的ID为：")
        print(request)
        print("------------------------------")
        Appointment = getattr(HospitalAppointment_pb2, 'Appointment')
        appointment = appointment_storage.get(request.appointmentId)
        if appointment is None:
            print("没有找到该挂号ID")
            return Appointment()
        else:
            return appointment
    # 通过病人姓名查询
    def queryByPatient(self, request, context):
        print("查询的病人姓名为：")
        print(request)
        print("------------------------------")
        patient_name = request.patientName
        AppointmentList = getattr(HospitalAppointment_pb2, 'AppointmentList')
        appointment_list = AppointmentList()
        for appointment in appointment_storage.values():
            if appointment.patientName == patient_name:
                appointment_list.appointments.add(
                    appointmentId = appointment.appointmentId,
                    patientName = appointment.patientName,
                    doctorName = appointment.doctorName,
                    department = appointment.department,
                    date = appointment.date,
                    timeSlot = appointment.timeSlot
                )
        if len(appointment_list.appointments) == 0:
            print("没有找到该病人挂的号")
            return appointment_list
        else:
            return appointment_list

    # 取消预约
    def cancelAppointment(self, request, context):
        print("取消的挂号ID为：")
        print(request)
        print("------------------------------")
        BoolResult = getattr(HospitalAppointment_pb2, 'BoolResult')
        appointmentId = request.appointmentId
        if appointmentId in appointment_storage:
            del appointment_storage[appointmentId]
            return BoolResult(res=True)
        else:
            print("没有找到该挂号ID，取消失败")

    # 获取当前预约信息数量
    def getStorageSize(self, request, context):
        StorageSize = getattr(HospitalAppointment_pb2, 'StorageSize')
        size = len(appointment_storage)
        return StorageSize(size=size)

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    HospitalAppointment_pb2_grpc.add_AppointmentServiceServicer_to_server(AppointmentServiceServicer(), server)
    print("正在启动服务端")
    server.add_insecure_port('[::]:50051')
    server.start()
    print("服务端已启动，在端口 50051 监听")
    server.wait_for_termination()

if __name__ == '__main__':
    serve()