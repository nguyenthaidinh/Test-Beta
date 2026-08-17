package nro.models.npc_list;

import nro.models.boss.event.Halloween.HalloweenExchangeService;
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
import nro.models.services.EventLeaderboardService;
import nro.models.services.GoldBarSpendService;
import nro.models.services.Service;
import nro.models.shop.ShopService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class ChiChi extends Npc {

    private static final String HALLOWEEN_EVENT_SHOP_TAG = "HALLOWEEN_EVENT_SHOP";
    private static final int MENU_BUY_TRAIN_ARMOR_5 = 186900;
    private static final int MENU_BUY_CLAN_CAPSULE = 186901;
    private static final int MENU_BUY_100_CLAN_CAPSULE = 186902;
    private static final short TRAIN_ARMOR_5_ITEM_ID = 1869;
    private static final long TRAIN_ARMOR_5_GOLD_COST = 36_000_000_000L;
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
                    "Cửa hàng",
                    "Giáp\nluyện tập\ncấp 5",
                    "1 Capsule\nBang\n10 TV",
                    "100 Capsule\nBang\n1K2 TV",
                    "Đóng"));

            menu.add(menu.size() - 1, "Shop\nsự kiện");
            menu.add(menu.size() - 1, "Top\nHòm\nHalloween");
            menu.add(menu.size() - 1, "Đổi quà\nHalloween");
            menu.add(menu.size() - 1, "Đổi\nKẹo\nbàn tay");
            menu.add(menu.size() - 1, "Top\nHộp Kẹo\nMa Quỷ");
            menu.add(menu.size() - 1, "Top\nCapsule\nHalloween");
            menu.add(menu.size() - 1, "Top\nTi\u00eau\nTh\u1ecfi V\u00e0ng");
            String[] menus = menu.toArray(new String[0]);

            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Bạn muốn hỏi chi?", menus);
        }

    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 5) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "SHOP_CHI_CHI", false);
                            break;
                        case 1:
                            createOtherMenu(player, MENU_BUY_TRAIN_ARMOR_5,
                                    "Con có muốn mua Giáp tập luyện cấp 5\nGiá "
                                    + Util.numberToMoney(TRAIN_ARMOR_5_GOLD_COST) + " vàng không?",
                                    "Mua", "Từ chối");
                            break;
                        case 2:
                            createOtherMenu(player, MENU_BUY_CLAN_CAPSULE,
                                    "Con có muốn mua 1 Capsule Bang cho bang hội\nvới giá 10 thỏi vàng không?",
                                    "Mua", "Từ chối");
                            break;
                        case 3:
                            createOtherMenu(player, MENU_BUY_100_CLAN_CAPSULE,
                                    "Con có muốn mua 100 Capsule Bang cho bang hội\n"
                                    + "với giá 1.200 thỏi vàng không?",
                                    "Mua", "Từ chối");
                            break;
                        case 4:
                            ShopService.gI().opendShop(player, HALLOWEEN_EVENT_SHOP_TAG, false);
                            break;
                        case 5:
                            createOtherMenu(player, ConstNpc.MENU_HALLOWEEN_BOX_TOP,
                                    "Đua top mở Hòm Halloween.\nMỗi lần mở thành công 1 Hòm Halloween sẽ được tính 1 điểm.",
                                    "Top 100\nHòm\nHalloween",
                                    "Xem điểm",
                                    "Đóng");
                            break;
                        case 6:
                            HalloweenExchangeService.openExchangeMenu(player, this);
                            break;
                        case 7:
                            HalloweenExchangeService.openHandCandyExchangeMenu(player, this);
                            break;
                        case 8:
                            createOtherMenu(player, ConstNpc.MENU_HALLOWEEN_CANDY_BOX_TOP,
                                    "Đua top mở Hộp Kẹo Ma Quỷ.\nMỗi lần mở thành công 1 Hộp Kẹo Ma Quỷ sẽ được tính 1 điểm.",
                                    "Top 100\nHộp Kẹo\nMa Quỷ",
                                    "Xem điểm",
                                    "Đóng");
                            break;
                        case 9:
                            createOtherMenu(player, ConstNpc.MENU_HALLOWEEN_CAPSULE_TOP,
                                    "Đua top mở Capsule Halloween.\nMỗi lần mở thành công 1 Capsule Halloween sẽ được tính 1 điểm.",
                                    "Top 100\nCapsule\nHalloween",
                                    "Xem điểm",
                                    "Đóng");
                            break;
                        case 10:
                            createOtherMenu(player, ConstNpc.MENU_GOLD_BAR_SPEND_TOP,
                                    "\u0110ua top ti\u00eau Th\u1ecfi V\u00e0ng.\nM\u1ed7i 1 Th\u1ecfi V\u00e0ng th\u1ef1c t\u1ebf d\u00f9ng \u0111\u1ec3 mua/b\u00e1n v\u1eadt ph\u1ea9m s\u1ebd t\u00ednh 1 \u0111i\u1ec3m.",
                                    "Top 100\nTi\u00eau\nTh\u1ecfi V\u00e0ng",
                                    "Xem \u0111i\u1ec3m",
                                    "\u0110\u00f3ng");
                            break;
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_HALLOWEEN_BOX_TOP) {
                    switch (select) {
                        case 0:
                            Service.gI().showListTopHiddenPoint(player, Manager.TopHalloweenBox);
                            break;
                        case 1:
                            if (hideTopPointForPlayer(player)) {
                                break;
                            }
                            player.point_halloween_box = Math.max(player.point_halloween_box,
                                    EventLeaderboardService.gI().getPoint(EventLeaderboardService.HALLOWEEN_BOX, player.id));
                            Service.gI().sendThongBao(player, "Bạn đã mở " + player.point_halloween_box + " Hòm Halloween.");
                            break;
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_HALLOWEEN_CAPSULE_TOP) {
                    switch (select) {
                        case 0:
                            Service.gI().showListTopHiddenPoint(player, Manager.TopHalloweenCapsule);
                            break;
                        case 1:
                            if (hideTopPointForPlayer(player)) {
                                break;
                            }
                            player.point_halloween_capsule = Math.max(player.point_halloween_capsule,
                                    EventLeaderboardService.gI().getPoint(EventLeaderboardService.HALLOWEEN_CAPSULE, player.id));
                            Service.gI().sendThongBao(player, "Bạn đã mở " + player.point_halloween_capsule + " Capsule Halloween.");
                            break;
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_HALLOWEEN_CANDY_BOX_TOP) {
                    switch (select) {
                        case 0:
                            Service.gI().showListTopHiddenPoint(player, Manager.TopHalloweenCandyBox);
                            break;
                        case 1:
                            if (hideTopPointForPlayer(player)) {
                                break;
                            }
                            player.point_halloween_candy_box = Math.max(player.point_halloween_candy_box,
                                    EventLeaderboardService.gI().getPoint(EventLeaderboardService.HALLOWEEN_CANDY_BOX, player.id));
                            Service.gI().sendThongBao(player, "Bạn đã mở " + player.point_halloween_candy_box + " Hộp Kẹo Ma Quỷ.");
                            break;
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_GOLD_BAR_SPEND_TOP) {
                    switch (select) {
                        case 0:
                            Service.gI().showListTopHiddenPoint(player, Manager.TopGoldBarSpend);
                            break;
                        case 1:
                            String point = GoldBarSpendService.gI().getPoint(player.id);
                            if (point == null) {
                                Service.gI().sendThongBao(player, "Kh\u00f4ng th\u1ec3 t\u1ea3i \u0111i\u1ec3m ti\u00eau Th\u1ecfi V\u00e0ng l\u00fac n\u00e0y.");
                                break;
                            }
                            Service.gI().sendThongBao(player, "B\u1ea1n \u0111ang c\u00f3 " + point + " \u0111i\u1ec3m ti\u00eau Th\u1ecfi V\u00e0ng.");
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
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_HALLOWEEN_EXCHANGE) {
                    HalloweenExchangeService.handleExchange(player, select);
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_HAND_CANDY_EXCHANGE) {
                    HalloweenExchangeService.handleHandCandyExchange(player, select);
                }
            }
        }
    }

    private boolean hideTopPointForPlayer(Player player) {
        if (player != null && !player.isAdmin()) {
            Service.gI().sendThongBao(player, "\u0110i\u1ec3m top \u0111ang \u0111\u01b0\u1ee3c \u1ea9n.");
            return true;
        }
        return false;
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
        GoldBarSpendService.gI().addPoint(player, cost);
        Service.gI().sendThongBao(player, "Mua thành công " + amount
                + " Capsule Bang cho bang hội với giá " + Util.numberToMoney(cost) + " thỏi vàng.");
    }

}
