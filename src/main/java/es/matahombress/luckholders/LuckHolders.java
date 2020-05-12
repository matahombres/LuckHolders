
package es.matahombress.luckholders;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.rojo8399.placeholderapi.ExpansionBuilder;
import me.rojo8399.placeholderapi.NoValueException;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import me.rojo8399.placeholderapi.impl.PlaceholderAPIPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.metastacking.DuplicateRemovalFunction;
import net.luckperms.api.metastacking.MetaStackDefinition;
import net.luckperms.api.metastacking.MetaStackElement;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.track.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.text.Text;

@Plugin(name=LuckHolders.PLUGIN_NAME, id=LuckHolders.PLUGIN_ID,version=LuckHolders.VERSION,description="Placeholder for luckperms",dependencies={
    @Dependency(id="placeholderapi"),
    @Dependency(id="luckperms")
})
public class LuckHolders {
    
    public static final String PLUGIN_ID = "luckholders2";
    public static final String PLUGIN_NAME = "LuckHolders2";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_NAME);
    public static LuckPerms luckperms;
    public static LuckHolders instance;
    private LuckPerms luck;
    PlaceholderService s;
    
    @Listener
    public void onStart(GameStartingServerEvent event) {
        Optional<ProviderRegistration<LuckPerms>> provider = Sponge.getServiceManager().getRegistration(LuckPerms.class);
        if (!provider.isPresent()) {
            LuckHolders.LOGGER.error("No found luckperms");
            return;
        }
        luckperms = provider.get().getProvider();
        luck=luckperms;
        Optional<ProviderRegistration<PlaceholderService>> providerplaceholder = Sponge.getServiceManager().getRegistration(PlaceholderService.class);
        if (!providerplaceholder.isPresent()) {
            LuckHolders.LOGGER.error("No found Placeholder");
            return;
        }
        LOGGER.info("Found Luckperms and Placeholder");
        LOGGER.info("Plugin make by matahombress");
        PlaceholderService service=Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
        System.out.println(this);
        Stream<? extends ExpansionBuilder<?, ?, ?, ? extends ExpansionBuilder<?,?,?,?>>> s=service.loadAll(this, this).stream();
        System.out.println(s);
        s.map(builder -> {
            switch (builder.getId()) { // TODO update?
                case "luckperms":
                    return builder.tokens("prefix",
                            "suffix",
                            "meta_[meta key]",
                            "prefix_element_[element]",
                            "suffix_element_[element]",
                            "context_[context]",
                            "groups",
                            "primary_group",
                            "has_permission_[permission]",
                            "inherits_permission_[permission]",
                            "check_permission_[permission]",
                            "in_group_[group]",
                            "inherits_group_[group]",
                            "on_track_[track]",
                            "has_groups_on_track_[track]",
                            "highest_group_by_weight",
                            "lowest_group_by_weight",
                            "first_group_on_tracks_[tracks]",
                            "last_group_on_tracks_[tracks]",
                            "expiry_time_[permission]",
                            "inherited_expiry_time_[permission]",
                            "group_expiry_time_[group name]"
                    ).description("LuckPerms Placeholders.");
            }
            return builder;
        }).map(builder -> builder.author("matahombress").version(LuckHolders.VERSION)).forEach(builder -> {
            try {
                builder.buildAndRegister();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
    @Placeholder(id = "luckperms")
    public Object luckperms(@Source Player player, @Token String token) throws NoValueException {
        luck=LuckHolders.luckperms;
        User user = luck.getUserManager().getUser(player.getUniqueId());
        if(user==null){
            return "Player invalid";
        }
        CachedDataManager userData=user.getCachedData();
        QueryOptions queryOptions = luck.getContextManager().getQueryOptions(player);
        if(token.startsWith("prefix_element")){
            if(token.split("prefix_element_").length==1){
                throw new NoValueException("Missing value");
            }
            String element=token.split("prefix_element_")[1];
            MetaStackElement stackElement = luck.getMetaStackFactory().fromString(element).orElse(null);
            if(stackElement== null){
                return "ERROR: Invalid element!";
            }
            
            MetaStackDefinition stackDefinition = luck.getMetaStackFactory().createDefinition(ImmutableList.of(stackElement), DuplicateRemovalFunction.RETAIN_ALL, "", "", "");
            QueryOptions newOptions = queryOptions.toBuilder()
                    .option(MetaStackDefinition.PREFIX_STACK_KEY, stackDefinition)
                    .option(MetaStackDefinition.SUFFIX_STACK_KEY, stackDefinition)
                    .build();
            return Strings.nullToEmpty(userData.getMetaData(newOptions).getPrefix());
        }else if(token.startsWith("suffix_element")){
            if(token.split("suffix_element_").length==1){
                throw new NoValueException("Missing value");
            }
            String element=token.split("suffix_element_")[1];
            MetaStackElement stackElement = luck.getMetaStackFactory().fromString(element).orElse(null);
            if (stackElement == null) {
                return "ERROR: Invalid element!";
            }

            MetaStackDefinition stackDefinition = luck.getMetaStackFactory().createDefinition(ImmutableList.of(stackElement), DuplicateRemovalFunction.RETAIN_ALL, "", "", "");
            QueryOptions newOptions = queryOptions.toBuilder()
                    .option(MetaStackDefinition.PREFIX_STACK_KEY, stackDefinition)
                    .option(MetaStackDefinition.SUFFIX_STACK_KEY, stackDefinition)
                    .build();
            return Strings.nullToEmpty(userData.getMetaData(newOptions).getSuffix());
        }else if(token.startsWith("prefix")){
            return Strings.nullToEmpty(userData.getMetaData(luck.getContextManager().getQueryOptions(player)).getPrefix());
        }else if(token.startsWith("suffix")){
            return Strings.nullToEmpty(userData.getMetaData(luck.getContextManager().getQueryOptions(player)).getSuffix());
        }else if(token.startsWith("meta")){
            if(token.split("meta_").length==1){
                throw new NoValueException("Missing value");
            }
            String node=token.split("meta_")[1];
            List<String> values = userData.getMetaData(luck.getContextManager().getQueryOptions(player)).getMeta().getOrDefault(node, ImmutableList.of());
            return values.isEmpty() ? "" : values.iterator().next();
        }else if(token.startsWith("context")){
            if(token.split("context_").length==1){
                throw new NoValueException("Missing value");
            }
            String key=token.split("context_")[1];
            return String.join(", ", luck.getContextManager().getContext(player).getValues(key));
        }else if(token.startsWith("groups")){
            return user.getNodes()
                    .stream()
                    .filter(NodeType.INHERITANCE::matches)
                    .map(NodeType.INHERITANCE::cast)
                    .filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context()))
                    .map(InheritanceNode::getGroupName)
                    .map(this::convertGroupDisplayName)
                    .collect(Collectors.joining(", "));
        }else if(token.startsWith("primary_group")){
            return convertGroupDisplayName(user.getPrimaryGroup());
        }else if(token.startsWith("has_permission")){
            if(token.split("has_permission_").length==1){
                throw new NoValueException("Missing value");
            }
            String node=token.split("has_permission_")[1];
            return user.getNodes().stream()
                        .filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context()))
                        .anyMatch(n -> n.getKey().equals(node));
        }else if(token.startsWith("inherits_permission")){
            if(token.split("inherits_permission_").length==1){
                throw new NoValueException("Missing value");
            }
            String node=token.split("inherits_permission_")[1];
            return user.resolveInheritedNodes(queryOptions).stream()
                        .filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context()))
                        .anyMatch(n -> n.getKey().equals(node));
        }else if(token.startsWith("check_permission")){
            if(token.split("check_permission_").length==1){
                throw new NoValueException("Missing value");
            }
            String node=token.split("check_permission_")[1];
            return user.getCachedData().getPermissionData(queryOptions).checkPermission(node).asBoolean();
        }else if(token.startsWith("in_group")){
            if(token.split("in_group_").length==1){
                throw new NoValueException("Missing value");
            }
            String groupName=token.split("in_group_")[1];
            return user.getNodes().stream()
                        .filter(NodeType.INHERITANCE::matches)
                        .map(NodeType.INHERITANCE::cast)
                        .filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context()))
                        .map(InheritanceNode::getGroupName)
                        .anyMatch(s -> s.equalsIgnoreCase(groupName));
        }else if(token.startsWith("inherits_group")){
            if(token.split("inherits_group_").length==1){
                throw new NoValueException("Missing value");
            }
            String groupName=token.split("inherits_group_")[1];
            return user.getCachedData().getPermissionData(queryOptions).checkPermission("group." + groupName).asBoolean();
        }else if(token.startsWith("on_track")){
            if(token.split("on_track_").length==1){
                throw new NoValueException("Missing value");
            }
            String trackName=token.split("on_track_")[1];
            return Optional.ofNullable(luck.getTrackManager().getTrack(trackName))
                        .map(t -> t.containsGroup(user.getPrimaryGroup()))
                        .orElse(false);
        }else if(token.startsWith("has_groups_on_track")){
            if(token.split("has_groups_on_track_").length==1){
                throw new NoValueException("Missing value");
            }
            String trackName=token.split("has_groups_on_track_")[1];
            return Optional.ofNullable(luck.getTrackManager().getTrack(trackName))
                        .map(t -> user.getNodes().stream()
                                .filter(NodeType.INHERITANCE::matches)
                                .map(NodeType.INHERITANCE::cast)
                                .map(InheritanceNode::getGroupName)
                                .anyMatch(t::containsGroup)
                        )
                .orElse(false);
        }else if(token.startsWith("highest_group_by_weight")){
            return user.getNodes().stream()
                        .filter(NodeType.INHERITANCE::matches)
                        .map(NodeType.INHERITANCE::cast)
                        .filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context()))
                        .map(InheritanceNode::getGroupName)
                        .map(n -> luck.getGroupManager().getGroup(n))
                        .filter(Objects::nonNull)
                        .min((o1, o2) -> {
                            int ret = Integer.compare(o1.getWeight().orElse(0), o2.getWeight().orElse(0));
                            return ret == 1 ? 1 : -1; 
                        })
                        .map(Group::getName)
                        .map(this::convertGroupDisplayName)
                        .orElse("");
        }else if(token.startsWith("lowest_group_by_weight")){
            return user.getNodes().stream()
                        .filter(NodeType.INHERITANCE::matches)
                        .map(NodeType.INHERITANCE::cast)
                        .filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context()))
                        .map(InheritanceNode::getGroupName)
                        .map(n -> luck.getGroupManager().getGroup(n))
                        .filter(Objects::nonNull)
                        .min((o1, o2) -> {
                            int ret = Integer.compare(o1.getWeight().orElse(0), o2.getWeight().orElse(0));
                            return ret == 1 ? -1 : 1;
                        })
                        .map(Group::getName)
                        .map(this::convertGroupDisplayName)
                        .orElse("");
        }else if(token.startsWith("first_group_on_tracks")){
            if(token.split("first_group_on_tracks_").length==1){
                throw new NoValueException("Missing value");
            }
            String argument=token.split("first_group_on_tracks_")[1];
            List<String> tracks = Splitter.on(',').trimResults().splitToList(argument);
            CachedPermissionData permData = userData.getPermissionData(queryOptions);
            return tracks.stream()
                    .map(n -> luck.getTrackManager().getTrack(n))
                    .filter(Objects::nonNull)
                    .map(Track::getGroups)
                    .map(groups -> groups.stream()
                            .filter(s -> permData.checkPermission("group." + s).asBoolean())
                            .findFirst()
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .map(this::convertGroupDisplayName)
                    .orElse("");
        }else if(token.startsWith("last_group_on_tracks")){
            if(token.split("last_group_on_tracks_").length==1){
                throw new NoValueException("Missing value");
            }
            String argument=token.split("last_group_on_tracks_")[1];
            List<String> tracks = Splitter.on(',').trimResults().splitToList(argument);
            CachedPermissionData permData = userData.getPermissionData(queryOptions);
            return tracks.stream()
                    .map(n -> luck.getTrackManager().getTrack(n))
                    .filter(Objects::nonNull)
                    .map(Track::getGroups)
                    .map(Lists::reverse)
                    .map(groups -> groups.stream()
                            .filter(s -> permData.checkPermission("group." + s).asBoolean())
                            .findFirst()
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .map(this::convertGroupDisplayName)
                    .orElse("");
        }else if(token.startsWith("expiry_time")){
            if(token.split("expiry_time_").length==1){
                throw new NoValueException("Missing value");
            }
            String node=token.split("expiry_time_")[1];
            long currentTime = System.currentTimeMillis() / 1000L;
            return user.getNodes().stream()
                    .filter(Node::hasExpiry)
                    .filter(n -> !n.hasExpired())
                    .filter(n -> n.getKey().equals(node))
                    .filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context()))
                    .map(Node::getExpiry)
                    .map(Instant::getEpochSecond)
                    .findFirst()
                    .map(e -> formatTime((int) (e - currentTime)))
                    .orElse("");
        }else if(token.startsWith("inherited_expiry_time")){
            if(token.split("inherited_expiry_time_").length==1){
                throw new NoValueException("Missing value");
            }
            String node=token.split("inherited_expiry_time_")[1];
            long currentTime = System.currentTimeMillis() / 1000L;
            return user.resolveInheritedNodes(QueryOptions.nonContextual()).stream()
                    .filter(Node::hasExpiry)
                    .filter(n -> !n.hasExpired())
                    .filter(n -> n.getKey().equals(node))
                    .filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context()))
                    .map(Node::getExpiry)
                    .map(Instant::getEpochSecond)
                    .findFirst()
                    .map(e -> formatTime((int) (e - currentTime)))
                    .orElse("");
        }else if(token.startsWith("group_expiry_time")){
            if(token.split("group_expiry_time_").length==1){
                throw new NoValueException("Missing value");
            }
            String group=token.split("group_expiry_time_")[1];
            long currentTime = System.currentTimeMillis() / 1000L;
            return user.getNodes().stream()
                    .filter(Node::hasExpiry)
                    .filter(n -> !n.hasExpired())
                    .filter(NodeType.INHERITANCE::matches)
                    .map(NodeType.INHERITANCE::cast)
                    .filter(n -> n.getGroupName().equalsIgnoreCase(group))
                    .filter(n -> n.getContexts().isSatisfiedBy(queryOptions.context()))
                    .map(Node::getExpiry)
                    .map(Instant::getEpochSecond)
                    .findFirst()
                    .map(e -> formatTime((int) (e - currentTime)))
                    .orElse("");
        }else{
            throw new NoValueException("Not enough arguments");
        }
    }
    
    private String formatTime(int time) {
        return String.valueOf(time);
    }
    private String formatBoolean(boolean value) {
        return value?"true":"false";
    }
    private String convertGroupDisplayName(String groupName) {
        Group group = luck.getGroupManager().getGroup(groupName);
        if (group != null) {
            groupName = group.getFriendlyName();
        }
        return groupName;
    }
}
