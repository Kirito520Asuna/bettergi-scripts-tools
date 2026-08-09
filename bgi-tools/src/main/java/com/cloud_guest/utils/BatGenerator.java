package com.cloud_guest.utils;


/**
 * @Author yan
 * @Date 2026/8/9 12:16:07
 * @Description
 */
public class BatGenerator {

    /**
     * 生成1Remote自启动bat脚本
     *
     * @param startDir  软件所在目录，如 D:\1Remote-1.2.1-net9-x64\
     * @param startUlid ULID标识
     * @return
     */
    public static String generateStartBatContent(String startDir, String startUlid) {
        String title = "自启动本地远程";
        String exeName = "1Remote.exe";
        int seconds = 5;
        return generateStartBatContent(title, startDir, exeName, startUlid, seconds);
    }


    public static String generateStartBatContent(String title, String startDir, String exeName, String startUlid, int seconds) {
        String batContent = """
                @echo off
                chcp 65001 >nul
                title %s
                color 0c
                mode con cols=60 lines=20
                
                set START_DIR=%s
                set START_EXE=%s
                set START_ULID=%s
                
                set START_KEY=ULID:%%START_ULID%%
                set START_DIRECTIVE= --start-minimized
                
                echo.
                echo 第一步：启动主程序（无参数）...
                start "" /D "%%START_DIR%%" "%%START_EXE%%"
                
                echo 等待 %s 秒，确保主程序完全就绪...
                ping 127.0.0.1 -n %s >nul
                
                echo 第二步：发送连接指令...
                start "" /D "%%START_DIR%%" "%%START_EXE%%" %%START_KEY%% %%START_DIRECTIVE%%
                
                echo 软件已启动
                exit
                """.formatted(title, startDir, exeName, startUlid, seconds, (1 + seconds));
        return batContent;
    }
}
