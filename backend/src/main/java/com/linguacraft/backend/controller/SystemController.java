package com.linguacraft.backend.controller;

import com.linguacraft.backend.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统冒烟接口:用于联调前置检查(后端服务是否可用、前端 vite 代理是否连通)。
 */
@Tag(name = "系统", description = "系统冒烟与连通性检查")
@RestController
@RequestMapping("/api/system")
public class SystemController {

    /**
     * 连通性检查。
     *
     * @return 统一响应,data 固定为 "pong"
     */
    @Operation(summary = "连通性检查", description = "返回 pong,用于验证后端服务与前端 vite 代理链路是否正常")
    @ApiResponse(responseCode = "200", description = "服务正常")
    @GetMapping("/ping")
    public R<String> ping() {
        return R.ok("pong");
    }
}