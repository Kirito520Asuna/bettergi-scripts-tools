package com.cloud_guest.domain.dto;

import com.cloud_guest.aop.validator.NotEmptyList;
import com.cloud_guest.view.BasicJsonView;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;

/**
 * @Author yan
 * @Date 2026/1/30 9:11:35
 * @Description
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CronListDto {
    @JsonView(BasicJsonView.NextCronTimestampALLView.class)
    @NotEmptyList(message = "cronList不能为空", groups = {BasicJsonView.NextCronTimestampALLView.class})
    @Schema(description = "cronList", required = true)
    private ArrayList<CronDto> cronList = new ArrayList<>();
}
