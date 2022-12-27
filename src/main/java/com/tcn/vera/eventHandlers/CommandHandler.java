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
import java.util.Arrays;
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
    private String[] botOwner;
    private final String prefix;

    /**
     * To create an instance of this class, please use the {@link CommandHandlerBuilder}.
     */
    CommandHandler(ArrayList<CommandTemplate> commandList, String[] botOwner, String prefix){
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
        if(toRegister.isAllowSlashCommand() && (null != toRegister.getSlashCommand())){
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
                if(command.isOwnerCommand()){
                    System.out.println("Author ID = " + event.getAuthor().getId());
                    if(Arrays.stream(botOwner).anyMatch(c -> c.equalsIgnoreCase(event.getAuthor().getId()))) {
                        logger.debug(event.getAuthor().getName() + " has used the \"" + command.getCommandName() + "\" chat command");
                        executeChatCommand(command, event, rawMessage.substring(commandName.length() + 1));
                    }else{
                        logger.warn(event.getAuthor().getName() + " has attempted to use the \"" + command.getCommandName()
                                + "\" owner chat command without being on the list of owners!");
                    }

                }else{
                    logger.debug(event.getAuthor().getName() + " has used the \"" + command.getCommandName() + "\" chat command");
                    executeChatCommand(command, event, rawMessage.substring(commandName.length() + 1));
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        CommandTemplate command = slashCommandSet.stream().filter(
                p -> p.getSlashCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if(null != command){
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeSlashCommand(command, event);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {

        //once JDA is ready we can actually send the new slash commands to discord
        List<CommandData> slashCommandList = new ArrayList<>();
        for (CommandTemplate command : slashCommandSet){
            slashCommandList.add(command.getSlashCommand());
        }
        event.getJDA().updateCommands().addCommands(slashCommandList).queue();
        logger.info("Registered " + slashCommandSet.size() + " slash commands");

        //we should also check to ensure that there is an owner set. If not, we should be able to get it from JDA
        if(null == botOwner || botOwner.length < 1){
            event.getJDA().retrieveApplicationInfo().onSuccess(c -> botOwner = new String[] {c.getOwner().getId()}).submit();
            logger.warn("No owner IDs were given. New owner ID: " + botOwner[0]);
        }
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
}
