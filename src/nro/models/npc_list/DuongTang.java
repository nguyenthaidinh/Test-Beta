package nro.models.npc_list;

import nro.models.consts.ConstMap;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstTask;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;

/** NPC Duong Tang dung tai Lang Aru de dua nguoi choi vao Ngu Hanh Son. */
public final class DuongTang extends Npc {

    private static final int ENTRY_X = 60;
    private static final int ENTRY_Y = 360;

    public DuongTang(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (this.mapId != ConstMap.LANG_ARU) {
            super.openBaseMenu(player);
            return;
        }
        createOtherMenu(player, ConstNpc.BASE_MENU,
                "Ta c\u00f3 th\u1ec3 \u0111\u01b0a ng\u01b0\u01a1i \u0111\u1ebfn Ng\u0169 H\u00e0nh S\u01a1n. "
                + "Ch\u1ec9 nh\u1eefng ng\u01b0\u1eddi \u0111\u00e3 ho\u00e0n th\u00e0nh nhi\u1ec7m v\u1ee5 Ma B\u01b0 m\u1edbi c\u00f3 th\u1ec3 b\u01b0\u1edbc v\u00e0o n\u01a1i n\u00e0y.",
                "V\u00e0o\nNg\u0169 H\u00e0nh S\u01a1n", "T\u1eeb ch\u1ed1i");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)
                || this.mapId != ConstMap.LANG_ARU
                || !player.idMark.isBaseMenu()
                || select != 0) {
            return;
        }
        if (!canEnterNguHanhSon(player)) {
            Service.gI().sendThongBao(player,
                    "B\u1ea1n c\u1ea7n ho\u00e0n th\u00e0nh to\u00e0n b\u1ed9 nhi\u1ec7m v\u1ee5 Ma B\u01b0 tr\u01b0\u1edbc khi v\u00e0o Ng\u0169 H\u00e0nh S\u01a1n.");
            return;
        }

        Zone zoneJoin = getAvailableNguHanhSonZone();
        if (zoneJoin == null) {
            Service.gI().sendThongBao(player,
                    "Ng\u0169 H\u00e0nh S\u01a1n ch\u01b0a \u0111\u01b0\u1ee3c m\u1edf ho\u1eb7c c\u00e1c khu \u0111\u1ec1u \u0111\u00e3 \u0111\u1ea7y.");
            return;
        }
        player.allowEnterNguHanhSon = true;
        try {
            ChangeMapService.gI().changeMap(player, zoneJoin, ENTRY_X, ENTRY_Y);
        } finally {
            player.allowEnterNguHanhSon = false;
        }
    }

    private boolean canEnterNguHanhSon(Player player) {
        return player.isAdmin()
                || TaskService.gI().getIdTask(player) >= ConstTask.TASK_29_0;
    }

    private Zone getAvailableNguHanhSonZone() {
        Map map = MapService.gI().getMapById(ConstMap.NGU_HANH_SON_123);
        if (map == null || map.zones == null) {
            return null;
        }
        for (Zone zone : map.zones) {
            if (zone != null && !zone.isFullPlayer()) {
                return zone;
            }
        }
        return null;
    }
}
