package com.web3.scanner;

// 事件分发器
public class EventDispatcher {
    public static void dispatch(ChainEvent event) {
        switch (event.getEventType()) {
            case "DEPOSIT" -> DepositHandler.handle(event);
            case "WITHDRAW" -> WithdrawHandler.handle(event);
            // ...
        }
    }
}