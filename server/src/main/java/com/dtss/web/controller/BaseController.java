package com.dtss.web.controller;

import com.dtss.commons.BussinessException;
import com.dtss.web.vo.Model;
import com.dtss.web.vo.ModelStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseController {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected static final String DEFAULT_SERVER_ERROR_MSG = "服务器出错了, 请联系客服！";

	@ResponseBody
	@ExceptionHandler(value = { Exception.class })
	public Model<String> exceptionHandle(HttpServletRequest request, Exception e) {

		Model<String> model = new Model<String>();
		if (e instanceof BussinessException) {
			logger.warn(e.getMessage(), e);
			model.setCode(ModelStatus.BUSINESS_ERROR);
			model.setErrorMsg(e.getMessage());
		} else {
			logger.error(e.getMessage(), e);
			model.setCode(ModelStatus.BUSINESS_ERROR);
			model.setErrorMsg(DEFAULT_SERVER_ERROR_MSG);
		}
		return model;
	}
}
