package nro.models.services.shenron;

import nro.models.boss.event.Halloween.HalloweenRewards;
import nro.models.network.Message;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.item.ItemTime;
import nro.models.player.Player;
import lombok.Getter;
import lombok.Setter;
import nro.models.map.Zone;
import nro.models.server.Client;
import nro.models.services.ItemService;
import nro.models.services.InventoryService;
import nro.models.services.ItemTimeService;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import nro.models.services.IntrinsicService;
import nro.models.services.GlobalSkyService;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class Shenron_Event {

    @Setter
    @Getter
    private Player player;

    @Setter
    @Getter
    private Zone zone;

    public long playerId;
    public boolean isPlayerDisconnect;
    public byte select;
    public int shenronType;
    public boolean leaveMap;

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;

    public static final byte DRAGON_EVENT = 1;

    public long lastTimeShenronWait;
    public static int timeResummonShenron = 60000;
    public static int timeShenronWait = 60000;

    public static final String SHENRONEVENT_SAY
            = "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy chọn đi:\n1) Đổi skill 3, 4 đệ tử (có thể trùng skill trước đó).\n2) Thay đổi nội tại.\n3) Cải trang siêu thần HSD 90 ngày.\n4) Cải trang Black Gohan Rose HSD 90 ngày.";

    public static final String[] SHENRON_WISHES
            = new String[]{"Điều ước 1", "Điều ước 2", "Điều ước 3", "Điều ước 4"};

    public static final String PUMPKIN_SHENRON_SAY
            = "Rong Bi Ngo se ban cho nguoi 1 dieu uoc, hay chon di:"
            + "\n1) Doi skill 3, 4 de tu."
            + "\n2) Tang 20% suc danh trong 60 phut."
            + "\n3) Tang 35% HP, KI trong 60 phut."
            + "\n4) Nhan 1 cai trang Halloween.";

    public static final String[] PUMPKIN_SHENRON_WISHES
            = new String[]{"Doi skill\n3-4 de tu", "Tang 20%\nsuc danh", "Tang 35%\nHP KI", "Cai trang\nHalloween"};

    public boolean shenronLeave;

    public void update() {
        try {
            if (!shenronLeave) {
                if (isPlayerDisconnect) {
                    Player pl = Client.gI().getPlayer(playerId);
                    if (pl != null) {
                        player = pl;
                        if (player.zone != null && player.zone.map.mapId != 0 && player.zone.map.mapId != 7 && player.zone.map.mapId != 14
                                && player.zone.map.mapId != 21 && player.zone.map.mapId != 22 && player.zone.map.mapId != 23) {
                            player.shenronEvent = this;
                            zone = player.zone;
                            player.idMark.setShenronType(shenronType);
                            isPlayerDisconnect = false;
                            reSummonShenron();
                        }
                    }
                }
                if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                    leaveMap = true;
                    NpcService.gI().createMenuRongThieng(player, ConstNpc.IGNORE_MENU, "Còn cái nịt =))\nCó không ước mất đừng tìm.", "Xin vĩnh biệt cụ........");
                    shenronLeave();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reSummonShenron() {
        activeShenron(true, DRAGON_EVENT);
        sendBlackGokuhesShenron();
    }

    public void sendBlackGokuhesShenron() {
        NpcService.gI().createMenuRongThieng(player, ConstNpc.SHOW_SHENRON_EVENT_CONFIRM,
                getShenronSay(), getShenronWishes());
    }

    private String getShenronSay() {
        return player.idMark.getShenronType() == Shenron_Service.TYPE_PUMPKIN
                ? PUMPKIN_SHENRON_SAY : SHENRONEVENT_SAY;
    }

    private String[] getShenronWishes() {
        return player.idMark.getShenronType() == Shenron_Service.TYPE_PUMPKIN
                ? PUMPKIN_SHENRON_WISHES : SHENRON_WISHES;
    }

    public void showConfirmShenron(byte select) {
        if (player.idMark.getShenronType() == Shenron_Service.TYPE_PUMPKIN) {
            if (select < 0 || select >= PUMPKIN_SHENRON_WISHES.length) {
                sendBlackGokuhesShenron();
                return;
            }
            this.select = select;
            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_EVENT_CONFIRM,
                    "Nguoi co chac muon uoc?", PUMPKIN_SHENRON_WISHES[select], "Tu choi");
            return;
        }
        this.select = select;
        String wish = null;
        switch (player.idMark.getShenronType()) {
            case 0:
                wish = SHENRON_WISHES[select];
                break;
        }
        NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_EVENT_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void activeShenron(boolean appear, byte type) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(player.zone.map.mapId);
                msg.writer().writeShort(player.zone.map.bgId);
                msg.writer().writeByte(player.zone.zoneId);
                msg.writer().writeInt((int) player.id);
                msg.writer().writeUTF("null");
                msg.writer().writeShort(player.location.x);
                msg.writer().writeShort(player.location.y);
                msg.writer().writeByte(type);
                playerId = player.id;
                shenronType = player.idMark.getShenronType();
                zone.shenronType = shenronType;
                lastTimeShenronWait = System.currentTimeMillis();
                player.isShenronAppear = true;
            }
            Service.gI().sendMessAllPlayerInMap(player, msg);
            if (!appear) {
                GlobalSkyService.gI().restoreAfterDragonLeaves();
            }
        } catch (Exception e) {
        }
    }

    public void confirmWish() {
        switch (player.idMark.getShenronType()) {
            case Shenron_Service.TYPE_PUMPKIN:
                switch (this.select) {
                    case 0:
                        if (!changePetSkill34()) {
                            return;
                        }
                        break;
                    case 1:
                        activatePumpkinDragonDameBuff();
                        break;
                    case 2:
                        activatePumpkinDragonHpKiBuff();
                        break;
                    case 3:
                        if (!givePumpkinDragonCostume()) {
                            return;
                        }
                        break;
                    default:
                        sendBlackGokuhesShenron();
                        return;
                }
                break;
            case 0:
                switch (this.select) {
                     case 0: //thay chiêu 3-4 đệ tử
                        if (player.pet != null) {
                            if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                                player.pet.openSkill3();
                                if (player.pet.playerSkill.skills.get(3).skillId != -1) {
                                    player.pet.openSkill4();
                                }
                            } else {
                                Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                                sendBlackGokuhesShenron();
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                    case 1:
                        if (player.getSession().player.nPoint.power >= 10_000_000_000L) {
                        IntrinsicService.gI().doinoitai(player);
                          } else {
                            Service.gI().sendThongBao(player, "10Tỷ Sức Mạnh?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                        
                    case 2:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            byte gender = this.player.gender;
                            Item avtVip = ItemService.gI().createNewItem((short) (gender == ConstPlayer.TRAI_DAT ? 905
                                    : gender == ConstPlayer.NAMEC ? 907 : 911));
                            avtVip.itemOptions.add(new Item.ItemOption(50, 22));
                            avtVip.itemOptions.add(new Item.ItemOption(47, 400));
                            avtVip.itemOptions.add(new Item.ItemOption(108, 30));
                            avtVip.itemOptions.add(new Item.ItemOption(33, 1));
                            avtVip.itemOptions.add(new Item.ItemOption(93, 90));
                            InventoryService.gI().addItemBag(player, avtVip);
                            InventoryService.gI().sendItemBags(player);
                        } else {
                            Service.gI().sendThongBao(player, "Hành trang đã đầy");
                            reSummonShenron();
                            return;
                        }
                        break;
                    case 3:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            byte gender = this.player.gender;
                            Item avtVip = ItemService.gI().createNewItem((short) (gender == ConstPlayer.TRAI_DAT ? 883
                                    : gender == ConstPlayer.NAMEC ? 883 : 883));
                            avtVip.itemOptions.add(new Item.ItemOption(50, 24));
                            avtVip.itemOptions.add(new Item.ItemOption(14, 3));
                            avtVip.itemOptions.add(new Item.ItemOption(103, 19));
                            avtVip.itemOptions.add(new Item.ItemOption(80, 10));
                            avtVip.itemOptions.add(new Item.ItemOption(93, 90));
                            InventoryService.gI().addItemBag(player, avtVip);
                            InventoryService.gI().sendItemBags(player);
                        } else {
                            Service.gI().sendThongBao(player, "Hành trang đã đầy");
                            reSummonShenron();
                            return;
                        }
                        break;
                    case 4:// Tăng hp, ki, sd
                        int timeRX = player.itemTime.timeRX / 1000 + 1800;
                        int maxTimeInSeconds = 32767;
                        if (timeRX >= maxTimeInSeconds) {
                            Service.gI().sendThongBao(player, "Ước ít thôi con :v");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemTime.isUseRX = true;
                        player.itemTime.timeRX = timeRX * 1000;
                        player.itemTime.lastTimeUseRX = System.currentTimeMillis();
                        ItemTimeService.gI().sendItemTime(player, 6581, timeRX);
                        player.nPoint.calPoint();
                        player.nPoint.setHp(player.nPoint.hpMax);
                        player.nPoint.setMp((int) player.nPoint.mpMax);
                        Service.gI().point(player);
                        Service.gI().Send_Info_NV(player);
                        break;
                    case 99: //quần đang đeo lên 1 cấp
                        Item item = this.player.inventory.itemsBody.get(1);
                        if (item.isNotNullItem()) {
                            int level = 0;
                            for (Item.ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id == 72) {
                                    level = io.param;
                                    if (level < 7) {
                                        io.param++;
                                    }
                                    break;
                                }
                            }
                            if (level < 7) {
                                if (level == 0) {
                                    item.itemOptions.add(new Item.ItemOption(72, 1));
                                }
                                for (Item.ItemOption io : item.itemOptions) {
                                    if (InventoryService.gI().optionCanUpgrade(io.optionTemplate.id)) {
                                        io.param += (io.param * 10 / 100);
                                    }
                                }
                                InventoryService.gI().sendItemBody(player);
                            } else {
                                Service.gI().sendThongBao(player, "Quần của ngươi đã đạt cấp tối đa");
                                sendBlackGokuhesShenron();
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi hiện tại có mang quần đâu");
                            sendBlackGokuhesShenron();
                            return;
                        }
                }
                break;
        }
        shenronLeave();
    }

    private boolean changePetSkill34() {
        if (player.pet != null) {
            if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                player.pet.openSkill3();
                if (player.pet.playerSkill.skills.get(3).skillId != -1) {
                    player.pet.openSkill4();
                }
                return true;
            }
            Service.gI().sendThongBao(player, "It nhat de tu nguoi phai co chieu 3 chu!");
            sendBlackGokuhesShenron();
            return false;
        }
        Service.gI().sendThongBao(player, "Nguoi lam gi co de tu?");
        sendBlackGokuhesShenron();
        return false;
    }

    private void activatePumpkinDragonDameBuff() {
        player.itemTime.isUsePumpkinDragonDame = true;
        player.itemTime.lastTimeUsePumpkinDragonDame = System.currentTimeMillis();
        ItemTimeService.gI().sendItemTime(player, 6581, ItemTime.TIME_PUMPKIN_DRAGON_BUFF / 1000);
        refreshPointAfterPumpkinWish(false);
        Service.gI().sendThongBao(player, "Suc danh tang 20% trong 60 phut.");
    }

    private void activatePumpkinDragonHpKiBuff() {
        player.itemTime.isUsePumpkinDragonHpKi = true;
        player.itemTime.lastTimeUsePumpkinDragonHpKi = System.currentTimeMillis();
        ItemTimeService.gI().sendItemTime(player, 6583, ItemTime.TIME_PUMPKIN_DRAGON_BUFF / 1000);
        refreshPointAfterPumpkinWish(true);
        Service.gI().sendThongBao(player, "HP va KI tang 35% trong 60 phut.");
    }

    private void refreshPointAfterPumpkinWish(boolean healFull) {
        player.nPoint.calPoint();
        if (healFull) {
            player.nPoint.setHp(player.nPoint.hpMax);
            player.nPoint.setMp((int) player.nPoint.mpMax);
        }
        Service.gI().point(player);
        Service.gI().Send_Info_NV(player);
    }

    private boolean givePumpkinDragonCostume() {
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hanh trang da day");
            reSummonShenron();
            return false;
        }
        Item costume = HalloweenRewards.createHalloweenCostumeReward(30);
        if (costume == null || !costume.isNotNullItem()) {
            Service.gI().sendThongBao(player, "Khong the tao cai trang Halloween");
            reSummonShenron();
            return false;
        }
        if (!InventoryService.gI().addItemBag(player, costume)) {
            Service.gI().sendThongBao(player, "Hanh trang da day");
            reSummonShenron();
            return false;
        }
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Ban nhan duoc " + costume.template.name);
        return true;
    }

    public void shenronLeave() {
        if (!shenronLeave) {
            shenronLeave = true;
            if (player != null && player.zone != null) {
                player.shenronEvent = null;
                if (!leaveMap) {
                    NpcService.gI().createTutorial(player, 0, "Điều ước của ngươi đã được thực hiện...tạm biệt");
                }
                activeShenron(false, DRAGON_EVENT);
                player.isShenronAppear = false;
                select = -1;
            }
            zone.shenronType = -1;
            updateLastTimeShenronAppeared();
            Shenron_Manager.gI().remove(this);
        }
    }

    private void updateLastTimeShenronAppeared() {
        if (player == null) {
            return;
        }
        if (shenronType == Shenron_Service.TYPE_PUMPKIN) {
            player.lastTimePumpkinShenronAppeared = System.currentTimeMillis();
        } else {
            player.lastTimeShenronAppeared = System.currentTimeMillis();
        }
    }
}
