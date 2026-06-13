package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.shop.ShopService;
import nro.models.services_func.Input;

public class Santa extends Npc {

    public Santa(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {

            Item pGG = InventoryService.gI().findItem(player.inventory.itemsBag, 459);
            int soLuong = 0;
            if (pGG != null) {
                soLuong = pGG.quantity;
            }
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Mua CT\nLio đẹp trai",
                    "Cửa hàng",
                    "Mở rộng\nHành trang\nRương đồ",
                    "Nhập mã\nquà tặng",
                    "Cửa hàng\nHạn sử dụng",
                    "Tiệm\nHớt tóc",
                    "Danh\nhiệu"));

            if (soLuong >= 1) {
                menu.add(2, "Giảm giá\n80%");
            }

            String[] menus = menu.toArray(new String[0]);

            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Xin chào, ta có một số vật phẩm đặc biệt cậu có muốn xem không?"
                    + "\n\n|7|HOT: CT Lio đẹp trai - 1.000.000 ngọc"
                    + "\n|2|+30% SĐ | +50% HP | +50% KI"
                    + "\n|2|+20% SĐCM | +5% HP/30s"
                    + "\n|2|+25% SĐ cho người xung quanh", menus);
        }

    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            Item pGG = InventoryService.gI().findItem(player.inventory.itemsBag, 459);
            int soLuong = 0;
            if (pGG != null) {
                soLuong = pGG.quantity;
            }

            if (this.mapId == 5 || this.mapId == 13 || this.mapId == 20) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0:
                            muaCTLioDepTrai(player);
                            break;
                        case 1:
                            ShopService.gI().opendShop(player, "SANTA", false);
                            break;
                        case 2:
                            if (soLuong >= 1) {
                                ShopService.gI().opendShop(player, "SANTA_GIAM_GIA_1", false);
                            } else {
                                ShopService.gI().opendShop(player, "SANTA_MO_RONG_HANH_TRANG", false);
                            }
                            break;
                        case 3:
                            if (soLuong >= 1) {
                                ShopService.gI().opendShop(player, "SANTA_MO_RONG_HANH_TRANG", false);
                            } else {
                                Input.gI().createFormGiftCode(player);
                            }
                            break;
                        case 4:
                            if (soLuong >= 1) {
                                Input.gI().createFormGiftCode(player);
                            } else {
                                ShopService.gI().opendShop(player, "SANTA_HAN_SU_DUNG", false);
                            }
                            break;
                        case 5:
                            if (soLuong >= 1) {
                                ShopService.gI().opendShop(player, "SANTA_HAN_SU_DUNG", false);
                            } else {
                                ShopService.gI().opendShop(player, "SANTA_HEAD", false);
                            }
                            break;
                        case 6:
                            if (soLuong >= 1) {
                                ShopService.gI().opendShop(player, "SANTA_HEAD", false);
                            } else {
                                ShopService.gI().opendShop(player, "SANTA_DANH_HIEU", false);
                            }
                            break;
                        case 7:
                            if (soLuong >= 1) {
                                ShopService.gI().opendShop(player, "SANTA_DANH_HIEU", false);
                            } else {
                                ShopService.gI().opendShop(player, "SHOP_VIP", false);
                            }
                            break;
                    }
                }
            }
        }
    }

    private void muaCTLioDepTrai(Player player) {
        int GIA_NGOC = 1_000_000;

        // Kiểm tra đã có chưa
        Item existing = InventoryService.gI().findItemBag(player, 1815);
        if (existing != null) {
            Service.gI().sendThongBao(player, "Bạn đã sở hữu CT Lio đẹp trai rồi!");
            return;
        }

        // Kiểm tra hành trang
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy!");
            return;
        }

        // Kiểm tra ngọc
        if (player.inventory.gem < GIA_NGOC) {
            Service.gI().sendThongBao(player, "Bạn không đủ ngọc!"
                    + "\nCần: " + GIA_NGOC + " ngọc"
                    + "\nHiện có: " + player.inventory.gem + " ngọc");
            return;
        }

        // Trừ ngọc
        player.inventory.gem -= GIA_NGOC;

        // Tạo CT Lio đẹp trai
        Item ct = ItemService.gI().createNewItem((short) 1815);
        ct.itemOptions.clear();
        ct.itemOptions.add(new Item.ItemOption(30, 30));  // SĐ +30%
        ct.itemOptions.add(new Item.ItemOption(77, 50));  // HP +50%
        ct.itemOptions.add(new Item.ItemOption(103, 50)); // KI +50%
        ct.itemOptions.add(new Item.ItemOption(50, 20));  // SĐCM +20%
        ct.itemOptions.add(new Item.ItemOption(173, 5));  // Hồi 5% HP/30s

        InventoryService.gI().addItemBag(player, ct);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "|7|Mua CT Lio đẹp trai thành công!"
                + "\n|2|+30% Sức Đánh"
                + "\n|2|+50% HP"
                + "\n|2|+50% KI"
                + "\n|2|+20% Sức Đánh Chí Mạng"
                + "\n|2|+5% HP hồi/30s"
                + "\n|2|+25% SĐ cho người xung quanh"
                + "\n\n|1|Chỉ có tác dụng khi hợp thể!");
    }
}
