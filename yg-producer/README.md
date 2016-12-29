1.#配置测试数据库，开启binlog
cat  /etc/my.cnf　

# 开启二进制日志功能，可以随便取，最好有含义
log-bin=mysql-bin
# 为每个session 分配的内存，在事务过程中用来存储二进制日志的缓存
binlog_cache_size=1M
# 主从复制的格式, 选择row模式，虽然Canal支持各种模式，但是想用otter，必须用ROW模式
binlog-format=ROW
# 二进制日志自动删除/过期的天数。默认值为0，表示不自动删除
expire_logs_days=10
#设置server_id，一般设置为IP
server_id=121
#跳过主从复制中遇到的所有错误或指定类型的错误，避免slave端复制中断
# 如：1062错误是指一些主键重复，1032错误是因为主从数据库数据不一致
slave_skip_errors=1062
--------------------------------------------------------------------
2.编译canal
git clone https://github.com/alibaba/canal.git
mvn clean install -Dmaven.test.skip -Denv=release
--------------------------------------------------------------------
3.添加Canal用户：
CREATE USER canal IDENTIFIED BY 'canal';
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
FLUSH PRIVILEGES;
---------------------------------------------------------------------------
4.在编译好的目录下的target中找到canal.deployer-1.0.23-SNAPSHOT.tar.gz，解压
mkdir /usr/lib/canal
tar -zxvf canal.deployer-1.0.23-SNAPSHOT.tar.gz  -C /usr/lib/canal/
---------------------------------------------------------------------------
5.配置conf/example/instance.properties:
## mysql serverId
canal.instance.mysql.slaveId = 122
# position info
canal.instance.master.address = 10.10.10.121:3306    #mysql所在主机地址
canal.instance.master.journal.name =mysql-bin.000001
canal.instance.master.position = 426
canal.instance.master.timestamp =
#canal.instance.standby.address =
#canal.instance.standby.journal.name =
#canal.instance.standby.position =
#canal.instance.standby.timestamp =
# username/password
canal.instance.dbUsername = canal     #mysql 用户
canal.instance.dbPassword = canal     #mysql 密码
canal.instance.defaultDatabaseName =
canal.instance.connectionCharset = UTF-8
# table regex
canal.instance.filter.regex = .*\\..*
# table black regex
canal.instance.filter.black.regex =
#################################################
订阅起始点可自定义，查看当前binlog状态：
mysql> show master status;
+------------------+----------+--------------+------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB |
+------------------+----------+--------------+------------------+
| mysql-bin.000001 |      426 |              |                  |
+------------------+----------+--------------+------------------+
1 row in set (0.00 sec)
mysql>
一般的，binlog通过文件名和position就可以定位到，timestamp一般可以不用填
-----------------------------------------------------------------------
6.配置conf/canal.properties
canal.id= 100001       ##唯一值
canal.ip=10.10.10.122  ##canal 部署所在的主机IP
canal.port= 10010      ##主机端口 自定义
canal.zkServers=10.10.10.121:2181,10.10.10.122:2181,10.10.11.69:2181

-----------------------------------------------------------------------
7.创建kafka的topic
bin/kafka-topics.sh --create --zookeeper master:2181,cps03:2181,cps04:2181 --replication-factor 2 --partitions 3 --topic  yg_binLog
------------------------------------------------------------------------
8.运行程序
9.mysql创建表及执行dml操作

