系统：Windows 11

语言：Java

IDE：Intellij IDEA

使用 maven 管理项目。

运行方式：运行多个 publisher ，并在终端中输入参数作为 device_id，再运行 Main 。若使用 Intellij IDEA 则可在运行/调试配置中添加复合，为多个 publisher 添加不同实参作为 device_id，把多个 publisher 和 Main 添加到复合中，这样点一下运行便能同时运行这些程序。

运行后在 Main 类可输入需要查看数据的 device_id，然后会在项目目录的 pic 目录（若没有会自动创建）下生成折线图，如此循环，输入 exit 退出。

