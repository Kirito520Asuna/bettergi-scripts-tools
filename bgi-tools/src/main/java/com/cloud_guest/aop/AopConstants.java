package com.cloud_guest.aop;

public interface AopConstants {
    int BaseOrder = 100;
    int EnvOrder = BaseOrder + 50;
    int AsyncOrder = BaseOrder + 60;
    int LoginOrder = BaseOrder + 70;
    int TokenOrder = BaseOrder + 75;
    int AviatorOrder = BaseOrder + 80;
    int SysLogOrder = BaseOrder + 100;
    int OperateLogOrder = SysLogOrder + 1;
}
