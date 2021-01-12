package moe.ofs.backend.function.motd.services.impl;

import moe.ofs.backend.common.AbstractMapService;
import moe.ofs.backend.function.motd.model.MotdMessageSet;
import moe.ofs.backend.function.motd.services.MotdManageService;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class MotdManageServiceImpl extends AbstractMapService<MotdMessageSet> implements MotdManageService {
    private MotdMessageSet activeMotdMessageSet;

    public MotdManageServiceImpl() {
        // default to the first set or read from configuration file
//        findAll().stream()
//                .min(Comparator.comparingLong(MotdMessageSet::getLastEditTime))
//                .ifPresent(m -> activeMotdSetName = m.getName());
    }

    @Override
    public void setActiveMotdSet(String name) {
//        activeMotdSetName = name;
    }

    @Override
    public void setActiveMotdSet(MotdMessageSet motdMessageSet) {
//        activeMotdSetName = motdMessageSet.getName();
    }
}
