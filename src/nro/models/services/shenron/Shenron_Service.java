package nro.models.services.shenron;

import nro.models.consts.ConstItem;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.map.service.NpcService;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class Shenron_Service {

    private static Shenron_Service instance;

    public static final int TYPE_ICE = 0;
    public static final int TYPE_PUMPKIN = 1;

    public static final short NGOC_RONG_1_SAO = 925;
    public static final short NGOC_RONG_2_SAO = 926;
    public static final short NGOC_RONG_3_SAO = 927;
    public static final short NGOC_RONG_4_SAO = 928;
    public static final short NGOC_RONG_5_SAO = 929;
    public static final short NGOC_RONG_6_SAO = 930;
    public static final short NGOC_RONG_7_SAO = 931;

    public static Shenron_Service gI() {
        if (instance == null) {
            instance = new Shenron_Service();
        }
        return instance;
    }

    public void openMenuSummonShenron(Player pl, int type) {
        int shenronType = getValidShenronType(type);
        pl.idMark.setShenronType(shenronType);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.SUMMON_SHENRON_EVENT, -1,
                "Ban co muon goi " + getDragonName(shenronType) + " khong ?",
                "Dong y", "Tu choi");
    }

    public void summonShenron(Player player) {
        if (player.zone.map.mapId != 0 && player.zone.map.mapId != 7 && player.zone.map.mapId != 14) {
            int shenronType = getValidShenronType(player.idMark.getShenronType());
            player.idMark.setShenronType(shenronType);
            if (checkShenronBall(player, shenronType)) {
                if (player.isShenronAppear || player.shenronEvent != null) {
                    Service.gI().sendThongBao(player, "Khong the thuc hien");
                    return;
                }

                long lastTimeShenronAppeared = getLastTimeShenronAppeared(player, shenronType);
                if (Util.canDoWithTime(lastTimeShenronAppeared, Shenron_Event.timeResummonShenron)) {
                    for (int i = getFirstBallId(shenronType); i <= getLastBallId(shenronType); i++) {
                        try {
                            InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, i), 1);
                        } catch (Exception ex) {
                        }
                    }
                    InventoryService.gI().sendItemBags(player);
                    Shenron_Event shenron = new Shenron_Event();
                    shenron.setPlayer(player);
                    Shenron_Manager.gI().add(shenron);
                    player.shenronEvent = shenron;
                    shenron.setZone(player.zone);
                    shenron.activeShenron(true, Shenron_Event.DRAGON_EVENT);
                    shenron.sendBlackGokuhesShenron();
                } else {
                    int timeLeft = (int) ((Shenron_Event.timeResummonShenron
                            - (System.currentTimeMillis() - lastTimeShenronAppeared)) / 1000);
                    Service.gI().sendThongBao(player, "Vui long doi "
                            + (timeLeft < 7200 ? timeLeft + " giay" : timeLeft / 60 + " phut") + " nua");
                }
            }
        } else {
            Service.gI().sendThongBao(player, "Khong the goi rong o day");
        }
    }

    private boolean checkShenronBall(Player pl, int type) {
        for (int i = getFirstBallId(type); i <= getLastBallId(type); i++) {
            if (!InventoryService.gI().isExistItemBag(pl, i)) {
                Item it = ItemService.gI().createNewItem((short) i);
                Service.gI().sendThongBao(pl, "Ban con thieu 1 vien " + it.template.name);
                return false;
            }
        }
        return true;
    }

    private int getFirstBallId(int type) {
        return type == TYPE_PUMPKIN ? ConstItem.BI_NGO_1_SAO : NGOC_RONG_1_SAO;
    }

    private int getLastBallId(int type) {
        return type == TYPE_PUMPKIN ? ConstItem.BI_NGO_7_SAO : NGOC_RONG_7_SAO;
    }

    private int getValidShenronType(int type) {
        return type == TYPE_PUMPKIN ? TYPE_PUMPKIN : TYPE_ICE;
    }

    private long getLastTimeShenronAppeared(Player player, int type) {
        return type == TYPE_PUMPKIN ? player.lastTimePumpkinShenronAppeared : player.lastTimeShenronAppeared;
    }

    public static String getDragonName(int type) {
        return type == TYPE_PUMPKIN ? "Rong Bi Ngo" : "Rong Bang";
    }
}
