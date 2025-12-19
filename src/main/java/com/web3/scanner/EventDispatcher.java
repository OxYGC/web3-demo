package com.web3.scanner;

import lombok.extern.slf4j.Slf4j;

// 事件分发器（演示版：仅打印日志）
@Slf4j
public class EventDispatcher {
    public static void dispatch(ChainEvent event) {
        // 在真实系统中，这里会根据事件类型交由不同的业务处理器
        log.info("[Scanner] Dispatch event | chainId={} type={} token={} to={} amount={} tx={}",
                event.getChainId(), event.getEventType(), event.getToken(),
                event.getToAddress(), event.getRawAmount(), event.getTxHash());
    }
}