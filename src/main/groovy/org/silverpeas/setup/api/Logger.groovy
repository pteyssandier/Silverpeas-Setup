package org.silverpeas.setup.api

import org.gradle.api.logging.LogLevel

import java.sql.SQLException

/**
 * The logger to be used in the plugin execution context to trace processing information into a
 * log file.
 * </p>
 * This logger is defined because Gradle vampires the logging system to redirect any traces into
 * the standard outputs and doesn't provide any configuration to define new appender or to override
 * this logging behaviour.
 * </p>
 * If this logger isn't initialized by invoking the {@code Logger#init(File, LogLevel)} static
 * method, then by default the traces will be written into the standard output with INFO as level.
 * @author mmoquillon
 */
class Logger {

  private static def DEFAULT_LOG_HANDLER = System.out
  private static LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO
  private LogLevel level
  private String namespace
  private def logHandler

  /**
   * Initialises the logging system of the plugin by specifying the default logging handler to use
   * for storing the traces of all loggers and the default logging level from which the traces will
   * be really output.
   * @param logFile the default log file to use to write the traces.
   * @param level the default level from which any traces will be output.
   */
  static void init(File logFile, LogLevel level) {
    DEFAULT_LOG_HANDLER = logFile
    DEFAULT_LOG_LEVEL = level
  }

  /**
   * Gets a logger for the specified namespace or scopes of traces.
   * @param namespace the namespace within which the traces to output has to belong.
   * @return the logger matching the specified namespace.
   */
  static Logger getLogger(String namespace) {
    return new Logger(namespace)
  }

  /**
   * Gets a logger for the specified namespace or scopes of traces and by using explicitly the
   * specified logging handler instead of the default one (the one that was set by invoking the
   * init method).
   * @param namespace the namespace within which the traces to output has to belong.
   * @param logHandler the logging handler to use instead of the default one.
   * @return the logger matching the specified namespace.
   */
  static Logger getLogger(String namespace, logHandler) {
    Logger logger = new Logger(namespace)
    logger.logHandler = logHandler
    return logger
  }

  /**
   * Gets the current logging level.
   * @return the logging level.
   */
  LogLevel level() {
    return this.level == null ? DEFAULT_LOG_LEVEL:this.level
  }

  /**
   * Sets a new logging level to this logger.
   * @param newLevel the new logging level to set.
   * @return itself.
   */
  Logger level(LogLevel newLevel) {
    this.level = newLevel
    return this
  }

  /**
   * Writes out in a new line the specified message as an INFORMATION level.
   * @param msg the message to output.
   * @return itself.
   */
  Logger info(String msg) {
    return formatInfo("\n${msgHeading()} %s", msg)
  }

  /**
   * Writes out as an INFORMATION level a raw message in the specified format and with the given
   * arguments.
   * @param format the format of the message to output.
   * @param args the different arguments to put in the message format.
   * @return itself.
   */
  Logger formatInfo(String format, Object... args) {
    if (this.level <= LogLevel.INFO) {
      formatMsg(format, args)
    }
    return this
  }

  /**
   * Writes out in a new line the specified message as an DEBUG level.
   * @param msg the message to output.
   * @return itself.
   */
  Logger debug(String msg) {
    return formatDebug("\n${msgHeading()} %s", msg)
  }

  /**
   * Writes out as a DEBUG level a raw message in the specified format and with the given arguments.
   * @param format the format of the message to output.
   * @param args the different arguments to put in the message format.
   * @return itself.
   */
  Logger formatDebug(String format, Object... args) {
    if (this.level <= LogLevel.DEBUG) {
      formatMsg(format, args)
    }
    return this
  }

  /**
   * Writes out in a new line the specified message as a WARNING level.
   * @param msg the message to output.
   * @return itself.
   */
  Logger warn(String msg) {
    return formatWarn("\n${msgHeading()} %s", msg)
  }

  /**
   * Writes out as a WARNING level a raw message in the specified format and with the given
   * arguments.
   * @param format the format of the message to output.
   * @param args the different arguments to put in the message format.
   * @return itself.
   */
  Logger formatWarn(String format, Object... args) {
    if (this.level <= LogLevel.WARN) {
      formatMsg(format, args)
    }
    return this
  }

  /**
   * Writes out in a new line the specified message as an ERROR level.
   * @param msg the message to output.
   * @return itself.
   */
  Logger error(String msg) {
    return formatError("\n${msgHeading()} %s", msg)
  }

  /**
   * Writes out in new lines the specified cause as an ERROR level.
   * @param cause the exception as the cause of the error.
   * @return itself.
   */
  Logger error(Throwable cause) {
    StringWriter stackTrace = new StringWriter()
    cause.printStackTrace(new PrintWriter(stackTrace))
    formatError("\n${msgHeading()} %s\n%s", cause.getMessage(), stackTrace.toString())
    if (cause instanceof SQLException) {
      SQLException ex = (SQLException) cause
      if (ex.nextException != null) {
        error(ex.nextException)
      }
    }
    return this
  }

  /**
   * Writes out in new lines the specified message as an ERROR level.
   * @param msg the message to output.
   * @param cause the exception as the cause of the error.
   * @return itself.
   */
  Logger error(String msg, Throwable cause) {
    StringWriter stackTrace = new StringWriter()
    cause.printStackTrace(new PrintWriter(stackTrace))
    return formatError("\n${msgHeading()} %s\n%s\n%s", msg, cause.getMessage(), stackTrace.toString())
  }

  /**
   * WWrites out as an ERROR level a raw message in the specified format and with the given
   * arguments.
   * @param format the format of the message to output.
   * @param args the different arguments to put in the message format.
   * @return itself.
   */
  Logger formatError(String format, Object... args) {
    if (this.level <= LogLevel.ERROR) {
      formatMsg(format, args)
    }
    return this
  }

  private String msgHeading() {
    return "[${namespace}]"
  }

  private void formatMsg(String format, Object... args) {
    Formatter formatter = new Formatter()
    def handler = getLogHandler()
    handler << formatter.format(format, args).toString()
  }

  private def getLogHandler() {
    return this.logHandler == null ? DEFAULT_LOG_HANDLER: this.logHandler;
  }

  private Logger(String namespace) {
    this.namespace = namespace
  }

}
