package com.hzgc.collect.expand.log;

/**
 * 此为数据接收和处理的公共接口，定义了写日志的方法，实现此接口的类具有如下功能：
 * 1.根据接收到的ReceiverEvent，将事件写入指定的日志文件中
 * 2.根据规定的日志文件大小进行日志分组
 * 3.使用顺序读写的方式进行日志文件的追加
 */
public interface LogWriter {

    /**
     * 日志count检查方法
     *
     * @param event 日志信息
     */
    void countCheck(LogEvent event);

    /**
     * 写日志操作，使用追加的方法
     *
     * @param event 封装的日志信息
     */
    void action(LogEvent event);

    /**
     * 获取最大序号（count）所在的日志文件的绝对路径
     *
     * @return 日志文件的绝对路径
     */
    long getLastCount();

    /**
     * 获取指定文件的最后一行
     * @param fileName 指定文件名称
     * @return 最后一行名称
     */
    String getLastLine(String fileName);

    /**
     * 当默认日志文件写入的日志个数大于配置的个数时,
     * 需要重新再写入新的日志,之前的默认日志文件名称里会包含最后一行日志的count值
     * 此方法用来生成这个文件名称
     *
     * @param defaultName 默认写入的日志文件名称
     * @param count       要标记的count值
     * @return 最终合并的文件名称
     */
    String logNameUpdate(String defaultName, long count);

    /**
     * 前置方法，通过此方法，可以初始化当前队列的序号（count）、
     * 如果当前队列之前有日志记录，则找到最后一个序号
     * 如果未找到序号或者是第一次创建此队列，序号（count）为1
     */
    void prepare();
}
