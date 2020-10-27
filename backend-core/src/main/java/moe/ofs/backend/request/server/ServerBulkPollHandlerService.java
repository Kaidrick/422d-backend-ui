package moe.ofs.backend.request.server;

import com.google.common.collect.Sets;
import moe.ofs.backend.domain.Level;
import moe.ofs.backend.domain.LuaState;
import moe.ofs.backend.domain.PlayerInfo;
import moe.ofs.backend.request.*;
import moe.ofs.backend.services.PlayerInfoService;
import moe.ofs.backend.services.UpdatableService;
import moe.ofs.backend.util.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("playerInfoBulk")
public final class ServerBulkPollHandlerService implements PollHandlerService {

    private final RequestHandler requestHandler;

    protected List<PlayerInfo> list;

    protected int flipCount;

    protected int flipThreshold = 20;

    protected boolean requestCompleted;

    protected final PlayerInfoService service;

    protected Level level;

    public void setFlipThreshold(int flipThreshold) {
        this.flipThreshold = flipThreshold;
    }

    @Autowired
    public ServerBulkPollHandlerService(RequestHandler requestHandler, PlayerInfoService service) {
        this.requestHandler = requestHandler;
        this.service = service;

        this.level = PlayerInfo.class.getAnnotation(LuaState.class).value();

        list = new ArrayList<>();

        setFlipThreshold(5);
    }

    @Override
    public void poll() throws IOException {

        flipCount++;

        BaseRequest request;
        if (flipCount >= flipThreshold && requestCompleted) {

            flipCount = 0;

            request = new PollRequest(level);

            requestCompleted = false;

        } else {

            request = new FillerRequest(level);

        }

        Connection connection = requestHandler.getConnections().get(level);
        String s = connection.transmitAndReceive(ConnectionManager.fastPack(request));

//        System.out.println(s);
//        String s = ConnectionManager.fastPackThenSendAndGet(request);

        List<JsonRpcResponse<List<PlayerInfo>>> jsonRpcResponseList = ConnectionManager.parseJsonResponse(s, PlayerInfo.class);
        List<PlayerInfo> objectList = ConnectionManager.flattenResponse(jsonRpcResponseList);
        list.addAll(objectList);

        jsonRpcResponseList.stream()
                .findAny().ifPresent(r -> {
                    if(list.size() == r.getResult().getTotal()) {

                        Set<PlayerInfo> record = service.findAll().parallelStream().collect(Collectors.toSet());
                        Set<PlayerInfo> update = new HashSet<>(list);

                        Sets.SetView<PlayerInfo> intersection = Sets.intersection(record, update);
                        Sets.SetView<PlayerInfo> obsoletePlayers = Sets.symmetricDifference(intersection, record);
                        Sets.SetView<PlayerInfo> newPlayers = Sets.symmetricDifference(intersection, update);

                        // intersection contains old data, need updated info instead
//                        intersection.forEach(o -> service.update(update.stream()
//                                .filter(playerInfo -> playerInfo.equals(o)).findFirst()
//                                .orElseThrow(() -> new RuntimeException("Unable to find target record PlayerInfo"))));

//        intersection.forEach(this::processUpdateData);
                        obsoletePlayers.forEach(service::remove);
                        newPlayers.forEach(service::add);
                        intersection.forEach(playerInfo -> {
                           PlayerInfo previous = service.update(
                                   update.stream()
                                           .filter(f -> f.equals(playerInfo)).findFirst()
                                           .orElseThrow(() -> new RuntimeException("Unable to find target record PlayerInfo"))
                           );

                           if (service.detectSlotChange(previous, playerInfo)) {  // returns a boolean value
                               System.out.println("Player slot change -> " + previous + ", " + playerInfo);
                           }
                        });

                        requestCompleted = true;
                        list.clear();
                    }
                });
    }

    @Override
    public void init() {
        requestCompleted = true;
        list.clear();
    }

}
