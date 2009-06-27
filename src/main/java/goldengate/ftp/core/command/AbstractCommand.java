/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package goldengate.ftp.core.command;

import goldengate.common.command.CommandInterface;
import goldengate.common.exception.InvalidArgumentException;
import goldengate.common.file.SessionInterface;
import goldengate.common.logging.GgInternalLogger;
import goldengate.common.logging.GgInternalLoggerFactory;
import goldengate.ftp.core.config.FtpConfiguration;
import goldengate.ftp.core.session.FtpSession;

/**
 * Abstract definition of an FTP Command
 *
 * @author Frederic Bregier
 *
 */
public abstract class AbstractCommand implements CommandInterface {
    /**
     * Internal Logger
     */
    private static final GgInternalLogger logger = GgInternalLoggerFactory
            .getLogger(AbstractCommand.class);

    /**
     * Code of Command
     */
    private FtpCommandCode code;

    /**
     * String attached to the command
     */
    private String command;

    /**
     * Argument attached to this command
     */
    private String arg;

    /**
     * The Ftp SessionInterface
     */
    private FtpSession session;

    /**
     * Internal Object (whatever the used). This has to be clean by Business
     * Handler cleanSession.
     */
    private Object object;

    /**
     * Extra allowed nextCommand
     */
    private FtpCommandCode extraNextCommand = null;

    /*
     * (non-Javadoc)
     *
     * @see
     * goldengate.common.command.CommandInterface#setArgs(goldengate.common.
     * session.Session, java.lang.String, java.lang.String, java.lang.Enum)
     */
    @SuppressWarnings("unchecked")
    public void setArgs(SessionInterface session, String command, String arg,
            Enum code) {
        this.session = (FtpSession) session;
        this.command = command;
        this.arg = arg;
        this.code = (FtpCommandCode) code;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * goldengate.common.command.CommandInterface#setExtraNextCommand(java.lang
     * .Enum)
     */
    @SuppressWarnings("unchecked")
    public void setExtraNextCommand(Enum extraNextCommand) {
        if (extraNextCommand != FtpCommandCode.NOOP) {
            this.extraNextCommand = (FtpCommandCode) extraNextCommand;
        } else {
            this.extraNextCommand = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * goldengate.common.command.CommandInterface#isNextCommandValid(goldengate
     * .common.command.CommandInterface)
     */
    public boolean isNextCommandValid(CommandInterface newCommandArg) {
        AbstractCommand newCommand = (AbstractCommand) newCommandArg;
        Class<? extends AbstractCommand> newClass = newCommand.getClass();
        // Special commands: QUIT ABORT STAT NOP
        if (FtpCommandCode.isSpecialCommand(newCommand.getCode())) {
            // logger.debug("VALID since {}", newCommand.command);
            return true;
        }
        if (extraNextCommand != null) {
            if (extraNextCommand.command == newClass) {
                // logger.debug("VALID {} after {} since extra next command",
                // newCommand.command, command);
                return true;
            }
            if (code.nextValids != null && code.nextValids.length > 0) {
                for (Class<?> nextValid: code.nextValids) {
                    if (nextValid == newClass) {
                        // logger.debug("VALID {} after {} since next command",
                        // newCommand.command, command);
                        return true;
                    }
                }
            }
            // logger.debug("NOT VALID {} after {}", newCommand.command,
            // command);
            return false;
        }
        if (code.nextValids == null || code.nextValids.length == 0) {
            // Any command is allowed
            // logger.debug("VALID {} after {} since all valid",
            // newCommand.command, command);
            return true;
        }
        for (Class<?> nextValid: code.nextValids) {
            if (nextValid == newClass) {
                // logger.debug("VALID {} since next command {}",
                // newCommand.command, command);
                return true;
            }
        }
        // logger.debug("DEFAULT NOT VALID {} after {}", newCommand.command,
        // command);
        return false;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getArg() {
        return arg;
    }

    public String[] getArgs() {
        return arg.split(" ");
    }

    public int getValue(String argx) throws InvalidArgumentException {
        int i = 0;
        try {
            i = Integer.parseInt(argx);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException("Not an integer", e);
        }
        return i;
    }

    public String getCommand() {
        return command;
    }

    public boolean hasArg() {
        return arg != null && arg.length() != 0;
    }

    /**
     *
     * @return the current FtpSession
     */
    public FtpSession getSession() {
        return session;
    }

    // some helpful functions
    /**
     *
     * @return The current configuration object
     */
    public FtpConfiguration getConfiguration() {
        return session.getConfiguration();
    }

    public void invalidCurrentCommand() {
        session.getRestart().setSet(false);
        session.setPreviousAsCurrentCommand();
    }

    /**
     *
     * @return The FtpCommandCode associated with this command
     */
    public FtpCommandCode getCode() {
        return code;
    }
}
