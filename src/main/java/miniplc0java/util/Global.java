package miniplc0java.util;

public class Global {
    public String value = "";
    public int count;
    public boolean isConst;

    public Global() {
        this.count = 4;
        this.isConst = false;
    }

    public Global(String value) {
        this.value = value;
        this.count = value.length();
        this.isConst = true;
    }
}
