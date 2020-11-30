package com.atguigu.ct.analysis.io;

import com.atguigu.ct.common.util.JDBCUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * MySQL数据格式化输入对象
 */
public class MySQLTextOutputFormat extends OutputFormat<Text, Text> {

    protected static class MySQLRecordWriter extends RecordWriter<Text, Text> {

        private Connection connection = null;
        private Jedis jedis = null;
        Map<String, Integer> userMap = new HashMap<String, Integer>();
        Map<String, Integer> dateMap = new HashMap<String, Integer>();

        public MySQLRecordWriter() {
            // 获取资源
            connection = JDBCUtil.getConnection();
//            jedis = new Jedis("192.168.190.102",6379);

            PreparedStatement pstat = null;
            ResultSet rs = null;

            try {

                String queryUserSql = "select id, tel from ct_user";
                pstat = connection.prepareStatement(queryUserSql);
                rs = pstat.executeQuery();
                while ( rs.next() ) {
                    Integer id = rs.getInt(1);
                    String tel = rs.getString(2);

                    userMap.put(tel, id);
                    System.out.println(userMap.keySet());
                }

                rs.close();

                String queryDateSql = "select id, year, month, day from ct_date";
                pstat = connection.prepareStatement(queryDateSql);
                rs = pstat.executeQuery();
                while ( rs.next() ) {
                    Integer id = rs.getInt(1);
                    String year = rs.getString(2);
                    String month = rs.getString(3);
                    if ( month.length() == 1 ) {
                        month = "0" + month;
                    }
                    String day = rs.getString(4);
                    if ( day.length() == 1 ) {
                        day = "0" + day;
                    }

//
//                    dateMap.put("2018",1);
//                    dateMap.put("201801",2);
//                    dateMap.put("201802",1000);
//                    dateMap.put("201803",1001);
//                    dateMap.put("201804",1002);
//                    dateMap.put("201805",1003);
//                    dateMap.put("201806",1004);
//                    dateMap.put("201807",1005);
//                    dateMap.put("201808",1006);
//                    dateMap.put("201809",1007);
//                    dateMap.put("201810",1008);
//                    dateMap.put("201811",1009);
//                    dateMap.put("201812",1010);


                    dateMap.put(year + month + day, id);

                    System.out.println(dateMap);

//                    System.out.println(dateMap.values());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if ( rs != null ) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if ( pstat != null ) {
                    try {
                        pstat.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 输出数据
         * @param key
         * @param value
         * @throws IOException
         * @throws InterruptedException
         */
        public void write(Text key, Text value) throws IOException, InterruptedException {

            String[] values = value.toString().split("_");
            String sumCall = values[0];
            String sumDuration = values[1];

            PreparedStatement pstat = null;
            try {
                String insertSQL = "insert into ct_call ( telid, dateid, sumcall, sumduration ) values ( ?, ?, ?, ? )";
                pstat = connection.prepareStatement(insertSQL);

                String k = key.toString();
//                System.out.println(k);
                String[] ks = k.split("_");

                String tel = ks[0];
                String date = ks[1];
//                System.out.println(date);
//                System.out.println(userMap.get(tel));
//                System.out.println(dateMap.get(date));
//                System.out.println(userMap.get(tel));
                pstat.setInt(1, userMap.get(tel));
                pstat.setInt(2, dateMap.get(date));
//                pstat.setInt(1, Integer.parseInt(jedis.hget("ct_user",tel)));
//                pstat.setInt(2, Integer.parseInt(jedis.hget("ct_date",date)));
                pstat.setInt(3, Integer.parseInt(sumCall) );
                pstat.setInt(4, Integer.parseInt(sumDuration));
                pstat.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if ( pstat != null ) {
                    try {
                        pstat.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 释放资源
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        public void close(TaskAttemptContext context) throws IOException, InterruptedException {
            if ( connection != null ) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
//            if (jedis != null){
//                jedis.close();
//            }
        }
    }

    public RecordWriter<Text, Text> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException {
        return new MySQLRecordWriter();
    }

    public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {

    }
    private FileOutputCommitter committer = null;
    public static Path getOutputPath(JobContext job) {
        String name = job.getConfiguration().get(FileOutputFormat.OUTDIR);
        return name == null ? null: new Path(name);
    }
    public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
        if (committer == null) {
            Path output = getOutputPath(context);
            committer = new FileOutputCommitter(output, context);
        }
        return committer;
    }
}
