/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2022-23 Thomas Wessel and the rest of Team Creative Name
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

import com.tcn.vera.commands.builtin.chatHelpCommand;
import com.tcn.vera.commands.builtin.slashHelpCommand;
import com.tcn.vera.commands.interactions.*;
import com.tcn.vera.commands.templates.*;
import com.tcn.vera.utils.VeraUtils;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The command handler for Vera.
 * <p>
 * It is recommended that you only have one instance of this class per bot. This class automatically registers all commands and
 * will therefore overwrite any commands it is not aware of.
 * <p>
 * To create an instance of this class, please use the {@link CommandHandlerBuilder}.
 */
public class CommandHandler extends ListenerAdapter {

    //Required for command processing
    private final Set<ChatCommandTemplate> chatCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<SlashCommandTemplate> slashCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<UserContextTemplate> userContextCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<MessageContextTemplate> messageContextCommandSet = ConcurrentHashMap.newKeySet();
    private final ButtonHandler buttonHandler;
    private final ExecutorService commandPool = Executors.newCachedThreadPool(VeraUtils.createThreadFactory("VeraCommandRunner", false));
    private final Logger logger;

    //bot specific information
    private final ArrayList<String> botOwner;
    private final String prefix;

    /**
     * To create an instance of this class, please use the {@link CommandHandlerBuilder}.
     */
    CommandHandler(ArrayList<? extends CommandTemplateBase> commandList, ArrayList<String> botOwner, String prefix, ButtonHandler buttonHandler, boolean enableHelpCommands) {
        logger = LoggerFactory.getLogger("Vera: Command Handler");
        this.botOwner = botOwner;
        this.prefix = prefix;
        this.buttonHandler = buttonHandler;

        //split the commandList into their respective hashmaps. This allows us to get specific command types easier
        for (CommandTemplateBase command : commandList) {
            switch (command.getCommandType()) {
                case CHAT_COMMAND -> registerCommand(this.chatCommandSet, (ChatCommandTemplate) command);
                case SLASH_COMMAND -> registerCommand(this.slashCommandSet, (SlashCommandTemplate) command);
                case USER_CONTEXT_COMMAND -> registerCommand(this.userContextCommandSet, (UserContextTemplate) command);
                case CONTEXT_MESSAGE_COMMAND -> registerCommand(this.messageContextCommandSet, (MessageContextTemplate) command);
                default -> throw new IllegalArgumentException("Sorry, this command handler is only capable of handling Chat, Slash, and Context commands. " +
                                "If you have created your own command type, you will have to extend this class and add support for it here!");
            }

            //aside from splitting our types out, we also need to put all buttons into their cache
            if (command instanceof ButtonInterface buttonInterface) {
                buttonHandler.registerButtonSet(buttonInterface.getButtonClassID(), buttonInterface::executeButton);
            }
        }

        //if help commands are enabled, we need to register them too
        if(enableHelpCommands) {
            if(!chatCommandSet.isEmpty()){
                registerCommand(this.chatCommandSet, new chatHelpCommand(chatCommandSet, prefix));
            }else{
                logger.debug("No chat commands were registered. The default chat help command will not be registered.");
            }

            if(!slashCommandSet.isEmpty()){
                registerCommand(this.slashCommandSet, new slashHelpCommand(slashCommandSet));
            }else{
                logger.debug("No slash commands were registered. The default slash help command will not be registered.");
            }
        }

        logger.info("Registered {} chat command(s).", chatCommandSet.size());
        logger.info("Registered {} slash command(s).", slashCommandSet.size());
        logger.info("Registered {} user context menu command(s).", userContextCommandSet.size());
        logger.info("Registered {} message context menu command(s).", messageContextCommandSet.size());

    }


    private <T extends CommandTemplateBase> void registerCommand(Set<T> commandSet, T toRegister){

        if(toRegister instanceof ChatCommandTemplate toRegisterChat){
            if(chatCommandSet.stream().flatMap(chatCommandTemplate -> chatCommandTemplate.getAllCommandNames().stream()).noneMatch(existingAlias -> toRegisterChat.getAllCommandNames().contains(existingAlias))){
                logger.debug("Registering "+ toRegister.getCommandType().toString().toLowerCase() + " with name \"" + toRegister.getCommandName()+ "\" and aliases:" + Arrays.toString(toRegisterChat.getAliases()));
                commandSet.add(toRegister);
            }else{
                logger.error("A chat command with either the name \"" + toRegisterChat.getCommandName() + "\" or one of its aliases " + Arrays.toString(toRegisterChat.getAliases()) + " has already been registered. Command names and aliases must be unique, lowercase and alphanumeric. This command will not be registered.");
            }
        }else if (commandSet.stream().map(CommandTemplateBase::getCommandName).noneMatch(c -> toRegister.getCommandName().equalsIgnoreCase(c))) {
            logger.debug("Registering {} with name \"{}\"", toRegister.getCommandType().toString().toLowerCase(), toRegister.getCommandName());
            commandSet.add(toRegister);
        } else {
            logger.error("A command with the name \"{}\" has already been registered as a {}. Command names must be unique, lowercase and alphanumeric. This command will not be registered.", toRegister.getCommandName(), toRegister.getCommandType().toString().toLowerCase());
        }
    }


