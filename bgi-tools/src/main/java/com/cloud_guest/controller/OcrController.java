package com.cloud_guest.controller;

import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.domain.OcrDto;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.OcrUtils;
import com.cloud_guest.view.BasicJsonView;
import com.cloud_guest.vo.OcrResultVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

import static com.cloud_guest.result.Result.ok;

/**
 * @Author yan
 * @Date 2026/1/27 16:17:20
 * @Description
 */
@Tag(name = "Ocr")
@RestController
@RequestMapping({"/ocr/", "/api/ocr/", "/jwt/ocr/"})
public class OcrController {
    @PostMapping("bytes")
    @SysLog
    @Operation(summary = "ocr图片字节组(只支持jpg)")
    public Result<String> ocrBytes(@Validated(BasicJsonView.OcrBytesView.class) @RequestBody OcrDto dto) {
        OcrResultVo ocrResultVo = OcrUtils.ocr(new ByteArrayInputStream(dto.getBytes()), ".jpg", OcrUtils.OcrFileTempPath, null);
        return ok(ocrResultVo.getStrRes());
    }
    @SneakyThrows
    @PostMapping("file")
    @SysLog
    @Operation(summary = "ocr图片文件(只支持jpg)")
    public Result<String> ocrFile(@RequestPart MultipartFile file) {
        OcrResultVo ocrResultVo = OcrUtils.ocr(file.getInputStream(), ".jpg", OcrUtils.OcrFileTempPath, null);
        return ok(ocrResultVo.getStrRes());
    }
}
