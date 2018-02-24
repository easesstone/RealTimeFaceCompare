
package com.hzgc.collect.expand.merge;

        import com.hzgc.collect.expand.conf.CommonConf;

        import java.util.concurrent.Executors;
        import java.util.concurrent.ScheduledExecutorService;
        import java.util.concurrent.TimeUnit;

public class ScheRecoErrData {
    public void scheduled(CommonConf conf) {
        ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
        pool.scheduleAtFixedRate(new RecoverErrProData(conf), conf.getMergeScanTime(),
                conf.getMergeScanTime(), TimeUnit.SECONDS);
    }
}