    @Override
    public void onReady(@NotNull ReadyEvent event) {
        //we need to send slash, userContext, and messageContext commands to discord
        List<CommandData> toAdd = new ArrayList<>();

        slashCommandSet.forEach(c -> toAdd.add(c.getSlashCommand()));
        userContextCommandSet.forEach(c -> toAdd.add(c.getUserContextCommand()));
        messageContextCommandSet.forEach(c -> toAdd.add(c.getMessageContextCommand()));

        event.getJDA().updateCommands().addCommands(toAdd).queue();

        logger.info("Sent " + slashCommandSet.size() + " slash command(s), " +
                userContextCommandSet.size() + " user context command(s), and " +
                messageContextCommandSet.size() + " message context command(s) to Discord.");

        //we should also check to ensure that there is an owner set. If not, we should be able to get it from JDA
        if (null == botOwner || botOwner.isEmpty()) {
            event.getJDA().retrieveApplicationInfo().onSuccess(c -> botOwner.add(c.getOwner().getId())).submit();
            logger.warn("No owner IDs were given. New owner ID: " + botOwner.get(0));
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().toLowerCase().startsWith(prefix) && !event.getAuthor().isBot()) {
            String rawMessage = event.getMessage().getContentRaw();
            String commandName = VeraUtils.getCommandName(rawMessage);

            ChatCommandTemplate command = chatCommandSet.stream().filter(
                    p -> p.getAllCommandNames().contains(commandName)
            ).findFirst().orElse(null);

            if (null != command) {
                if (command.isOwnerCommand()) {
                    if (botOwner.stream().anyMatch(c -> c.equalsIgnoreCase(event.getAuthor().getId()))) {
                        logger.debug(event.getAuthor().getName() + " has used the \"" + command.getCommandName() + "\" chat command");
                        executeChatCommand(command, event, rawMessage.substring(commandName.length() + 1));
                    } else {
                        logger.warn(event.getAuthor().getName() + " has attempted to use the \"" + command.getCommandName()
                                + "\" owner chat command without being on the list of owners!");
                    }
                } else {
                    logger.debug(event.getAuthor().getName() + " has used the \"" + command.getCommandName() + "\" chat command");
                    executeChatCommand(command, event, rawMessage.substring(commandName.length() + 1));
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        SlashCommandTemplate command = slashCommandSet.stream().filter(
                p -> p.getSlashCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if (null != command) {
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeSlashCommand(command, event);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        SlashCommandTemplate command = slashCommandSet.stream().filter(
                p -> p.getSlashCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if (command instanceof AutoCompleteInterface autoCompleteInstance) {
            logger.debug(event.getUser().getName() + " is using autocomplete on \"" + command.getCommandName() + "\"");
            executeAutoCompleteInteraction(autoCompleteInstance, event);
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (buttonHandler != null) {
            executeButtonInteraction(event);
        }
    }

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        for (UserContextTemplate command : userContextCommandSet) {
            if (command instanceof StringSelectInterface stringSelectInterfaceInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(stringSelectInterfaceInstance.getMenu().getId())) {
                    executeStringSelectInteraction(stringSelectInterfaceInstance, event);
                    return;
                }
            }
        }

        for (MessageContextTemplate command : messageContextCommandSet) {
            if (command instanceof StringSelectInterface stringSelectInterfaceInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(stringSelectInterfaceInstance.getMenu().getId())) {
                    executeStringSelectInteraction(stringSelectInterfaceInstance, event);
                    return;
                }
            }
        }

        for (SlashCommandTemplate command : slashCommandSet) {
            if (command instanceof StringSelectInterface stringSelectInterfaceInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(stringSelectInterfaceInstance.getMenu().getId())) {
                    executeStringSelectInteraction(stringSelectInterfaceInstance, event);
                    return;
                }
            }
        }
    }

    @Override
    public void onEntitySelectInteraction(@Nonnull EntitySelectInteractionEvent event) {
        for (UserContextTemplate command : userContextCommandSet) {
            if (command instanceof EntitySelectInterface entitySelectInterfaceInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(entitySelectInterfaceInstance.getMenu().getId())) {
                    executeEntitySelectInteraction(entitySelectInterfaceInstance, event);
                    return;
                }
            }
        }

        for (MessageContextTemplate command : messageContextCommandSet) {
            if (command instanceof EntitySelectInterface entitySelectInterfaceInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(entitySelectInterfaceInstance.getMenu().getId())) {
                    executeEntitySelectInteraction(entitySelectInterfaceInstance, event);
                    return;
                }
            }
        }

        for (SlashCommandTemplate command : slashCommandSet) {
            if (command instanceof EntitySelectInterface entitySelectInterfaceInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(entitySelectInterfaceInstance.getMenu().getId())) {
                    executeEntitySelectInteraction(entitySelectInterfaceInstance, event);
                    return;
                }
            }
        }
    }

