package com.example.memo.controller;

import com.example.memo.service.MemoService;
import com.example.memo.service.WeatherApiService;
import com.example.memo.support.ClientIpResolver;
import com.example.memo.support.WeatherDisplayHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import java.time.ZoneId;

@Controller
public class MemoController {

    private final MemoService memoService;
    private final WeatherApiService weatherApiService;

    public MemoController(MemoService memoService, WeatherApiService weatherApiService) {
        this.memoService = memoService;
        this.weatherApiService = weatherApiService;
    }

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        model.addAttribute("zone", ZoneId.systemDefault());
        model.addAttribute("memos", memoService.findAllMemos());
        model.addAttribute("logs", memoService.findAllLogs());

        WeatherApiService.WeatherSnapshot preview = WeatherApiService.WeatherSnapshot.empty();
        try {
            preview = weatherApiService.fetchRealtimeForClientIp(ClientIpResolver.resolve(request));
        } catch (Exception ignored) {
        }
        boolean previewHasData = preview.hasAny()
                || preview.weatherCondition() != null
                || preview.tempC() != null;
        model.addAttribute("weatherPreviewHasData", previewHasData);
        model.addAttribute("weatherPreviewCity", preview.hasAny() ? preview.location() : null);
        model.addAttribute("weatherPreviewCondition", preview.weatherCondition());
        model.addAttribute("weatherPreviewTempC", preview.tempC());
        model.addAttribute("weatherPreviewEmoji",
                WeatherDisplayHelper.emojiForConditionStatic(preview.weatherCondition()));

        return "index";
    }

    @PostMapping("/memos")
    public String create(@RequestParam String title,
                         @RequestParam(required = false, defaultValue = "") String content,
                         HttpServletRequest request) {
        if (title == null || title.isBlank()) {
            return "redirect:/";
        }
        memoService.createMemo(title.trim(), content, ClientIpResolver.resolve(request));
        return "redirect:/";
    }

    @PostMapping("/memos/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            memoService.deleteMemo(id);
        } catch (IllegalArgumentException ignored) {
        }
        return "redirect:/";
    }
}
