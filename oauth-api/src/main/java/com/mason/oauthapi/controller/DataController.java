package com.mason.oauthapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequestMapping("/data")
public class DataController {

    @RequestMapping("/info")
    public String dataInfo(){
        return "I am mason";
    }

}
