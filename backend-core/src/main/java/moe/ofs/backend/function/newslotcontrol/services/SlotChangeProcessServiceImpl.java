package moe.ofs.backend.function.newslotcontrol.services;

import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import moe.ofs.backend.domain.ChatCommand;
import moe.ofs.backend.function.newslotcontrol.model.SlotChangeData;
import moe.ofs.backend.handlers.MissionStartObservable;
import moe.ofs.backend.request.RequestToServer;
import moe.ofs.backend.request.server.ServerDataRequest;
import moe.ofs.backend.request.services.RequestTransmissionService;
import moe.ofs.backend.util.LuaScripts;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SlotChangeProcessServiceImpl implements SlotChangeProcessService {
    private final RequestTransmissionService requestTransmissionService;

    public SlotChangeProcessServiceImpl(RequestTransmissionService requestTransmissionService) {
        this.requestTransmissionService = requestTransmissionService;
    }

    @PostConstruct
    public void init() {
        MissionStartObservable missionStartObservable = s -> {
            requestTransmissionService.send(
                    new ServerDataRequest(RequestToServer.State.DEBUG,
                            LuaScripts.loadAndPrepare("slotchange/new/player_slot_record_hook.lua",
                                    getClass().getName())));

            log.info("Setting up Slot Change Process Service");
        };
        missionStartObservable.register();
    }

    @Override
    public List<SlotChangeData> poll() {
        Type type = TypeToken.getParameterized(ArrayList.class, SlotChangeData.class).getType();
        return ((ServerDataRequest) requestTransmissionService
                .send(new ServerDataRequest(RequestToServer.State.DEBUG,
                        LuaScripts.load("slotchange/new/fetch_player_slot_records.lua"))))
                .getAs(type);
    }
}