    /**
     * Called by JDA whenever a ModalInteractionEvent fires on the event bus. Modals are supported by the Slash, userContext,
     * and messageContext templates, and we check every registered command of those types to see if they are responsible for
     * the event. If a match is found, we pass the event to the command and allow it to handle it further.
     *
     * @param event The {@link ModalInteractionEvent that JDA sent to Vera.}
     * @implNote This method loops through every registered command that supports
     */
    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        for (MessageContextTemplate command : messageContextCommandSet) {
            if (command instanceof ModalInterface modalInstance) {
                if (modalInstance.getModal().getId().equals(event.getModalId())) {
                    executeModalInteraction(modalInstance, event);
                    return;
                }
            }
        }

        for (UserContextTemplate command : userContextCommandSet) {
            if (command instanceof ModalInterface modalInstance) {
                if (modalInstance.getModal().getId().equals(event.getModalId())) {
                    executeModalInteraction(modalInstance, event);
                    return;
                }
            }
        }

        for (SlashCommandTemplate command : slashCommandSet) {
            if (command instanceof ModalInterface modalInstance) {
                if (modalInstance.getModal().getId().equals(event.getModalId())) {
                    executeModalInteraction((ModalInterface) command, event);
                    return;
                }
            }
        }
    }

    @Override
    public void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {
        UserContextTemplate command = userContextCommandSet.stream().filter(
                p -> p.getUserContextCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if (null != command) {
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeUserContextCommand(command, event);
        }
    }

    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
        MessageContextTemplate command = messageContextCommandSet.stream().filter(
                p -> p.getMessageContextCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if (null != command) {
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeMessageContextCommand(command, event);
        }
    }

    private void executeChatCommand(ChatCommandTemplate template, MessageReceivedEvent event, String messageContent) {
        this.commandPool.submit(() -> {
            try {
                template.executeChatCommand(event, event.getMessage(), messageContent);
            } catch (final Exception e) {
                logger.error("Error while executing the \"" + template.getCommandName() + "\" chat command! \n" +
                        "Exception: " + e.getLocalizedMessage());
                event.getMessage().reply("Sorry, I was unable to finish executing that command. Please try again later.").queue();
            }
        });
    }

    private void executeSlashCommand(SlashCommandTemplate template, SlashCommandInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeSlashCommand(event);
            } catch (final Exception e) {
                logger.error("Error while executing the \"" + template.getCommandName() + "\" slash command! \n" +
                        "Exception: " + e.getLocalizedMessage());
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to finish executing that command. Please try again later.").setActionRow().setEmbeds().queue();
                } else {
                    event.reply("Sorry, I was unable to finish executing that command. Please try again later.").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeAutoCompleteInteraction(AutoCompleteInterface template, CommandAutoCompleteInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeAutocomplete(event);
            } catch (final Exception e) {
                //we don't really care if this breaks tbh... I'll just log this
                logger.error("Unable to autocomplete the \"" + event.getFullCommandName() + "\" slash command");
            }
        });
    }

    private void executeButtonInteraction(ButtonInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                buttonHandler.onEvent(event);
            } catch (final Exception e) {
                logger.error("Button interaction failed! Button ID: " + event.getId());
            }
        });
    }

    private void executeStringSelectInteraction(StringSelectInterface template, StringSelectInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeStringSelectInteraction(event);
            } catch (Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeEntitySelectInteraction(EntitySelectInterface template, EntitySelectInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeEntitySelectInteraction(event);
            } catch (Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeModalInteraction(ModalInterface template, ModalInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeModal(event);
            } catch (Exception e) {
                //this one is very important to catch. The modal will not close unless it gets handled.
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command. Please try again later").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeUserContextCommand(UserContextTemplate template, UserContextInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeUserContextCommand(event);
            } catch (final Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeMessageContextCommand(MessageContextTemplate template, MessageContextInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeMessageContextCommand(event);
            } catch (final Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }

    /**
     * @return a copy of all of the chat command objects
     */
    public List<ChatCommandTemplate> getChatCommandSet() {
        return List.copyOf(chatCommandSet);
    }

    /**
     * @return a copy of all of the slash command objects
     */
    public List<SlashCommandTemplate> getSlashCommandSet() {
        return List.copyOf(slashCommandSet);
    }

    /**
     * @return a copy of all of the autocomplete command objects
     */
    public List<UserContextTemplate> getUserContextSet() {
        return List.copyOf(userContextCommandSet);
    }

    /**
     * @return a copy of all of the autocomplete command objects
     */
    public List<MessageContextTemplate> getMessageContextSet() {
        return List.copyOf(messageContextCommandSet);
    }

}
