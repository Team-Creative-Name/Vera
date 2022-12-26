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
package com.tcn.vera.eventHandlers;

import com.tcn.vera.commands.CommandTemplate;
import com.tcn.vera.utils.VeraUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The command handler for Vera. This class must be constructed via the CommandHandlerBuilder.
 */
public class CommandHandler extends ListenerAdapter {

    //Required for command processing
    private final Set<CommandTemplate> chatCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<CommandTemplate> slashCommandSet = ConcurrentHashMap.newKeySet();
    private final ExecutorService commandPool = Executors.newCachedThreadPool(VeraUtils.createThreadFactory("VeraCommandRunner", false));
    private final Logger logger;

    //bot specific information
    private final String[] botOwner;
    private final String prefix;

    private CommandHandler(ArrayList<CommandTemplate> commandList, String[] botOwner, String prefix){
        logger = LoggerFactory.getLogger("Vera: Command Handler");
        this.botOwner = botOwner;
        this.prefix = prefix;

        for(CommandTemplate command: commandList){
            registerChatCommand(command);
            registerSlashCommand(command);
        }
        logger.info("Registered " + chatCommandSet.size() + " chat commands.");
    }

    private void registerChatCommand(CommandTemplate toRegister){
        if(toRegister.isAllowChatCommand()){
            if(this.chatCommandSet.stream().map(CommandTemplate::getCommandName).noneMatch(c -> toRegister.getCommandName().equalsIgnoreCase(c))){
                this.chatCommandSet.add(toRegister);
            }
        }
    }

    private void registerSlashCommand(CommandTemplate toRegister){
        if(toRegister.isAllowSlashCommand()){
            if(this.slashCommandSet.stream().map(CommandTemplate::getCommandName).noneMatch(c -> toRegister.getCommandName().equalsIgnoreCase(c))){
                this.slashCommandSet.add(toRegister);
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        if(event.getMessage().getContentRaw().toLowerCase().startsWith(prefix) && !event.getAuthor().isBot()){
            String rawMessage = event.getMessage().getContentRaw();
            String commandName = VeraUtils.getCommandName(rawMessage);

            CommandTemplate command = chatCommandSet.stream().filter(
                    p -> p.getAllCommandNames().contains(commandName)
            ).findFirst().orElse(null);


            if(null != command){
                logger.debug(event.getAuthor().getName() + " has used the \"" + command.getCommandName() + "\" chat command");
                executeChatCommand(command, event, rawMessage.substring(commandName.length() + 1));
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        CommandTemplate command = slashCommandSet.stream().filter(
                p -> p.getCommandName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if(null != command){
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeSlashCommand(command, event);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> slashCommandList = new ArrayList<>();
        for (CommandTemplate command : slashCommandSet){
            slashCommandList.add(command.getSlashCommand());
        }
        event.getJDA().updateCommands().addCommands(slashCommandList).queue();
        logger.info("Registered" + slashCommandSet.size() + " slash commands");
    }

    private void executeChatCommand(CommandTemplate template, MessageReceivedEvent event, String messageContent){
        this.commandPool.submit(() ->{
            try{
                template.executeChatCommand(event, event.getMessage(), messageContent);
            }catch (final Exception e){
                logger.error("Error while executing the \"" + template.getCommandName() + "\" chat command! \n" +
                        "Exception: " + e.getLocalizedMessage());
                event.getMessage().reply("Sorry, I was unable to finish executing that command. Please try again later.").queue();
            }
        });
    }

    private void executeSlashCommand(CommandTemplate template, SlashCommandInteractionEvent event){
        this.commandPool.submit(() ->{
            try{
                template.executeSlashCommand(event);
            }catch (final Exception e){
                logger.error("Error while executing the \"" + template.getCommandName() + "\" slash command! \n" +
                        "Exception: " + e.getLocalizedMessage());
                if(event.isAcknowledged()){
                    event.getHook().editOriginal("Sorry, I was unable to finish executing that command. Please try again later.").setActionRow().setEmbeds().queue();
                }else{
                    event.reply("Sorry, I was unable to finish executing that command. Please try again later.").setEphemeral(true).queue();
                }
            }
        });
    }



    public static class CommandHandlerBuilder{
        private ArrayList<CommandTemplate> commandList = new ArrayList<>();
        private String[] botOwner = new String[0];
        private String prefix = "!";

        public CommandHandler build(){
            runChecks();
            return new CommandHandler(commandList, botOwner, prefix);
        }

        private void runChecks(){

            if(commandList.isEmpty()){
                throw new IllegalArgumentException("Cannot build CommandHandler without any commands! Please ensure that there " +
                        "is at least one command added via the CommandHandlerBuilder.addCommand method!");
            }

            if(prefix.length() > 1 || prefix.equalsIgnoreCase(" ")){
                throw new IllegalArgumentException("The prefix cannot be longer than one character and cannot be a black space." +
                        "Please modify the prefix given to the changePrefix method.");
            }

            for (CommandTemplate command : commandList){

                //ensure that there is at least one type of command enabled
                if(!command.isAllowChatCommand() && !command.isAllowSlashCommand()){
                    throw new IllegalArgumentException("The command \"" + command.getCommandName() + "\" must have either " +
                            "a slash command or a chat command enabled in order to be registered. Please set one of the " +
                            "booleans to \"true\" in the command's constructor.");
                }

                //ensure that there is not a space in the command name
                if(command.getCommandName().contains(" ")){
                    throw new IllegalArgumentException("The command \"" + command.getCommandName() + "\" must not contain " +
                            "any spaces in its name. Please change the commandName string in the command's constructor.");
                }
            }

        }

        /**
         * Adds a command to be registered to the command handler. Can be called multiple times to add more commands.
         * @param newCommand
         *  The command object that you wish to be handled by the command handler.
         * @return
         *  This builder.
         */
        public CommandHandlerBuilder addCommand(CommandTemplate newCommand){
            commandList.add(newCommand);
            return this;
        }

        /**
         * Adds a user ID that is allowed to access commands marked as owner only. This command can be called multiple
         * times to add additional users.
         * @param owner
         *  The discord ID of the user you would like to add.
         * @return
         *  This builder
         */
        public CommandHandlerBuilder addOwner(String owner){
            String[] temp = new String[botOwner.length + 1];
            System.arraycopy(botOwner, 0, temp, 0, botOwner.length);
            temp[botOwner.length] = owner;
            botOwner = temp;
            return this;
        }

        /**
         * Adds multiple user IDs that are allowed to access commands marked as owner only. This command can be called multiple
         * times to add additional users.
         * @param owner
         *  A string array of discord user IDs.
         * @return
         *  This builder
         */
        public CommandHandlerBuilder addOwner(String[] owner){
            String[] temp = new String[botOwner.length + owner.length];
            System.arraycopy(botOwner, 0, temp, 0, botOwner.length);
            System.arraycopy(owner, 0, temp, botOwner.length, owner.length);
            botOwner = temp;
            return this;
        }

        /**
         * Changes the bot prefix from the default '!' prefix.
         * @param newPrefix
         *  The prefix that you want to use. It cannot be longer than one character.
         * @return
         *  This builder
         */
        public CommandHandlerBuilder changePrefix(String newPrefix){
            prefix = newPrefix;
            return this;
        }

    }

}
