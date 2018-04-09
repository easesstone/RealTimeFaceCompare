package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PrisonSearchOpts implements Serializable{
    private List<String> pkeysCount; // 求各个对象类型下，人员所在位置的人数情况下的时候，需要传过来的参数，举例：List<pkeys>
    private List<String> pkeysReset; // 需要重置人员位置的对象库，即传过来一批对象类型List, 举例：List<pkeys>
    private Map<String, List<String>> pkeysUpate;  // 需要更新的人员的数据。 多批人员，举例：Map<location,List<id>>

    public PrisonSearchOpts() {
    }

    public List<String> getPkeysCount() {
        return pkeysCount;
    }

    public void setPkeysCount(List<String> pkeysCount) {
        this.pkeysCount = pkeysCount;
    }

    public List<String> getPkeysReset() {
        return pkeysReset;
    }

    public void setPkeysReset(List<String> pkeysReset) {
        this.pkeysReset = pkeysReset;
    }

    public Map<String, List<String>> getPkeysUpate() {
        return pkeysUpate;
    }

    public void setPkeysUpate(Map<String, List<String>> pkeysUpate) {
        this.pkeysUpate = pkeysUpate;
    }

    @Override
    public String toString() {
        return "PrisonSearchOpts{" +
                "pkeysCount=" + pkeysCount +
                ", pkeysReset=" + pkeysReset +
                ", pkeysUpate=" + pkeysUpate +
                '}';
    }
}
