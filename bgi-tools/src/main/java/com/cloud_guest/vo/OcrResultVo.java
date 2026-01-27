package com.cloud_guest.vo;


import cn.hutool.core.collection.CollUtil;
import com.benjaminwan.ocrlibrary.OcrResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2025/9/22 14:41:15
 * @Description
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultVo {
    private double dbNetTime;
    private ArrayList<TextBlockVo> textBlocks=new ArrayList<>();
    private double detectTime;
    private String strRes;
    private List<String> resList=new ArrayList<>();

    public OcrResultVo(OcrResult ocrResult) {
        this.dbNetTime = ocrResult.getDbNetTime();
        this.detectTime = ocrResult.getDetectTime();
        this.strRes = ocrResult.getStrRes();

        ArrayList<TextBlockVo> textBlocksList = CollUtil.newArrayList();
        ocrResult.getTextBlocks()
                .stream()
                .map(TextBlockVo::new)
                .forEach(textBlockVo -> textBlocks.add(textBlockVo));

        this.textBlocks = textBlocksList;

        this.resList = Arrays.stream(ocrResult.getStrRes().split("\n"))
                .collect(Collectors.toList());
    }
}
