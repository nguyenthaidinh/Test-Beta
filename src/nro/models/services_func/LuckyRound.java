package nro.models.services_func;

import nro.models.consts.ConstItem;
import nro.models.item.Item;
import java.io.IOException;
import java.util.ArrayList;
import nro.models.player.Player;
import nro.models.network.Message;
import nro.models.services.RewardService;
import nro.models.services.Service;
import java.util.List;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;

/**
 *
 * @author By Mr Blue
 * 
 */

public class LuckyRound {

    private static final byte MAX_ITEM_IN_BOX = 100;

    public static final byte USING_GEM = 7;
    public static final byte USING_GOLD = 0;
    public static final byte USING_GOLD_BAR = 1;
    public static final byte USING_TICKET = 2;

    private static final byte PRICE_GEM = 4;
    private static final int PRICE_GOLD = 10000000; // giảm từ 250M xuống 10M
    private static final int PRICE_GOLD_BAR = 2;
    private static final int PRICE_TICKET = 1;
    private static final int TICKET = 821;

    private static LuckyRound instance;

    public static LuckyRound gI() {
        if (instance == null) {
            instance = new LuckyRound();
        }
        return instance;
    }

    public void openCrackBallUI(Player pl, byte type) {
        pl.idMark.setTypeLuckyRound(type);
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(0);
            msg.writer().writeByte(7);
            for (int i = 0; i < 7; i++) {
                msg.writer().writeShort(419 + i);
            }
            msg.writer().writeByte(type);
            msg.writer().writeInt(getPrice(type));
            msg.writer().writeShort(getPaymentItemId(type));
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void openCrackBallVipUI(Player pl, byte type) {
        pl.idMark.setTypeLuckyRound(type);
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(0);
            msg.writer().writeByte(7);
            for (int i = 0; i < 7; i++) {
                msg.writer().writeShort(419);
            }
            msg.writer().writeByte(type);
            msg.writer().writeInt(getPrice(type));
            msg.writer().writeShort(getPaymentItemId(type));
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void readOpenBall(Player player, Message msg) {
        try {
            msg.reader().readByte();
            int count = msg.reader().readByte();
            if (count <= 0) {
                Service.gI().sendThongBao(player, "Số lần quay không hợp lệ");
                return;
            }
            switch (player.idMark.getTypeLuckyRound()) {
                case USING_GEM:
                    openBallByGem(player, count);
                    break;
                case USING_GOLD:
                    openBallByGold(player, count);
                    break;
                case USING_GOLD_BAR:
                    openBallByGoldBar(player, count);
                    break;
                case USING_TICKET:
                    openBallByTicket(player, count);
                    break;
            }
        } catch (Exception e) {
            switch (player.idMark.getTypeLuckyRound()) {
                case USING_GEM:
                case USING_GOLD_BAR:
                    openCrackBallVipUI(player, player.idMark.getTypeLuckyRound());
                    break;
                default:
                    openCrackBallUI(player, player.idMark.getTypeLuckyRound());
                    break;
            }
        }
    }

    public void openGoldBarBall(Player player, int count) {
        openBallByGoldBar(player, count);
    }

    private void openBallByGem(Player player, int count) {
        int gemNeed = (count * PRICE_GEM);
        if (player.inventory.gem < gemNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ ngọc để mở");
        } else {
            if (hasRoomInLuckyBox(player, count)) {
                player.inventory.gem -= gemNeed;
                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, true);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void openBallByGold(Player player, int count) {
        int goldNeed = (count * PRICE_GOLD);
        if (player.inventory.gold < goldNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng để mở");
        } else {
            if (hasRoomInLuckyBox(player, count)) {
                player.inventory.gold -= goldNeed;
                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, false);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void openBallByGoldBar(Player player, int count) {
        if (count <= 0) {
            Service.gI().sendThongBao(player, "Số lần quay không hợp lệ");
            return;
        }
        int goldBarNeed = count * PRICE_GOLD_BAR;
        Item goldBar = InventoryService.gI().findItemBag(player, ConstItem.THOI_VANG);
        if (goldBar == null || goldBar.quantity < goldBarNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ thỏi vàng để quay");
            sendReward(player, new ArrayList<>());
        } else {
            if (hasRoomInLuckyBox(player, count)) {
                InventoryService.gI().subQuantityItemsBag(player, goldBar, goldBarNeed);
                InventoryService.gI().sendItemBags(player);
                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, true);
                addItemToBox(player, list);
                sendReward(player, list);
                if (count >= 100) {
                    Service.gI().sendThongBao(player, "Đã quay " + count + " lần, phần thưởng đã vào rương phụ.");
                }
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void openBallByTicket(Player player, int count) {
        int ticketNeed = (count * PRICE_TICKET);
        Item ticket = InventoryService.gI().findItemBag(player, TICKET);
        if (ticket == null || ticket.quantity < ticketNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ " + ItemService.gI().createNewItem((short) TICKET).template.name + " để quay");
            sendReward(player, new ArrayList<>());
        } else {
            if (hasRoomInLuckyBox(player, count)) {
                InventoryService.gI().subQuantityItemsBag(player, ticket, ticketNeed);
                InventoryService.gI().sendItemBags(player);
                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, true);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private int getPrice(byte type) {
        switch (type) {
            case USING_GEM:
                return PRICE_GEM;
            case USING_GOLD_BAR:
                return PRICE_GOLD_BAR;
            case USING_TICKET:
                return PRICE_TICKET;
            default:
                return PRICE_GOLD;
        }
    }

    private short getPaymentItemId(byte type) {
        switch (type) {
            case USING_GOLD_BAR:
                return (short) ConstItem.THOI_VANG;
            case USING_TICKET:
                return (short) TICKET;
            default:
                return -1;
        }
    }

    private boolean hasRoomInLuckyBox(Player player, int count) {
        return count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX;
    }

    private void sendReward(Player player, List<Item> items) {
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                msg.writer().writeShort(item.template.iconID);
            }
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void addItemToBox(Player player, List<Item> items) {
        player.inventory.itemsBoxCrackBall.addAll(items);
    }
}
