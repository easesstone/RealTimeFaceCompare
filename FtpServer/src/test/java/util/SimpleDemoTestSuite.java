package util;

import org.junit.Test;

public class SimpleDemoTestSuite {
    final long hello = 123456;
    final Flag flag = new Flag();
    @Test
    public void demoFinal() {
//        hello = 12345;
        System.out.println(hello);
        System.out.println(flag.isFlag());
        flag.setFlag(true);
        System.out.println(flag.isFlag());
    }
}

class Flag {
    private boolean flag;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "Flag{" +
                "flag=" + flag +
                '}';
    }
}