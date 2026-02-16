import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.domain.dto.CronDto;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.DateUtils;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.*;
import java.util.Optional;

/**
 * @Author yan
 * @Date 2026/1/12 18:51:28
 * @Description
 */
public class Main {
    /**
     * 查找 [startTime, endTime] 区间内，离 startTime 最近的一次 cron 执行时间
     *
     * @param cronExpression cron 表达式（Quartz 风格）
     * @param startTimeMs    开始时间戳（毫秒，Unix timestamp）
     * @param endTimeMs      结束时间戳（毫秒，Unix timestamp）
     * @return 最近的执行时间戳（毫秒），如果区间内没有则返回 empty
     */
    public static Long findNearestExecutionAfter(
            String cronExpression,
            long startTimeMs,
            long endTimeMs) {

        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

        try {
            Cron cron = parser.parse(cronExpression).validate();
            ExecutionTime executionTime = ExecutionTime.forCron(cron);

            // long → Instant → ZonedDateTime
            ZonedDateTime from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTimeMs), ZoneId.systemDefault());
            ZonedDateTime to = ZonedDateTime.ofInstant(Instant.ofEpochMilli(endTimeMs), ZoneId.systemDefault());

            // 寻找下一个执行时间
            Optional<ZonedDateTime> next = executionTime.nextExecution(from);

            // 如果存在且在 endTime 之前/等于，返回对应的毫秒时间戳
            return next.filter(candidate -> !candidate.isAfter(to))
                    .map(ZonedDateTime::toInstant)
                    .map(Instant::toEpochMilli).orElseGet(null);

        } catch (Exception e) {
            // cron 非法 或其他异常
            return (Long) Optional.empty().orElseGet(() -> null);
        }
    }

    public static void main(String[] args) {
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 12, 14, 51, 28);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 12, 18, 51, 28);
        long start = DateUtils.LocalDateTimeTolong(startTime);
        long end = DateUtils.LocalDateTimeTolong(endTime);
        String cronExpression = "0 0 15 * * ? ";
        //Long nearestExecutionAfter = findNearestExecutionAfter(cronExpression, start, end);
        //LocalDateTime localDateTime = DateUtils.longToLocalDateTime(nearestExecutionAfter);
        //System.out.println(localDateTime);
        CronDto cronDto = new CronDto();
        cronDto.setCronExpression(cronExpression);
        cronDto.setStartTimestamp(start);
        cronDto.setEndTimestamp(end);
        String p = "http://";
        p+="localhost:8081";
        //p=+"192.168.3.170:8081";
        String post = HttpUtil.post(p + "/bgi/cron/next-timestamp", JSONUtil.toJsonStr(cronDto));
        System.err.println(post);
        System.err.println(JSONUtil.toBean(post, Result.class));
    }
}
