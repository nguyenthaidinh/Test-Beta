package nro.models.npc_list;

import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services_dungeon.HeroWarService;

/**
 *
 * @author By Mr Blue
 * 
 */

public class DaiThienSu extends Npc {

    private static final int MENU_HERO_WAR_BASE = 64144;
    private static final int MENU_HERO_WAR_POWER = 64145;

    public DaiThienSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (HeroWarService.gI().isHeroWarMap(this.mapId)) {
            createOtherMenu(player, MENU_HERO_WAR_BASE,
                    "Đại chiến Anh Hùng đang diễn ra.",
                    "Wish", "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.getIndexMenu() == MENU_HERO_WAR_BASE) {
            if (select == 0) {
                createOtherMenu(player, MENU_HERO_WAR_POWER,
                        HeroWarService.gI().getFriendPowerMenuText(player),
                        HeroWarService.gI().getFriendPowerMenuOptions(player));
            }
            return;
        }
        if (player.idMark.getIndexMenu() == MENU_HERO_WAR_POWER) {
            HeroWarService.gI().selectFriendPower(player, select);
        }
    }
}
