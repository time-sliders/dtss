package com.dtss.web.controller;

import com.dtss.client.model.JobConfig;
import com.dtss.commons.DateUtil;
import com.dtss.server.service.JobAddService;
import com.dtss.server.service.JobQueryService;
import com.dtss.web.consts.OptStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 新增JOB
 *
 * @author luyun
 * @since 2018.02.03 11:53
 */
@Controller
@RequestMapping("/job")
public class JobAddController extends BaseController implements OptStatus {

    @Autowired
    private JobAddService jobAddService;

    @Autowired
    private JobQueryService jobQueryService;

    @RequestMapping("/toadd")
    public String toAdd() {
        return "jobInfo/add";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addJobInfo(@ModelAttribute("data") JobConfig jobConfig, ModelMap model) {
        if (jobConfig.isActivity() == null) {
            jobConfig.setActivity(false);
        }

        if (jobAddService.addJob(jobConfig)) {
            JobConfig newData = jobQueryService.findById(jobConfig.getId());
            model.addAttribute("data", newData);
            model.addAttribute("status", STATUS_SUCCESS);
            model.addAttribute("successMsg", "保存成功:"
                    + DateUtil.format(newData.getCreateTime(), DateUtil.DEFAULT_FORMAT));
        } else {
            model.addAttribute("status", STATUS_FAILURE);
            model.addAttribute("errorMsg", "操作失败");
        }
        return "jobInfo/edit";
    }


}
