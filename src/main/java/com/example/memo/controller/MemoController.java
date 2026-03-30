package com.example.memo.controller;

import com.example.memo.service.MemoService;
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

    public MemoController(MemoService memoService) {
        this.memoService = memoService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("zone", ZoneId.systemDefault());
        model.addAttribute("memos", memoService.findAllMemos());
        model.addAttribute("logs", memoService.findAllLogs());
        return "index";
    }

    @PostMapping("/memos")
    public String create(@RequestParam String title,
                         @RequestParam(required = false, defaultValue = "") String content,
                         @RequestParam(required = false) String memoCity,
                         @RequestParam(required = false) String memoWeatherCondition,
                         @RequestParam(required = false) String memoTempC) {
        if (title == null || title.isBlank()) {
            return "redirect:/";
        }
        memoService.createMemo(
                title.trim(),
                content,
                blankToNull(memoCity),
                blankToNull(memoWeatherCondition),
                parseDoubleNullable(memoTempC)
        );
        return "redirect:/";
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private static Double parseDoubleNullable(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
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
