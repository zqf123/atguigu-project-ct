package com.atguigu.ct.producer.io;

import com.atguigu.ct.common.bean.Data;
import com.atguigu.ct.common.bean.DataIn;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LocalFileDataIn implements DataIn {

    public BufferedReader reader = null;

    public LocalFileDataIn(String path) throws FileNotFoundException {
        setPath(path);
    }

    public void setPath(String path) throws FileNotFoundException {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

    }

    public Object read() throws IOException {
        return null;
    }

    public <T extends Data> List<T> read(Class<T> clazz) throws IOException, IllegalAccessException, InstantiationException {

        List<T> ts = new ArrayList<T>();

        //从数据文件中读取所有的数据
        String line = null;
        while ((line=reader.readLine())!=null){

            T t = clazz.newInstance();
            t.setValue(line);
            ts.add(t);
        }

        //将数据转换为指定类型的对象，封装为集合返回
        return ts;
    }

    public void close() throws IOException {

        if (reader!=null)
            reader.close();
    }
}
