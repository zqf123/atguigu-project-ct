package com.atguigu.ct.common.bean;

import java.io.Closeable;
import java.io.IOException;

/*
* 生产者接口
* */
public interface Producer extends Closeable {

    public void setIn(DataIn in);
    public void setOut(DataOut out);

    /*生产数据
    * */
    public void produce() throws Exception;
}
