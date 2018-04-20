package com.dtss.client.core.handle;

import com.dtss.client.cmp.AbstractAdapter;
import com.dtss.client.enums.OptType;
import org.springframework.stereotype.Component;

/**
 * @author LuYun
 * @since 2018.04.16
 */
@Component
public class CmdDispatcher extends AbstractAdapter<Integer, JobHandler> {

    @Override
    public void afterPropertiesSet() throws Exception {
        registerExecutor(OptType.EXE.getCode(),"jobExeHandler");
        registerExecutor(OptType.TEST.getCode(),"jobTestHandler");
        registerExecutor(OptType.STOP.getCode(),"jobStopHandler");
        super.afterPropertiesSet();
    }
}
