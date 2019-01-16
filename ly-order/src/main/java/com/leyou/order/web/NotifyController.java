package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    @PostMapping(value = "wxpay", produces = "application/xml")
    public Map<String,String> payNotify(@RequestBody Map<String,String> msg){
        // 处理回调结果
        orderService.handleNotify(msg);
        Map<String,String> result = new HashMap<>();
        result.put("return_code", "SUCCESS");
        result.put("return_msg", "OK");
        return result;
    }
}
