package com.dtss.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

	@RequestMapping("/")
	public String index(){
		return "/layout/dashboard";
	}

	@RequestMapping("/version")
	public String listVersion(){
		return "commons/version";
	}
	
}
