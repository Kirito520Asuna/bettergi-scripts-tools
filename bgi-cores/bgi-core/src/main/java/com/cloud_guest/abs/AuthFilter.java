package com.cloud_guest.abs;


import com.cloud_guest.abs.order.FilterOrderConstants;
import jakarta.servlet.Filter;
import org.springframework.core.Ordered;


/**
 * @Author yan
 * @Date 2026/2/10 12:54:47
 * @Description
 */
public interface AuthFilter extends Ordered, Filter, AbsAuth {
    @Override
    default int getOrder() {
        return FilterOrderConstants.AuthOrder;
    }
}
