package com.example.jennifer_adapter.controller;

import com.example.jennifer_adapter.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @PostMapping("/trigger")
    public String triggerException(@RequestParam String type, Model model) {
        try {
            eventService.trigger(type);
            model.addAttribute("result", "예외가 발생하지 않았습니다: " + type);
        } catch (Exception e) {
            model.addAttribute("result", "예외 발생 성공: " + e.getClass().getSimpleName() + " - " + e.getMessage());

            List<String> traceList = Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList());

            model.addAttribute("exception", e);
            model.addAttribute("stackTraceList", traceList);
        }
        return "result"; // 결과를 보여줄 페이지
    }
}