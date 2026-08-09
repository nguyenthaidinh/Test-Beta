package nro.models.event;

import nro.models.consts.ConstItem;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.item.ItemTime;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services_dungeon.AncientCastleService;

public class NoiBanh extends Npc {

    private static final int DUOI_KHI_ID = 1045;
    private static final int COST_BTT1 = 50;   // 50 Đuôi Khỉ
    private static final int COST_BTT2 = 80;   // 80 Đuôi Khỉ
    private static final int COST_BTTDB = 120;  // 120 Đuôi Khỉ

    public NoiBanh(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        Item duoiKhi = InventoryService.gI().findItemBag(player, DUOI_KHI_ID);
        int soLuong = (duoiKhi != null) ? duoiKhi.quantity : 0;
        createOtherMenu(player, 0,
                "Xin chào " + player.name + "\nĐây là Nồi Bánh Trung Thu!"
                + "\nBạn đang có " + soLuong + " Đuôi Khỉ"
                + "\n\nChọn loại bánh muốn đổi:",
                "Bánh 1 Trứng\n(" + COST_BTT1 + " Đuôi Khỉ)",
                "Bánh 2 Trứng\n(" + COST_BTT2 + " Đuôi Khỉ)",
                "Bánh Đặc Biệt\n(" + COST_BTTDB + " Đuôi Khỉ)",
                "Xem hiệu ứng",
                "Muốn\nthử thách\nhả",
                "Từ chối");
    }

    @Override
    public void confirmMenu(Player pl, int select) {
        if (canOpenNpc(pl)) {
            switch (pl.idMark.getIndexMenu()) {
                case 0 -> {
                    switch (select) {
                        case 0 -> doiVaAnBanh(pl, COST_BTT1, ConstItem.BANH_TRUNG_THU_1_TRUNG,
                                "Bánh Trung Thu 1 Trứng", 1);
                        case 1 -> doiVaAnBanh(pl, COST_BTT2, ConstItem.BANH_TRUNG_THU_2_TRUNG,
                                "Bánh Trung Thu 2 Trứng", 2);
                        case 2 -> doiVaAnBanh(pl, COST_BTTDB, ConstItem.BANH_TRUNG_THU_DAC_BIET,
                                "Bánh Trung Thu Đặc Biệt", 3);
                        case 3 -> showInfo(pl);
                        case 4 -> enterAncientCastle(pl);
                        default -> {
                        }
                    }
                }
            }
        }
    }

    private void doiVaAnBanh(Player pl, int cost, int banhId, String tenBanh, int tier) {
        Item duoiKhi = InventoryService.gI().findItemBag(pl, DUOI_KHI_ID);
        if (duoiKhi == null || duoiKhi.quantity < cost) {
            Service.gI().sendThongBao(pl, "Bạn cần " + cost + " Đuôi Khỉ để đổi " + tenBanh
                    + "\n(Hiện có: " + (duoiKhi != null ? duoiKhi.quantity : 0) + ")");
            return;
        }

        // Kiểm tra đã có buff Bánh Trung Thu cùng loại chưa
        if (pl.itemTime != null) {
            if ((tier == 1 && pl.itemTime.isUseBanhTT1)
                    || (tier == 2 && pl.itemTime.isUseBanhTT2)
                    || (tier == 3 && pl.itemTime.isUseBanhTTDB)) {
                Service.gI().sendThongBao(pl, "Bạn đang có hiệu ứng " + tenBanh + " rồi!");
                return;
            }
        }

        // Trừ Đuôi Khỉ
        InventoryService.gI().subQuantityItemsBag(pl, duoiKhi, cost);
        InventoryService.gI().sendItemBags(pl);

        // Kích hoạt buff
        if (pl.itemTime == null) {
            pl.itemTime = new ItemTime(pl);
        }

        String thongBao;
        switch (tier) {
            case 1 -> {
                pl.itemTime.isUseBanhTT1 = true;
                pl.itemTime.lastTimeBanhTT1 = System.currentTimeMillis();
                thongBao = "Ăn " + tenBanh + " thành công!\n+10% Sức Đánh / HP / KI trong 60 phút";
            }
            case 2 -> {
                pl.itemTime.isUseBanhTT2 = true;
                pl.itemTime.lastTimeBanhTT2 = System.currentTimeMillis();
                thongBao = "Ăn " + tenBanh + " thành công!\n+20% Sức Đánh / HP / KI trong 90 phút";
            }
            case 3 -> {
                pl.itemTime.isUseBanhTTDB = true;
                pl.itemTime.lastTimeBanhTTDB = System.currentTimeMillis();
                thongBao = "Ăn " + tenBanh + " thành công!\n+15% Sức Đánh, +20% HP / KI trong 60 phút";
            }
            default -> thongBao = "";
        }

        Service.gI().point(pl);
        Service.gI().sendThongBao(pl, thongBao);
    }

    private void enterAncientCastle(Player pl) {
        AncientCastleService.gI().startOrRejoin(pl);
    }

    private void showInfo(Player pl) {
        String info = "|7|=== BÁNH TRUNG THU ==="
                + "\n|2|Bánh 1 Trứng (50 Đuôi Khỉ):"
                + "\n|1|  +10% SĐ / HP / KI - 60 phút"
                + "\n|2|Bánh 2 Trứng (80 Đuôi Khỉ):"
                + "\n|1|  +20% SĐ / HP / KI - 90 phút"
                + "\n|2|Bánh Đặc Biệt (120 Đuôi Khỉ):"
                + "\n|1|  +15% SĐ, +20% HP / KI - 60 phút"
                + "\n\n|7|Đuôi Khỉ drop từ Boss Khỉ Đột"
                + "\n|7|Các buff có thể dùng đồng thời!";
        Service.gI().sendThongBaoOK(pl, info);
    }
}
