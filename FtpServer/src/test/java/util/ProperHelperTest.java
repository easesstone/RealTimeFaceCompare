package util;

import com.hzgc.collect.FTP;
import com.hzgc.collect.expand.util.*;
import org.junit.Test;

import java.io.IOException;

public class ProperHelperTest {

    public ProperHelperTest() {
        HelperFactory.regist();
    }

    @Test
    public void ClusterOverFtpProperHelperTest() {
        ClusterOverFtpProperHelper.getPort();
        ClusterOverFtpProperHelper.getDataPorts();
        ClusterOverFtpProperHelper.getImplicitSsl();
        ClusterOverFtpProperHelper.getThreadNum();
        ClusterOverFtpProperHelper.getProps();
    }

    @Test
    public void FTPAddressProperHelperTest() {
        FTPAddressProperHelper.getIp();
        FTPAddressProperHelper.getPort();
        FTPAddressProperHelper.getUser();
        FTPAddressProperHelper.getPassword();
        FTPAddressProperHelper.getPathRule();
        FTPAddressProperHelper.getProps();
    }

    @Test
    public void ProducerOverFtpProperHelperTest() {
        ProducerOverFtpProperHelper.getBootstrapServers();
        ProducerOverFtpProperHelper.getClientId();
        ProducerOverFtpProperHelper.getRequestRequiredAcks();
        ProducerOverFtpProperHelper.getRetries();
        ProducerOverFtpProperHelper.getKeySerializer();
        ProducerOverFtpProperHelper.getTopicFeature();
        ProducerOverFtpProperHelper.getProps();
    }

    @Test
    public void RocketMQProperHelperTest() {
        RocketMQProperHelper.getAddress();
        RocketMQProperHelper.getTopic();
        RocketMQProperHelper.getGroup();
    }


}
