package com.web3.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
@Slf4j
public class PageController {

    @GetMapping("/")
    public String index(Model model) {
        log.info("访问主页");
        model.addAttribute("pageTitle", "钱包管理系统");
        return "index";
    }

    @GetMapping("/wallet")
    public String wallet(Model model) {
        log.info("访问钱包页面");
        model.addAttribute("pageTitle", "钱包管理");
        return "wallet";
    }

    @GetMapping("/vanity")
    public String vanity(Model model) {
        log.info("访问靓号生成页面");
        model.addAttribute("pageTitle", "靓号地址生成");
        return "vanity";
    }

    @GetMapping("/test")
    public String test() {
        log.info("测试页面");
        return "test";
    }
}
