import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Zk {
    private static final Logger LOG = Logger.getLogger(Zk.class);
    //定义session失效时间
    private static final int SESSION_TIMEOUT = 10000;
    //Zookeeper地址
    private static final String ZOOKEEPER_ADDRESS = "172.18.18.103:2181,172.18.18.104:2181,172.18.18.105:2181";
    //注册在path上的Watcher,节点变更会通知会向客户端发起通知
    private static final boolean WATCHER = false;
    //Zookeeper中存储ipcIds路径
    private static final String PATH = "/mq_ipcid";
    //Zookeeper变量
    private ZooKeeper zooKeeper = null;
    //信号量设置，用于等待zookeeper连接建立之后，通知阻塞程序继续向下执行
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public void createConnection(String connectAddr, int sessionTimeout) {
        releaseConnection();
        try {
            zooKeeper = new ZooKeeper(connectAddr, sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    //获取事件的状态
                    Event.KeeperState keeperState = watchedEvent.getState();
                    Event.EventType eventType = watchedEvent.getType();
                    //如果是建立连接
                    if (Event.KeeperState.SyncConnected == keeperState) {
                        if (Event.EventType.None == eventType) {
                            //如果建立连接成功，则发送信号量，让后续阻塞程序向下执行
                            connectedSemaphore.countDown();
                            System.out.println("ZK 建立连接");
                            //LOG.info("ZK 建立连接");
                        }
                    }
                }
            });
            System.out.println("开始连接Zookeeper服务器");
            //LOG.info("开始连接Zookeeper服务器");
            //进行阻塞
            connectedSemaphore.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncCreateNode(List<String> ipcIdList){
        this.createConnection(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT);
        StringBuilder ipcIds = new StringBuilder();
        if (!ipcIdList.isEmpty()){
            for (String ipcId : ipcIdList){
                ipcIds.append(ipcId).append(",");
            }
            try {
                //String path = zooKeeper.create(PATH,ipcIds.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
                zooKeeper.setData(PATH,ipcIds.toString().getBytes(),-1);
                //System.out.println("path = " + path);
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 取出zk信息
     * @return ipcids
     */
    public String readDate() {
        String ipcids = null;
        this.createConnection(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT);
        try {
            Stat stat = zooKeeper.exists(PATH,WATCHER);
            byte[] data = zooKeeper.getData(PATH, WATCHER, stat);
            ipcids = new String(data);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            releaseConnection();
        }
        return ipcids;
    }

    /**
     * 关闭ZK连接
     */
    public void releaseConnection() {
        if (this.zooKeeper != null) {
            try {
                this.zooKeeper.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Zk zk = new Zk();
        String aa = zk.readDate();
        System.out.println(aa);
        List<String> ipcIdList = new ArrayList<>();
        ipcIdList.add("aaaa");
        ipcIdList.add("bbbb");
        ipcIdList.add("333");
        zk.syncCreateNode(ipcIdList);
    }
}
