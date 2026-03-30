package com.example.memo.controller;

import com.example.memo.service.MemoService;
import com.example.memo.support.ClientIpResolver;
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
