package qr.program.sse.controller;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author qr
 * @Date 2022/5/13-19:48
 */
@Controller
@RestController
@RequestMapping("/sse")
@Slf4j
public class SseController {

    private final static Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();

    @GetMapping("subscript")
    public SseEmitter subscript(@RequestParam String id) {
        SseEmitter sseEmitter = new SseEmitter(3600_000L);
        sseCache.put(id, sseEmitter);
        sseEmitter.onTimeout(()-> sseCache.remove(id));
        sseEmitter.onCompletion(()-> System.out.println("连接完成!"));
        log.info("id：{} 订阅", id);
        return sseEmitter;
    }

    @GetMapping("push")
    public String push(@RequestParam String id) throws IOException {
        SseEmitter sseEmitter = sseCache.get(id);
        if (sseEmitter != null) {
            String strs = RandomUtil.randomString(10);
            sseEmitter.send(strs);
            log.info("id：{} 推送：{}", id, strs);
        }
        return "over";
    }

    @GetMapping("unsubscribe")
    public String unsubscribe(@RequestParam String id) {
        SseEmitter sseEmitter = sseCache.get(id);
        if (sseEmitter != null) {
            sseEmitter.complete();
            sseCache.remove(id);
        }
        log.info("id：{}取消订阅", id);
        return "over";
    }
}
