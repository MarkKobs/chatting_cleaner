package com.techf5ve.cleaner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by FredZeng on 2017/11/5.
 */

public class Util {
    public static void startPcmToWav(String src, String target) {
        File file = new File(src);
        if (file != null) {
            try {
                @SuppressWarnings("resource")
                FileInputStream inputStream = new FileInputStream(file);
                // 计算长度
                byte[] buf = new byte[1024 * 100];
                int size = inputStream.read(buf);
                int pcmSize = 0;
                while (size != -1) {
                    pcmSize += size;
                    size = inputStream.read(buf);
                }
                inputStream.close();
                // 填入参数，比特率等。16位双声道，8000HZ
                WaveHeader header = new WaveHeader();
                // 长度字段 = 内容的大小（PCMSize) + 头部字段的大小
                // (不包括前面4字节的标识符RIFF以及fileLength本身的4字节
                header.fileLength = pcmSize + (44 - 8);
                header.FmtHdrLeth = 16;
                header.BitsPerSample = 16;
                header.Channels = 2;
                header.FormatTag = 0x0001;
                header.SamplesPerSec = 8000;
                header.BlockAlign = (short) (header.Channels
                        * header.BitsPerSample / 8);
                header.AvgBytesPerSec = header.BlockAlign
                        * header.SamplesPerSec;
                header.DataHdrLeth = pcmSize;

                byte[] h = header.getHeader();
                assert h.length == 44; // WAV标准，头部应该是44字节

                File targetFile = new File(target);
                if (!targetFile.exists()) {
                    targetFile.createNewFile();
                }
                FileOutputStream outputStream = new FileOutputStream(targetFile);
                inputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024 * 100];
                int tardetSize = inputStream.read(buffer);
                outputStream.write(h, 0, h.length);
                while (tardetSize != -1) {
                    outputStream.write(buffer, 0, tardetSize);
                    tardetSize = inputStream.read(buffer);
                }
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
