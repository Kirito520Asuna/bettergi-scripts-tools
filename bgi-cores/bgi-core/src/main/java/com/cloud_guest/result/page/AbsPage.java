package com.cloud_guest.result.page;

import com.cloud_guest.result.Result;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Author yan
 * @Date 2025/5/18 14:17:01
 * @Description
 */
public interface AbsPage {
    /**
     * 通用返回
     *
     * @param list
     * @param pageNumber
     * @param pageSize
     * @param total
     * @param <T>
     * @return
     */
    default <T> Result<ResultPage<T>> result(List<T> list, long pageNumber, long pageSize, long total) {
        long tempPages = total / pageSize;
        long pages = total % pageSize > 0 ? tempPages : tempPages + 1;
        ResultPage<T> resultPage = new ResultPage<>();
        resultPage.setList(list)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setPages(pages)
                .setTotal(total);
        return Result.ok(resultPage);
    }

    /**
     * 通用返回
     *
     * @param list
     * @param pageNumber
     * @param pageSize
     * @param total
     * @param <T>
     * @return
     */
    default <T> ResultPage<T> resultPage(List<T> list, long pageNumber, long pageSize, long total) {
        long tempPages = total / pageSize;
        long pages = total % pageSize > 0 ? tempPages : tempPages + 1;
        ResultPage<T> resultPage = new ResultPage<>();
        resultPage.setList(list)
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setPages(pages)
                .setTotal(total);
        return resultPage;
    }

    /**
     * pagehelper分页插件分页转换
     *
     * @param list
     * @param <T>
     * @return
     */
    default <T> ResultPage<T> listToPage(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        int pageNumber = pageInfo.getPageNum();
        int pageSize = pageInfo.getPageSize();
        int pages = pageInfo.getPages();
        long total = pageInfo.getTotal();
        List<T> pageInfoList = pageInfo.getList();
        ResultPage<T> resultPage = new ResultPage<>(pageInfoList, pageSize, pageNumber, pages, total);
        return resultPage;
    }


    //
//    /**
//     * mybatis-plus 分页转化为通用分页
//     * @param iPage
//     * @return
//     * @param <T>
//     */
//    public static <T> ResultPage<T> ipageToPage(IPage<T> iPage){
//        long pages = iPage.getPages();
//        List<T> list = iPage.getRecords();
//        long total = iPage.getTotal();
//        long pageSize = iPage.getSize();
//        long pageNumber = iPage.getCurrent();
//        ResultPage<T> resultPage = new ResultPage<>(list, pageSize, pageNumber, pages, total);
//        return resultPage;
//    }
    default <T> ResultPage<T> pageToVoPage(ResultPage page, List<T> list) {
        // 数据列表转换为通用分页
        ResultPage<T> result = new ResultPage<>();
        result.setList(list)
                .setPageSize(page.getPageSize())
                .setPageNumber(page.getPageNumber())
                .setPages(page.getPages())
                .setTotal(page.getTotal());
        // 返回结果
        return result;
    }

}
