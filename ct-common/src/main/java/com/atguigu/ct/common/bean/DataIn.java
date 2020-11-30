package com.atguigu.ct.common.bean;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface DataIn extends Closeable {
    public void setPath(String path) throws FileNotFoundException;
    public Object read()throws IOException;
    public <T extends Data> List<T>read(Class<T>clazz) throws IOException, IllegalAccessException, InstantiationException;
}
