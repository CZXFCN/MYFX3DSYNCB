package org.interactivemesh.jfx.sample3d.tuxcube;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author 【Device】（【张辉】 【Device.zhang】@tuya.com）
 * @since 2021/1/3 12:04 PM
 */
public class FileToMapUtil {
    private String line = System.getProperty("line.separator");
    private String separ = "=";
    static String basePath = System.getProperty("user.dir");
    static String filePath = "./ClientInfo.cfg";// 文件相对路径
    public static void main(String[] args) {
        FileToMapUtil fileToMap1 = new FileToMapUtil();
       // Map<String, String> oldMap = fileToMap1.fTM();
        Map<String, String> oldMap = new HashMap<>();
        oldMap.put("B","N");

        Map<String, String> newMap = new HashMap<String, String>();
        newMap.put("A", "A''");
        Map<String, String> map = fileToMap1.newMapToOldMap(newMap, oldMap, false);
        try {
            fileToMap1.mapToFileDefault(map, new File(filePath), fileToMap1.separ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 有参构造函数
     *
     * @param separ
     * @param filePath
     */
    public FileToMapUtil(String separ, String filePath) {
        this.filePath = filePath;
        this.separ = separ;
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("文件不存在");
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("路径:" + filePath + "创建失败");
                e.printStackTrace();
            }
        }
    }

    /**
     * 无参构造函数，用类默认的配置。
     */
    public FileToMapUtil() {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("文件不存在");
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("路径:" + filePath + "创建失败");
                e.printStackTrace();
            }
        }
    }

    /**
     * 将map写入到file文件中。默认map（String A,String A')file中以A=A'来表示，map中每个键值对显示一行
     * @throws IOException
     */
    public File mapToFileDefault(Map<String, String> map,File file,String separ) throws IOException{
        StringBuffer buffer = new StringBuffer();
        FileWriter writer = new FileWriter(file, false);
        for(Map.Entry entry:map.entrySet()){
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            buffer.append(key + "=" + value).append(line);
        }
        writer.write(buffer.toString());
        writer.close();
        return file;

    }
    /**
     * 在newMap替换oldMap时，是否覆盖（isOverwrite)如果是，就直接替换，如果否，则将oldMap中的key前加“#”，默认为否
     * @param newMap
     * @param oldMap
     * @return
     */
    public Map<String, String> newMapToOldMapDefault(Map<String, String> newMap,Map<String, String> oldMap){
        return newMapToOldMap(newMap,oldMap,false);
    }

    /**
     * 在newMap替换oldMap时，是否覆盖（isOverwrite)如果是，就直接替换，如果否，则将oldMap中的key前加“#”，默认为否
     */
    public Map<String, String> newMapToOldMap(Map<String, String> newMap,
                                               Map<String, String> oldMap, boolean isOverwrite) {
        // 由于oldMap中包含了file中更多内容，所以newMap中内容在oldMap中调整后，最后返回oldMap修改之后的map.
        // 如果选择true覆盖相同的key
        if (isOverwrite) {
            // 循环遍历newMap
            for (Map.Entry entry : newMap.entrySet()) {
                String newKey = (String) entry.getKey();
                String newValue = (String) entry.getValue();
                oldMap.put(newKey, newValue);
            }
        } else {
            // 不覆盖oldMap,需要在key相同的oldMap的key前加#；
            // 循环遍历newMap
            for (Map.Entry entry : newMap.entrySet()) {
                String newKey = (String) entry.getKey();
                String newValue = (String) entry.getValue();
                String oldValue = oldMap.get(newKey);
                oldMap.put("#" + newKey, oldValue);
                oldMap.put(newKey, newValue);
            }
        }
        return oldMap;
    }

    /**
     * 将文件转换成map存储
     *
     * @return
     */
    private Map<String, String> fTM() {
        Map<String, String> map = new HashMap<String, String>();
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                System.out.println("line " + line + ": " + tempString);
                if (!tempString.startsWith("#")) {
                    String[] strArray = tempString.split("=");
                    map.put(strArray[0], strArray[1]);
                }
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        for (Map.Entry entry : map.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
        return map;
    }
}
