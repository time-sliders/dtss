package com.dtss.server.core.job.model;

/**
 * @author LuYun
 * @since 2018.04.16
 */
public class JobStopResult {

    private boolean success;

    private String msg;

    public static JobStopResult buildFail(String errorMsg) {
        JobStopResult r = new JobStopResult();
        r.setSuccess(false);
        r.setMsg(errorMsg);
        return r;
    }

    public static JobStopResult buildSucc(String msg) {
        JobStopResult r = new JobStopResult();
        r.setSuccess(true);
        r.setMsg(msg);
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
