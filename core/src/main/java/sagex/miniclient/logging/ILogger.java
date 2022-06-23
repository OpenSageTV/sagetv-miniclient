package sagex.miniclient.logging;

public interface ILogger
{

    ILogger getLoggerInstance(Class cls);
    ILogger getLoggerInstance(String name);
    void recordException(Throwable t);
    void logError(String message);
    void logWarning(String message);
    void logDebug(String message);
    void logInfo(String message);
    void logTrace(String message);
    void logError(String message, Throwable t);
    void logWarning(String message, Throwable t);
    void logDebug(String message, Throwable t);
    void logInfo(String message, Throwable t);
    void logTrace(String message, Throwable t);

    void setCustomKey(String key, String value);
    void setUserID(String userID);
}
