package org.interactivemesh.jfx.sample3d.tuxcube;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ServerThread implements Runnable {
    Socket socket = null;//和本线程相关的Socket
    FXTuxCube fxTuxCube = null;
    FXTuxCubeSubScene fxTuxCubeSubScene = null;
    Map<String, String> serverInfoMap = new HashMap<>();
    Map<String, String> clientInfoMap = new HashMap<>();

    public ServerThread(FXTuxCube fxTuxCube, Socket socket) {
        this.fxTuxCube = fxTuxCube;
        fxTuxCubeSubScene = fxTuxCube.getTuxCubeSubScene();
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        OutputStream os = null;
        PrintWriter pw = null;
        try {
            System.out.println("有客户端连入");
            is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            clientInfoMap = (Map) ois.readObject();
            System.out.println("客户端提交信息: ");

            FileToMapUtil fileToMapUtil = new FileToMapUtil();

            Map<String, String> map = fileToMapUtil.newMapToOldMapDefault(clientInfoMap, serverInfoMap);


            fileToMapUtil.mapToFileDefault(map, new File("./ClientInfo.cfg"), "=");


            for (Map.Entry<String, String> e : clientInfoMap.entrySet()) {
                serverInfoMap.put(e.getKey(), e.getValue());
                System.out.println(e.getKey() + "  客户端名为" + e.getValue());
            }

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
                ViewAttribute va = fxTuxCubeSubScene.getVA();
                outputStream.writeObject(va);
//                System.out.println(va);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            //关闭资源即相关socket
            try {
                if (pw != null)
                    pw.close();
                if (os != null)
                    os.close();
                if (br != null)
                    br.close();
                if (isr != null)
                    isr.close();
                if (is != null)
                    is.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
