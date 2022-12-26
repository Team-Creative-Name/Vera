/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2022 Thomas Wessel and the rest of Team Creative Name
 *
 *
 * This library is licensed under the GNU Lesser General Public License v2.1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 *
 * For more information, please check out the original repository of this project on github
 * https://github.com/Team-Creative-Name/Vera
 */
package com.tcn.vera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;

/**
 * A collection of static methods used internally for Vera.
 */
public class VeraUtils {

    public static ThreadFactory createThreadFactory(String threadName, Logger logger, boolean isDaemon){

        return (r ->{
            Thread thread = new Thread(r, threadName);
            thread.setDaemon(isDaemon);
            thread.setUncaughtExceptionHandler((Thread errorThread, Throwable throwable) ->
                    logger.error("There was an uncaught exception in the {} thread pool! ", thread.getName(), throwable)
            );
            return thread;
        });
    }

    public static ThreadFactory createThreadFactory(String threadName) {
        return createThreadFactory(threadName, LoggerFactory.getLogger("Vera: Threading"), true);
    }

    public static ThreadFactory createThreadFactory(String threadName, Logger logger) {
        return createThreadFactory(threadName, logger, true);
    }

    public static ThreadFactory createThreadFactory(String threadName, boolean isDaemon) {
        return createThreadFactory(threadName, LoggerFactory.getLogger("Vera: Threading"), isDaemon);
    }

    /**
     * Takes in a full command string and strips it of both the prefix and any additional command context.
     * @return
     *      A String containing just the first word in a string with the assumed prefix removed
     */
    public static String getCommandName(String fullCommand){
        if(fullCommand.trim().contains(" ")){
            int index = fullCommand.trim().indexOf(" ");
            return fullCommand.trim().substring(1, index);
        }else if(fullCommand.length() > 1){
            return fullCommand.trim().substring(1);
        }else{
            return fullCommand.trim();
        }
    }

}
