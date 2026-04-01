package com.example.memo.controller;

import com.example.memo.service.MemoService;
import com.example.memo.support.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;

@Controller
public class MemoController {

    private static final Logger log = LoggerFactory.getLogger(MemoController.class);

    private final MemoService memoService;

    public MemoController(MemoService memoService) {
        this.memoService = memoService;
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam(name = "q", required = false) String q) {
        String raw = q != null ? q : "";
        String memoQuery = raw.strip();
        boolean memoSearchActive = !memoQuery.isEmpty();
        model.addAttribute("memoQuery", memoQuery);
        model.addAttribute("memoSearchActive", memoSearchActive);
        model.addAttribute("zone", ZoneId.systemDefault());
        model.addAttribute("memos", memoService.findMemosForList(raw));
        model.addAttribute("logs", memoService.findAllLogs());
        return "index";
    }

    @PostMapping("/memos")
    public String create(@RequestParam String title,
                         @RequestParam(required = false, defaultValue = "") String content,
                         @RequestParam(name = "includeWeather", defaultValue = "false") boolean includeWeather,
                         @RequestParam(name = "returnQ", required = false) String returnQ,
                         HttpServletRequest request) {
        if (title == null || title.isBlank()) {
            return redirectToIndex(returnQ);
        }
        String clientIp = ClientIpResolver.resolve(request);
        log.info("메모 저장 요청 - includeWeather={}, clientIp={}", includeWeather, clientIp);
        memoService.createMemo(title.trim(), content, includeWeather, clientIp);
        return redirectToIndex(returnQ);
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    @PostMapping("/memos/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(name = "returnQ", required = false) String returnQ) {
        try {
            memoService.deleteMemo(id);
        } catch (IllegalArgumentException ignored) {
        }
        return redirectToIndex(returnQ);
    }

    private static String redirectToIndex(String q) {
        if (q == null || q.isBlank()) {
            return "redirect:/";
        }
        return "redirect:/?q=" + UriUtils.encodeQueryParam(q.trim(), StandardCharsets.UTF_8);
    }
}
