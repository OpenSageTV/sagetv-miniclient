package sagex.miniclient.android.util;

import sagex.miniclient.logging.ILogger;
import org.slf4j.LoggerFactory;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class Logger implements ILogger
{
    private Class cls;
    private org.slf4j.Logger log;
    //private FirebaseCrashlytics crashlogger;

    public static Logger getLogger(Class cls)
    {
        Logger log = new Logger();
        log.log = LoggerFactory.getLogger(cls);

        //log.crashlogger = FirebaseCrashlytics.getInstance();

        return log;
    }

    public static Logger getLogger(String name)
    {
        Logger log = new Logger();
        log.log = LoggerFactory.getLogger(name);
        //log.crashlogger = FirebaseCrashlytics.getInstance();

        return log;
    }

    @Override
    public ILogger getLoggerInstance(String name)
    {
        return Logger.getLogger(name);
    }

    @Override
    public ILogger getLoggerInstance(Class cls)
    {
        return Logger.getLogger(cls);
    }

    @Override
    public void recordException(Throwable t)
    {
        FirebaseCrashlytics.getInstance().recordException(t);

    }

    @Override
    public void logError(String message)
    {
        FirebaseCrashlytics.getInstance().log(message);
        log.error(message);
    }

    @Override
    public void logError(String message, Throwable t)
    {
        FirebaseCrashlytics.getInstance().log(message);
        FirebaseCrashlytics.getInstance().recordException(t);
        log.error(message);
    }

    @Override
    public void logWarning(String message)
    {
        FirebaseCrashlytics.getInstance().log(message);
        log.warn(message);
    }

    @Override
    public void logWarning(String message, Throwable t)
    {
        FirebaseCrashlytics.getInstance().log(message);
        FirebaseCrashlytics.getInstance().recordException(t);
        log.warn(message, t);
    }

    @Override
    public void logDebug(String message)
    {
        FirebaseCrashlytics.getInstance().log(message);
        log.debug(message);
    }

    @Override
    public void logDebug(String message, Throwable t)
    {
        FirebaseCrashlytics.getInstance().log(message);
        FirebaseCrashlytics.getInstance().recordException(t);
        log.debug(message, t);
    }

    @Override
    public void logInfo(String message)
    {
        FirebaseCrashlytics.getInstance().log(message);
        log.info(message);
    }

    @Override
    public void logInfo(String message, Throwable t)
    {
        FirebaseCrashlytics.getInstance().log(message);
        FirebaseCrashlytics.getInstance().recordException(t);
        log.info(message, t);
    }

    @Override
    public void logTrace(String message)
    {
        FirebaseCrashlytics.getInstance().log(message);
        log.trace(message);
    }

    @Override
    public void logTrace(String message, Throwable t)
    {
        FirebaseCrashlytics.getInstance().log(message);
        FirebaseCrashlytics.getInstance().recordException(t);
        log.trace(message, t);
    }

    @Override
    public void setCustomKey(String key, String value)
    {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value);
    }

    @Override
    public void setUserID(String userID)
    {
        FirebaseCrashlytics.getInstance().setUserId(userID);
    }
}
