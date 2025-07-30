package com.example.jennifer_adapter.controller;

import com.example.jennifer_adapter.service.EventService;
import com.example.jennifer_adapter.service.EventType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping("/index")
    public String index(Model model) {
        // 가능한 이벤트 타입 키 목록을 뷰에 전달
        List<String> types = Arrays.stream(EventType.values())
                .map(EventType::getKey)
                .collect(Collectors.toList());
        model.addAttribute("types", types);
        return "index";
    }

    @PostMapping("/trigger")
    public String triggerException(@RequestParam String type, Model model) throws Throwable {
        try{
            EventType eventType = EventType.fromKey(type);
            eventService.trigger(type);
            System.out.println("예외가 발생하지 않음" + type);
            //log.warn("🚨예외가 발생하지 않음", type);
            model.addAttribute("result", "예외 테스트가 완료되었지만 예외가 발생하지 않았습니다. 테스트 로직을 확인해주세요.");

        }catch (Throwable e){
            System.out.println("Exception start" + type + "-" + e.getMessage());

            //예외 정보를 모델에 추가
            model.addAttribute("exception", e);
            model.addAttribute("result", "예외가 발생했습니다.");

            //스택 트레이스 문자열 변환
            List<String> stackTraceList = getStackThaceList(e);
            model.addAttribute("stackTraceList", stackTraceList);
            model.addAttribute("exceptionType", e.getClass().getSimpleName());
            model.addAttribute("exceptionMessage", e.getMessage() != null ? e.getMessage() : "메시지 없음");
            model.addAttribute("exceptionCause", e.getCause() != null ? e.getCause().toString() : null);
        }


        return "result";
    }

    /*예외의 스택 트레이스를 문자열 리스트로 변환      */
    private List<String> getStackThaceList(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace();
        String stackTrace = sw.toString();
        return Arrays.stream(stackTrace.split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).limit( 50).collect(Collectors.toList());
    }
/*
    *//*index 리다이렉트   *//*
    @GetMapping("/index")
    public String home() {
        return
                "redirect:/index";
    }*/
}


