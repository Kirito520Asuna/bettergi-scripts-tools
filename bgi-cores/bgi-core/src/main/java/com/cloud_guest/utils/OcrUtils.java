package com.cloud_guest.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.benjaminwan.ocrlibrary.OcrResult;
import com.cloud_guest.vo.OcrResultVo;
import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @Author yan
 * @Date 2024/8/13 0013 10:39:14
 * @Description
 */
@Slf4j
public class OcrUtils {
    public static String OcrFileTempPath = "temp";
    public static Model model = Model.ONNX_PPOCR_V4;

    @SneakyThrows
    public static void main(String[] args) {
        //test();
        //if (true) return;

        if (args.length < 1) {
            System.err.println("{\"error\": \"Usage: java -jar ocr-utils.jar <image-path-or-url>\"}");
            System.exit(1);
        }

        String imagePath = args[0];

        if (args.length > 1) {
            OcrFileTempPath = args[1];
        }

        if (args.length > 2) {
            model = Model.valueOf(args[2]);
        }


        InputStream input = null;
        String prx = imagePath.substring(imagePath.lastIndexOf("."), imagePath.length());

        try {
            input = new URL(imagePath).openStream();
        } catch (Exception e) {
            log.error("Failed to open URL: {}, trying as local file", imagePath, e);
            try {
                input = FileUtil.getInputStream(new File(imagePath));
            } catch (Exception ex) {
                System.err.println("{\"error\": \"Invalid image path or URL: " + imagePath + "\"}");
                System.exit(1);
            }
        }

        OcrResultVo ocrResult = ocr(input, prx, OcrFileTempPath, model);
        if (ObjectUtil.isNotEmpty(ocrResult)) {
            System.out.println(JSONUtil.toJsonStr(ocrResult.getResList()));
        }
        //System.out.println(JSONUtil.toJsonStr(ocrResult));
    }

    /**
     * 通用ocr
     *
     * @param input
     * @param suffix          文件后缀
     * @param ocrFileTempPath 文件临时路径
     * @param model
     * @return
     */
    public static OcrResultVo ocr(InputStream input, String suffix, String ocrFileTempPath, Model model) {
        InferenceEngine engine = InferenceEngine.getInstance(ObjectUtil.isNotEmpty(model) ? model : Model.ONNX_PPOCR_V3);
        String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
        String pathname = ocrFileTempPath + "/" + format + "/" + UUID.randomUUID() + suffix;
        log.info("ocr file init pathname:{}", pathname);
        File file = new File(pathname);
        try {
            OutputStream output = FileUtil.getOutputStream(file);
            IoUtil.copy(input, output);
            String path = file.getPath();
            OcrResult ocrResult = engine.runOcr(path);
            log.info("ocr result:{}", JSONUtil.toJsonStr(ocrResult));
            return new OcrResultVo(ocrResult);
        } finally {
            FileUtil.del(file);
            log.info("ocr file del file pathname:{}", pathname);
        }
    }

    public static void test() {
        boolean isInput = false;
        InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V4);
        String imagePath = "C:\\Users\\Administrator\\Desktop\\Snipaste_2024-08-12_18-12-35.png";
        imagePath = "https://img.zcool.cn/community/0177c35548e8fb0000019ae93040ce.jpg@2o.jpg";
        imagePath = "D:\\Administrator\\Pictures\\+20.jpg";
        InputStream input = null;
        String prx = imagePath.substring(imagePath.lastIndexOf("."), imagePath.length());
        try {
            input = new URL(imagePath).openStream();
            isInput = true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                input = FileUtil.getInputStream(new File(imagePath));
            } catch (Exception ex) {

            }
        }
        //ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (true) {
            OcrResultVo ocrResult = ocr(input, prx, "temp", null);
            System.err.println(JSONUtil.toJsonStr(ocrResult) + "\n---");
            System.err.println(ocrResult.getStrRes().trim() + "\n---");
            return;
        }

/*        String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
        String pathname = "temp/" + format + "/" + UUID.randomUUID() + prx;
        File file = new File(pathname);
        try {
            OutputStream output = FileUtil.getOutputStream(file);
            if (isInput) {
                IoUtil.copy(input, output);
            }
            String path = file.getPath();
            OcrResult ocrResult = engine.runOcr(path);
            System.err.println(JSONUtil.toJsonStr(ocrResult));
            System.err.println(ocrResult.getStrRes().trim());
        } finally {
            FileUtil.del(file);
        }*/
    }
}
