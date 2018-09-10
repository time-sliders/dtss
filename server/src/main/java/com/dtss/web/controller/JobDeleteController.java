package com.dtss.web.controller;

import com.dtss.server.service.JobDeleteService;
import com.dtss.web.consts.OptStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author luyun
 * @since 2018.02.03 17:48
 */
@Controller
@RequestMapping("/job")
public class JobDeleteController extends BaseController implements OptStatus {

    @Autowired
    private JobDeleteService jobDeleteService;

    @ResponseBody
    @RequestMapping("/delete")
    public Map<String, Object> delete(@RequestParam Long id) {
        Map<String, Object> mapResult = new HashMap<String, Object>();

        if (jobDeleteService.deleteJob(id)) {
            mapResult.put("status", STATUS_SUCCESS);
        } else {
            mapResult.put("status", STATUS_FAILURE);
            mapResult.put("errorMsg", "删除失败");
        }

        return mapResult;
    }

}
