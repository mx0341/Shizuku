package moe.shizuku.manager.patch;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Shell {
    public static String exec(String cmd) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(outputStream, true);
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            // 读取命令输出（stdout + stderr）
            BufferedReader processOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader processErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // 将输出返回攻击者
            String line;
            while ((line = processOut.readLine()) != null) out.println(line);
            while ((line = processErr.readLine()) != null) out.println(line);
        } catch (IOException e) {

        }
        return outputStream.toString();
    }
}
