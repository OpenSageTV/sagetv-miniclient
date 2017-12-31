package sagex.miniclient.uibridge;

public interface HandlesNativeGFXCommand {
    /**
     *
     * @param cmd
     * @param len
     * @param cmddata
     * @param hasret
     * @return 1 if hasret has a return, otherwise 0
     */
    int ExecuteGFXCommand(int cmd, int len, byte[] cmddata, int[] hasret);
}
