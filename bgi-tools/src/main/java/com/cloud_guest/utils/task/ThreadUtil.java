package com.cloud_guest.utils.task;

import com.cloud_guest.task.domain.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/8/26 17:47:51
 * @Description
 */
@Slf4j
public class ThreadUtil {

    /**
     * 并发执行任务列表，并等待所有任务完成（阻塞当前线程）。
     *
     * @param tasks       任务列表
     * @param executor    线程池，可为 null 使用默认 ForkJoinPool
     * @return 每个任务是否成功的布尔值列表，与 tasks 顺序一致
     */
    public static List<Boolean> executeAllAndWait(List<Task> tasks,
                                                  ThreadPoolTaskExecutor executor) {
        if (tasks == null || tasks.isEmpty()) {
            log.warn("任务列表为空，无需执行");
            return Collections.emptyList();
        }

        List<CompletableFuture<Boolean>> futures = tasks.stream()
                .map(task -> executeTaskAsync(task, executor))
                .collect(Collectors.toList());

        // 等待所有任务完成（阻塞）
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 所有任务已完成，安全地获取结果（不会阻塞）
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * 并发执行任务列表，并等待所有任务完成（阻塞方式）。
     *
     * @param tasks       任务列表
     * @param executor    线程池，可为 null 使用默认 ForkJoinPool
     * @param taskHandler 任务处理函数，返回 Boolean 表示成功/失败
     * @param timeout     超时时间，单位秒，<=0 表示不限制超时
     * @return 每个任务是否成功的列表，与 tasks 顺序一致
     * @demo
     * 1. 阻塞等待所有任务完成，并获取每个任务是否成功
     * List<Object> taskList = List.of("任务1", "任务2", "任务3");
     * ThreadPoolTaskExecutor executor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
     *
     * List<Boolean> results = ThreadUtil.executeAllAndWait(
     *         taskList,
     *         executor,
     *         task -> {
     *             // 实际业务处理
     *             log.debug("处理任务: {}", task);
     *             return true; // 模拟成功
     *         },
     *         30 // 超时30秒
     * );
     *
     * for (int i = 0; i < results.size(); i++) {
     *     if (!results.get(i)) {
     *         log.warn("任务 {} 失败", taskList.get(i));
     *     }
     * }
     * 2. 非阻塞执行，完成后自动回调
     * ThreadUtil.executeAllAsync(
     *         taskList,
     *         executor,
     *         task -> {
     *             // 无返回值任务
     *             doSomething(task);
     *         },
     *         () -> log.info("所有任务已完成")
     * );
     */
    public static List<Boolean> executeAllAndWait(
            List<Object> tasks,
            ThreadPoolTaskExecutor executor,
            Function<Object, Boolean> taskHandler,
            long timeout) {

        if (tasks == null || tasks.isEmpty()) {
            log.warn("任务列表为空，无需执行");
            return java.util.Collections.emptyList();
        }

        List<CompletableFuture<Boolean>> futures = tasks.stream()
                .map(task -> executeTask(task, executor, taskHandler))
                .collect(Collectors.toList());

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        try {
            if (timeout > 0) {
                allFuture.get(timeout, TimeUnit.SECONDS);
            } else {
                allFuture.join();
            }
        } catch (Exception e) {
            log.error("等待异步任务完成时发生异常", e);
            // 可以选择取消未完成的任务
            futures.forEach(f -> f.cancel(true));
        }

        // 此时已完成（或已取消/超时），安全地获取结果
        return futures.stream()
                .map(f -> {
                    try {
                        return f.getNow(false); // 已取消或未完成返回默认值 false
                    } catch (Exception e) {
                        log.error("获取任务结果异常", e);
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 执行单个任务，并处理异常。
     */
    private static CompletableFuture<Boolean> executeTask(
            Object task,
            ThreadPoolTaskExecutor executor,
            Function<Object, Boolean> taskHandler) {

        CompletableFuture<Boolean> future;
        if (executor != null) {
            future = CompletableFuture.supplyAsync(() -> taskHandler.apply(task), executor);
        } else {
            future = CompletableFuture.supplyAsync(() -> taskHandler.apply(task));
        }

        // 异常处理：记录日志并返回 false，避免整个 allOf 因异常立即失败
        return future.exceptionally(ex -> {
            log.error("任务执行异常，任务: {}", task, ex);
            return false;
        });
    }

    /**
     * 执行单个任务，并处理异常（异常时返回 false）。
     */
    private static CompletableFuture<Boolean> executeTaskAsync(Task task,
                                                               ThreadPoolTaskExecutor executor) {
        CompletableFuture<Boolean> future;
        if (executor != null) {
            future = CompletableFuture.supplyAsync(() -> {
                task.execute();
                return true;
            }, executor);
        } else {
            future = CompletableFuture.supplyAsync(() -> {
                task.execute();
                return true;
            });
        }

        // 异常时返回 false，并记录日志
        return future.exceptionally(ex -> {
            log.error("任务执行异常，任务: {}", task, ex);
            return false;
        });
    }

    /**
     * 并发执行无返回值的任务（例如清理、通知等），非阻塞方式。
     */
    public static void executeAllAsync(
            List<Object> tasks,
            ThreadPoolTaskExecutor executor,
            Consumer<Object> taskConsumer,
            Runnable onAllDone) {

        if (tasks == null || tasks.isEmpty()) {
            if (onAllDone != null) {
                onAllDone.run();
            }
            return;
        }

        List<CompletableFuture<Void>> futures = tasks.stream()
                .map(task -> {
                    CompletableFuture<Void> future;
                    if (executor != null) {
                        future = CompletableFuture.runAsync(() -> taskConsumer.accept(task), executor);
                    } else {
                        future = CompletableFuture.runAsync(() -> taskConsumer.accept(task));
                    }
                    return future.exceptionally(ex -> {
                        log.error("任务执行异常，任务: {}", task, ex);
                        return null;
                    });
                })
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    if (onAllDone != null) {
                        onAllDone.run();
                    }
                })
                .exceptionally(ex -> {
                    log.error("异步回调异常", ex);
                    return null;
                });
    }


}
