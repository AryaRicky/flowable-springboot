package com.ljg.service.delegate;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DataDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("todo去处理数据");
    }
}
