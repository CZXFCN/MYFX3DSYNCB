package org.interactivemesh.jfx.sample3d.tuxcube;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientThread implements Runnable {

    String name = null;
    FXTuxCube fxTuxCube = null;
    FXTuxCubeSubScene fxTuxCubeSubScene = null;
    Socket socket = null;
    InetAddress inetAddress = null;
    private static Map<String, String > infoMap = new HashMap<>();
    public ClientThread(FXTuxCube fxTuxCube, Socket socket, String name) {
        this.fxTuxCube = fxTuxCube;
        this.socket = socket;
        this.fxTuxCubeSubScene = fxTuxCube.getTuxCubeSubScene();
        this.name = name;
    }

    @Override
    public void run() {
        try {
            System.out.println("连接上服务器了!");
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            inetAddress = InetAddress.getLocalHost();
            StringBuilder sb = new StringBuilder(inetAddress.getHostName());
            sb.append(":").append(inetAddress.getHostAddress());
            infoMap.put(sb.toString(), name);
            os.writeObject(infoMap);
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            ObjectInputStream ois = new ObjectInputStream(is);
            while(true) {

                ViewAttribute va = (ViewAttribute)ois.readObject();
//                System.out.println(va);
                fxTuxCubeSubScene.applyViewAttribute(va);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
