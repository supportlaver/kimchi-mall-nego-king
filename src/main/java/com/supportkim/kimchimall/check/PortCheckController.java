package com.supportkim.kimchimall.check;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PortCheckController {
    private final Environment env;
    @GetMapping("/check")
    public String check() {
        return env.getProperty("server.port");
    }
}
