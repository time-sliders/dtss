package com.dtss.commons;


import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * 推荐只在远程调用的接口层使用, 即对外提供服务的最外层.<br>

 * @param <V> data 对象的泛型
 */
@XmlRootElement
public class Result<V> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success = false;

    /**
     * 结果数据，如果正常的处理了请求，返回相应的逻辑结果。
     */
    private V data;

    /**
     * 错误信息，建议简单易懂，可直接给使用者或用户直接展示，与 errorCode 配合使用
     */
    private String errorMsg;
    /**
     * 错误信息代码， 与 errorMsg 配合使用
     */
    private String errorCode;
    /**
     * 异常信息，一般是 e.getMessage();
     */
    private String exceptionMsg;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public V getData() {
        return data;
    }

    public void setData(V data) {
        this.data = data;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public void setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
    }

    public static <T> Result<T> buildFail(String errorCode) {
        return buildFail(errorCode, null);
    }

    public static <T> Result<T> buildFail(String errorCode, String errorMsg) {
        return buildFail(errorCode, errorMsg, null);
    }

    public static <T> Result<T> buildFail(String errorCode, String errorMsg, String exceptionMsg) {
        Result<T> result = new Result<T>();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMsg(errorMsg);
        result.setExceptionMsg(exceptionMsg);
        return result;
    }

    public static <T> Result<T> buildSucc(T data) {
        Result<T> result = new Result<T>();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

}
