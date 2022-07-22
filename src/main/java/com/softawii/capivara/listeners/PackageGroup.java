package com.softawii.capivara.listeners;

import com.softawii.capivara.core.EmbedManager;
import com.softawii.capivara.core.PackageManager;
import com.softawii.capivara.exceptions.*;
import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.*;
import kotlin.Pair;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@IGroup(name= "Package", description = "Group to manage roles", hidden = false)
public class PackageGroup {

    public static PackageManager packageManager;
    public static EmbedManager  embedManager;
    private static final String packageMenu       = "package-menu";
    private static final String packageUniqueMenu = "package-unique-menu";
    private static final String packageButton     = "package-button";
    private static final String roleMenu          = "role-menu";

    private static final Logger LOGGER = LogManager.getLogger(PackageGroup.class);

    @ICommand(name = "create", description = "Cria um package que terá cargos associados", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "Nome do package que será criado", required = true, type= OptionType.STRING)
    @IArgument(name="unique", description = "Se só poderá escolher um dos cargos do package", required = false, type= OptionType.BOOLEAN)
    @IArgument(name="description", description = "Descrição do package", required = false, type= OptionType.STRING)
    @IArgument(name="emoji", description = "Emoji associado ao package", required = false, type= OptionType.STRING)
    public static void create(SlashCommandInteractionEvent event) {
        String name = event.getOption("name").getAsString();

        if(name.contains(":")) {
            LOGGER.debug(String.format("create: Package Name contains :, just ignore it (%s)", name));
            event.replyEmbeds(Utils.nameContainsColon("O nome do package")).setEphemeral(true).queue();
            return;
        }

        boolean unique  = event.getOption("unique") != null && event.getOption("unique").getAsBoolean();
        Long guildId = event.getGuild().getIdLong();
        String description = event.getOption("description") != null ? event.getOption("description").getAsString() : "";
        String emojiString = event.getOption("emoji") != null ? event.getOption("emoji").getAsString() : "";
        boolean isUnicode = false;

        try {
            Pair<String, Boolean> emoji = Utils.getEmoji(emojiString);
            emojiString = emoji.getFirst();
            isUnicode = emoji.getSecond();
        } catch (MultipleEmojiMessageException e) {
            LOGGER.debug("create: Package contains Multiple Emojis");
            MessageEmbed embed = Utils.simpleEmbed("Quantos Emojis você quer amigão??",
                                                        "Você só pode usar um por vez, vamo maneirar ai...", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        try {
            packageManager.create(guildId, name, unique, description, emojiString, isUnicode);
            LOGGER.info(String.format("create: Package %s created", name));
            MessageEmbed embed = Utils.simpleEmbed("Pacote criado e pronto pra usar!",
                                                    String.format("Tudo pronto pra você adicionar vários cargos no pacote '%s', só usar os comandos de /package role ...", name),
                                                    Color.GREEN,
                                                    new MessageEmbed.Field("Características", "O pacote é único? " + (unique ? "Sim" : "Não") + "\n" + "Descrição: " + description + "\n" + "Emoji: " + emojiString, false));

            event.replyEmbeds(embed).queue();
        } catch (PackageAlreadyExistsException e) {
            LOGGER.info(String.format("create: Package %s already exists", name));
            MessageEmbed embed = Utils.simpleEmbed("Pacote já existe!", "Ta querendo criar de novo por que??? Ta doido?", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
        }
    }

    @ICommand(name = "edit", description = "Atualizar o package", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "Nome do package", required = true, type= OptionType.STRING, hasAutoComplete = true)
    @IArgument(name="unique", description = "Se só poderá escolher um dos cargos do package", required = false, type= OptionType.BOOLEAN)
    @IArgument(name="description", description = "Descrição do package", required = false, type= OptionType.STRING)
    @IArgument(name="emoji", description = "Emoji do package", required = false, type= OptionType.STRING)
    public static void edit(SlashCommandInteractionEvent event) {
        String name    = event.getOption("name").getAsString();
        Long guildId = event.getGuild().getIdLong();

        Boolean unique  = event.getOption("unique") != null ? event.getOption("unique").getAsBoolean() : null;
        String description = event.getOption("description") != null ? event.getOption("description").getAsString() : null;
        String emojiString = event.getOption("emoji") != null ? event.getOption("emoji").getAsString() : null;
        boolean isUnicode = false;

        if(emojiString != null) {
            try {
                Pair<String, Boolean> emoji = Utils.getEmoji(emojiString);
                emojiString = emoji.getFirst();
                isUnicode = emoji.getSecond();
            } catch (MultipleEmojiMessageException e) {
                LOGGER.debug("edit: Package contains Multiple Emojis");
                MessageEmbed embed = Utils.simpleEmbed("Quantos Emojis você quer amigão??", "Você só pode usar um por vez, vamo maneirar ai...", Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }
        }

        try {
            packageManager.update(guildId, name, unique, description, emojiString, isUnicode);
            LOGGER.info(String.format("edit: Package %s updated", name));

            StringBuilder sb = new StringBuilder();
            if (unique != null)  sb.append("O pacote é único? ").append(unique ? "Sim" : "Não").append("\n");
            if (description != null) sb.append("Descrição: ").append(description).append("\n");
            if (emojiString != null) sb.append("Emoji: ").append(emojiString).append("\n");

            MessageEmbed embed = Utils.simpleEmbed("Pacote atualizado, ta como se tivesse criado de novo",
                    "Só usar os comandos de /package role ...",
                    Color.GREEN,
                    new MessageEmbed.Field("O que mudou???", sb.toString(), false));
            event.replyEmbeds(embed).queue();
        } catch (PackageDoesNotExistException e) {
            LOGGER.info(String.format("edit: Package %s does not exist", name));
            MessageEmbed embed = Utils.simpleEmbed("Pacote não existe", "Você pode criar um novo pacote com /package create", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
        }
    }

    @ICommand(name = "destroy", description = "Destroi um package", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "O nome do package que será destruído", required = true, type= OptionType.STRING, hasAutoComplete = true)
    public static void destroy(SlashCommandInteractionEvent event) {
        Long guildId = event.getGuild().getIdLong();
        String name = event.getOption("name").getAsString();

        try {
            packageManager.destroy(guildId, name);
            LOGGER.info(String.format("destroy: Package %s destroyed", name));
            MessageEmbed embed = Utils.simpleEmbed(String.format("Pacote %s destruído", name), "Acabamos com esse ai, mas sempre que quiser criar um novo você já sabe!", Color.GREEN);
            event.replyEmbeds(embed).queue();
        } catch (PackageDoesNotExistException e) {
            LOGGER.debug(String.format("destroy: Package %s does not exist", name));
            MessageEmbed embed = Utils.simpleEmbed("Pacote não existe", "Ta doido?? Apagando pacote que não existe", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
        }
    }

    @ISubGroup(name = "role", description = "role subgroup")
    public static class RoleGroup {

        @ICommand(name = "add", description = "Adicionar um cargo em um package", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "package", description = "O package que o cargo será adicionado", required = true, type = OptionType.STRING, hasAutoComplete = true)
        @IArgument(name = "role", description = "Cargo que será adicionado", required = true, type = OptionType.ROLE)
        @IArgument(name = "name", description = "Nome para associar ao cargo", required = false, type = OptionType.STRING)
        @IArgument(name = "description", description = "A descrição que será associada ao cargo", required = false, type = OptionType.STRING)
        @IArgument(name = "emoji", description = "O emoji que será associado ao cargo", required = false, type = OptionType.STRING)
        public static void add(SlashCommandInteractionEvent event) {
            Long guildId = event.getGuild().getIdLong();
            String packageName = event.getOption("package").getAsString();

            if(packageName.contains(":")) {
                LOGGER.debug(String.format("add: Package Name contains :, just ignore it (%s)", packageName));
                event.replyEmbeds(Utils.nameContainsColon("O nome do package")).setEphemeral(true).queue();
                return;
            }

            Role role = event.getOption("role").getAsRole();
            String name = event.getOption("name") != null ? event.getOption("name").getAsString() : role.getName();

            if(name.contains(":")) {
                LOGGER.debug(String.format("add: Role Name contains :, just ignore it (%s)", name));
                event.replyEmbeds(Utils.nameContainsColon("O nome do package")).setEphemeral(true).queue();
                return;
            }

            String description = event.getOption("description") != null ? event.getOption("description").getAsString() : "";
            String emojiString = event.getOption("emoji") != null ? event.getOption("emoji").getAsString() : "";
            boolean isUnicode = false;

            try {
                Pair<String, Boolean> emoji = Utils.getEmoji(emojiString);
                emojiString = emoji.getFirst();
                isUnicode = emoji.getSecond();
            } catch (MultipleEmojiMessageException e) {
                LOGGER.debug("add: MultipleEmojiMessageException");
                event.replyEmbeds(Utils.multipleEmoji()).setEphemeral(true).queue();
                return;
            }

            try {
                packageManager.addRole(guildId, packageName, role, name, description, emojiString, isUnicode);
                LOGGER.info(String.format("add: Role %s added to package %s", role.getName(), packageName));
                MessageEmbed embed = Utils.simpleEmbed("Cargo adicionado!", String.format("O cargo %s foi adicionado ao pacote '%s', qualquer lista com o pacote já está atualizada",
                                                        role.getAsMention(), packageName), Color.GREEN);
                event.replyEmbeds(embed).queue();
            } catch (PackageDoesNotExistException e) {
                LOGGER.debug("add: PackageDoesNotExistException");
                MessageEmbed embed = Utils.simpleEmbed("Pacote não existe!", String.format("O pacote '%s' não existe, crie-o primeiro!", packageName), Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } catch (RoleAlreadyAddedException e) {
                LOGGER.debug("add: RoleAlreadyAddedException");
                MessageEmbed embed = Utils.simpleEmbed("Cargo já existe!", String.format("O cargo '%s' já existe no pacote '%s'!", role.getAsMention(), packageName), Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } catch (KeyAlreadyInPackageException e) {
                LOGGER.debug("add: KeyAlreadyInPackageException");
                MessageEmbed embed = Utils.simpleEmbed("Nome já existe!", String.format("O nome '%s' já existe no pacote '%s'!", name, packageName), Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
        }

        @ICommand(name = "edit", description = "Edita um cargo de um package", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "package", description = "Nome do package que será editado", required = true, type = OptionType.STRING, hasAutoComplete = true)
        @IArgument(name = "name", description = "Nome associado ao cargo", required = true, type = OptionType.STRING, hasAutoComplete = true)
        @IArgument(name = "role", description = "Cargo que será editado", required = false, type = OptionType.ROLE)
        @IArgument(name = "description", description = "Descrição associado ao cargo", required = false, type = OptionType.STRING)
        @IArgument(name = "emoji", description = "Emoji associado ao cargo", required = false, type = OptionType.STRING)
        public static void edit(SlashCommandInteractionEvent event) {
            // Primary Key
            Long guildId = event.getGuild().getIdLong();
            String packageName = event.getOption("package").getAsString();
            String name = event.getOption("name").getAsString();

            // Attributes
            Role role = event.getOption("role") != null ? event.getOption("role").getAsRole() : null;
            String description = event.getOption("description") != null ? event.getOption("description").getAsString() : null;
            String emojiString = event.getOption("emoji") != null ? event.getOption("emoji").getAsString() : null;
            boolean isUnicode = false;

            if (emojiString != null) {
                try {
                    Pair<String, Boolean> emoji = Utils.getEmoji(emojiString);
                    emojiString = emoji.getFirst();
                    isUnicode = emoji.getSecond();
                } catch (MultipleEmojiMessageException e) {
                    LOGGER.debug("edit: MultipleEmojiMessageException");
                    MessageEmbed embed = Utils.simpleEmbed("Muitos emojis!", "Não é possível usar mais de um emoji ao mesmo tempo!", Color.RED);
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                }
            }

            try {
                packageManager.editRole(guildId, packageName, name, role, description, emojiString, isUnicode);
                LOGGER.info("edit: Role edited successfully");
                MessageEmbed embed = Utils.simpleEmbed("Cargo editado!", String.format("O cargo '%s' foi editado com sucesso no pacote '%s'!", name, packageName), Color.GREEN);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } catch (PackageDoesNotExistException e) {
                LOGGER.debug("edit: PackageDoesNotExistException");
                MessageEmbed embed = Utils.simpleEmbed("Pacote não existe!", String.format("O pacote '%s' não existe!", packageName), Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } catch (RoleAlreadyAddedException e) {
                LOGGER.debug("edit: RoleAlreadyAddedException");
                MessageEmbed embed = Utils.simpleEmbed("Cargo já existe!", String.format("O cargo '%s' já existe no pacote '%s'!", name, packageName), Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } catch (RoleDoesNotExistException e) {
                LOGGER.debug("edit: RoleDoesNotExistException");
                MessageEmbed embed = Utils.simpleEmbed("Cargo não existe!", String.format("O cargo '%s' não existe no pacote '%s'!", name, packageName), Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
        }

        @ICommand(name = "remove", description = "Remove um cargo de um package", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "package", description = "Package que terá o cargo removido", required = true, type = OptionType.STRING, hasAutoComplete = true)
        @IArgument(name = "name", description = "Nome do cargo associado ao package", required = true, type = OptionType.STRING, hasAutoComplete = true)
        public static void remove(SlashCommandInteractionEvent event) {
            Long guildId = event.getGuild().getIdLong();
            String packageName = event.getOption("package").getAsString();
            String roleName = event.getOption("name").getAsString();

            try {
                packageManager.removeRole(guildId, packageName, roleName);
                LOGGER.info("remove: Role removed successfully");
                MessageEmbed embed = Utils.simpleEmbed("Cargo removido!", String.format("O cargo '%s' foi removido com sucesso do pacote '%s'!", roleName, packageName), Color.GREEN);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } catch (RoleDoesNotExistException | RoleNotFoundException e) {
                LOGGER.debug("remove: RoleDoesNotExistException | RoleNotFoundException");
                MessageEmbed embed = Utils.simpleEmbed("Cargo não existe!", String.format("O cargo '%s' não existe no pacote '%s'!", roleName, packageName), Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } catch (PackageDoesNotExistException e) {
                LOGGER.debug("remove: PackageDoesNotExistException");
                MessageEmbed embed = Utils.simpleEmbed("Pacote não existe!", String.format("O pacote '%s' não existe!", packageName), Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
        }

    }
    @ICommand(name = "list", description = "Lista todos os packages")
    public static void list(SlashCommandInteractionEvent event) {
        boolean showError =  event.getMember().hasPermission(Permission.ADMINISTRATOR);

        Long guildId = event.getGuild().getIdLong();
        List<Role> roles = event.getGuild().getRoles();
        MessageEmbed guildPackages = packageManager.getGuildPackages(guildId, roles, showError);
        event.replyEmbeds(guildPackages).queue();
    }

    @ICommand(name = "message", description = "Gera uma mensagem com botões associados aos packages")
    @IArgument(name="target", description = "O canal que você quer enviar a mensagem", type = OptionType.CHANNEL, required = true)
    @IArgument(name="button-text", description = "Texto do botão", required = true, type= OptionType.STRING)
    @IArgument(name="type", description = "Um ou mais packages", required = true, type= OptionType.STRING,
            choices={@IArgument.IChoice(key="Packages", value="packages"), @IArgument.IChoice(key="Unique", value="Unique")})
    @IArgument(name="message", description = "Mensagem bonita pra ficar fora do embed!", required = false, type= OptionType.STRING)
    @IRange(value=@IArgument(name="package", description = "O package", required = false, type= OptionType.STRING, hasAutoComplete = true), min = 0, max = 20)
    public static void message(SlashCommandInteractionEvent event) {
        System.out.println("message");
        Long guildId          = event.getGuild().getIdLong();
        String message        = event.getOption("message") != null ? event.getOption("message").getAsString() : null;
        String buttonText     = event.getOption("button-text").getAsString();
        List<String> packages = new ArrayList<>();

        for(int i = 0; i <= 20; i++) {
            String packageName = event.getOption("package" + i) != null ? event.getOption("package" + i).getAsString() : null;
            if(packageName != null) packages.add(packageName);
        }

        String type = event.getOption("type").getAsString();

        // Button to Reply
        Button button;

        // Get Multiple or All Packages
        if(type.equals("packages")) {
            if(!packages.isEmpty() && !packageManager.checkIfAllPackagesExist(guildId, packages)) {
                event.reply("Um ou mais packages não existem").queue();
                return;
            }
            String buttonId = packageButton;
            if(!packages.isEmpty()) {
                String packagesString = String.join(":", packages);
                buttonId += ":" + packagesString;
            }

            button = Button.success(buttonId, buttonText);
        } else {
            if(packages.size() != 1) {
                event.reply("Você precisa especificar exatamente **UM** package").queue();
                return;
            }

            if(!packageManager.checkIfPackageExists(guildId, packages.get(0))) {
                event.reply("O package não existe").queue();
                return;
            }

            // ECHO PART
            String packageName = packages.get(0);
            String buttonId = packageUniqueMenu + ":" + packageName;
            button = Button.success(buttonId, buttonText);
        }

        // Generating Embed Model
        Map.Entry<String, EmbedManager.EmbedHandler> init = embedManager.init();
        init.getValue().setMessage(message);
        init.getValue().setTarget(event.getOption("target").getAsChannel().asGuildMessageChannel());
        init.getValue().setActiveRows(Collections.singletonList(ActionRow.of(button)));

        if(message != null) {
            event.reply(message)
                    .addEmbeds(init.getValue().build())
                    .addActionRows(EchoGroup.embedEditor(init.getKey()))
                    .setEphemeral(true)
                    .queue();
        } else {
            event.replyEmbeds(init.getValue().build())
                    .addActionRows(EchoGroup.embedEditor(init.getKey()))
                    .setEphemeral(true)
                    .queue();
        }
    }

    @IButton(id=packageButton)
    public static void packageGetter(ButtonInteractionEvent event) {
        System.out.println("packageGetter " + event.getComponentId());

        Long guildId = event.getGuild().getIdLong();

        String[] packages = event.getComponentId().split(":");
        packages = Arrays.copyOfRange(packages, 1, packages.length);

        List<RichCustomEmoji> emotes = event.getGuild().getEmojis();

        SelectMenu menu = packageManager.getGuildPackagesMenu(guildId, Arrays.stream(packages).toList(), packageMenu, emotes);

        event.reply("Selecione um package").addActionRow(menu).setEphemeral(true).queue();
    }

    @IMenu(id=packageMenu)
    public static void package_selector(SelectMenuInteractionEvent event) {
        System.out.println("package_selector");

        // The selected option
        Optional<SelectOption> option = event.getInteraction().getSelectedOptions().stream().findFirst();

        option.ifPresentOrElse(opt -> {
                String _package  = opt.getValue();
                String customId = roleMenu + ":" + _package;
                Long guildId   = event.getGuild().getIdLong();

                List<Long> roleIds = event.getGuild().getRoles().stream().map(Role::getIdLong).collect(Collectors.toList());
                SelectMenu menu = null;
                try {
                    List<RichCustomEmoji> emotes = event.getGuild().getEmojis();
                    menu = packageManager.getGuildPackageRoleMenu(guildId, _package, customId, event.getMember(), roleIds, emotes);
                } catch (PackageDoesNotExistException e) {
                    event.editMessage("O package não existe").setActionRow().queue();
                    return;
                }

                event.reply("Selecione um cargo").addActionRow(menu).setEphemeral(true).queue();

            }, () -> event.editMessage("Nenhum package foi selecionado").queue()
        );
    }

    @IButton(id=packageUniqueMenu)
    public static void packageUniqueMenu(ButtonInteractionEvent event) {
        System.out.println("packageUniqueMenu " + event.getComponentId());

        Long guildId = event.getGuild().getIdLong();
        String _package = event.getComponentId().split(":")[1];
        String customId = roleMenu + ":" + _package;

        List<Long> roleIds = event.getGuild().getRoles().stream().map(Role::getIdLong).collect(Collectors.toList());
        SelectMenu menu = null;
        try {
            List<RichCustomEmoji> emotes = event.getGuild().getEmojis();
            menu = packageManager.getGuildPackageRoleMenu(guildId, _package, customId, event.getMember(), roleIds, emotes);
        } catch (PackageDoesNotExistException e) {
            event.editMessage("O package não existe").setActionRow().queue();
            return;
        }

        event.reply("Selecione um cargo").addActionRow(menu).setEphemeral(true).queue();
    }

    @IMenu(id=roleMenu)
    public static void getting_role(SelectMenuInteractionEvent event) {
        System.out.println(event.getInteraction().getId());

        List<SelectOption> selectOptions = event.getSelectedOptions();

        event.getInteraction().getSelectMenu().getOptions().forEach(opt -> {
            // roleId
            Long roleId = Long.valueOf(opt.getValue());

            Role role = event.getGuild().getRoleById(roleId);

            if(role == null) {
                event.editMessage(event.getMessage().getContentDisplay() + "\nCargo " + opt.getLabel() +  " : " +  opt.getValue() + " não existe").queue();
                return;
            }

            try {
                if (selectOptions.contains(opt)) {
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                } else {
                    event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                }
            } catch(HierarchyException e) {
                event.getChannel().sendMessage("Não posso atribuir o cargo " + opt.getValue() + " para você").queue();
            }
        });

        event.reply("Seus cargos foram atualizados").setEphemeral(true).queue();
    }

    public static class AutoCompleter extends ListenerAdapter {

        @Override
        public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
            // This is where you can add your own auto-completion logic
            // This autocompleter is just for the PackageGroup class not for PackageGroup.RoleGroup!
            if(event.getGuild() == null) {
                return;
            }

            String eventPath = event.getCommandPath();

            System.out.println(String.format("AutoCompleter: %s : %s", eventPath, event.getFocusedOption().getName()));

            if(eventPath.equals("package/edit") || eventPath.equals("package/destroy")) {
                String focusedKey   = event.getFocusedOption().getName();
                String focusedValue = event.getFocusedOption().getValue();
                // AutoComplete to name (Package Name)
                if(focusedKey.equals("name")) {
                    event.replyChoices(packageManager.autoCompletePackageName(event.getGuild().getIdLong(), focusedValue)).queue();
                }
            } else if(eventPath.equals("package/message")) {
                String focusedKey   = event.getFocusedOption().getName();
                String focusedValue = event.getFocusedOption().getValue();
                // AutoComplete to name (Package Name)
                if(focusedKey.startsWith("package")) {
                    event.replyChoices(packageManager.autoCompletePackageName(event.getGuild().getIdLong(), focusedValue)).queue();
                }
            } else if(eventPath.equals("package/role/add")) {
                String focusedKey   = event.getFocusedOption().getName();
                String focusedValue = event.getFocusedOption().getValue();
                // AutoComplete to package (Package Name)
                if(focusedKey.equals("package")) {
                    event.replyChoices(packageManager.autoCompletePackageName(event.getGuild().getIdLong(), focusedValue)).queue();
                }
            } else if(eventPath.equals("package/role/remove") || eventPath.equals("package/role/edit")) {
                String focusedKey   = event.getFocusedOption().getName();
                String focusedValue = event.getFocusedOption().getValue();
                // AutoComplete to package (Package Name)
                if(focusedKey.equals("package")) {
                    event.replyChoices(packageManager.autoCompletePackageName(event.getGuild().getIdLong(), focusedValue)).queue();
                }
                // AutoComplete to name (Role Name)
                else if(focusedKey.equals("name")) {
                    String packageName = event.getOption("package") != null ? event.getOption("package").getAsString() : "";
                    event.replyChoices(packageManager.autoCompleteRolePackageName(event.getGuild().getIdLong(), packageName, focusedValue)).queue();
                }
            }
        }
    }
}