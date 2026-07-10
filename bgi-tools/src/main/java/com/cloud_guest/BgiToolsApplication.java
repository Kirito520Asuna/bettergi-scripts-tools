package com.cloud_guest;

import com.cloud_guest.runner.CacheRunner;
import com.cloud_guest.utils.ApplicationContextHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
public class BgiToolsApplication {

	public static void main(String[] args) {
		//SpringApplication app = new SpringApplication(BgiToolsApplication.class);
		//// 将 Banner 输出到日志
		//app.setBannerMode(Banner.Mode.LOG);
		//app.run(args);
		ConfigurableApplicationContext context = SpringApplication.run(BgiToolsApplication.class, args);
		// 保存上下文和参数
		ApplicationContextHolder.setContext(context, args);
	}

	@PostConstruct
	public void init() {
		// 启动时加载
		CacheRunner.createCacheDir();
	}
	@PreDestroy
	public void destroy() {
		// 关闭时清理
	}
}
