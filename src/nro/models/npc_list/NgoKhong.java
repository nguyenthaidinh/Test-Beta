package nro.models.npc_list;

import nro.models.consts.ConstMap;
import nro.models.consts.ConstNpc;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;

/** NPC Ngộ Không tại map thứ ba của Ngũ Hành Sơn. */
public final class NgoKhong extends Npc {

    private static final int TRAINING_ENTRY_X = 300;
    private static final int TRAINING_ENTRY_Y = 408;

    public NgoKhong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (this.mapId != ConstMap.NGU_HANH_SON) {
            super.openBaseMenu(player);
            return;
        }
        createOtherMenu(player, ConstNpc.BASE_MENU,
                "Ngươi muốn vào phòng luyện tập sao?",
                "Luyện tập", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)
                || this.mapId != ConstMap.NGU_HANH_SON
                || !player.idMark.isBaseMenu()
                || select != 0) {
            return;
        }

        Zone zoneJoin = getAvailableTrainingZone(player);
        if (zoneJoin == null) {
            Service.gI().sendThongBao(player,
                    "Phòng luyện tập chưa được mở hoặc các khu đều đã đầy.");
            return;
        }
        ChangeMapService.gI().changeMap(player, zoneJoin,
                TRAINING_ENTRY_X, TRAINING_ENTRY_Y);
    }

    private Zone getAvailableTrainingZone(Player player) {
        Map map = MapService.gI().getMapById(ConstMap.PHONG_LUYEN_TAP_NGU_HANH_SON);
        if (map == null || map.zones == null || map.zones.isEmpty()) {
            return null;
        }

        int currentZoneId = player.zone == null ? -1 : player.zone.zoneId;
        if (currentZoneId >= 0 && currentZoneId < map.zones.size()) {
            Zone sameZone = map.zones.get(currentZoneId);
            if (sameZone != null && !sameZone.isFullPlayer()) {
                return sameZone;
            }
        }
        for (Zone zone : map.zones) {
            if (zone != null && !zone.isFullPlayer()) {
                return zone;
            }
        }
        return null;
    }
}
