package nro.models.npc_list;

import nro.models.clan.ClanMember;
import nro.models.consts.ConstItem;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.server.Manager;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services_func.Input;
import nro.models.shop.ShopService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class ChiChi extends Npc {

    private static final int MENU_BUY_TRAIN_ARMOR_5 = 186900;
    private static final int MENU_BUY_CLAN_CAPSULE = 186901;
    private static final int MENU_BUY_100_CLAN_CAPSULE = 186902;
    private static final int MENU_BUY_GOLD_BAR = 186903;
    private static final short TRAIN_ARMOR_5_ITEM_ID = 1869;
    private static final long TRAIN_ARMOR_5_GOLD_COST = 36_000_000_000L;
    private static final long GOLD_BAR_COST = 50_000_000L;
    private static final int CLAN_CAPSULE_AMOUNT = 1;
    private static final int CLAN_CAPSULE_COST_TV = 10;
    private static final int CLAN_CAPSULE_BULK_AMOUNT = 100;
    private static final int CLAN_CAPSULE_BULK_COST_TV = 1_200;

    public ChiChi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Top\nHộp quà\nthiếu nhi\n2025",
                    "Top\nNước mía",
                    "Top\nKem trái cây",
                    "Cửa hàng",
                    "Giáp\nluyện tập\ncấp 5",
                    "1 Capsule\nBang\n10 TV",
                    "100 Capsule\nBang\n1K2 TV",
                    "Mua 1\nthỏi vàng\n50tr vàng",
                    "Đóng"));

            String[] menus = menu.toArray(new String[0]);

            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Bạn muốn hỏi chi?", menus);
        }

    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            int soLuong = 0;
            if (this.mapId == 5) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 3:
                            ShopService.gI().opendShop(player, "SHOP_CHI_CHI", false);
                            break;
                        case 4:
                            createOtherMenu(player, MENU_BUY_TRAIN_ARMOR_5,
                                    "Con có muốn mua Giáp tập luyện cấp 5\nGiá "
                                    + Util.numberToMoney(TRAIN_ARMOR_5_GOLD_COST) + " vàng không?",
                                    "Mua", "Từ chối");
                            break;
                        case 5:
                            createOtherMenu(player, MENU_BUY_CLAN_CAPSULE,
                                    "Con có muốn mua 1 Capsule Bang cho bang hội\nvới giá 10 thỏi vàng không?",
                                    "Mua", "Từ chối");
                            break;
                        case 6:
                            createOtherMenu(player, MENU_BUY_100_CLAN_CAPSULE,
                                    "Con có muốn mua 100 Capsule Bang cho bang hội\n"
                                    + "với giá 1.200 thỏi vàng không?",
                                    "Mua", "Từ chối");
                            break;
                        case 7:
                            createOtherMenu(player, MENU_BUY_GOLD_BAR,
                                    "Con có muốn mua 1 thỏi vàng với giá 50 triệu vàng không?",
                                    "Mua", "Từ chối");
                            break;
                        case 0:
                            createOtherMenu(player, ConstNpc.PHAO_BONG_VIP,
                                    "Sự kiện đua top Hộp quà thiếu nhi nhận quà khủng\n Kết thúc và trao giải sau (....)\nHạn chót nhận giải: (15 ngày nữa)\nĐến gặp ChiChi để nhận giải nhé\nChi tiết xem tại diễn đàn, Fanpage",
                                    "Top 100\nHộp quà\nthiếu nhi\n2025",
                                    "Xem điểm",
                                    "Đóng");
                            break;
                        case 1:
                            createOtherMenu(player, ConstNpc.PHAO_BONG,
                                    "Sự kiện đua top Nước mía nhận quà khủng\n Kết thúc và trao giải sau (....)\nHạn chót nhận giải: (15 ngày nữa)\nĐến gặp ChiChi để nhận giải nhé\nChi tiết xem tại diễn đàn, Fanpage",
                                    "Top 100\nNước mía",
                                    "Xem điểm",
                                    "Đóng");
                            break;
                        case 2:
                            createOtherMenu(player, ConstNpc.GOKU_DAY,
                                    "Sự kiện đua top Kem trái cây nhận quà khủng\n Kết thúc và trao giải sau (....)\nHạn chót nhận giải: (15 ngày nữa)\nĐến gặp ChiChi để nhận giải nhé\nChi tiết xem tại diễn đàn, Fanpage",
                                    "Top 100\nKem trái cây",
                                    "Xem điểm",
                                    "Đóng");
                            break;
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.PHAO_BONG_VIP) {
                    switch (select) {
                        case 0:
                            Service.gI().showListTop(player, Manager.Topsukien);
                            break;
                        case 1:
                            Service.gI().sendThongBao(player, "Bạn có " + player.point_sukien + " điểm Hộp quà thiếu nhi.");
                            break;
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.PHAO_BONG) {
                    switch (select) {
                        case 0:
                            Service.gI().showListTop(player, Manager.Topsukien1);
                            break;
                        case 1:
                            Service.gI().sendThongBao(player, "Bạn có " + player.point_sukien1 + " điểm Nước mía.");
                            break;
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.GOKU_DAY) {
                    switch (select) {
                        case 0:
                            Service.gI().showListTop(player, Manager.Topsukien2);
                            break;
                        case 1:
                            Service.gI().sendThongBao(player, "Bạn có " + player.point_sukien2 + " điểm Kem trái cây.");
                            break;
                    }
                } else if (player.idMark.getIndexMenu() == MENU_BUY_TRAIN_ARMOR_5) {
                    if (select == 0) {
                        buyTrainArmor5(player);
                    }
                } else if (player.idMark.getIndexMenu() == MENU_BUY_CLAN_CAPSULE) {
                    if (select == 0) {
                        buyClanCapsule(player, CLAN_CAPSULE_AMOUNT, CLAN_CAPSULE_COST_TV);
                    }
                } else if (player.idMark.getIndexMenu() == MENU_BUY_100_CLAN_CAPSULE) {
                    if (select == 0) {
                        buyClanCapsule(player, CLAN_CAPSULE_BULK_AMOUNT, CLAN_CAPSULE_BULK_COST_TV);
                    }
                } else if (player.idMark.getIndexMenu() == MENU_BUY_GOLD_BAR) {
                    if (select == 0) {
                        buyGoldBar(player);
                    }
                }
            }
        }
    }

    private void buyTrainArmor5(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy");
            return;
        }
        if (player.inventory.gold < TRAIN_ARMOR_5_GOLD_COST) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
                    + Util.numberToMoney(TRAIN_ARMOR_5_GOLD_COST - player.inventory.gold) + " vàng");
            return;
        }
        Item trainArmor = ItemService.gI().createNewItem(TRAIN_ARMOR_5_ITEM_ID);
        if (trainArmor.template == null) {
            Service.gI().sendThongBao(player, "Vật phẩm không tồn tại");
            return;
        }
        trainArmor.itemOptions.add(new Item.ItemOption(77, 15));
        trainArmor.itemOptions.add(new Item.ItemOption(103, 15));
        trainArmor.itemOptions.add(new Item.ItemOption(9, 0));
        if (!InventoryService.gI().addItemBag(player, trainArmor)) {
            return;
        }
        player.inventory.gold -= TRAIN_ARMOR_5_GOLD_COST;
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "Mua thành công " + trainArmor.template.name);
    }

    private void buyClanCapsule(Player player, int amount, int cost) {
        if (player.clan == null) {
            Service.gI().sendThongBao(player, "Bạn cần có bang hội để mua Capsule Bang.");
            return;
        }
        Item thoiVang = InventoryService.gI().findItemBag(player, ConstItem.THOI_VANG);
        int currentQuantity = thoiVang == null ? 0 : thoiVang.quantity;
        if (currentQuantity < cost) {
            Service.gI().sendThongBao(player, "Bạn không đủ thỏi vàng, còn thiếu "
                    + (cost - currentQuantity) + " thỏi vàng.");
            return;
        }

        InventoryService.gI().subQuantityItemsBag(player, thoiVang, cost);
        player.clan.capsuleClan += amount;
        ClanMember member = player.clan.getClanMember((int) player.id);
        if (member != null) {
            member.memberPoint += amount;
            member.clanPoint += amount;
        }
        InventoryService.gI().sendItemBags(player);
        player.clan.sendMyClanForAllMember();
        Service.gI().sendThongBao(player, "Mua thành công " + amount
                + " Capsule Bang cho bang hội với giá " + Util.numberToMoney(cost) + " thỏi vàng.");
    }

    private void buyGoldBar(Player player) {
        if (player.inventory.gold < GOLD_BAR_COST) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
                    + Util.numberToMoney(GOLD_BAR_COST - player.inventory.gold) + " vàng.");
            return;
        }

        Item goldBar = ItemService.gI().createNewItem((short) ConstItem.THOI_VANG, 1);
        if (goldBar.template == null) {
            Service.gI().sendThongBao(player, "Vật phẩm không tồn tại.");
            return;
        }
        if (!InventoryService.gI().addItemBag(player, goldBar)) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }

        player.inventory.gold -= GOLD_BAR_COST;
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "Mua thành công 1 thỏi vàng với giá 50 triệu vàng.");
    }
}
