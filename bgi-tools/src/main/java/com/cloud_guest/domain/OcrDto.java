package com.cloud_guest.domain;

import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @Author yan
 * @Date 2026/1/27 16:23:22
 * @Description
 */
@Data @Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OCR请求参数")
public class OcrDto {
    @JsonView(BasicJsonView.OcrBytesView.class)
    @NotNull(message = "bytes不能为空",groups = {BasicJsonView.OcrBytesView.class})
    @Schema(description = "OCR图片字节数组")
    private byte[] bytes;
}
